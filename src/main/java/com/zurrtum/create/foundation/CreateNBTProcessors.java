package com.zurrtum.create.foundation;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.codecs.CatnipCodecUtils;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class CreateNBTProcessors {
    public static void register() {
        NBTProcessors.addProcessor(
            BlockEntityType.LECTERN, data -> {
                if (!data.contains("Book"))
                    return data;
                CompoundTag book = data.getCompoundOrEmpty("Book");

                // Writable books can't have click events, so they're safe to keep
                Identifier writableBookResource = BuiltInRegistries.ITEM.getKey(Items.WRITABLE_BOOK);
                if (writableBookResource != BuiltInRegistries.ITEM.getDefaultKey() && book.getStringOr("id", "")
                    .equals(writableBookResource.toString()))
                    return data;

                WrittenBookContent bookContent = CatnipCodecUtils.decodeOrNull(WrittenBookContent.CODEC, book);
                if (bookContent == null)
                    return data;

                for (Filterable<Component> page : bookContent.pages()) {
                    if (NBTProcessors.textComponentHasClickEvent(page.get(false)))
                        return null;
                }

                return data;
            }
        );

        NBTProcessors.addProcessor(AllBlockEntityTypes.CLIPBOARD, CreateNBTProcessors::clipboardProcessor);

        NBTProcessors.addProcessor(AllBlockEntityTypes.CREATIVE_CRATE, NBTProcessors.itemProcessor("Filter"));
    }

    @Nullable
    public static CompoundTag clipboardProcessor(CompoundTag data) {
        DataComponentMap components = data.getCompound("components").flatMap(c -> CatnipCodecUtils.decode(DataComponentMap.CODEC, c)).orElse(null);
        if (components == null)
            return data;

        ClipboardContent content = components.get(AllDataComponents.CLIPBOARD_CONTENT);
        if (content == null)
            return data;

        for (List<ClipboardEntry> entries : content.pages()) {
            for (ClipboardEntry entry : entries) {
                if (NBTProcessors.textComponentHasClickEvent(entry.text))
                    return null;
            }
        }

        return data;
    }
}