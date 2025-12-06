package com.zurrtum.create.client.content.equipment.blueprint;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.logistics.tableCloth.BlueprintOverlayShopContext;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.content.trains.track.TrackPlacement.PlacementInfo;
import com.zurrtum.create.infrastructure.component.ShoppingList;
import com.zurrtum.create.infrastructure.packet.c2s.BlueprintPreviewRequestPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class BlueprintOverlayRenderer {
    static boolean active;
    static boolean empty;
    static boolean noOutput;
    static boolean lastSneakState;
    static BlueprintSection lastTargetedSection;
    static BlueprintOverlayShopContext shopContext;

    static Map<ItemStack, ItemStack[]> cachedRenderedFilters = new IdentityHashMap<>();
    static List<Pair<ItemStack, Boolean>> ingredients = new ArrayList<>();
    static List<ItemStack> results = new ArrayList<>();
    static boolean resultCraftable = false;

    public static void tick(MinecraftClient mc) {
        BlueprintSection last = lastTargetedSection;
        lastTargetedSection = null;
        active = false;
        noOutput = false;
        shopContext = null;

        if (mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR || mc.currentScreen != null)
            return;

        HitResult mouseOver = mc.crosshairTarget;
        if (mouseOver == null)
            return;
        if (mouseOver.getType() != Type.ENTITY)
            return;

        EntityHitResult entityRay = (EntityHitResult) mouseOver;
        if (!(entityRay.getEntity() instanceof BlueprintEntity blueprintEntity))
            return;

        BlueprintSection sectionAt = blueprintEntity.getSectionAt(entityRay.getPos().subtract(blueprintEntity.getEntityPos()));

        lastTargetedSection = last;
        active = true;

        boolean sneak = mc.player.isSneaking();
        if (sectionAt != lastTargetedSection || AnimationTickHolder.getTicks() % 10 == 0 || lastSneakState != sneak)
            rebuild(mc, blueprintEntity, sectionAt, sneak);

        lastTargetedSection = sectionAt;
        lastSneakState = sneak;
    }

    public static void displayTrackRequirements(PlacementInfo info, ItemStack pavementItem) {
        if (active)
            return;
        prepareCustomOverlay();

        int tracks = info.requiredTracks;
        while (tracks > 0) {
            ingredients.add(Pair.of(new ItemStack(info.trackMaterial.getBlock(), Math.min(64, tracks)), info.hasRequiredTracks));
            tracks -= 64;
        }

        int pavement = info.requiredPavement;
        while (pavement > 0) {
            ingredients.add(Pair.of(pavementItem.copyWithCount(Math.min(64, pavement)), info.hasRequiredPavement));
            pavement -= 64;
        }
    }

    public static void displayChainRequirements(Item chainItem, int count, boolean fulfilled) {
        if (active)
            return;
        prepareCustomOverlay();

        int chains = count;
        while (chains > 0) {
            ingredients.add(Pair.of(new ItemStack(chainItem, Math.min(64, chains)), fulfilled));
            chains -= 64;
        }
    }

    public static void displayClothShop(TableClothBlockEntity dce, int alreadyPurchased, ShoppingList list) {
        if (active)
            return;
        prepareCustomOverlay();
        noOutput = false;

        shopContext = new BlueprintOverlayShopContext(false, dce.getStockLevelForTrade(list), alreadyPurchased);

        ingredients.add(Pair.of(
            dce.getPaymentItem().copyWithCount(dce.getPaymentAmount()),
            !dce.getPaymentItem().isEmpty() && shopContext.stockLevel() > shopContext.purchases()
        ));
        for (BigItemStack entry : dce.requestData.encodedRequest().stacks())
            results.add(entry.stack.copyWithCount(entry.count));
    }

    public static void displayShoppingList(ClientPlayerEntity player, Couple<InventorySummary> bakedList) {
        if (active || bakedList == null)
            return;
        prepareCustomOverlay();
        noOutput = false;

        shopContext = new BlueprintOverlayShopContext(true, 1, 0);

        for (BigItemStack entry : bakedList.getSecond().getStacksByCount()) {
            ingredients.add(Pair.of(entry.stack.copyWithCount(entry.count), canAfford(player, entry)));
        }

        for (BigItemStack entry : bakedList.getFirst().getStacksByCount())
            results.add(entry.stack.copyWithCount(entry.count));
    }

    private static boolean canAfford(ClientPlayerEntity player, BigItemStack entry) {
        int itemsPresent = 0;
        PlayerInventory playerInventory = player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack item = playerInventory.getStack(i);
            if (item.isEmpty() || !ItemStack.areItemsAndComponentsEqual(item, entry.stack))
                continue;
            itemsPresent += item.getCount();
        }
        return itemsPresent >= entry.count;
    }

    private static void prepareCustomOverlay() {
        active = true;
        empty = false;
        noOutput = true;
        ingredients.clear();
        results.clear();
        shopContext = null;
    }

    public static void rebuild(MinecraftClient mc, BlueprintEntity blueprintEntity, BlueprintSection sectionAt, boolean sneak) {
        empty = sectionAt.getItems().isEmpty();
        if (empty) {
            cachedRenderedFilters.clear();
            ingredients.clear();
            results.clear();
            return;
        }
        mc.player.networkHandler.sendPacket(new BlueprintPreviewRequestPacket(blueprintEntity.getId(), sectionAt.index, sneak));
    }

    public static void updatePreview(List<ItemStack> available, List<ItemStack> missing, ItemStack result) {
        cachedRenderedFilters.clear();
        ingredients.clear();
        results.clear();
        if (available.isEmpty() && missing.isEmpty()) {
            return;
        }
        for (ItemStack stack : available) {
            ingredients.add(Pair.of(stack, true));
        }
        if (!missing.isEmpty()) {
            for (ItemStack stack : missing) {
                ingredients.add(Pair.of(stack, false));
            }
            results.add(result);
            resultCraftable = false;
        } else if (result.isEmpty()) {
            resultCraftable = false;
        } else {
            results.add(result);
            resultCraftable = true;
        }
    }

    public static void renderOverlay(MinecraftClient mc, DrawContext guiGraphics) {
        if (mc.currentScreen != null)
            return;

        if (!active || empty)
            return;

        boolean invalidShop = shopContext != null && (ingredients.isEmpty() || ingredients.getFirst().getFirst()
            .isEmpty() || shopContext.stockLevel() == 0);

        int w = 21 * ingredients.size();

        if (!noOutput) {
            w += 21 * results.size();
            w += 30;
        }

        int width = guiGraphics.getScaledWindowWidth();
        int x = (width - w) / 2;
        int y = guiGraphics.getScaledWindowHeight() - 100;

        if (shopContext != null) {
            TooltipBackgroundRenderer.render(guiGraphics, x - 2, y + 1, w + 4, 19, null);

            AllGuiTextures.TRADE_OVERLAY.render(guiGraphics, width / 2 - 48, y - 19);
            if (shopContext.purchases() > 0) {
                guiGraphics.drawItem(AllItems.SHOPPING_LIST.getDefaultStack(), width / 2 + 20, y - 20);
                guiGraphics.drawText(
                    mc.textRenderer,
                    Text.literal("x" + shopContext.purchases()),
                    width / 2 + 20 + 16,
                    y - 20 + 4,
                    0xff_eeeeee,
                    true
                );
            }
        }

        // Ingredients
        for (Pair<ItemStack, Boolean> pair : ingredients) {
            (pair.getSecond() ? AllGuiTextures.HOTSLOT_ACTIVE : AllGuiTextures.HOTSLOT).render(guiGraphics, x, y);
            ItemStack itemStack = pair.getFirst();
            String count = shopContext != null && !shopContext.checkout() || pair.getSecond() ? null : Formatting.GOLD.toString() + itemStack.getCount();
            drawItemStack(guiGraphics, mc, x, y, itemStack, count);
            x += 21;
        }

        if (noOutput)
            return;

        // Arrow
        x += 5;
        if (invalidShop)
            AllGuiTextures.HOTSLOT_ARROW_BAD.render(guiGraphics, x, y + 4);
        else
            AllGuiTextures.HOTSLOT_ARROW.render(guiGraphics, x, y + 4);
        x += 25;

        // Outputs
        if (results.isEmpty()) {
            AllGuiTextures.HOTSLOT.render(guiGraphics, x, y);
            guiGraphics.drawItem(Items.BARRIER.getDefaultStack(), x + 3, y + 3);
        } else {
            for (ItemStack result : results) {
                AllGuiTextures slot = resultCraftable ? AllGuiTextures.HOTSLOT_SUPER_ACTIVE : AllGuiTextures.HOTSLOT;
                if (!invalidShop && shopContext != null && shopContext.stockLevel() > shopContext.purchases())
                    slot = AllGuiTextures.HOTSLOT_ACTIVE;
                slot.render(guiGraphics, resultCraftable ? x - 1 : x, resultCraftable ? y - 1 : y);
                drawItemStack(guiGraphics, mc, x, y, result, null);
                x += 21;
            }
        }

        if (shopContext != null && !shopContext.checkout()) {
            int cycle = 0;
            for (boolean count : Iterate.trueAndFalse)
                for (int i = 0; i < results.size(); i++) {
                    ItemStack result = results.get(i);
                    List<Text> tooltipLines = result.getTooltip(TooltipContext.create(mc.world), mc.player, TooltipType.Default.BASIC);
                    if (tooltipLines.size() <= 1)
                        continue;
                    if (count) {
                        cycle++;
                        continue;
                    }
                    if ((mc.inGameHud.getTicks() / 40) % cycle != i)
                        continue;
                    Window window = mc.getWindow();
                    guiGraphics.drawTooltip(mc.textRenderer, tooltipLines, 0, 0);
                    guiGraphics.drawTooltipImmediately(
                        mc.textRenderer,
                        tooltipLines.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList(),
                        window.getScaledWidth(),
                        window.getScaledHeight(),
                        HoveredTooltipPositioner.INSTANCE,
                        null
                    );
                }
        }
    }

    public static void drawItemStack(DrawContext graphics, MinecraftClient mc, int x, int y, ItemStack itemStack, String count) {
        if (itemStack.getItem() instanceof FilterItem) {
            int step = AnimationTickHolder.getTicks(mc.world) / 10;
            ItemStack[] itemsMatchingFilter = getItemsMatchingFilter(itemStack);
            if (itemsMatchingFilter.length > 0)
                itemStack = itemsMatchingFilter[step % itemsMatchingFilter.length];
        }

        graphics.drawItem(itemStack, x + 3, y + 3);
        graphics.drawStackOverlay(mc.textRenderer, itemStack, x + 3, y + 3, count);
    }

    private static ItemStack[] getItemsMatchingFilter(ItemStack filter) {
        return cachedRenderedFilters.computeIfAbsent(
            filter, itemStack -> {
                if (itemStack.getItem() instanceof FilterItem filterItem) {
                    return filterItem.getFilterItems(itemStack);
                }

                return new ItemStack[0];
            }
        );
    }
}
