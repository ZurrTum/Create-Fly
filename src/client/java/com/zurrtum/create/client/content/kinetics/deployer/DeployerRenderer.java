package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.State;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.AxisDirection;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerRenderer extends SafeBlockEntityRenderer<DeployerBlockEntity> {

    public DeployerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(DeployerBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        renderItem(be, partialTicks, ms, buffer, light, overlay);
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        renderComponents(be, partialTicks, ms, buffer, light);
    }

    protected void renderItem(DeployerBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        if (be.heldItem.isEmpty())
            return;

        BlockState deployerState = be.getCachedState();
        Vec3d offset = getHandOffset(be, partialTicks, deployerState).add(VecHelper.getCenterOf(BlockPos.ZERO));
        ms.push();
        ms.translate(offset.x, offset.y, offset.z);

        Direction facing = deployerState.get(FACING);
        boolean punching = be.mode == Mode.PUNCH;

        float yRot = AngleHelper.horizontalAngle(facing) + 180;
        float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
        boolean displayMode = facing == Direction.UP && be.getSpeed() == 0 && !punching;

        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRot));
        if (!displayMode) {
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRot));
            ms.translate(0, 0, -11 / 16f);
        }

        if (punching)
            ms.translate(0, 1 / 8f, -1 / 16f);

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        ItemDisplayContext transform;
        if (displayMode) {
            transform = ItemDisplayContext.GROUND;
        } else {
            transform = punching ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.FIXED;
        }
        itemRenderer.itemModelManager.clearAndUpdate(itemRenderer.itemRenderState, be.heldItem, transform, be.getWorld(), null, 0);
        boolean isBlockItem = (be.heldItem.getItem() instanceof BlockItem) && itemRenderer.itemRenderState.isSideLit();
        if (displayMode) {
            float scale = isBlockItem ? 1.25f : 1;
            ms.translate(0, isBlockItem ? 9 / 16f : 11 / 16f, 0);
            ms.scale(scale, scale, scale);
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(AnimationTickHolder.getRenderTime(be.getWorld())));
        } else {
            float scale = punching ? .75f : isBlockItem ? .75f - 1 / 64f : .5f;
            ms.scale(scale, scale, scale);
        }

        itemRenderer.itemRenderState.render(ms, buffer, light, overlay);
        ms.pop();
    }

    protected void renderComponents(DeployerBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
        if (!VisualizationManager.supportsVisualization(be.getWorld())) {
            KineticBlockEntityRenderer.renderRotatingKineticBlock(be, getRenderedBlockState(be), ms, vb, light);
        }

        BlockState blockState = be.getCachedState();
        Vec3d offset = getHandOffset(be, partialTicks, blockState);

        SuperByteBuffer pole = CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, blockState);
        SuperByteBuffer hand = CachedBuffers.partial(getHandPose(be), blockState);

        transform(pole.translate(offset.x, offset.y, offset.z), blockState, true).light(light).renderInto(ms, vb);
        transform(hand.translate(offset.x, offset.y, offset.z), blockState, false).light(light).renderInto(ms, vb);
    }

    public static PartialModel getHandPose(DeployerBlockEntity be) {
        return be.mode == Mode.PUNCH ? AllPartialModels.DEPLOYER_HAND_PUNCHING : be.heldItem.isEmpty() ? AllPartialModels.DEPLOYER_HAND_POINTING : AllPartialModels.DEPLOYER_HAND_HOLDING;
    }

    protected Vec3d getHandOffset(DeployerBlockEntity be, float partialTicks, BlockState blockState) {
        float distance = getHandOffset(be, partialTicks);
        return Vec3d.of(blockState.get(FACING).getVector()).multiply(distance);
    }

    public static float getHandOffset(DeployerBlockEntity be, float partialTicks) {
        if (be.isVirtual())
            return be.animatedOffset.getValue(partialTicks);

        float progress = 0;
        int timerSpeed = be.getTimerSpeed();
        PartialModel handPose = getHandPose(be);

        if (be.state == State.EXPANDING) {
            progress = 1 - (be.timer - partialTicks * timerSpeed) / 1000f;
            if (be.fistBump)
                progress *= progress;
        }
        if (be.state == State.RETRACTING)
            progress = (be.timer - partialTicks * timerSpeed) / 1000f;
        float handLength = handPose == AllPartialModels.DEPLOYER_HAND_POINTING ? 0 : handPose == AllPartialModels.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
        return Math.min(MathHelper.clamp(progress, 0, 1) * (be.reach + handLength), 21 / 16f);
    }

    protected BlockState getRenderedBlockState(KineticBlockEntity be) {
        return KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(be));
    }

    private static SuperByteBuffer transform(SuperByteBuffer buffer, BlockState deployerState, boolean axisDirectionMatters) {
        Direction facing = deployerState.get(FACING);

        float yRot = AngleHelper.horizontalAngle(facing);
        float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        float zRot = axisDirectionMatters && (deployerState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) ? 90 : 0;

        buffer.rotateCentered((float) ((yRot) / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) ((xRot) / 180 * Math.PI), Direction.EAST);
        buffer.rotateCentered((float) ((zRot) / 180 * Math.PI), Direction.SOUTH);
        return buffer;
    }

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        VertexConsumer builder = buffer.getBuffer(RenderLayer.getSolid());
        BlockState blockState = context.state;
        Mode mode = NBTHelper.readEnum(context.blockEntityData, "Mode", Mode.class);
        PartialModel handPose = getHandPose(mode);

        float speed = (float) context.getAnimationSpeed();
        if (context.contraption.stalled)
            speed = 0;

        SuperByteBuffer shaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState());
        SuperByteBuffer pole = CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, blockState);
        SuperByteBuffer hand = CachedBuffers.partial(handPose, blockState);

        double factor;
        if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = MathHelper.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vec3d center = VecHelper.getCenterOf(BlockPos.ofFloored(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion).distanceTo(center);
            factor = .5f - MathHelper.clamp(MathHelper.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }

        Vec3d offset = Vec3d.of(blockState.get(FACING).getVector()).multiply(factor);

        MatrixStack m = matrices.getModel();
        m.push();

        m.push();
        Direction.Axis axis = Direction.Axis.Y;
        if (context.state.getBlock() instanceof IRotate def) {
            axis = def.getRotationAxis(context.state);
        }

        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        float angle = (time * speed) % 360;

        TransformStack.of(m).center().rotateYDegrees(axis == Direction.Axis.Z ? 90 : 0).rotateZDegrees(axis.isHorizontal() ? 90 : 0).uncenter();
        shaft.transform(m);
        shaft.rotateCentered(angle, Direction.get(AxisDirection.POSITIVE, Direction.Axis.Y));
        m.pop();

        if (!context.disabled)
            m.translate(offset.x, offset.y, offset.z);
        pole.transform(m);
        hand.transform(m);

        transform(pole, blockState, true);
        transform(hand, blockState, false);

        shaft.light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos)).useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), builder);
        pole.light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos)).useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), builder);
        hand.light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos)).useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), builder);

        m.pop();
    }

    static PartialModel getHandPose(Mode mode) {
        return mode == Mode.PUNCH ? AllPartialModels.DEPLOYER_HAND_PUNCHING : AllPartialModels.DEPLOYER_HAND_POINTING;
    }

}
