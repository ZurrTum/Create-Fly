package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import net.minecraft.component.ComponentChanges;

import java.util.List;
import java.util.Optional;

public class CreateComponentProcessors {
    @SuppressWarnings({"unchecked", "OptionalAssignedToNull"})
    public static ComponentChanges clipboardProcessor(ComponentChanges data) {
        data.withRemovedIf(type -> {
            if (type.equals(AllDataComponents.CLIPBOARD_PAGES)) {
                Optional<List<List<ClipboardEntry>>> optional = (Optional<List<List<ClipboardEntry>>>) data.get(type);
                if (optional != null) {
                    for (List<ClipboardEntry> page : optional.orElse(List.of())) {
                        for (ClipboardEntry entry : page) {
                            if (NBTProcessors.textComponentHasClickEvent(entry.text))
                                return true;
                        }
                    }
                }
            }

            return false;
        });

        return data;
    }
}
