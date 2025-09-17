package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.MixingDisplay;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.client.compat.rei.IngredientHelper.condenseIngredients;
import static com.zurrtum.create.client.compat.rei.IngredientHelper.getRenderEntryStack;

public class MixingCategory implements DisplayCategory<MixingDisplay> {
    @Override
    public CategoryIdentifier<? extends MixingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.MIXING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.mixing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER.getDefaultStack(), AllItems.BASIN.getDefaultStack());
    }

    @Override
    public List<Widget> setupDisplay(MixingDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        List<EntryIngredient> ingredients = condenseIngredients(display.inputs());
        List<Point> points = new ArrayList<>();
        for (int i = 0, size = ingredients.size(), xOffset = size < 3 ? (3 - size) * 19 / 2 : 0; i < size; i++) {
            points.add(new Point(bounds.x + 17 + xOffset + (i % 3) * 19, bounds.y + 56 - (i / 3) * 19));
        }
        Point output = new Point(bounds.x + 147, bounds.y + 56);
        HeatCondition requiredHeat = display.heat();
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            for (Point point : points) {
                AllGuiTextures.JEI_SLOT.render(graphics, point.x - 1, point.y - 1);
            }
            AllGuiTextures.JEI_SLOT.render(graphics, output.x - 1, output.y - 1);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + 37);
            Matrix3x2f pose = new Matrix3x2f(graphics.getMatrices());
            if (requiredHeat == HeatCondition.NONE) {
                AllGuiTextures.JEI_NO_HEAT_BAR.render(graphics, bounds.x + 9, bounds.y + 85);
                AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 86, bounds.y + 73);
            } else {
                AllGuiTextures.JEI_HEAT_BAR.render(graphics, bounds.x + 9, bounds.y + 85);
                AllGuiTextures.JEI_LIGHT.render(graphics, bounds.x + 86, bounds.y + 93);
                graphics.state.addSpecialElement(new BasinBlazeBurnerRenderState(
                    pose,
                    bounds.x + 96,
                    bounds.y + 74,
                    requiredHeat.visualizeAsBlazeBurner()
                ));
            }
            graphics.state.addSpecialElement(new MixingBasinRenderState(pose, bounds.x + 96, bounds.y));
            graphics.drawText(
                MinecraftClient.getInstance().textRenderer,
                CreateLang.translateDirect(requiredHeat.getTranslationKey()),
                bounds.x + 14,
                bounds.y + 91,
                requiredHeat.getColor(),
                false
            );
        }));
        for (int i = 0, size = points.size(); i < size; i++) {
            widgets.add(Widgets.createSlot(points.get(i)).markInput().disableBackground().entries(getRenderEntryStack(ingredients.get(i))));
        }
        widgets.add(Widgets.createSlot(output).markOutput().disableBackground().entries(getRenderEntryStack(display.output())));
        if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
            widgets.add(Widgets.createSlot(new Point(bounds.x + 139, bounds.y + 86)).disableBackground()
                .entries(EntryIngredients.of(AllItems.BLAZE_BURNER)));
        }
        if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
            widgets.add(Widgets.createSlot(new Point(bounds.x + 158, bounds.y + 86)).disableBackground()
                .entries(EntryIngredients.of(AllItems.BLAZE_CAKE)));
        }
        return widgets;
    }

    @Override
    public int getDisplayWidth(MixingDisplay display) {
        return 187;
    }

    @Override
    public int getDisplayHeight() {
        return 113;
    }
}