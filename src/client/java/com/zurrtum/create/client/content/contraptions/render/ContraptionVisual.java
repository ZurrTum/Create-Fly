package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption.RenderedBlocks;
import com.zurrtum.create.client.flywheel.api.material.CardinalLightingMode;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualEmbedding;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizerRegistry;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.baked.BlockModelBuilder;
import com.zurrtum.create.client.flywheel.lib.task.ForEachPlan;
import com.zurrtum.create.client.flywheel.lib.task.NestedPlan;
import com.zurrtum.create.client.flywheel.lib.task.PlanMap;
import com.zurrtum.create.client.flywheel.lib.task.RunnablePlan;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractEntityVisual;
import com.zurrtum.create.client.foundation.utility.worldWrappers.WrappedBlockAndTintGetter;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockRenderView;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ContraptionVisual<E extends AbstractContraptionEntity> extends AbstractEntityVisual<E> implements DynamicVisual, TickableVisual, ShaderLightVisual {
    protected static final int DEFAULT_LIGHT_PADDING = 1;

    protected final VisualEmbedding embedding;
    protected final List<BlockEntityVisual<?>> children = new ArrayList<>();
    protected final List<ActorVisual> actors = new ArrayList<>();
    protected final PlanMap<DynamicVisual, DynamicVisual.Context> dynamicVisuals = new PlanMap<>();
    protected final PlanMap<TickableVisual, TickableVisual.Context> tickableVisuals = new PlanMap<>();
    protected TransformedInstance structure;
    protected SectionCollector sectionCollector;
    protected long minSection, maxSection;
    /// The number of blocks around the contraption's bounding box to include when capturing sections for shader light.
    protected int lightPaddingBlocks = DEFAULT_LIGHT_PADDING;

    protected int lastStructureVersion;
    protected int lastVersionChildren;

    private final MatrixStack contraptionMatrix = new MatrixStack();

    public ContraptionVisual(VisualizationContext ctx, E entity, float partialTick) {
        super(ctx, entity, partialTick);
        embedding = ctx.createEmbedding(Vec3i.ZERO);

        setEmbeddingMatrices(partialTick);

        Contraption contraption = entity.getContraption();
        // The contraption could be null if it wasn't synced (ex. too much data)
        if (contraption == null)
            return;

        var clientContraption = getOrCreateClientContraptionLazy(contraption);

        setupStructure(clientContraption);
        setupChildren(contraption, clientContraption, partialTick);
    }

    private void setupStructure(ClientContraption clientContraption) {
        var renderLevel = clientContraption.getRenderLevel();

        RenderedBlocks blocks = clientContraption.getRenderedBlocks();
        // Must wrap the render level so that the differences between the contraption's actual structure and the rendered blocks are accounted for in e.g. ambient occlusion.
        BlockRenderView modelWorld = new WrappedBlockAndTintGetter(renderLevel) {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                return blocks.lookup().apply(pos);
            }
        };

        var model = new BlockModelBuilder(modelWorld, blocks.positions()).materialFunc((renderType, shaded) -> {
            Material material = ModelUtil.getMaterial(renderType, shaded);
            if (material != null && material.cardinalLightingMode() == CardinalLightingMode.ENTITY) {
                return SimpleMaterial.builderOf(material).cardinalLightingMode(CardinalLightingMode.CHUNK).build();
            } else {
                return material;
            }
        }).build();

        var instancer = embedding.instancerProvider().instancer(InstanceTypes.TRANSFORMED, model);

        // Null in ctor, so we need to create it
        // But we can steal it if it already exists
        if (structure == null) {
            structure = instancer.createInstance();
        } else {
            instancer.stealInstance(structure);
        }

        structure.setChanged();

        lastStructureVersion = clientContraption.structureVersion();
    }

    @SuppressWarnings("unchecked")
    public ClientContraption getOrCreateClientContraptionLazy(Contraption contraption) {
        AtomicReference<ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
        var out = clientContraption.getAcquire();
        if (out == null) {
            // Another thread may hit this block in the same moment.
            // One thread will win and the ContraptionRenderInfo that
            // it generated will become canonical. It's important that
            // we only maintain one RenderInfo instance, specifically
            // for the VirtualRenderWorld inside.
            clientContraption.compareAndExchangeRelease(null, createClientContraption(contraption));

            // Must get again to ensure we have the canonical instance.
            out = clientContraption.getAcquire();
        }
        return out;
    }

    protected ClientContraption createClientContraption(Contraption contraption) {
        return new ClientContraption(contraption);
    }

    private void setupChildren(Contraption contraption, ClientContraption clientContraption, float partialTick) {
        // Setup child visuals.
        children.forEach(BlockEntityVisual::delete);
        children.clear();
        dynamicVisuals.clear();
        tickableVisuals.clear();
        for (BlockEntity be : clientContraption.renderedBlockEntityView) {
            setupVisualizer(be, partialTick);
        }

        var renderLevel = clientContraption.getRenderLevel();

        // Setup actor visuals.
        actors.forEach(ActorVisual::delete);
        actors.clear();
        for (var actor : contraption.getActors()) {
            setupActor(actor, renderLevel);
        }

        lastVersionChildren = clientContraption.childrenVersion();
    }

    @SuppressWarnings("unchecked")
    protected <T extends BlockEntity> void setupVisualizer(T be, float partialTicks) {
        BlockEntityVisualizer<? super T> visualizer = (BlockEntityVisualizer<? super T>) VisualizerRegistry.getVisualizer(be.getType());
        if (visualizer == null) {
            return;
        }

        BlockEntityVisual<? super T> visual = visualizer.createVisual(this.embedding, be, partialTicks);

        children.add(visual);

        if (visual instanceof DynamicVisual dynamic) {
            dynamicVisuals.add(dynamic, dynamic.planFrame());
        }

        if (visual instanceof TickableVisual tickable) {
            tickableVisuals.add(tickable, tickable.planTick());
        }
    }

    protected void setupActor(MutablePair<StructureTemplate.StructureBlockInfo, MovementContext> actor, VirtualRenderWorld renderLevel) {
        MovementContext context = actor.getRight();
        if (context == null) {
            return;
        }
        if (context.world == null) {
            context.world = level;
        }

        StructureTemplate.StructureBlockInfo blockInfo = actor.getLeft();

        MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(blockInfo.state());
        if (movementBehaviour == null || movementBehaviour.attachRender == null) {
            return;
        }
        MovementRenderBehaviour render = (MovementRenderBehaviour) movementBehaviour.attachRender;
        var visual = render.createVisual(this.embedding, renderLevel, context);

        if (visual == null) {
            return;
        }

        actors.add(visual);
    }

    @Override
    public Plan<TickableVisual.Context> planTick() {
        return NestedPlan.of(ForEachPlan.of(() -> actors, ActorVisual::tick), tickableVisuals);
    }

    @Override
    public Plan<DynamicVisual.Context> planFrame() {
        // Must run beginFrame first to ensure changes to child visuals are picked up.
        return RunnablePlan.of(this::beginFrame).then(NestedPlan.of(ForEachPlan.of(() -> actors, ActorVisual::beginFrame), dynamicVisuals));
    }

    protected void beginFrame(DynamicVisual.Context context) {
        var partialTick = context.partialTick();
        setEmbeddingMatrices(partialTick);

        checkAndUpdateLightSections();

        var contraption = entity.getContraption();
        var clientContraption = getOrCreateClientContraptionLazy(contraption);
        if (this.lastStructureVersion != clientContraption.structureVersion()) {
            // The contraption has changed, we need to set up everything again.
            setupStructure(clientContraption);
        }

        if (this.lastVersionChildren != clientContraption.childrenVersion()) {
            setupChildren(contraption, clientContraption, partialTick);
        }
    }

    private void setEmbeddingMatrices(float partialTick) {
        var origin = renderOrigin();
        double x;
        double y;
        double z;
        if (entity.isPrevPosInvalid()) {
            // When the visual is created the entity's old position is often zero
            x = entity.getX() - origin.getX();
            y = entity.getY() - origin.getY();
            z = entity.getZ() - origin.getZ();

        } else {
            x = MathHelper.lerp(partialTick, entity.lastX, entity.getX()) - origin.getX();
            y = MathHelper.lerp(partialTick, entity.lastY, entity.getY()) - origin.getY();
            z = MathHelper.lerp(partialTick, entity.lastZ, entity.getZ()) - origin.getZ();
        }

        contraptionMatrix.loadIdentity();
        contraptionMatrix.translate(x, y, z);
        transform(contraptionMatrix, partialTick);

        embedding.transforms(contraptionMatrix.peek().getPositionMatrix(), contraptionMatrix.peek().getNormalMatrix());
    }

    public void transform(MatrixStack contraptionMatrix, float partialTick) {
    }

    @Override
    public void setSectionCollector(SectionCollector collector) {
        this.sectionCollector = collector;
        checkAndUpdateLightSections();
    }

    private void checkAndUpdateLightSections() {
        var boundingBox = entity.getBoundingBox();

        var minSectionX = ChunkSectionPos.getSectionCoord(MathHelper.floor(boundingBox.minX) - lightPaddingBlocks);
        var minSectionY = ChunkSectionPos.getSectionCoord(MathHelper.floor(boundingBox.minY) - lightPaddingBlocks);
        var minSectionZ = ChunkSectionPos.getSectionCoord(MathHelper.floor(boundingBox.minZ) - lightPaddingBlocks);
        int maxSectionX = ChunkSectionPos.getSectionCoord(MathHelper.ceil(boundingBox.maxX) + lightPaddingBlocks);
        int maxSectionY = ChunkSectionPos.getSectionCoord(MathHelper.ceil(boundingBox.maxY) + lightPaddingBlocks);
        int maxSectionZ = ChunkSectionPos.getSectionCoord(MathHelper.ceil(boundingBox.maxZ) + lightPaddingBlocks);

        if (minSection == ChunkSectionPos.asLong(minSectionX, minSectionY, minSectionZ) && maxSection == ChunkSectionPos.asLong(
            maxSectionX,
            maxSectionY,
            maxSectionZ
        )) {
            return;
        }

        minSection = ChunkSectionPos.asLong(minSectionX, minSectionY, minSectionZ);
        maxSection = ChunkSectionPos.asLong(maxSectionX, maxSectionY, maxSectionZ);

        LongSet longSet = new LongArraySet();

        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int y = minSectionY; y <= maxSectionY; y++) {
                for (int z = minSectionZ; z <= maxSectionZ; z++) {
                    longSet.add(ChunkSectionPos.asLong(x, y, z));
                }
            }
        }

        sectionCollector.sections(longSet);
    }

    @Override
    protected void _delete() {
        children.forEach(BlockEntityVisual::delete);

        actors.forEach(ActorVisual::delete);

        if (structure != null) {
            structure.delete();
        }

        embedding.delete();
    }
}
