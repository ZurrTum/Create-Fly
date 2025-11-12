package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.MixingDisplay;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class MixingCategory extends CreateCategory<MixingDisplay> {
    @Override
    public CategoryIdentifier<? extends MixingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.MIXING;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.mixing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER, AllItems.BASIN);
    }

    @Override
    public void addWidgets(List<Widget> widgets, MixingDisplay display, Rectangle bounds) {
        List<EntryIngredient> ingredients = condenseIngredients(display.inputs());
        List<Point> points = new ArrayList<>();
        for (int i = 0, size = ingredients.size(), xOffset = size < 3 ? (3 - size) * 19 / 2 : 0; i < size; i++) {
            points.add(new Point(bounds.x + 17 + xOffset + (i % 3) * 19, bounds.y + 56 - (i / 3) * 19));
        }
        Point output = new Point(bounds.x + 147, bounds.y + 56);
        HeatCondition requiredHeat = display.heat();
        widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, points, output);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + 37);
            Matrix3x2f pose = new Matrix3x2f(graphics.pose());
            if (requiredHeat == HeatCondition.NONE) {
                AllGuiTextures.JEI_NO_HEAT_BAR.render(graphics, bounds.x + 9, bounds.y + 85);
                AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 86, bounds.y + 73);
            } else {
                AllGuiTextures.JEI_HEAT_BAR.render(graphics, bounds.x + 9, bounds.y + 85);
                AllGuiTextures.JEI_LIGHT.render(graphics, bounds.x + 86, bounds.y + 93);
                graphics.guiRenderState.submitPicturesInPictureState(new BasinBlazeBurnerRenderState(
                    pose,
                    bounds.x + 96,
                    bounds.y + 74,
                    requiredHeat.visualizeAsBlazeBurner()
                ));
            }
            graphics.guiRenderState.submitPicturesInPictureState(new MixingBasinRenderState(pose, bounds.x + 96, bounds.y));
            graphics.drawString(
                Minecraft.getInstance().font,
                CreateLang.translateDirect(requiredHeat.getTranslationKey()),
                bounds.x + 14,
                bounds.y + 91,
                requiredHeat.getColor(),
                false
            );
        }));
        for (int i = 0, size = points.size(); i < size; i++) {
            widgets.add(createInputSlot(points.get(i)).entries(getRenderEntryStack(ingredients.get(i))));
        }
        widgets.add(createOutputSlot(output).entries(getRenderEntryStack(display.output())));
        if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
            widgets.add(createSlot(new Point(bounds.x + 139, bounds.y + 86)).entries(EntryIngredients.of(AllItems.BLAZE_BURNER)));
        }
        if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
            widgets.add(createSlot(new Point(bounds.x + 158, bounds.y + 86)).entries(EntryIngredients.of(AllItems.BLAZE_CAKE)));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 113;
    }
}