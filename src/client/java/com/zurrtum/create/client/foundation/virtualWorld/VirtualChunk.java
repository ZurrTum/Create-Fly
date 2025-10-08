package com.zurrtum.create.client.foundation.virtualWorld;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.EmptyTickSchedulers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class VirtualChunk extends Chunk {
    public final VirtualRenderWorld world;

    private final VirtualChunkSection[] sections;

    private boolean needsLight;

    public VirtualChunk(VirtualRenderWorld world, int x, int z) {
        super(new ChunkPos(x, z), UpgradeData.NO_UPGRADE_DATA, world, world.getPalettesFactory(), 0L, null, null);

        this.world = world;

        int sectionCount = world.countVerticalSections();
        this.sections = new VirtualChunkSection[sectionCount];

        for (int i = 0, bottom = world.getBottomSectionCoord(); i < sectionCount; i++) {
            sections[i] = new VirtualChunkSection(this, (i + bottom) << 4);
        }

        this.needsLight = true;

        //TODO
        //		Mods.STARLIGHT.executeIfInstalled(() -> () -> {
        //			((ExtendedChunk) this).setBlockNibbles(StarLightEngine.getFilledEmptyLight(this));
        //			((ExtendedChunk) this).setSkyNibbles(StarLightEngine.getFilledEmptyLight(this));
        //		});
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos pos, BlockState state, int flags) {
        return null;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
    }

    @Override
    public void addEntity(Entity entity) {
    }

    @Override
    public Set<BlockPos> getBlockEntityPositions() {
        return Collections.emptySet();
    }

    @Override
    public ChunkSection[] getSectionArray() {
        return sections;
    }

    @Override
    public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
        return Collections.emptySet();
    }

    @Override
    public void setHeightmap(Heightmap.Type type, long[] data) {
    }

    @Override
    public Heightmap getHeightmap(Heightmap.Type type) {
        return null;
    }

    @Override
    public int sampleHeightmap(Heightmap.Type type, int x, int z) {
        return 0;
    }

    @Override
    @Nullable
    public StructureStart getStructureStart(Structure structure) {
        return null;
    }

    @Override
    public void setStructureStart(Structure structure, StructureStart structureStart) {
    }

    @Override
    public Map<Structure, StructureStart> getStructureStarts() {
        return Collections.emptyMap();
    }

    @Override
    public void setStructureStarts(Map<Structure, StructureStart> structureStarts) {
    }

    @Override
    public LongSet getStructureReferences(Structure pStructure) {
        return LongSets.emptySet();
    }

    @Override
    public void addStructureReference(Structure structure, long reference) {
    }

    @Override
    public Map<Structure, LongSet> getStructureReferences() {
        return Collections.emptyMap();
    }

    @Override
    public void setStructureReferences(Map<Structure, LongSet> structureReferencesMap) {
    }

    @Override
    public void markNeedsSaving() {
    }

    @Override
    public boolean needsSaving() {
        return false;
    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.LIGHT;
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
    }

    @Override
    public ShortList[] getPostProcessingLists() {
        return new ShortList[0];
    }

    @Override
    @Nullable
    public NbtCompound getBlockEntityNbt(BlockPos pos) {
        return null;
    }

    @Override
    @Nullable
    public NbtCompound getPackedBlockEntityNbt(BlockPos pos, RegistryWrapper.WrapperLookup registries) {
        return null;
    }

    @Override
    public void forEachBlockMatchingPredicate(@NotNull Predicate<BlockState> roughFilter, @NotNull BiConsumer<BlockPos, BlockState> output) {
        world.blockStates.forEach((blockPos, state) -> {
            if (ChunkSectionPos.getSectionCoord(blockPos.getX()) == pos.x && ChunkSectionPos.getSectionCoord(blockPos.getZ()) == pos.z) {
                if (roughFilter.test(state)) {
                    output.accept(blockPos, state);
                }
            }
        });
    }

    @Override
    public BasicTickScheduler<Block> getBlockTickScheduler() {
        return EmptyTickSchedulers.getReadOnlyTickScheduler();
    }

    @Override
    public BasicTickScheduler<Fluid> getFluidTickScheduler() {
        return EmptyTickSchedulers.getReadOnlyTickScheduler();
    }

    @Override
    public TickSchedulers getTickSchedulers(long time) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getInhabitedTime() {
        return 0;
    }

    @Override
    public void setInhabitedTime(long amount) {
    }

    @Override
    public boolean isLightOn() {
        return needsLight;
    }

    @Override
    public void setLightOn(boolean lightCorrect) {
        this.needsLight = lightCorrect;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return world.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return world.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return world.getFluidState(pos);
    }
}
