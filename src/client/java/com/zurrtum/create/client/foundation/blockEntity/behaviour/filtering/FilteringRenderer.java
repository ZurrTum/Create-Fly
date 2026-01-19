package com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox.ItemValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FilteringRenderer {
    public static void tick(Minecraft mc) {
        HitResult target = mc.hitResult;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientLevel world = mc.level;
        BlockPos pos = result.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (mc.player.isShiftKeyDown())
            return;
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity sbe))
            return;

        ItemStack mainhandItem = mc.player.getItemInHand(InteractionHand.MAIN_HAND);

        List<FilteringBehaviour<?>> behaviours;
        if (sbe instanceof FactoryPanelBlockEntity fpbe) {
            behaviours = FactoryPanelBehaviour.allBehaviours(fpbe);
        } else {
            FilteringBehaviour<?> behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
            if (behaviour instanceof SidedFilteringBehaviour sidedBehaviour) {
                behaviour = sidedBehaviour.get(result.getDirection());
            }
            if (behaviour == null) {
                return;
            }
            behaviours = List.of(behaviour);
        }

        for (FilteringBehaviour<?> behaviour : behaviours) {
            if (!behaviour.isActive())
                continue;
            if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
                ((Sided) behaviour.slotPositioning).fromSide(result.getDirection());
            if (!behaviour.slotPositioning.shouldRender(state))
                continue;
            if (!behaviour.mayInteract(mc.player))
                continue;

            ItemStack filter = behaviour.getFilter();
            boolean isFilterSlotted = filter.getItem() instanceof FilterItem;
            boolean showCount = behaviour.isCountVisible();
            Component label = behaviour.getLabel();
            boolean hit = behaviour.slotPositioning.testHit(world, pos, state, target.getLocation().subtract(Vec3.atLowerCornerOf(pos)));

            AABB emptyBB = new AABB(Vec3.ZERO, Vec3.ZERO);
            AABB bb = isFilterSlotted ? emptyBB.inflate(.45f, .31f, .2f) : emptyBB.inflate(.25f);

            ValueBox box = new ItemValueBox(label, bb, pos, filter, behaviour.getCountLabelForValueBox());
            box.passive(!hit || behaviour.bypassesInput(mainhandItem));

            Outliner.getInstance().showOutline(Pair.of("filter" + behaviour.netId(), pos), box.transform(behaviour.slotPositioning))
                .lineWidth(1 / 64f).withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null).highlightFace(result.getDirection());

            if (!hit)
                continue;

            List<MutableComponent> tip = new ArrayList<>();
            tip.add(label.copy());
            tip.add(behaviour.getTip());
            if (showCount)
                tip.add(behaviour.getAmountTip());

            Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
        }
    }

    @Nullable
    public static FilterRenderState getFilterRenderState(
        SmartBlockEntity be,
        BlockState blockState,
        ItemModelResolver itemModelManager,
        double distance
    ) {
        if (be instanceof FactoryPanelBlockEntity) {
            List<SingleFilterRenderState> list = null;
            int count = 0;
            boolean check = distance != -1;
            for (BlockEntityBehaviour<?> behaviour : be.getAllBehaviours()) {
                if (behaviour instanceof FactoryPanelBehaviour factoryPanelBehaviour) {
                    if (factoryPanelBehaviour.behaviour != null && factoryPanelBehaviour.isActive()) {
                        if (check) {
                            if (isOutOfRange(factoryPanelBehaviour, distance)) {
                                return null;
                            }
                            check = false;
                        }
                        ItemStack filter = factoryPanelBehaviour.getFilter();
                        ValueBoxTransform slotPositioning = factoryPanelBehaviour.getSlotPositioning();
                        if (!filter.isEmpty() && slotPositioning.shouldRender(blockState)) {
                            if (list == null) {
                                list = new ArrayList<>(4);
                            }
                            list.add(SingleFilterRenderState.create(slotPositioning, itemModelManager, filter, be.getLevel()));
                        }
                    }
                    if (++count == 4) {
                        break;
                    }
                }
            }
            return list == null ? null : new FactoryPanelRenderState(list);
        }
        FilteringBehaviour<?> behaviour = be.getBehaviour(FilteringBehaviour.TYPE);
        if (behaviour == null) {
            return null;
        }
        if (!be.isVirtual() && (behaviour.behaviour == null || !behaviour.isActive() || isOutOfRange(behaviour, distance))) {
            return null;
        }
        if (behaviour instanceof SidedFilteringBehaviour sidedFilteringBehaviour) {
            return SidedFilterRenderState.create(sidedFilteringBehaviour, blockState, itemModelManager, be.getLevel());
        }
        ItemStack filter = behaviour.getFilter();
        if (filter.isEmpty()) {
            return null;
        }
        ValueBoxTransform slotPositioning = behaviour.getSlotPositioning();
        if (slotPositioning instanceof Sided sided) {
            return SidedSingleFilterRenderState.create(sided, blockState, itemModelManager, filter, be.getLevel());
        }
        return SingleFilterRenderState.create(slotPositioning, itemModelManager, filter, be.getLevel());
    }

    private static boolean isOutOfRange(FilteringBehaviour<?> behaviour, double distance) {
        if (distance == -1) {
            return false;
        }
        float max = behaviour.getRenderDistance();
        return max * max < distance;
    }

    public interface FilterRenderState {
        void render(BlockState blockState, SubmitNodeCollector queue, PoseStack ms, int light);
    }

    public record FactoryPanelRenderState(List<SingleFilterRenderState> states) implements FilterRenderState {
        @Override
        public void render(BlockState blockState, SubmitNodeCollector queue, PoseStack ms, int light) {
            for (SingleFilterRenderState state : states) {
                state.render(blockState, queue, ms, light);
            }
        }
    }

    public record SingleFilterRenderState(ValueBoxTransform slotPositioning, ItemStackRenderState state, float offset) implements FilterRenderState {
        public static SingleFilterRenderState create(
            ValueBoxTransform slotPositioning,
            ItemModelResolver itemModelManager,
            ItemStack stack,
            Level world
        ) {
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, stack, ItemDisplayContext.FIXED, world, null, 0);
            return new SingleFilterRenderState(slotPositioning, renderState, ValueBoxRenderer.customZOffset(stack.getItem()));
        }

        @Override
        public void render(BlockState blockState, SubmitNodeCollector queue, PoseStack ms, int light) {
            ms.pushPose();
            slotPositioning.transform(blockState, ms);
            ValueBoxRenderer.renderItemIntoValueBox(state, queue, ms, light, offset);
            ms.popPose();
        }
    }

    public record SidedSingleFilterRenderState(
        Sided sided, Direction side, ItemStackRenderState state, Float offset, List<Direction> sides
    ) implements FilterRenderState {
        public static SidedSingleFilterRenderState create(
            Sided sided,
            BlockState blockState,
            ItemModelResolver itemModelManager,
            ItemStack filter,
            Level world
        ) {
            ItemStackRenderState renderState = new ItemStackRenderState();
            Float offset;
            if (blockState.is(AllBlocks.CONTRAPTION_CONTROLS)) {
                renderState.displayContext = ItemDisplayContext.GUI;
                offset = null;
            } else {
                renderState.displayContext = ItemDisplayContext.FIXED;
                offset = ValueBoxRenderer.customZOffset(filter.getItem());
            }
            itemModelManager.appendItemLayers(renderState, filter, renderState.displayContext, world, null, 0);
            Direction side = sided.getSide();
            List<Direction> sides = new ArrayList<>();
            for (Direction direction : Iterate.directions) {
                sided.fromSide(direction);
                if (sided.shouldRender(blockState)) {
                    sides.add(direction);
                }
            }
            sided.fromSide(side);
            return new SidedSingleFilterRenderState(sided, side, renderState, offset, sides);
        }

        @Override
        public void render(BlockState blockState, SubmitNodeCollector queue, PoseStack ms, int light) {
            boolean flat = offset == null;
            for (Direction side : sides) {
                ms.pushPose();
                sided.fromSide(side);
                sided.transform(blockState, ms);
                if (flat) {
                    ValueBoxRenderer.renderFlatItemIntoValueBox(state, queue, ms, light);
                } else {
                    ValueBoxRenderer.renderItemIntoValueBox(state, queue, ms, light, offset);
                }
                ms.popPose();
            }
            sided.fromSide(side);
        }
    }

    public record SidedFilterRenderState(
        Sided slotPositioning, Direction side, List<BoxRenderState> boxes
    ) implements FilterRenderState {
        @Nullable
        public static FilterRenderState create(
            SidedFilteringBehaviour behaviour,
            BlockState blockState,
            ItemModelResolver itemModelManager,
            Level world
        ) {
            boolean flat = blockState.is(AllBlocks.CONTRAPTION_CONTROLS);
            Sided sided = behaviour.getSlotPositioning();
            List<SidedFilterRenderState.BoxRenderState> boxes = new ArrayList<>();
            Direction side = sided.getSide();
            for (Direction direction : Iterate.directions) {
                ItemStack filter = behaviour.getFilter(direction);
                if (filter.isEmpty())
                    continue;
                sided.fromSide(direction);
                if (!sided.shouldRender(blockState))
                    continue;
                ItemStackRenderState renderState = new ItemStackRenderState();
                if (flat) {
                    renderState.displayContext = ItemDisplayContext.GUI;
                    itemModelManager.appendItemLayers(renderState, filter, renderState.displayContext, world, null, 0);
                    boxes.add(new FlatBoxState(direction, renderState));
                } else {
                    renderState.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.appendItemLayers(renderState, filter, renderState.displayContext, world, null, 0);
                    boxes.add(new BoxState(direction, renderState, ValueBoxRenderer.customZOffset(filter.getItem())));
                }
            }
            sided.fromSide(side);
            return boxes.isEmpty() ? null : new SidedFilterRenderState(sided, side, boxes);
        }

        @Override
        public void render(BlockState blockState, SubmitNodeCollector queue, PoseStack ms, int light) {
            for (BoxRenderState box : boxes) {
                ms.pushPose();
                slotPositioning.fromSide(box.side());
                slotPositioning.transform(blockState, ms);
                box.render(queue, ms, light);
                ms.popPose();
            }
            slotPositioning.fromSide(side);
        }

        public interface BoxRenderState {
            Direction side();

            void render(SubmitNodeCollector queue, PoseStack ms, int light);
        }

        record FlatBoxState(Direction side, ItemStackRenderState state) implements BoxRenderState {
            @Override
            public void render(SubmitNodeCollector queue, PoseStack ms, int light) {
                ValueBoxRenderer.renderFlatItemIntoValueBox(state, queue, ms, light);
            }
        }

        record BoxState(Direction side, ItemStackRenderState state, float offset) implements BoxRenderState {
            @Override
            public void render(SubmitNodeCollector queue, PoseStack ms, int light) {
                ValueBoxRenderer.renderItemIntoValueBox(state, queue, ms, light, offset);
            }
        }
    }
}
