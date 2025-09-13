package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record SchematicannonOptions(int replaceMode, boolean skipMissing, boolean replaceBlockEntities) {
    public static final Codec<SchematicannonOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("replace_mode").forGetter(SchematicannonOptions::replaceMode),
        Codec.BOOL.fieldOf("skip_missing").forGetter(SchematicannonOptions::skipMissing),
        Codec.BOOL.fieldOf("replace_block_entities").forGetter(SchematicannonOptions::replaceBlockEntities)
    ).apply(i, SchematicannonOptions::new));

    public static final PacketCodec<PacketByteBuf, SchematicannonOptions> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        SchematicannonOptions::replaceMode,
        PacketCodecs.BOOLEAN,
        SchematicannonOptions::skipMissing,
        PacketCodecs.BOOLEAN,
        SchematicannonOptions::replaceBlockEntities,
        SchematicannonOptions::new
    );
}