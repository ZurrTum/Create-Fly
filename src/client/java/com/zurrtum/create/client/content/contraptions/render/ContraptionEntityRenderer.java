package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity, S extends ContraptionEntityRenderer.AbstractContraptionState> extends EntityRenderer<C, S> {
    public ContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
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

        ContraptionRenderInfo renderInfo = ContraptionRenderInfo.get(state.contraption);
        VirtualRenderWorld renderWorld = renderInfo.getRenderWorld();
        ContraptionMatrices matrices = renderInfo.getMatrices();
        matrices.setup((matrixStack, partialTicks) -> transform(state, matrixStack, partialTicks), poseStack, state);

        if (!VisualizationManager.supportsVisualization(state.world)) {
            for (BlockRenderLayer renderType : BlockRenderLayer.values()) {
                SuperByteBuffer sbb = ContraptionRenderInfo.getBuffer(state.contraption, renderWorld, renderType);
                if (!sbb.isEmpty()) {
                    VertexConsumer vc = buffers.getBuffer(getRenderLayer(renderType));
                    sbb.transform(matrices.getModel()).useLevelLight(state.world, matrices.getWorld()).renderInto(poseStack, vc);
                }
            }
        }

        BlockEntityRenderHelper.renderBlockEntities(
            state.world,
            renderWorld,
            state.contraption.getRenderedBEs(),
            matrices.getModelViewProjection(),
            matrices.getLight(),
            buffers
        );
        renderActors(state.world, renderWorld, state.contraption, matrices, buffers);

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
        state.contraption = entity.getContraption();
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
        public Contraption contraption;
        public double lastRenderX;
        public double lastRenderY;
        public double lastRenderZ;
        public double entityX;
        public double entityY;
        public double entityZ;
    }
}
