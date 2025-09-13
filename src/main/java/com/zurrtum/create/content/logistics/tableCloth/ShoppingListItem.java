package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.foundation.item.TooltipWorldContext;
import com.zurrtum.create.infrastructure.component.ShoppingList;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class ShoppingListItem extends Item {
    public ShoppingListItem(Settings pProperties) {
        super(pProperties);
    }

    public static ShoppingList getList(ItemStack stack) {
        return stack.get(AllDataComponents.SHOPPING_LIST);
    }

    public static ItemStack saveList(ItemStack stack, ShoppingList list, String address) {
        stack.set(AllDataComponents.SHOPPING_LIST, list);
        stack.set(AllDataComponents.SHOPPING_LIST_ADDRESS, address);
        return stack;
    }

    public static String getAddress(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.SHOPPING_LIST_ADDRESS, "");
    }

    @Override
    public void appendTooltip(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> textConsumer,
        TooltipType type
    ) {
        ShoppingList list = getList(stack);

        if (list != null && context instanceof TooltipWorldContext worldContext) {
            Couple<InventorySummary> lists = list.bakeEntries(worldContext.create$getWorld(), null);

            for (InventorySummary items : lists) {
                List<BigItemStack> entries = items.getStacksByCount();
                boolean cost = items == lists.getSecond();

                if (cost)
                    textConsumer.accept(Text.empty());

                if (entries.size() == 1) {
                    BigItemStack entry = entries.getFirst();
                    textConsumer.accept((cost ? Text.translatable("create.table_cloth.total_cost") : Text.literal("")).formatted(Formatting.GOLD)
                        .append(entry.stack.getName().copyContentOnly().append(" x").append(String.valueOf(entry.count))
                            .formatted(cost ? Formatting.YELLOW : Formatting.GRAY)));

                } else {
                    if (cost)
                        textConsumer.accept(Text.translatable("create.table_cloth.total_cost").formatted(Formatting.GOLD));
                    for (BigItemStack entry : entries) {
                        textConsumer.accept(entry.stack.getName().copyContentOnly().append(" x").append(String.valueOf(entry.count))
                            .formatted(cost ? Formatting.YELLOW : Formatting.GRAY));
                    }
                }
            }
        }

        textConsumer.accept(Text.translatable("create.table_cloth.hand_to_shop_keeper").formatted(Formatting.GRAY));

        textConsumer.accept(Text.translatable("create.table_cloth.sneak_click_discard").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public ActionResult use(World pLevel, PlayerEntity pPlayer, Hand pUsedHand) {
        if (pUsedHand == Hand.OFF_HAND || pPlayer == null || !pPlayer.isSneaking())
            return ActionResult.PASS;

        pPlayer.sendMessage(Text.translatable("create.table_cloth.shopping_list_discarded"), true);
        pPlayer.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN);
        return ActionResult.SUCCESS.withNewHandStack(ItemStack.EMPTY);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext pContext) {
        Hand pUsedHand = pContext.getHand();
        PlayerEntity pPlayer = pContext.getPlayer();
        if (pUsedHand == Hand.OFF_HAND || pPlayer == null || !pPlayer.isSneaking())
            return ActionResult.PASS;
        pPlayer.setStackInHand(pUsedHand, ItemStack.EMPTY);

        pPlayer.sendMessage(Text.translatable("create.table_cloth.shopping_list_discarded"), true);
        pPlayer.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN);
        return ActionResult.SUCCESS;
    }
}