package com.zurrtum.create.foundation;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.codecs.CatnipCodecUtils;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

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

                WrittenBookContentComponent bookContent = CatnipCodecUtils.decodeOrNull(WrittenBookContentComponent.CODEC, book);
                if (bookContent == null)
                    return data;

                for (RawFilteredPair<Text> page : bookContent.pages()) {
                    if (NBTProcessors.textComponentHasClickEvent(page.get(false)))
                        return null;
                }

                return data;
            }
        );

        NBTProcessors.addProcessor(AllBlockEntityTypes.CLIPBOARD, CreateNBTProcessors::clipboardProcessor);

        NBTProcessors.addProcessor(AllBlockEntityTypes.CREATIVE_CRATE, NBTProcessors.itemProcessor("Filter"));
    }

    public static NbtCompound clipboardProcessor(NbtCompound data) {
        ComponentMap components = data.getCompound("components").flatMap(c -> CatnipCodecUtils.decode(ComponentMap.CODEC, c)).orElse(null);
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