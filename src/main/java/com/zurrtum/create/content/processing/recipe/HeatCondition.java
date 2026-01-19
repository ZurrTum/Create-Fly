package com.zurrtum.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.netty.buffer.ByteBuf;

import java.util.Locale;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum HeatCondition implements StringRepresentable {

    NONE(0xffffffff),
    HEATED(0xFFE88300),
    SUPERHEATED(0xFF5C93E8);

    private final int color;

    public static final Codec<HeatCondition> CODEC = StringRepresentable.fromEnum(HeatCondition::values);
    public static final StreamCodec<ByteBuf, HeatCondition> PACKET_CODEC = CatnipStreamCodecBuilders.ofEnum(HeatCondition.class);

    HeatCondition(int color) {
        this.color = color;
    }

    public boolean testBlazeBurner(HeatLevel level) {
        if (this == SUPERHEATED)
            return level == HeatLevel.SEETHING;
        if (this == HEATED)
            return level != HeatLevel.NONE && level != HeatLevel.SMOULDERING;
        return true;
    }

    public HeatLevel visualizeAsBlazeBurner() {
        if (this == SUPERHEATED)
            return HeatLevel.SEETHING;
        if (this == HEATED)
            return HeatLevel.KINDLED;
        return HeatLevel.NONE;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getTranslationKey() {
        return "recipe.heat_requirement." + getSerializedName();
    }

    public int getColor() {
        return color;
    }


}