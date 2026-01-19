package com.zurrtum.create.client.content.kinetics.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class EncasedFanRenderer implements BlockEntityRenderer<EncasedFanBlockEntity, EncasedFanRenderer.EncasedFanRenderState> {
    public EncasedFanRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public EncasedFanRenderState createRenderState() {
        return new EncasedFanRenderState();
    }

    @Override
    public void extractRenderState(
        EncasedFanBlockEntity be,
        EncasedFanRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.breakProgress = crumblingOverlay;
        state.layer = RenderTypes.cutoutMovingBlock();
        Direction direction = state.blockState.getValue(FACING);
        Direction opposite = direction.getOpposite();
        Level world = be.getLevel();
        if (world != null) {
            state.lightBehind = LevelRenderer.getLightCoords(world, state.blockPos.relative(opposite));
            state.lightInFront = LevelRenderer.getLightCoords(world, state.blockPos.relative(direction));
        } else {
            state.lightBehind = state.lightInFront = LightCoordsUtil.FULL_BRIGHT;
        }
        state.shaftHalf = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, opposite);
        state.fanInner = CachedBuffers.partialFacing(AllPartialModels.ENCASED_FAN_INNER, state.blockState, opposite);
        float time = AnimationTickHolder.getRenderTime(world);
        float speed = be.getSpeed() * 5;
        if (speed > 0)
            speed = Mth.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = Mth.clamp(speed, -64 * 20, -80);
        float angle = (time * speed * 3 / 10f) % 360;
        Direction.Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.blockPos, axis);
        state.direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        state.color = KineticBlockEntityRenderer.getColor(be);
        state.fanAngle = angle / 180f * (float) Math.PI;
    }

    @Override
    public void submit(EncasedFanRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class EncasedFanRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public int lightBehind;
        public int lightInFront;
        public SuperByteBuffer shaftHalf;
        public float angle;
        public Direction direction;
        public Color color;
        public SuperByteBuffer fanInner;
        public float fanAngle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            shaftHalf.light(lightBehind);
            shaftHalf.rotateCentered(angle, direction);
            shaftHalf.color(color);
            shaftHalf.renderInto(matricesEntry, vertexConsumer);
            fanInner.light(lightInFront);
            fanInner.rotateCentered(fanAngle, direction);
            fanInner.color(color);
            fanInner.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
