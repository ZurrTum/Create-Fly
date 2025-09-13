package com.zurrtum.create.client;

import com.zurrtum.create.client.content.trains.entity.TrainIcon;
import com.zurrtum.create.content.trains.entity.TrainIconType;
import net.minecraft.util.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;

public class AllTrainIcons {
    public static final Map<TrainIconType, TrainIcon> ALL = new IdentityHashMap<>();
    public static final TrainIcon TRADITIONAL = register(TrainIconType.TRADITIONAL, TrainIcon.ASSEMBLE, 2, 205);
    public static final TrainIcon ELECTRIC = register(TrainIconType.ELECTRIC, TrainIcon.ASSEMBLE, 2, 216);
    public static final TrainIcon MODERN = register(TrainIconType.MODERN, TrainIcon.ASSEMBLE, 2, 227);

    public static TrainIcon byType(TrainIconType type) {
        return ALL.getOrDefault(type, TRADITIONAL);
    }

    public static TrainIcon register(TrainIconType type, Identifier sheet, int x, int y) {
        TrainIcon icon = new TrainIcon(type, sheet, x, y);
        ALL.put(type, icon);
        return icon;
    }

    public static void register() {
    }
}
