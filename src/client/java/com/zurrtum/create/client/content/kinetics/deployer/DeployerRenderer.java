package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.State;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerRenderer implements BlockEntityRenderer<DeployerBlockEntity, DeployerRenderer.DeployerRenderState> {
    protected final ItemModelManager itemModelManager;

    public DeployerRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public DeployerRenderState createRenderState() {
        return new DeployerRenderState();
    }

    @Override
    public void updateRenderState(
        DeployerBlockEntity be,
        DeployerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        World world = be.getWorld();
        updateItemRenderState(be, state, itemModelManager, world, tickProgress);
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            state.blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.squaredDistanceTo(VecHelper.getCenterOf(state.pos))
        );
        updateComponentsRenderState(be, state, world, tickProgress);
    }

    public static void updateItemRenderState(
        DeployerBlockEntity be,
        DeployerRenderState state,
        ItemModelManager itemModelManager,
        World world,
        float tickProgress
    ) {
        ItemStack heldItem = be.heldItem;
        if (heldItem.isEmpty()) {
            return;
        }
        DeployerItemRenderState data = state.item = new DeployerItemRenderState();
        data.offset = getHandOffset(state, be, tickProgress, state.blockState).add(VecHelper.getCenterOf(BlockPos.ZERO));
        Direction facing = state.blockState.get(FACING);
        data.punching = be.mode == Mode.PUNCH;
        data.yRot = MathHelper.RADIANS_PER_DEGREE * (AngleHelper.horizontalAngle(facing) + 180);
        data.xRot = MathHelper.RADIANS_PER_DEGREE * (facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0);
        data.displayMode = facing == Direction.UP && be.getSpeed() == 0 && !data.punching;
        ItemRenderState renderState = data.state = new ItemRenderState();
        if (data.displayMode) {
            renderState.displayContext = ItemDisplayContext.GROUND;
        } else {
            renderState.displayContext = data.punching ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.FIXED;
        }
        itemModelManager.update(renderState, heldItem, renderState.displayContext, world, null, 0);
        boolean isBlockItem = (heldItem.getItem() instanceof BlockItem) && renderState.isSideLit();
        if (data.displayMode) {
            data.translateY = isBlockItem ? 9 / 16f : 11 / 16f;
            data.scale = isBlockItem ? 1.25f : 1;
            data.yRot2 = MathHelper.RADIANS_PER_DEGREE * AnimationTickHolder.getRenderTime(world);
        } else {
            data.scale = data.punching ? .75f : isBlockItem ? .75f - 1 / 64f : .5f;
        }
    }

    public static void updateComponentsRenderState(DeployerBlockEntity be, DeployerRenderState state, World world, float tickProgress) {
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        ComponentsRenderState components = state.components = new ComponentsRenderState();
        components.layer = RenderLayer.getSolid();
        components.light = state.lightmapCoordinates;
        Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        components.shaft = CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, KineticBlockEntityRenderer.shaft(axis));
        components.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.pos, axis);
        components.direction = Direction.from(axis, AxisDirection.POSITIVE);
        components.color = KineticBlockEntityRenderer.getColor(be);
        components.offset = getHandOffset(state, be, tickProgress, state.blockState);
        components.pole = CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, state.blockState);
        components.hand = CachedBuffers.partial(getHandPose(be), state.blockState);
        Direction facing = state.blockState.get(FACING);
        components.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        components.xRot = MathHelper.RADIANS_PER_DEGREE * (facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0);
        components.zRot = MathHelper.RADIANS_PER_DEGREE * ((state.blockState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90 : 0);
    }

    @Override
    public void render(DeployerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.item != null) {
            state.item.render(matrices, queue, state.lightmapCoordinates);
        }
        if (state.filter != null) {
            state.filter.render(state.blockState, queue, matrices, state.lightmapCoordinates);
        }
        if (state.components != null) {
            queue.submitCustom(matrices, state.components.layer, state.components);
        }
    }

    public static PartialModel getHandPose(DeployerBlockEntity be) {
        return be.mode == Mode.PUNCH ? AllPartialModels.DEPLOYER_HAND_PUNCHING : be.heldItem.isEmpty() ? AllPartialModels.DEPLOYER_HAND_POINTING : AllPartialModels.DEPLOYER_HAND_HOLDING;
    }

    public static Vec3d getHandOffset(DeployerRenderState state, DeployerBlockEntity be, float partialTicks, BlockState blockState) {
        if (state.offset != null) {
            return state.offset;
        }
        return state.offset = getHandOffset(be, partialTicks, blockState);
    }

    public static Vec3d getHandOffset(DeployerBlockEntity be, float partialTicks, BlockState blockState) {
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

    private static SuperByteBuffer transform(SuperByteBuffer buffer, BlockState deployerState, boolean axisDirectionMatters) {
        Direction facing = deployerState.get(FACING);

        float yRot = AngleHelper.horizontalAngle(facing);
        float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        float zRot = axisDirectionMatters && (deployerState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90 : 0;

        buffer.rotateCentered((float) ((yRot) / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) ((xRot) / 180 * Math.PI), Direction.EAST);
        buffer.rotateCentered((float) ((zRot) / 180 * Math.PI), Direction.SOUTH);
        return buffer;
    }

    static PartialModel getHandPose(Mode mode) {
        return mode == Mode.PUNCH ? AllPartialModels.DEPLOYER_HAND_PUNCHING : AllPartialModels.DEPLOYER_HAND_POINTING;
    }

    public static class DeployerRenderState extends BlockEntityRenderState {
        public Vec3d offset;
        public DeployerItemRenderState item;
        public FilterRenderState filter;
        public ComponentsRenderState components;
    }

    public static class DeployerItemRenderState {
        public Vec3d offset;
        public boolean punching;
        public float yRot;
        public float xRot;
        public boolean displayMode;
        public ItemRenderState state;
        public float translateY;
        public float scale;
        public float yRot2;

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light) {
            matrices.push();
            matrices.translate(offset);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            if (!displayMode) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(xRot));
                matrices.translate(0, 0, -11 / 16f);
            }
            if (punching) {
                matrices.translate(0, 1 / 8f, -1 / 16f);
            }
            if (displayMode) {
                matrices.translate(0, translateY, 0);
                matrices.scale(scale, scale, scale);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot2));
            } else {
                matrices.scale(scale, scale, scale);
            }
            state.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
            matrices.pop();
        }
    }

    public static class ComponentsRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public int light;
        public SuperByteBuffer shaft;
        public float angle;
        public Direction direction;
        public Color color;
        public Vec3d offset;
        public SuperByteBuffer pole;
        public SuperByteBuffer hand;
        public float yRot;
        public float xRot;
        public float zRot;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            shaft.light(light).rotateCentered(angle, direction).color(color).renderInto(matricesEntry, vertexConsumer);
            pole.translate(offset).rotateCentered(yRot, Direction.UP).rotateCentered(xRot, Direction.EAST).rotateCentered(zRot, Direction.SOUTH)
                .light(light).renderInto(matricesEntry, vertexConsumer);
            hand.translate(offset).rotateCentered(yRot, Direction.UP).rotateCentered(xRot, Direction.EAST).light(light)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
