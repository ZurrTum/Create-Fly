package com.zurrtum.create.client.content.kinetics.crank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HandCrankRenderer extends KineticBlockEntityRenderer<HandCrankBlockEntity, HandCrankRenderer.HandCrankRenderState> {
    public HandCrankRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public HandCrankRenderState createRenderState() {
        return new HandCrankRenderState();
    }

    @Override
    public void extractRenderState(
        HandCrankBlockEntity be,
        HandCrankRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        if (shouldRenderShaft()) {
            super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
            if (state.support) {
                return;
            }
        } else {
            Level world = be.getLevel();
            state.support = VisualizationManager.supportsVisualization(world);
            if (state.support) {
                return;
            }
            updateBaseRenderState(be, state, world, crumblingOverlay);
        }
        state.handle = getRenderedHandle(state.blockState);
        state.handleAngle = getIndependentAngle(be, tickProgress);
    }

    @Override
    protected RenderType getRenderType(HandCrankBlockEntity be, BlockState state) {
        return RenderTypes.solidMovingBlock();
    }

    public float getIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return getHandCrankIndependentAngle(be, partialTicks);
    }

    public static float getHandCrankIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return (be.independentAngle + partialTicks * be.chasingVelocity) / 360;
    }

    public SuperByteBuffer getRenderedHandle(BlockState blockState) {
        Direction facing = blockState.getOptionalValue(HandCrankBlock.FACING).orElse(Direction.UP);
        return CachedBuffers.partialFacing(AllPartialModels.HAND_CRANK_HANDLE, blockState, facing.getOpposite());
    }

    public boolean shouldRenderShaft() {
        return true;
    }

    public static class HandCrankRenderState extends KineticRenderState {
        public SuperByteBuffer handle;
        public float handleAngle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (model != null) {
                super.render(matricesEntry, vertexConsumer);
            }
            handle.light(lightCoords);
            handle.rotateCentered(handleAngle, direction);
            handle.color(color);
            handle.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
