package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.ShadedBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption.RenderedBlocks;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper.BlockEntityListRenderState;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity, S extends ContraptionEntityRenderer.AbstractContraptionState> extends EntityRenderer<C, S> {
    public static final SuperByteBufferCache.Compartment<Pair<Contraption, BlockRenderLayer>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
    private final MatrixStack matrixStack;

    public ContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.matrixStack = new MatrixStack();
    }

    public static SuperByteBuffer getBuffer(
        Contraption contraption,
        ClientContraption clientContraption,
        VirtualRenderWorld renderWorld,
        BlockRenderLayer renderType
    ) {
        return SuperByteBufferCache.getInstance()
            .get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(clientContraption, renderWorld, renderType));
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

    @SuppressWarnings("removal")
    private static SuperByteBuffer buildStructureBuffer(ClientContraption clientContraption, VirtualRenderWorld renderWorld, BlockRenderLayer layer) {
        BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
        BlockModelRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        MatrixStack poseStack = objects.poseStack;
        Random random = objects.random;
        RenderedBlocks blocks = clientContraption.getRenderedBlocks();

        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.begin();

        BlockModelRenderer.enableBrightnessCache();
        for (BlockPos pos : blocks.positions()) {
            BlockState state = blocks.lookup().apply(pos);
            if (state.getRenderType() == BlockRenderType.MODEL) {
                BlockStateModel model = dispatcher.getModel(state);
                long randomSeed = state.getRenderingSeed(pos);
                random.setSeed(randomSeed);
                if (RenderLayers.getBlockLayer(state) == layer) {
                    poseStack.push();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    List<BlockModelPart> parts = new ObjectArrayList<>();
                    if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                        wrapper.addPartsWithInfo(renderWorld, pos, state, random, parts);
                    } else {
                        model.addParts(random, parts);
                    }
                    renderer.render(renderWorld, parts, state, pos, poseStack, sbbBuilder, true, OverlayTexture.DEFAULT_UV);
                    poseStack.pop();
                }
            }
        }
        BlockModelRenderer.disableBrightnessCache();

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
    public void updateRenderState(C entity, S state, float tickProgress) {
        state.entityType = entity.getType();
        Contraption contraption = entity.getContraption();
        ClientContraption clientContraption = contraption != null ? getOrCreateClientContraptionLazy(contraption) : null;
        if (clientContraption == null) {
            return;
        }
        Camera camera = dispatcher.camera;
        if (camera == null) {
            return;
        }
        state.contraption = contraption;
        state.x = MathHelper.lerp(tickProgress, entity.lastRenderX, entity.getX());
        state.y = MathHelper.lerp(tickProgress, entity.lastRenderY, entity.getY());
        state.z = MathHelper.lerp(tickProgress, entity.lastRenderZ, entity.getZ());
        World world = entity.getEntityWorld();
        VirtualRenderWorld renderWorld = clientContraption.getRenderLevel();
        ContraptionMatrices matrices = clientContraption.getMatrices();
        matrixStack.push();
        Vec3d cameraPos = camera.getPos();
        matrixStack.translate(state.x - cameraPos.x, state.y - cameraPos.y, state.z - cameraPos.z);
        matrices.setup(this, matrixStack, state);
        MatrixStack projection = new MatrixStack();
        projection.peek().copy(matrices.getModelViewProjection().peek());
        Matrix4f lightMatrix4f = new Matrix4f(matrices.getLight());
        Matrix4f worldMatrix4f = new Matrix4f(matrices.getWorld());
        matrices.clear();
        matrixStack.pop();
        boolean support = VisualizationManager.supportsVisualization(world);
        if (!support) {
            state.block = ContraptionBlockRenderState.create(contraption, clientContraption, renderWorld, projection, world, worldMatrix4f);
        }
        var adjustRenderedBlockEntities = clientContraption.getAndAdjustShouldRenderBlockEntities();
        clientContraption.scratchErroredBlockEntities.clear();
        state.blockEntity = BlockEntityRenderHelper.getBlockEntitiesRenderState(
            support,
            clientContraption.renderedBlockEntityView,
            adjustRenderedBlockEntities,
            clientContraption.scratchErroredBlockEntities,
            renderWorld,
            world,
            projection,
            lightMatrix4f,
            contraption.entity.toLocalVector(cameraPos, tickProgress),
            tickProgress
        );
        clientContraption.shouldRenderBlockEntities.andNot(clientContraption.scratchErroredBlockEntities);
        state.actor = ActorListRenderState.create(cameraPos, getTextRenderer(), world, renderWorld, contraption, worldMatrix4f, projection);
    }

    @Override
    public void render(S state, MatrixStack poseStack, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        if (state.contraption == null) {
            return;
        }
        if (state.block != null) {
            state.block.render(queue);
        }
        if (state.blockEntity != null) {
            state.blockEntity.render(queue, cameraRenderState);
        }
        if (state.actor != null) {
            state.actor.render(queue);
        }
    }

    public void transform(S state, MatrixStack matrixStack) {
    }

    public static class AbstractContraptionState extends EntityRenderState {
        public Contraption contraption;
        public ContraptionBlockRenderState block;
        public BlockEntityListRenderState blockEntity;
        public ActorListRenderState actor;
    }

    public record ContraptionBlockRenderState(MatrixStack matrices, List<ContraptionBlockLayer> layers) {
        @Nullable
        public static ContraptionBlockRenderState create(
            Contraption contraption,
            ClientContraption clientContraption,
            VirtualRenderWorld renderWorld,
            MatrixStack matrices,
            World world,
            Matrix4f lightTransform
        ) {
            List<ContraptionBlockLayer> layers = new ArrayList<>();
            for (BlockRenderLayer blockLayer : BlockRenderLayer.values()) {
                SuperByteBuffer buffer = getBuffer(contraption, clientContraption, renderWorld, blockLayer);
                if (buffer.isEmpty()) {
                    continue;
                }
                layers.add(new ContraptionBlockLayer(getRenderLayer(blockLayer), buffer, world, lightTransform));
            }
            if (layers.isEmpty()) {
                return null;
            }
            return new ContraptionBlockRenderState(matrices, layers);
        }

        private static RenderLayer getRenderLayer(BlockRenderLayer layer) {
            return switch (layer) {
                case SOLID -> RenderLayer.getSolid();
                case CUTOUT_MIPPED -> RenderLayer.getCutoutMipped();
                case CUTOUT -> RenderLayer.getCutout();
                case TRANSLUCENT -> ShadersModHelper.isShaderPackInUse() ? RenderLayer.getTranslucentMovingBlock() : PonderRenderTypes.translucent();
                case TRIPWIRE -> RenderLayer.getTripwire();
            };
        }

        public void render(OrderedRenderCommandQueue queue) {
            for (ContraptionBlockLayer layer : layers) {
                queue.submitCustom(matrices, layer.renderLayer, layer);
            }
        }
    }


    public record ContraptionBlockLayer(
        RenderLayer renderLayer, SuperByteBuffer buffer, World world, Matrix4f lightTransform
    ) implements OrderedRenderCommandQueue.Custom {
        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            buffer.useLevelLight(world, lightTransform).renderInto(matricesEntry, vertexConsumer);
        }
    }

    public record ActorListRenderState(MatrixStack matrices, List<MovementRenderState> actors) {
        @Nullable
        public static ActorListRenderState create(
            Vec3d camera,
            TextRenderer textRenderer,
            World world,
            VirtualRenderWorld renderWorld,
            Contraption contraption,
            Matrix4f worldMatrix4f,
            MatrixStack viewProjection
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
            return new ActorListRenderState(viewProjection, actors);
        }

        public void render(OrderedRenderCommandQueue queue) {
            for (MovementRenderState actor : actors) {
                matrices.push();
                actor.transform(matrices);
                actor.render(matrices, queue);
                matrices.pop();
            }
        }
    }

    @SuppressWarnings("removal")
    private static class ThreadLocalObjects {
        public final MatrixStack poseStack = new MatrixStack();
        public final Random random = Random.createLocal();
        public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
