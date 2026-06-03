package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.api.ReliableRecipeViewerClientPlugin;
import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import cc.cassian.rrv.common.builtin.crafting.CraftingClientRecipe;
import cc.cassian.rrv.common.builtin.crafting.CraftingClientRecipeType;
import cc.cassian.rrv.common.overlay.OverlayManager;
import cc.cassian.rrv.common.overlay.itemlist.view.ItemViewOverlay;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.client.compat.rrv.category.*;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.Create.MOD_ID;

public class RrvClientPlugin implements ReliableRecipeViewerClientPlugin {
    public static final Map<ReliableClientRecipeType, List<RecipeTransferHandler>> TRANSFER = new IdentityHashMap<>();
    public static final List<RecipeTransferHandler> UNIVERSAL_TRANSFER = new ArrayList<>();

    public static void addTransferHandler(CraftingClientRecipeType type, RecipeTransferHandler handler) {
        TRANSFER.computeIfAbsent(type, t -> new ArrayList<>()).add(handler);
    }

    public static void addTransferHandler(RecipeTransferHandler handler) {
        UNIVERSAL_TRANSFER.add(handler);
    }

    @Override
    public void onIntegrationInitialize() {
        ItemView.addClientRecipeProvider(RrvClientPlugin::register);
        addTransferHandler(CraftingClientRecipeType.INSTANCE, new BlueprintTransferHandler());
        addTransferHandler(new StockKeeperTransferHandler());
        AbstractSimiContainerScreen.setExclusionZoneSync(new RrvExclusionZoneSync(OverlayManager.INSTANCE));
        StockKeeperRequestScreen.setSearchSync(new RrvStockSearchSync(ItemViewOverlay.INSTANCE));
    }

    private static void register(List<ReliableClientRecipe> output) {
        ClientRecipeManager recipeManager = ClientRecipeManager.INSTANCE;
        AutoCompactingCategory.register(recipeManager, output);
        CompactingCategory.register(recipeManager, output);
        PressingCategory.register(recipeManager, output);
        AutoMixingCategory.register(recipeManager, output);
        MixingCategory.register(recipeManager, output);
        MillingCategory.register(recipeManager, output);
        SawingCategory.register(recipeManager, output);
        CrushingCategory.register(recipeManager, output);
        MysteriousItemConversionCategory.register(output);
        ManualApplicationCategory.register(recipeManager, output);
        DeployingCategory.register(recipeManager, output);
        DrainingCategory.register(recipeManager, output);
        MechanicalCraftingCategory.register(recipeManager, output);
        SpoutFillingCategory.register(recipeManager, output);
        SandPaperPolishingCategory.register(recipeManager, output);
        SequencedAssemblyCategory.register(recipeManager, output);
        FanBlastingCategory.register(recipeManager, output);
        FanHauntingCategory.register(recipeManager, output);
        FanSmokingCategory.register(recipeManager, output);
        FanWashingCategory.register(recipeManager, output);
        PotionCategory.register(recipeManager, output);
        BlockCuttingCategory.register(recipeManager, output);
        registerToolboxRecipes(output);
    }

    public static void registerToolboxRecipes(List<ReliableClientRecipe> output) {
        for (DyeColor color : DyeColor.values()) {
            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "create.toolbox.color/" + color);
            List<SlotContent> ingredients = List.of(
                SlotContent.of(ItemHelper.createDyeIngredient(color)),
                SlotContent.of(AllItemTags.TOOLBOXES)
            );
            SlotContent result = SlotContent.of(ToolboxBlock.getColorBlock(color));
            output.add(new CraftingClientRecipe.Builder(id, ingredients).setResult(result).build());
        }
    }
}
