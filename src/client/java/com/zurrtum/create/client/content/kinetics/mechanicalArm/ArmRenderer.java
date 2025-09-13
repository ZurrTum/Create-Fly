package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ArmRenderer extends KineticBlockEntityRenderer<ArmBlockEntity> {

    public ArmRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ArmBlockEntity be, float pt, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, pt, ms, buffer, light, overlay);

        ItemStack item = be.heldItem;
        boolean hasItem = !item.isEmpty();
        World world = be.getWorld();
        boolean usingFlywheel = VisualizationManager.supportsVisualization(world);

        if (usingFlywheel && !hasItem)
            return;

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        boolean isBlockItem;
        if (hasItem) {
            itemRenderer.itemModelManager.clearAndUpdate(itemRenderer.itemRenderState, item, ItemDisplayContext.FIXED, world, null, 0);
            if (item.getItem() instanceof BlockItem) {
                isBlockItem = itemRenderer.itemRenderState.isSideLit();
            } else {
                isBlockItem = false;
            }
        } else {
            isBlockItem = false;
        }

        VertexConsumer builder = buffer.getBuffer(be.goggles ? RenderLayer.getCutout() : RenderLayer.getSolid());
        BlockState blockState = be.getCachedState();

        MatrixStack msLocal = new MatrixStack();
        var msr = TransformStack.of(msLocal);

        float baseAngle;
        float lowerArmAngle;
        float upperArmAngle;
        float headAngle;
        int color;
        boolean inverted = blockState.get(ArmBlock.CEILING);

        boolean rave = be.phase == Phase.DANCING && be.getSpeed() != 0;
        if (rave) {
            float renderTick = AnimationTickHolder.getRenderTime(world) + (be.hashCode() % 64);
            baseAngle = (renderTick * 10) % 360;
            lowerArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 4) + 1) / 2, -45, 15);
            upperArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 8) + 1) / 4, -45, 95);
            headAngle = -lowerArmAngle;
            color = Color.rainbowColor(AnimationTickHolder.getTicks() * 100).getRGB();
        } else {
            baseAngle = be.baseAngle.getValue(pt);
            lowerArmAngle = be.lowerArmAngle.getValue(pt) - 135;
            upperArmAngle = be.upperArmAngle.getValue(pt) - 90;
            headAngle = be.headAngle.getValue(pt);
            color = 0xFFFFFF;
        }

        msr.center();

        if (inverted)
            msr.rotateXDegrees(180);

        if (usingFlywheel)
            doItemTransforms(msr, baseAngle, lowerArmAngle, upperArmAngle, headAngle);
        else
            renderArm(
                builder,
                ms,
                msLocal,
                msr,
                blockState,
                color,
                baseAngle,
                lowerArmAngle,
                upperArmAngle,
                headAngle,
                be.goggles,
                inverted && be.goggles,
                hasItem,
                isBlockItem,
                light
            );

        if (hasItem) {
            ms.push();
            float itemScale = isBlockItem ? .5f : .625f;
            msr.rotateXDegrees(90);
            msLocal.translate(0, isBlockItem ? -9 / 16f : -10 / 16f, 0);
            msLocal.scale(itemScale, itemScale, itemScale);

            ms.peek().getPositionMatrix().mul(msLocal.peek().getPositionMatrix());

            itemRenderer.itemRenderState.render(ms, buffer, light, overlay);
            ms.pop();
        }

    }

    private void renderArm(
        VertexConsumer builder,
        MatrixStack ms,
        MatrixStack msLocal,
        TransformStack msr,
        BlockState blockState,
        int color,
        float baseAngle,
        float lowerArmAngle,
        float upperArmAngle,
        float headAngle,
        boolean goggles,
        boolean inverted,
        boolean hasItem,
        boolean isBlockItem,
        int light
    ) {
        SuperByteBuffer base = CachedBuffers.partial(AllPartialModels.ARM_BASE, blockState).light(light);
        SuperByteBuffer lowerBody = CachedBuffers.partial(AllPartialModels.ARM_LOWER_BODY, blockState).light(light);
        SuperByteBuffer upperBody = CachedBuffers.partial(AllPartialModels.ARM_UPPER_BODY, blockState).light(light);
        SuperByteBuffer claw = CachedBuffers.partial(goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE, blockState)
            .light(light);
        SuperByteBuffer upperClawGrip = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_UPPER, blockState).light(light);
        SuperByteBuffer lowerClawGrip = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_LOWER, blockState).light(light);

        transformBase(msr, baseAngle);
        base.transform(msLocal).renderInto(ms, builder);

        transformLowerArm(msr, lowerArmAngle);
        lowerBody.color(color).transform(msLocal).renderInto(ms, builder);

        transformUpperArm(msr, upperArmAngle);
        upperBody.color(color).transform(msLocal).renderInto(ms, builder);

        transformHead(msr, headAngle);

        if (inverted)
            msr.rotateZDegrees(180);
        claw.transform(msLocal).renderInto(ms, builder);

        if (inverted)
            msr.rotateZDegrees(180);

        for (int flip : Iterate.positiveAndNegative) {
            msLocal.push();
            transformClawHalf(msr, hasItem, isBlockItem, flip);
            (flip > 0 ? lowerClawGrip : upperClawGrip).transform(msLocal).renderInto(ms, builder);
            msLocal.pop();
        }
    }

    private void doItemTransforms(TransformStack msr, float baseAngle, float lowerArmAngle, float upperArmAngle, float headAngle) {

        transformBase(msr, baseAngle);
        transformLowerArm(msr, lowerArmAngle);
        transformUpperArm(msr, upperArmAngle);
        transformHead(msr, headAngle);
    }

    public static void transformClawHalf(TransformStack msr, boolean hasItem, boolean isBlockItem, int flip) {
        msr.translate(0, -flip * (hasItem ? isBlockItem ? 3 / 16f : 5 / 64f : 1 / 16f), -6 / 16d);
    }

    public static void transformHead(TransformStack msr, float headAngle) {
        msr.translate(0, 0, -15 / 16d);
        msr.rotateXDegrees(headAngle - 45f);
    }

    public static void transformUpperArm(TransformStack msr, float upperArmAngle) {
        msr.translate(0, 0, -14 / 16d);
        msr.rotateXDegrees(upperArmAngle - 90);
    }

    public static void transformLowerArm(TransformStack msr, float lowerArmAngle) {
        msr.translate(0, 2 / 16d, 0);
        msr.rotateXDegrees(lowerArmAngle + 135);
    }

    public static void transformBase(TransformStack msr, float baseAngle) {
        msr.translate(0, 4 / 16d, 0);
        msr.rotateYDegrees(baseAngle);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ArmBlockEntity be, BlockState state) {
        return CachedBuffers.partial(AllPartialModels.ARM_COG, state);
    }

}
