package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextCodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClipboardEntry {
    public static final Codec<ClipboardEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.BOOL.fieldOf("checked").forGetter(c -> c.checked),
        TextCodecs.CODEC.fieldOf("text").forGetter(c -> c.text),
        ItemStack.OPTIONAL_CODEC.fieldOf("icon").forGetter(c -> c.icon),
        Codec.INT.fieldOf("item_amount").forGetter(c -> c.itemAmount)
    ).apply(
        i, (checked, text, icon, itemAmount) -> {
            ClipboardEntry entry = new ClipboardEntry(checked, text.copy());
            if (!icon.isEmpty())
                entry.displayItem(icon, itemAmount);

            return entry;
        }
    ));

    public static final PacketCodec<RegistryByteBuf, ClipboardEntry> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        c -> c.checked,
        TextCodecs.PACKET_CODEC,
        c -> c.text,
        ItemStack.OPTIONAL_PACKET_CODEC,
        c -> c.icon,
        PacketCodecs.INTEGER,
        c -> c.itemAmount,
        (checked, text, icon, itemAmount) -> {
            ClipboardEntry entry = new ClipboardEntry(checked, text.copy());
            if (!icon.isEmpty())
                entry.displayItem(icon, itemAmount);

            return entry;
        }
    );

    public boolean checked;
    public MutableText text;
    public ItemStack icon;
    public int itemAmount;

    public ClipboardEntry(boolean checked, MutableText text) {
        this.checked = checked;
        this.text = text;
        this.icon = ItemStack.EMPTY;
    }

    public ClipboardEntry displayItem(ItemStack icon, int amount) {
        this.icon = icon;
        this.itemAmount = amount;
        return this;
    }

    public static List<List<ClipboardEntry>> readAll(ItemStack clipboardItem) {
        List<List<ClipboardEntry>> entries = new ArrayList<>();

        // Both these lists are immutable, so we unfortunately need to re-create them to make them mutable
        List<List<ClipboardEntry>> saved = clipboardItem.getOrDefault(AllDataComponents.CLIPBOARD_PAGES, Collections.emptyList());
        for (List<ClipboardEntry> inner : saved)
            entries.add(new ArrayList<>(inner));

        return entries;
    }

    public static List<ClipboardEntry> getLastViewedEntries(ItemStack heldItem) {
        List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(heldItem);
        if (pages.isEmpty())
            return new ArrayList<>();
        int page = !heldItem.contains(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE) ? 0 : Math.min(
            heldItem.getOrDefault(
                AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE,
                0
            ), pages.size() - 1
        );
        List<ClipboardEntry> entries = pages.get(page);
        return entries;
    }

    public static void saveAll(List<List<ClipboardEntry>> entries, ItemStack clipboardItem) {
        clipboardItem.set(AllDataComponents.CLIPBOARD_PAGES, entries);
    }

    public NbtCompound writeNBT() {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("Checked", checked);
        nbt.put("Text", TextCodecs.CODEC, text);
        if (icon.isEmpty())
            return nbt;
        nbt.put("Icon", ItemStack.CODEC, icon);
        nbt.putInt("ItemAmount", itemAmount);
        return nbt;
    }

    public static ClipboardEntry readNBT(NbtCompound tag) {
        ClipboardEntry clipboardEntry = new ClipboardEntry(
            tag.getBoolean("Checked", false),
            tag.get("Text", TextCodecs.CODEC).orElse(ScreenTexts.EMPTY).copy()
        );
        if (tag.contains("Icon"))
            clipboardEntry.displayItem(tag.get("Icon", ItemStack.CODEC).orElse(ItemStack.EMPTY), tag.getInt("ItemAmount", 0));
        return clipboardEntry;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClipboardEntry that))
            return false;

        return checked == that.checked && text.equals(that.text) && ItemStack.areItemsAndComponentsEqual(icon, that.icon);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(checked);
        result = 31 * result + text.hashCode();
        result = 31 * result + ItemStack.hashCode(icon);
        return result;
    }
}
