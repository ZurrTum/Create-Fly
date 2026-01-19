package com.zurrtum.create.client.content.kinetics.crafter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class MechanicalCrafterRenderer implements BlockEntityRenderer<MechanicalCrafterBlockEntity, MechanicalCrafterRenderer.MechanicalCrafterRenderState> {
    protected final ItemModelResolver itemModelManager;

    public MechanicalCrafterRenderer(BlockEntityRendererProvider.Context context) {
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        Level world = be.getLevel();
        Phase phase = be.phase;
        state.item = createItemState(itemModelManager, be, world, state.blockState, phase, tickProgress);
        Direction facing = state.blockState.getValue(HORIZONTAL_FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        if (state.item != null) {
            Vec3 vec = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(.58).add(.5, .5, .5);
            if (phase == Phase.EXPORTING) {
                Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
                float progress = Mth.clamp((1000 - be.countDown + be.getCountDownSpeed() * tickProgress) / 1000f, 0, 1);
                vec = vec.add(Vec3.atLowerCornerOf(targetDirection.getUnitVec3i()).scale(progress * .75f));
            }
            state.offset = vec;
            state.yRot = Mth.DEG_TO_RAD * yRot;
        }
        state.layer = RenderTypes.solidMovingBlock();
        if (!VisualizationManager.supportsVisualization(world)) {
            state.cogwheel = CogwheelRenderState.create(be, state.blockState, state.blockPos, facing);
        }
        float xRot = state.blockState.getValue(MechanicalCrafterBlock.POINTING).getXRotation();
        state.upRot = (float) ((yRot + 90) / 180 * Math.PI);
        state.eastRot = (float) ((xRot) / 180 * Math.PI);
        if ((be.covered || phase != Phase.IDLE) && phase != Phase.CRAFTING && phase != Phase.INSERTING) {
            state.lid = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_LID, state.blockState);
        }
        Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
        if (MechanicalCrafterBlock.isValidTarget(world, state.blockPos.relative(targetDirection), state.blockState)) {
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
        ItemModelResolver itemModelManager,
        MechanicalCrafterBlockEntity be,
        Level world,
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
    public void submit(MechanicalCrafterRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
        if (state.item != null) {
            matrices.translate(state.offset);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(state.yRot));
            state.item.render(queue, matrices, state.lightCoords);
        }
    }

    private SuperByteBuffer renderAndTransform(PartialModel renderBlock, BlockState crafterState) {
        SuperByteBuffer buffer = CachedBuffers.partial(renderBlock, crafterState);
        float xRot = crafterState.getValue(MechanicalCrafterBlock.POINTING).getXRotation();
        float yRot = AngleHelper.horizontalAngle(crafterState.getValue(HORIZONTAL_FACING));
        buffer.rotateCentered((float) ((yRot + 90) / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) ((xRot) / 180 * Math.PI), Direction.EAST);
        return buffer;
    }

    public static class MechanicalCrafterRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public Vec3 offset;
        public float yRot;
        public MechanicalCrafterItemRenderState item;
        public RenderType layer;
        public CogwheelRenderState cogwheel;
        public float upRot;
        public float eastRot;
        public SuperByteBuffer lid;
        public SuperByteBuffer belt;
        public SuperByteBuffer frame;
        public float beltScroll;
        public SuperByteBuffer arrow;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (cogwheel != null) {
                cogwheel.render(matricesEntry, vertexConsumer, lightCoords);
            }
            if (lid != null) {
                lid.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST).light(lightCoords)
                    .renderInto(matricesEntry, vertexConsumer);
            }
            if (belt != null) {
                belt.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST);
                if (beltScroll != 0) {
                    belt.shiftUVtoSheet(AllSpriteShifts.CRAFTER_THINGIES, beltScroll, 0, 1);
                }
                belt.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
                frame.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST).light(lightCoords)
                    .renderInto(matricesEntry, vertexConsumer);
            } else {
                arrow.rotateCentered(upRot, Direction.UP).rotateCentered(eastRot, Direction.EAST).light(lightCoords)
                    .renderInto(matricesEntry, vertexConsumer);
            }
        }
    }

    public record CogwheelRenderState(
        SuperByteBuffer cogwheel, float angle, Direction direction, Color color, float upAngle
    ) {
        public static CogwheelRenderState create(MechanicalCrafterBlockEntity be, BlockState blockState, BlockPos pos, Direction facing) {
            SuperByteBuffer model = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, blockState);
            net.minecraft.core.Direction.Axis axis = facing.getAxis();
            float angle = KineticBlockEntityRenderer.getAngleForBe(be, pos, axis);
            Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            float upAngle = axis != net.minecraft.core.Direction.Axis.X ? 0 : Mth.HALF_PI;
            Color color = KineticBlockEntityRenderer.getColor(be);
            return new CogwheelRenderState(model, angle, direction, color, upAngle);
        }

        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer, int light) {
            cogwheel.rotateCentered(angle, direction).rotateCentered(upAngle, Direction.UP).rotateCentered(Mth.HALF_PI, Direction.EAST).light(light)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }

    public interface MechanicalCrafterItemRenderState {
        void render(SubmitNodeCollector queue, PoseStack ms, int light);
    }

    public record MechanicalCrafterSingleItemRenderState(
        float offset, float yRot, ItemStackRenderState state
    ) implements MechanicalCrafterItemRenderState {
        public static MechanicalCrafterSingleItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            Level world
        ) {
            ItemStack stack = be.getInventory().getStack();
            if (stack.isEmpty()) {
                return null;
            }
            float offset = -1 / 256f;
            float yRot = Mth.DEG_TO_RAD * 180;
            ItemStackRenderState state = new ItemStackRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
            return new MechanicalCrafterSingleItemRenderState(offset, yRot, state);
        }

        @Override
        public void render(SubmitNodeCollector queue, PoseStack ms, int light) {
            ms.pushPose();
            ms.translate(0, 0, offset);
            ms.mulPose(Axis.YP.rotation(yRot));
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
    }

    public record MechanicalCrafterCraftingItemRenderState(
        float scale, Vec3 centering, List<GridItemRenderState> before, float yRot, float zRot, float upScaling, float downScaling,
        List<ItemStackRenderState> states
    ) implements MechanicalCrafterItemRenderState {
        public static MechanicalCrafterCraftingItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            Level world,
            float tickProgress
        ) {
            GroupedItems items = be.groupedItemsBeforeCraft;
            boolean beforeEmpty = items.grid.isEmpty();
            boolean itemsEmpty = be.groupedItems.grid.isEmpty();
            if (beforeEmpty && itemsEmpty) {
                return null;
            }
            float yRot = Mth.DEG_TO_RAD * 180;
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
                float progress = Mth.clamp((2000 - value) / 1000f, 0, 1);
                float earlyProgress = Mth.clamp(progress * 2, 0, 1);
                scale = 1 - Mth.clamp(progress * 2 - 1, 0, 1);
                centering = new Vec3(-items.minX + (-items.width + 1) / 2f, -items.minY + (-items.height + 1) / 2f, 0).scale(earlyProgress)
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
                    ItemStackRenderState state = new ItemStackRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
                    before.add(new GridItemRenderState(state, offsetX, offsetY, offsetZ));
                });
            }
            float zRot, upScaling, downScaling;
            List<ItemStackRenderState> states;
            if (itemsEmpty) {
                zRot = upScaling = downScaling = 0;
                states = null;
            } else {
                float progress = Mth.clamp((1000 - value) / 1000f, 0, 1);
                float earlyProgress = Mth.clamp(progress * 2, 0, 1);
                zRot = Mth.DEG_TO_RAD * (earlyProgress * 2 * 360);
                upScaling = earlyProgress * 1.125f;
                downScaling = 1 + (1 - Mth.clamp(progress * 2 - 1, 0, 1)) * .125f;
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
            return new MechanicalCrafterCraftingItemRenderState(scale, centering, before, yRot, zRot, upScaling, downScaling, states);
        }

        @Override
        public void render(SubmitNodeCollector queue, PoseStack ms, int light) {
            if (before != null) {
                ms.pushPose();
                ms.scale(scale, scale, scale);
                ms.translate(centering);
                for (GridItemRenderState state : before) {
                    state.render(queue, ms, yRot, light);
                }
                ms.popPose();
            }
            if (states != null) {
                ms.mulPose(Axis.ZP.rotation(zRot));
                ms.scale(upScaling, upScaling, upScaling);
                ms.scale(downScaling, downScaling, downScaling);
                for (ItemStackRenderState state : states) {
                    ms.pushPose();
                    ms.mulPose(Axis.YP.rotation(yRot));
                    state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
                    ms.popPose();
                }
            }
        }
    }

    public record MechanicalCrafterPhaseItemRenderState(List<GridItemRenderState> states, float yRot) implements MechanicalCrafterItemRenderState {
        public static MechanicalCrafterPhaseItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            Level world,
            BlockState blockState,
            Phase phase
        ) {
            Map<Pair<Integer, Integer>, ItemStack> grid = be.groupedItems.grid;
            if (grid.isEmpty()) {
                return null;
            }
            float distance = .5f;
            boolean onlyRenderFirst = phase == Phase.INSERTING;
            boolean isExporting = phase == Phase.EXPORTING && blockState.hasProperty(MechanicalCrafterBlock.POINTING);
            Pointing pointing = isExporting ? blockState.getValue(MechanicalCrafterBlock.POINTING) : null;
            float yRot = Mth.DEG_TO_RAD * 180;
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
                ItemStackRenderState state = new ItemStackRenderState();
                state.displayContext = ItemDisplayContext.FIXED;
                itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
                states.add(new GridItemRenderState(state, offsetX, offsetY, offsetZ));
            });
            return new MechanicalCrafterPhaseItemRenderState(states, yRot);
        }

        @Override
        public void render(SubmitNodeCollector queue, PoseStack ms, int light) {
            for (GridItemRenderState state : states) {
                state.render(queue, ms, yRot, light);
            }
        }
    }

    public record GridItemRenderState(ItemStackRenderState state, float offsetX, float offsetY, float offsetZ) {
        public void render(SubmitNodeCollector queue, PoseStack ms, float yRot, int light) {
            ms.pushPose();
            ms.translate(offsetX, offsetY, 0);
            ms.mulPose(Axis.YP.rotation(yRot));
            ms.translate(0, 0, offsetZ);
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
    }
}
