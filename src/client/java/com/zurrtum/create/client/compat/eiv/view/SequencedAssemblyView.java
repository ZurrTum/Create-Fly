package com.zurrtum.create.client.compat.eiv.view;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.AllAssemblyRecipeNames;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.client.foundation.gui.render.SpoutRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.display.SequencedAssemblyDisplay;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.infrastructure.component.BottleType;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.AdditionalStackModifier;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import de.crafty.eiv.common.recipe.item.FluidItem;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.*;

public class SequencedAssemblyView extends CreateView {
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

    private final SlotContent result;
    private final List<SlotContent> ingredients;
    private final List<Recipe<?>> sequence;
    private final IntSet empty;
    private final float chance;
    private final int loops;

    public SequencedAssemblyView(SequencedAssemblyDisplay display) {
        ProcessingOutput output = display.result;
        result = SlotContent.of(output.create());
        chance = output.chance();
        sequence = display.sequence;
        int size = sequence.size();
        ingredients = new ArrayList<>(size + 1);
        empty = new IntOpenHashSet();
        for (int i = 0; i < size; i++) {
            addSlot(sequence.get(i), i);
        }
        ingredients.add(SlotContent.of(display.ingredient));
        loops = display.loops;
    }

    private <T extends Recipe<?>> void addSlot(T sequence, int i) {
        SequencedRenderer<T> renderer = getRenderer(sequence);
        if (renderer != null) {
            SlotContent slot = renderer.createSlot(sequence);
            if (slot != null) {
                ingredients.add(slot);
                return;
            }
        }
        empty.add(i);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.SEQUENCED_ASSEMBLY;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return ingredients;
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    protected int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        for (int j = 0, size = sequence.size(), left = 91 - 14 * size; j < size; j++) {
            if (empty.contains(j)) {
                continue;
            }
            slotDefinition.addItemSlot(i++, left + j * 28, 17);
        }
        if (chance == 1) {
            slotDefinition.addItemSlot(i++, 22, 93);
            slotDefinition.addItemSlot(i++, 127, 93);
        } else {
            slotDefinition.addItemSlot(i++, 15, 93);
            slotDefinition.addItemSlot(i++, 120, 93);
        }
        return i;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        Iterator<SlotContent> iterator = ingredients.iterator();
        for (int j = 0, size = sequence.size(); j < size; j++) {
            if (empty.contains(j)) {
                continue;
            }
            slotFillContext.bindOptionalSlot(i, iterator.next(), SLOT);
            slotFillContext.addAdditionalStackModifier(i, new SequenceTooltip<>(sequence.get(j), ++i));
        }
        slotFillContext.bindOptionalSlot(i++, iterator.next(), SLOT);
        if (chance == 1) {
            slotFillContext.bindOptionalSlot(i++, result, SLOT);
        } else {
            bindChanceSlot(slotFillContext, i++, result, chance);
        }
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        boolean checkHover = screen.focusedSlot == null;
        boolean checkStep = mouseY >= 7 && mouseY <= 86;
        TextRenderer textRenderer = screen.getTextRenderer();
        Iterator<SlotContent> iterator = ingredients.iterator();
        for (int i = 0, size = sequence.size(), left = 91 - 14 * size; i < size; i++) {
            int x = left + i * 28;
            String n = ROMANS[Math.min(i, ROMANS.length)];
            context.drawText(textRenderer, n, x + 8 - textRenderer.getWidth(n) / 2, 4, 0xff888888, false);
            Recipe<?> recipe = sequence.get(i);
            ItemStack stack;
            if (empty.contains(i)) {
                stack = null;
            } else {
                SlotContent slot = iterator.next();
                stack = slot.getByIndex(slot.index());
            }
            SequencedRenderer<?> draw = getRenderer(recipe);
            if (draw != null) {
                draw.render(context, i, x, 17, stack);
            } else {
                AllGuiTextures.JEI_CHANCE_SLOT.render(context, x - 1, 16);
                Text text = Text.literal("?").formatted(Formatting.BOLD);
                context.drawText(textRenderer, text, x + textRenderer.getWidth(text) / -2 + 7, 21, 0xffefefef, true);
            }
            if (checkHover && checkStep && mouseX > x - 7 && mouseX < x + 22) {
                checkHover = false;
                Text text = draw != null ? SequencedRenderer.getSequenceName(draw, recipe, stack) : SequencedRenderer.getSequenceName(recipe);
                List<Text> tooltip = List.of(CreateLang.translateDirect("recipe.assembly.step", i + 1), text.copy().formatted(Formatting.DARK_GREEN));
                context.drawTooltip(textRenderer, tooltip, mouseX + position.left(), mouseY + position.top());
            }
        }
        int xOffset = 0;
        if (chance != 1) {
            xOffset = -7;
            AllGuiTextures.JEI_CHANCE_SLOT.render(context, 138, 92);
            Text text = Text.literal("?").formatted(Formatting.BOLD);
            context.drawText(textRenderer, text, 146 + textRenderer.getWidth(text) / -2, 97, 0xffefefef, true);
            if (checkHover && mouseX >= 138 && mouseX <= 155 && mouseY >= 92 && mouseY <= 109) {
                checkHover = false;
                context.fill(139, 93, 155, 109, 0x80FFFFFF);
                float junk = 1 - chance;
                String number = junk < 0.01 ? "<1" : junk > 0.99 ? ">99" : String.valueOf(Math.round(junk * 100));
                List<Text> tooltip = List.of(
                    CreateLang.translateDirect("recipe.assembly.junk"),
                    CreateLang.translateDirect("recipe.processing.chance", number).formatted(Formatting.GOLD)
                );
                context.drawTooltip(textRenderer, tooltip, mouseX + position.left(), mouseY + position.top());
            }
        }
        AllGuiTextures.JEI_LONG_ARROW.render(context, xOffset + 47, 96);
        if (loops > 1) {
            AllIcons.I_SEQ_REPEAT.render(context, xOffset + 60, 101);
            context.drawText(textRenderer, Text.literal("x" + loops), xOffset + 76, 106, 0xff888888, false);
            if (checkHover && mouseX >= 43 && mouseX < 108 && mouseY >= 94 && mouseY < 118) {
                Text text = CreateLang.translateDirect("recipe.assembly.repeat", loops);
                context.drawTooltip(textRenderer, text, mouseX + position.left(), mouseY + position.top());
            }
        }
    }

