package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
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
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class FilterItem extends Item implements MenuProvider, SupportsItemCopying {
    public static ListFilterItem regular(Settings properties) {
        return new ListFilterItem(properties);
    }

    public static AttributeFilterItem attribute(Settings properties) {
        return new AttributeFilterItem(properties);
    }

    public static PackageFilterItem address(Settings properties) {
        return new PackageFilterItem(properties);
    }

    protected FilterItem(Settings properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null)
            return ActionResult.PASS;
        return use(context.getWorld(), context.getPlayer(), context.getHand());
    }

    @Override
    @SuppressWarnings("deprecation")
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

    public abstract List<Text> makeSummary(ItemStack filter);

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
    public abstract @Nullable MenuBase<?> createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData);

    @Override
    public Text getDisplayName() {
        return getName();
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

    public abstract ComponentType<?> getComponentType();

    public abstract FilterItemStack makeStackWrapper(ItemStack filter);

    public abstract ItemStack[] getFilterItems(ItemStack stack);
}
