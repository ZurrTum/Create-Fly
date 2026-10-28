package com.zurrtum.create.client.content.processing.basin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper.FluidRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.processing.basin.BasinRenderer.BasinRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasinRenderer implements BlockEntityRenderer<BasinBlockEntity, BasinRenderState> {
    protected final ItemModelResolver itemModelManager;
    protected final FluidStateModelSet fluidStateModelSet;

    public BasinRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
        fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockPos blockPos = be.getBlockPos();
        BlockState blockState = be.getBlockState();
        int lightCoords = SmartBlockEntityRenderer.getLightCoords(be.getLevel(), blockPos);
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(blockPos))
        );
        float fluidLevel = updateFluids(be, blockPos, lightCoords, state, tickProgress);
        updateIngredients(be, blockPos, state, tickProgress, fluidLevel);
        updateOutputs(be, blockPos, blockState, state, tickProgress);
        if (state.filter != null || state.fluids != null || state.ingredients != null || state.outputs != null) {
            state.blockPos = blockPos;
            state.blockState = blockState;
            state.lightCoords = lightCoords;
            state.blockEntityType = be.getType();
        }
    }

    @Override
    public void submit(
        BasinRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.fluids != null) {
            for (FluidRenderState fluid : state.fluids) {
                fluid.submit(matrices, queue);
            }
        }
        if (state.ingredients != null) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate(0.5f, 0.2f, 0.5f);
            if (state.ingredientYRot != 0) {
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * state.ingredientYRot);
            }
            for (IngredientRenderData ingredient : state.ingredients) {
                matrices.pushPose();
                pose = matrices.last();
                Vec3 itemPosition = ingredient.itemPosition;
                pose.translate((float) itemPosition.x, (float) itemPosition.y, (float) itemPosition.z);
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * ingredient.yRot);
                pose.rotate(Axis.XP, Radians.DEG_65);
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
                Pose pose = matrices.last();
                Vec3 offset = item.offset;
                pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
                if (state.outputYRadians != 0) {
                    pose.rotate(Axis.YP, state.outputYRadians);
                }
                if (item.xRadians != 0) {
                    pose.rotate(Axis.XP, item.xRadians);
                }
                item.renderState.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                matrices.popPose();
            }
        }
    }

    public float updateFluids(
        BasinBlockEntity basin,
        BlockPos blockPos,
        int lightCoords,
        BasinRenderState state,
        float partialTicks
    ) {
        float totalUnits = basin.getTotalFluidUnits(partialTicks);
        if (totalUnits < 1) {
            return 0;
        }
        List<FluidRenderState> fluids = new ArrayList<>();
        BlockAndTintGetter level = (BlockAndTintGetter) basin.getLevel();
        float fluidLevel = Mth.clamp(totalUnits / (BucketFluidInventory.CAPACITY * 2), 0, 1);
        fluidLevel = 1 - (1 - fluidLevel) * (1 - fluidLevel);
        float xMin = 2 / 16.0f;
        float xMax = 2 / 16.0f;
        float yMin = 2 / 16.0f;
        float yMax = yMin + 12 / 16.0f * fluidLevel;
        float zMin = 2 / 16.0f;
        float zMax = 14 / 16.0f;
        for (SmartFluidTankBehaviour behaviour : List.of(
            basin.getBehaviour(SmartFluidTankBehaviour.INPUT),
            basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
        )) {
            if (behaviour == null) {
                continue;
            }
            for (TankSegment tankSegment : behaviour.getTanks()) {
                FluidStack renderedFluid = tankSegment.getRenderedFluid();
                if (renderedFluid.isEmpty()) {
                    continue;
                }
                float units = tankSegment.getTotalUnits(partialTicks);
                if (units < 1) {
                    continue;
                }
                xMax += Mth.clamp(units / totalUnits, 0, 1) * 12 / 16.0f;
                fluids.add(FluidRenderHelper.extractFluidRenderState(
                    level,
                    blockPos,
                    fluidStateModelSet,
                    renderedFluid.getFluid(),
                    renderedFluid.getComponentChanges(),
                    xMin,
                    yMin,
                    zMin,
                    xMax,
                    yMax,
                    zMax,
                    lightCoords,
                    false,
                    false
                ));
                xMin = xMax;
            }
        }
        if (fluids.isEmpty()) {
            return 0;
        }
        state.fluids = fluids;
        return yMax;
    }

    public void updateIngredients(
        BasinBlockEntity be,
        BlockPos blockPos,
        BasinRenderState state,
        float partialTicks,
        float fluidLevel
    ) {
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
        float level = Mth.clamp(fluidLevel - 0.3f, 0.125f, 0.6f);
        RandomSource r = RandomSource.create(blockPos.hashCode());
        Vec3 baseVector = new Vec3(itemCount == 1 ? 0 : 0.125, level, 0);
        Level world = be.getLevel();
        float time = AnimationTickHolder.getRenderTime(world);
        float anglePartition = 360.0f / itemCount;
        IngredientRenderData[] ingredients = new IngredientRenderData[itemCount];
        for (int i = 0, size = itemCount; i < size; i++) {
            ItemStack stack = stacks.get(i);
            Vec3 itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Direction.Axis.Y);
            if (fluidLevel > 0) {
                itemPosition = itemPosition.add(
                    0,
                    (Mth.sin(time / 12.0f + anglePartition * itemCount) + 1.5f) * 0.03125f,
                    0
                );
            }
            float yRot = anglePartition * itemCount + 35;
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.GROUND;
            itemModelManager.appendItemLayers(renderState, stack, renderState.displayContext, world, null, 0);
            int count = stack.getCount() / 8 + 1;
            Vec3[] offsets = new Vec3[count];
            for (int j = 0; j < count; j++) {
                offsets[j] = VecHelper.offsetRandomly(Vec3.ZERO, r, 0.0625f);
            }
            ingredients[i] = new IngredientRenderData(renderState, itemPosition, yRot, offsets);
            itemCount--;
        }
        state.ingredientYRot = be.ingredientRotation.getValue(partialTicks);
        state.ingredients = ingredients;
    }

    private void updateOutputs(
        BasinBlockEntity be,
        BlockPos blockPos,
        BlockState blockState,
        BasinRenderState state,
        float partialTicks
    ) {
        if (!(blockState.getBlock() instanceof BasinBlock)) {
            return;
        }
        Direction direction = blockState.getValue(BasinBlock.FACING);
        if (direction == Direction.DOWN) {
            return;
        }
        List<IntAttached<ItemStack>> visualizedOutputItems = be.visualizedOutputItems;
        if (visualizedOutputItems.isEmpty()) {
            return;
        }
        Vec3 directionVec = Vec3.atLowerCornerOf(direction.getUnitVec3i());
        Vec3 outVec = VecHelper.getCenterOf(BlockPos.ZERO).add(directionVec.scale(0.55).subtract(0, 0.5f, 0));
        Level world = be.getLevel();
        boolean outToBasin = world.getBlockState(blockPos.relative(direction)).getBlock() instanceof BasinBlock;
        List<OutputItemRenderData> outputs = new ArrayList<>();
        for (IntAttached<ItemStack> intAttached : visualizedOutputItems) {
            float progress = 1 - (intAttached.getFirst() - partialTicks) / BasinBlockEntity.OUTPUT_ANIMATION_TIME;
            if (!outToBasin && progress > 0.35f) {
                continue;
            }
            Vec3 offset = outVec.add(0, Math.max(-0.55f, -(progress * progress * 2)), 0)
                .add(directionVec.scale(progress * 0.5f));
            float xRadians = progress * Radians.DEG_180;
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.GROUND;
            itemModelManager.appendItemLayers(
                renderState,
                intAttached.getValue(),
                renderState.displayContext,
                world,
                null,
                0
            );
            outputs.add(new OutputItemRenderData(renderState, offset, xRadians));
        }
        if (outputs.isEmpty()) {
            return;
        }
        state.outputYRadians = Radians.horizontal(direction);
        state.outputs = outputs;
    }

    @Override
    public int getViewDistance() {
        return 16;
    }

    public static class BasinRenderState extends BlockEntityRenderState {
        public @UnknownNullability FilterRenderState filter;
        public @Nullable List<FluidRenderState> fluids;
        public float ingredientYRot;
        public IngredientRenderData @Nullable [] ingredients;
        public float outputYRadians;
        public @Nullable List<OutputItemRenderData> outputs;
    }

    public record IngredientRenderData(ItemStackRenderState renderState, Vec3 itemPosition, float yRot,
                                       Vec3[] offsets) {
    }

    public record OutputItemRenderData(ItemStackRenderState renderState, Vec3 offset, float xRadians) {
    }
}
