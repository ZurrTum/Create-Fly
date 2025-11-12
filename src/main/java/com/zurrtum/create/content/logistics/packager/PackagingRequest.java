package com.zurrtum.create.content.logistics.packager;

import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public record PackagingRequest(
    ItemStack item, MutableInt count, String address, int linkIndex, MutableBoolean finalLink, MutableInt packageCounter, int orderId,
    @Nullable PackageOrderWithCrafts context
) {

    public static PackagingRequest create(
        ItemStack item,
        int count,
        String address,
        int linkIndex,
        MutableBoolean finalLink,
        int packageCount,
        int orderId,
        @Nullable PackageOrderWithCrafts context
    ) {
        return new PackagingRequest(item, new MutableInt(count), address, linkIndex, finalLink, new MutableInt(packageCount), orderId, context);
    }

    public int getCount() {
        return count.intValue();
    }

    public void subtract(int toSubtract) {
        count.setValue(getCount() - toSubtract);
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }
}
