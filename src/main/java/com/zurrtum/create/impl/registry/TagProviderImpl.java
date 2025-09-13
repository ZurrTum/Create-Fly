package com.zurrtum.create.impl.registry;

import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TagProviderImpl<K, V> implements SimpleRegistry.Provider<K, V> {
    private final TagKey<K> tag;
    private final Function<K, RegistryEntry<K>> holderGetter;
    private final V value;

    public TagProviderImpl(TagKey<K> tag, Function<K, RegistryEntry<K>> holderGetter, V value) {
        this.tag = tag;
        this.holderGetter = holderGetter;
        this.value = value;
    }

    // eye of the beholder? check the nametag, buddy
    public static RegistryEntry<BlockEntityType<?>> getBeHolder(BlockEntityType<?> type) {
        Identifier key = Registries.BLOCK_ENTITY_TYPE.getId(type);
        if (key == null)
            throw new IllegalStateException("Unregistered BlockEntityType: " + type);

        return Registries.BLOCK_ENTITY_TYPE.getEntry(key).orElseThrow();
    }

    @Override
    @Nullable
    public V get(K object) {
        RegistryEntry<K> holder = this.holderGetter.apply(object);
        return holder.isIn(this.tag) ? this.value : null;
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
