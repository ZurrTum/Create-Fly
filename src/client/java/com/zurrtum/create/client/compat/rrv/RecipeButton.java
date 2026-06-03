package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.ReliablePlainButton;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

public class RecipeButton extends ReliablePlainButton {
    private @Nullable IntSet missingIndices;

    public RecipeButton(
        Screen screen,
        RecipeTransferHandler handler,
        ReliableClientRecipe view,
        MutableComponent literal,
        int x,
        int y,
        int width,
        int height
    ) {
        super(literal, new RecipeTransferAction(screen, handler, view), x, y, width, height);
    }

    public void init() {
        active = ((RecipeTransferAction) onPress).onCheck(this);
        visible = active || tooltip.get() != null;
    }

    public void setTooltip(Component tooltip) {
        setTooltip(Tooltip.create(tooltip));
    }

    public void updateMissing(IntSet missingIndices, Component tooltip) {
        this.missingIndices = missingIndices;
        setTooltip(Tooltip.create(tooltip));
    }

    public void setSuccess() {
        missingIndices = null;
        super.setTooltip(null);
    }

    public void renderInvalidSlots(RecipeViewMenu menu, GuiGraphicsExtractor guiGraphics, int displayId) {
        if (missingIndices == null) {
            return;
        }
        int left = -menu.guiOffsetLeft();
        int top = -menu.guiOffsetTop(displayId);
        int index = displayId * menu.getClientRecipeType().getSlotCount();
        for (int i : missingIndices) {
            Slot slot = menu.getSlot(index + i);
            int x = left + slot.x;
            int y = top + slot.y;
            guiGraphics.fill(x, y, x + 16, y + 16, 0x40FF0000);
        }
    }

    public record RecipeTransferAction(Screen screen, RecipeTransferHandler handler,
                                       ReliableClientRecipe view) implements OnPress {
        @Override
        public void onPress(Button button) {
            if (handler.handle(screen, view, (RecipeButton) button, true)) {
                Minecraft.getInstance().setScreen(screen);
            }
        }

        public boolean onCheck(RecipeButton button) {
            return handler.handle(screen, view, button, false);
        }
    }
}
