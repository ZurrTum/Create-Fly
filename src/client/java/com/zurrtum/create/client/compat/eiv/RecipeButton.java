package com.zurrtum.create.client.compat.eiv;

import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class RecipeButton extends ButtonWidget {
    private IntSet missingIndices;

    public RecipeButton(int x, int y, int width, int height, Text message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }

    public static ButtonWidget.Builder builder(Text message, RecipeTransferContext context, IEivViewRecipe view) {
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

    public void setTooltip(Text tooltip) {
        setTooltip(Tooltip.of(tooltip));
    }

    public void updateMissing(IntSet missingIndices, Text tooltip) {
        this.missingIndices = missingIndices;
        setTooltip(Tooltip.of(tooltip));
    }

    public void setSuccess() {
        missingIndices = null;
        setTooltip((Tooltip) null);
    }

    public void renderInvalidSlots(DrawContext context, int displayId) {
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

    public static class Builder extends ButtonWidget.Builder {
        private final Text message;
        private final PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private NarrationSupplier narrationSupplier = ButtonWidget.DEFAULT_NARRATION_SUPPLIER;

        public Builder(Text message, PressAction onPress) {
            super(message, onPress);
            this.message = message;
            this.onPress = onPress;
        }

        @Override
        public ButtonWidget.Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        @Override
        public ButtonWidget.Builder width(int width) {
            this.width = width;
            return this;
        }

        @Override
        public ButtonWidget.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        @Override
        public ButtonWidget.Builder dimensions(int x, int y, int width, int height) {
            return position(x, y).size(width, height);
        }

        @Override
        public ButtonWidget.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        @Override
        public ButtonWidget.Builder narrationSupplier(NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        @Override
        public ButtonWidget build() {
            ButtonWidget buttonWidget = new RecipeButton(x, y, width, height, message, onPress, narrationSupplier);
            buttonWidget.setTooltip(tooltip);
            return buttonWidget;
        }
    }

    public record RecipeTransferAction(RecipeTransferContext context, IEivViewRecipe view) implements PressAction {
        @Override
        public void onPress(ButtonWidget button) {
            if (context.handler().handle(context.screen(), view, (RecipeButton) button, true)) {
                MinecraftClient.getInstance().setScreen(context.screen());
            }
        }

        public boolean onCheck(RecipeButton button) {
            return context.handler().handle(context.screen(), view, button, false);
        }
    }
}
