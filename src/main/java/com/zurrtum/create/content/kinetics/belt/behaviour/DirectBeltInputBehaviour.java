package com.zurrtum.create.content.kinetics.belt.behaviour;

import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Behaviour for BlockEntities to which belts can transfer items directly in a
 * backup-friendly manner. Example uses: Basin, Saw, Depot
 */
public class DirectBeltInputBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<DirectBeltInputBehaviour> TYPE = new BehaviourType<>();

    private InsertionCallback tryInsert;
    private OccupiedPredicate isOccupied;
    private AvailabilityPredicate canInsert;
    private Supplier<Boolean> supportsBeltFunnels;

    public DirectBeltInputBehaviour(SmartBlockEntity be) {
        super(be);
        tryInsert = this::defaultInsertionCallback;
        canInsert = d -> true;
        isOccupied = d -> false;
        supportsBeltFunnels = () -> false;
    }

    public DirectBeltInputBehaviour allowingBeltFunnelsWhen(Supplier<Boolean> pred) {
        supportsBeltFunnels = pred;
        return this;
    }

    public DirectBeltInputBehaviour allowingBeltFunnels() {
        supportsBeltFunnels = () -> true;
        return this;
    }

    public DirectBeltInputBehaviour onlyInsertWhen(AvailabilityPredicate pred) {
        canInsert = pred;
        return this;
    }

    public DirectBeltInputBehaviour considerOccupiedWhen(OccupiedPredicate pred) {
        isOccupied = pred;
        return this;
    }

    public DirectBeltInputBehaviour setInsertionHandler(InsertionCallback callback) {
        tryInsert = callback;
        return this;
    }

    private ItemStack defaultInsertionCallback(TransportedItemStack inserted, Direction side, boolean simulate) {
        Inventory lazy = ItemHelper.getInventory(blockEntity.getWorld(), blockEntity.getPos(), null, blockEntity, side);
        if (lazy == null)
            return inserted.stack;
        int count = inserted.stack.getCount();
        int insert = lazy.insertExist(inserted.stack, side);
        if (insert == 0) {
            return inserted.stack;
        }
        if (insert == count) {
            return ItemStack.EMPTY;
        }
        return inserted.stack.copyWithCount(count - insert);
    }

    // TODO: verify that this side is consistent across all calls
    public boolean canInsertFromSide(Direction side) {
        return canInsert.test(side);
    }

    public boolean isOccupied(Direction side) {
        return isOccupied.test(side);
    }

    public ItemStack handleInsertion(ItemStack stack, Direction side, boolean simulate) {
        return handleInsertion(new TransportedItemStack(stack), side, simulate);
    }

    public ItemStack handleInsertion(TransportedItemStack stack, Direction side, boolean simulate) {
        return tryInsert.apply(stack, side, simulate);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @FunctionalInterface
    public interface InsertionCallback {
        ItemStack apply(TransportedItemStack stack, Direction side, boolean simulate);
    }

    @FunctionalInterface
    public interface OccupiedPredicate {
        boolean test(Direction side);
    }

    @FunctionalInterface
    public interface AvailabilityPredicate {
        boolean test(Direction side);
    }

    @Nullable
    public ItemStack tryExportingToBeltFunnel(ItemStack stack, @Nullable Direction side, boolean simulate) {
        BlockPos funnelPos = blockEntity.getPos().up();
        World world = getWorld();
        BlockState funnelState = world.getBlockState(funnelPos);
        if (!(funnelState.getBlock() instanceof BeltFunnelBlock))
            return null;
        if (funnelState.get(BeltFunnelBlock.SHAPE) != Shape.PULLING)
            return null;
        if (side != null && FunnelBlock.getFunnelFacing(funnelState) != side)
            return null;
        BlockEntity be = world.getBlockEntity(funnelPos);
        if (!(be instanceof FunnelBlockEntity))
            return null;
        if (funnelState.get(BeltFunnelBlock.POWERED))
            return null;
        ItemStack insert = FunnelBlock.tryInsert(world, funnelPos, stack, simulate);
        if (insert.getCount() != stack.getCount() && !simulate)
            ((FunnelBlockEntity) be).flap(true);
        return insert;
    }

    public boolean canSupportBeltFunnels() {
        return supportsBeltFunnels.get();
    }

}
