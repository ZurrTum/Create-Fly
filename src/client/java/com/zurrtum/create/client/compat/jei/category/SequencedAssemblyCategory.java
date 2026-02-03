package com.zurrtum.create.client.compat.jei.category;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.*;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.compat.jei.widget.JunkChanceTooltip;
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
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.*;

public class SequencedAssemblyCategory extends CreateCategory<RecipeHolder<SequencedAssemblyRecipe>> {
    public static String[] ROMANS = {"I", "II", "III", "IV", "V", "VI", "-"};
    public static Map<RecipeType<?>, SequencedRenderer<?>> RENDER = new IdentityHashMap<>();

    static {
        registerRenderer(AllRecipeTypes.PRESSING, new PressingRenderer());
        registerRenderer(AllRecipeTypes.DEPLOYING, new DeployingRenderer());
        registerRenderer(AllRecipeTypes.FILLING, new FillingRenderer());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> SequencedRenderer<T> getRenderer(T recipe) {
        return (SequencedRenderer<T>) RENDER.getOrDefault(recipe.getType(), SequencedRenderer.DEFAULT);
    }

    public static <T extends Recipe<?>> void registerRenderer(RecipeType<T> type, SequencedRenderer<T> draw) {
        RENDER.put(type, draw);
    }

    public static List<RecipeHolder<SequencedAssemblyRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.SEQUENCED_ASSEMBLY).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<SequencedAssemblyRecipe>> getRecipeType() {
        return JeiClientPlugin.SEQUENCED_ASSEMBLY;
    }

