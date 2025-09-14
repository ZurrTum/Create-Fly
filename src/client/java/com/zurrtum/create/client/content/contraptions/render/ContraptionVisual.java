package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.flywheel.api.material.CardinalLightingMode;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visual.*;
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
import com.zurrtum.create.content.contraptions.Contraption.RenderedBlocks;
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
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class ContraptionVisual<E extends AbstractContraptionEntity> extends AbstractEntityVisual<E> implements DynamicVisual, TickableVisual, LightUpdatedVisual, ShaderLightVisual {
    protected static final int LIGHT_PADDING = 1;

    protected final VisualEmbedding embedding;
    protected final List<BlockEntityVisual<?>> children = new ArrayList<>();
    protected final List<ActorVisual> actors = new ArrayList<>();
    protected final PlanMap<DynamicVisual, DynamicVisual.Context> dynamicVisuals = new PlanMap<>();
    protected final PlanMap<TickableVisual, TickableVisual.Context> tickableVisuals = new PlanMap<>();
    protected VirtualRenderWorld virtualRenderWorld;
    protected Model model;
    protected TransformedInstance structure;
    protected SectionCollector sectionCollector;
    protected long minSection, maxSection;
    protected long minBlock, maxBlock;

    private final MatrixStack contraptionMatrix = new MatrixStack();

    public ContraptionVisual(VisualizationContext ctx, E entity, float partialTick) {
        super(ctx, entity, partialTick);
        embedding = ctx.createEmbedding(Vec3i.ZERO);

        setEmbeddingMatrices(partialTick);

        Contraption contraption = entity.getContraption();
        // The contraption could be null if it wasn't synced (ex. too much data)
        if (contraption == null)
            return;

        setupModel(contraption);

        setupChildren(partialTick, contraption);

        setupActors(partialTick, contraption);
    }

    // Must be called before setup children or setup actors as this creates the render world
    private void setupModel(Contraption contraption) {
        virtualRenderWorld = ContraptionRenderInfo.get(contraption).getRenderWorld();

        RenderedBlocks blocks = contraption.getRenderedBlocks();
        BlockRenderView modelWorld = new WrappedBlockAndTintGetter(virtualRenderWorld) {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                return blocks.lookup().apply(pos);
            }
        };

        model = new BlockModelBuilder(modelWorld, blocks.positions()).materialFunc((renderType, shaded) -> {
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

    }

    private void setupChildren(float partialTick, Contraption contraption) {
        children.forEach(BlockEntityVisual::delete);
        children.clear();
        for (BlockEntity be : contraption.getRenderedBEs()) {
            setupVisualizer(be, partialTick);
        }
    }

    private void setupActors(float partialTick, Contraption contraption) {
        actors.forEach(ActorVisual::delete);
        actors.clear();
        for (var actor : contraption.getActors()) {
            setupActor(actor, partialTick);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends BlockEntity> void setupVisualizer(T be, float partialTicks) {
        BlockEntityVisualizer<? super T> visualizer = (BlockEntityVisualizer<? super T>) VisualizerRegistry.getVisualizer(be.getType());
        if (visualizer == null) {
            return;
        }

        World level = be.getWorld();
        be.setWorld(virtualRenderWorld);
        BlockEntityVisual<? super T> visual = visualizer.createVisual(this.embedding, be, partialTicks);

        children.add(visual);

        if (visual instanceof DynamicVisual dynamic) {
            dynamicVisuals.add(dynamic, dynamic.planFrame());
        }

        if (visual instanceof TickableVisual tickable) {
            tickableVisuals.add(tickable, tickable.planTick());
        }

        be.setWorld(level);
    }

    private void setupActor(MutablePair<StructureTemplate.StructureBlockInfo, MovementContext> actor, float partialTick) {
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
        var visual = render.createVisual(this.embedding, virtualRenderWorld, context);

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
        return NestedPlan.of(RunnablePlan.of(this::beginFrame), ForEachPlan.of(() -> actors, ActorVisual::beginFrame), dynamicVisuals);
    }

    protected void beginFrame(DynamicVisual.Context context) {
        var partialTick = context.partialTick();
        setEmbeddingMatrices(partialTick);

        if (hasMovedSections()) {
            sectionCollector.sections(collectLightSections());
        }

        if (hasMovedBlocks()) {
            updateLight(partialTick);
        }

        var contraption = entity.getContraption();
        if (contraption.deferInvalidate) {
            setupModel(contraption);
            setupChildren(partialTick, contraption);
            setupActors(partialTick, contraption);

            contraption.deferInvalidate = false;
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
    public void updateLight(float partialTick) {
    }

    public LongSet collectLightSections() {
        var boundingBox = entity.getBoundingBox();

        var minSectionX = minLightSection(boundingBox.minX);
        var minSectionY = minLightSection(boundingBox.minY);
        var minSectionZ = minLightSection(boundingBox.minZ);
        int maxSectionX = maxLightSection(boundingBox.maxX);
        int maxSectionY = maxLightSection(boundingBox.maxY);
        int maxSectionZ = maxLightSection(boundingBox.maxZ);

        minSection = ChunkSectionPos.asLong(minSectionX, minSectionY, minSectionZ);
        maxSection = ChunkSectionPos.asLong(maxSectionX, maxSectionY, maxSectionZ);

        LongSet longSet = new LongArraySet();

        for (int x = 0; x <= maxSectionX - minSectionX; x++) {
            for (int y = 0; y <= maxSectionY - minSectionY; y++) {
                for (int z = 0; z <= maxSectionZ - minSectionZ; z++) {
                    longSet.add(ChunkSectionPos.offset(minSection, x, y, z));
                }
            }
        }

        return longSet;
    }

    protected boolean hasMovedBlocks() {
        var boundingBox = entity.getBoundingBox();

        int minX = minLight(boundingBox.minX);
        int minY = minLight(boundingBox.minY);
        int minZ = minLight(boundingBox.minZ);
        int maxX = maxLight(boundingBox.maxX);
        int maxY = maxLight(boundingBox.maxY);
        int maxZ = maxLight(boundingBox.maxZ);

        return minBlock != BlockPos.asLong(minX, minY, minZ) || maxBlock != BlockPos.asLong(maxX, maxY, maxZ);
    }

    protected boolean hasMovedSections() {
        var boundingBox = entity.getBoundingBox();

        var minSectionX = minLightSection(boundingBox.minX);
        var minSectionY = minLightSection(boundingBox.minY);
        var minSectionZ = minLightSection(boundingBox.minZ);
        int maxSectionX = maxLightSection(boundingBox.maxX);
        int maxSectionY = maxLightSection(boundingBox.maxY);
        int maxSectionZ = maxLightSection(boundingBox.maxZ);

        return minSection != ChunkSectionPos.asLong(minSectionX, minSectionY, minSectionZ) || maxSection != ChunkSectionPos.asLong(
            maxSectionX,
            maxSectionY,
            maxSectionZ
        );
    }

    @Override
    public void setSectionCollector(SectionCollector collector) {
        this.sectionCollector = collector;
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

    public static int minLight(double aabbPos) {
        return MathHelper.floor(aabbPos) - LIGHT_PADDING;
    }

    public static int maxLight(double aabbPos) {
        return MathHelper.ceil(aabbPos) + LIGHT_PADDING;
    }

    public static int minLightSection(double aabbPos) {
        return ChunkSectionPos.getSectionCoord(minLight(aabbPos));
    }

    public static int maxLightSection(double aabbPos) {
        return ChunkSectionPos.getSectionCoord(maxLight(aabbPos));
    }
}
