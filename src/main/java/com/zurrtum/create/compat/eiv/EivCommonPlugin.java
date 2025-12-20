package com.zurrtum.create.compat.eiv;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.display.*;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import de.crafty.eiv.common.api.IExtendedItemViewIntegration;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.EivRecipeType.EmptyRecipeConstructor;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.api.recipe.ItemView;
import de.crafty.eiv.common.builtin.shapeless.ShapelessServerRecipe;
import de.crafty.eiv.common.recipe.ServerRecipeManager;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.zurrtum.create.Create.MOD_ID;

public class EivCommonPlugin implements IExtendedItemViewIntegration {
    public static final EivRecipeType<AutoCompactingDisplay> AUTOMATIC_PACKING = register("automatic_packing", AutoCompactingDisplay::new);
    public static final EivRecipeType<CompactingDisplay> PACKING = register("packing", CompactingDisplay::new);
    public static final EivRecipeType<PressingDisplay> PRESSING = register("pressing", PressingDisplay::new);
    public static final EivRecipeType<AutoMixingDisplay> AUTOMATIC_SHAPELESS = register("automatic_shapeless", AutoMixingDisplay::new);
    public static final EivRecipeType<MixingDisplay> MIXING = register("mixing", MixingDisplay::new);
    public static final EivRecipeType<MilingDisplay> MILLING = register("milling", MilingDisplay::new);
    public static final EivRecipeType<SawingDisplay> SAWING = register("sawing", SawingDisplay::new);
    public static final EivRecipeType<CrushingDisplay> CRUSHING = register("crushing", CrushingDisplay::new);
    public static final EivRecipeType<MysteriousItemConversionDisplay> MYSTERY_CONVERSION = register(
        "mystery_conversion",
        MysteriousItemConversionDisplay::new
    );
    public static final EivRecipeType<ManualApplicationDisplay> ITEM_APPLICATION = register("item_application", ManualApplicationDisplay::new);
    public static final EivRecipeType<DeployingDisplay> DEPLOYING = register("deploying", DeployingDisplay::new);
    public static final EivRecipeType<DrainingDisplay> DRAINING = register("draining", DrainingDisplay::new);
    public static final EivRecipeType<MechanicalCraftingDisplay> MECHANICAL_CRAFTING = register(
        "mechanical_crafting",
        MechanicalCraftingDisplay::new
    );
    public static final EivRecipeType<SpoutFillingDisplay> SPOUT_FILLING = register("spout_filling", SpoutFillingDisplay::new);
    public static final EivRecipeType<SandPaperPolishingDisplay> SANDPAPER_POLISHING = register(
        "sandpaper_polishing",
        SandPaperPolishingDisplay::new
    );
    public static final EivRecipeType<SequencedAssemblyDisplay> SEQUENCED_ASSEMBLY = register("sequenced_assembly", SequencedAssemblyDisplay::new);
    public static final EivRecipeType<FanBlastingDisplay> FAN_BLASTING = register("fan_blasting", FanBlastingDisplay::new);
    public static final EivRecipeType<FanHauntingDisplay> FAN_HAUNTING = register("fan_haunting", FanHauntingDisplay::new);
    public static final EivRecipeType<FanSmokingDisplay> FAN_SMOKING = register("fan_smoking", FanSmokingDisplay::new);
    public static final EivRecipeType<FanWashingDisplay> FAN_WASHING = register("fan_washing", FanWashingDisplay::new);
    public static final EivRecipeType<PotionDisplay> AUTOMATIC_BREWING = register("automatic_brewing", PotionDisplay::new);
    public static final EivRecipeType<BlockCuttingDisplay> BLOCK_CUTTING = register("block_cutting", BlockCuttingDisplay::new);

    private static <T extends IEivServerRecipe> EivRecipeType<T> register(String id, EmptyRecipeConstructor<T> factory) {
        return EivRecipeType.register(Identifier.of(MOD_ID, id), factory);
    }

    @Override
    public void onIntegrationInitialize() {
        ItemView.addRecipeProvider(EivCommonPlugin::register);
    }

