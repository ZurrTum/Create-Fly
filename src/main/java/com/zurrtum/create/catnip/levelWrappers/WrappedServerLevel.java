package com.zurrtum.create.catnip.levelWrappers;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.ticks.TickPriority;

public class WrappedServerLevel extends ServerLevel {
    protected ServerLevel level;

    public WrappedServerLevel(ServerLevel level) {
        super(
            level.getServer(),
            Util.backgroundExecutor(),
            level.getServer().storageSource,
            (ServerLevelData) level.getLevelData(),
            level.dimension(),
            new LevelStem(level.dimensionTypeRegistration(), level.getChunkSource().getGenerator()),
            level.isDebug(),
            level.getBiomeManager().biomeZoomSeed,
            Collections.emptyList(),
            false,
            level.getRandomSequences()
        );
        this.level = level;
    }

    @Override
    public float getSunAngle(float tickProgress) {
        return 0;
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos pos) {
        return 15;
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        level.sendBlockUpdated(pos, oldState, newState, flags);
    }

    @Override
    public void scheduleTick(BlockPos pos, Block block, int delay) {
    }

    @Override
    public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {
    }

    @Override
    public void scheduleTick(BlockPos pos, Block block, int delay, TickPriority priority) {
    }

    @Override
    public void scheduleTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
    }

    @Override
    public void levelEvent(@Nullable Entity source, int eventId, BlockPos pos, int data) {
    }

    @Override
    public List<ServerPlayer> players() {
        return Collections.emptyList();
    }

    @Override
    public void playSound(
        @Nullable Entity source,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundSource category,
        float volume,
        float pitch
    ) {
    }

    @Override
    public void playSound(@Nullable Entity source, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return null;
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId id) {
        return null;
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        entity.setLevel(level);
        return level.addFreshEntity(entity);
    }

    @Override
    public void setMapData(MapId id, MapItemSavedData state) {
    }

    @Override
    public MapId getFreeMapId() {
        return new MapId(0);
    }

    @Override
    public void destroyBlockProgress(int entityId, BlockPos pos, int progress) {
    }

    @Override
    public RecipeManager recipeAccess() {
        return level.recipeAccess();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return level.getUncachedNoiseBiome(biomeX, biomeY, biomeZ);
    }
}
