package com.zurrtum.create.client.compat.eiv;

import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import static com.zurrtum.create.Create.MOD_ID;

public abstract class CreateCategory implements IEivRecipeViewType {
    public void placeButton(ButtonWidget button) {
        button.setX(button.getX() - 21);
        button.setY(button.getY() - (getDisplayHeight() / 2));
    }

    public void renderSubIcon(DrawContext context, int x, int y) {
        ItemStack subIcon = getSubIcon();
        if (subIcon != null) {
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(x + 13, y + 13);
            matrices.scale(0.5f, 0.5f);
            context.drawItem(subIcon, 0, 0);
            matrices.popMatrix();
        }
    }

    @Nullable
    public ItemStack getSubIcon() {
        return null;
    }

    @Override
    public int getDisplayWidth() {
        return 170;
    }

    @Override
    public void placeSlots(SlotDefinition slotDefinition) {
        for (int i = 0, size = getSlotCount(); i < size; i++) {
            slotDefinition.addItemSlot(i, 0, 0);
        }
    }

    @Override
    public Identifier getGuiTexture() {
        return Identifier.of(MOD_ID, "textures/gui/jei/background.png");
    }
}
