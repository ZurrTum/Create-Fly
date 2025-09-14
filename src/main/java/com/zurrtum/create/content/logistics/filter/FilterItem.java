package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FilterItem extends Item implements MenuProvider, SupportsItemCopying {

    private final FilterType type;

    private enum FilterType {
        REGULAR,
        ATTRIBUTE,
        PACKAGE;
    }

    public static FilterItem regular(Settings properties) {
        return new FilterItem(FilterType.REGULAR, properties);
    }

    public static FilterItem attribute(Settings properties) {
        return new FilterItem(FilterType.ATTRIBUTE, properties);
    }

    public static FilterItem address(Settings properties) {
        return new FilterItem(FilterType.PACKAGE, properties);
    }

    private FilterItem(FilterType type, Settings properties) {
        super(properties);
        this.type = type;
    }

    @NotNull
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null)
            return ActionResult.PASS;
        return use(context.getWorld(), context.getPlayer(), context.getHand());
    }

    @Override
    public void appendTooltip(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> textConsumer,
        TooltipType type
    ) {
        if (AllClientHandle.INSTANCE.shiftDown())
            return;
        List<Text> makeSummary = makeSummary(stack);
        if (makeSummary.isEmpty())
            return;
        textConsumer.accept(ScreenTexts.SPACE);
        makeSummary.forEach(textConsumer);
    }

    private List<Text> makeSummary(ItemStack filter) {
        List<Text> list = new ArrayList<>();
        if (filter.getComponentChanges().isEmpty())
            return list;

        if (type == FilterType.REGULAR) {
            ItemStackHandler filterItems = getFilterItems(filter);
            boolean blacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);

            list.add((blacklist ? Text.translatable("create.gui.filter.deny_list") : Text.translatable("create.gui.filter.allow_list")).formatted(
                Formatting.GOLD));
            int count = 0;
            for (int i = 0, size = filterItems.size(); i < size; i++) {
                if (count > 3) {
                    list.add(Text.literal("- ...").formatted(Formatting.DARK_GRAY));
                    break;
                }

                ItemStack filterStack = filterItems.getStack(i);
                if (filterStack.isEmpty())
                    continue;
                list.add(Text.literal("- ").append(filterStack.getName()).formatted(Formatting.GRAY));
                count++;
            }

            if (count == 0)
                return Collections.emptyList();
        }

        if (type == FilterType.ATTRIBUTE) {
            AttributeFilterWhitelistMode whitelistMode = filter.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);
            list.add((whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ ? Text.translatable(
                "create.gui.attribute_filter.allow_list_conjunctive") : whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ ? Text.translatable(
                "create.gui.attribute_filter.allow_list_disjunctive") : Text.translatable("create.gui.attribute_filter.deny_list")).formatted(
                Formatting.GOLD));

            int count = 0;
            List<ItemAttributeEntry> attributes = filter.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, new ArrayList<>());
            //noinspection DataFlowIssue
            for (ItemAttributeEntry attributeEntry : attributes) {
                ItemAttribute attribute = attributeEntry.attribute();
                if (attribute == null)
                    continue;
                boolean inverted = attributeEntry.inverted();
                if (count > 3) {
                    list.add(Text.literal("- ...").formatted(Formatting.DARK_GRAY));
                    break;
                }
                list.add(Text.literal("- ").append(attribute.format(inverted)));
                count++;
            }

            if (count == 0)
                return Collections.emptyList();
        }

        if (type == FilterType.PACKAGE) {
            String address = PackageItem.getAddress(filter);
            if (!address.isBlank())
                list.add(Text.literal("-> ").formatted(Formatting.GRAY).append(Text.literal(address).formatted(Formatting.GOLD)));
        }

        return list;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!player.isSneaking() && hand == Hand.MAIN_HAND) {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer)
                openHandledScreen(serverPlayer);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public @Nullable MenuBase<?> createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        ItemStack heldItem = player.getMainHandStack();
        ItemStack.PACKET_CODEC.encode(extraData, heldItem);
        if (type == FilterType.REGULAR)
            return new FilterMenu(id, inv, heldItem);
        if (type == FilterType.ATTRIBUTE)
            return new AttributeFilterMenu(id, inv, heldItem);
        if (type == FilterType.PACKAGE)
            return new PackageFilterMenu(id, inv, heldItem);
        return null;
    }

    @Override
    public Text getDisplayName() {
        return getName();
    }

    public static ItemStackHandler getFilterItems(ItemStack stack) {
        ItemStackHandler newInv = new ItemStackHandler(18);
        if (AllItems.FILTER != stack.getItem())
            throw new IllegalArgumentException("Cannot get filter items from non-filter: " + stack);
        if (!stack.contains(AllDataComponents.FILTER_ITEMS))
            return newInv;

        //noinspection DataFlowIssue - It's fine:tm: we check if it has the component before doing this
        ItemHelper.fillItemStackHandler(stack.get(AllDataComponents.FILTER_ITEMS), newInv);

        return newInv;
    }

    public static boolean testDirect(ItemStack filter, ItemStack stack, boolean matchNBT) {
        if (matchNBT) {
            if (PackageItem.isPackage(filter) && PackageItem.isPackage(stack))
                return doPackagesHaveSameData(filter, stack);

            return ItemStack.areItemsAndComponentsEqual(filter, stack);
        }

        if (PackageItem.isPackage(filter) && PackageItem.isPackage(stack))
            return true;

        return ItemHelper.sameItem(filter, stack);
    }

    public static boolean doPackagesHaveSameData(@NotNull ItemStack a, @NotNull ItemStack b) {
        if (a.isEmpty())
            return false;
        if (!ItemStack.areItemsAndComponentsEqual(a, b))
            return false;
        for (Component<?> component : a.getComponents()) {
            ComponentType<?> type = component.type();
            if (type.equals(AllDataComponents.PACKAGE_ORDER_DATA) || type.equals(AllDataComponents.PACKAGE_ORDER_CONTEXT))
                continue;
            if (!Objects.equals(a.get(type), b.get(type)))
                return false;
        }
        return true;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return switch (type) {
            case ATTRIBUTE -> AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES;
            case PACKAGE -> AllDataComponents.PACKAGE_ADDRESS;
            case REGULAR -> AllDataComponents.FILTER_ITEMS;
        };
    }

}
