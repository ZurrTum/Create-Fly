package com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.AllBlocks;
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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FilteringRenderer {
    public static void tick(MinecraftClient mc) {
        HitResult target = mc.crosshairTarget;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientWorld world = mc.world;
        BlockPos pos = result.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (mc.player.isSneaking())
            return;
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity sbe))
            return;

        ItemStack mainhandItem = mc.player.getStackInHand(Hand.MAIN_HAND);

        List<FilteringBehaviour<?>> behaviours;
        if (sbe instanceof FactoryPanelBlockEntity fpbe) {
            behaviours = FactoryPanelBehaviour.allBehaviours(fpbe);
        } else {
            FilteringBehaviour<?> behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
            if (behaviour instanceof SidedFilteringBehaviour sidedBehaviour) {
                behaviour = sidedBehaviour.get(result.getSide());
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
                ((Sided) behaviour.slotPositioning).fromSide(result.getSide());
            if (!behaviour.slotPositioning.shouldRender(state))
                continue;
            if (!behaviour.mayInteract(mc.player))
                continue;

            ItemStack filter = behaviour.getFilter();
            boolean isFilterSlotted = filter.getItem() instanceof FilterItem;
            boolean showCount = behaviour.isCountVisible();
            Text label = behaviour.getLabel();
            boolean hit = behaviour.slotPositioning.testHit(world, pos, state, target.getPos().subtract(Vec3d.of(pos)));

            Box emptyBB = new Box(Vec3d.ZERO, Vec3d.ZERO);
            Box bb = isFilterSlotted ? emptyBB.expand(.45f, .31f, .2f) : emptyBB.expand(.25f);

            ValueBox box = new ItemValueBox(label, bb, pos, filter, behaviour.getCountLabelForValueBox());
            box.passive(!hit || behaviour.bypassesInput(mainhandItem));

            Outliner.getInstance().showOutline(Pair.of("filter" + behaviour.netId(), pos), box.transform(behaviour.slotPositioning))
                .lineWidth(1 / 64f).withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null).highlightFace(result.getSide());

            if (!hit)
                continue;

            List<MutableText> tip = new ArrayList<>();
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
        ItemModelManager itemModelManager,
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
                            list.add(SingleFilterRenderState.create(slotPositioning, itemModelManager, filter, be.getWorld()));
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
            return SidedFilterRenderState.create(sidedFilteringBehaviour, blockState, itemModelManager, be.getWorld());
        }
        ItemStack filter = behaviour.getFilter();
        if (filter.isEmpty()) {
            return null;
        }
        ValueBoxTransform slotPositioning = behaviour.getSlotPositioning();
        if (slotPositioning instanceof Sided sided) {
            return SidedSingleFilterRenderState.create(sided, blockState, itemModelManager, filter, be.getWorld());
        }
        return SingleFilterRenderState.create(slotPositioning, itemModelManager, filter, be.getWorld());
    }

    private static boolean isOutOfRange(FilteringBehaviour<?> behaviour, double distance) {
        if (distance == -1) {
            return false;
        }
        float max = behaviour.getRenderDistance();
        return max * max < distance;
    }

    public interface FilterRenderState {
        void render(BlockState blockState, OrderedRenderCommandQueue queue, MatrixStack ms, int light);
    }

    public record FactoryPanelRenderState(List<SingleFilterRenderState> states) implements FilterRenderState {
        @Override
        public void render(BlockState blockState, OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            for (SingleFilterRenderState state : states) {
                state.render(blockState, queue, ms, light);
            }
        }
    }

    public record SingleFilterRenderState(ValueBoxTransform slotPositioning, ItemRenderState state, float offset) implements FilterRenderState {
        public static SingleFilterRenderState create(
            ValueBoxTransform slotPositioning,
            ItemModelManager itemModelManager,
            ItemStack stack,
            World world
        ) {
            ItemRenderState renderState = new ItemRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(renderState, stack, ItemDisplayContext.FIXED, world, null, 0);
            return new SingleFilterRenderState(slotPositioning, renderState, ValueBoxRenderer.customZOffset(stack.getItem()));
        }

        @Override
        public void render(BlockState blockState, OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            ms.push();
            slotPositioning.transform(blockState, ms);
            ValueBoxRenderer.renderItemIntoValueBox(state, queue, ms, light, offset);
            ms.pop();
        }
    }

    public record SidedSingleFilterRenderState(
        Sided sided, Direction side, ItemRenderState state, Float offset, List<Direction> sides
    ) implements FilterRenderState {
        public static SidedSingleFilterRenderState create(
            Sided sided,
            BlockState blockState,
            ItemModelManager itemModelManager,
            ItemStack filter,
            World world
        ) {
            ItemRenderState renderState = new ItemRenderState();
            Float offset;
            if (blockState.isOf(AllBlocks.CONTRAPTION_CONTROLS)) {
                renderState.displayContext = ItemDisplayContext.GUI;
                offset = null;
            } else {
                renderState.displayContext = ItemDisplayContext.FIXED;
                offset = ValueBoxRenderer.customZOffset(filter.getItem());
            }
            itemModelManager.update(renderState, filter, renderState.displayContext, world, null, 0);
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
        public void render(BlockState blockState, OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            boolean flat = offset == null;
            for (Direction side : sides) {
                ms.push();
                sided.fromSide(side);
                sided.transform(blockState, ms);
                if (flat) {
                    ValueBoxRenderer.renderFlatItemIntoValueBox(state, queue, ms, light);
                } else {
                    ValueBoxRenderer.renderItemIntoValueBox(state, queue, ms, light, offset);
                }
                ms.pop();
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
            ItemModelManager itemModelManager,
            World world
        ) {
            boolean flat = blockState.isOf(AllBlocks.CONTRAPTION_CONTROLS);
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
                ItemRenderState renderState = new ItemRenderState();
                if (flat) {
                    renderState.displayContext = ItemDisplayContext.GUI;
                    itemModelManager.update(renderState, filter, renderState.displayContext, world, null, 0);
                    boxes.add(new FlatBoxState(direction, renderState));
                } else {
                    renderState.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.update(renderState, filter, renderState.displayContext, world, null, 0);
                    boxes.add(new BoxState(direction, renderState, ValueBoxRenderer.customZOffset(filter.getItem())));
                }
            }
            sided.fromSide(side);
            return boxes.isEmpty() ? null : new SidedFilterRenderState(sided, side, boxes);
        }

        @Override
        public void render(BlockState blockState, OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            for (BoxRenderState box : boxes) {
                ms.push();
                slotPositioning.fromSide(box.side());
                slotPositioning.transform(blockState, ms);
                box.render(queue, ms, light);
                ms.pop();
            }
            slotPositioning.fromSide(side);
        }

        public interface BoxRenderState {
            Direction side();

            void render(OrderedRenderCommandQueue queue, MatrixStack ms, int light);
        }

        record FlatBoxState(Direction side, ItemRenderState state) implements BoxRenderState {
            @Override
            public void render(OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
                ValueBoxRenderer.renderFlatItemIntoValueBox(state, queue, ms, light);
            }
        }

        record BoxState(Direction side, ItemRenderState state, float offset) implements BoxRenderState {
            @Override
            public void render(OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
                ValueBoxRenderer.renderItemIntoValueBox(state, queue, ms, light, offset);
            }
        }
    }
}
