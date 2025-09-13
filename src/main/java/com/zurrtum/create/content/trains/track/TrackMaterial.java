package com.zurrtum.create.content.trains.track;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.Create;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TrackMaterial implements ItemConvertible {
    public static final Map<Identifier, TrackMaterial> ALL = new HashMap<>();
    public static final Codec<TrackMaterial> CODEC = Identifier.CODEC.xmap(TrackMaterial::fromId, TrackMaterial::getId);
    public static final PacketCodec<ByteBuf, TrackMaterial> PACKET_CODEC = Identifier.PACKET_CODEC.xmap(TrackMaterial::fromId, TrackMaterial::getId);

    private final Identifier id;
    private final Supplier<TrackBlock> trackBlock;
    public Object modelHolder;

    @SuppressWarnings("unchecked")
    public <T> T getModelHolder() {
        return (T) modelHolder;
    }

    public TrackMaterial(Identifier id, Supplier<TrackBlock> trackBlock) {
        this.id = id;
        this.trackBlock = trackBlock;
        ALL.put(this.id, this);
    }

    public Identifier getId() {
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

    public static TrackMaterial fromId(Identifier id) {
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