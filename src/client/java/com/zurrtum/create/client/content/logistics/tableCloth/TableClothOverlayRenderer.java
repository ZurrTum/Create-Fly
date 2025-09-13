package com.zurrtum.create.client.content.logistics.tableCloth;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.zurrtum.create.content.logistics.tableCloth.ShoppingListItem;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.infrastructure.component.ShoppingList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class TableClothOverlayRenderer {
    public static void tick(MinecraftClient mc) {
        if (mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)
            return;
        HitResult mouseOver = mc.crosshairTarget;
        if (mouseOver == null)
            return;

        ItemStack heldItem = mc.player.getMainHandStack();

        ClientWorld world = mc.world;
        if (mouseOver.getType() != Type.ENTITY) {
            if (!(mouseOver instanceof BlockHitResult bhr))
                return;
            if (!(world.getBlockEntity(bhr.getBlockPos()) instanceof TableClothBlockEntity dcbe))
                return;
            if (!dcbe.isShop())
                return;
            if (heldItem.isOf(AllItems.CLIPBOARD))
                return;
            TableClothFilteringBehaviour behaviour = (TableClothFilteringBehaviour) dcbe.getBehaviour(TableClothFilteringBehaviour.TYPE);
            if (behaviour.targetsPriceTag(mc.player, bhr))
                return;

            int alreadyPurchased = 0;
            ShoppingList list = ShoppingListItem.getList(heldItem);
            if (list != null)
                alreadyPurchased = list.getPurchases(dcbe.getPos());

            BlueprintOverlayRenderer.displayClothShop(dcbe, alreadyPurchased, list);
            return;
        }

        EntityHitResult entityRay = (EntityHitResult) mouseOver;
        if (!heldItem.isOf(AllItems.SHOPPING_LIST))
            return;

        ShoppingList list = ShoppingListItem.getList(heldItem);
        BlockPos stockTickerPosition = StockTickerInteractionHandler.getStockTickerPosition(entityRay.getEntity());

        if (list == null || stockTickerPosition == null)
            return;
        if (!(world.getBlockEntity(stockTickerPosition) instanceof StockTickerBlockEntity tickerBE))
            return;
        if (!tickerBE.behaviour.freqId.equals(list.shopNetwork()))
            return;

        BlueprintOverlayRenderer.displayShoppingList(mc.player, list.bakeEntries(world, null));
    }
}
