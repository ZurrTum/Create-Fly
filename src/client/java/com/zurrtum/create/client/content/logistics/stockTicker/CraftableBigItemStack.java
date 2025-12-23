package com.zurrtum.create.client.content.logistics.stockTicker;

import com.zurrtum.create.content.logistics.BigItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CraftableBigItemStack extends BigItemStack {
    public final Identifier id;
    public final CraftableInput input;

    public CraftableBigItemStack(Identifier id, CraftableInput input, ItemStack output) {
        super(output.copy());
        this.id = id;
        this.input = input;
    }
}
