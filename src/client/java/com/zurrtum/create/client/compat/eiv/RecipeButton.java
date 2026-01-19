package com.zurrtum.create.client.compat.eiv;

import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

public class RecipeButton extends Button.Plain {
    private IntSet missingIndices;

    public RecipeButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }

    public static Button.Builder builder(Component message, RecipeTransferContext context, IEivViewRecipe view) {
        return new Builder(message, new RecipeTransferAction(context, view));
    }

    public void init() {
        RecipeTransferAction action = (RecipeTransferAction) onPress;
        active = action.onCheck(this);
        visible = true;
        if (action.view.getViewType() instanceof CreateCategory category) {
            category.placeButton(this);
        }
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
        setTooltip((Tooltip) null);
    }

    public void renderInvalidSlots(GuiGraphics context, int displayId) {
        if (missingIndices == null) {
            return;
        }
        RecipeTransferAction action = (RecipeTransferAction) onPress;
        RecipeViewMenu menu = action.context.menu();
        IEivRecipeViewType type = action.view.getViewType();
        int left = (menu.getWidth() - type.getDisplayWidth()) / -2;
        int top = -24 - displayId * (type.getDisplayHeight() + 16);
        int index = displayId * type.getSlotCount();
        for (int i : missingIndices) {
            Slot slot = menu.getSlot(index + i);
            int x = left + slot.x;
            int y = top + slot.y;
            context.fill(x, y, x + 16, y + 16, 0x40FF0000);
        }
    }

    public static class Builder extends Button.Builder {
        private final Component message;
        private final OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private CreateNarration narrationSupplier = Button.DEFAULT_NARRATION;

        public Builder(Component message, OnPress onPress) {
            super(message, onPress);
            this.message = message;
            this.onPress = onPress;
        }

        @Override
        public Button.Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        @Override
        public Button.Builder width(int width) {
            this.width = width;
            return this;
        }

        @Override
        public Button.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        @Override
        public Button.Builder bounds(int x, int y, int width, int height) {
            return pos(x, y).size(width, height);
        }

        @Override
        public Button.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        @Override
        public Button.Builder createNarration(CreateNarration narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        @Override
        public Button build() {
            Button buttonWidget = new RecipeButton(x, y, width, height, message, onPress, narrationSupplier);
            buttonWidget.setTooltip(tooltip);
            return buttonWidget;
        }
    }

    public record RecipeTransferAction(RecipeTransferContext context, IEivViewRecipe view) implements OnPress {
        @Override
        public void onPress(Button button) {
            if (context.handler().handle(context.screen(), view, (RecipeButton) button, true)) {
                Minecraft.getInstance().setScreen(context.screen());
            }
        }

        public boolean onCheck(RecipeButton button) {
            return context.handler().handle(context.screen(), view, button, false);
        }
    }
}
