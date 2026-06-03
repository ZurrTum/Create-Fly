package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.api.overlay.ButtonData;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.DisplayInfo;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.Create.MOD_ID;

public abstract class CreateCategory implements ReliableClientRecipeType {
    private final Identifier id;

    public CreateCategory(String name) {
        id = Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(id.getNamespace() + ".recipe." + id.getPath());
    }

    @Override
    public void renderIcon(
        RecipeViewScreen screen,
        int x,
        int y,
        GuiGraphicsExtractor guiGraphics,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        guiGraphics.fakeItem(getIcon(), x, y);
        ItemStack subIcon = getSubIcon();
        if (subIcon != null) {
            Matrix3x2fStack matrices = guiGraphics.pose();
            matrices.pushMatrix();
            matrices.translate(x + 9, y + 9);
            matrices.scale(0.5f, 0.5f);
            guiGraphics.item(subIcon, 0, 0);
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
    public ButtonData placeRecipeTransferButton(DisplayInfo info) {
        return new ButtonData(info.guiLeft() + getDisplayWidth() - 31, info.guiTop() - 6, true);
    }

    @Override
    public ButtonData placeRecipeShareButton(DisplayInfo info) {
        return new ButtonData(info.guiLeft() + getDisplayWidth() - 17, info.guiTop() - 6, true);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Nullable
    @Override
    public Identifier getGuiTexture() {
        return null;
    }
}
