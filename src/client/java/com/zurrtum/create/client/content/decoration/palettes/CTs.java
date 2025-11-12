package com.zurrtum.create.client.content.decoration.palettes;

import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShifter;
import com.zurrtum.create.client.foundation.block.connected.CTType;

import java.util.function.Function;

import net.minecraft.resources.Identifier;

public enum CTs {
    PILLAR(AllCTTypes.RECTANGLE, s -> toLocation(s, "pillar")),
    CAP(AllCTTypes.OMNIDIRECTIONAL, s -> toLocation(s, "cap")),
    LAYERED(AllCTTypes.HORIZONTAL_KRYPPERS, s -> toLocation(s, "layered"));

    public final CTType type;
    private final Function<String, Identifier> srcFactory;
    private final Function<String, Identifier> targetFactory;

    CTs(CTType type, Function<String, Identifier> factory) {
        this(type, factory, factory);
    }

    CTs(CTType type, Function<String, Identifier> srcFactory, Function<String, Identifier> targetFactory) {
        this.type = type;
        this.srcFactory = srcFactory;
        this.targetFactory = targetFactory;
    }

    public CTSpriteShiftEntry get(String variant) {
        Identifier resLoc = srcFactory.apply(variant);
        Identifier resLocTarget = targetFactory.apply(variant);
        return CTSpriteShifter.getCT(type, resLoc, resLocTarget.withSuffix("_connected"));
    }

    private static Identifier toLocation(String variant, String texture) {
        return Create.asResource(String.format("block/palettes/stone_types/%s/%s", texture, variant + "_cut_" + texture));
    }
}