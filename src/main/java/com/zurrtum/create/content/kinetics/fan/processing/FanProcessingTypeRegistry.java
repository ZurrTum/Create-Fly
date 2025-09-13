package com.zurrtum.create.content.kinetics.fan.processing;

import com.zurrtum.create.api.registry.CreateRegistries;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public class FanProcessingTypeRegistry {
    private static final List<FanProcessingType> SORTED_TYPES = new ReferenceArrayList<>();
    @UnmodifiableView
    public static final List<FanProcessingType> SORTED_TYPES_VIEW = Collections.unmodifiableList(SORTED_TYPES);

    public static void register() {
        SORTED_TYPES.clear();
        CreateRegistries.FAN_PROCESSING_TYPE.forEach(SORTED_TYPES::add);
        SORTED_TYPES.sort((t1, t2) -> t2.getPriority() - t1.getPriority());
    }
}
