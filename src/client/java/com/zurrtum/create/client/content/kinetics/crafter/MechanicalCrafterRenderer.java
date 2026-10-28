package com.zurrtum.create.client.content.kinetics.crafter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.crafter.MechanicalCrafterRenderer.MechanicalCrafterRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Phase;
import com.zurrtum.create.content.kinetics.crafter.RecipeGridHandler.GroupedItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getDegreesWithoutBeOffset;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;
import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class MechanicalCrafterRenderer implements BlockEntityRenderer<MechanicalCrafterBlockEntity, MechanicalCrafterRenderState> {
    protected static final int CRAFTING_PHASE_ORDINAL = Phase.CRAFTING.ordinal();
    protected final ItemModelResolver itemModelManager;

    public MechanicalCrafterRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public MechanicalCrafterRenderState createRenderState() {
        return new MechanicalCrafterRenderState();
    }

    @Override
    public void extractRenderState(
        MechanicalCrafterBlockEntity be,
        MechanicalCrafterRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        state.item = createItemState(itemModelManager, be, level, be.getBlockState(), be.phase, tickProgress);
        Direction facing;
        if (VisualizationManager.supportsVisualization(level)) {
            if (state.item == null) {
                return;
            }
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            facing = state.blockState.getValue(HORIZONTAL_FACING);
        } else {
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            facing = state.blockState.getValue(HORIZONTAL_FACING);
            state.model = MechanicalCrafterBlockRenderState.create(be, level, state, facing);
            if (state.item == null) {
                return;
            }
        }
        Vec3 vec = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(0.58).add(0.5, 0.5, 0.5);
        if (be.phase == Phase.EXPORTING) {
            Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
            float progress = Mth.clamp((1000 - be.countDown + be.getCountDownSpeed() * tickProgress) / 1000.0f, 0, 1);
            vec = vec.add(Vec3.atLowerCornerOf(targetDirection.getUnitVec3i()).scale(progress * 0.75f));
        }
        state.offset = vec;
        state.yRadians = Radians.horizontal(facing);
    }

    @Nullable
    public static MechanicalCrafterItemRenderState createItemState(
        ItemModelResolver itemModelManager,
        MechanicalCrafterBlockEntity be,
        @Nullable Level world,
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
    public void submit(
        MechanicalCrafterRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.item != null) {
            matrices.pushPose();
            Pose pose = matrices.last();
            Vec3 offset = state.offset;
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            pose.scale(0.5f, 0.5f, 0.5f);
            if (state.yRadians != 0) {
                pose.rotate(Axis.YP, state.yRadians);
            }
            state.item.submit(queue, matrices, state.lightCoords);
            matrices.popPose();
        }
        if (state.model != null) {
            state.model.submit(matrices, queue);
        }
    }

    public static class MechanicalCrafterRenderState extends BlockEntityRenderState {
        public @UnknownNullability Vec3 offset;
        public float yRadians;
        public @Nullable MechanicalCrafterItemRenderState item;
        public @Nullable MechanicalCrafterBlockRenderState model;
    }

    public record MechanicalCrafterBlockRenderState(SuperByteBufferRenderState cogwheel, Direction.Axis axis,
                                                    float angle, float beltYRadians, float beltXAngle,
                                                    @Nullable SuperByteBufferRenderState lid,
                                                    @Nullable SuperByteBufferRenderState belt,
                                                    @UnknownNullability SuperByteBufferRenderState frame,
                                                    @UnknownNullability SuperByteBufferRenderState arrow) {
        public static MechanicalCrafterBlockRenderState create(
            MechanicalCrafterBlockEntity be,
            @Nullable Level level,
            MechanicalCrafterRenderState state,
            Direction facing
        ) {
            CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
            SuperByteBufferRenderState cogwheel = CachedBuffers.partial(
                AllPartialModels.SHAFTLESS_COGWHEEL,
                state.blockState
            ).cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            Direction.Axis axis = facing.getAxis();
            float angle = getDegreesWithoutBeOffset(axis, be, state, level);
            float beltYRadians = Radians.horizontalPlus90(facing);
            float beltXAngle = state.blockState.getValue(MechanicalCrafterBlock.POINTING).getXRotation();
            Phase phase = be.phase;
            SuperByteBufferRenderState lid = null, belt = null, frame = null, arrow = null;
            if ((be.covered || phase != Phase.IDLE) && phase.ordinal() < CRAFTING_PHASE_ORDINAL) {
                lid = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_LID, state.blockState)
                    .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            }
            Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
            if (MechanicalCrafterBlock.isValidTarget(
                level,
                state.blockPos.relative(targetDirection),
                state.blockState
            )) {
                SuperByteBuffer beltBuffer = CachedBuffers.partial(
                    AllPartialModels.MECHANICAL_CRAFTER_BELT,
                    state.blockState
                );
                if (phase == Phase.EXPORTING) {
                    int textureIndex = (int) (be.getCountDownSpeed() / 128.0f * AnimationTickHolder.getTicks());
                    beltBuffer.shiftUVtoSheet(AllSpriteShifts.CRAFTER_THINGIES, (textureIndex % 4) / 4.0f, 0, 1);
                }
                belt = beltBuffer.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
                frame = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_BELT_FRAME, state.blockState)
                    .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            } else {
                arrow = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_ARROW, state.blockState)
                    .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            }
            return new MechanicalCrafterBlockRenderState(
                cogwheel,
                axis,
                angle,
                beltYRadians,
                beltXAngle,
                lid,
                belt,
                frame,
                arrow
            );
        }

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            if (angle != 0) {
                pose.rotateCentered(axis, Mth.DEG_TO_RAD * angle);
            }
            if (axis == Direction.Axis.X) {
                pose.rotateYCentered(Radians.DEG_90);
            }
            pose.rotateXCentered(Radians.DEG_90);
            cogwheel.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            if (beltYRadians != 0) {
                pose.rotateYCentered(beltYRadians);
            }
            if (beltXAngle != 0) {
                pose.rotateXCentered(Mth.DEG_TO_RAD * beltXAngle);
            }
            if (lid != null) {
                lid.submit(matrices, queue);
            }
            if (belt != null) {
                belt.submit(matrices, queue);
                frame.submit(matrices, queue);
            } else {
                arrow.submit(matrices, queue);
            }
            matrices.popPose();
        }
    }

    public interface MechanicalCrafterItemRenderState {
        void submit(SubmitNodeCollector queue, PoseStack ms, int light);
    }

    public record MechanicalCrafterSingleItemRenderState(float offset,
                                                         ItemStackRenderState state) implements MechanicalCrafterItemRenderState {
        @Nullable
        public static MechanicalCrafterSingleItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            @Nullable Level world
        ) {
            ItemStack stack = be.getInventory().getStack();
            if (stack.isEmpty()) {
                return null;
            }
            float offset = -1 / 256.0f;
            ItemStackRenderState state = new ItemStackRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
            return new MechanicalCrafterSingleItemRenderState(offset, state);
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack ms, int light) {
            ms.pushPose();
            Pose pose = ms.last();
            pose.translate(0, 0, offset);
            pose.rotate(Axis.YP, Radians.DEG_180);
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
    }

    public record MechanicalCrafterCraftingItemRenderState(float scale, @Nullable Vec3 centering,
                                                           @Nullable List<GridItemRenderState> before, float zRot,
                                                           float upScaling, float downScaling,
                                                           @Nullable List<ItemStackRenderState> states) implements MechanicalCrafterItemRenderState {
        @Nullable
        public static MechanicalCrafterCraftingItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            @Nullable Level world,
            float tickProgress
        ) {
            GroupedItems items = be.groupedItemsBeforeCraft;
            boolean beforeEmpty = items.grid.isEmpty();
            boolean itemsEmpty = be.groupedItems.grid.isEmpty();
            if (beforeEmpty && itemsEmpty) {
                return null;
            }
            float value = be.countDown - be.getCountDownSpeed() * tickProgress;
            float scale;
            Vec3 centering;
            List<GridItemRenderState> before;
            if (beforeEmpty) {
                scale = 0;
                centering = null;
                before = null;
            } else {
                items.calcStats();
                float progress = Mth.clamp((2000 - value) / 1000.0f, 0, 1);
                float earlyProgress = Mth.clamp(progress * 2, 0, 1);
                scale = 1 - Mth.clamp(progress * 2 - 1, 0, 1);
                centering = new Vec3(
                    -items.minX + (-items.width + 1) / 2.0f,
                    -items.minY + (-items.height + 1) / 2.0f,
                    0
                ).scale(earlyProgress).multiply(0.5, 0.5, 1);
                float distance = 0.5f + (-4 * (progress - 0.5f) * (progress - 0.5f) + 1) * 0.25f;
                boolean onlyRenderFirst = be.countDown < 1000;
                before = GridItemRenderState.create(items.grid, itemModelManager, world, distance, 0, onlyRenderFirst);
            }
            float zRot;
            float upScaling;
            float downScaling;
            List<ItemStackRenderState> states;
            if (itemsEmpty) {
                zRot = upScaling = downScaling = 0;
                states = null;
            } else {
                float progress = Mth.clamp((1000 - value) / 1000.0f, 0, 1);
                float earlyProgress = Mth.clamp(progress * 2, 0, 1);
                zRot = earlyProgress * 720;
                upScaling = earlyProgress * 1.125f;
                downScaling = 1 + (1 - Mth.clamp(progress * 2 - 1, 0, 1)) * 0.125f;
                items = be.groupedItems;
                states = new ArrayList<>(items.grid.size());
                items.grid.forEach((pair, stack) -> {
                    if (pair.getFirst() != 0 || pair.getSecond() != 0) {
                        return;
                    }
                    ItemStackRenderState state = new ItemStackRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
                    states.add(state);
                });
            }
            return new MechanicalCrafterCraftingItemRenderState(
                scale,
                centering,
                before,
                zRot,
                upScaling,
                downScaling,
                states
            );
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack ms, int light) {
            if (before != null) {
                ms.pushPose();
                Pose pose = ms.last();
                pose.scale(scale, scale, scale);
                pose.translate((float) centering.x, (float) centering.y, (float) centering.z);
                for (GridItemRenderState state : before) {
                    state.submit(queue, ms, light);
                }
                ms.popPose();
            }
            if (states != null) {
                Pose pose = ms.last();
                if (zRot != 0) {
                    pose.rotate(Axis.ZP, Mth.DEG_TO_RAD * zRot);
                }
                pose.scale(upScaling, upScaling, upScaling);
                pose.scale(downScaling, downScaling, downScaling);
                pose.rotate(Axis.YP, Radians.DEG_180);
                for (ItemStackRenderState state : states) {
                    state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
                }
            }
        }
    }

    public record MechanicalCrafterPhaseItemRenderState(
        List<GridItemRenderState> states) implements MechanicalCrafterItemRenderState {
        @Nullable
        public static MechanicalCrafterPhaseItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            @Nullable Level world,
            BlockState blockState,
            Phase phase
        ) {
            Map<Pair<Integer, Integer>, ItemStack> grid = be.groupedItems.grid;
            if (grid.isEmpty()) {
                return null;
            }
            float offset;
            if (phase == Phase.EXPORTING && blockState.hasProperty(MechanicalCrafterBlock.POINTING)) {
                offset = switch (blockState.getValue(MechanicalCrafterBlock.POINTING)) {
                    case UP -> -9 / 1024.0f;
                    case LEFT -> 18 / 1024.0f;
                    case RIGHT -> -18 / 1024.0f;
                    case DOWN -> 9 / 1024.0f;
                };
            } else {
                offset = 0;
            }
            List<GridItemRenderState> states = GridItemRenderState.create(
                grid,
                itemModelManager,
                world,
                0.5f,
                offset,
                phase == Phase.INSERTING
            );
            return new MechanicalCrafterPhaseItemRenderState(states);
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack ms, int light) {
            for (GridItemRenderState state : states) {
                state.submit(queue, ms, light);
            }
        }
    }

    public record GridItemRenderState(ItemStackRenderState state, float offsetX, float offsetY, float offsetZ) {
        public static List<GridItemRenderState> create(
            Map<Pair<Integer, Integer>, ItemStack> grid,
            ItemModelResolver itemModelManager,
            @Nullable Level level,
            float distance,
            float offset,
            boolean onlyRenderFirst
        ) {
            List<GridItemRenderState> states;
            if (onlyRenderFirst) {
                states = new ArrayList<>(1);
                for (Map.Entry<Pair<Integer, Integer>, ItemStack> entry : grid.entrySet()) {
                    Pair<Integer, Integer> pair = entry.getKey();
                    if (pair.getFirst() != 0 || pair.getSecond() != 0) {
                        continue;
                    }
                    ItemStackRenderState state = new ItemStackRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.appendItemLayers(state, entry.getValue(), state.displayContext, level, null, 0);
                    states.add(new GridItemRenderState(state, 0, 0, offset));
                    break;
                }
            } else {
                states = new ArrayList<>(grid.size());
                grid.forEach((pair, stack) -> {
                    int x = pair.getFirst();
                    int y = pair.getSecond();
                    float offsetX = x * distance;
                    float offsetY = y * distance;
                    float offsetZ = (x + y * 3) / 1024.0f;
                    ItemStackRenderState state = new ItemStackRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.appendItemLayers(state, stack, state.displayContext, level, null, 0);
                    states.add(new GridItemRenderState(state, offsetX, offsetY, offsetZ + offset));
                });
            }
            return states;
        }

        public void submit(SubmitNodeCollector queue, PoseStack ms, int light) {
            ms.pushPose();
            Pose pose = ms.last();
            pose.translate(offsetX, offsetY, 0);
            pose.rotate(Axis.YP, Radians.DEG_180);
            pose.translate(0, 0, offsetZ);
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
    }
}
