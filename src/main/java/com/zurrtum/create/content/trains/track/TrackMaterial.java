package com.zurrtum.create.content.trains.track;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.Create;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class TrackMaterial implements ItemLike {
    public static final Map<ResourceLocation, TrackMaterial> ALL = new HashMap<>();
    public static final Codec<TrackMaterial> CODEC = ResourceLocation.CODEC.xmap(TrackMaterial::fromId, TrackMaterial::getId);
    public static final StreamCodec<ByteBuf, TrackMaterial> PACKET_CODEC = ResourceLocation.STREAM_CODEC.map(TrackMaterial::fromId, TrackMaterial::getId);

    private final ResourceLocation id;
    private final Supplier<TrackBlock> trackBlock;
    public Object modelHolder;

    @SuppressWarnings("unchecked")
    public <T> T getModelHolder() {
        return (T) modelHolder;
    }

    public TrackMaterial(ResourceLocation id, Supplier<TrackBlock> trackBlock) {
        this.id = id;
        this.trackBlock = trackBlock;
        ALL.put(this.id, this);
    }

    public ResourceLocation getId() {
        return id;
    }

    public TrackBlock getBlock() {
        return trackBlock.get();
    }

    @Override
    public Item asItem() {
        return trackBlock.get().asItem();
    }

    public static Block[] allBlocks() {
        return ALL.values().stream().map(TrackMaterial::getBlock).toArray(Block[]::new);
    }

    public static TrackMaterial fromId(ResourceLocation id) {
        if (ALL.containsKey(id))
            return ALL.get(id);

        Create.LOGGER.error("Failed to locate serialized track material: {}", id);
        return AllTrackMaterials.ANDESITE;
    }

    public static TrackMaterial fromItem(Item item) {
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ITrackBlock trackBlock)
            return trackBlock.getMaterial();
        return AllTrackMaterials.ANDESITE;
    }
}