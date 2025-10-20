package com.zurrtum.create.client.content.kinetics.crafter;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Phase;
import com.zurrtum.create.content.kinetics.crafter.RecipeGridHandler.GroupedItems;
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
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class MechanicalCrafterRenderer implements BlockEntityRenderer<MechanicalCrafterBlockEntity, MechanicalCrafterRenderer.MechanicalCrafterRenderState> {
    protected final ItemModelManager itemModelManager;

    public MechanicalCrafterRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public MechanicalCrafterRenderState createRenderState() {
        return new MechanicalCrafterRenderState();
    }

    @Override
    public void updateRenderState(
        MechanicalCrafterBlockEntity be,
        MechanicalCrafterRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        World world = be.getWorld();
        Phase phase = be.phase;
        state.item = createItemState(itemModelManager, be, world, state.blockState, phase, tickProgress);
        Direction facing = state.blockState.get(HORIZONTAL_FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        if (state.item != null) {
            Vec3d vec = Vec3d.of(facing.getVector()).multiply(.58).add(.5, .5, .5);
            if (phase == Phase.EXPORTING) {
                Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
                float progress = MathHelper.clamp((1000 - be.countDown + be.getCountDownSpeed() * tickProgress) / 1000f, 0, 1);
                vec = vec.add(Vec3d.of(targetDirection.getVector()).multiply(progress * .75f));
            }
            state.offset = vec;
            state.yRot = MathHelper.RADIANS_PER_DEGREE * yRot;
        }
        state.layer = RenderLayer.getSolid();
        if (!VisualizationManager.supportsVisualization(world)) {
            state.cogwheel = CogwheelRenderState.create(be, state.blockState, state.pos, facing);
        }
        float xRot = state.blockState.get(MechanicalCrafterBlock.POINTING).getXRotation();
        state.upRot = (float) ((yRot + 90) / 180 * Math.PI);
        state.eastRot = (float) ((xRot) / 180 * Math.PI);
        if ((be.covered || phase != Phase.IDLE) && phase != Phase.CRAFTING && phase != Phase.INSERTING) {
            state.lid = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_LID, state.blockState);
        }
        Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
        if (MechanicalCrafterBlock.isValidTarget(world, state.pos.offset(targetDirection), state.blockState)) {
            state.belt = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_BELT, state.blockState);
            state.frame = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_BELT_FRAME, state.blockState);
            if (phase == Phase.EXPORTING) {
                int textureIndex = (int) ((be.getCountDownSpeed() / 128f * AnimationTickHolder.getTicks()));
                state.beltScroll = (textureIndex % 4) / 4f;
            }
        } else {
            state.arrow = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_ARROW, state.blockState);
        }
    }

    public static MechanicalCrafterItemRenderState createItemState(
        ItemModelManager itemModelManager,
        MechanicalCrafterBlockEntity be,
        World world,
        BlockState blockState,
        Phase phase,
        float tickProgress
    ) {
        if (phase == Phase.IDLE) {
            return MechanicalCrafterSingleItemRenderState.create(itemModelManager, be, world);
        }
        if (phase == Phase.CRAFTING) {
            return MechanicalCrafterCraftingItemRenderState.create(itemModelManager, be, world, tickProgress);
        }
        return MechanicalCrafterPhaseItemRenderState.create(itemModelManager, be, world, blockState, phase);
    }

    @Override
    public void render(MechanicalCrafterRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
        if (state.item != null) {
            matrices.translate(state.offset);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.yRot));
            state.item.render(queue, matrices, state.lightmapCoordinates);
        }
    }

    private SuperByteBuffer renderAndTransform(PartialModel renderBlock, BlockState crafterState) {
        SuperByteBuffer buffer = CachedBuffers.partial(renderBlock, crafterState);
        float xRot = crafterState.get(MechanicalCrafterBlock.POINTING).getXRotation();
        float yRot = AngleHelper.horizontalAngle(crafterState.get(HORIZONTAL_FACING));
        buffer.rotateCentered((float) ((yRot + 90) / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) ((xRot) / 180 * Math.PI), Direction.EAST);
        return buffer;
    }

    public static class MechanicalCrafterRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public Vec3d offset;
        public float yRot;
        public MechanicalCrafterItemRenderState item;
        public RenderLayer layer;
        public CogwheelRenderState cogwheel;
        public float upRot;
        public float eastRot;
        public SuperByteBuffer lid;
        public SuperByteBuffer belt;
        public SuperByteBuffer frame;
        public float beltScroll;
        public SuperByteBuffer arrow;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (cogwheel != null) {
                cogwheel.render(matricesEntry, vertexConsumer, lightmapCoordinates);
            }
            if (lid != null) {
                lid.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST).light(lightmapCoordinates)
                    .renderInto(matricesEntry, vertexConsumer);
            }
            if (belt != null) {
                belt.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST);
                if (beltScroll != 0) {
                    belt.shiftUVtoSheet(AllSpriteShifts.CRAFTER_THINGIES, beltScroll, 0, 1);
                }
                belt.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
                frame.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST).light(lightmapCoordinates)
                    .renderInto(matricesEntry, vertexConsumer);
            } else {
                arrow.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST).light(lightmapCoordinates)
                    .renderInto(matricesEntry, vertexConsumer);
            }
        }
    }

    public record CogwheelRenderState(
        SuperByteBuffer cogwheel, float angle, Direction direction, Color color, float upAngle
    ) {
        public static CogwheelRenderState create(MechanicalCrafterBlockEntity be, BlockState blockState, BlockPos pos, Direction facing) {
            SuperByteBuffer model = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, blockState);
            Axis axis = facing.getAxis();
            float angle = KineticBlockEntityRenderer.getAngleForBe(be, pos, axis);
            Direction direction = Direction.from(axis, Direction.AxisDirection.POSITIVE);
            float upAngle = axis != Axis.X ? 0 : MathHelper.HALF_PI;
            Color color = KineticBlockEntityRenderer.getColor(be);
            return new CogwheelRenderState(model, angle, direction, color, upAngle);
        }

        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer, int light) {
            cogwheel.rotateCentered(angle, direction).rotateCentered(upAngle, Direction.UP).rotateCentered(MathHelper.HALF_PI, Direction.EAST)
                .light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }

    public interface MechanicalCrafterItemRenderState {
        void render(OrderedRenderCommandQueue queue, MatrixStack ms, int light);
    }

    public record MechanicalCrafterSingleItemRenderState(
        float offset, float yRot, ItemRenderState state
    ) implements MechanicalCrafterItemRenderState {
        public static MechanicalCrafterSingleItemRenderState create(ItemModelManager itemModelManager, MechanicalCrafterBlockEntity be, World world) {
            ItemStack stack = be.getInventory().getStack();
            if (stack.isEmpty()) {
                return null;
            }
            float offset = -1 / 256f;
            float yRot = MathHelper.RADIANS_PER_DEGREE * 180;
            ItemRenderState state = new ItemRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(state, stack, state.displayContext, world, null, 0);
            return new MechanicalCrafterSingleItemRenderState(offset, yRot, state);
        }

        @Override
        public void render(OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            ms.push();
            ms.translate(0, 0, offset);
            ms.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            state.render(ms, queue, light, OverlayTexture.DEFAULT_UV, 0);
            ms.pop();
        }
    }

    public record MechanicalCrafterCraftingItemRenderState(
        float scale, Vec3d centering, List<GridItemRenderState> before, float yRot, float zRot, float upScaling, float downScaling,
        List<ItemRenderState> states
    ) implements MechanicalCrafterItemRenderState {
        public static MechanicalCrafterCraftingItemRenderState create(
            ItemModelManager itemModelManager,
            MechanicalCrafterBlockEntity be,
            World world,
            float tickProgress
        ) {
            GroupedItems items = be.groupedItemsBeforeCraft;
            boolean beforeEmpty = items.grid.isEmpty();
            boolean itemsEmpty = be.groupedItems.grid.isEmpty();
            if (beforeEmpty && itemsEmpty) {
                return null;
            }
            float yRot = MathHelper.RADIANS_PER_DEGREE * 180;
            float value = be.countDown - be.getCountDownSpeed() * tickProgress;
            float scale;
            Vec3d centering;
            List<GridItemRenderState> before;
            if (beforeEmpty) {
                scale = 0;
                centering = null;
                before = null;
            } else {
                items.calcStats();
                float progress = MathHelper.clamp((2000 - value) / 1000f, 0, 1);
                float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
                scale = 1 - MathHelper.clamp(progress * 2 - 1, 0, 1);
                centering = new Vec3d(-items.minX + (-items.width + 1) / 2f, -items.minY + (-items.height + 1) / 2f, 0).multiply(earlyProgress)
                    .multiply(0.5, 0.5, 1);
                float distance = .5f + (-4 * (progress - .5f) * (progress - .5f) + 1) * .25f;
                boolean onlyRenderFirst = be.countDown < 1000;
                before = new ArrayList<>(items.grid.size());
                items.grid.forEach((pair, stack) -> {
                    if (onlyRenderFirst && (pair.getFirst() != 0 || pair.getSecond() != 0)) {
                        return;
                    }
                    int x = pair.getFirst();
                    int y = pair.getSecond();
                    float offsetX = x * distance;
                    float offsetY = y * distance;
                    float offsetZ = (x + y * 3) / 1024f;
                    ItemRenderState state = new ItemRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.update(state, stack, state.displayContext, world, null, 0);
                    before.add(new GridItemRenderState(state, offsetX, offsetY, offsetZ));
                });
            }
            float zRot, upScaling, downScaling;
            List<ItemRenderState> states;
            if (itemsEmpty) {
                zRot = upScaling = downScaling = 0;
                states = null;
            } else {
                float progress = MathHelper.clamp((1000 - value) / 1000f, 0, 1);
                float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
                zRot = MathHelper.RADIANS_PER_DEGREE * (earlyProgress * 2 * 360);
                upScaling = earlyProgress * 1.125f;
                downScaling = 1 + (1 - MathHelper.clamp(progress * 2 - 1, 0, 1)) * .125f;
                items = be.groupedItems;
                states = new ArrayList<>(items.grid.size());
                items.grid.forEach((pair, stack) -> {
                    if (pair.getFirst() != 0 || pair.getSecond() != 0) {
                        return;
                    }
                    ItemRenderState state = new ItemRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.update(state, stack, state.displayContext, world, null, 0);
                    states.add(state);
                });
            }
            return new MechanicalCrafterCraftingItemRenderState(scale, centering, before, yRot, zRot, upScaling, downScaling, states);
        }

        @Override
        public void render(OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            if (before != null) {
                ms.push();
                ms.scale(scale, scale, scale);
                ms.translate(centering);
                for (GridItemRenderState state : before) {
                    state.render(queue, ms, yRot, light);
                }
                ms.pop();
            }
            if (states != null) {
                ms.multiply(RotationAxis.POSITIVE_Z.rotation(zRot));
                ms.scale(upScaling, upScaling, upScaling);
                ms.scale(downScaling, downScaling, downScaling);
                for (ItemRenderState state : states) {
                    ms.push();
                    ms.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
                    state.render(ms, queue, light, OverlayTexture.DEFAULT_UV, 0);
                    ms.pop();
                }
            }
        }
    }

    public record MechanicalCrafterPhaseItemRenderState(List<GridItemRenderState> states, float yRot) implements MechanicalCrafterItemRenderState {
        public static MechanicalCrafterPhaseItemRenderState create(
            ItemModelManager itemModelManager,
            MechanicalCrafterBlockEntity be,
            World world,
            BlockState blockState,
            Phase phase
        ) {
            Map<Pair<Integer, Integer>, ItemStack> grid = be.groupedItems.grid;
            if (grid.isEmpty()) {
                return null;
            }
            float distance = .5f;
            boolean onlyRenderFirst = phase == Phase.INSERTING;
            boolean isExporting = phase == Phase.EXPORTING && blockState.contains(MechanicalCrafterBlock.POINTING);
            Pointing pointing = isExporting ? blockState.get(MechanicalCrafterBlock.POINTING) : null;
            float yRot = MathHelper.RADIANS_PER_DEGREE * 180;
            List<GridItemRenderState> states = new ArrayList<>(grid.size());
            grid.forEach((pair, stack) -> {
                if (onlyRenderFirst && (pair.getFirst() != 0 || pair.getSecond() != 0)) {
                    return;
                }
                int x = pair.getFirst();
                int y = pair.getSecond();
                float offsetX = x * distance;
                float offsetY = y * distance;
                int value = x + y * 3;
                if (pointing != null) {
                    switch (pointing) {
                        case UP -> value -= 9;
                        case LEFT -> value += 18;
                        case RIGHT -> value -= 18;
                        case DOWN -> value += 9;
                    }
                }
                float offsetZ = value / 1024f;
                ItemRenderState state = new ItemRenderState();
                state.displayContext = ItemDisplayContext.FIXED;
                itemModelManager.update(state, stack, state.displayContext, world, null, 0);
                states.add(new GridItemRenderState(state, offsetX, offsetY, offsetZ));
            });
            return new MechanicalCrafterPhaseItemRenderState(states, yRot);
        }

        @Override
        public void render(OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            for (GridItemRenderState state : states) {
                state.render(queue, ms, yRot, light);
            }
        }
    }

    public record GridItemRenderState(ItemRenderState state, float offsetX, float offsetY, float offsetZ) {
        public void render(OrderedRenderCommandQueue queue, MatrixStack ms, float yRot, int light) {
            ms.push();
            ms.translate(offsetX, offsetY, 0);
            ms.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            ms.translate(0, 0, offsetZ);
            state.render(ms, queue, light, OverlayTexture.DEFAULT_UV, 0);
            ms.pop();
        }
    }
}
