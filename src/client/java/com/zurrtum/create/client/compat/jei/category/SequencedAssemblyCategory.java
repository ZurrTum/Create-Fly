package com.zurrtum.create.client.compat.jei.category;

import com.mojang.datafixers.util.Either;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.client.foundation.gui.render.SpoutRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.zurrtum.create.infrastructure.component.BottleType;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SequencedAssemblyCategory extends CreateCategory<RecipeEntry<SequencedAssemblyRecipe>> {
    public static String[] ROMANS = {"I", "II", "III", "IV", "V", "VI", "-"};
    public static Map<RecipeType<?>, SequencedRenderer<?>> RENDER = new IdentityHashMap<>();

    static {
        registerRenderer(AllRecipeTypes.PRESSING, new PressingRenderer());
        registerRenderer(AllRecipeTypes.DEPLOYING, new DeployingRenderer());
        registerRenderer(AllRecipeTypes.FILLING, new FillingRenderer());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> SequencedRenderer<T> getRenderer(T recipe) {
        return (SequencedRenderer<T>) RENDER.get(recipe.getType());
    }

    public static <T extends Recipe<?>> void registerRenderer(RecipeType<T> type, SequencedRenderer<T> draw) {
        RENDER.put(type, draw);
    }

    public static List<RecipeEntry<SequencedAssemblyRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.SEQUENCED_ASSEMBLY).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<SequencedAssemblyRecipe>> getRecipeType() {
        return JeiClientPlugin.SEQUENCED_ASSEMBLY;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.sequenced_assembly");
    }

    @Override
    public IDrawable getIcon() {
        return new IconRenderer(AllItems.PRECISION_MECHANISM);
    }

    @Override
    public int getHeight() {
        return 115;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<SequencedAssemblyRecipe> entry, IFocusGroup focuses) {
        SequencedAssemblyRecipe recipe = entry.value();
        ChanceOutput chanceOutput = recipe.result();
        boolean randomOutput = chanceOutput.chance() != 1;
        int xOffset = randomOutput ? -7 : 0;
        builder.addInputSlot(xOffset + 22, 91).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addChanceSlot(builder, xOffset + 127, 91, chanceOutput);
        if (randomOutput) {
            addJunkSlot(builder, xOffset + 146, 91, 1 - chanceOutput.chance());
        }
        List<Recipe<?>> recipes = recipe.sequence();
        int size = recipes.size() / recipe.loops();
        for (int i = 0, left = 94 - 14 * size; i < size; i++) {
            addSlot(builder, left + i * 28, recipes.get(i), i);
        }
    }

    private static <T extends Recipe<?>> void addSlot(IRecipeLayoutBuilder builder, int x, T sequence, int i) {
        SequencedRenderer<T> renderer = getRenderer(sequence);
        if (renderer != null) {
            IRecipeSlotBuilder slot = renderer.addSlot(builder, x, 15, sequence);
            if (slot != null) {
                slot.addRichTooltipCallback(new SequenceTooltip<>(renderer, sequence, i)).setSlotName(String.valueOf(i));
            }
        }
    }

    @Override
    public void draw(
        RecipeEntry<SequencedAssemblyRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        DrawContext graphics,
        double mouseX,
        double mouseY
    ) {
        SequencedAssemblyRecipe recipe = entry.value();
        ChanceOutput chanceOutput = recipe.result();
        boolean randomOutput = chanceOutput.chance() != 1;
        int xOffset = randomOutput ? -7 : 0;
        List<Recipe<?>> recipes = recipe.sequence();
        int size = recipes.size() / recipe.loops();
        TextRenderer textRenderer = graphics.client.textRenderer;
        for (int i = 0, left = 94 - 14 * size; i < size; i++) {
            int x = left + i * 28;
            String text = ROMANS[Math.min(i, ROMANS.length)];
            Optional<IRecipeSlotView> slot = recipeSlotsView.findSlotByName(String.valueOf(i));
            if (slot.isPresent()) {
                AllGuiTextures.JEI_SLOT.render(graphics, x - 1, 14);
            }
            graphics.drawText(textRenderer, text, x + 8 - textRenderer.getWidth(text) / 2, 2, 0xff888888, false);
            SequencedRenderer<?> draw = getRenderer(recipes.get(i));
            if (draw != null) {
                draw.render(graphics, i, x, 15, slot);
            }
        }
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, xOffset + 47, 94);
        if (recipe.loops() > 1) {
            AllIcons.I_SEQ_REPEAT.render(graphics, xOffset + 60, 99);
            Text repeat = Text.literal("x" + recipe.loops());
            graphics.drawText(textRenderer, repeat, xOffset + 76, 104, 0xff888888, false);
        }
    }

    @Override
    public void getTooltip(
        ITooltipBuilder tooltip,
        RecipeEntry<SequencedAssemblyRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        double mouseX,
        double mouseY
    ) {
        SequencedAssemblyRecipe recipe = entry.value();
        if (recipe.loops() > 1 && mouseX >= 43 && mouseX < 108 && mouseY >= 92 && mouseY < 116) {
            tooltip.add(CreateLang.translateDirect("recipe.assembly.repeat", recipe.loops()));
            return;
        }
        if (mouseY < 5 || mouseY > 84) {
            return;
        }
        List<Recipe<?>> recipes = recipe.sequence();
        int size = recipes.size() / recipe.loops();
        for (int i = 0, left = 88 - 14 * size; i < size; i++) {
            int x = left + i * 28;
            if (x <= mouseX && x + 28 > mouseX) {
                onRichTooltip(tooltip, recipes.get(i), recipeSlotsView, i);
                break;
            }
        }
    }

    private static <T extends Recipe<?>> void onRichTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView recipeSlotsView, int i) {
        tooltip.add(SequenceTooltip.getStep(i));
        SequencedRenderer<T> renderer = getRenderer(recipe);
        if (renderer == null) {
            return;
        }
        tooltip.add(SequenceTooltip.getSequenceName(renderer, recipe, recipeSlotsView.findSlotByName(String.valueOf(i))));
    }

    public interface SequencedRenderer<T extends Recipe<?>> {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        void render(DrawContext graphics, int i, int x, int y, Optional<IRecipeSlotView> slot);

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        default Text getSequenceName(T recipe, Optional<IRecipeSlotView> slot) {
            Identifier id = Registries.RECIPE_TYPE.getId(recipe.getType());
            if (id != null) {
                String namespace = id.getNamespace();
                String recipeName;
                if (namespace.equals("create")) {
                    recipeName = id.getPath();
                } else {
                    recipeName = id.getNamespace() + "." + id.getPath();
                }
                return Text.translatable("create.recipe.assembly." + recipeName);
            }
            return ScreenTexts.EMPTY;
        }

        default IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, T recipe) {
            return null;
        }
    }

    public static class PressingRenderer implements SequencedRenderer<PressingRecipe> {
        @Override
        public void render(DrawContext graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
            float scale = 19 / 30f;
            Matrix3x2fStack matrices = graphics.getMatrices();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            matrices.translate(-x, -y);
            graphics.state.addSpecialElement(new PressRenderState(i, new Matrix3x2f(matrices), x - 3, y + 18, i));
            matrices.popMatrix();
        }
    }

    public static class DeployingRenderer implements SequencedRenderer<DeployerApplicationRecipe> {
        @Override
        public void render(DrawContext graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
            float scale = 59 / 78f;
            Matrix3x2fStack matrices = graphics.getMatrices();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            matrices.translate(-x, -y);
            graphics.state.addSpecialElement(new DeployerRenderState(i, new Matrix3x2f(matrices), x - 3, y + 18, i));
            matrices.popMatrix();
        }

        @Override
        public Text getSequenceName(DeployerApplicationRecipe recipe, Optional<IRecipeSlotView> slot) {
            Text name = slot.flatMap(IRecipeSlotView::getDisplayedItemStack).map(ItemStack::getName).orElse(ScreenTexts.EMPTY);
            return Text.translatable("create.recipe.assembly.deploying_item", name);
        }

        @Override
        public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, DeployerApplicationRecipe recipe) {
            return builder.addInputSlot(x, y).setBackground(EMPTY, 0, 0).add(recipe.ingredient());
        }
    }

    public static class FillingRenderer implements SequencedRenderer<FillingRecipe> {
        @Override
        public void render(DrawContext graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
            slot.flatMap(s -> s.getDisplayedIngredient(FabricTypes.FLUID_STACK)).ifPresent(ingredient -> {
                float scale = 35 / 46f;
                Matrix3x2fStack matrices = graphics.getMatrices();
                matrices.pushMatrix();
                matrices.translate(x, y);
                matrices.scale(scale, scale);
                matrices.translate(-x, -y);
                FluidVariant fluidVariant = ingredient.getFluidVariant();
                Fluid fluid = fluidVariant.getFluid();
                ComponentChanges components = fluidVariant.getComponents();
                graphics.state.addSpecialElement(new SpoutRenderState(i, new Matrix3x2f(matrices), fluid, components, x - 2, y + 24, i));
                matrices.popMatrix();
            });
        }

        @Override
        public Text getSequenceName(FillingRecipe recipe, Optional<IRecipeSlotView> slot) {
            Text name = slot.flatMap(s -> s.getDisplayedIngredient(FabricTypes.FLUID_STACK)).map(ingredient -> {
                FluidVariant fluidVariant = ingredient.getFluidVariant();
                Fluid fluid = fluidVariant.getFluid();
                if (fluid == AllFluids.POTION) {
                    ComponentMap components = fluidVariant.getComponentMap();
                    PotionContentsComponent contents = components.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                    BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                    ItemConvertible itemFromBottleType = PotionFluidHandler.itemFromBottleType(bottleType);
                    return contents.getName(itemFromBottleType.asItem().getTranslationKey() + ".effect.");
                }
                Block block = fluid.getDefaultState().getBlockState().getBlock();
                if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
                    return Text.translatable(Util.createTranslationKey("block", Registries.FLUID.getId(fluid)));
                }
                return block.getName();
            }).orElse(ScreenTexts.EMPTY);
            return Text.translatable("create.recipe.assembly.spout_filling_fluid", name);
        }

        @Override
        public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, FillingRecipe recipe) {
            return addFluidSlot(builder, x, y, recipe.fluidIngredient());
        }
    }

    public record SequenceTooltip<T extends Recipe<?>>(SequencedRenderer<T> renderer, T recipe, int i) implements IRecipeSlotRichTooltipCallback {
        public static Text getStep(int i) {
            return CreateLang.translateDirect("recipe.assembly.step", i + 1);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public static <T extends Recipe<?>> Text getSequenceName(SequencedRenderer<T> renderer, T recipe, Optional<IRecipeSlotView> slot) {
            return renderer.getSequenceName(recipe, slot).copy().formatted(Formatting.DARK_GREEN);
        }

        @Override
        public void onRichTooltip(IRecipeSlotView slot, ITooltipBuilder tooltip) {
            List<Either<StringVisitable, TooltipData>> lines = tooltip.getLines();
            if (!lines.isEmpty()) {
                lines.removeFirst();
            }
            lines.addAll(0, List.of(Either.left(getStep(i)), Either.left(getSequenceName(renderer, recipe, Optional.of(slot)))));
        }
    }
}
