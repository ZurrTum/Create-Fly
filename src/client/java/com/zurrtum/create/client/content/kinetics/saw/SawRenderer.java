package com.zurrtum.create.client.content.kinetics.saw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class SawRenderer implements BlockEntityRenderer<SawBlockEntity, SawRenderer.SawRenderState> {
    protected final ItemModelResolver itemModelManager;

    public SawRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.cutoutMipped();
        state.partialTicks = tickProgress;
        state.speed = be.getSpeed();
        updateBlade(state);
        Level world = be.getLevel();
        updateItems(be.inventory, world, state);
        if (!be.isRemoved()) {
            state.filter = FilteringRenderer.getFilterRenderState(
                be,
                state.blockState,
                itemModelManager,
                be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
            );
        }
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.shaft = getRotatedModel(state.blockState, axis);
        state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.blockPos, axis);
        state.direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        state.color = KineticBlockEntityRenderer.getColor(be);
    }

    @Override
    public void submit(SawRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
        if (state.items != null) {
            renderItems(state, matrices, queue);
        }
        if (state.filter != null) {
            state.filter.render(state.blockState, queue, matrices, state.lightCoords);
        }
    }

    public void updateBlade(SawRenderState state) {
        BlockState blockState = state.blockState;
        PartialModel partial;
        float speed = state.speed;
        boolean rotate = false;

        if (SawBlock.isHorizontal(blockState)) {
            if (speed > 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE;
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_REVERSED;
            } else {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE;
            }
        } else {
            if (speed > 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE;
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_REVERSED;
            } else {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE;
            }

            if (blockState.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE))
                rotate = true;
        }

        state.blade = CachedBuffers.partialFacing(partial, blockState);
        if (rotate) {
            state.bladeAngle = AngleHelper.rad(90);
        } else {
            state.bladeAngle = -1;
        }
    }

    public void updateItems(ProcessingInventory inventory, Level world, SawRenderState state) {
        if (state.blockState.getValue(SawBlock.FACING) != Direction.UP) {
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
        state.alongZ = !state.blockState.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
        state.duration = inventory.recipeDuration;
        state.remainingTime = inventory.remainingTime;
        state.appliedRecipe = inventory.appliedRecipe;
    }

    public void renderItems(SawRenderState state, PoseStack ms, SubmitNodeCollector queue) {
        boolean alongZ = state.alongZ;
        float duration = state.duration;
        float speed = state.speed;
        boolean moving = duration != 0;
        float offset = moving ? state.remainingTime / duration : 0;
        if (moving) {
            float processingSpeed = Mth.clamp(Math.abs(speed) / 32, 1, 128);
            offset = Mth.clamp(offset + ((-state.partialTicks + .5f) * processingSpeed) / duration, 0.125f, 1f);
            if (!state.appliedRecipe)
                offset += 1;
            offset /= 2;
        }

        if (speed == 0)
            offset = .5f;
        if (speed < 0 ^ alongZ)
            offset = 1 - offset;

        int outputs = state.outputs;

        ms.pushPose();
        if (alongZ)
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        ms.translate(outputs <= 1 ? .5 : .25, 0, offset);
        ms.translate(alongZ ? -1 : 0, 0, 0);

        int renderedI = 0;
        List<ItemStackRenderState> items = state.items;
        BooleanList boxList = state.box;
        int light = state.lightCoords;
        int size = items.size();
        PoseTransformStack msr = size > 1 && outputs > 1 ? TransformStack.of(ms) : null;
        for (int i = 0; i < size; i++) {
            ItemStackRenderState renderState = items.get(i);

            ms.pushPose();
            ms.translate(0, renderState.usesBlockLight() ? .925f : 13f / 16f, 0);

            if (i > 0 && outputs > 1) {
                ms.translate((0.5 / (outputs - 1)) * renderedI, 0, 0);
                msr.nudge(i * 133);
            }

            boolean box = boxList.getBoolean(i);
            if (box) {
                ms.translate(0, 4 / 16f, 0);
                ms.scale(1.5f, 1.5f, 1.5f);
            } else {
                ms.scale(.5f, .5f, .5f);
            }

            if (!box) {
                ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            }

            renderState.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            renderedI++;

            ms.popPose();
        }
        ms.popPose();
    }

    protected SuperByteBuffer getRotatedModel(BlockState state, Axis axis) {
        if (state.getValue(FACING).getAxis().isHorizontal())
            return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.getBlock().rotate(state, Rotation.CLOCKWISE_180));
        return CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, KineticBlockEntityRenderer.shaft(axis));
    }

    public static class SawRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float speed;
        public float partialTicks;
        public SuperByteBuffer blade;
        public float bladeAngle;
        public List<ItemStackRenderState> items;
        public BooleanList box;
        public int outputs;
        public boolean alongZ;
        public float duration;
        public float remainingTime;
        public boolean appliedRecipe;
        public FilterRenderState filter;
        public SuperByteBuffer shaft;
        public float angle;
        public Direction direction;
        public Color color;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (bladeAngle != -1) {
                blade.rotateCentered(bladeAngle, Direction.UP);
            }
            blade.color(0xFFFFFF).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            if (shaft != null) {
                shaft.light(lightCoords).rotateCentered(angle, direction).color(color).renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
