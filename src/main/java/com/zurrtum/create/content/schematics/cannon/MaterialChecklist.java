package com.zurrtum.create.content.schematics.cannon;

import com.google.common.collect.Sets;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
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

        List<Filterable<Component>> pages = new ArrayList<>();

        int itemsWritten = 0;
        MutableComponent textComponent;

        if (blocksNotLoaded) {
            textComponent = Component.literal("\n" + ChatFormatting.RED);
            textComponent.append(Component.translatable("create.materialChecklist.blocksNotLoaded"));
            pages.add(Filterable.passThrough(textComponent));
        }

        List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
        keys.sort((item1, item2) -> {
            Locale locale = Locale.ENGLISH;
            String name1 = item1.getName().getString().toLowerCase(locale);
            String name2 = item2.getName().getString().toLowerCase(locale);
            return name1.compareTo(name2);
        });

        textComponent = Component.empty();
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
                textComponent.append(Component.literal("\n >>>").withStyle(ChatFormatting.BLUE));
                pages.add(Filterable.passThrough(textComponent));
                textComponent = Component.empty();
            }

            itemsWritten++;
            textComponent.append(entry(new ItemStack(item), amount, true, true));
        }

        for (Item item : completed) {
            if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
                itemsWritten = 0;
                textComponent.append(Component.literal("\n >>>").withStyle(ChatFormatting.DARK_GREEN));
                pages.add(Filterable.passThrough(textComponent));
                textComponent = Component.empty();
            }

            itemsWritten++;
            textComponent.append(entry(new ItemStack(item), getRequiredAmount(item), false, true));
        }

        pages.add(Filterable.passThrough(textComponent));

        WrittenBookContent contents = new WrittenBookContent(
            Filterable.passThrough(ChatFormatting.BLUE + "Material Checklist"),
            "Schematicannon",
            0,
            pages,
            true
        );
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, contents);
        textComponent = Component.translatable("create.materialChecklist")
            .setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(Boolean.FALSE));
        book.set(DataComponents.CUSTOM_NAME, textComponent);

        return book;
    }

    public ItemStack createWrittenClipboard() {
        int itemsWritten = 0;

        List<List<ClipboardEntry>> pages = new ArrayList<>();
        List<ClipboardEntry> currentPage = new ArrayList<>();

        if (blocksNotLoaded) {
            currentPage.add(new ClipboardEntry(
                false,
                Component.translatable("create.materialChecklist.blocksNotLoaded").withStyle(ChatFormatting.RED)
            ));
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
                currentPage.add(new ClipboardEntry(false, Component.literal(">>>").withStyle(ChatFormatting.DARK_GRAY)));
                pages.add(currentPage);
                currentPage = new ArrayList<>();
            }

            itemsWritten++;
            currentPage.add(new ClipboardEntry(false, entry(new ItemStack(item), amount, true, false)).displayItem(new ItemStack(item), amount));
        }

        for (Item item : completed) {
            if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
                itemsWritten = 0;
                currentPage.add(new ClipboardEntry(true, Component.literal(">>>").withStyle(ChatFormatting.DARK_GREEN)));
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

        ItemStack clipboard = AllItems.CLIPBOARD.getDefaultInstance();
        clipboard.set(AllDataComponents.CLIPBOARD_CONTENT, new ClipboardContent(ClipboardType.WRITTEN, pages, true));
        clipboard.set(DataComponents.CUSTOM_NAME, Component.translatable("create.materialChecklist").setStyle(Style.EMPTY.withItalic(false)));
        return clipboard;
    }

    public int getRequiredAmount(Item item) {
        int amount = required.getOrDefault(item, 0);
        if (damageRequired.containsKey(item))
            amount += (int) Math.ceil(damageRequired.getInt(item) / (float) new ItemStack(item).getMaxDamage());
        return amount;
    }

    private MutableComponent entry(ItemStack item, int amount, boolean unfinished, boolean forBook) {
        int stacks = amount / 64;
        int remainder = amount % 64;
        MutableComponent tc = Component.empty();
        tc.append(Component.translatable(item.getItem().getDescriptionId()).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowItem(item))));

        if (!unfinished && forBook)
            tc.append(" ✔");
        if (!unfinished || forBook)
            tc.withStyle(unfinished ? ChatFormatting.BLUE : ChatFormatting.DARK_GREEN);
        return tc.append(Component.literal("\n" + " x" + amount).withStyle(ChatFormatting.BLACK))
            .append(Component.literal(" | " + stacks + "▤ +" + remainder + (forBook ? "\n" : "")).withStyle(ChatFormatting.GRAY));
    }

}
