package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.client.content.logistics.filter.AttributeFilterScreen;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.packet.c2s.GhostItemSubmitPacket;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class GhostIngredientHandler<T extends AbstractSimiContainerScreen<? extends GhostItemMenu<?>>> implements IGhostIngredientHandler<T> {

    @Override
    @NotNull
    public <I> List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new LinkedList<>();

        if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
            List<Slot> slots = gui.getMenu().slots;
            if (gui instanceof AttributeFilterScreen) {
                if (slots.get(36).isActive())
                    targets.add(new GhostTarget<>(gui, 0, true));
            } else {
                for (int i = 36; i < slots.size(); i++) {
                    if (slots.get(i).isActive())
                        targets.add(new GhostTarget<>(gui, i - 36, false));
                }
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {
    }

    @Override
    public boolean shouldHighlightTargets() {
        // TODO change to false and highlight the slots ourself in some better way
        return true;
    }

    private static class GhostTarget<I, T extends AbstractSimiContainerScreen<? extends GhostItemMenu<?>>> implements Target<I> {

        private final Rect2i area;
        private final T gui;
        private final int slotIndex;
        private final boolean isAttributeFilter;

        public GhostTarget(T gui, int slotIndex, boolean isAttributeFilter) {
            this.gui = gui;
            this.slotIndex = slotIndex;
            this.isAttributeFilter = isAttributeFilter;
            Slot slot = gui.getMenu().slots.get(slotIndex + 36);
            this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        @NotNull
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(I ingredient) {
            ItemStack stack = ((ItemStack) ingredient).copy();
            stack.setCount(1);
            gui.getMenu().ghostInventory.setItem(slotIndex, stack);

            if (isAttributeFilter)
                return;

            // sync new filter contents with server
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.connection.send(new GhostItemSubmitPacket(stack, slotIndex));
            }
        }
    }
}