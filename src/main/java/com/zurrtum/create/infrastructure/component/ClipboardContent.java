package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;
import java.util.Optional;

public record ClipboardContent(
    ClipboardType type, List<List<ClipboardEntry>> pages, boolean readOnly, int previouslyOpenedPage, Optional<NbtCompound> copiedValues
) {
    public static final ClipboardContent EMPTY = new ClipboardContent(ClipboardType.EMPTY, List.of(), false, 0, Optional.empty());

    public static final Codec<List<List<ClipboardEntry>>> PAGES_CODEC = ClipboardEntry.CODEC.listOf().listOf();
    public static final PacketCodec<RegistryByteBuf, List<List<ClipboardEntry>>> PAGES_STREAM_CODEC = ClipboardEntry.STREAM_CODEC.collect(PacketCodecs.toList())
        .collect(PacketCodecs.toList());

    public static final Codec<ClipboardContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ClipboardType.CODEC.fieldOf("type").forGetter(ClipboardContent::type),
        PAGES_CODEC.fieldOf("pages").forGetter(ClipboardContent::pages),
        Codec.BOOL.fieldOf("read_only").forGetter(ClipboardContent::readOnly),
        Codec.INT.fieldOf("previously_opened_page").forGetter(ClipboardContent::previouslyOpenedPage),
        NbtCompound.CODEC.optionalFieldOf("copied_values").forGetter(ClipboardContent::copiedValues)
    ).apply(instance, ClipboardContent::new));

    public static final PacketCodec<RegistryByteBuf, ClipboardContent> STREAM_CODEC = PacketCodec.tuple(
        ClipboardType.STREAM_CODEC,
        ClipboardContent::type,
        PAGES_STREAM_CODEC,
        ClipboardContent::pages,
        PacketCodecs.BOOLEAN,
        ClipboardContent::readOnly,
        PacketCodecs.VAR_INT,
        ClipboardContent::previouslyOpenedPage,
        PacketCodecs.optional(PacketCodecs.NBT_COMPOUND),
        ClipboardContent::copiedValues,
        ClipboardContent::new
    );

    public ClipboardContent(ClipboardType type, List<List<ClipboardEntry>> pages, boolean readOnly) {
        this(type, pages, readOnly, 0, Optional.empty());
    }

    public ClipboardContent setType(ClipboardType type) {
        return new ClipboardContent(type, this.pages, this.readOnly, this.previouslyOpenedPage, this.copiedValues);
    }

    public ClipboardContent setPages(List<List<ClipboardEntry>> pages) {
        return new ClipboardContent(this.type, pages, this.readOnly, this.previouslyOpenedPage, this.copiedValues);
    }

    public ClipboardContent setReadOnly(boolean readOnly) {
        return new ClipboardContent(this.type, this.pages, readOnly, this.previouslyOpenedPage, this.copiedValues);
    }

    public ClipboardContent setPreviouslyOpenedPage(int previouslyOpenedPage) {
        return new ClipboardContent(this.type, this.pages, this.readOnly, previouslyOpenedPage, this.copiedValues);
    }

    public ClipboardContent setCopiedValues(NbtCompound copiedValues) {
        return new ClipboardContent(this.type, this.pages, this.readOnly, this.previouslyOpenedPage, Optional.of(copiedValues));
    }
}
