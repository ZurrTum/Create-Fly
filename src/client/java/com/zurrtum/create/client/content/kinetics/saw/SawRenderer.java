package com.zurrtum.create.client.content.kinetics.saw;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.state.property.Properties.FACING;

public class SawRenderer implements BlockEntityRenderer<SawBlockEntity, SawRenderer.SawRenderState> {
    protected final ItemModelManager itemModelManager;

    public SawRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public SawRenderState createRenderState() {
        return new SawRenderState();
    }

    @Override
    public void updateRenderState(
        SawBlockEntity be,
        SawRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getCutoutMipped();
        state.partialTicks = tickProgress;
        state.speed = be.getSpeed();
        updateBlade(state);
        World world = be.getWorld();
        updateItems(be.inventory, world, state);
        if (!be.isRemoved()) {
            state.filter = FilteringRenderer.getFilterRenderState(
                be,
                state.blockState,
                itemModelManager,
                be.isVirtual() ? -1 : cameraPos.squaredDistanceTo(VecHelper.getCenterOf(state.pos))
            );
        }
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.shaft = getRotatedModel(state.blockState, axis);
        state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.pos, axis);
        state.direction = Direction.from(axis, Direction.AxisDirection.POSITIVE);
        state.color = KineticBlockEntityRenderer.getColor(be);
    }

    @Override
    public void render(SawRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
        if (state.items != null) {
            renderItems(state, matrices, queue);
        }
        if (state.filter != null) {
            state.filter.render(state.blockState, queue, matrices, state.lightmapCoordinates);
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

            if (blockState.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE))
                rotate = true;
        }

        state.blade = CachedBuffers.partialFacing(partial, blockState);
        if (rotate) {
            state.bladeAngle = AngleHelper.rad(90);
        } else {
            state.bladeAngle = -1;
        }
    }

    public void updateItems(ProcessingInventory inventory, World world, SawRenderState state) {
        if (state.blockState.get(SawBlock.FACING) != Direction.UP) {
            return;
        }
        List<ItemRenderState> items = new ArrayList<>();
        BooleanList box = new BooleanArrayList();
        ItemStack stack = inventory.getStack(0);
        boolean hasInput = !stack.isEmpty();
        if (hasInput) {
            ItemRenderState renderState = new ItemRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(renderState, stack, ItemDisplayContext.FIXED, world, null, 0);
            items.add(renderState);
            box.add(PackageItem.isPackage(stack));
        }
        for (int i = 1, size = inventory.size(); i < size; i++) {
            stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemRenderState renderState = new ItemRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(renderState, stack, ItemDisplayContext.FIXED, world, null, 0);
            items.add(renderState);
            box.add(PackageItem.isPackage(stack));
        }
        if (items.isEmpty()) {
            return;
        }
        state.items = items;
        state.box = box;
        state.outputs = hasInput ? items.size() - 1 : items.size();
        state.alongZ = !state.blockState.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
        state.duration = inventory.recipeDuration;
        state.remainingTime = inventory.remainingTime;
        state.appliedRecipe = inventory.appliedRecipe;
    }

    public void renderItems(SawRenderState state, MatrixStack ms, OrderedRenderCommandQueue queue) {
        boolean alongZ = state.alongZ;
        float duration = state.duration;
        float speed = state.speed;
        boolean moving = duration != 0;
        float offset = moving ? state.remainingTime / duration : 0;
        if (moving) {
            float processingSpeed = MathHelper.clamp(Math.abs(speed) / 32, 1, 128);
            offset = MathHelper.clamp(offset + ((-state.partialTicks + .5f) * processingSpeed) / duration, 0.125f, 1f);
            if (!state.appliedRecipe)
                offset += 1;
            offset /= 2;
        }

        if (speed == 0)
            offset = .5f;
        if (speed < 0 ^ alongZ)
            offset = 1 - offset;

        int outputs = state.outputs;

        ms.push();
        if (alongZ)
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        ms.translate(outputs <= 1 ? .5 : .25, 0, offset);
        ms.translate(alongZ ? -1 : 0, 0, 0);

        int renderedI = 0;
        List<ItemRenderState> items = state.items;
        BooleanList boxList = state.box;
        int light = state.lightmapCoordinates;
        int size = items.size();
        PoseTransformStack msr = size > 1 && outputs > 1 ? TransformStack.of(ms) : null;
        for (int i = 0; i < size; i++) {
            ItemRenderState renderState = items.get(i);

            ms.push();
            ms.translate(0, renderState.isSideLit() ? .925f : 13f / 16f, 0);

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
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            }

            renderState.render(ms, queue, light, OverlayTexture.DEFAULT_UV, 0);
            renderedI++;

            ms.pop();
        }
        ms.pop();
    }

    protected SuperByteBuffer getRotatedModel(BlockState state, Axis axis) {
        if (state.get(FACING).getAxis().isHorizontal())
            return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.getBlock().rotate(state, BlockRotation.CLOCKWISE_180));
        return CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, KineticBlockEntityRenderer.shaft(axis));
    }

    public static class SawRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float speed;
        public float partialTicks;
        public SuperByteBuffer blade;
        public float bladeAngle;
        public List<ItemRenderState> items;
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
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (bladeAngle != -1) {
                blade.rotateCentered(bladeAngle, Direction.UP);
            }
            blade.color(0xFFFFFF).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            if (shaft != null) {
                shaft.light(lightmapCoordinates).rotateCentered(angle, direction).color(color).renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
