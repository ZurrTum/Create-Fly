package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SchematicannonOptions(int replaceMode, boolean skipMissing, boolean replaceBlockEntities) {
    public static final Codec<SchematicannonOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("replace_mode").forGetter(SchematicannonOptions::replaceMode),
        Codec.BOOL.fieldOf("skip_missing").forGetter(SchematicannonOptions::skipMissing),
        Codec.BOOL.fieldOf("replace_block_entities").forGetter(SchematicannonOptions::replaceBlockEntities)
    ).apply(i, SchematicannonOptions::new));

    public static final StreamCodec<FriendlyByteBuf, SchematicannonOptions> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SchematicannonOptions::replaceMode,
        ByteBufCodecs.BOOL,
        SchematicannonOptions::skipMissing,
        ByteBufCodecs.BOOL,
        SchematicannonOptions::replaceBlockEntities,
        SchematicannonOptions::new
    );
}