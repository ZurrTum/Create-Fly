package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

public class SchematicChunkSource extends ChunkManager {
    private final World fallbackWorld;

    public SchematicChunkSource(World world) {
        fallbackWorld = world;
    }

    @Nullable
    @Override
    public Chunk getChunk(int x, int z) {
        return new EmptierChunk(fallbackWorld);
    }

    @Override
    public World getWorld() {
        return fallbackWorld;
    }

    @Nullable
    @Override
    public Chunk getChunk(int x, int z, ChunkStatus status, boolean p_212849_4_) {
        return getChunk(x, z);
    }

    @Override
    public String getDebugString() {
        return "WrappedChunkProvider";
    }

    @Override
    public LightingProvider getLightingProvider() {
        return fallbackWorld.getLightingProvider();
    }

    @Override
    public void tick(BooleanSupplier p_202162_, boolean p_202163_) {
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    public static class EmptierChunk extends WorldChunk {

        private static final class DummyLevel extends World {
            private DummyLevel(
                MutableWorldProperties pLevelData,
                RegistryKey<World> pDimension,
                DynamicRegistryManager pRegistryAccess,
                RegistryEntry<DimensionType> pDimensionTypeRegistration,
                boolean pIsClientSide,
                boolean pIsDebug,
                long pBiomeZoomSeed,
                int pMaxChainedNeighborUpdates
            ) {
                super(
                    pLevelData,
                    pDimension,
                    pRegistryAccess,
                    pDimensionTypeRegistration,
                    pIsClientSide,
                    pIsDebug,
                    pBiomeZoomSeed,
                    pMaxChainedNeighborUpdates
                );
                access = pRegistryAccess;
            }

            private final DynamicRegistryManager access;

            private DummyLevel(World level) {
                this(null, null, level.getRegistryManager(), level.getDimensionEntry(), false, false, 0, 0);
            }

            @Override
            public ChunkManager getChunkManager() {
                return null;
            }

            @Override
            public void syncWorldEvent(Entity pPlayer, int pType, BlockPos pPos, int pData) {
            }

            @Override
            public void emitGameEvent(@Nullable Entity entity, RegistryEntry<GameEvent> gameEvent, Vec3d pos) {
            }

            @Override
            public void emitGameEvent(RegistryEntry<GameEvent> holder, Vec3d vec3, GameEvent.Emitter context) {
            }

            @Override
            public DynamicRegistryManager getRegistryManager() {
                return access;
            }

            @Override
            public BrewingRecipeRegistry getBrewingRecipeRegistry() {
                return null;
            }

            @Override
            public FuelRegistry getFuelRegistry() {
                return null;
            }

            @Override
            public List<? extends PlayerEntity> getPlayers() {
                return null;
            }

            @Override
            public RegistryEntry<Biome> getGeneratorStoredBiome(int pX, int pY, int pZ) {
                return null;
            }

            @Override
            public int getSeaLevel() {
                return 63;
            }

            @Override
            public float getBrightness(Direction pDirection, boolean pShade) {
                return 0;
            }

            @Override
            public void updateListeners(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
            }

            @Override
            public void playSound(
                Entity pPlayer,
                double pX,
                double pY,
                double pZ,
                SoundEvent pSound,
                SoundCategory pCategory,
                float pVolume,
                float pPitch
            ) {
            }

            @Override
            public void playSoundFromEntity(Entity pPlayer, Entity pEntity, SoundEvent pEvent, SoundCategory pCategory, float pVolume, float pPitch) {
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
            public void playSound(
                Entity p_220363_,
                double p_220364_,
                double p_220365_,
                double p_220366_,
                SoundEvent p_220367_,
                SoundCategory p_220368_,
                float p_220369_,
                float p_220370_,
                long p_220371_
            ) {
            }

            @Override
            public void playSoundFromEntity(
                Entity p_220372_,
                Entity p_220373_,
                RegistryEntry<SoundEvent> p_220374_,
                SoundCategory p_220375_,
                float p_220376_,
                float p_220377_,
                long p_220378_
            ) {
            }

            @Override
            public String asString() {
                return null;
            }

            @Override
            public Entity getEntityById(int pId) {
                return null;
            }

            @Override
            public Collection<EnderDragonPart> getEnderDragonParts() {
                return List.of();
            }

            @Nullable
            @Override
            public MapState getMapState(MapIdComponent mapId) {
                return null;
            }

            @Override
            public void setBlockBreakingInfo(int pBreakerId, BlockPos pPos, int pProgress) {
            }

            @Override
            public Scoreboard getScoreboard() {
                return null;
            }

            @Override
            public RecipeManager getRecipeManager() {
                return null;
            }

            @Override
            public EntityLookup<Entity> getEntityLookup() {
                return null;
            }

            @Override
            public QueryableTickScheduler<Block> getBlockTickScheduler() {
                return EmptyTickSchedulers.getClientTickScheduler();
            }

            @Override
            public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
                return EmptyTickSchedulers.getClientTickScheduler();
            }

            @Override
            public FeatureSet getEnabledFeatures() {
                return FeatureSet.empty();
            }

            @Override
            public TickManager getTickManager() {
                return null;
            }
        }

        public EmptierChunk(World level) {
            super(new DummyLevel(level), ChunkPos.ORIGIN);
        }

        @Override
        public BlockState getBlockState(BlockPos p_180495_1_) {
            return Blocks.VOID_AIR.getDefaultState();
        }

        @Nullable
        public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_) {
            return null;
        }

        @Override
        public FluidState getFluidState(BlockPos p_204610_1_) {
            return Fluids.EMPTY.getDefaultState();
        }

        @Override
        public int getLuminance(BlockPos p_217298_1_) {
            return 0;
        }

        @Nullable
        public BlockEntity getBlockEntity(BlockPos p_177424_1_, CreationType p_177424_2_) {
            return null;
        }

        @Override
        public void addBlockEntity(BlockEntity p_150813_1_) {
        }

        @Override
        public void setBlockEntity(BlockEntity p_177426_2_) {
        }

        @Override
        public void removeBlockEntity(BlockPos p_177425_1_) {
        }

        public void markUnsaved() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean areSectionsEmptyBetween(int p_76606_1_, int p_76606_2_) {
            return true;
        }

        @Override
        public ChunkLevelType getLevelType() {
            return ChunkLevelType.FULL;
        }
    }
}
