package com.zurrtum.create.client.foundation.virtualWorld;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class VirtualChunkSource extends ChunkManager {
    private final VirtualRenderWorld world;
    private final Long2ObjectMap<VirtualChunk> chunks = new Long2ObjectOpenHashMap<>();

    public VirtualChunkSource(VirtualRenderWorld world) {
        this.world = world;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Chunk getChunk(int x, int z) {
        return chunks.computeIfAbsent(
            ChunkPos.toLong(x, z),
            packedPos -> new VirtualChunk(world, ChunkPos.getPackedX(packedPos), ChunkPos.getPackedZ(packedPos))
        );
    }

    @Override
    @Nullable
    public WorldChunk getWorldChunk(int x, int z, boolean load) {
        return null;
    }

    @Override
    @Nullable
    public Chunk getChunk(int x, int z, ChunkStatus status, boolean load) {
        return getChunk(x, z);
    }

    @Override
    public void tick(BooleanSupplier hasTimeLeft, boolean tickChunks) {
    }

    @Override
    public String getDebugString() {
        return "VirtualChunkSource";
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return world.getLightingProvider();
    }
}
