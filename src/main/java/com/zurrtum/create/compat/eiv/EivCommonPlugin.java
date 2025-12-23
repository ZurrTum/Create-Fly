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
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;

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
        return EivRecipeType.register(Identifier.fromNamespaceAndPath(MOD_ID, id), factory);
    }

    @Override
    public void onIntegrationInitialize() {
        ItemView.addRecipeProvider(EivCommonPlugin::register);
    }

    private static void register(List<IEivServerRecipe> recipes) {
        RecipeMap preparedRecipes = ServerRecipeManager.INSTANCE.getVanillaRecipeManager().recipes;
        preparedRecipes.byType(AllRecipeTypes.COMPACTING).stream().map(CompactingDisplay::new).forEach(recipes::add);
        Collection<RecipeHolder<CraftingRecipe>> craftingRecipes = preparedRecipes.byType(RecipeType.CRAFTING);
        craftingRecipes.stream().map(AutoCompactingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        craftingRecipes.stream().map(AutoMixingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.PRESSING).stream().map(PressingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.MIXING).stream().map(MixingDisplay::new).forEach(recipes::add);
        Collection<RecipeHolder<MillingRecipe>> millingRecipes = preparedRecipes.byType(AllRecipeTypes.MILLING);
        millingRecipes.stream().map(MilingDisplay::new).forEach(recipes::add);
        millingRecipes.stream().map(CrushingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.CUTTING).stream().map(SawingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.CRUSHING).stream().map(CrushingDisplay::new).forEach(recipes::add);
        Collection<RecipeHolder<ManualApplicationRecipe>> manualApplicationRecipes = preparedRecipes.byType(AllRecipeTypes.ITEM_APPLICATION);
        manualApplicationRecipes.stream().map(ManualApplicationDisplay::new).forEach(recipes::add);
        manualApplicationRecipes.stream().map(DeployingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.DEPLOYING).stream().map(DeployingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        Collection<RecipeHolder<SandPaperPolishingRecipe>> sandPaperPolishingRecipes = preparedRecipes.byType(AllRecipeTypes.SANDPAPER_POLISHING);
        sandPaperPolishingRecipes.stream().map(DeployingDisplay::of).filter(Objects::nonNull).forEach(recipes::add);
        sandPaperPolishingRecipes.stream().map(SandPaperPolishingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.SEQUENCED_ASSEMBLY).stream().map(SequencedAssemblyDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.EMPTYING).stream().map(DrainingDisplay::new).forEach(recipes::add);
        DrainingDisplay.registerGenericItem(recipes);
        preparedRecipes.byType(AllRecipeTypes.MECHANICAL_CRAFTING).stream().map(MechanicalCraftingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.FILLING).stream().map(SpoutFillingDisplay::new).forEach(recipes::add);
        SpoutFillingDisplay.registerGenericItem(recipes);
        RegistryAccess registryManager = ServerRecipeManager.INSTANCE.getServer().registryAccess();
        Collection<RecipeHolder<SmokingRecipe>> smokingRecipes = preparedRecipes.byType(RecipeType.SMOKING);
        Collection<RecipeHolder<SmeltingRecipe>> smeltingRecipes = preparedRecipes.byType(RecipeType.SMELTING);
        smeltingRecipes.stream().map(entry -> FanBlastingDisplay.of(entry, registryManager, null, smokingRecipes)).filter(Objects::nonNull)
            .forEach(recipes::add);
        preparedRecipes.byType(RecipeType.BLASTING).stream()
            .map(entry -> FanBlastingDisplay.of(entry, registryManager, smeltingRecipes, smokingRecipes)).filter(Objects::nonNull)
            .forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.HAUNTING).stream().map(FanHauntingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(RecipeType.SMOKING).stream().map(FanSmokingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.SPLASHING).stream().map(FanWashingDisplay::new).forEach(recipes::add);
        preparedRecipes.byType(AllRecipeTypes.POTION).stream().map(PotionDisplay::new).forEach(recipes::add);
        MysteriousItemConversionDisplay.register(recipes);
        BlockCuttingDisplay.register(recipes, preparedRecipes);
        registerToolboxRecipes(recipes, registryManager);
    }

    public static void registerToolboxRecipes(List<IEivServerRecipe> recipes, RegistryAccess registryManager) {
        HolderSet.Named<Item> entries = registryManager.lookupOrThrow(Registries.ITEM).getOrThrow(AllItemTags.TOOLBOXES);
        Ingredient ingredient = Ingredient.of(entries);
        for (DyeColor color : DyeColor.values()) {
            recipes.add(new ShapelessServerRecipe(
                List.of(Ingredient.of(DyeItem.byColor(color)), ingredient),
                ToolboxBlock.getColorBlock(color).asItem().getDefaultInstance()
            ));
        }
    }
}
