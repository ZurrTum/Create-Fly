package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.render.ShadedBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption.RenderedBlocks;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper.BlockEntityListRenderState;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity, S extends ContraptionEntityRenderer.AbstractContraptionState> extends EntityRenderer<C, S> {
    public static final SuperByteBufferCache.Compartment<Pair<Contraption, ChunkSectionLayer>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
    private final PoseStack matrixStack;

    public ContraptionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.matrixStack = new PoseStack();
    }

    public static SuperByteBuffer getBuffer(
        Contraption contraption,
        ClientContraption clientContraption,
        VirtualRenderWorld renderWorld,
        ChunkSectionLayer renderType
    ) {
        return SuperByteBufferCache.getInstance()
            .get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(clientContraption, renderWorld, renderType));
    }

    @SuppressWarnings("unchecked")
    public ClientContraption getOrCreateClientContraptionLazy(Contraption contraption) {
        AtomicReference<@Nullable ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
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

    @SuppressWarnings("removal")
    private static SuperByteBuffer buildStructureBuffer(
        ClientContraption clientContraption,
        VirtualRenderWorld renderWorld,
        ChunkSectionLayer layer
    ) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        PoseStack poseStack = objects.poseStack;
        RandomSource random = objects.random;
        RenderedBlocks blocks = clientContraption.getRenderedBlocks();

        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.begin();

        ModelBlockRenderer.enableCaching();
        for (BlockPos pos : blocks.positions()) {
            BlockState state = blocks.lookup().apply(pos);
            if (state.getRenderShape() == RenderShape.MODEL) {
                BlockStateModel model = dispatcher.getBlockModel(state);
                long randomSeed = state.getSeed(pos);
                random.setSeed(randomSeed);
                if (ItemBlockRenderTypes.getChunkRenderType(state) == layer) {
                    poseStack.pushPose();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    List<BlockModelPart> parts = new ObjectArrayList<>();
                    if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                        wrapper.addPartsWithInfo(renderWorld, pos, state, random, parts);
                    } else {
                        model.collectParts(random, parts);
                    }
                    renderer.tesselateBlock(renderWorld, parts, state, pos, poseStack, sbbBuilder, true, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                }
            }
        }
        ModelBlockRenderer.clearCache();

        return sbbBuilder.end();
    }

    @Override
    public boolean shouldRender(C entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (entity.getContraption() == null)
            return false;
        if (!entity.isAliveOrStale())
            return false;
        if (!entity.isReadyForRender())
            return false;

        return super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new AbstractContraptionState();
    }

    @Override
    public void extractRenderState(C entity, S state, float tickProgress) {
        state.entityType = entity.getType();
        Contraption contraption = entity.getContraption();
        ClientContraption clientContraption = contraption != null ? getOrCreateClientContraptionLazy(contraption) : null;
        if (clientContraption == null) {
            return;
        }
        Camera camera = entityRenderDispatcher.camera;
        if (camera == null) {
            return;
        }
        state.contraption = contraption;
        state.x = Mth.lerp(tickProgress, entity.xOld, entity.getX());
        state.y = Mth.lerp(tickProgress, entity.yOld, entity.getY());
        state.z = Mth.lerp(tickProgress, entity.zOld, entity.getZ());
        matrixStack.pushPose();
        transform(state, matrixStack);
        state.modelEntry = matrixStack.last().copy();
        matrixStack.popPose();
        Level world = entity.level();
        VirtualRenderWorld renderWorld = clientContraption.getRenderLevel();
        Matrix4f worldMatrix4f = new Matrix4f().setTranslation((float) state.x, (float) state.y, (float) state.z);
        boolean support = VisualizationManager.supportsVisualization(world);
        if (!support) {
            state.layers = createLayers(contraption, clientContraption, renderWorld, world, worldMatrix4f);
        }
        var adjustRenderedBlockEntities = clientContraption.getAndAdjustShouldRenderBlockEntities();
        clientContraption.scratchErroredBlockEntities.clear();
        Vec3 cameraPos = camera.position();
        Matrix4f lightTransform = worldMatrix4f.mul(state.modelEntry.pose(), new Matrix4f());
        state.blockEntity = BlockEntityRenderHelper.getBlockEntitiesRenderState(
            support,
            clientContraption.renderedBlockEntityView,
            adjustRenderedBlockEntities,
            clientContraption.scratchErroredBlockEntities,
            renderWorld,
            world,
            lightTransform,
            contraption.entity.toLocalVector(cameraPos, tickProgress),
            tickProgress
        );
        clientContraption.shouldRenderBlockEntities.andNot(clientContraption.scratchErroredBlockEntities);
        state.actors = createActors(cameraPos, getFont(), world, renderWorld, contraption, worldMatrix4f);
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
        if (state.contraption == null) {
            return;
        }
        poseStack.pushPose();
        PoseStack.Pose entry = poseStack.last();
        entry.pose().mul(state.modelEntry.pose());
        entry.normal().mul(state.modelEntry.normal());
        if (state.layers != null) {
            for (ContraptionBlockLayer layer : state.layers) {
                queue.submitCustomGeometry(poseStack, layer.renderLayer, layer);
            }
        }
        if (state.blockEntity != null) {
            state.blockEntity.render(poseStack, queue, cameraRenderState);
        }
        if (state.actors != null) {
            for (MovementRenderState actor : state.actors) {
                poseStack.pushPose();
                actor.transform(poseStack);
                actor.render(poseStack, queue);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    public void transform(S state, PoseStack matrixStack) {
    }

    public static class AbstractContraptionState extends EntityRenderState {
        public @Nullable Contraption contraption;
        public PoseStack.Pose modelEntry;
        public @Nullable List<ContraptionBlockLayer> layers;
        public @Nullable BlockEntityListRenderState blockEntity;
        public @Nullable List<MovementRenderState> actors;
    }

    @Nullable
    public static List<ContraptionBlockLayer> createLayers(
        Contraption contraption,
        ClientContraption clientContraption,
        VirtualRenderWorld renderWorld,
        Level world,
        Matrix4f lightTransform
    ) {
        List<ContraptionBlockLayer> layers = new ArrayList<>();
        for (ChunkSectionLayer blockLayer : ChunkSectionLayer.values()) {
            SuperByteBuffer buffer = getBuffer(contraption, clientContraption, renderWorld, blockLayer);
            if (buffer.isEmpty()) {
                continue;
            }
            layers.add(new ContraptionBlockLayer(getRenderLayer(blockLayer), buffer, world, lightTransform));
        }
        if (layers.isEmpty()) {
            return null;
        }
        return layers;
    }

    private static RenderType getRenderLayer(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            case TRIPWIRE -> RenderTypes.tripwireMovingBlock();
        };
    }

    public record ContraptionBlockLayer(
        RenderType renderLayer, SuperByteBuffer buffer, Level world, Matrix4f lightTransform
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            buffer.useLevelLight(world, lightTransform).renderInto(matricesEntry, vertexConsumer);
        }
    }

    @Nullable
    public static List<MovementRenderState> createActors(
        Vec3 camera,
        Font textRenderer,
        Level world,
        VirtualRenderWorld renderWorld,
        Contraption contraption,
        Matrix4f worldMatrix4f
    ) {
        List<MovementRenderState> actors = new ArrayList<>();
        for (Pair<StructureTemplate.StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
            MovementContext context = actor.getRight();
            if (context == null) {
                continue;
            }
            if (context.world == null) {
                context.world = world;
            }
            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(context.state);
            if (movementBehaviour != null) {
                MovementRenderBehaviour render = movementBehaviour.getAttachRender();
                if (render == null || contraption.isHiddenInPortal(context.localPos)) {
                    continue;
                }
                MovementRenderState renderState = render.getRenderState(camera, textRenderer, context, renderWorld, worldMatrix4f);
                if (renderState != null) {
                    actors.add(renderState);
                }
            }
        }
        if (actors.isEmpty()) {
            return null;
        }
        return actors;
    }

    @SuppressWarnings("removal")
    private static class ThreadLocalObjects {
        public final PoseStack poseStack = new PoseStack();
        public final RandomSource random = RandomSource.createNewThreadLocalInstance();
        public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
