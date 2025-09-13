package com.zurrtum.create.client.content.processing.basin;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class BasinRenderer extends SmartBlockEntityRenderer<BasinBlockEntity> {

    public BasinRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BasinBlockEntity basin, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(basin, partialTicks, ms, buffer, light, overlay);

        float fluidLevel = renderFluids(basin, partialTicks, ms, buffer, light, overlay);
        float level = MathHelper.clamp(fluidLevel - .3f, .125f, .6f);

        ms.push();

        BlockPos pos = basin.getPos();
        ms.translate(.5, .2f, .5);
        TransformStack.of(ms).rotateYDegrees(basin.ingredientRotation.getValue(partialTicks));

        Random r = Random.create(pos.hashCode());
        Vec3d baseVector = new Vec3d(.125, level, 0);

        World world = basin.getWorld();
        BasinInventory inv = basin.itemCapability;
        if (inv != null) {
            int size = inv.size();
            int itemCount = 0;
            for (int slot = 0; slot < size; slot++)
                if (!inv.getStack(slot).isEmpty())
                    itemCount++;

            if (itemCount == 1)
                baseVector = new Vec3d(0, level, 0);

            float anglePartition = 360f / itemCount;
            for (int slot = 0; slot < size; slot++) {
                ItemStack stack = inv.getStack(slot);
                if (stack.isEmpty())
                    continue;

                ms.push();

                if (fluidLevel > 0) {
                    ms.translate(
                        0,
                        (MathHelper.sin(AnimationTickHolder.getRenderTime(world) / 12f + anglePartition * itemCount) + 1.5f) * 1 / 32f,
                        0
                    );
                }

                Vec3d itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Axis.Y);
                ms.translate(itemPosition.x, itemPosition.y, itemPosition.z);
                TransformStack.of(ms).rotateYDegrees(anglePartition * itemCount + 35).rotateXDegrees(65);

                for (int i = 0; i <= stack.getCount() / 8; i++) {
                    ms.push();

                    Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, 1 / 16f);

                    ms.translate(vec.x, vec.y, vec.z);
                    renderItem(ms, buffer, light, overlay, stack);
                    ms.pop();
                }
                ms.pop();

                itemCount--;
            }
        }
        ms.pop();

        BlockState blockState = basin.getCachedState();
        if (!(blockState.getBlock() instanceof BasinBlock))
            return;
        Direction direction = blockState.get(BasinBlock.FACING);
        if (direction == Direction.DOWN)
            return;
        Vec3d directionVec = Vec3d.of(direction.getVector());
        Vec3d outVec = VecHelper.getCenterOf(BlockPos.ZERO).add(directionVec.multiply(.55).subtract(0, 1 / 2f, 0));

        boolean outToBasin = world.getBlockState(basin.getPos().offset(direction)).getBlock() instanceof BasinBlock;

        for (IntAttached<ItemStack> intAttached : basin.visualizedOutputItems) {
            float progress = 1 - (intAttached.getFirst() - partialTicks) / BasinBlockEntity.OUTPUT_ANIMATION_TIME;

            if (!outToBasin && progress > .35f)
                continue;

            ms.push();
            TransformStack.of(ms).translate(outVec).translate(new Vec3d(0, Math.max(-.55f, -(progress * progress * 2)), 0))
                .translate(directionVec.multiply(progress * .5f)).rotateYDegrees(AngleHelper.horizontalAngle(direction))
                .rotateXDegrees(progress * 180);
            renderItem(ms, buffer, light, overlay, intAttached.getValue());
            ms.pop();
        }
    }

    protected void renderItem(MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay, ItemStack stack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getItemRenderer().renderItem(stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, mc.world, 0);
    }

    protected float renderFluids(BasinBlockEntity basin, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        SmartFluidTankBehaviour inputFluids = basin.getBehaviour(SmartFluidTankBehaviour.INPUT);
        SmartFluidTankBehaviour outputFluids = basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT);
        SmartFluidTankBehaviour[] tanks = {inputFluids, outputFluids};
        float totalUnits = basin.getTotalFluidUnits(partialTicks);
        if (totalUnits < 1)
            return 0;

        float fluidLevel = MathHelper.clamp(totalUnits / (BucketFluidInventory.CAPACITY * 2), 0, 1);

        fluidLevel = 1 - ((1 - fluidLevel) * (1 - fluidLevel));

        float xMin = 2 / 16f;
        float xMax = 2 / 16f;
        final float yMin = 2 / 16f;
        final float yMax = yMin + 12 / 16f * fluidLevel;
        final float zMin = 2 / 16f;
        final float zMax = 14 / 16f;

        for (SmartFluidTankBehaviour behaviour : tanks) {
            if (behaviour == null)
                continue;
            for (TankSegment tankSegment : behaviour.getTanks()) {
                FluidStack renderedFluid = tankSegment.getRenderedFluid();
                if (renderedFluid.isEmpty())
                    continue;
                float units = tankSegment.getTotalUnits(partialTicks);
                if (units < 1)
                    continue;

                float partial = MathHelper.clamp(units / totalUnits, 0, 1);
                xMax += partial * 12 / 16f;
                FluidRenderHelper.renderFluidBox(renderedFluid, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, false, false);

                xMin = xMax;
            }
        }

        return yMax;
    }

    @Override
    public int getRenderDistance() {
        return 16;
    }

}
