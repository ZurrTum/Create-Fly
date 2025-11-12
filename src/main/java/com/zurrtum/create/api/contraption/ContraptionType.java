package com.zurrtum.create.api.contraption;

import com.mojang.serialization.Codec;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.contraptions.Contraption;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

public final class ContraptionType {
    public static final Codec<ContraptionType> CODEC = CreateRegistries.CONTRAPTION_TYPE.byNameCodec();
    public final Supplier<? extends Contraption> factory;
    public final Holder.Reference<ContraptionType> holder = CreateRegistries.CONTRAPTION_TYPE.createIntrusiveHolder(this);

    public ContraptionType(Supplier<? extends Contraption> factory) {
        this.factory = factory;
    }

    public boolean is(TagKey<ContraptionType> tag) {
        return this.holder.is(tag);
    }
}
