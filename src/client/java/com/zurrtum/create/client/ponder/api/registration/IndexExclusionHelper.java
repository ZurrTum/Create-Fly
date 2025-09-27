package com.zurrtum.create.client.ponder.api.registration;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

import java.util.function.Predicate;

public interface IndexExclusionHelper {

    IndexExclusionHelper exclude(ItemConvertible item);

    IndexExclusionHelper excludeItemVariants(Class<? extends Item> itemClazz, Item originalVariant);

    IndexExclusionHelper excludeBlockVariants(Class<? extends Block> blockClazz, Block originalVariant);

    IndexExclusionHelper exclude(Predicate<ItemConvertible> predicate);

}