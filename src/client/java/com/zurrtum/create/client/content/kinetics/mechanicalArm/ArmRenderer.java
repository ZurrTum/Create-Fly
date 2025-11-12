package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ArmRenderer extends KineticBlockEntityRenderer<ArmBlockEntity, ArmRenderer.ArmRenderState> {
    protected static ItemModelResolver itemModelManager;

    public ArmRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public ArmRenderState createRenderState() {
        return new ArmRenderState();
    }

    @Override
    public void extractRenderState(
        ArmBlockEntity be,
        ArmRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        ItemStack item = be.heldItem;
        boolean empty = item.isEmpty();
        if (state.support) {
            if (empty) {
                return;
            }
            BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        }
        Level world = be.getLevel();
        boolean isBlockItem;
        if (empty) {
            isBlockItem = false;
        } else {
            ArmItemData data = state.item = new ArmItemData();
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, item, renderState.displayContext, world, null, 0);
            isBlockItem = item.getItem() instanceof BlockItem && renderState.usesBlockLight();
            data.state = renderState;
            data.xRot = Mth.DEG_TO_RAD * 90;
            if (isBlockItem) {
                data.offset = -9 / 16f;
                data.scale = .5f;
            } else {
                data.offset = -10 / 16f;
                data.scale = .625f;
            }
        }
        boolean inverted = state.blockState.getValue(ArmBlock.CEILING);
        if (inverted) {
            state.rotate = Mth.DEG_TO_RAD * 180;
        }
        boolean rave = be.phase == Phase.DANCING && be.getSpeed() != 0;
        if (rave) {
            float renderTick = AnimationTickHolder.getRenderTime(world) + (be.hashCode() % 64);
            state.baseAngle = Mth.DEG_TO_RAD * ((renderTick * 10) % 360);
            state.lowerArmAngle = Mth.DEG_TO_RAD * (Mth.lerpInt((Mth.sin(renderTick / 4) + 1) / 2, -45, 15) - 135);
            state.upperArmAngle = Mth.DEG_TO_RAD * (Mth.lerpInt((Mth.sin(renderTick / 8) + 1) / 4, -45, 95) - 90);
            state.headAngle = Mth.DEG_TO_RAD * (-state.lowerArmAngle - 45);
        } else {
            state.baseAngle = Mth.DEG_TO_RAD * be.baseAngle.getValue(tickProgress);
            state.lowerArmAngle = Mth.DEG_TO_RAD * be.lowerArmAngle.getValue(tickProgress);
            state.upperArmAngle = Mth.DEG_TO_RAD * (be.upperArmAngle.getValue(tickProgress) - 180);
            state.headAngle = Mth.DEG_TO_RAD * (be.headAngle.getValue(tickProgress) - 45);
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
            data.light = state.lightCoords;
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
    public void submit(ArmRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (state.rotate != 0) {
            matrices.mulPose(Axis.XP.rotation(state.rotate));
        }
        matrices.translate(0, 0.25f, 0);
        matrices.mulPose(Axis.YP.rotation(state.baseAngle));
        if (state.support) {
            matrices.translate(0, 0.125f, 0);
            matrices.mulPose(Axis.XP.rotation(state.lowerArmAngle));
            matrices.translate(0, 0, -0.875f);
            matrices.mulPose(Axis.XP.rotation(state.upperArmAngle));
            matrices.translate(0, 0, -0.9375f);
            matrices.mulPose(Axis.XP.rotation(state.headAngle));
            state.item.render(matrices, queue, state.lightCoords);
        } else {
            queue.submitCustomGeometry(matrices, state.layer, state.arm::renderBase);
            matrices.translate(0, 0.125f, 0);
            matrices.mulPose(Axis.XP.rotation(state.lowerArmAngle));
            queue.submitCustomGeometry(matrices, state.layer, state.arm::renderLower);
            matrices.translate(0, 0, -0.875f);
            matrices.mulPose(Axis.XP.rotation(state.upperArmAngle));
            queue.submitCustomGeometry(matrices, state.layer, state.arm::renderUpper);
            matrices.translate(0, 0, -0.9375f);
            matrices.mulPose(Axis.XP.rotation(state.headAngle));
            if (state.arm.inverted) {
                matrices.mulPose(Axis.ZP.rotation(state.rotate));
                queue.submitCustomGeometry(matrices, state.layer, state.arm::renderClaw);
                matrices.mulPose(Axis.ZP.rotation(state.rotate));
            } else {
                queue.submitCustomGeometry(matrices, state.layer, state.arm::renderClaw);
            }
            matrices.pushPose();
            matrices.translate(0, -state.arm.clawOffset, -0.375f);
            queue.submitCustomGeometry(matrices, state.layer, state.arm::renderClawLower);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0, state.arm.clawOffset, -0.375f);
            queue.submitCustomGeometry(matrices, state.layer, state.arm::renderClawUpper);
            matrices.popPose();
            if (state.item != null) {
                state.item.render(matrices, queue, state.lightCoords);
            }
        }
    }

    @Override
    protected RenderType getRenderType(ArmBlockEntity be, BlockState state) {
        return be.goggles ? RenderType.cutout() : RenderType.solid();
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
    public boolean shouldRenderOffScreen() {
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
        public ItemStackRenderState state;
        public float xRot;
        public float offset;
        public float scale;

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light) {
            matrices.mulPose(Axis.XP.rotation(xRot));
            matrices.translate(0, offset, 0);
            matrices.scale(scale, scale, scale);
            state.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
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

        public void renderBase(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            base.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderClaw(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            claw.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderClawLower(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            clawLower.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderClawUpper(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            clawUpper.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderLower(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            lower.light(light).color(color).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderUpper(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            upper.light(light).color(color).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
