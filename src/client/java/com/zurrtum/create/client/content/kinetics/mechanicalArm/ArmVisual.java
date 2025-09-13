package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.google.common.collect.Lists;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.AbstractInstance;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ArmVisual extends SingleAxisRotatingVisual<ArmBlockEntity> implements SimpleDynamicVisual {

    final TransformedInstance base;
    final TransformedInstance lowerBody;
    final TransformedInstance upperBody;
    final TransformedInstance claw;

    private final ArrayList<TransformedInstance> clawGrips;
    private final ArrayList<TransformedInstance> models;
    private final boolean ceiling;

    private final MatrixStack poseStack = new MatrixStack();
    private final ItemRenderState itemRenderState = new ItemRenderState();

    private boolean wasDancing = false;
    private float baseAngle = Float.NaN;
    private float lowerArmAngle = Float.NaN;
    private float upperArmAngle = Float.NaN;
    private float headAngle = Float.NaN;

    public ArmVisual(VisualizationContext context, ArmBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.ARM_COG));

        base = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_BASE)).createInstance();
        lowerBody = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_LOWER_BODY)).createInstance();
        upperBody = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_UPPER_BODY)).createInstance();
        claw = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.partial(blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE)
        ).createInstance();

        TransformedInstance clawGrip1 = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_CLAW_GRIP_UPPER))
            .createInstance();
        TransformedInstance clawGrip2 = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_CLAW_GRIP_LOWER))
            .createInstance();

        clawGrips = Lists.newArrayList(clawGrip1, clawGrip2);
        models = Lists.newArrayList(base, lowerBody, upperBody, claw, clawGrip1, clawGrip2);
        ceiling = blockState.get(ArmBlock.CEILING);

        var msr = TransformStack.of(poseStack);
        msr.translate(getVisualPosition());
        msr.center();

        if (ceiling)
            msr.rotateXDegrees(180);

        animate(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
        if (blockEntity.phase == ArmBlockEntity.Phase.DANCING && blockEntity.getSpeed() != 0) {
            animateRave(pt);
            wasDancing = true;
            return;
        }

        float baseAngleNow = blockEntity.baseAngle.getValue(pt);
        float lowerArmAngleNow = blockEntity.lowerArmAngle.getValue(pt);
        float upperArmAngleNow = blockEntity.upperArmAngle.getValue(pt);
        float headAngleNow = blockEntity.headAngle.getValue(pt);

        boolean settled = MathHelper.approximatelyEquals(baseAngle, baseAngleNow) && MathHelper.approximatelyEquals(
            lowerArmAngle,
            lowerArmAngleNow
        ) && MathHelper.approximatelyEquals(upperArmAngle, upperArmAngleNow) && MathHelper.approximatelyEquals(
            headAngle,
            headAngleNow
        );

        this.baseAngle = baseAngleNow;
        this.lowerArmAngle = lowerArmAngleNow;
        this.upperArmAngle = upperArmAngleNow;
        this.headAngle = headAngleNow;

        // Need to reset the animation if the arm is dancing. We'd very likely be settled
        if (!settled || wasDancing)
            animateArm();

        wasDancing = false;
    }

    private void animateRave(float partialTick) {
        var ticks = AnimationTickHolder.getTicks(blockEntity.getWorld());
        float renderTick = ticks + partialTick + (blockEntity.hashCode() % 64);

        float baseAngle = (renderTick * 10) % 360;
        float lowerArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 4) + 1) / 2, -45, 15);
        float upperArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 8) + 1) / 4, -45, 95);
        float headAngle = -lowerArmAngle;
        int color = Color.rainbowColor(ticks * 100).getRGB();
        updateAngles(baseAngle, lowerArmAngle, upperArmAngle, headAngle, color);
    }

    private void animateArm() {
        updateAngles(this.baseAngle, this.lowerArmAngle - 135, this.upperArmAngle - 90, this.headAngle, 0xFFFFFF);
    }

    private void updateAngles(float baseAngle, float lowerArmAngle, float upperArmAngle, float headAngle, int color) {
        poseStack.push();

        var msr = TransformStack.of(poseStack);

        ArmRenderer.transformBase(msr, baseAngle);
        base.setTransform(poseStack).setChanged();

        ArmRenderer.transformLowerArm(msr, lowerArmAngle);
        lowerBody.setTransform(poseStack).colorRgb(color).setChanged();

        ArmRenderer.transformUpperArm(msr, upperArmAngle);
        upperBody.setTransform(poseStack).colorRgb(color).setChanged();

        ArmRenderer.transformHead(msr, headAngle);

        if (ceiling && blockEntity.goggles)
            msr.rotateZDegrees(180);

        claw.setTransform(poseStack).setChanged();

        if (ceiling && blockEntity.goggles)
            msr.rotateZDegrees(180);

        ItemStack item = blockEntity.heldItem;
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        boolean hasItem = !item.isEmpty();
        boolean isBlockItem;
        if (hasItem && item.getItem() instanceof BlockItem) {
            itemRenderer.itemModelManager.clearAndUpdate(itemRenderState, item, ItemDisplayContext.FIXED, mc.world, null, 0);
            isBlockItem = itemRenderState.isSideLit();
        } else {
            isBlockItem = false;
        }

        for (int index : Iterate.zeroAndOne) {
            poseStack.push();
            int flip = index * 2 - 1;
            ArmRenderer.transformClawHalf(msr, hasItem, isBlockItem, flip);
            clawGrips.get(index).setTransform(poseStack).setChanged();
            poseStack.pop();
        }

        poseStack.pop();
    }

    @Override
    public void update(float pt) {
        super.update(pt);
        instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.partial(blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE)
        ).stealInstance(claw);
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);

        relight(models.toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        super._delete();
        models.forEach(AbstractInstance::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        models.forEach(consumer);
    }
}
