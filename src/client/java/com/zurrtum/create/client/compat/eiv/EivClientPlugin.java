package com.zurrtum.create.client.compat.eiv;

import com.zurrtum.create.client.compat.eiv.category.*;
import com.zurrtum.create.client.compat.eiv.view.*;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.IExtendedItemViewIntegration;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.api.recipe.ItemView;
import de.crafty.eiv.common.builtin.shaped.CraftingViewType;
import de.crafty.eiv.common.overlay.OverlayManager;
import de.crafty.eiv.common.overlay.itemlist.view.ItemViewOverlay;
import net.minecraft.client.gui.components.EditBox;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class EivClientPlugin implements IExtendedItemViewIntegration {
    public static final AutoCompactingCategory AUTOMATIC_PACKING = new AutoCompactingCategory();
    public static final CompactingCategory PACKING = new CompactingCategory();
    public static final PressingCategory PRESSING = new PressingCategory();
    public static final AutoMixingCategory AUTOMATIC_SHAPELESS = new AutoMixingCategory();
    public static final MixingCategory MIXING = new MixingCategory();
    public static final MillingCategory MILLING = new MillingCategory();
    public static final SawingCategory SAWING = new SawingCategory();
    public static final CrushingCategory CRUSHING = new CrushingCategory();
    public static final MysteriousItemConversionCategory MYSTERY_CONVERSION = new MysteriousItemConversionCategory();
    public static final ManualApplicationCategory ITEM_APPLICATION = new ManualApplicationCategory();
    public static final DeployingCategory DEPLOYING = new DeployingCategory();
    public static final DrainingCategory DRAINING = new DrainingCategory();
    public static final MechanicalCraftingCategory MECHANICAL_CRAFTING = new MechanicalCraftingCategory();
    public static final SpoutFillingCategory SPOUT_FILLING = new SpoutFillingCategory();
    public static final SandPaperPolishingCategory SANDPAPER_POLISHING = new SandPaperPolishingCategory();
    public static final SequencedAssemblyCategory SEQUENCED_ASSEMBLY = new SequencedAssemblyCategory();
    public static final FanBlastingCategory FAN_BLASTING = new FanBlastingCategory();
    public static final FanHauntingCategory FAN_HAUNTING = new FanHauntingCategory();
    public static final FanSmokingCategory FAN_SMOKING = new FanSmokingCategory();
    public static final FanWashingCategory FAN_WASHING = new FanWashingCategory();
    public static final PotionCategory AUTOMATIC_BREWING = new PotionCategory();
    public static final BlockCuttingCategory BLOCK_CUTTING = new BlockCuttingCategory();
    public static final Map<IEivRecipeViewType, List<RecipeTransferHandler>> TRANSFER = new IdentityHashMap<>();
    public static final List<RecipeTransferHandler> UNIVERSAL_TRANSFER = new ArrayList<>();

    public static void addTransferHandler(IEivRecipeViewType type, RecipeTransferHandler handler) {
        TRANSFER.computeIfAbsent(type, t -> new ArrayList<>()).add(handler);
    }

    public static void addTransferHandler(RecipeTransferHandler handler) {
        UNIVERSAL_TRANSFER.add(handler);
    }

    @Override
    public void onIntegrationInitialize() {
        ItemView.registerRecipeWrapper(EivCommonPlugin.AUTOMATIC_PACKING, AutoCompactingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.PACKING, CompactingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.PRESSING, PressingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.AUTOMATIC_SHAPELESS, AutoMixingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.MIXING, MixingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.MILLING, MillingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.SAWING, SawingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.CRUSHING, CrushingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.MYSTERY_CONVERSION, MysteriousItemConversionView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.ITEM_APPLICATION, ManualApplicationView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.DEPLOYING, DeployingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.DRAINING, DrainingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.MECHANICAL_CRAFTING, MechanicalCraftingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.SPOUT_FILLING, SpoutFillingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.SANDPAPER_POLISHING, SandPaperPolishingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.SEQUENCED_ASSEMBLY, SequencedAssemblyView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.FAN_BLASTING, FanBlastingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.FAN_HAUNTING, FanHauntingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.FAN_SMOKING, FanSmokingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.FAN_WASHING, FanWashingView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.AUTOMATIC_BREWING, PotionView::new);
        ItemView.registerRecipeWrapper(EivCommonPlugin.BLOCK_CUTTING, BlockCuttingView::new);
        EivExclusionZoneHelper.setRuntime(OverlayManager.INSTANCE);
        addTransferHandler(CraftingViewType.INSTANCE, new BlueprintTransferHandler());
        addTransferHandler(new StockKeeperTransferHandler());
        StockKeeperRequestScreen.setSearchConsumer(EivClientPlugin::setSearchText);
    }

    public static void setSearchText(String text) {
        EditBox searchbar = ItemViewOverlay.INSTANCE.getSearchbar();
        if (searchbar != null) {
            searchbar.setValue(text);
        }
    }
}
