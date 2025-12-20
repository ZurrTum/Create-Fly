package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.compat.jei.category.*;
import com.zurrtum.create.client.compat.jei.display.BlockCuttingDisplay;
import com.zurrtum.create.client.compat.jei.display.MysteriousItemConversionDisplay;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintScreen;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.zurrtum.create.client.content.logistics.filter.AbstractFilterScreen;
import com.zurrtum.create.client.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerScreen;
import com.zurrtum.create.client.content.trains.schedule.ScheduleScreen;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.zurrtum.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

@JeiPlugin
public class JeiClientPlugin implements IModPlugin {
    public static final Identifier ID = Create.asResource("jei_plugin");
    public static final IRecipeType<RecipeEntry<CraftingRecipe>> AUTOMATIC_PACKING = createRecipeHolderType("automatic_packing");
    public static final IRecipeType<RecipeEntry<CompactingRecipe>> PACKING = createRecipeHolderType("packing");
    public static final IRecipeType<RecipeEntry<PressingRecipe>> PRESSING = createRecipeHolderType("pressing");
    public static final IRecipeType<RecipeEntry<ShapelessRecipe>> AUTOMATIC_SHAPELESS = createRecipeHolderType("automatic_shapeless");
    public static final IRecipeType<RecipeEntry<MixingRecipe>> MIXING = createRecipeHolderType("mixing");
    public static final IRecipeType<RecipeEntry<MillingRecipe>> MILLING = createRecipeHolderType("milling");
    public static final IRecipeType<RecipeEntry<CuttingRecipe>> SAWING = createRecipeHolderType("sawing");
    public static final IRecipeType<RecipeEntry<? extends AbstractCrushingRecipe>> CRUSHING = createRecipeHolderType("crushing");
    public static final IRecipeType<RecipeEntry<ManualApplicationRecipe>> ITEM_APPLICATION = createRecipeHolderType("item_application");
    public static final IRecipeType<RecipeEntry<? extends ItemApplicationRecipe>> DEPLOYING = createRecipeHolderType("deploying");
    public static final IRecipeType<RecipeEntry<EmptyingRecipe>> DRAINING = createRecipeHolderType("draining");
    public static final IRecipeType<RecipeEntry<MechanicalCraftingRecipe>> MECHANICAL_CRAFTING = createRecipeHolderType("mechanical_crafting");
    public static final IRecipeType<RecipeEntry<FillingRecipe>> SPOUT_FILLING = createRecipeHolderType("spout_filling");
    public static final IRecipeType<RecipeEntry<SandPaperPolishingRecipe>> SANDPAPER_POLISHING = createRecipeHolderType("sandpaper_polishing");
    public static final IRecipeType<RecipeEntry<SequencedAssemblyRecipe>> SEQUENCED_ASSEMBLY = createRecipeHolderType("sequenced_assembly");
    public static final IRecipeType<RecipeEntry<? extends SingleStackRecipe>> FAN_BLASTING = createRecipeHolderType("fan_blasting");
    public static final IRecipeType<RecipeEntry<HauntingRecipe>> FAN_HAUNTING = createRecipeHolderType("fan_haunting");
    public static final IRecipeType<RecipeEntry<SmokingRecipe>> FAN_SMOKING = createRecipeHolderType("fan_smoking");
    public static final IRecipeType<RecipeEntry<SplashingRecipe>> FAN_WASHING = createRecipeHolderType("fan_washing");
    public static final IRecipeType<RecipeEntry<PotionRecipe>> AUTOMATIC_BREWING = createRecipeHolderType("automatic_brewing");
    public static final IRecipeType<MysteriousItemConversionDisplay> MYSTERY_CONVERSION = IRecipeType.create(
        MOD_ID,
        "mystery_conversion",
        MysteriousItemConversionDisplay.class
    );
    public static final IRecipeType<BlockCuttingDisplay> BLOCK_CUTTING = IRecipeType.create(MOD_ID, "block_cutting", BlockCuttingDisplay.class);

    @SuppressWarnings("unchecked")
    public static <T> IRecipeType<T> createRecipeHolderType(String path) {
        Identifier uid = Identifier.of(MOD_ID, path);
        return (IRecipeType<T>) IRecipeType.create(uid, RecipeEntry.class);
    }