    @Override
    @NotNull
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SequencedAssemblyRecipe> entry, IFocusGroup focuses) {
        SequencedAssemblyRecipe recipe = entry.value();
        ProcessingOutput chanceOutput = recipe.result();
        boolean randomOutput = chanceOutput.chance() != 1;
        int xOffset = randomOutput ? -7 : 0;
        builder.addInputSlot(xOffset + 22, 91).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addChanceSlot(builder, xOffset + 127, 91, chanceOutput);
        if (randomOutput) {
            addJunkSlot(builder, xOffset + 146, 91).addRichTooltipCallback(new JunkChanceTooltip(1 - chanceOutput.chance()));
        }
        List<Recipe<?>> recipes = recipe.sequence();
        int size = recipes.size() / recipe.loops();
        for (int i = 0, left = 94 - 14 * size; i < size; i++) {
            addSlot(builder, left + i * 28, recipes.get(i), i);
        }
    }

    private static <T extends Recipe<?>> void addSlot(IRecipeLayoutBuilder builder, int x, T sequence, int i) {
        SequencedRenderer<T> renderer = getRenderer(sequence);
        IRecipeSlotBuilder slot = renderer.addSlot(builder, x, 15, sequence);
        if (slot != null) {
            slot.addRichTooltipCallback(new SequenceTooltip<>(renderer, sequence, i)).setSlotName(String.valueOf(i));
        }
    }

    @Override
    public void draw(
        RecipeHolder<SequencedAssemblyRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        GuiGraphics graphics,
        double mouseX,
        double mouseY
    ) {
        SequencedAssemblyRecipe recipe = entry.value();
        ProcessingOutput chanceOutput = recipe.result();
        boolean randomOutput = chanceOutput.chance() != 1;
        int xOffset = randomOutput ? -7 : 0;
        List<Recipe<?>> recipes = recipe.sequence();
        int size = recipes.size() / recipe.loops();
        Font textRenderer = graphics.minecraft.font;
        for (int i = 0, left = 94 - 14 * size; i < size; i++) {
            int x = left + i * 28;
            String text = ROMANS[Math.min(i, ROMANS.length)];
            Optional<IRecipeSlotView> slot = recipeSlotsView.findSlotByName(String.valueOf(i));
            if (slot.isPresent()) {
                AllGuiTextures.JEI_SLOT.render(graphics, x - 1, 14);
            }
            graphics.drawString(textRenderer, text, x + 8 - textRenderer.width(text) / 2, 2, 0xff888888, false);
            getRenderer(recipes.get(i)).render(graphics, i, x, 15, slot);
        }
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, xOffset + 47, 94);
        if (recipe.loops() > 1) {
            AllIcons.I_SEQ_REPEAT.render(graphics, xOffset + 60, 99);
            Component repeat = Component.literal("x" + recipe.loops());
            graphics.drawString(textRenderer, repeat, xOffset + 76, 104, 0xff888888, false);
        }
    }

    @Override
    public void getTooltip(
        ITooltipBuilder tooltip,
        RecipeHolder<SequencedAssemblyRecipe> entry,
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
        tooltip.add(SequenceTooltip.getSequenceName(getRenderer(recipe), recipe, recipeSlotsView.findSlotByName(String.valueOf(i))));
    }

    public static class SequencedRenderer<T extends Recipe<?>> {
        public static final SequencedRenderer<Recipe<?>> DEFAULT = new SequencedRenderer<>();
        private static final Map<Recipe<?>, Component> NAMES = new WeakHashMap<>();

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public void render(GuiGraphics graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public Component getSequenceName(T recipe, Optional<IRecipeSlotView> slot) {
            Component name = NAMES.get(recipe);
            if (name != null) {
                return name;
            }
            Identifier id = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
            if (id == null) {
                name = CommonComponents.EMPTY;
            } else {
                RegistryOps<JsonElement> ops = Minecraft.getInstance().level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                name = Recipe.CODEC.encodeStart(ops, recipe).result().map(json -> AllAssemblyRecipeNames.get(ops, json))
                    .orElse(CommonComponents.EMPTY);
            }
            NAMES.put(recipe, name);
            return name;
        }

        public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, T recipe) {
            return addJunkSlot(builder, x, y);
        }
    }

    public static class PressingRenderer extends SequencedRenderer<PressingRecipe> {
        @Override
        public void render(GuiGraphics graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
            float scale = 19 / 30f;
            Matrix3x2fStack matrices = graphics.pose();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            matrices.translate(-x, -y);
            graphics.guiRenderState.submitPicturesInPictureState(new PressRenderState(i, new Matrix3x2f(matrices), x - 3, y + 18, i));
            matrices.popMatrix();
        }

        @Override
        public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, PressingRecipe recipe) {
            return null;
        }
    }

    public static class DeployingRenderer extends SequencedRenderer<DeployerApplicationRecipe> {
        @Override
        public void render(GuiGraphics graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
            float scale = 59 / 78f;
            Matrix3x2fStack matrices = graphics.pose();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            matrices.translate(-x, -y);
            graphics.guiRenderState.submitPicturesInPictureState(new DeployerRenderState(i, new Matrix3x2f(matrices), x - 3, y + 18, i));
            matrices.popMatrix();
        }

        @Override
        public Component getSequenceName(DeployerApplicationRecipe recipe, Optional<IRecipeSlotView> slot) {
            Component name = slot.flatMap(IRecipeSlotView::getDisplayedItemStack).map(ItemStack::getHoverName).orElse(CommonComponents.EMPTY);
            return Component.translatable("create.recipe.assembly.deploying_item", name);
        }

        @Override
        public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, DeployerApplicationRecipe recipe) {
            return builder.addInputSlot(x, y).setBackground(EMPTY, 0, 0).add(recipe.ingredient());
        }
    }

    public static class FillingRenderer extends SequencedRenderer<FillingRecipe> {
        @Override
        public void render(GuiGraphics graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
            slot.flatMap(s -> s.getDisplayedIngredient(FabricTypes.FLUID_STACK)).ifPresent(ingredient -> {
                float scale = 35 / 46f;
                Matrix3x2fStack matrices = graphics.pose();
                matrices.pushMatrix();
                matrices.translate(x, y);
                matrices.scale(scale, scale);
                matrices.translate(-x, -y);
                FluidVariant fluidVariant = ingredient.getFluidVariant();
                Fluid fluid = fluidVariant.getFluid();
                DataComponentPatch components = fluidVariant.getComponents();
                graphics.guiRenderState.submitPicturesInPictureState(new SpoutRenderState(
                    i,
                    new Matrix3x2f(matrices),
                    fluid,
                    components,
                    x - 2,
                    y + 24,
                    i
                ));
                matrices.popMatrix();
            });
        }

        @Override
        public Component getSequenceName(FillingRecipe recipe, Optional<IRecipeSlotView> slot) {
            Component name = slot.flatMap(s -> s.getDisplayedIngredient(FabricTypes.FLUID_STACK)).map(ingredient -> {
                FluidVariant fluidVariant = ingredient.getFluidVariant();
                Fluid fluid = fluidVariant.getFluid();
                if (fluid == AllFluids.POTION) {
                    DataComponentMap components = fluidVariant.getComponentMap();
                    PotionContents contents = components.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                    ItemLike itemFromBottleType = PotionFluidHandler.itemFromBottleType(bottleType);
                    return contents.getName(itemFromBottleType.asItem().getDescriptionId() + ".effect.");
                }
                Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
                if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
                    return Component.translatable(Util.makeDescriptionId("block", BuiltInRegistries.FLUID.getKey(fluid)));
                }
                return block.getName();
            }).orElse(CommonComponents.EMPTY);
            return Component.translatable("create.recipe.assembly.spout_filling_fluid", name);
        }

        @Override
        public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, FillingRecipe recipe) {
            return addFluidSlot(builder, x, y, recipe.fluidIngredient());
        }
    }

    public record SequenceTooltip<T extends Recipe<?>>(SequencedRenderer<T> renderer, T recipe, int i) implements IRecipeSlotRichTooltipCallback {
        public static Component getStep(int i) {
            return CreateLang.translateDirect("recipe.assembly.step", i + 1);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public static <T extends Recipe<?>> Component getSequenceName(SequencedRenderer<T> renderer, T recipe, Optional<IRecipeSlotView> slot) {
            return renderer.getSequenceName(recipe, slot).copy().withStyle(ChatFormatting.DARK_GREEN);
        }

        @Override
        public void onRichTooltip(IRecipeSlotView slot, ITooltipBuilder tooltip) {
            List<Either<FormattedText, TooltipComponent>> lines = tooltip.getLines();
            if (!lines.isEmpty()) {
                lines.removeFirst();
            }
            lines.addAll(0, List.of(Either.left(getStep(i)), Either.left(getSequenceName(renderer, recipe, Optional.of(slot)))));
        }
    }
}
