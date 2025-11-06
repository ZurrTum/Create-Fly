package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.client.content.logistics.filter.AttributeFilterScreen;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.packet.c2s.GhostItemSubmitPacket;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class GhostIngredientHandler<T extends GhostItemMenu<?>> implements IGhostIngredientHandler<AbstractSimiContainerScreen<T>> {

    @Override
    @NotNull
    public <I> List<Target<I>> getTargetsTyped(AbstractSimiContainerScreen<T> gui, ITypedIngredient<I> ingredient, boolean doStart) {
        boolean isAttributeFilter = gui instanceof AttributeFilterScreen;
        List<Target<I>> targets = new LinkedList<>();

        if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
            for (int i = 36; i < gui.getScreenHandler().slots.size(); i++) {
                if (gui.getScreenHandler().slots.get(i).isEnabled())
                    targets.add(new GhostTarget<>(gui, i - 36, isAttributeFilter));

                // Only accept items in 1st slot. 2nd is used for functionality, don't wanna
                // override that one
                if (isAttributeFilter)
                    break;
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

    private static class GhostTarget<I, T extends GhostItemMenu<?>> implements Target<I> {

        private final Rect2i area;
        private final AbstractSimiContainerScreen<T> gui;
        private final int slotIndex;
        private final boolean isAttributeFilter;

        public GhostTarget(AbstractSimiContainerScreen<T> gui, int slotIndex, boolean isAttributeFilter) {
            this.gui = gui;
            this.slotIndex = slotIndex;
            this.isAttributeFilter = isAttributeFilter;
            Slot slot = gui.getScreenHandler().slots.get(slotIndex + 36);
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
            gui.getScreenHandler().ghostInventory.setStack(slotIndex, stack);

            if (isAttributeFilter)
                return;

            // sync new filter contents with server
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.networkHandler.sendPacket(new GhostItemSubmitPacket(stack, slotIndex));
            }
        }
    }
}