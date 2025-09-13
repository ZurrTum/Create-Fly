package com.zurrtum.create.content.schematics.cannon;

import com.google.common.collect.Sets;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.clipboard.ClipboardOverrides;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MaterialChecklist {

    public static final int MAX_ENTRIES_PER_PAGE = 5;
    public static final int MAX_ENTRIES_PER_CLIPBOARD_PAGE = 7;

    public Object2IntMap<Item> gathered = new Object2IntArrayMap<>();
    public Object2IntMap<Item> required = new Object2IntArrayMap<>();
    public Object2IntMap<Item> damageRequired = new Object2IntArrayMap<>();
    public boolean blocksNotLoaded;

    public void warnBlockNotLoaded() {
        blocksNotLoaded = true;
    }

    public void require(ItemRequirement requirement) {
        if (requirement.isEmpty())
            return;
        if (requirement.isInvalid())
            return;

        for (ItemRequirement.StackRequirement stack : requirement.getRequiredItems()) {
            if (stack.usage == ItemUseType.DAMAGE)
                putOrIncrement(damageRequired, stack.stack);
            if (stack.usage == ItemUseType.CONSUME)
                putOrIncrement(required, stack.stack);
        }
    }

    private void putOrIncrement(Object2IntMap<Item> map, ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.AIR)
            return;
        if (map.containsKey(item))
            map.put(item, map.getInt(item) + stack.getCount());
        else
            map.put(item, stack.getCount());
    }

    public void collect(ItemStack stack) {
        Item item = stack.getItem();
        if (required.containsKey(item) || damageRequired.containsKey(item))
            if (gathered.containsKey(item))
                gathered.put(item, gathered.getInt(item) + stack.getCount());
            else
                gathered.put(item, stack.getCount());
    }

    public ItemStack createWrittenBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        List<RawFilteredPair<Text>> pages = new ArrayList<>();

        int itemsWritten = 0;
        MutableText textComponent;

        if (blocksNotLoaded) {
            textComponent = Text.literal("\n" + Formatting.RED);
            textComponent = textComponent.append(Text.translatable("create.materialChecklist.blocksNotLoaded"));
            pages.add(RawFilteredPair.of(textComponent));
        }

        List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
        Collections.sort(
            keys, (item1, item2) -> {
                Locale locale = Locale.ENGLISH;
                String name1 = item1.getName().getString().toLowerCase(locale);
                String name2 = item2.getName().getString().toLowerCase(locale);
                return name1.compareTo(name2);
            }
        );

        textComponent = Text.empty();
        List<Item> completed = new ArrayList<>();
        for (Item item : keys) {
            int amount = getRequiredAmount(item);
            if (gathered.containsKey(item))
                amount -= gathered.getInt(item);

            if (amount <= 0) {
                completed.add(item);
                continue;
            }

            if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
                itemsWritten = 0;
                textComponent.append(Text.literal("\n >>>").formatted(Formatting.BLUE));
                pages.add(RawFilteredPair.of(textComponent));
                textComponent = Text.empty();
            }

            itemsWritten++;
            textComponent.append(entry(new ItemStack(item), amount, true, true));
        }

        for (Item item : completed) {
            if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
                itemsWritten = 0;
                textComponent.append(Text.literal("\n >>>").formatted(Formatting.DARK_GREEN));
                pages.add(RawFilteredPair.of(textComponent));
                textComponent = Text.empty();
            }

            itemsWritten++;
            textComponent.append(entry(new ItemStack(item), getRequiredAmount(item), false, true));
        }

        pages.add(RawFilteredPair.of(textComponent));

        WrittenBookContentComponent contents = new WrittenBookContentComponent(
            RawFilteredPair.of(Formatting.BLUE + "Material Checklist"),
            "Schematicannon",
            0,
            pages,
            true
        );
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, contents);
        textComponent = Text.translatable("create.materialChecklist").setStyle(Style.EMPTY.withColor(Formatting.BLUE).withItalic(Boolean.FALSE));
        book.set(DataComponentTypes.CUSTOM_NAME, textComponent);

        return book;
    }

    public ItemStack createWrittenClipboard() {
        ItemStack clipboard = AllItems.CLIPBOARD.getDefaultStack();
        int itemsWritten = 0;

        List<List<ClipboardEntry>> pages = new ArrayList<>();
        List<ClipboardEntry> currentPage = new ArrayList<>();

        if (blocksNotLoaded) {
            currentPage.add(new ClipboardEntry(false, Text.translatable("create.materialChecklist.blocksNotLoaded").formatted(Formatting.RED)));
        }

        List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
        keys.sort((item1, item2) -> {
            Locale locale = Locale.ENGLISH;
            String name1 = item1.getName().getString().toLowerCase(locale);
            String name2 = item2.getName().getString().toLowerCase(locale);
            return name1.compareTo(name2);
        });

        List<Item> completed = new ArrayList<>();
        for (Item item : keys) {
            int amount = getRequiredAmount(item);
            if (gathered.containsKey(item))
                amount -= gathered.getInt(item);

            if (amount <= 0) {
                completed.add(item);
                continue;
            }

            if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
                itemsWritten = 0;
                currentPage.add(new ClipboardEntry(false, Text.literal(">>>").formatted(Formatting.DARK_GRAY)));
                pages.add(currentPage);
                currentPage = new ArrayList<>();
            }

            itemsWritten++;
            currentPage.add(new ClipboardEntry(false, entry(new ItemStack(item), amount, true, false)).displayItem(new ItemStack(item), amount));
        }

        for (Item item : completed) {
            if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
                itemsWritten = 0;
                currentPage.add(new ClipboardEntry(true, Text.literal(">>>").formatted(Formatting.DARK_GREEN)));
                pages.add(currentPage);
                currentPage = new ArrayList<>();
            }

            itemsWritten++;
            currentPage.add(new ClipboardEntry(
                true,
                entry(new ItemStack(item), getRequiredAmount(item), false, false)
            ).displayItem(new ItemStack(item), 0));
        }

        pages.add(currentPage);
        ClipboardEntry.saveAll(pages, clipboard);
        ClipboardOverrides.switchTo(ClipboardType.WRITTEN, clipboard);
        clipboard.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("create.materialChecklist").setStyle(Style.EMPTY.withItalic(false)));
        clipboard.set(AllDataComponents.CLIPBOARD_READ_ONLY, Unit.INSTANCE);
        return clipboard;
    }

    public int getRequiredAmount(Item item) {
        int amount = required.getOrDefault(item, 0);
        if (damageRequired.containsKey(item))
            amount += Math.ceil(damageRequired.getInt(item) / (float) new ItemStack(item).getMaxDamage());
        return amount;
    }

    private MutableText entry(ItemStack item, int amount, boolean unfinished, boolean forBook) {
        int stacks = amount / 64;
        int remainder = amount % 64;
        MutableText tc = Text.empty();
        tc.append(Text.translatable(item.getItem().getTranslationKey()).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowItem(item))));

        if (!unfinished && forBook)
            tc.append(" ✔");
        if (!unfinished || forBook)
            tc.formatted(unfinished ? Formatting.BLUE : Formatting.DARK_GREEN);
        return tc.append(Text.literal("\n" + " x" + amount).formatted(Formatting.BLACK))
            .append(Text.literal(" | " + stacks + "▤ +" + remainder + (forBook ? "\n" : "")).formatted(Formatting.GRAY));
    }

}
