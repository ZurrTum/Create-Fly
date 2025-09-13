package com.zurrtum.create.content.equipment.clipboard;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import net.minecraft.item.ItemStack;

public class ClipboardOverrides {
    public static void switchTo(ClipboardType type, ItemStack clipboardItem) {
        clipboardItem.set(AllDataComponents.CLIPBOARD_TYPE, type);
    }
}
