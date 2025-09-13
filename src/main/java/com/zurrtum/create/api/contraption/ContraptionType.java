package com.zurrtum.create.api.contraption;

import com.mojang.serialization.Codec;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Supplier;

public final class ContraptionType {
    public static final Codec<ContraptionType> CODEC = CreateRegistries.CONTRAPTION_TYPE.getCodec();
    public final Supplier<? extends Contraption> factory;
    public final RegistryEntry.Reference<ContraptionType> holder = CreateRegistries.CONTRAPTION_TYPE.createEntry(this);

    public ContraptionType(Supplier<? extends Contraption> factory) {
        this.factory = factory;
    }

    public boolean is(TagKey<ContraptionType> tag) {
        return this.holder.isIn(tag);
    }
}
