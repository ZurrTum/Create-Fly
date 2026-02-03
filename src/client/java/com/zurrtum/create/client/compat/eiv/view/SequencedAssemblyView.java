package com.zurrtum.create.client.compat.eiv.view;

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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
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
        result = SlotContent.of(output.stack());
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
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        boolean checkHover = screen.hoveredSlot == null;
        boolean checkStep = mouseY >= 7 && mouseY <= 86;
        Font textRenderer = screen.getFont();
        Iterator<SlotContent> iterator = ingredients.iterator();
        for (int i = 0, size = sequence.size(), left = 91 - 14 * size; i < size; i++) {
            int x = left + i * 28;
            String n = ROMANS[Math.min(i, ROMANS.length)];
            context.drawString(textRenderer, n, x + 8 - textRenderer.width(n) / 2, 4, 0xff888888, false);
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
                Component text = Component.literal("?").withStyle(ChatFormatting.BOLD);
                context.drawString(textRenderer, text, x + textRenderer.width(text) / -2 + 7, 21, 0xffefefef, true);
            }
            if (checkHover && checkStep && mouseX > x - 7 && mouseX < x + 22) {
                checkHover = false;
                Component text = draw != null ? SequencedRenderer.getSequenceName(draw, recipe, stack) : SequencedRenderer.getSequenceName(recipe);
                List<Component> tooltip = List.of(
                    CreateLang.translateDirect("recipe.assembly.step", i + 1),
                    text.copy().withStyle(ChatFormatting.DARK_GREEN)
                );
                context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX + position.left(), mouseY + position.top());
            }
        }
        int xOffset = 0;
        if (chance != 1) {
            xOffset = -7;
            AllGuiTextures.JEI_CHANCE_SLOT.render(context, 138, 92);
            Component text = Component.literal("?").withStyle(ChatFormatting.BOLD);
            context.drawString(textRenderer, text, 146 + textRenderer.width(text) / -2, 97, 0xffefefef, true);
            if (checkHover && mouseX >= 138 && mouseX <= 155 && mouseY >= 92 && mouseY <= 109) {
                checkHover = false;
                context.fill(139, 93, 155, 109, 0x80FFFFFF);
                float junk = 1 - chance;
                String number = junk < 0.01 ? "<1" : junk > 0.99 ? ">99" : String.valueOf(Math.round(junk * 100));
                List<Component> tooltip = List.of(
                    CreateLang.translateDirect("recipe.assembly.junk"),
                    CreateLang.translateDirect("recipe.processing.chance", number).withStyle(ChatFormatting.GOLD)
                );
                context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX + position.left(), mouseY + position.top());
            }
        }
        AllGuiTextures.JEI_LONG_ARROW.render(context, xOffset + 47, 96);
        if (loops > 1) {
            AllIcons.I_SEQ_REPEAT.render(context, xOffset + 60, 101);
            context.drawString(textRenderer, Component.literal("x" + loops), xOffset + 76, 106, 0xff888888, false);
            if (checkHover && mouseX >= 43 && mouseX < 108 && mouseY >= 94 && mouseY < 118) {
                Component text = CreateLang.translateDirect("recipe.assembly.repeat", loops);
                context.setTooltipForNextFrame(textRenderer, text, mouseX + position.left(), mouseY + position.top());
            }
        }
    }

    public interface SequencedRenderer<T extends Recipe<?>> {
        void render(GuiGraphics graphics, int i, int x, int y, @Nullable ItemStack stack);

        static Component getSequenceName(Recipe<?> recipe) {
            Identifier id = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
            if (id != null) {
                String namespace = id.getNamespace();
                String recipeName;
                if (namespace.equals("create")) {
                    recipeName = id.getPath();
                } else {
                    recipeName = id.getNamespace() + "." + id.getPath();
                }
                return Component.translatable("create.recipe.assembly." + recipeName);
            }
            return CommonComponents.EMPTY;
        }

        @SuppressWarnings("unchecked")
        static <T extends Recipe<?>> Component getSequenceName(SequencedRenderer<?> render, T recipe, ItemStack stack) {
            return ((SequencedRenderer<T>) render).getSequenceName(recipe, stack);
        }

        default Component getSequenceName(T recipe, ItemStack stack) {
            return getSequenceName(recipe);
        }

        @Nullable
        default SlotContent createSlot(T recipe) {
            return null;
        }
    }

    public static class PressingRenderer implements SequencedRenderer<PressingRecipe> {
        @Override
        public void render(GuiGraphics graphics, int i, int x, int y, @Nullable ItemStack stack) {
            float scale = 19 / 30f;
            Matrix3x2fStack matrices = graphics.pose();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            matrices.translate(-x, -y);
            graphics.guiRenderState.submitPicturesInPictureState(new PressRenderState(i, new Matrix3x2f(matrices), x - 3, y + 18, i));
            matrices.popMatrix();
        }
    }

    public static class DeployingRenderer implements SequencedRenderer<DeployerApplicationRecipe> {
        @Override
        public void render(GuiGraphics graphics, int i, int x, int y, @Nullable ItemStack stack) {
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
        public Component getSequenceName(DeployerApplicationRecipe recipe, ItemStack stack) {
            return Component.translatable("create.recipe.assembly.deploying_item", stack.getHoverName());
        }

        @Override
        public SlotContent createSlot(DeployerApplicationRecipe recipe) {
            return SlotContent.of(recipe.ingredient());
        }
    }

    public static class FillingRenderer implements SequencedRenderer<FillingRecipe> {
        @Override
        public void render(GuiGraphics graphics, int i, int x, int y, @Nullable ItemStack stack) {
            if (stack != null && stack.getItem() instanceof FluidItem item) {
                float scale = 35 / 46f;
                Matrix3x2fStack matrices = graphics.pose();
                matrices.pushMatrix();
                matrices.translate(x, y);
                matrices.scale(scale, scale);
                matrices.translate(-x, -y);
                Fluid fluid = item.getFluid();
                DataComponentPatch components = stack.getComponentsPatch();
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
            }
        }

        @Override
        public Component getSequenceName(FillingRecipe recipe, ItemStack stack) {
            Component name;
            if (stack.getItem() instanceof FluidItem item) {
                Fluid fluid = item.getFluid();
                if (fluid == AllFluids.POTION) {
                    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    BottleType bottleType = stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
                    ItemLike itemFromBottleType = PotionFluidHandler.itemFromBottleType(bottleType);
                    return contents.getName(itemFromBottleType.asItem().getDescriptionId() + ".effect.");
                }
                Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
                if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
                    return Component.translatable(Util.makeDescriptionId("block", BuiltInRegistries.FLUID.getKey(fluid)));
                }
                name = block.getName();
            } else {
                name = CommonComponents.EMPTY;
            }
            return Component.translatable("create.recipe.assembly.spout_filling_fluid", name);
        }

        @Override
        public SlotContent createSlot(FillingRecipe recipe) {
            return SlotContent.of(getItemStacks(recipe.fluidIngredient()));
        }
    }

    public record SequenceTooltip<T extends Recipe<?>>(T recipe, int i) implements AdditionalStackModifier {
        @Override
        public void addTooltip(ItemStack stack, List<Component> list) {
            SequencedRenderer<T> renderer = getRenderer(recipe);
            Component text;
            if (renderer == null) {
                text = SequencedRenderer.getSequenceName(recipe);
            } else {
                text = renderer.getSequenceName(recipe, stack);
            }
            text = text.copy().withStyle(ChatFormatting.DARK_GREEN);
            if (list.isEmpty()) {
                list.add(text);
            } else {
                list.set(0, text);
            }
            list.addFirst(CreateLang.translateDirect("recipe.assembly.step", i));
        }
    }
}
