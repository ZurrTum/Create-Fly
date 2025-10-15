package com.zurrtum.create.content.kinetics.crafter;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.CrafterItemHandler;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class ConnectedInputHandler {

    public static boolean shouldConnect(World world, BlockPos pos, Direction face, Direction direction) {
        BlockState refState = world.getBlockState(pos);
        if (!refState.contains(HORIZONTAL_FACING))
            return false;
        Direction refDirection = refState.get(HORIZONTAL_FACING);
        if (direction.getAxis() == refDirection.getAxis())
            return false;
        if (face == refDirection)
            return false;
        BlockState neighbour = world.getBlockState(pos.offset(direction));
        if (!neighbour.isOf(AllBlocks.MECHANICAL_CRAFTER))
            return false;
        return refDirection == neighbour.get(HORIZONTAL_FACING);
    }

    public static void toggleConnection(World world, BlockPos pos, BlockPos pos2) {
        MechanicalCrafterBlockEntity crafter1 = CrafterHelper.getCrafter(world, pos);
        MechanicalCrafterBlockEntity crafter2 = CrafterHelper.getCrafter(world, pos2);

        if (crafter1 == null || crafter2 == null)
            return;

        BlockPos controllerPos1 = crafter1.getPos().add(crafter1.input.data.getFirst());
        BlockPos controllerPos2 = crafter2.getPos().add(crafter2.input.data.getFirst());

        if (controllerPos1.equals(controllerPos2)) {
            MechanicalCrafterBlockEntity controller = CrafterHelper.getCrafter(world, controllerPos1);

            Set<BlockPos> positions = controller.input.data.stream().map(controllerPos1::add).collect(Collectors.toSet());
            List<BlockPos> frontier = new LinkedList<>();
            List<BlockPos> splitGroup = new ArrayList<>();

            frontier.add(pos2);
            positions.remove(pos2);
            positions.remove(pos);
            while (!frontier.isEmpty()) {
                BlockPos current = frontier.removeFirst();
                for (Direction direction : Iterate.directions) {
                    BlockPos next = current.offset(direction);
                    if (!positions.remove(next))
                        continue;
                    splitGroup.add(next);
                    frontier.add(next);
                }
            }

            initAndAddAll(world, crafter1, positions);
            initAndAddAll(world, crafter2, splitGroup);

            crafter1.markDirty();
            crafter1.connectivityChanged();
            crafter2.markDirty();
            crafter2.connectivityChanged();
            return;
        }

        if (!crafter1.input.isController)
            crafter1 = CrafterHelper.getCrafter(world, controllerPos1);
        if (!crafter2.input.isController)
            crafter2 = CrafterHelper.getCrafter(world, controllerPos2);
        if (crafter1 == null || crafter2 == null)
            return;

        connectControllers(world, crafter1, crafter2);

        world.setBlockState(crafter1.getPos(), crafter1.getCachedState(), Block.NOTIFY_ALL);

        crafter1.markDirty();
        crafter1.connectivityChanged();
        crafter2.markDirty();
        crafter2.connectivityChanged();
    }

    public static void initAndAddAll(World world, MechanicalCrafterBlockEntity crafter, Collection<BlockPos> positions) {
        crafter.input = new ConnectedInput();
        positions.forEach(splitPos -> {
            modifyAndUpdate(
                world, splitPos, input -> {
                    input.attachTo(crafter.getPos(), splitPos);
                    crafter.input.data.add(splitPos.subtract(crafter.getPos()));
                }
            );
        });
    }

    public static void connectControllers(World world, MechanicalCrafterBlockEntity crafter1, MechanicalCrafterBlockEntity crafter2) {

        crafter1.input.data.forEach(offset -> {
            BlockPos connectedPos = crafter1.getPos().add(offset);
            modifyAndUpdate(
                world, connectedPos, input -> {
                }
            );
        });

        crafter2.input.data.forEach(offset -> {
            if (offset.equals(BlockPos.ZERO))
                return;
            BlockPos connectedPos = crafter2.getPos().add(offset);
            modifyAndUpdate(
                world, connectedPos, input -> {
                    input.attachTo(crafter1.getPos(), connectedPos);
                    crafter1.input.data.add(BlockPos.ORIGIN.subtract(input.data.getFirst()));
                }
            );
        });

        crafter2.input.attachTo(crafter1.getPos(), crafter2.getPos());
        crafter1.input.data.add(BlockPos.ORIGIN.subtract(crafter2.input.data.getFirst()));
    }

    private static void modifyAndUpdate(World world, BlockPos pos, Consumer<ConnectedInput> callback) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof MechanicalCrafterBlockEntity crafter))
            return;

        callback.accept(crafter.input);
        crafter.markDirty();
        crafter.connectivityChanged();
    }

    public static class ConnectedInput {
        private static final Comparator<BlockPos> Y_COMPARATOR = Comparator.comparingInt(BlockPos::getY).reversed();
        private static final Comparator<BlockPos> NORTH_COMPARATOR = Y_COMPARATOR.thenComparing(Comparator.comparingInt(BlockPos::getX).reversed());
        private static final Comparator<BlockPos> SOUTH_COMPARATOR = Y_COMPARATOR.thenComparingInt(BlockPos::getX);
        private static final Comparator<BlockPos> EAST_COMPARATOR = Y_COMPARATOR.thenComparing(Comparator.comparingInt(BlockPos::getZ).reversed());
        private static final Comparator<BlockPos> WEST_COMPARATOR = Y_COMPARATOR.thenComparingInt(BlockPos::getZ);

        boolean isController;
        List<BlockPos> data = Collections.synchronizedList(new ArrayList<>());

        public ConnectedInput() {
            isController = true;
            data.add(BlockPos.ORIGIN);
        }

        public void attachTo(BlockPos controllerPos, BlockPos myPos) {
            isController = false;
            data.clear();
            data.add(controllerPos.subtract(myPos));
        }

        public Inventory getItemHandler(World world, BlockPos pos) {
            return new ConnectedInventory(getInventories(world, pos));
        }

        public CrafterItemHandler[] getInventories(World world, BlockPos pos) {
            if (!isController) {
                BlockPos controllerPos = pos.add(data.getFirst());
                ConnectedInput input = CrafterHelper.getInput(world, controllerPos);
                if (input == this || input == null || !input.isController)
                    return new CrafterItemHandler[0];
                return input.getInventories(world, controllerPos);
            }

            Comparator<BlockPos> invOrdering = switch (world.getBlockState(pos).get(MechanicalCrafterBlock.HORIZONTAL_FACING, Direction.SOUTH)) {
                case NORTH -> NORTH_COMPARATOR;
                case EAST -> EAST_COMPARATOR;
                case WEST -> WEST_COMPARATOR;
                default -> SOUTH_COMPARATOR;
            };

            return data.stream().sorted(invOrdering).map(l -> CrafterHelper.getCrafter(world, pos.add(l))).filter(Objects::nonNull)
                .map(MechanicalCrafterBlockEntity::getInventory).toArray(CrafterItemHandler[]::new);
        }

        public void write(WriteView view) {
            view.putBoolean("Controller", isController);
            view.put("Data", CreateCodecs.BLOCK_POS_LIST_CODEC, data);
        }

        public void read(ReadView view) {
            isController = view.getBoolean("Controller", false);
            data.clear();
            view.read("Data", CreateCodecs.BLOCK_POS_LIST_CODEC).ifPresent(data::addAll);

            // nbt got wiped -> reset
            if (data.isEmpty()) {
                isController = true;
                data.add(BlockPos.ORIGIN);
            }
        }
    }

    private static class ConnectedInventory implements SidedItemInventory {
        private final CrafterItemHandler[] itemHandler;
        private final int[] slots;
        private final int size;

        private ConnectedInventory(CrafterItemHandler[] itemHandler) {
            this.itemHandler = itemHandler;
            this.size = itemHandler.length;
            this.slots = SlotRangeCache.get(size);
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return slots;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return itemHandler[slot].canInsert(0, stack, dir);
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        @Override
        public ItemStack onExtract(ItemStack stack) {
            return removeMaxSize(stack, CrafterItemHandler.LIMIT);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot >= size) {
                return ItemStack.EMPTY;
            }
            return itemHandler[slot].getStack();
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= size) {
                return;
            }
            CrafterItemHandler handler = itemHandler[slot];
            handler.setStack(stack);
            handler.markDirty();
        }
    }
}
