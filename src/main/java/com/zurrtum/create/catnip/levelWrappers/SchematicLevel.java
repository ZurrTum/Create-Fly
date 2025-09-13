package com.zurrtum.create.catnip.levelWrappers;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.components.ComponentProcessors;
import com.zurrtum.create.catnip.math.BBHelper;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;

import java.util.*;
import java.util.function.Predicate;

public class SchematicLevel extends WrappedLevel implements ServerWorldAccess, SchematicLevelAccessor {
    protected Map<BlockPos, BlockState> blocks;
    protected Map<BlockPos, BlockEntity> blockEntities;
    protected List<BlockEntity> renderedBlockEntities;
    protected List<Entity> entities;
    protected BlockBox bounds;

    public BlockPos anchor;
    public boolean renderMode;

    public SchematicLevel(World original) {
        this(BlockPos.ORIGIN, original);
    }

    public SchematicLevel(BlockPos anchor, World original) {
        super(original);
        setChunkSource(new SchematicChunkSource(this));
        this.blocks = new HashMap<>();
        this.blockEntities = new HashMap<>();
        this.bounds = new BlockBox(BlockPos.ORIGIN);
        this.anchor = anchor;
        this.entities = new ArrayList<>();
        this.renderedBlockEntities = new ArrayList<>();
    }

    @Override
    public Set<BlockPos> getAllPositions() {
        return blocks.keySet();
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        if (entityIn instanceof ItemFrameEntity itemFrame)
            itemFrame.setHeldItemStack(ComponentProcessors.withUnsafeComponentsDiscarded(itemFrame.getHeldItemStack()));
        if (entityIn instanceof ArmorStandEntity armorStand)
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values())
                armorStand.equipStack(equipmentSlot, ComponentProcessors.withUnsafeComponentsDiscarded(armorStand.getEquippedStack(equipmentSlot)));

        return entities.add(entityIn);
    }

    @Override
    public List<Entity> getEntityList() {
        return entities;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (isOutOfHeightLimit(pos))
            return null;
        if (blockEntities.containsKey(pos))
            return blockEntities.get(pos);
        if (!blocks.containsKey(pos.subtract(anchor)))
            return null;

        BlockState blockState = getBlockState(pos);
        if (blockState.hasBlockEntity()) {
            try {
                BlockEntity blockEntity = ((BlockEntityProvider) blockState.getBlock()).createBlockEntity(pos, blockState);
                if (blockEntity != null) {
                    onBEadded(blockEntity, pos);
                    blockEntities.put(pos, blockEntity);
                    renderedBlockEntities.add(blockEntity);
                }
                return blockEntity;
            } catch (Exception e) {
                Create.LOGGER.debug("Could not create BlockEntity of block " + blockState, e);
            }
        }
        return null;
    }

    protected void onBEadded(BlockEntity blockEntity, BlockPos pos) {
        blockEntity.setWorld(this);
    }

    @Override
    public BlockState getBlockState(BlockPos globalPos) {
        BlockPos pos = globalPos.subtract(anchor);

        if (pos.getY() - bounds.getMinY() == -1 && !renderMode)
            return Blocks.DIRT.getDefaultState();
        if (getBounds().contains(pos) && blocks.containsKey(pos))
            return processBlockStateForPrinting(blocks.get(pos));
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public Map<BlockPos, BlockState> getBlockMap() {
        return blocks;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public RegistryEntry<Biome> getBiome(BlockPos pos) {
        return level.getRegistryManager().getOrThrow(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS);
        //return ForgeRegistries.BIOMES.getHolder(Biomes.PLAINS.location()).orElse(null);
    }

    @Override
    public int getLightLevel(LightType lightLayer, BlockPos pos) {
        return 15;
    }

    @Override
    public float getBrightness(Direction face, boolean hasShade) {
        return 1f;
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
    public List<Entity> getOtherEntities(Entity arg0, Box arg1, Predicate<? super Entity> arg2) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Entity> List<T> getEntitiesByClass(Class<T> arg0, Box arg1, Predicate<? super T> arg2) {
        return Collections.emptyList();
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public int getAmbientDarkness() {
        return 0;
    }

    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> predicate) {
        return predicate.test(getBlockState(pos));
    }

    @Override
    public boolean breakBlock(BlockPos arg0, boolean arg1) {
        return setBlockState(arg0, Blocks.AIR.getDefaultState(), 3);
    }

    @Override
    public boolean removeBlock(BlockPos arg0, boolean arg1) {
        return setBlockState(arg0, Blocks.AIR.getDefaultState(), 3);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState arg1, int arg2) {
        pos = pos.toImmutable().subtract(anchor);
        bounds = BBHelper.encapsulate(bounds, pos);
        blocks.put(pos, arg1);
        if (blockEntities.containsKey(pos)) {
            BlockEntity blockEntity = blockEntities.get(pos);
            if (!blockEntity.getType().supports(arg1)) {
                blockEntities.remove(pos);
                renderedBlockEntities.remove(blockEntity);
            }
        }

        BlockEntity blockEntity = getBlockEntity(pos);
        if (blockEntity != null)
            blockEntities.put(pos, blockEntity);

        return true;
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
    }

    @Override
    public BlockBox getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(BlockBox bounds) {
        this.bounds = bounds;
    }

    @Override
    public Iterable<BlockEntity> getBlockEntities() {
        return blockEntities.values();
    }

    @Override
    public Iterable<BlockEntity> getRenderedBlockEntities() {
        return renderedBlockEntities;
    }

    protected BlockState processBlockStateForPrinting(BlockState state) {
        if (state.getBlock() instanceof AbstractFurnaceBlock && state.contains(Properties.LIT))
            state = state.with(Properties.LIT, false);
        return state;
    }

    @Override
    public ServerWorld toServerWorld() {
        if (level instanceof ServerWorld serverWorld) {
            return serverWorld;
        }
        throw new IllegalStateException("Cannot use IServerWorld#getWorld in a client environment");
    }
}
