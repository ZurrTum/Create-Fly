package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.zurrtum.create.Create.LOGGER;

public class ClientContraption {

    private final VirtualRenderWorld renderLevel;
    /**
     * The block entities that should be rendered.
     * This will exclude e.g. drills and deployers which are rendered in contraptions as actors.
     * All block entities are created with {@link #renderLevel} as their level.
     */
    private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
    public final List<BlockEntity> renderedBlockEntityView = Collections.unmodifiableList(renderedBlockEntities);

    // Parallel array to renderedBlockEntities, true if the block entity should be rendered.
    public final BitSet shouldRenderBlockEntities = new BitSet();
    // Parallel array to renderedBlockEntities. Scratch space for marking block entities that errored during rendering.
    public final BitSet scratchErroredBlockEntities = new BitSet();

    private final ContraptionMatrices matrices = new ContraptionMatrices();
    protected final Contraption contraption;
    private int structureVersion = 0;
    private int childrenVersion = 0;

    public ClientContraption(Contraption contraption) {
        var level = contraption.entity.getWorld();
        this.contraption = contraption;

        BlockPos origin = contraption.anchor;
        int minY = VirtualRenderWorld.nextMultipleOf16(MathHelper.floor(contraption.bounds.minY - 1));
        int height = VirtualRenderWorld.nextMultipleOf16(MathHelper.ceil(contraption.bounds.maxY + 1)) - minY;
        renderLevel = new VirtualRenderWorld(level, minY, height, origin, this::invalidateStructure) {
            @Override
            public boolean supportsVisualization() {
                return VisualizationManager.supportsVisualization(level);
            }
        };

        setupRenderLevelAndRenderedBlockEntities();
    }

    public Contraption getContraption() {
        return contraption;
    }

    /**
     * A version integer incremented each time the render level changes.
     */
    public int structureVersion() {
        return structureVersion;
    }

    public int childrenVersion() {
        return childrenVersion;
    }

    public void resetRenderLevel() {
        renderedBlockEntities.clear();
        renderLevel.clear();
        shouldRenderBlockEntities.clear();

        setupRenderLevelAndRenderedBlockEntities();

        invalidateStructure();
        invalidateChildren();
    }

    public void invalidateChildren() {
        childrenVersion++;
    }

    public void invalidateStructure() {
        for (BlockRenderLayer renderType : BlockRenderLayer.values()) {
            SuperByteBufferCache.getInstance().invalidate(ContraptionEntityRenderer.CONTRAPTION, Pair.of(contraption, renderType));
        }

        structureVersion++;
    }

    private void setupRenderLevelAndRenderedBlockEntities() {
        for (StructureBlockInfo info : contraption.getBlocks().values()) {
            renderLevel.setBlockState(info.pos(), info.state(), 0);

            BlockEntity blockEntity = readBlockEntity(renderLevel, info, contraption.getIsLegacy().getBoolean(info.pos()));

            if (blockEntity != null) {
                renderLevel.addBlockEntity(blockEntity);

                // Don't render block entities that have an actor renderer registered in the MovementBehaviour.
                MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(info.state());
                if (movementBehaviour == null || !movementBehaviour.disableBlockEntityRendering()) {
                    renderedBlockEntities.add(blockEntity);
                }
            }
        }

        shouldRenderBlockEntities.set(0, renderedBlockEntities.size());

        renderLevel.runLightEngine();
    }

    @Nullable
    public BlockEntity readBlockEntity(World level, StructureBlockInfo info, boolean legacy) {
        BlockState state = info.state();
        BlockPos pos = info.pos();
        NbtCompound nbt = info.nbt();

        if (legacy) {
            // for contraptions that were assembled pre-updateTags, we need to use the old strategy.
            if (nbt == null)
                return null;

            nbt.putInt("x", pos.getX());
            nbt.putInt("y", pos.getY());
            nbt.putInt("z", pos.getZ());

            BlockEntity be = BlockEntity.createFromNbt(pos, state, nbt, level.getRegistryManager());
            postprocessReadBlockEntity(level, be, state);
            return be;
        }

        if (!state.hasBlockEntity() || !(state.getBlock() instanceof BlockEntityProvider entityBlock))
            return null;

        BlockEntity be = entityBlock.createBlockEntity(pos, state);
        postprocessReadBlockEntity(level, be, state);
        if (be != null && nbt != null) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(be.getReporterContext(), LOGGER)) {
                be.read(NbtReadView.create(logging, level.getRegistryManager(), nbt));
            }
        }

        return be;
    }

    @SuppressWarnings("deprecation")
    protected static void postprocessReadBlockEntity(World level, @Nullable BlockEntity be, BlockState blockState) {
        if (be != null) {
            be.setWorld(level);
            be.setCachedState(blockState);
            if (be instanceof KineticBlockEntity kbe) {
                kbe.setSpeed(0);
            }
        }
    }

    public VirtualRenderWorld getRenderLevel() {
        return renderLevel;
    }

    public ContraptionMatrices getMatrices() {
        return matrices;
    }

    public RenderedBlocks getRenderedBlocks() {
        return new RenderedBlocks(
            pos -> {
                StructureBlockInfo info = contraption.getBlocks().get(pos);
                if (info == null) {
                    return Blocks.AIR.getDefaultState();
                }
                return info.state();
            }, contraption.getBlocks().keySet()
        );
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos localPos) {
        return renderLevel.getBlockEntity(localPos);
    }

    /**
     * Get the BitSet marking which block entities should be rendered, potentially with additional filtering.
     *
     * <p>Implementors: DO NOT modify {@link #shouldRenderBlockEntities} directly.
     */
    public BitSet getAndAdjustShouldRenderBlockEntities() {
        return shouldRenderBlockEntities;
    }

    public record RenderedBlocks(Function<BlockPos, BlockState> lookup, Iterable<BlockPos> positions) {
    }

    /**
     * Entirely reset the client contraption, rebuilding the client level and re-running light updates.
     */
    @SuppressWarnings("unchecked")
    public static void resetClientContraption(Contraption contraption) {
        AtomicReference<ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
        var maybeNullClientContraption = clientContraption.getAcquire();

        // Nothing to invalidate if it hasn't been created yet.
        if (maybeNullClientContraption != null) {
            maybeNullClientContraption.resetRenderLevel();
        }
    }

    /**
     * Invalidate the structure of the client contraption, triggering a rebuild of the main mesh.
     */
    @SuppressWarnings("unchecked")
    public static void invalidateClientContraptionStructure(Contraption contraption) {
        AtomicReference<ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
        var maybeNullClientContraption = clientContraption.getAcquire();

        // Nothing to invalidate if it hasn't been created yet.
        if (maybeNullClientContraption != null) {
            maybeNullClientContraption.invalidateStructure();
        }
    }

    /**
     * Invalidate the children of the client contraption, triggering a rebuild of all child visuals.
     */
    @SuppressWarnings("unchecked")
    public static void invalidateClientContraptionChildren(Contraption contraption) {
        AtomicReference<ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
        var maybeNullClientContraption = clientContraption.getAcquire();

        // Nothing to invalidate if it hasn't been created yet.
        if (maybeNullClientContraption != null) {
            maybeNullClientContraption.invalidateChildren();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static BlockEntity getBlockEntityClientSide(Contraption contraption, BlockPos localPos) {
        AtomicReference<ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
        var maybeNullClientContraption = clientContraption.getAcquire();

        if (maybeNullClientContraption == null) {
            return null;
        }

        return maybeNullClientContraption.getBlockEntity(localPos);
    }
}