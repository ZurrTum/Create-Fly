package com.zurrtum.create.foundation;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CreateNBTProcessors {
    public static void register() {
        NBTProcessors.addProcessor(
            BlockEntityType.LECTERN, data -> {
                if (!data.contains("Book"))
                    return data;
                NbtCompound book = data.getCompoundOrEmpty("Book");

                // Writable books can't have click events, so they're safe to keep
                Identifier writableBookResource = Registries.ITEM.getId(Items.WRITABLE_BOOK);
                if (writableBookResource != Registries.ITEM.getDefaultId() && book.getString("id", "").equals(writableBookResource.toString()))
                    return data;

                if (!book.contains("tag"))
                    return data;
                NbtCompound tag = book.getCompoundOrEmpty("tag");

                if (!tag.contains("pages"))
                    return data;
                NbtList pages = tag.getListOrEmpty("pages");

                for (NbtElement inbt : pages) {
                    if (NBTProcessors.textComponentHasClickEvent(((NbtString) inbt).value()))
                        return null;
                }
                return data;
            }
        );

        NBTProcessors.addProcessor(AllBlockEntityTypes.CLIPBOARD, CreateNBTProcessors::clipboardProcessor);

        NBTProcessors.addProcessor(AllBlockEntityTypes.CREATIVE_CRATE, NBTProcessors.itemProcessor("Filter"));
    }

    public static NbtCompound clipboardProcessor(NbtCompound data) {
        if (!data.contains("Item"))
            return data;
        NbtCompound item = data.getCompoundOrEmpty("Item");

        if (!item.contains("components"))
            return data;
        NbtCompound itemComponents = item.getCompoundOrEmpty("components");

        if (!itemComponents.contains("create:clipboard_pages"))
            return data;
        NbtList pages = itemComponents.getListOrEmpty("create:clipboard_pages");

        for (NbtElement page : pages) {
            if (!(page instanceof NbtList entries))
                return data;

            for (int i = 0; i < entries.size(); i++) {
                NbtCompound entry = entries.getCompoundOrEmpty(i);

                if (NBTProcessors.textComponentHasClickEvent(entry.getCompoundOrEmpty("text").asString().orElse("")))
                    return null;
            }
        }

        return data;
    }
}