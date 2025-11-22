package com.zurrtum.create.client.content.contraptions.bearing;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BearingRenderer<T extends KineticBlockEntity & IBearingBlockEntity> extends KineticBlockEntityRenderer<T, BearingRenderer.BearingRenderState> {
    public BearingRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public BearingRenderState createRenderState() {
        return new BearingRenderState();
    }

    @Override
    public void updateRenderState(
        T be,
        BearingRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            return;
        }
        PartialModel top = be.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;
        state.top = CachedBuffers.partial(top, state.blockState);
        state.topAngle = (float) (be.getInterpolatedAngle(tickProgress - 1) / 180 * Math.PI);
        if (state.axis != Axis.Y) {
            state.upAngle = AngleHelper.rad(AngleHelper.horizontalAngle(state.facing.getOpposite()));
        } else {
            state.upAngle = -1;
        }
        state.eastAngle = AngleHelper.rad(-90 - AngleHelper.verticalAngle(state.facing));
    }

    @Override
    public void updateBaseRenderState(
        T be,
        BearingRenderState state,
        World world,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateBaseRenderState(be, state, world, crumblingOverlay);
        state.facing = state.blockState.get(Properties.FACING);
    }

    @Override
    protected RenderLayer getRenderType(T be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticBlockEntity be, BearingRenderState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, state.facing.getOpposite());
    }

    public static class BearingRenderState extends KineticRenderState {
        public Direction facing;
        public SuperByteBuffer top;
        public float topAngle;
        public float upAngle;
        public float eastAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            top.light(lightmapCoordinates);
            top.rotateCentered(topAngle, direction);
            top.color(color);
            if (upAngle != -1) {
                top.rotateCentered(upAngle, Direction.UP);
            }
            top.rotateCentered(eastAngle, Direction.EAST);
            top.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
