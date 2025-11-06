package com.zurrtum.create.client.compat.jei.renderer;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JunkSlotRenderer implements IIngredientRenderer<ItemStack> {
    private static final JunkSlotRenderer INSTANCE = new JunkSlotRenderer();

    public static void addSlot(IRecipeLayoutBuilder builder, int x, int y, float chance) {
        ItemStack temp = Items.BARRIER.getDefaultStack();
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat("chance", chance);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, temp, nbt);
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y).setCustomRenderer(VanillaTypes.ITEM_STACK, INSTANCE).add(temp);
    }

    @Override
    public void render(DrawContext graphics, ItemStack temp) {
        AllGuiTextures.JEI_CHANCE_SLOT.render(graphics, -1, -1);
        Text text = Text.literal("?").formatted(Formatting.BOLD);
        TextRenderer textRenderer = graphics.client.textRenderer;
        graphics.drawText(textRenderer, text, textRenderer.getWidth(text) / -2 + 7, 4, 0xffefefef, true);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipType tooltipFlag) {
        tooltip.clear();
        IIngredientRenderer.super.getTooltip(tooltip, ingredient, tooltipFlag);
    }

    @Override
    @NotNull
    public List<Text> getTooltip(ItemStack temp, TooltipType tooltipFlag) {
        float chance = temp.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getFloat("chance", 0);
        String number = chance < 0.01 ? "<1" : chance > 0.99 ? ">99" : String.valueOf(Math.round(chance * 100));
        return List.of(
            CreateLang.translateDirect("recipe.assembly.junk"),
            CreateLang.translateDirect("recipe.processing.chance", number).formatted(Formatting.GOLD)
        );
    }
}