    private static void register(List<IEivServerRecipe> recipes) {
        PreparedRecipes preparedRecipes = ServerRecipeManager.INSTANCE.getVanillaRecipeManager().preparedRecipes;
        preparedRecipes.getAll(AllRecipeTypes.COMPACTING).stream().map(CompactingDisplay::new).forEach(recipes::add);
        Collection<RecipeEntry<CraftingRecipe>> craftingRecipes = preparedRecipes.getAll(RecipeType.CRAFTING);
        craftingRecipes.stream().map(AutoCompactingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        craftingRecipes.stream().map(AutoMixingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.PRESSING).stream().map(PressingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.MIXING).stream().map(MixingDisplay::new).forEach(recipes::add);
        Collection<RecipeEntry<MillingRecipe>> millingRecipes = preparedRecipes.getAll(AllRecipeTypes.MILLING);
        millingRecipes.stream().map(MilingDisplay::new).forEach(recipes::add);
        millingRecipes.stream().map(CrushingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.CUTTING).stream().map(SawingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.CRUSHING).stream().map(CrushingDisplay::new).forEach(recipes::add);
        Collection<RecipeEntry<ManualApplicationRecipe>> manualApplicationRecipes = preparedRecipes.getAll(AllRecipeTypes.ITEM_APPLICATION);
        manualApplicationRecipes.stream().map(ManualApplicationDisplay::new).forEach(recipes::add);
        manualApplicationRecipes.stream().map(DeployingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.DEPLOYING).stream().map(DeployingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        Collection<RecipeEntry<SandPaperPolishingRecipe>> sandPaperPolishingRecipes = preparedRecipes.getAll(AllRecipeTypes.SANDPAPER_POLISHING);
        sandPaperPolishingRecipes.stream().map(DeployingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        sandPaperPolishingRecipes.stream().map(SandPaperPolishingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.SEQUENCED_ASSEMBLY).stream().map(SequencedAssemblyDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.EMPTYING).stream().map(DrainingDisplay::new).forEach(recipes::add);
        DrainingDisplay.registerGenericItem(recipes);
        preparedRecipes.getAll(AllRecipeTypes.MECHANICAL_CRAFTING).stream().map(MechanicalCraftingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.FILLING).stream().map(SpoutFillingDisplay::new).forEach(recipes::add);
        SpoutFillingDisplay.registerGenericItem(recipes);
        DynamicRegistryManager registryManager = ServerRecipeManager.INSTANCE.getServer().getRegistryManager();
        Collection<RecipeEntry<SmokingRecipe>> smokingRecipes = preparedRecipes.getAll(RecipeType.SMOKING);
        Collection<RecipeEntry<SmeltingRecipe>> smeltingRecipes = preparedRecipes.getAll(RecipeType.SMELTING);
        smeltingRecipes.stream().map(entry -> FanBlastingDisplay.of(entry, registryManager, null, smokingRecipes)).filter(Objects::nonNull)
            .forEach(recipes::add);
        preparedRecipes.getAll(RecipeType.BLASTING).stream()
            .map(entry -> FanBlastingDisplay.of(entry, registryManager, smeltingRecipes, smokingRecipes)).filter(Objects::nonNull)
            .forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.HAUNTING).stream().map(FanHauntingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(RecipeType.SMOKING).stream().map(FanSmokingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.SPLASHING).stream().map(FanWashingDisplay::new).forEach(recipes::add);
        preparedRecipes.getAll(AllRecipeTypes.POTION).stream().map(PotionDisplay::new).forEach(recipes::add);
        MysteriousItemConversionDisplay.register(recipes);
        BlockCuttingDisplay.register(recipes, preparedRecipes);
        registerToolboxRecipes(recipes, registryManager);
    }

    public static void registerToolboxRecipes(List<IEivServerRecipe> recipes, DynamicRegistryManager registryManager) {
        RegistryEntryList.Named<Item> entries = registryManager.getOrThrow(RegistryKeys.ITEM).getOrThrow(AllItemTags.TOOLBOXES);
        Ingredient ingredient = Ingredient.ofTag(entries);
        for (DyeColor color : DyeColor.values()) {
            recipes.add(new ShapelessServerRecipe(
                List.of(Ingredient.ofItem(DyeItem.byColor(color)), ingredient),
                ToolboxBlock.getColorBlock(color).asItem().getDefaultStack()
            ));
        }
    }
}
