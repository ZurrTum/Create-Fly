package com.zurrtum.create.client.ponder.api.level;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.PonderWorldParticles;
import com.zurrtum.create.client.ponder.foundation.level.PonderChunk;
import com.zurrtum.create.ponder.api.VirtualBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PonderLevel extends SchematicLevel {

    @Nullable
    public PonderScene scene;

    protected Map<BlockPos, BlockState> originalBlocks;
    protected Map<BlockPos, CompoundTag> originalBlockEntities;
    protected Map<BlockPos, Integer> blockBreakingProgressions;
    protected List<Entity> originalEntities;
    @Nullable
    private Long2ObjectMap<PonderChunk> chunks;

    protected PonderWorldParticles particles;

    int overrideLight;
    @Nullable Selection mask;
    boolean currentlyTickingEntities;

    public PonderLevel(BlockPos anchor, Level original) {
        super(anchor, original);
        originalBlocks = new HashMap<>();
        originalBlockEntities = new HashMap<>();
        blockBreakingProgressions = new HashMap<>();
        originalEntities = new ArrayList<>();
        particles = new PonderWorldParticles(this);
        renderMode = true;
    }

    public void createBackup() {
        originalBlocks.clear();
        originalBlockEntities.clear();
        originalBlocks.putAll(blocks);
        RegistryAccess registryManager = registryAccess();
        blockEntities.forEach((k, v) -> originalBlockEntities.put(k, v.saveWithFullMetadata(registryManager)));
        entities.forEach(e -> {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(e.problemPath(), Ponder.LOGGER)) {
                TagValueOutput writeView = TagValueOutput.createWithContext(logging, registryManager);
                e.save(writeView);
                ValueInput readView = TagValueInput.create(logging, registryManager, writeView.buildResult());
                EntityType.create(readView, this, EntitySpawnReason.LOAD).ifPresent(originalEntities::add);
            }
        });
    }

    public void restore() {
        entities.clear();
        blocks.clear();
        blockEntities.clear();
        blockBreakingProgressions.clear();
        renderedBlockEntities.clear();
        blocks.putAll(originalBlocks);
        RegistryAccess registryManager = registryAccess();
        originalBlockEntities.forEach((k, v) -> {
            BlockEntity blockEntity = BlockEntity.loadStatic(k, originalBlocks.get(k), v, registryManager);
            onBEAdded(blockEntity, blockEntity.getBlockPos());
            blockEntities.put(k, blockEntity);
            renderedBlockEntities.add(blockEntity);
        });
        originalEntities.forEach(e -> {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(e.problemPath(), Ponder.LOGGER)) {
                TagValueOutput writeView = TagValueOutput.createWithContext(logging, registryManager);
                e.save(writeView);
                ValueInput readView = TagValueInput.create(logging, registryManager, writeView.buildResult());
                EntityType.create(readView, this, EntitySpawnReason.LOAD).ifPresent(entities::add);
            }
        });
        particles.clearEffects();

        PonderIndex.forEachPlugin(plugin -> plugin.onPonderLevelRestore(this));
    }

    public void restoreBlocks(Selection selection) {
        selection.forEach(p -> {
            if (originalBlocks.containsKey(p))
                blocks.put(p, originalBlocks.get(p));
            if (originalBlockEntities.containsKey(p)) {
                BlockEntity blockEntity = BlockEntity.loadStatic(p, originalBlocks.get(p), originalBlockEntities.get(p), registryAccess());
                if (blockEntity != null) {
                    onBEAdded(blockEntity, blockEntity.getBlockPos());
                    blockEntities.put(p, blockEntity);
                }
            }
        });
        redraw();
    }

    private void redraw() {
        if (scene != null)
            scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
    }

    public void pushFakeLight(int light) {
        this.overrideLight = light;
    }

    public void popLight() {
        this.overrideLight = -1;
    }

    @Override
    public int getBrightness(LightLayer p_226658_1_, BlockPos p_226658_2_) {
        return overrideLight == -1 ? 15 : overrideLight;
    }

    public void setMask(@Nullable Selection mask) {
        this.mask = mask;
    }

    public void clearMask() {
        this.mask = null;
    }

    @Override
    public BlockState getBlockState(BlockPos globalPos) {
        if (mask != null && !mask.test(globalPos.subtract(anchor)))
            return Blocks.AIR.defaultBlockState();
        if (currentlyTickingEntities && globalPos.getY() < 0)
            return Blocks.AIR.defaultBlockState();
        return super.getBlockState(globalPos);
    }

    @Override // For particle collision
    public BlockGetter getChunkForCollisions(int p_225522_1_, int p_225522_2_) {
        return this;
    }

    public void renderEntities(PoseStack ms, SubmitNodeCollector queue, Camera ari, CameraRenderState cameraRenderState, float pt) {
        Vec3 Vector3d = ari.getPosition();
        double d0 = Vector3d.x();
        double d1 = Vector3d.y();
        double d2 = Vector3d.z();

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        for (Entity entity : entities) {
            if (entity.tickCount == 0) {
                entity.xOld = entity.getX();
                entity.yOld = entity.getY();
                entity.zOld = entity.getZ();
            }
            renderEntity(renderManager, entity, cameraRenderState, d0, d1, d2, pt, ms, queue);
        }
    }

    private void renderEntity(
        EntityRenderDispatcher renderManager,
        Entity entity,
        CameraRenderState cameraRenderState,
        double x,
        double y,
        double z,
        float pt,
        PoseStack ms,
        SubmitNodeCollector queue
    ) {
        EntityRenderState state = renderManager.extractEntity(entity, pt);
        renderManager.submit(state, cameraRenderState, state.x - x, state.y - y, state.z - z, ms, queue);
    }

    public void renderParticles(PoseStack ms, SubmitNodeStorage queue, Camera ari, CameraRenderState cameraRenderState, float pt) {
        particles.renderParticles(ms, queue, ari, cameraRenderState, pt);
    }

    public void tick() {
        currentlyTickingEntities = true;

        particles.tick();

        for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();

            entity.tickCount++;
            entity.xOld = entity.getX();
            entity.yOld = entity.getY();
            entity.zOld = entity.getZ();
            entity.tick();

            if (entity.getY() <= -.5f)
                entity.discard();

            if (!entity.isAlive())
                iterator.remove();
        }

        currentlyTickingEntities = false;
    }

    @Override
    public void addParticle(ParticleOptions data, double x, double y, double z, double mx, double my, double mz) {
        particles.addParticle(data, x, y, z, mx, my, mz);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions data, double x, double y, double z, double mx, double my, double mz) {
        addParticle(data, x, y, z, mx, my, mz);
    }

    public void addParticle(@Nullable Particle p) {
        if (p != null)
            particles.addParticle(p);
    }

    protected void onBEAdded(BlockEntity blockEntity, BlockPos pos) {
        super.onBEadded(blockEntity, pos);
        if (!(blockEntity instanceof VirtualBlockEntity virtualBlockEntity))
            return;
        virtualBlockEntity.markVirtual();
    }

    public void setBlockBreakingProgress(BlockPos pos, int damage) {
        if (damage == 0)
            blockBreakingProgressions.remove(pos);
        else
            blockBreakingProgressions.put(pos, damage - 1);
    }

    public Map<BlockPos, Integer> getBlockBreakingProgressions() {
        return blockBreakingProgressions;
    }

    public void addBlockDestroyEffects(BlockPos pos, BlockState state) {
        VoxelShape voxelshape = state.getShape(this, pos);
        if (voxelshape.isEmpty())
            return;

        AABB bb = voxelshape.bounds();
        double d1 = Math.min(1.0D, bb.maxX - bb.minX);
        double d2 = Math.min(1.0D, bb.maxY - bb.minY);
        double d3 = Math.min(1.0D, bb.maxZ - bb.minZ);
        int i = Math.max(2, Mth.ceil(d1 / 0.25D));
        int j = Math.max(2, Mth.ceil(d2 / 0.25D));
        int k = Math.max(2, Mth.ceil(d3 / 0.25D));

        for (int l = 0; l < i; ++l) {
            for (int i1 = 0; i1 < j; ++i1) {
                for (int j1 = 0; j1 < k; ++j1) {
                    double d4 = (l + 0.5D) / i;
                    double d5 = (i1 + 0.5D) / j;
                    double d6 = (j1 + 0.5D) / k;
                    double d7 = d4 * d1 + bb.minX;
                    double d8 = d5 * d2 + bb.minY;
                    double d9 = d6 * d3 + bb.minZ;
                    addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        pos.getX() + d7,
                        pos.getY() + d8,
                        pos.getZ() + d9,
                        d4 - 0.5D,
                        d5 - 0.5D,
                        d6 - 0.5D
                    );
                }
            }
        }
    }

    @Override
    protected BlockState processBlockStateForPrinting(BlockState state) {
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasChunkAt(BlockPos pos) {
        return true; // fix particle lighting
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasChunkAt(int x, int y) {
        return true; // fix particle lighting
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return true; // fix particle lighting
    }

    @Override
    public boolean hasNearbyAlivePlayer(double p_217358_1_, double p_217358_3_, double p_217358_5_, double p_217358_7_) {
        return true; // always enable spawner animations
    }

    @Override
    public BlockHitResult clip(ClipContext context) {
        return BlockGetter.traverseBlocks(
            context.getFrom(), context.getTo(), context, (innerContext, pos) -> {
                BlockState blockState = getBlockState(pos);
                FluidState fluidState = blockState.getFluidState();
                Vec3 vec3d = innerContext.getFrom();
                Vec3 vec3d2 = innerContext.getTo();
                VoxelShape voxelShape = innerContext.getBlockShape(blockState, this, pos);
                BlockHitResult blockHitResult = clipWithInteractionOverride(vec3d, vec3d2, pos, voxelShape, blockState);
                VoxelShape voxelShape2 = innerContext.getFluidShape(fluidState, this, pos);
                BlockHitResult blockHitResult2 = voxelShape2.clip(vec3d, vec3d2, pos);
                double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getFrom().distanceToSqr(blockHitResult.getLocation());
                double e = blockHitResult2 == null ? Double.MAX_VALUE : innerContext.getFrom().distanceToSqr(blockHitResult2.getLocation());
                return d <= e ? blockHitResult : blockHitResult2;
            }, (innerContext) -> {
                Vec3 vec3d = innerContext.getFrom().subtract(innerContext.getTo());
                return BlockHitResult.miss(
                    innerContext.getTo(),
                    Direction.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z),
                    BlockPos.containing(innerContext.getTo())
                );
            }
        );
    }

    @Override
    public LevelChunk getChunk(int x, int z) {
        if (chunks == null) {
            chunks = new Long2ObjectOpenHashMap<>();
        }
        return chunks.computeIfAbsent(ChunkPos.asLong(x, z), packedPos -> new PonderChunk(this, ChunkPos.getX(packedPos), ChunkPos.getZ(packedPos)));
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        return getChunk(x, z);
    }
}