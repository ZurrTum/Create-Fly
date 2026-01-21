package com.zurrtum.create.foundation.blockEntity.behaviour.inventory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper.ExtractionCountMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public abstract class CapManipulationBehaviourBase<T, S extends CapManipulationBehaviourBase<?, ?>> extends BlockEntityBehaviour<SmartBlockEntity> {

    protected InterfaceProvider target;
    protected @Nullable T targetCapability;
    protected Predicate<BlockEntity> filter;
    protected boolean simulateNext;
    protected boolean bypassSided;
    private boolean findNewNextTick;

    public CapManipulationBehaviourBase(SmartBlockEntity be, InterfaceProvider target) {
        super(be);
        setLazyTickRate(5);
        this.target = target;
        targetCapability = null;
        simulateNext = false;
        bypassSided = false;
        filter = Predicates.alwaysTrue();
    }

    @Nullable
    protected abstract T getCapability(Level world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side);

    @Override
    public void initialize() {
        super.initialize();
        findNewNextTick = true;
    }

    @Override
    public void onNeighborChanged(BlockPos neighborPos) {
        if (getTarget().getConnectedPos().equals(neighborPos))
            onHandlerInvalidated();
    }

    @SuppressWarnings("unchecked")
    public S bypassSidedness() {
        bypassSided = true;
        return (S) this;
    }

    /**
     * Only simulate the upcoming operation
     */
    @SuppressWarnings("unchecked")
    public S simulate() {
        simulateNext = true;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S withFilter(Predicate<BlockEntity> filter) {
        this.filter = filter;
        return (S) this;
    }

    public boolean hasInventory() {
        return targetCapability != null;
    }

    @Nullable
    public T getInventory() {
        return targetCapability;
    }

    /**
     * Get the target of this is behavior, which is the face of the owner BlockEntity that acts as the interface.
     * To get the BlockFace to use for capability lookup, call getOpposite on the result.
     */
    public BlockFace getTarget() {
        return this.target.getTarget(this.getLevel(), this.blockEntity.getBlockPos(), this.blockEntity.getBlockState());
    }

    protected boolean onHandlerInvalidated() {
        if (targetCapability == null)
            return false;
        findNewNextTick = true;
        targetCapability = null;

        return true;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (targetCapability == null)
            findNewCapability();
    }

    @Override
    public void tick() {
        super.tick();
        if (findNewNextTick || getLevel().getGameTime() % 64 == 0) {
            findNewNextTick = false;
            findNewCapability();
        }
    }

    public int getAmountFromFilter() {
        int amount = -1;
        ServerFilteringBehaviour filter = blockEntity.getBehaviour(ServerFilteringBehaviour.TYPE);
        if (filter != null && !filter.anyAmount())
            amount = filter.getAmount();
        return amount;
    }

    public ExtractionCountMode getModeFromFilter() {
        ExtractionCountMode mode = ExtractionCountMode.UPTO;
        ServerFilteringBehaviour filter = blockEntity.getBehaviour(ServerFilteringBehaviour.TYPE);
        if (filter != null && !filter.upTo)
            mode = ExtractionCountMode.EXACTLY;
        return mode;
    }

    public void findNewCapability() {
        Level world = getLevel();
        BlockFace targetBlockFace = this.getTarget().getOpposite();
        BlockPos pos = targetBlockFace.getPos();

        targetCapability = null;

        if (!world.isLoaded(pos))
            return;
        BlockEntity invBE = world.getBlockEntity(pos);
        if (!filter.test(invBE))
            return;
        targetCapability = getCapability(world, pos, invBE, bypassSided ? null : targetBlockFace.getFace());
    }

    @FunctionalInterface
    public interface InterfaceProvider {

        static InterfaceProvider towardBlockFacing() {
            return (w, p, s) -> new BlockFace(
                p,
                s.hasProperty(BlockStateProperties.FACING) ? s.getValue(BlockStateProperties.FACING) : s.getValue(BlockStateProperties.HORIZONTAL_FACING)
            );
        }

        static InterfaceProvider oppositeOfBlockFacing() {
            return (w, p, s) -> new BlockFace(
                p,
                (s.hasProperty(BlockStateProperties.FACING) ? s.getValue(BlockStateProperties.FACING) : s.getValue(BlockStateProperties.HORIZONTAL_FACING)).getOpposite()
            );
        }

        BlockFace getTarget(Level world, BlockPos pos, BlockState blockState);
    }

}
