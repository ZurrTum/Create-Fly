package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.foundation.item.TooltipWorldContext;
import com.zurrtum.create.infrastructure.component.ShoppingList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ShoppingListItem extends Item {
    public ShoppingListItem(Properties pProperties) {
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
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplay displayComponent,
        Consumer<Component> textConsumer,
        TooltipFlag type
    ) {
        ShoppingList list = getList(stack);

        if (list != null && context instanceof TooltipWorldContext worldContext) {
            Couple<InventorySummary> lists = list.bakeEntries(worldContext.create$getWorld(), null);

            for (InventorySummary items : lists) {
                List<BigItemStack> entries = items.getStacksByCount();
                boolean cost = items == lists.getSecond();

                if (cost)
                    textConsumer.accept(Component.empty());

                if (entries.size() == 1) {
                    BigItemStack entry = entries.getFirst();
                    textConsumer.accept((cost ? Component.translatable("create.table_cloth.total_cost") : Component.literal("")).withStyle(ChatFormatting.GOLD)
                        .append(entry.stack.getHoverName().plainCopy().append(" x").append(String.valueOf(entry.count))
                            .withStyle(cost ? ChatFormatting.YELLOW : ChatFormatting.GRAY)));

                } else {
                    if (cost)
                        textConsumer.accept(Component.translatable("create.table_cloth.total_cost").withStyle(ChatFormatting.GOLD));
                    for (BigItemStack entry : entries) {
                        textConsumer.accept(entry.stack.getHoverName().plainCopy().append(" x").append(String.valueOf(entry.count))
                            .withStyle(cost ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
                    }
                }
            }
        }

        textConsumer.accept(Component.translatable("create.table_cloth.hand_to_shop_keeper").withStyle(ChatFormatting.GRAY));

        textConsumer.accept(Component.translatable("create.table_cloth.sneak_click_discard").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.OFF_HAND || pPlayer == null || !pPlayer.isShiftKeyDown())
            return InteractionResult.PASS;

        pPlayer.displayClientMessage(Component.translatable("create.table_cloth.shopping_list_discarded"), true);
        pPlayer.makeSound(SoundEvents.BOOK_PAGE_TURN);
        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemStack.EMPTY);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        InteractionHand pUsedHand = pContext.getHand();
        Player pPlayer = pContext.getPlayer();
        if (pUsedHand == InteractionHand.OFF_HAND || pPlayer == null || !pPlayer.isShiftKeyDown())
            return InteractionResult.PASS;
        pPlayer.setItemInHand(pUsedHand, ItemStack.EMPTY);

        pPlayer.displayClientMessage(Component.translatable("create.table_cloth.shopping_list_discarded"), true);
        pPlayer.makeSound(SoundEvents.BOOK_PAGE_TURN);
        return InteractionResult.SUCCESS;
    }
}