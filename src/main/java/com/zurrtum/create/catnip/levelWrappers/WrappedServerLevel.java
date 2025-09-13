package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class WrappedServerLevel extends ServerWorld {
    protected ServerWorld level;

    public WrappedServerLevel(ServerWorld level) {
        super(
            level.getServer(),
            Util.getMainWorkerExecutor(),
            level.getServer().session,
            (ServerWorldProperties) level.getLevelProperties(),
            level.getRegistryKey(),
            new DimensionOptions(level.getDimensionEntry(), level.getChunkManager().getChunkGenerator()),
            new DummyStatusListener(),
            level.isDebugWorld(),
            level.getBiomeAccess().seed,
            Collections.emptyList(),
            false,
            level.getRandomSequences()
        );
        this.level = level;
    }

    @Override
    public float getSkyAngleRadians(float tickProgress) {
        return 0;
    }

    @Override
    public int getLightLevel(BlockPos pos) {
        return 15;
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        level.updateListeners(pos, oldState, newState, flags);
    }

    @Override
    public void scheduleBlockTick(BlockPos pos, Block block, int delay) {
    }

    @Override
    public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay) {
    }

    @Override
    public void scheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority) {
    }

    @Override
    public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
    }

    @Override
    public void syncWorldEvent(@Nullable Entity source, int eventId, BlockPos pos, int data) {
    }

    @Override
    public List<ServerPlayerEntity> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public void playSound(
        @Nullable Entity source,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundCategory category,
        float volume,
        float pitch
    ) {
    }

    @Override
    public void playSoundFromEntity(@Nullable Entity source, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
    }

    @Override
    public @Nullable Entity getEntityById(int id) {
        return null;
    }

    @Override
    public @Nullable MapState getMapState(MapIdComponent id) {
        return null;
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        entity.setWorld(level);
        return level.spawnEntity(entity);
    }

    @Override
    public void putMapState(MapIdComponent id, MapState state) {
    }

    @Override
    public MapIdComponent increaseAndGetMapId() {
        return new MapIdComponent(0);
    }

    @Override
    public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
    }

    @Override
    public ServerRecipeManager getRecipeManager() {
        return level.getRecipeManager();
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return level.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }
}
