package com.zurrtum.create.client.ponder.api.level;

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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PonderLevel extends SchematicLevel {

    @Nullable
    public PonderScene scene;

    protected Map<BlockPos, BlockState> originalBlocks;
    protected Map<BlockPos, NbtCompound> originalBlockEntities;
    protected Map<BlockPos, Integer> blockBreakingProgressions;
    protected List<Entity> originalEntities;
    @Nullable
    private Long2ObjectMap<PonderChunk> chunks;

    protected PonderWorldParticles particles;

    int overrideLight;
    @Nullable Selection mask;
    boolean currentlyTickingEntities;

    public PonderLevel(BlockPos anchor, World original) {
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
        DynamicRegistryManager registryManager = getRegistryManager();
        blockEntities.forEach((k, v) -> originalBlockEntities.put(k, v.createNbtWithIdentifyingData(registryManager)));
        entities.forEach(e -> {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(e.getErrorReporterContext(), Ponder.LOGGER)) {
                NbtWriteView writeView = NbtWriteView.create(logging, registryManager);
                e.saveData(writeView);
                ReadView readView = NbtReadView.create(logging, registryManager, writeView.getNbt());
                EntityType.getEntityFromData(readView, this, SpawnReason.LOAD).ifPresent(originalEntities::add);
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
        DynamicRegistryManager registryManager = getRegistryManager();
        originalBlockEntities.forEach((k, v) -> {
            BlockEntity blockEntity = BlockEntity.createFromNbt(k, originalBlocks.get(k), v, registryManager);
            onBEAdded(blockEntity, blockEntity.getPos());
            blockEntities.put(k, blockEntity);
            renderedBlockEntities.add(blockEntity);
        });
        originalEntities.forEach(e -> {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(e.getErrorReporterContext(), Ponder.LOGGER)) {
                NbtWriteView writeView = NbtWriteView.create(logging, registryManager);
                e.saveData(writeView);
                ReadView readView = NbtReadView.create(logging, registryManager, writeView.getNbt());
                EntityType.getEntityFromData(readView, this, SpawnReason.LOAD).ifPresent(entities::add);
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
                BlockEntity blockEntity = BlockEntity.createFromNbt(p, originalBlocks.get(p), originalBlockEntities.get(p), getRegistryManager());
                if (blockEntity != null) {
                    onBEAdded(blockEntity, blockEntity.getPos());
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
    public int getLightLevel(LightType p_226658_1_, BlockPos p_226658_2_) {
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
            return Blocks.AIR.getDefaultState();
        if (currentlyTickingEntities && globalPos.getY() < 0)
            return Blocks.AIR.getDefaultState();
        return super.getBlockState(globalPos);
    }

    @Override // For particle collision
    public BlockView getChunkAsView(int p_225522_1_, int p_225522_2_) {
        return this;
    }

    public void renderEntities(MatrixStack ms, OrderedRenderCommandQueue queue, Camera ari, CameraRenderState cameraRenderState, float pt) {
        Vec3d Vector3d = ari.getPos();
        double d0 = Vector3d.getX();
        double d1 = Vector3d.getY();
        double d2 = Vector3d.getZ();

        MinecraftClient mc = MinecraftClient.getInstance();
        EntityRenderManager renderManager = mc.getEntityRenderDispatcher();
        for (Entity entity : entities) {
            if (entity.age == 0) {
                entity.lastRenderX = entity.getX();
                entity.lastRenderY = entity.getY();
                entity.lastRenderZ = entity.getZ();
            }
            renderEntity(renderManager, entity, cameraRenderState, d0, d1, d2, pt, ms, queue);
        }
    }

    private void renderEntity(
        EntityRenderManager renderManager,
        Entity entity,
        CameraRenderState cameraRenderState,
        double x,
        double y,
        double z,
        float pt,
        MatrixStack ms,
        OrderedRenderCommandQueue queue
    ) {
        EntityRenderState state = renderManager.getAndUpdateRenderState(entity, pt);
        renderManager.render(state, cameraRenderState, state.x - x, state.y - y, state.z - z, ms, queue);
    }

    public void renderParticles(MatrixStack ms, OrderedRenderCommandQueueImpl queue, Camera ari, CameraRenderState cameraRenderState, float pt) {
        particles.renderParticles(ms, queue, ari, cameraRenderState, pt);
    }

    public void tick() {
        currentlyTickingEntities = true;

        particles.tick();

        for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();

            entity.age++;
            entity.lastRenderX = entity.getX();
            entity.lastRenderY = entity.getY();
            entity.lastRenderZ = entity.getZ();
            entity.tick();

            if (entity.getY() <= -.5f)
                entity.discard();

            if (!entity.isAlive())
                iterator.remove();
        }

        currentlyTickingEntities = false;
    }

    @Override
    public void addParticleClient(ParticleEffect data, double x, double y, double z, double mx, double my, double mz) {
        particles.addParticle(data, x, y, z, mx, my, mz);
    }

    @Override
    public void addImportantParticleClient(ParticleEffect data, double x, double y, double z, double mx, double my, double mz) {
        addParticleClient(data, x, y, z, mx, my, mz);
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
        VoxelShape voxelshape = state.getOutlineShape(this, pos);
        if (voxelshape.isEmpty())
            return;

        Box bb = voxelshape.getBoundingBox();
        double d1 = Math.min(1.0D, bb.maxX - bb.minX);
        double d2 = Math.min(1.0D, bb.maxY - bb.minY);
        double d3 = Math.min(1.0D, bb.maxZ - bb.minZ);
        int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
        int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
        int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

        for (int l = 0; l < i; ++l) {
            for (int i1 = 0; i1 < j; ++i1) {
                for (int j1 = 0; j1 < k; ++j1) {
                    double d4 = (l + 0.5D) / i;
                    double d5 = (i1 + 0.5D) / j;
                    double d6 = (j1 + 0.5D) / k;
                    double d7 = d4 * d1 + bb.minX;
                    double d8 = d5 * d2 + bb.minY;
                    double d9 = d6 * d3 + bb.minZ;
                    addParticleClient(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
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
    public boolean isChunkLoaded(BlockPos pos) {
        return true; // fix particle lighting
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPosLoaded(int x, int y) {
        return true; // fix particle lighting
    }

    @Override
    public boolean isPosLoaded(BlockPos pos) {
        return true; // fix particle lighting
    }

    @Override
    public boolean isPlayerInRange(double p_217358_1_, double p_217358_3_, double p_217358_5_, double p_217358_7_) {
        return true; // always enable spawner animations
    }

    @Override
    public BlockHitResult raycast(RaycastContext context) {
        return BlockView.raycast(
            context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
                BlockState blockState = getBlockState(pos);
                FluidState fluidState = blockState.getFluidState();
                Vec3d vec3d = innerContext.getStart();
                Vec3d vec3d2 = innerContext.getEnd();
                VoxelShape voxelShape = innerContext.getBlockShape(blockState, this, pos);
                BlockHitResult blockHitResult = raycastBlock(vec3d, vec3d2, pos, voxelShape, blockState);
                VoxelShape voxelShape2 = innerContext.getFluidShape(fluidState, this, pos);
                BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, pos);
                double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult.getPos());
                double e = blockHitResult2 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
                return d <= e ? blockHitResult : blockHitResult2;
            }, (innerContext) -> {
                Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
                return BlockHitResult.createMissed(
                    innerContext.getEnd(),
                    Direction.getFacing(vec3d.x, vec3d.y, vec3d.z),
                    BlockPos.ofFloored(innerContext.getEnd())
                );
            }
        );
    }

    @Override
    public WorldChunk getChunk(int x, int z) {
        if (chunks == null) {
            chunks = new Long2ObjectOpenHashMap<>();
        }
        return chunks.computeIfAbsent(
            ChunkPos.toLong(x, z),
            packedPos -> new PonderChunk(this, ChunkPos.getPackedX(packedPos), ChunkPos.getPackedZ(packedPos))
        );
    }

    @Override
    public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        return getChunk(x, z);
    }
}