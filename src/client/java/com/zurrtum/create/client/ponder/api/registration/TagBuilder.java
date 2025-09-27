package com.zurrtum.create.client.ponder.api.registration;

import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;

public interface TagBuilder {

    TagBuilder title(String title);

    TagBuilder description(String description);

    TagBuilder addToIndex();

    TagBuilder icon(Identifier location);

    TagBuilder icon(String path);

    TagBuilder idAsIcon();

    TagBuilder item(ItemConvertible item, boolean useAsIcon, boolean useAsMainItem);

    default TagBuilder item(ItemConvertible item) {
        return item(item, true, true);
    }

    void register();

}