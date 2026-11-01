package com.zurrtum.create.infrastructure.tag;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader.EntryWithSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagRemovalInternals {
    public static final ScopedValue<Identifier> TAG_ID_SCOPED_VALUE = ScopedValue.newInstance();
    private static final ThreadLocal<Map<Identifier, List<String>>> TAG_SOURCE_ORDER = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Map<Identifier, List<EntryWithSource>>> REMOVE_ENTRIES = ThreadLocal.withInitial(
        HashMap::new);

    public static void addTagSource(Identifier tagId, String source) {
        TAG_SOURCE_ORDER.get().computeIfAbsent(tagId, _ -> new ArrayList<>()).add(source);
    }

    public static void addRemoveEntry(Identifier tagId, EntryWithSource entry) {
        TagEntry tagEntry = entry.entry();
        TagEntry optional = tagEntry.tag ? TagEntry.optionalTag(tagEntry.id) : TagEntry.optionalElement(tagEntry.id);
        REMOVE_ENTRIES.get().computeIfAbsent(tagId, _ -> new ArrayList<>())
            .add(new EntryWithSource(optional, entry.source()));
    }

    public static boolean isEntryRemove(EntryWithSource entry) {
        List<EntryWithSource> list = REMOVE_ENTRIES.get().get(TAG_ID_SCOPED_VALUE.get());
        return list != null && list.contains(entry);
    }

    public static List<EntryWithSource> mergeAddedAndRemovedEntries(Identifier tagId, List<EntryWithSource> entries) {
        Map<Identifier, List<EntryWithSource>> map = REMOVE_ENTRIES.get();
        if (map.isEmpty()) {
            return entries;
        }
        List<EntryWithSource> result = new ArrayList<>();
        Map<Identifier, List<String>> source = TAG_SOURCE_ORDER.get();
        List<String> list = source.get(tagId);
        if (list != null) {
            for (String sourceId : list) {
                for (EntryWithSource entry : entries) {
                    if (entry.source().equals(sourceId)) {
                        result.add(entry);
                    }
                }
                List<EntryWithSource> remove = map.get(tagId);
                if (remove == null) {
                    continue;
                }
                for (EntryWithSource entry : remove) {
                    if (entry.source().equals(sourceId)) {
                        result.add(entry);
                    }
                }
            }
        }
        return result;
    }

    public static void removeTagRemovalReference(Identifier tagKey) {
        TAG_SOURCE_ORDER.get().remove(tagKey);
        REMOVE_ENTRIES.get().remove(tagKey);
    }

    public static void removeTagRemovalReferences() {
        TAG_SOURCE_ORDER.remove();
        REMOVE_ENTRIES.remove();
    }
}