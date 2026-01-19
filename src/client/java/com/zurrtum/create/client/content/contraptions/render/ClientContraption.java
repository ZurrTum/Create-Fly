package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.TagValueInput;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

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

    protected final Contraption contraption;
    private int structureVersion = 0;
    private int childrenVersion = 0;

    public ClientContraption(Contraption contraption) {
        var level = contraption.entity.level();
        this.contraption = contraption;

        BlockPos origin = contraption.anchor;
        int minY = VirtualRenderWorld.nextMultipleOf16(Mth.floor(contraption.bounds.minY - 1));
        int height = VirtualRenderWorld.nextMultipleOf16(Mth.ceil(contraption.bounds.maxY + 1)) - minY;
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
        for (ChunkSectionLayer renderType : ChunkSectionLayer.values()) {
            SuperByteBufferCache.getInstance().invalidate(ContraptionEntityRenderer.CONTRAPTION, Pair.of(contraption, renderType));
        }

        structureVersion++;
    }

    private void setupRenderLevelAndRenderedBlockEntities() {
        for (StructureBlockInfo info : contraption.getBlocks().values()) {
            renderLevel.setBlock(info.pos(), info.state(), 0);

            BlockEntity blockEntity = readBlockEntity(renderLevel, info, contraption.getIsLegacy().getBoolean(info.pos()));

            if (blockEntity != null) {
                renderLevel.setBlockEntity(blockEntity);

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
    public BlockEntity readBlockEntity(Level level, StructureBlockInfo info, boolean legacy) {
        BlockState state = info.state();
        BlockPos pos = info.pos();
        CompoundTag nbt = info.nbt();

        if (legacy) {
            // for contraptions that were assembled pre-updateTags, we need to use the old strategy.
            if (nbt == null)
                return null;

            nbt.putInt("x", pos.getX());
            nbt.putInt("y", pos.getY());
            nbt.putInt("z", pos.getZ());

            BlockEntity be = BlockEntity.loadStatic(pos, state, nbt, level.registryAccess());
            postprocessReadBlockEntity(level, be, state);
            return be;
        }

        if (!state.hasBlockEntity() || !(state.getBlock() instanceof EntityBlock entityBlock))
            return null;

        BlockEntity be = entityBlock.newBlockEntity(pos, state);
        postprocessReadBlockEntity(level, be, state);
        if (be != null && nbt != null) {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(be.problemPath(), LOGGER)) {
                be.loadWithComponents(TagValueInput.create(logging, level.registryAccess(), nbt));
            }
        }

        return be;
    }

    @SuppressWarnings("deprecation")
    protected static void postprocessReadBlockEntity(Level level, @Nullable BlockEntity be, BlockState blockState) {
        if (be != null) {
            be.setLevel(level);
            be.setBlockState(blockState);
            if (be instanceof KineticBlockEntity kbe) {
                kbe.setSpeed(0);
            }
        }
    }

    public VirtualRenderWorld getRenderLevel() {
        return renderLevel;
    }

    public RenderedBlocks getRenderedBlocks() {
        return new RenderedBlocks(
            pos -> {
                StructureBlockInfo info = contraption.getBlocks().get(pos);
                if (info == null) {
                    return Blocks.AIR.defaultBlockState();
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