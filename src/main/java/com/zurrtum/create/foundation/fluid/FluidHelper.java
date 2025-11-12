package com.zurrtum.create.foundation.fluid;

import com.zurrtum.create.AllFluidItemInventory;
import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FluidHelper {
    private static final Map<BlockPos, FluidInventoryCache> INV_CACHE = new Object2ReferenceOpenHashMap<>();

    public enum FluidExchange {
        ITEM_TO_TANK,
        TANK_TO_ITEM;
    }

    public static boolean isWater(Fluid fluid) {
        return convertToStill(fluid) == Fluids.WATER;
    }

    public static boolean isLava(Fluid fluid) {
        return convertToStill(fluid) == Fluids.LAVA;
    }

    public static boolean isSame(FluidStack fluidStack, FluidStack fluidStack2) {
        return fluidStack.getFluid() == fluidStack2.getFluid();
    }

    public static boolean isSame(FluidStack fluidStack, Fluid fluid) {
        return fluidStack.getFluid() == fluid;
    }

    @SuppressWarnings("deprecation")
    public static boolean isTag(Fluid fluid, TagKey<Fluid> tag) {
        return fluid.is(tag);
    }

    public static boolean isTag(FluidState fluid, TagKey<Fluid> tag) {
        return fluid.is(tag);
    }

    public static boolean isTag(FluidStack fluid, TagKey<Fluid> tag) {
        return isTag(fluid.getFluid(), tag);
    }

    public static SoundEvent getFillSound(FluidStack fluid) {
        //TODO
        SoundEvent soundevent = null;//fluid.getFluid().getFluidType().getSound(fluid, SoundActions.BUCKET_FILL);
        if (soundevent == null)
            soundevent = FluidHelper.isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        return soundevent;
    }

    public static SoundEvent getEmptySound(FluidStack fluid) {
        //TODO
        SoundEvent soundevent = null;//fluid.getFluid().getFluidType().getSound(fluid, SoundActions.BUCKET_EMPTY);
        if (soundevent == null)
            soundevent = FluidHelper.isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        return soundevent;
    }

    public static boolean hasBlockState(Fluid fluid) {
        BlockState blockState = fluid.defaultFluidState().createLegacyBlock();
        return blockState != null && blockState != Blocks.AIR.defaultBlockState();
    }

    public static FluidStack copyStackWithAmount(FluidStack fs, int amount) {
        if (amount <= 0)
            return FluidStack.EMPTY;
        if (fs.isEmpty())
            return FluidStack.EMPTY;
        FluidStack copy = fs.copy();
        copy.setAmount(amount);
        return copy;
    }

    public static Fluid convertToFlowing(Fluid fluid) {
        if (fluid instanceof FlowingFluid flowableFluid)
            return flowableFluid.getFlowing();
        return fluid;
    }

    public static Fluid convertToStill(Fluid fluid) {
        if (fluid instanceof FlowingFluid flowableFluid)
            return flowableFluid.getSource();
        return fluid;
    }

    public static FluidInventory getFluidInventory(Level world, BlockPos pos, Direction direction) {
        return getFluidInventory(world, pos, null, null, direction);
    }

    public static FluidInventory getFluidInventory(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, Direction direction) {
        if (state == null) {
            state = blockEntity != null ? blockEntity.getBlockState() : world.getBlockState(pos);
        }
        if (state.getBlock() instanceof FluidInventoryProvider<?> provider) {
            return provider.getFluidInventory(state, world, pos, blockEntity, direction);
        }
        return AllTransfer.getFluidInventory(world, pos, state, blockEntity, direction);
    }

    public static boolean hasFluidInventory(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, Direction direction) {
        if (state == null) {
            state = blockEntity != null ? blockEntity.getBlockState() : world.getBlockState(pos);
        }
        if (state.getBlock() instanceof FluidInventoryProvider<?>) {
            return true;
        }
        return AllTransfer.hasFluidInventory(world, pos, state, blockEntity, direction);
    }

    public static FluidItemInventory getFluidInventory(ItemStack stack) {
        FluidItemInventory inventory = AllFluidItemInventory.of(stack);
        if (inventory != null) {
            return inventory;
        }
        return AllTransfer.getFluidInventory(stack);
    }

    public static boolean hasFluidInventory(ItemStack stack) {
        return AllFluidItemInventory.has(stack) || AllTransfer.hasFluidInventory(stack);
    }

    public static boolean tryEmptyItemIntoBE(Level worldIn, Player player, InteractionHand handIn, ItemStack heldItem, SmartBlockEntity be) {
        if (!GenericItemEmptying.canItemBeEmptied(worldIn, heldItem))
            return false;

        FluidInventory capability = getFluidInventory(worldIn, be.getBlockPos(), null, be, null);
        if (capability == null) {
            return false;
        }
        if (worldIn.isClientSide())
            return true;
        Pair<FluidStack, ItemStack> emptyingResult = GenericItemEmptying.emptyItem(worldIn, heldItem, true);
        FluidStack fluidStack = emptyingResult.getFirst();
        if (!capability.preciseInsert(fluidStack, null)) {
            return false;
        }

        ItemStack copyOfHeld = heldItem.copy();
        emptyingResult = GenericItemEmptying.emptyItem(worldIn, copyOfHeld, false);

        if (!player.isCreative() && !(be instanceof CreativeFluidTankBlockEntity)) {
            if (copyOfHeld.isEmpty())
                player.setItemInHand(handIn, emptyingResult.getSecond());
            else {
                player.setItemInHand(handIn, copyOfHeld);
                player.getInventory().placeItemBackInInventory(emptyingResult.getSecond());
            }
        }
        return true;
    }

    public static boolean tryFillItemFromBE(Level world, Player player, InteractionHand handIn, ItemStack heldItem, SmartBlockEntity be) {
        if (!GenericItemFilling.canItemBeFilled(world, heldItem))
            return false;

        FluidInventory capability = FluidHelper.getFluidInventory(world, be.getBlockPos(), null, be, null);

        if (capability == null)
            return false;

        for (FluidStack fluid : capability) {
            if (fluid.isEmpty())
                continue;
            int requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(world, heldItem, fluid.copy());
            if (requiredAmountForItem == -1)
                continue;
            if (requiredAmountForItem > fluid.getAmount())
                continue;

            if (world.isClientSide())
                return true;

            if (player.isCreative() || be instanceof CreativeFluidTankBlockEntity)
                heldItem = heldItem.copy();
            ItemStack out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, fluid.copy());

            FluidStack copy = fluid.copy();
            copy.setAmount(requiredAmountForItem);
            capability.extract(copy, null);

            if (!player.isCreative())
                player.getInventory().placeItemBackInInventory(out);
            be.notifyUpdate();
            return true;
        }

        return false;
    }

    //TODO
    //    @Nullable
    //    public static FluidExchange exchange(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred, int maxAmount) {
    //        return exchange(fluidTank, fluidItem, preferred, true, maxAmount);
    //    }
    //
    //    @Nullable
    //    public static FluidExchange exchangeAll(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred) {
    //        return exchange(fluidTank, fluidItem, preferred, false, Integer.MAX_VALUE);
    //    }
    //
    //    @Nullable
    //    private static FluidExchange exchange(
    //        IFluidHandler fluidTank,
    //        IFluidHandlerItem fluidItem,
    //        FluidExchange preferred,
    //        boolean singleOp,
    //        int maxTransferAmountPerTank
    //    ) {
    //
    //        // Locks in the transfer direction of this operation
    //        FluidExchange lockedExchange = null;
    //
    //        for (int tankSlot = 0; tankSlot < fluidTank.getTanks(); tankSlot++) {
    //            for (int slot = 0; slot < fluidItem.getTanks(); slot++) {
    //
    //                FluidStack fluidInTank = fluidTank.getFluidInTank(tankSlot);
    //                int tankCapacity = fluidTank.getTankCapacity(tankSlot) - fluidInTank.getAmount();
    //                boolean tankEmpty = fluidInTank.isEmpty();
    //
    //                FluidStack fluidInItem = fluidItem.getFluidInTank(tankSlot);
    //                int itemCapacity = fluidItem.getTankCapacity(tankSlot) - fluidInItem.getAmount();
    //                boolean itemEmpty = fluidInItem.isEmpty();
    //
    //                boolean undecided = lockedExchange == null;
    //                boolean canMoveToTank = (undecided || lockedExchange == FluidExchange.ITEM_TO_TANK) && tankCapacity > 0;
    //                boolean canMoveToItem = (undecided || lockedExchange == FluidExchange.TANK_TO_ITEM) && itemCapacity > 0;
    //
    //                // Incompatible Liquids
    //                if (!tankEmpty && !itemEmpty && !FluidStack.isSameFluidSameComponents(fluidInItem, fluidInTank))
    //                    continue;
    //
    //                // Transfer liquid to tank
    //                if (((tankEmpty || itemCapacity <= 0) && canMoveToTank) || undecided && preferred == FluidExchange.ITEM_TO_TANK) {
    //
    //                    int amount = fluidTank.fill(
    //                        fluidItem.drain(Math.min(maxTransferAmountPerTank, tankCapacity), FluidAction.EXECUTE),
    //                        FluidAction.EXECUTE
    //                    );
    //                    if (amount > 0) {
    //                        lockedExchange = FluidExchange.ITEM_TO_TANK;
    //                        if (singleOp)
    //                            return lockedExchange;
    //                        continue;
    //                    }
    //                }
    //
    //                // Transfer liquid from tank
    //                if (((itemEmpty || tankCapacity <= 0) && canMoveToItem) || undecided && preferred == FluidExchange.TANK_TO_ITEM) {
    //
    //                    int amount = fluidItem.fill(
    //                        fluidTank.drain(Math.min(maxTransferAmountPerTank, itemCapacity), FluidAction.EXECUTE),
    //                        FluidAction.EXECUTE
    //                    );
    //                    if (amount > 0) {
    //                        lockedExchange = FluidExchange.TANK_TO_ITEM;
    //                        if (singleOp)
    //                            return lockedExchange;
    //                        continue;
    //                    }
    //
    //                }
    //
    //            }
    //        }
    //
    //        return null;
    //    }

    public static Supplier<FluidInventory> getFluidInventoryCache(ServerLevel world, BlockPos pos, Direction direction) {
        FluidInventoryCache cache = new FluidInventoryCache(world, pos, direction);
        INV_CACHE.put(pos, cache);
        return cache;
    }

    public static void invalidateInventoryCache(BlockPos pos) {
        FluidInventoryCache cache = INV_CACHE.get(pos);
        if (cache != null) {
            cache.invalidate();
        }
    }
}
