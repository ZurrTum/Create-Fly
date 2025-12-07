package com.zurrtum.create.client.content.logistics.stockTicker;

import com.zurrtum.create.content.logistics.BigItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CraftableBigItemStack extends BigItemStack {
    public final Identifier id;
    public final CraftableInput input;

    public CraftableBigItemStack(Identifier id, CraftableInput input, ItemStack output) {
        super(output.copy());
        this.id = id;
        this.input = input;
    }
}
