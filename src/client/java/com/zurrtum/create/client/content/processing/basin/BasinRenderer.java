package com.zurrtum.create.client.content.processing.basin;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasinRenderer extends SmartBlockEntityRenderer<BasinBlockEntity, BasinRenderer.BasinRenderState> {
    public BasinRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public BasinRenderState createRenderState() {
        return new BasinRenderState();
    }

    @Override
    public void updateRenderState(
        BasinBlockEntity be,
        BasinRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        float fluidLevel = updateFluids(be, state, tickProgress);
        updateIngredients(be, state, tickProgress, fluidLevel);
        updateOutputs(be, state, tickProgress);
    }

    @Override
    public void render(BasinRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.fluids != null) {
            queue.submitCustom(matrices, state.layer, state);
        }
        if (state.ingredients != null) {
            matrices.push();
            matrices.translate(.5, .2f, .5);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.ingredientYRot));
            for (IngredientRenderData ingredient : state.ingredients) {
                matrices.push();
                matrices.translate(ingredient.itemPosition);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(ingredient.yRot));
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.ingredientXRot));
                for (Vec3d offset : ingredient.offsets) {
                    matrices.push();
                    matrices.translate(offset);
                    ingredient.renderState.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
                    matrices.pop();
                }
                matrices.pop();
            }
            matrices.pop();
        }
        if (state.outputs != null) {
            for (OutputItemRenderData item : state.outputs) {
                matrices.push();
                matrices.translate(item.offset);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.outputYRot));
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(item.xRot));
                item.renderState.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
                matrices.pop();
            }
        }
    }

    public float updateFluids(BasinBlockEntity basin, BasinRenderState state, float partialTicks) {
        float totalUnits = basin.getTotalFluidUnits(partialTicks);
        if (totalUnits < 1)
            return 0;
        float xMin = 2 / 16f;
        float xMax = 2 / 16f;
        List<FluidRenderData> fluids = new ArrayList<>();
        for (SmartFluidTankBehaviour behaviour : List.of(
            basin.getBehaviour(SmartFluidTankBehaviour.INPUT),
            basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
        )) {
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
                fluids.add(new FluidRenderData(renderedFluid.getFluid(), renderedFluid.getComponentChanges(), xMin, xMax));

                xMin = xMax;
            }
        }
        if (fluids.isEmpty()) {
            return 0;
        }
        float fluidLevel = MathHelper.clamp(totalUnits / (BucketFluidInventory.CAPACITY * 2), 0, 1);
        fluidLevel = 1 - ((1 - fluidLevel) * (1 - fluidLevel));
        state.layer = PonderRenderTypes.fluid();
        state.fluids = fluids;
        state.yMin = 2 / 16f;
        state.yMax = state.yMin + 12 / 16f * fluidLevel;
        state.zMin = 2 / 16f;
        state.zMax = 14 / 16f;
        return state.yMax;
    }

    public void updateIngredients(BasinBlockEntity be, BasinRenderState state, float partialTicks, float fluidLevel) {
        BasinInventory inv = be.itemCapability;
        if (inv == null) {
            return;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0, size = inv.size(); slot < size; slot++) {
            ItemStack stack = inv.getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }
            stacks.add(stack);
        }
        int itemCount = stacks.size();
        if (itemCount == 0) {
            return;
        }
        float level = MathHelper.clamp(fluidLevel - .3f, .125f, .6f);
        Random r = Random.create(state.pos.hashCode());
        Vec3d baseVector = new Vec3d(itemCount == 1 ? 0 : .125, level, 0);
        World world = be.getWorld();
        float time = AnimationTickHolder.getRenderTime(world);
        float anglePartition = 360f / itemCount;
        IngredientRenderData[] ingredients = new IngredientRenderData[itemCount];
        for (int i = 0, size = itemCount; i < size; i++) {
            ItemStack stack = stacks.get(i);
            Vec3d itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Axis.Y);
            if (fluidLevel > 0) {
                itemPosition = itemPosition.add(0, (MathHelper.sin(time / 12f + anglePartition * itemCount) + 1.5f) * 1 / 32f, 0);
            }
            float yRot = MathHelper.RADIANS_PER_DEGREE * (anglePartition * itemCount + 35);
            ItemRenderState renderState = new ItemRenderState();
            renderState.displayContext = ItemDisplayContext.GROUND;
            itemModelManager.update(renderState, stack, renderState.displayContext, world, null, 0);
            int count = stack.getCount() / 8 + 1;
            Vec3d[] offsets = new Vec3d[count];
            for (int j = 0; j < count; j++) {
                offsets[j] = VecHelper.offsetRandomly(Vec3d.ZERO, r, 1 / 16f);
            }
            ingredients[i] = new IngredientRenderData(renderState, itemPosition, yRot, offsets);
            itemCount--;
        }
        state.ingredientYRot = MathHelper.RADIANS_PER_DEGREE * be.ingredientRotation.getValue(partialTicks);
        state.ingredientXRot = MathHelper.RADIANS_PER_DEGREE * 65;
        state.ingredients = ingredients;
    }

    private void updateOutputs(BasinBlockEntity be, BasinRenderState state, float partialTicks) {
        if (!(state.blockState.getBlock() instanceof BasinBlock)) {
            return;
        }
        Direction direction = state.blockState.get(BasinBlock.FACING);
        if (direction == Direction.DOWN) {
            return;
        }
        List<IntAttached<ItemStack>> visualizedOutputItems = be.visualizedOutputItems;
        if (visualizedOutputItems.isEmpty()) {
            return;
        }
        Vec3d directionVec = Vec3d.of(direction.getVector());
        Vec3d outVec = VecHelper.getCenterOf(BlockPos.ZERO).add(directionVec.multiply(.55).subtract(0, 1 / 2f, 0));
        World world = be.getWorld();
        boolean outToBasin = world.getBlockState(state.pos.offset(direction)).getBlock() instanceof BasinBlock;
        List<OutputItemRenderData> outputs = new ArrayList<>();
        for (IntAttached<ItemStack> intAttached : visualizedOutputItems) {
            float progress = 1 - (intAttached.getFirst() - partialTicks) / BasinBlockEntity.OUTPUT_ANIMATION_TIME;
            if (!outToBasin && progress > .35f) {
                continue;
            }
            Vec3d offset = outVec.add(0, Math.max(-.55f, -(progress * progress * 2)), 0).add(directionVec.multiply(progress * .5f));
            float xRot = MathHelper.RADIANS_PER_DEGREE * progress * 180;
            ItemRenderState renderState = new ItemRenderState();
            renderState.displayContext = ItemDisplayContext.GROUND;
            itemModelManager.update(renderState, intAttached.getValue(), renderState.displayContext, world, null, 0);
            outputs.add(new OutputItemRenderData(renderState, offset, xRot));
        }
        if (outputs.isEmpty()) {
            return;
        }
        state.outputYRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(direction);
        state.outputs = outputs;
    }

    @Override
    public int getRenderDistance() {
        return 16;
    }

    public static class BasinRenderState extends SmartRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float yMin, yMax, zMin, zMax;
        public List<FluidRenderData> fluids;
        public float ingredientYRot, ingredientXRot;
        public IngredientRenderData[] ingredients;
        public float outputYRot;
        public List<OutputItemRenderData> outputs;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            for (FluidRenderData data : fluids) {
                FluidRenderHelper.renderFluidBox(
                    data.fluid,
                    data.changes,
                    data.xMin,
                    yMin,
                    zMin,
                    data.xMax,
                    yMax,
                    zMax,
                    vertexConsumer,
                    matricesEntry,
                    lightmapCoordinates,
                    false,
                    false
                );
            }
        }
    }

    public record FluidRenderData(Fluid fluid, ComponentChanges changes, float xMin, float xMax) {
    }

    public record IngredientRenderData(ItemRenderState renderState, Vec3d itemPosition, float yRot, Vec3d[] offsets) {
    }

    public record OutputItemRenderData(ItemRenderState renderState, Vec3d offset, float xRot) {
    }
}