    @Override
    @NotNull
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new AutoCompactingCategory(),
            new CompactingCategory(),
            new PressingCategory(),
            new AutoMixingCategory(),
            new MixingCategory(),
            new MillingCategory(),
            new SawingCategory(),
            new CrushingCategory(),
            new MysteriousItemConversionCategory(),
            new ManualApplicationCategory(),
            new DeployingCategory(),
            new DrainingCategory(),
            new MechanicalCraftingCategory(),
            new SpoutFillingCategory(),
            new SandpaperPolishingCategory(),
            new SequencedAssemblyCategory(),
            new FanBlastingCategory(),
            new FanHauntingCategory(),
            new FanSmokingCategory(),
            new FanWashingCategory(),
            new PotionCategory(),
            new BlockCuttingCategory()
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(AUTOMATIC_PACKING, AllItems.MECHANICAL_PRESS, AllItems.BASIN);
        registration.addCraftingStation(PACKING, AllItems.MECHANICAL_PRESS, AllItems.BASIN);
        registration.addCraftingStation(PRESSING, AllItems.MECHANICAL_PRESS);
        registration.addCraftingStation(AUTOMATIC_SHAPELESS, AllItems.MECHANICAL_MIXER, AllItems.BASIN);
        registration.addCraftingStation(MIXING, AllItems.MECHANICAL_MIXER, AllItems.BASIN);
        registration.addCraftingStation(MILLING, AllItems.MILLSTONE);
        registration.addCraftingStation(SAWING, AllItems.MECHANICAL_SAW);
        registration.addCraftingStation(CRUSHING, AllItems.CRUSHING_WHEEL);
        registration.addCraftingStation(DEPLOYING, AllItems.DEPLOYER, AllItems.DEPOT, AllItems.BELT_CONNECTOR);
        registration.addCraftingStation(DRAINING, AllItems.ITEM_DRAIN);
        registration.addCraftingStation(MECHANICAL_CRAFTING, AllItems.MECHANICAL_CRAFTER);
        registration.addCraftingStation(SPOUT_FILLING, AllItems.SPOUT);
        registration.addCraftingStation(SANDPAPER_POLISHING, AllItems.SAND_PAPER, AllItems.RED_SAND_PAPER);
        registration.addCraftingStation(FAN_BLASTING, AllItems.ENCASED_FAN);
        registration.addCraftingStation(FAN_HAUNTING, AllItems.ENCASED_FAN);
        registration.addCraftingStation(FAN_SMOKING, AllItems.ENCASED_FAN);
        registration.addCraftingStation(FAN_WASHING, AllItems.ENCASED_FAN);
        registration.addCraftingStation(AUTOMATIC_BREWING, AllItems.MECHANICAL_MIXER, AllItems.BASIN);
        registration.addCraftingStation(BLOCK_CUTTING, AllItems.MECHANICAL_SAW);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        PreparedRecipes preparedRecipes = Internal.getClientSyncedRecipes();
        registration.addRecipes(AUTOMATIC_PACKING, AutoCompactingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(PACKING, CompactingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(PRESSING, PressingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(AUTOMATIC_SHAPELESS, AutoMixingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(MIXING, MixingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(MILLING, MillingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(SAWING, SawingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(CRUSHING, CrushingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(MYSTERY_CONVERSION, MysteriousItemConversionCategory.getRecipes());
        registration.addRecipes(ITEM_APPLICATION, ManualApplicationCategory.getRecipes(preparedRecipes));
        registration.addRecipes(DEPLOYING, DeployingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(MECHANICAL_CRAFTING, MechanicalCraftingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(SANDPAPER_POLISHING, SandpaperPolishingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(SEQUENCED_ASSEMBLY, SequencedAssemblyCategory.getRecipes(preparedRecipes));
        registration.addRecipes(FAN_BLASTING, FanBlastingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(FAN_HAUNTING, FanHauntingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(FAN_SMOKING, FanSmokingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(FAN_WASHING, FanWashingCategory.getRecipes(preparedRecipes));
        registration.addRecipes(AUTOMATIC_BREWING, PotionCategory.getRecipes(preparedRecipes));
        IIngredientManager ingredientManager = registration.getIngredientManager();
        Collection<ItemStack> stacks = ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK);
        registration.addRecipes(DRAINING, DrainingCategory.getRecipes(preparedRecipes, stacks.stream()));
        registration.addRecipes(
            SPOUT_FILLING,
            SpoutFillingCategory.getRecipes(preparedRecipes, stacks.stream(), ingredientManager.getAllIngredients(FabricTypes.FLUID_STACK).stream())
        );
        registerToolboxRecipes(registration);
        registration.addRecipes(BLOCK_CUTTING, BlockCuttingCategory.getRecipes(preparedRecipes));
    }

    public static void registerToolboxRecipes(IRecipeRegistration registration) {
        List<RegistryEntry<Item>> toolboxes = new ArrayList<>();
        for (RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(AllItemTags.TOOLBOXES)) {
            toolboxes.add(entry);
        }
        Ingredient ingredient = Ingredient.ofTag(RegistryEntryList.of(toolboxes));
        String group = "create.toolbox.color";
        List<RecipeEntry<CraftingRecipe>> recipes = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            recipes.add(new RecipeEntry<>(
                RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, group + "/" + color)), new ShapelessRecipe(
                group,
                CraftingRecipeCategory.MISC,
                ToolboxBlock.getColorBlock(color).asItem().getDefaultStack(),
                List.of(Ingredient.ofItem(DyeItem.byColor(color)), ingredient)
            )
            ));
        }
        registration.addRecipes(RecipeTypes.CRAFTING, recipes);
    }

    @Override
    public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
        registration.registerSubtypeInterpreter(FabricTypes.FLUID_STACK, AllFluids.POTION, PotionFluidSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(FabricTypes.FLUID_STACK, AllFluids.POTION.getFlowing(), PotionFluidSubtypeInterpreter.INSTANCE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new JeiExclusionZones());
        registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler<>());
        registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler<>());
        registration.addGhostIngredientHandler(LinkedControllerScreen.class, new GhostIngredientHandler<>());
        registration.addGhostIngredientHandler(ScheduleScreen.class, new GhostIngredientHandler<>());
        registration.addGhostIngredientHandler(RedstoneRequesterScreen.class, new GhostIngredientHandler<>());
        registration.addGhostIngredientHandler(FactoryPanelSetItemScreen.class, new GhostIngredientHandler<>());
        registration.addGuiContainerHandler(StockKeeperRequestScreen.class, new StockKeeperGuiContainerHandler());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new BlueprintTransferHandler(), RecipeTypes.CRAFTING);
        registration.addUniversalRecipeTransferHandler(new StockKeeperTransferHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        StockKeeperRequestScreen.setSearchConsumer(jeiRuntime.getIngredientFilter()::setFilterText);
    }
}
