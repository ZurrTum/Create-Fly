package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ConcurrentModificationException;

public abstract class BlockEntityBehaviour<T extends SmartBlockEntity> {

    public T blockEntity;
    private int lazyTickRate;
    private int lazyTickCounter;

    public BlockEntityBehaviour(T be) {
        blockEntity = be;
        setLazyTickRate(10);
    }

    public static <T extends BlockEntityBehaviour<?>> T get(BlockGetter reader, BlockPos pos, BehaviourType<T> type) {
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

    public void read(ValueInput view, boolean clientPacket) {

    }

    public void write(ValueOutput view, boolean clientPacket) {

    }

    /**
     * Called when isSafeNBT == true. Defaults to write()
     */
    public void writeSafe(ValueOutput view) {
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
        return blockEntity.getBlockPos();
    }

    public Level getLevel() {
        return blockEntity.getLevel();
    }
}