package com.zurrtum.create.client.content.logistics.tableCloth;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.zurrtum.create.content.logistics.tableCloth.ShoppingListItem;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.infrastructure.component.ShoppingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class TableClothOverlayRenderer {
    public static void tick(Minecraft mc) {
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        HitResult mouseOver = mc.hitResult;
        if (mouseOver == null)
            return;

        ItemStack heldItem = mc.player.getMainHandItem();

        ClientLevel world = mc.level;
        if (mouseOver.getType() != Type.ENTITY) {
            if (!(mouseOver instanceof BlockHitResult bhr))
                return;
            if (!(world.getBlockEntity(bhr.getBlockPos()) instanceof TableClothBlockEntity dcbe))
                return;
            if (!dcbe.isShop())
                return;
            if (heldItem.is(AllItems.CLIPBOARD))
                return;
            TableClothFilteringBehaviour behaviour = (TableClothFilteringBehaviour) dcbe.getBehaviour(TableClothFilteringBehaviour.TYPE);
            if (behaviour.targetsPriceTag(mc.player, bhr))
                return;

            int alreadyPurchased = 0;
            ShoppingList list = ShoppingListItem.getList(heldItem);
            if (list != null)
                alreadyPurchased = list.getPurchases(dcbe.getBlockPos());

            BlueprintOverlayRenderer.displayClothShop(dcbe, alreadyPurchased, list);
            return;
        }

        EntityHitResult entityRay = (EntityHitResult) mouseOver;
        if (!heldItem.is(AllItems.SHOPPING_LIST))
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
