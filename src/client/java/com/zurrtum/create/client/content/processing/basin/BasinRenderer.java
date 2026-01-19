package com.zurrtum.create.client.content.processing.basin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasinRenderer extends SmartBlockEntityRenderer<BasinBlockEntity, BasinRenderer.BasinRenderState> {
    public BasinRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BasinRenderState createRenderState() {
        return new BasinRenderState();
    }

    @Override
    public void extractRenderState(
        BasinBlockEntity be,
        BasinRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        float fluidLevel = updateFluids(be, state, tickProgress);
        updateIngredients(be, state, tickProgress, fluidLevel);
        updateOutputs(be, state, tickProgress);
    }

    @Override
    public void submit(BasinRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.fluids != null) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
        if (state.ingredients != null) {
            matrices.pushPose();
            matrices.translate(.5, .2f, .5);
            matrices.mulPose(Axis.YP.rotation(state.ingredientYRot));
            for (IngredientRenderData ingredient : state.ingredients) {
                matrices.pushPose();
                matrices.translate(ingredient.itemPosition);
                matrices.mulPose(Axis.YP.rotation(ingredient.yRot));
                matrices.mulPose(Axis.XP.rotation(state.ingredientXRot));
                for (Vec3 offset : ingredient.offsets) {
                    matrices.pushPose();
                    matrices.translate(offset);
                    ingredient.renderState.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    matrices.popPose();
                }
                matrices.popPose();
            }
            matrices.popPose();
        }
        if (state.outputs != null) {
            for (OutputItemRenderData item : state.outputs) {
                matrices.pushPose();
                matrices.translate(item.offset);
                matrices.mulPose(Axis.YP.rotation(state.outputYRot));
                matrices.mulPose(Axis.XP.rotation(item.xRot));
                item.renderState.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                matrices.popPose();
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

                float partial = Mth.clamp(units / totalUnits, 0, 1);
                xMax += partial * 12 / 16f;
                fluids.add(new FluidRenderData(renderedFluid.getFluid(), renderedFluid.getComponentChanges(), xMin, xMax));

                xMin = xMax;
            }
        }
        if (fluids.isEmpty()) {
            return 0;
        }
        float fluidLevel = Mth.clamp(totalUnits / (BucketFluidInventory.CAPACITY * 2), 0, 1);
        fluidLevel = 1 - ((1 - fluidLevel) * (1 - fluidLevel));
        state.layer = RenderTypes.translucentMovingBlock();
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
        for (int slot = 0, size = inv.getContainerSize(); slot < size; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            stacks.add(stack);
        }
        int itemCount = stacks.size();
        if (itemCount == 0) {
            return;
        }
        float level = Mth.clamp(fluidLevel - .3f, .125f, .6f);
        RandomSource r = RandomSource.create(state.blockPos.hashCode());
        Vec3 baseVector = new Vec3(itemCount == 1 ? 0 : .125, level, 0);
        Level world = be.getLevel();
        float time = AnimationTickHolder.getRenderTime(world);
        float anglePartition = 360f / itemCount;
        IngredientRenderData[] ingredients = new IngredientRenderData[itemCount];
        for (int i = 0, size = itemCount; i < size; i++) {
            ItemStack stack = stacks.get(i);
            Vec3 itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, net.minecraft.core.Direction.Axis.Y);
            if (fluidLevel > 0) {
                itemPosition = itemPosition.add(0, (Mth.sin(time / 12f + anglePartition * itemCount) + 1.5f) * 1 / 32f, 0);
            }
            float yRot = Mth.DEG_TO_RAD * (anglePartition * itemCount + 35);
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.GROUND;
            itemModelManager.appendItemLayers(renderState, stack, renderState.displayContext, world, null, 0);
            int count = stack.getCount() / 8 + 1;
            Vec3[] offsets = new Vec3[count];
            for (int j = 0; j < count; j++) {
                offsets[j] = VecHelper.offsetRandomly(Vec3.ZERO, r, 1 / 16f);
            }
            ingredients[i] = new IngredientRenderData(renderState, itemPosition, yRot, offsets);
            itemCount--;
        }
        state.ingredientYRot = Mth.DEG_TO_RAD * be.ingredientRotation.getValue(partialTicks);
        state.ingredientXRot = Mth.DEG_TO_RAD * 65;
        state.ingredients = ingredients;
    }

    private void updateOutputs(BasinBlockEntity be, BasinRenderState state, float partialTicks) {
        if (!(state.blockState.getBlock() instanceof BasinBlock)) {
            return;
        }
        Direction direction = state.blockState.getValue(BasinBlock.FACING);
        if (direction == Direction.DOWN) {
            return;
        }
        List<IntAttached<ItemStack>> visualizedOutputItems = be.visualizedOutputItems;
        if (visualizedOutputItems.isEmpty()) {
            return;
        }
        Vec3 directionVec = Vec3.atLowerCornerOf(direction.getUnitVec3i());
        Vec3 outVec = VecHelper.getCenterOf(BlockPos.ZERO).add(directionVec.scale(.55).subtract(0, 1 / 2f, 0));
        Level world = be.getLevel();
        boolean outToBasin = world.getBlockState(state.blockPos.relative(direction)).getBlock() instanceof BasinBlock;
        List<OutputItemRenderData> outputs = new ArrayList<>();
        for (IntAttached<ItemStack> intAttached : visualizedOutputItems) {
            float progress = 1 - (intAttached.getFirst() - partialTicks) / BasinBlockEntity.OUTPUT_ANIMATION_TIME;
            if (!outToBasin && progress > .35f) {
                continue;
            }
            Vec3 offset = outVec.add(0, Math.max(-.55f, -(progress * progress * 2)), 0).add(directionVec.scale(progress * .5f));
            float xRot = Mth.DEG_TO_RAD * progress * 180;
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.GROUND;
            itemModelManager.appendItemLayers(renderState, intAttached.getValue(), renderState.displayContext, world, null, 0);
            outputs.add(new OutputItemRenderData(renderState, offset, xRot));
        }
        if (outputs.isEmpty()) {
            return;
        }
        state.outputYRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(direction);
        state.outputs = outputs;
    }

    @Override
    public int getViewDistance() {
        return 16;
    }

    public static class BasinRenderState extends SmartRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float yMin, yMax, zMin, zMax;
        public List<FluidRenderData> fluids;
        public float ingredientYRot, ingredientXRot;
        public IngredientRenderData[] ingredients;
        public float outputYRot;
        public List<OutputItemRenderData> outputs;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
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
                    lightCoords,
                    false,
                    false
                );
            }
        }
    }

    public record FluidRenderData(Fluid fluid, DataComponentPatch changes, float xMin, float xMax) {
    }

    public record IngredientRenderData(ItemStackRenderState renderState, Vec3 itemPosition, float yRot, Vec3[] offsets) {
    }

    public record OutputItemRenderData(ItemStackRenderState renderState, Vec3 offset, float xRot) {
    }
}
