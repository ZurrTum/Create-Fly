package com.zurrtum.create.impl.registry;

import com.zurrtum.create.api.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TagProviderImpl<K, V> implements SimpleRegistry.Provider<K, V> {
    private final TagKey<K> tag;
    private final Function<K, Holder<K>> holderGetter;
    private final V value;

    public TagProviderImpl(TagKey<K> tag, Function<K, Holder<K>> holderGetter, V value) {
        this.tag = tag;
        this.holderGetter = holderGetter;
        this.value = value;
    }

    // eye of the beholder? check the nametag, buddy
    public static Holder<BlockEntityType<?>> getBeHolder(BlockEntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (key == null)
            throw new IllegalStateException("Unregistered BlockEntityType: " + type);

        return BuiltInRegistries.BLOCK_ENTITY_TYPE.get(key).orElseThrow();
    }

    @Override
    @Nullable
    public V get(K object) {
        Holder<K> holder = this.holderGetter.apply(object);
        return holder.is(this.tag) ? this.value : null;
    }

    @Override
    public void onRegister(Runnable invalidate) {
        //TODO
        //        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
        //            if (event.shouldUpdateStaticData()) {
        //                invalidate.run();
        //            }
        //        });
    }
}
