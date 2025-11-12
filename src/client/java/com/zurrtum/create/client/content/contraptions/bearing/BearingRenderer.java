package com.zurrtum.create.client.content.contraptions.bearing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BearingRenderer<T extends KineticBlockEntity & IBearingBlockEntity> extends KineticBlockEntityRenderer<T, BearingRenderer.BearingRenderState> {
    public BearingRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BearingRenderState createRenderState() {
        return new BearingRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        BearingRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
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
    public void updateBaseRenderState(T be, BearingRenderState state, Level world, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.updateBaseRenderState(be, state, world, crumblingOverlay);
        state.facing = state.blockState.getValue(BlockStateProperties.FACING);
    }

    @Override
    protected RenderType getRenderType(T be, BlockState state) {
        return RenderType.solid();
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
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            top.light(lightCoords);
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
