package com.zurrtum.create.infrastructure.tag;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FabricTagFile {
    List<TagEntry> create$remove();

    void create$setRemove(@Nullable List<TagEntry> remove);

    static List<TagEntry> getRemove(TagFile tagFile) {
        return ((FabricTagFile) (Object) tagFile).create$remove();
    }

    static TagFile create(List<TagEntry> entries, boolean replace, List<TagEntry> remove) {
        TagFile tagFile = new TagFile(entries, replace);
        ((FabricTagFile) (Object) tagFile).create$setRemove(remove);
        return tagFile;
    }
}
