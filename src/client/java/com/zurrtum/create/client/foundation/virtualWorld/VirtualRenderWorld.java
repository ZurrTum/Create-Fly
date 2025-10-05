package com.zurrtum.create.client.foundation.virtualWorld;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationLevel;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.map.MapState;
import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VirtualRenderWorld extends World implements VisualizationLevel {
    protected final World level;
    protected final int minBuildHeight;
    protected final int height;
    protected final Vec3i biomeOffset;

    protected final VirtualChunkSource chunkSource;
    protected final LightingProvider lightEngine;

    protected final Map<BlockPos, BlockState> blockStates = new HashMap<>();
    protected final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
    protected final Object2ShortMap<ChunkSectionPos> nonEmptyBlockCounts = new Object2ShortOpenHashMap<>();

    protected final EntityLookup<Entity> entityGetter = new VirtualLevelEntityGetter<>();

    protected final BlockPos.Mutable scratchPos = new BlockPos.Mutable();

    private int externalPackedLight = 0;

    public VirtualRenderWorld(World level) {
        this(level, Vec3i.ZERO);
    }

    public VirtualRenderWorld(World level, Vec3i biomeOffset) {
        this(level, level.getBottomY(), level.getHeight(), biomeOffset);
    }

    public VirtualRenderWorld(World level, int minBuildHeight, int height, Vec3i biomeOffset) {
        super(
            (MutableWorldProperties) level.getLevelProperties(),
            level.getRegistryKey(),
            level.getRegistryManager(),
            level.getDimensionEntry(),
            true,
            false,
            0,
            0
        );
        this.level = level;
        this.minBuildHeight = nextMultipleOf16(minBuildHeight);
        this.height = nextMultipleOf16(height);
        this.biomeOffset = biomeOffset;

        this.chunkSource = new VirtualChunkSource(this);
        this.lightEngine = new LightingProvider(chunkSource, true, false);
    }

    @Override
    public void setSpawnPoint(WorldProperties.SpawnPoint spawnPoint) {
        level.setSpawnPoint(spawnPoint);
    }

    @Override
    public WorldProperties.SpawnPoint getSpawnPoint() {
        return level.getSpawnPoint();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return level.getWorldBorder();
    }

    /**
     * We need to ensure that height and minBuildHeight are multiples of 16.
     * Adapted from: https://math.stackexchange.com/questions/291468
     */
    public static int nextMultipleOf16(int a) {
        if (a < 0) {
            return -(((Math.abs(a) - 1) | 15) + 1);
        } else {
            return ((a - 1) | 15) + 1;
        }
    }

    /**
     * Set an external light value that will be maxed with any light queries.
     */
    public void setExternalLight(int packedLight) {
        this.externalPackedLight = packedLight;
    }

    /**
     * Reset the external light.
     */
    public void resetExternalLight() {
        this.externalPackedLight = 0;
    }

    @Override
    public int getLightLevel(LightType lightType, BlockPos blockPos) {
        var selfBrightness = super.getLightLevel(lightType, blockPos);

        if (lightType == LightType.SKY) {
            return Math.max(selfBrightness, LightmapTextureManager.getSkyLightCoordinates(externalPackedLight));
        } else {
            return Math.max(selfBrightness, LightmapTextureManager.getBlockLightCoordinates(externalPackedLight));
        }
    }

    public void clear() {
        blockStates.clear();
        blockEntities.clear();

        nonEmptyBlockCounts.forEach((sectionPos, nonEmptyBlockCount) -> {
            if (nonEmptyBlockCount > 0) {
                lightEngine.setSectionStatus(sectionPos, true);
            }
        });

        nonEmptyBlockCounts.clear();

        runLightEngine();
    }

    public void setBlockEntities(Collection<BlockEntity> blockEntities) {
        this.blockEntities.clear();
        blockEntities.forEach(this::addBlockEntity);
    }

    /**
     * Run this after you're done using setBlock().
     */
    public void runLightEngine() {
        Set<ChunkPos> chunkPosSet = new ObjectOpenHashSet<>();
        nonEmptyBlockCounts.object2ShortEntrySet().forEach(entry -> {
            if (entry.getShortValue() > 0) {
                chunkPosSet.add(entry.getKey().toChunkPos());
            }
        });
        for (ChunkPos chunkPos : chunkPosSet) {
            lightEngine.propagateLight(chunkPos);
        }

        lightEngine.doLightUpdates();
    }

    // MEANINGFUL OVERRIDES

    @Override
    public WorldChunk getChunk(int x, int z) {
        throw new UnsupportedOperationException();
    }

    public Chunk actuallyGetChunk(int x, int z) {
        return getChunk(x, z, ChunkStatus.FULL);
    }

    @Override
    public Chunk getChunk(BlockPos pos) {
        return actuallyGetChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags, int recursionLeft) {
        if (isOutOfHeightLimit(pos)) {
            return false;
        }

        pos = pos.toImmutable();

        BlockState oldState = getBlockState(pos);
        if (oldState == newState) {
            return false;
        }

        blockStates.put(pos, newState);

        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        short nonEmptyBlockCount = nonEmptyBlockCounts.getShort(sectionPos);
        boolean prevEmpty = nonEmptyBlockCount == 0;
        if (!oldState.isAir()) {
            --nonEmptyBlockCount;
        }
        if (!newState.isAir()) {
            ++nonEmptyBlockCount;
        }
        nonEmptyBlockCounts.put(sectionPos, nonEmptyBlockCount);
        boolean nowEmpty = nonEmptyBlockCount == 0;

        if (prevEmpty != nowEmpty) {
            lightEngine.setSectionStatus(sectionPos, nowEmpty);
        }

        lightEngine.checkBlock(pos);

        return true;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return lightEngine;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (isOutOfHeightLimit(pos)) {
            return Blocks.VOID_AIR.getDefaultState();
        }
        BlockState state = blockStates.get(pos);
        if (state != null) {
            return state;
        }
        return Blocks.AIR.getDefaultState();
    }

    public BlockState getBlockState(int x, int y, int z) {
        return getBlockState(scratchPos.set(x, y, z));
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (isOutOfHeightLimit(pos)) {
            return Fluids.EMPTY.getDefaultState();
        }
        return getBlockState(pos).getFluidState();
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (!isOutOfHeightLimit(pos)) {
            return blockEntities.get(pos);
        }
        return null;
    }

    @Override
    public void addBlockEntity(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getPos();
        if (!isOutOfHeightLimit(pos)) {
            blockEntities.put(pos, blockEntity);
        }
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        if (!isOutOfHeightLimit(pos)) {
            blockEntities.remove(pos);
        }
    }

    @Override
    public EntityLookup<Entity> getEntityLookup() {
        return entityGetter;
    }

    @Override
    public ChunkManager getChunkManager() {
        return chunkSource;
    }

    @Override
    public int getBottomY() {
        return minBuildHeight;
    }

    @Override
    public int getHeight() {
        return height;
    }

    // BIOME OFFSET

    @Override
    public RegistryEntry<Biome> getBiome(BlockPos pos) {
        return super.getBiome(pos.add(biomeOffset));
    }

    @Override
    public RegistryEntry<Biome> getBiomeForNoiseGen(int x, int y, int z) {
        // Control flow should never reach this method,
        // so we add biomeOffset in case some other mod calls this directly.
        return level.getBiomeForNoiseGen(x + biomeOffset.getX(), y + biomeOffset.getY(), z + biomeOffset.getZ());
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int x, int y, int z) {
        // Control flow should never reach this method,
        // so we add biomeOffset in case some other mod calls this directly.
        return level.getGeneratorStoredBiome(x + biomeOffset.getX(), y + biomeOffset.getY(), z + biomeOffset.getZ());
    }

    @Override
    public int getSeaLevel() {
        return level.getSeaLevel();
    }

    // RENDERING CONSTANTS

    @Override
    public int getLightLevel(BlockPos pos) {
        return 15;
    }

    @Override
    public float getBrightness(Direction direction, boolean shade) {
        return 1f;
    }

    // THIN WRAPPERS

    @Override
    public Scoreboard getScoreboard() {
        return level.getScoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return level.getRecipeManager();
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return level.getBiomeAccess();
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
    public FeatureSet getEnabledFeatures() {
        return level.getEnabledFeatures();
    }

    @Override
    public BrewingRecipeRegistry getBrewingRecipeRegistry() {
        return level.getBrewingRecipeRegistry();
    }

    @Override
    public FuelRegistry getFuelRegistry() {
        return level.getFuelRegistry();
    }

    // ADDITIONAL OVERRRIDES

    @Override
    public void updateComparators(BlockPos pos, Block block) {
    }

    @Override
    public boolean isPosLoaded(BlockPos pos) {
        return true;
    }

    // UNIMPORTANT IMPLEMENTATIONS

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
    }

    @Override
    public void playSound(
        Entity player,
        double x,
        double y,
        double z,
        RegistryEntry<SoundEvent> soundEvent,
        SoundCategory soundSource,
        float volume,
        float pitch,
        long seed
    ) {
    }

    @Override
    public void playSoundFromEntity(
        Entity player,
        Entity entity,
        RegistryEntry<SoundEvent> soundEvent,
        SoundCategory soundSource,
        float volume,
        float pitch,
        long seed
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
    public String asString() {
        return "";
    }

    @Override
    @Nullable
    public Entity getEntityById(int id) {
        return null;
    }

    @Override
    public Collection<EnderDragonPart> getEnderDragonParts() {
        return level.getEnderDragonParts();
    }

    @Override
    public TickManager getTickManager() {
        return level.getTickManager();
    }

    @Override
    @Nullable
    public MapState getMapState(MapIdComponent mapId) {
        return null;
    }

    @Override
    public void setBlockBreakingInfo(int breakerId, BlockPos pos, int progress) {
    }

    @Override
    public void syncWorldEvent(@Nullable Entity player, int type, BlockPos pos, int data) {
    }

    @Override
    public void emitGameEvent(RegistryEntry<GameEvent> gameEvent, Vec3d pos, Emitter context) {
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return Collections.emptyList();
    }

    // Override Starlight's ExtendedWorld interface methods:

    public WorldChunk getChunkAtImmediately(final int chunkX, final int chunkZ) {
        return chunkSource.getWorldChunk(chunkX, chunkZ, false);
    }

    public Chunk getAnyChunkImmediately(final int chunkX, final int chunkZ) {
        return chunkSource.getChunk(chunkX, chunkZ);
    }

    // Intentionally copied from LevelHeightAccessor. Lithium overrides these methods so we need to, too.


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
}
