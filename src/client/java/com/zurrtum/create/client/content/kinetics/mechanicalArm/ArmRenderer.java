package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArmRenderer extends KineticBlockEntityRenderer<ArmBlockEntity, ArmRenderer.ArmRenderState> {
    protected static ItemModelManager itemModelManager;

    public ArmRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        itemModelManager = context.itemModelManager();
    }

    @Override
    public ArmRenderState createRenderState() {
        return new ArmRenderState();
    }

    @Override
    public void updateRenderState(
        ArmBlockEntity be,
        ArmRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        ItemStack item = be.heldItem;
        boolean empty = item.isEmpty();
        if (state.support) {
            if (empty) {
                return;
            }
            BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        }
        World world = be.getWorld();
        boolean isBlockItem;
        if (empty) {
            isBlockItem = false;
        } else {
            ArmItemData data = state.item = new ArmItemData();
            ItemRenderState renderState = new ItemRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(renderState, item, renderState.displayContext, world, null, 0);
            isBlockItem = item.getItem() instanceof BlockItem && renderState.isSideLit();
            data.state = renderState;
            data.xRot = MathHelper.RADIANS_PER_DEGREE * 90;
            if (isBlockItem) {
                data.offset = -9 / 16f;
                data.scale = .5f;
            } else {
                data.offset = -10 / 16f;
                data.scale = .625f;
            }
        }
        boolean inverted = state.blockState.get(ArmBlock.CEILING);
        if (inverted) {
            state.rotate = MathHelper.RADIANS_PER_DEGREE * 180;
        }
        boolean rave = be.phase == Phase.DANCING && be.getSpeed() != 0;
        if (rave) {
            float renderTick = AnimationTickHolder.getRenderTime(world) + (be.hashCode() % 64);
            state.baseAngle = MathHelper.RADIANS_PER_DEGREE * ((renderTick * 10) % 360);
            state.lowerArmAngle = MathHelper.RADIANS_PER_DEGREE * (MathHelper.lerp((MathHelper.sin(renderTick / 4) + 1) / 2, -45, 15) - 135);
            state.upperArmAngle = MathHelper.RADIANS_PER_DEGREE * (MathHelper.lerp((MathHelper.sin(renderTick / 8) + 1) / 4, -45, 95) - 90);
            state.headAngle = MathHelper.RADIANS_PER_DEGREE * (-state.lowerArmAngle - 45);
        } else {
            state.baseAngle = MathHelper.RADIANS_PER_DEGREE * be.baseAngle.getValue(tickProgress);
            state.lowerArmAngle = MathHelper.RADIANS_PER_DEGREE * be.lowerArmAngle.getValue(tickProgress);
            state.upperArmAngle = MathHelper.RADIANS_PER_DEGREE * (be.upperArmAngle.getValue(tickProgress) - 180);
            state.headAngle = MathHelper.RADIANS_PER_DEGREE * (be.headAngle.getValue(tickProgress) - 45);
        }
        if (!state.support) {
            ArmRenderData data = state.arm = new ArmRenderData();
            boolean goggles = be.goggles;
            data.base = CachedBuffers.partial(AllPartialModels.ARM_BASE, state.blockState);
            data.lower = CachedBuffers.partial(AllPartialModels.ARM_LOWER_BODY, state.blockState);
            data.upper = CachedBuffers.partial(AllPartialModels.ARM_UPPER_BODY, state.blockState);
            data.claw = CachedBuffers.partial(goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE, state.blockState);
            data.clawUpper = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_UPPER, state.blockState);
            data.clawLower = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_LOWER, state.blockState);
            data.light = state.lightmapCoordinates;
            if (rave) {
                data.color = Color.rainbowColor(AnimationTickHolder.getTicks() * 100).getRGB();
            } else {
                data.color = 0xFFFFFF;
            }
            data.inverted = inverted && goggles;
            data.clawOffset = empty ? 1 / 16f : isBlockItem ? 3 / 16f : 5 / 64f;
        }
    }

    @Override
    public void render(ArmRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (state.rotate != 0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.rotate));
        }
        matrices.translate(0, 0.25f, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.baseAngle));
        if (state.support) {
            matrices.translate(0, 0.125f, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.lowerArmAngle));
            matrices.translate(0, 0, -0.875f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.upperArmAngle));
            matrices.translate(0, 0, -0.9375f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.headAngle));
            state.item.render(matrices, queue, state.lightmapCoordinates);
        } else {
            queue.submitCustom(matrices, state.layer, state.arm::renderBase);
            matrices.translate(0, 0.125f, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.lowerArmAngle));
            queue.submitCustom(matrices, state.layer, state.arm::renderLower);
            matrices.translate(0, 0, -0.875f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.upperArmAngle));
            queue.submitCustom(matrices, state.layer, state.arm::renderUpper);
            matrices.translate(0, 0, -0.9375f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.headAngle));
            if (state.arm.inverted) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(state.rotate));
                queue.submitCustom(matrices, state.layer, state.arm::renderClaw);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(state.rotate));
            } else {
                queue.submitCustom(matrices, state.layer, state.arm::renderClaw);
            }
            matrices.push();
            matrices.translate(0, -state.arm.clawOffset, -0.375f);
            queue.submitCustom(matrices, state.layer, state.arm::renderClawLower);
            matrices.pop();
            matrices.push();
            matrices.translate(0, state.arm.clawOffset, -0.375f);
            queue.submitCustom(matrices, state.layer, state.arm::renderClawUpper);
            matrices.pop();
            if (state.item != null) {
                state.item.render(matrices, queue, state.lightmapCoordinates);
            }
        }
    }

    @Override
    protected RenderLayer getRenderType(ArmBlockEntity be, BlockState state) {
        return be.goggles ? RenderLayer.getCutout() : RenderLayer.getSolid();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ArmBlockEntity be, ArmRenderState state) {
        return CachedBuffers.partial(AllPartialModels.ARM_COG, state.blockState);
    }

    public static void transformClawHalf(TransformStack<?> msr, boolean hasItem, boolean isBlockItem, int flip) {
        msr.translate(0, -flip * (hasItem ? isBlockItem ? 3 / 16f : 5 / 64f : 1 / 16f), -6 / 16d);
    }

    public static void transformHead(TransformStack<?> msr, float headAngle) {
        msr.translate(0, 0, -15 / 16d);
        msr.rotateXDegrees(headAngle - 45f);
    }

    public static void transformUpperArm(TransformStack<?> msr, float upperArmAngle) {
        msr.translate(0, 0, -14 / 16d);
        msr.rotateXDegrees(upperArmAngle - 90);
    }

    public static void transformLowerArm(TransformStack<?> msr, float lowerArmAngle) {
        msr.translate(0, 2 / 16d, 0);
        msr.rotateXDegrees(lowerArmAngle + 135);
    }

    public static void transformBase(TransformStack<?> msr, float baseAngle) {
        msr.translate(0, 4 / 16d, 0);
        msr.rotateYDegrees(baseAngle);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    public static class ArmRenderState extends KineticRenderState {
        public float rotate;
        public float baseAngle;
        public float lowerArmAngle;
        public float upperArmAngle;
        public float headAngle;
        public ArmRenderData arm;
        public ArmItemData item;
    }

    public static class ArmItemData {
        public ItemRenderState state;
        public float xRot;
        public float offset;
        public float scale;

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(xRot));
            matrices.translate(0, offset, 0);
            matrices.scale(scale, scale, scale);
            state.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
        }
    }

    public static class ArmRenderData {
        public SuperByteBuffer base;
        public SuperByteBuffer lower;
        public SuperByteBuffer upper;
        public SuperByteBuffer claw;
        public SuperByteBuffer clawUpper;
        public SuperByteBuffer clawLower;
        public int light;
        public int color;
        public boolean inverted;
        public float clawOffset;

        public void renderBase(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            base.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderClaw(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            claw.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderClawLower(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            clawLower.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderClawUpper(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            clawUpper.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderLower(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            lower.light(light).color(color).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderUpper(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            upper.light(light).color(color).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
