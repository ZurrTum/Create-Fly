package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ConcurrentModificationException;

public abstract class BlockEntityBehaviour<T extends SmartBlockEntity> {

    public T blockEntity;
    private int lazyTickRate;
    private int lazyTickCounter;

    public BlockEntityBehaviour(T be) {
        blockEntity = be;
        setLazyTickRate(10);
    }

    public static <T extends BlockEntityBehaviour<?>> T get(BlockView reader, BlockPos pos, BehaviourType<T> type) {
        BlockEntity be;
        try {
            be = reader.getBlockEntity(pos);
        } catch (ConcurrentModificationException e) {
            be = null;
        }
        return get(be, type);
    }

    public static <T extends BlockEntityBehaviour<?>> T get(BlockEntity be, BehaviourType<T> type) {
        if (be == null)
            return null;
        if (!(be instanceof SmartBlockEntity ste))
            return null;
        return ste.getBehaviour(type);
    }

    public abstract BehaviourType<?> getType();

    public void initialize() {

    }

    public void tick() {
        if (lazyTickCounter-- <= 0) {
            lazyTickCounter = lazyTickRate;
            lazyTick();
        }

    }

    public void read(ReadView view, boolean clientPacket) {

    }

    public void write(WriteView view, boolean clientPacket) {

    }

    /**
     * Called when isSafeNBT == true. Defaults to write()
     */
    public void writeSafe(WriteView view) {
        write(view, false);
    }

    public boolean isSafeNBT() {
        return false;
    }

    public ItemRequirement getRequiredItems() {
        return ItemRequirement.NONE;
    }

    public void onBlockChanged(BlockState oldState) {

    }

    public void onNeighborChanged(BlockPos neighborPos) {

    }

    /**
     * Block destroyed or Chunk unloaded. Usually invalidates capabilities
     */
    public void unload() {
    }

    /**
     * Block destroyed or removed. Requires block to call ITE::onRemove
     */
    public void destroy() {
    }

    public void setLazyTickRate(int slowTickRate) {
        this.lazyTickRate = slowTickRate;
        this.lazyTickCounter = slowTickRate;
    }

    public void lazyTick() {

    }

    public void onBehaviourAdded(BehaviourType<?> type, BlockEntityBehaviour<?> behaviour) {
    }

    public BlockPos getPos() {
        return blockEntity.getPos();
    }

    public World getWorld() {
        return blockEntity.getWorld();
    }
}