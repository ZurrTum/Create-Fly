package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.category.*;
import com.zurrtum.create.client.compat.rei.display.MysteriousItemConversionDisplay;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.compat.rei.display.DrainingDisplay;
import com.zurrtum.create.compat.rei.display.SpoutFillingDisplay;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry.CategoryConfiguration;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCraftingDisplay;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public class ReiClientPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<MysteriousItemConversionDisplay> MYSTERY_CONVERSION = CategoryIdentifier.of(MOD_ID, "mystery_conversion");

    private <T extends Display> Consumer<CategoryConfiguration<T>> workstations(ItemLike... item) {
        EntryIngredient[] workstations = new EntryIngredient[item.length];
        for (int i = 0; i < item.length; i++) {
            workstations[i] = EntryIngredients.of(item[i]);
        }
        return config -> config.addWorkstations(workstations);
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new PotionCategory(), workstations(AllItems.MECHANICAL_MIXER, AllItems.BASIN));
        registry.add(new AutoCompactingCategory(), workstations(AllItems.MECHANICAL_PRESS, AllItems.BASIN));
        registry.add(new AutoMixingCategory(), workstations(AllItems.MECHANICAL_MIXER, AllItems.BASIN));
        registry.add(new BlockCuttingCategory(), workstations(AllItems.MECHANICAL_SAW));
        registry.add(new CrushingCategory(), workstations(AllItems.CRUSHING_WHEEL));
        registry.add(new DeployingCategory(), workstations(AllItems.DEPLOYER, AllItems.DEPOT, AllItems.BELT_CONNECTOR));
        registry.add(new DrainingCategory(), workstations(AllItems.ITEM_DRAIN));
        registry.add(new FanBlastingCategory(), workstations(AllItems.ENCASED_FAN));
        registry.add(new FanHauntingCategory(), workstations(AllItems.ENCASED_FAN));
        registry.add(new FanSmokingCategory(), workstations(AllItems.ENCASED_FAN));
        registry.add(new FanWashingCategory(), workstations(AllItems.ENCASED_FAN));
        registry.add(new ManualApplicationCategory());
        registry.add(new MechanicalCraftingCategory(), workstations(AllItems.MECHANICAL_CRAFTER));
        registry.add(new MillingCategory(), workstations(AllItems.MILLSTONE));
        registry.add(new MixingCategory(), workstations(AllItems.MECHANICAL_MIXER, AllItems.BASIN));
        registry.add(new MysteriousItemConversionCategory());
        registry.add(new CompactingCategory(), workstations(AllItems.MECHANICAL_PRESS, AllItems.BASIN));
        registry.add(new PressingCategory(), workstations(AllItems.MECHANICAL_PRESS));
        registry.add(new SandpaperPolishingCategory(), workstations(AllItems.SAND_PAPER, AllItems.RED_SAND_PAPER));
        registry.add(new SawingCategory(), workstations(AllItems.MECHANICAL_SAW));
        registry.add(new SequencedAssemblyCategory());
        registry.add(new SpoutFillingCategory(), workstations(AllItems.SPOUT));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.add(new MysteriousItemConversionDisplay(AllItems.EMPTY_BLAZE_BURNER, AllItems.BLAZE_BURNER));
        registry.add(new MysteriousItemConversionDisplay(AllItems.PECULIAR_BELL, AllItems.HAUNTED_BELL));
        registerToolboxRecipes(registry);
        EntryRegistry entrys = EntryRegistry.getInstance();
        SpoutFillingDisplay.register(
            entrys.getEntryStacks().filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.ITEM)),
            entrys.getEntryStacks().filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.FLUID)),
            registry
        );
        DrainingDisplay.register(entrys.getEntryStacks().filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.ITEM)), registry);
    }

    private static void registerToolboxRecipes(DisplayRegistry registry) {
        EntryIngredient ingredient = EntryIngredients.ofItemTag(AllItemTags.TOOLBOXES);
        for (DyeColor color : DyeColor.values()) {
            registry.add(new ClientsidedCraftingDisplay.Shapeless(
                List.of(ingredient, EntryIngredients.of(DyeItem.byColor(color))),
                List.of(EntryIngredients.of(ToolboxBlock.getColorBlock(color))),
                Optional.empty()
            ));
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new GhostIngredientHandler<>());
        registry.registerFocusedStack(new StockKeeperGuiContainerHandler());
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new BlueprintTransferHandler());
        registry.register(new StockKeeperTransferHandler());
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AbstractSimiContainerScreen.class, new ReiExclusionZones());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerInputMethods(InputMethodRegistry registry) {
        REIRuntime runtime = REIRuntime.getInstance();
        TextField search = runtime.getSearchTextField();
        if (search != null) {
            StockKeeperRequestScreen.setSearchConsumer(search::setText);
            StockKeeperRequestScreen.setSearchSupplier(force -> {
                if (force || search.isFocused()) {
                    return search.getText();
                }
                return null;
            });
        }
    }
}
