package com.zurrtum.create.client.content.kinetics.crank;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HandCrankRenderer extends KineticBlockEntityRenderer<HandCrankBlockEntity, HandCrankRenderer.HandCrankRenderState> {
    public HandCrankRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public HandCrankRenderState createRenderState() {
        return new HandCrankRenderState();
    }

    @Override
    public void updateRenderState(
        HandCrankBlockEntity be,
        HandCrankRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        if (shouldRenderShaft()) {
            super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
            if (state.support) {
                return;
            }
        } else {
            World world = be.getWorld();
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
    protected RenderLayer getRenderType(HandCrankBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    public float getIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return getHandCrankIndependentAngle(be, partialTicks);
    }

    public static float getHandCrankIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return (be.independentAngle + partialTicks * be.chasingVelocity) / 360;
    }

    public SuperByteBuffer getRenderedHandle(BlockState blockState) {
        Direction facing = blockState.getOrEmpty(HandCrankBlock.FACING).orElse(Direction.UP);
        return CachedBuffers.partialFacing(AllPartialModels.HAND_CRANK_HANDLE, blockState, facing.getOpposite());
    }

    public boolean shouldRenderShaft() {
        return true;
    }

    public static class HandCrankRenderState extends KineticRenderState {
        public SuperByteBuffer handle;
        public float handleAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (model != null) {
                super.render(matricesEntry, vertexConsumer);
            }
            handle.light(lightmapCoordinates);
            handle.rotateCentered(handleAngle, direction);
            handle.color(color);
            handle.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