    public interface SequencedRenderer<T extends Recipe<?>> {
        Map<Recipe<?>, Text> NAMES = new WeakHashMap<>();

        void render(DrawContext graphics, int i, int x, int y, @Nullable ItemStack stack);

        static Text getSequenceName(Recipe<?> recipe) {
            Text name = NAMES.get(recipe);
            if (name != null) {
                return name;
            }
            Identifier id = Registries.RECIPE_TYPE.getId(recipe.getType());
            if (id == null) {
                name = ScreenTexts.EMPTY;
            } else {
                RegistryOps<JsonElement> ops = MinecraftClient.getInstance().world.getRegistryManager().getOps(JsonOps.INSTANCE);
                name = Recipe.CODEC.encodeStart(ops, recipe).result().map(json -> AllAssemblyRecipeNames.get(ops, json))
                    .orElse(ScreenTexts.EMPTY);
            }
            NAMES.put(recipe, name);
            return name;
        }

        @SuppressWarnings("unchecked")
        static <T extends Recipe<?>> Text getSequenceName(SequencedRenderer<?> render, T recipe, ItemStack stack) {
            return ((SequencedRenderer<T>) render).getSequenceName(recipe, stack);
        }

        default Text getSequenceName(T recipe, ItemStack stack) {
            return getSequenceName(recipe);
        }

        @Nullable
        default SlotContent createSlot(T recipe) {
            return null;
        }
    }

    public static class PressingRenderer implements SequencedRenderer<PressingRecipe> {
        @Override
        public void render(DrawContext graphics, int i, int x, int y, @Nullable ItemStack stack) {
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
        public void render(DrawContext graphics, int i, int x, int y, @Nullable ItemStack stack) {
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
        public Text getSequenceName(DeployerApplicationRecipe recipe, ItemStack stack) {
            return Text.translatable("create.recipe.assembly.deploying_item", stack.getName());
        }

        @Override
        public SlotContent createSlot(DeployerApplicationRecipe recipe) {
            return SlotContent.of(recipe.ingredient());
        }
    }

    public static class FillingRenderer implements SequencedRenderer<FillingRecipe> {
        @Override
        public void render(DrawContext graphics, int i, int x, int y, @Nullable ItemStack stack) {
            if (stack != null && stack.getItem() instanceof FluidItem item) {
                float scale = 35 / 46f;
                Matrix3x2fStack matrices = graphics.getMatrices();
                matrices.pushMatrix();
                matrices.translate(x, y);
                matrices.scale(scale, scale);
                matrices.translate(-x, -y);
                Fluid fluid = item.getFluid();
                ComponentChanges components = stack.getComponentChanges();
                graphics.state.addSpecialElement(new SpoutRenderState(i, new Matrix3x2f(matrices), fluid, components, x - 2, y + 24, i));
                matrices.popMatrix();
            }
        }

        @Override
        public Text getSequenceName(FillingRecipe recipe, ItemStack stack) {
            Text name;
            if (stack.getItem() instanceof FluidItem item) {
                Fluid fluid = item.getFluid();
                if (fluid == AllFluids.POTION) {
                    PotionContentsComponent contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                    BottleType bottleType = stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                    ItemConvertible itemFromBottleType = PotionFluidHandler.itemFromBottleType(bottleType);
                    return contents.getName(itemFromBottleType.asItem().getTranslationKey() + ".effect.");
                }
                Block block = fluid.getDefaultState().getBlockState().getBlock();
                if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
                    return Text.translatable(Util.createTranslationKey("block", Registries.FLUID.getId(fluid)));
                }
                name = block.getName();
            } else {
                name = ScreenTexts.EMPTY;
            }
            return Text.translatable("create.recipe.assembly.spout_filling_fluid", name);
        }

        @Override
        public SlotContent createSlot(FillingRecipe recipe) {
            return SlotContent.of(getItemStacks(recipe.fluidIngredient()));
        }
    }

    public record SequenceTooltip<T extends Recipe<?>>(T recipe, int i) implements AdditionalStackModifier {
        @Override
        public void addTooltip(ItemStack stack, List<Text> list) {
            SequencedRenderer<T> renderer = getRenderer(recipe);
            Text text;
            if (renderer == null) {
                text = SequencedRenderer.getSequenceName(recipe);
            } else {
                text = renderer.getSequenceName(recipe, stack);
            }
            text = text.copy().formatted(Formatting.DARK_GREEN);
            if (list.isEmpty()) {
                list.add(text);
            } else {
                list.set(0, text);
            }
            list.addFirst(CreateLang.translateDirect("recipe.assembly.step", i));
        }
    }
}
