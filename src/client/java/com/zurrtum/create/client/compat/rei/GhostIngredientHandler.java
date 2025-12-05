package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.content.equipment.blueprint.BlueprintScreen;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.zurrtum.create.client.content.logistics.filter.AbstractFilterScreen;
import com.zurrtum.create.client.content.logistics.filter.AttributeFilterScreen;
import com.zurrtum.create.client.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerScreen;
import com.zurrtum.create.client.content.trains.schedule.ScheduleScreen;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.packet.c2s.GhostItemSubmitPacket;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GhostIngredientHandler<T extends GhostItemMenu<?>> implements DraggableStackVisitor<AbstractSimiContainerScreen<T>> {
    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<AbstractSimiContainerScreen<T>> context, DraggableStack stack) {
        Stream<BoundsProvider> bounds = getDraggableAcceptingBounds(context, stack);
        Point cursor = context.getCurrentPosition();
        if (cursor != null) {
            int x = cursor.getX();
            int y = cursor.getY();
            Optional<BoundsProvider> target = bounds.filter(b -> {
                Box box = b.bounds().getBoundingBox();
                double minX = box.minX;
                double minY = box.minY;
                double maxX = box.maxX;
                double maxY = box.maxY;
                return x >= minX && x <= maxX && y >= minY && y <= maxY && b instanceof GhostTarget;
            }).findFirst();
            if (target.isPresent() && target.get() instanceof GhostTarget<?> ghost) {
                Object held = stack.getStack().getValue();
                if (held instanceof ItemStack item) {
                    ghost.accept(item);
                    return DraggedAcceptorResult.CONSUMED;
                }
            }
        }
        return DraggableStackVisitor.super.acceptDraggedStack(context, stack);
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<AbstractSimiContainerScreen<T>> context, DraggableStack stack) {
        List<BoundsProvider> targets = new ArrayList<>();
        AbstractSimiContainerScreen<T> gui = context.getScreen();

        if (stack.getStack().getValue() instanceof ItemStack) {
            if (gui instanceof AttributeFilterScreen) {
                if (gui.getScreenHandler().slots.get(36).isEnabled())
                    targets.add(new GhostTarget<>(gui, 0, true));
            } else {
                for (int i = 36; i < gui.getScreenHandler().slots.size(); i++) {
                    if (gui.getScreenHandler().slots.get(i).isEnabled())
                        targets.add(new GhostTarget<>(gui, i - 36, false));
                }
            }
        }
        return targets.stream();
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof AbstractFilterScreen || screen instanceof BlueprintScreen || screen instanceof LinkedControllerScreen || screen instanceof ScheduleScreen || screen instanceof RedstoneRequesterScreen || screen instanceof FactoryPanelSetItemScreen;
    }

    private static class GhostTarget<T extends GhostItemMenu<?>> implements BoundsProvider {

        private final VoxelShape area;
        private final AbstractSimiContainerScreen<T> gui;
        private final int slotIndex;
        private final boolean isAttributeFilter;

        public GhostTarget(AbstractSimiContainerScreen<T> gui, int slotIndex, boolean isAttributeFilter) {
            this.gui = gui;
            this.slotIndex = slotIndex;
            this.isAttributeFilter = isAttributeFilter;
            Slot slot = gui.getScreenHandler().slots.get(slotIndex + 36);
            int minX = gui.getGuiLeft() + slot.x;
            int minY = gui.getGuiTop() + slot.y;
            this.area = VoxelShapes.cuboidUnchecked(minX, minY, 0, minX + 16, minY + 16, 0.1);
        }

        public void accept(ItemStack ingredient) {
            ItemStack stack = ingredient.copy();
            stack.setCount(1);
            gui.getScreenHandler().ghostInventory.setStack(slotIndex, stack);

            if (isAttributeFilter)
                return;

            // sync new filter contents with server
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.networkHandler.sendPacket(new GhostItemSubmitPacket(stack, slotIndex));
            }
        }

        @Override
        public VoxelShape bounds() {
            return area;
        }
    }
}
