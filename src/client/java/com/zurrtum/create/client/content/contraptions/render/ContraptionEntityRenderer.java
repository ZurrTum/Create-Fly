package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.ShadedBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption.RenderedBlocks;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity, S extends ContraptionEntityRenderer.AbstractContraptionState> extends EntityRenderer<C, S> {
    public static final SuperByteBufferCache.Compartment<Pair<Contraption, BlockRenderLayer>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    public ContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
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
    public void render(S state, MatrixStack poseStack, VertexConsumerProvider buffers, int overlay) {
        if (state.contraption == null) {
            return;
        }

        ClientContraption clientContraption = state.contraption;
        Contraption contraption = clientContraption.getContraption();
        VirtualRenderWorld renderWorld = clientContraption.getRenderLevel();
        ContraptionMatrices matrices = clientContraption.getMatrices();
        matrices.setup((matrixStack, partialTicks) -> transform(state, matrixStack, partialTicks), poseStack, state);

        if (!VisualizationManager.supportsVisualization(state.world)) {
            for (BlockRenderLayer renderType : BlockRenderLayer.values()) {
                SuperByteBuffer sbb = getBuffer(contraption, clientContraption, renderWorld, renderType);
                if (!sbb.isEmpty()) {
                    VertexConsumer vc = buffers.getBuffer(getRenderLayer(renderType));
                    sbb.transform(matrices.getModel()).useLevelLight(state.world, matrices.getWorld()).renderInto(poseStack, vc);
                }
            }
        }

        var adjustRenderedBlockEntities = clientContraption.getAndAdjustShouldRenderBlockEntities();
        clientContraption.scratchErroredBlockEntities.clear();
        BlockEntityRenderHelper.renderBlockEntities(
            clientContraption.renderedBlockEntityView,
            adjustRenderedBlockEntities,
            clientContraption.scratchErroredBlockEntities,
            renderWorld,
            state.world,
            matrices.getModelViewProjection(),
            matrices.getLight(),
            buffers,
            AnimationTickHolder.getPartialTicks()
        );
        clientContraption.shouldRenderBlockEntities.andNot(clientContraption.scratchErroredBlockEntities);
        renderActors(state.world, renderWorld, contraption, matrices, buffers);

        matrices.clear();
    }

    private RenderLayer getRenderLayer(BlockRenderLayer layer) {
        return switch (layer) {
            case SOLID -> RenderLayer.getSolid();
            case CUTOUT_MIPPED -> RenderLayer.getCutoutMipped();
            case CUTOUT -> RenderLayer.getCutout();
            case TRANSLUCENT -> PonderRenderTypes.translucent();
            case TRIPWIRE -> RenderLayer.getTripwire();
        };
    }

    public void transform(S state, MatrixStack matrixStack, float partialTicks) {
    }

    @Override
    public void updateRenderState(C entity, S state, float tickProgress) {
        state.world = entity.getWorld();
        Contraption contraption = entity.getContraption();
        state.contraption = contraption != null ? getOrCreateClientContraptionLazy(contraption) : null;
        state.lastRenderX = entity.lastRenderX;
        state.lastRenderY = entity.lastRenderY;
        state.lastRenderZ = entity.lastRenderZ;
        state.entityX = entity.getX();
        state.entityY = entity.getY();
        state.entityZ = entity.getZ();
    }

    private static void renderActors(
        World level,
        VirtualRenderWorld renderWorld,
        Contraption c,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        MatrixStack m = matrices.getModel();

        for (Pair<StructureTemplate.StructureBlockInfo, MovementContext> actor : c.getActors()) {
            MovementContext context = actor.getRight();
            if (context == null)
                continue;
            if (context.world == null)
                context.world = level;
            StructureTemplate.StructureBlockInfo blockInfo = actor.getLeft();

            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(blockInfo.state());
            if (movementBehaviour != null) {
                MovementRenderBehaviour render = movementBehaviour.getAttachRender();
                if (render == null || c.isHiddenInPortal(blockInfo.pos()))
                    continue;
                m.push();
                TransformStack.of(m).translate(blockInfo.pos());
                render.renderInContraption(context, renderWorld, matrices, buffer);
                m.pop();
            }
        }
    }

    public static class AbstractContraptionState extends EntityRenderState {
        public World world;
        public ClientContraption contraption;
        public double lastRenderX;
        public double lastRenderY;
        public double lastRenderZ;
        public double entityX;
        public double entityY;
        public double entityZ;
    }

    @SuppressWarnings("removal")
    private static class ThreadLocalObjects {
        public final MatrixStack poseStack = new MatrixStack();
        public final Random random = Random.createLocal();
        public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
