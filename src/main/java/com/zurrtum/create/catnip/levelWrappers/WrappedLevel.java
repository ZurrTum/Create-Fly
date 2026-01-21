package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.core.*;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class WrappedLevel extends Level {
    protected Level level;
    protected @Nullable ChunkSource chunkSource;
    protected LevelEntityGetter<Entity> entityGetter = new DummyLevelEntityGetter<>();

    public WrappedLevel(Level level) {
        super(
            (WritableLevelData) level.getLevelData(),
            level.dimension(),
            level.registryAccess(),
            level.dimensionTypeRegistration(),
            level.isClientSide(),
            level.isDebug(),
            0,
            0
        );
        this.level = level;
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        return level.environmentAttributes();
    }

    @Override
    public void setRespawnData(LevelData.RespawnData spawnPoint) {
        level.setRespawnData(spawnPoint);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return level.getWorldBorder();
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return level.getRespawnData();
    }

    @Override
    public Collection<EnderDragonPart> dragonParts() {
        return level.dragonParts();
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public RandomSource getRandom() {
        return level.getRandom();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return level.getLightEngine();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return level.getBlockState(pos);
    }

    @Override
    public boolean isStateAtPosition(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return level.isStateAtPosition(p_217375_1_, p_217375_2_);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return level.getBlockEntity(pos);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        return level.setBlock(pos, newState, flags);
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
    public LevelTickAccess<Block> getBlockTicks() {
        return level.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return level.getFluidTicks();
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunkSource != null ? chunkSource : level.getChunkSource();
    }

    public void setChunkSource(ChunkSource source) {
        this.chunkSource = source;
    }

    @Override
    public void levelEvent(@Nullable Entity player, int type, BlockPos pos, int data) {
    }

    @Override
    public List<? extends Player> players() {
        return Collections.emptyList();
    }

    @Override
    public void playSeededSound(
        @Nullable Entity pPlayer,
        double pX,
        double pY,
        double pZ,
        Holder<SoundEvent> pSound,
        SoundSource pSource,
        float pVolume,
        float pPitch,
        long pSeed
    ) {
    }

    @Override
    public void playSeededSound(
        @Nullable Entity pPlayer,
        Entity pEntity,
        Holder<SoundEvent> pSound,
        SoundSource pCategory,
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
        SoundSource category,
        float volume,
        float pitch
    ) {
    }

    @Override
    public void playSound(
        @Nullable Entity p_217384_1_,
        Entity p_217384_2_,
        SoundEvent p_217384_3_,
        SoundSource p_217384_4_,
        float p_217384_5_,
        float p_217384_6_
    ) {
    }

    @Override
    public void explode(
        @Nullable Entity entity,
        @Nullable DamageSource damageSource,
        @Nullable ExplosionDamageCalculator behavior,
        double x,
        double y,
        double z,
        float power,
        boolean createFire,
        ExplosionInteraction explosionSourceType,
        ParticleOptions smallParticle,
        ParticleOptions largeParticle,
        WeightedList<ExplosionParticleInfo> blockParticles,
        Holder<SoundEvent> soundEvent
    ) {
        level.explode(
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
    @Nullable
    public Entity getEntity(int id) {
        return null;
    }

    @Override
    public TickRateManager tickRateManager() {
        return level.tickRateManager();
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public boolean addFreshEntity(Entity entityIn) {
        entityIn.setLevel(level);
        return level.addFreshEntity(entityIn);
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
    }

    @Override
    public Scoreboard getScoreboard() {
        return level.getScoreboard();
    }

    @Override
    public RecipeAccess recipeAccess() {
        return level.recipeAccess();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return level.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
    }

    @Override
    public int getSeaLevel() {
        return level.getSeaLevel();
    }

    @Override
    public RegistryAccess registryAccess() {
        return level.registryAccess();
    }

    @Override
    public PotionBrewing potionBrewing() {
        return level.potionBrewing();
    }

    @Override
    public FuelValues fuelValues() {
        return level.fuelValues();
    }

    @Override
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        return level.getShade(p_230487_1_, p_230487_2_);
    }

    @Override
    public void updateNeighbourForOutputSignal(BlockPos p_175666_1_, Block p_175666_2_) {
    }

    @Override
    public void gameEvent(@Nullable Entity entity, Holder<GameEvent> gameEvent, Vec3 pos) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
    }

    @Override
    public String gatherChunkSourceStats() {
        return level.gatherChunkSourceStats();
    }

    @Override
    public LevelEntityGetter<Entity> getEntities() {
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
    public int getMaxY() {
        return this.getMinY() + this.getHeight() - 1;
    }

    @Override
    public int getSectionsCount() {
        return this.getMaxSectionY() - this.getMinSectionY() + 1;
    }

    @Override
    public int getMinSectionY() {
        return SectionPos.blockToSectionCoord(this.getMinY());
    }

    @Override
    public int getMaxSectionY() {
        return SectionPos.blockToSectionCoord(this.getMaxY());
    }

    @Override
    public boolean isInsideBuildHeight(int y) {
        return y >= this.getMinY() && y <= this.getMaxY();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return this.isOutsideBuildHeight(pos.getY());
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return y < this.getMinY() || y > this.getMaxY();
    }

    @Override
    public int getSectionIndex(int y) {
        return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(y));
    }

    @Override
    public int getSectionIndexFromSectionY(int coord) {
        return coord - this.getMinSectionY();
    }

    @Override
    public int getSectionYFromSectionIndex(int index) {
        return index + this.getMinSectionY();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return level.enabledFeatures();
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
