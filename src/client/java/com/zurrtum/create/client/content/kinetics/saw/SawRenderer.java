package com.zurrtum.create.client.content.kinetics.saw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.QuadRenderHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.saw.SawRenderer.SawRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class SawRenderer implements BlockEntityRenderer<SawBlockEntity, SawRenderState> {
    protected final ItemModelResolver itemModelManager;
    protected final TextureAtlasSprite sprite;

    public SawRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
        sprite = context.sprites().get(Sheets.BLOCKS_MAPPER.defaultNamespaceApply("stonecutter_saw"));
    }

    @Override
    public SawRenderState createRenderState() {
        return new SawRenderState();
    }

    @Override
    public void extractRenderState(
        SawBlockEntity be,
        SawRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        float speed = be.getSpeed();
        Direction facing = state.blockState.getValue(FACING);
        updateItems(facing, speed, be.inventory, level, state, tickProgress);
        if (!be.isRemoved()) {
            state.filter = FilteringRenderer.getFilterRenderState(
                be,
                state.blockState,
                itemModelManager,
                be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
            );
        }
        if (VisualizationManager.supportsVisualization(level)) {
            return;
        }
        Direction.Axis axis = facing.getAxis();
        PartialModel partial;
        if (axis == Direction.Axis.Y) {
            if (state.blockState.getValue(AXIS_ALONG_FIRST_COORDINATE)) {
                state.alongFirst = true;
                axis = Direction.Axis.X;
            } else {
                axis = Direction.Axis.Z;
            }
            state.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
                .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            if (speed > 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE;
                QuadRenderHelper.markSpriteActive(sprite);
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_REVERSED;
                QuadRenderHelper.markSpriteActive(sprite);
            } else {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE;
            }
        } else {
            state.shaft = CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF,
                state.blockState.getBlock().rotate(state.blockState, Rotation.CLOCKWISE_180)
            ).cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            if (speed > 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE;
                QuadRenderHelper.markSpriteActive(sprite);
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_REVERSED;
                QuadRenderHelper.markSpriteActive(sprite);
            } else {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE;
            }
        }
        state.blade = CachedBuffers.partialFacing(partial, state.blockState, facing).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).extractRenderState();
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
    }

    @Override
    public void submit(
        SawRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.blade != null) {
            if (state.alongFirst) {
                matrices.pushPose();
                matrices.rotateYCentered(Radians.DEG_90);
                state.blade.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.blade.submit(matrices, queue);
            }
        }
        if (state.shaft != null) {
            if (state.angle != 0) {
                matrices.pushPose();
                matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
                state.shaft.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.shaft.submit(matrices, queue);
            }
        }
        if (state.items != null) {
            renderItems(state, matrices, queue);
        }
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
    }

    public void updateItems(
        Direction facing,
        float speed,
        ProcessingInventory inventory,
        @Nullable Level world,
        SawRenderState state,
        float partialTicks
    ) {
        if (facing != Direction.UP) {
            return;
        }
        List<ItemStackRenderState> items = new ArrayList<>();
        BooleanList box = new BooleanArrayList();
        ItemStack stack = inventory.getItem(0);
        boolean hasInput = !stack.isEmpty();
        if (hasInput) {
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, stack, ItemDisplayContext.FIXED, world, null, 0);
            items.add(renderState);
            box.add(PackageItem.isPackage(stack));
        }
        for (int i = 1, size = inventory.getContainerSize(); i < size; i++) {
            stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, stack, ItemDisplayContext.FIXED, world, null, 0);
            items.add(renderState);
            box.add(PackageItem.isPackage(stack));
        }
        if (items.isEmpty()) {
            return;
        }
        state.items = items;
        state.box = box;
        state.outputs = hasInput ? items.size() - 1 : items.size();
        boolean alongZ = !state.blockState.getValue(AXIS_ALONG_FIRST_COORDINATE);
        float offset;
        if (speed == 0) {
            offset = 0.5f;
        } else {
            float duration = inventory.recipeDuration;
            if (duration != 0) {
                offset = inventory.remainingTime / duration;
                float processingSpeed = Mth.clamp(Math.abs(speed) / 32, 1, 128);
                offset = Mth.clamp(offset + (-partialTicks + 0.5f) * processingSpeed / duration, 0.125f, 1.0f);
                if (!inventory.appliedRecipe) {
                    offset += 1;
                }
                offset /= 2;
            } else {
                offset = 0;
            }
        }
        if (speed < 0 ^ alongZ) {
            offset = 1 - offset;
        }
        state.itemOffset = offset;
        state.alongZ = alongZ;
    }

    public void renderItems(SawRenderState state, PoseStack ms, SubmitNodeCollector queue) {
        int outputs = state.outputs;
        ms.pushPose();
        Pose pose = ms.last();
        if (state.alongZ) {
            pose.rotate(Axis.YP, Radians.DEG_90);
            pose.translate(outputs <= 1 ? 0.5f : 0.25f, 0, state.itemOffset);
            pose.translate(-1, 0, 0);
        } else {
            pose.translate(outputs <= 1 ? 0.5f : 0.25f, 0, state.itemOffset);
        }

        int renderedI = 0;
        List<ItemStackRenderState> items = state.items;
        BooleanList boxList = state.box;
        int light = state.lightCoords;
        int size = items.size();
        for (int i = 0; i < size; i++) {
            ItemStackRenderState renderState = items.get(i);
            ms.pushPose();
            pose = ms.last();
            pose.translate(0, renderState.usesBlockLight() ? 0.925f : 0.8125f, 0);
            if (i > 0 && outputs > 1) {
                pose.translate((float) (0.5 / (outputs - 1) * renderedI), 0, 0);
                SuperByteBuffer.nudge(pose, i * 133);
            }
            if (boxList.getBoolean(i)) {
                pose.translate(0, 0.25f, 0);
                pose.scale(1.5f, 1.5f, 1.5f);
            } else {
                pose.scale(0.5f, 0.5f, 0.5f);
                pose.rotate(Axis.XP, Radians.DEG_90);
            }
            renderState.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            renderedI++;

            ms.popPose();
        }
        ms.popPose();
    }

    public static class SawRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState blade;
        public boolean alongFirst;
        public @Nullable List<ItemStackRenderState> items;
        public @UnknownNullability BooleanList box;
        public int outputs;
        public boolean alongZ;
        public float itemOffset;
        public @Nullable FilterRenderState filter;
        public @Nullable SuperByteBufferRenderState shaft;
        public Direction.@UnknownNullability Axis axis;
        public float angle;
    }
}
