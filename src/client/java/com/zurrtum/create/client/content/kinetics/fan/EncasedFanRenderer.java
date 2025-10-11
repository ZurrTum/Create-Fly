package com.zurrtum.create.client.content.kinetics.fan;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.state.property.Properties.FACING;

public class EncasedFanRenderer implements BlockEntityRenderer<EncasedFanBlockEntity, EncasedFanRenderer.EncasedFanRenderState> {
    public EncasedFanRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public EncasedFanRenderState createRenderState() {
        return new EncasedFanRenderState();
    }

    @Override
    public void updateRenderState(
        EncasedFanBlockEntity be,
        EncasedFanRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        state.pos = be.getPos();
        state.blockState = be.getCachedState();
        state.type = be.getType();
        state.crumblingOverlay = crumblingOverlay;
        state.layer = RenderLayer.getCutoutMipped();
        Direction direction = state.blockState.get(FACING);
        Direction opposite = direction.getOpposite();
        World world = be.getWorld();
        if (world != null) {
            state.lightBehind = WorldRenderer.getLightmapCoordinates(world, state.pos.offset(opposite));
            state.lightInFront = WorldRenderer.getLightmapCoordinates(world, state.pos.offset(direction));
        } else {
            state.lightBehind = state.lightInFront = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }
        state.shaftHalf = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, opposite);
        state.fanInner = CachedBuffers.partialFacing(AllPartialModels.ENCASED_FAN_INNER, state.blockState, opposite);
        float time = AnimationTickHolder.getRenderTime(world);
        float speed = be.getSpeed() * 5;
        if (speed > 0)
            speed = MathHelper.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = MathHelper.clamp(speed, -64 * 20, -80);
        float angle = (time * speed * 3 / 10f) % 360;
        Direction.Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.pos, axis);
        state.direction = Direction.from(axis, Direction.AxisDirection.POSITIVE);
        state.color = KineticBlockEntityRenderer.getColor(be);
        state.fanAngle = angle / 180f * (float) Math.PI;
    }

    @Override
    public void render(EncasedFanRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static class EncasedFanRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public int lightBehind;
        public int lightInFront;
        public SuperByteBuffer shaftHalf;
        public float angle;
        public Direction direction;
        public Color color;
        public SuperByteBuffer fanInner;
        public float fanAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
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
