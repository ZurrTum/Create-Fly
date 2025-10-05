package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.map.MapState;
import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class WrappedLevel extends World {
    protected World level;
    protected ChunkManager chunkSource;
    protected EntityLookup<Entity> entityGetter = new DummyLevelEntityGetter<>();

    public WrappedLevel(World level) {
        super(
            (MutableWorldProperties) level.getLevelProperties(),
            level.getRegistryKey(),
            level.getRegistryManager(),
            level.getDimensionEntry(),
            level.isClient(),
            level.isDebugWorld(),
            0,
            0
        );
        this.level = level;
    }

    @Override
    public void setSpawnPoint(WorldProperties.SpawnPoint spawnPoint) {
        level.setSpawnPoint(spawnPoint);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return level.getWorldBorder();
    }

    @Override
    public WorldProperties.SpawnPoint getSpawnPoint() {
        return level.getSpawnPoint();
    }

    public Collection<EnderDragonPart> getEnderDragonParts() {
        return level.getEnderDragonParts();
    }

    public World getLevel() {
        return level;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return level.getLightingProvider();
    }

    @Override
    public BlockState getBlockState(@Nullable BlockPos pos) {
        return level.getBlockState(pos);
    }

    @Override
    public boolean testBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return level.testBlockState(p_217375_1_, p_217375_2_);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return level.getBlockEntity(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        return level.setBlockState(pos, newState, flags);
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
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return level.getBlockTickScheduler();
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return level.getFluidTickScheduler();
    }

    @Override
    public ChunkManager getChunkManager() {
        return chunkSource != null ? chunkSource : level.getChunkManager();
    }

    public void setChunkSource(ChunkManager source) {
        this.chunkSource = source;
    }

    @Override
    public void syncWorldEvent(@Nullable Entity player, int type, BlockPos pos, int data) {
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public void playSound(
        Entity pPlayer,
        double pX,
        double pY,
        double pZ,
        RegistryEntry<SoundEvent> pSound,
        SoundCategory pSource,
        float pVolume,
        float pPitch,
        long pSeed
    ) {
    }

    @Override
    public void playSoundFromEntity(
        Entity pPlayer,
        Entity pEntity,
        RegistryEntry<SoundEvent> pSound,
        SoundCategory pCategory,
        float pVolume,
        float pPitch,
        long pSeed
    ) {
    }

    @Override
    public void playSound(
        @Nullable Entity player,
        double x,
        double y,
        double z,
        SoundEvent soundIn,
        SoundCategory category,
        float volume,
        float pitch
    ) {
    }

    @Override
    public void playSoundFromEntity(
        @Nullable Entity p_217384_1_,
        Entity p_217384_2_,
        SoundEvent p_217384_3_,
        SoundCategory p_217384_4_,
        float p_217384_5_,
        float p_217384_6_
    ) {
    }

    @Override
    public void createExplosion(
        @Nullable Entity entity,
        @Nullable DamageSource damageSource,
        @Nullable ExplosionBehavior behavior,
        double x,
        double y,
        double z,
        float power,
        boolean createFire,
        ExplosionSourceType explosionSourceType,
        ParticleEffect smallParticle,
        ParticleEffect largeParticle,
        Pool<BlockParticleEffect> blockParticles,
        RegistryEntry<SoundEvent> soundEvent
    ) {
        level.createExplosion(
            entity,
            damageSource,
            behavior,
            x,
            y,
            z,
            power,
            createFire,
            explosionSourceType,
            smallParticle,
            largeParticle,
            blockParticles,
            soundEvent
        );
    }

    @Override
    public Entity getEntityById(int id) {
        return null;
    }

    @Override
    public TickManager getTickManager() {
        return level.getTickManager();
    }

    @Nullable
    @Override
    public MapState getMapState(MapIdComponent mapId) {
        return null;
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        entityIn.setWorld(level);
        return level.spawnEntity(entityIn);
    }

    @Override
    public void setBlockBreakingInfo(int breakerId, BlockPos pos, int progress) {
    }

    @Override
    public Scoreboard getScoreboard() {
        return level.getScoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return level.getRecipeManager();
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return level.getGeneratorStoredBiome(p_225604_1_, p_225604_2_, p_225604_3_);
    }

    @Override
    public int getSeaLevel() {
        return level.getSeaLevel();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return level.getRegistryManager();
    }

    @Override
    public BrewingRecipeRegistry getBrewingRecipeRegistry() {
        return level.getBrewingRecipeRegistry();
    }

    @Override
    public FuelRegistry getFuelRegistry() {
        return level.getFuelRegistry();
    }

    @Override
    public float getBrightness(Direction p_230487_1_, boolean p_230487_2_) {
        return level.getBrightness(p_230487_1_, p_230487_2_);
    }

    @Override
    public void updateComparators(BlockPos p_175666_1_, Block p_175666_2_) {
    }

    @Override
    public void emitGameEvent(@Nullable Entity entity, RegistryEntry<GameEvent> gameEvent, Vec3d pos) {
    }

    @Override
    public void emitGameEvent(RegistryEntry<GameEvent> holder, Vec3d vec3, GameEvent.Emitter context) {
    }

    @Override
    public String asString() {
        return level.asString();
    }

    @Override
    public EntityLookup<Entity> getEntityLookup() {
        return entityGetter;
    }

    // Intentionally copied from LevelHeightAccessor. Workaround for issues caused
    // when other mods (such as Lithium)
    // override the vanilla implementations in ways which cause WrappedWorlds to
    // return incorrect, default height info.
    // WrappedWorld subclasses should implement their own getMinBuildHeight and
    // getHeight overrides where they deviate
    // from the defaults for their dimension.

    @Override
    public int getTopYInclusive() {
        return this.getBottomY() + this.getHeight() - 1;
    }

    @Override
    public int countVerticalSections() {
        return this.getTopSectionCoord() - this.getBottomSectionCoord() + 1;
    }

    @Override
    public int getBottomSectionCoord() {
        return ChunkSectionPos.getSectionCoord(this.getBottomY());
    }

    @Override
    public int getTopSectionCoord() {
        return ChunkSectionPos.getSectionCoord(this.getTopYInclusive());
    }

    @Override
    public boolean isInHeightLimit(int y) {
        return y >= this.getBottomY() && y <= this.getTopYInclusive();
    }

    @Override
    public boolean isOutOfHeightLimit(BlockPos pos) {
        return this.isOutOfHeightLimit(pos.getY());
    }

    @Override
    public boolean isOutOfHeightLimit(int y) {
        return y < this.getBottomY() || y > this.getTopYInclusive();
    }

    @Override
    public int getSectionIndex(int y) {
        return this.sectionCoordToIndex(ChunkSectionPos.getSectionCoord(y));
    }

    @Override
    public int sectionCoordToIndex(int coord) {
        return coord - this.getBottomSectionCoord();
    }

    @Override
    public int sectionIndexToCoord(int index) {
        return index + this.getBottomSectionCoord();
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return level.getEnabledFeatures();
    }

    public float getDayTimeFraction() {
        return 0;
    }

    // Neo's patched methods
    public void setDayTimeFraction(float var1) {
    }

    public float getDayTimePerTick() {
        return 0;
    }

    public void setDayTimePerTick(float var1) {
    }
}
