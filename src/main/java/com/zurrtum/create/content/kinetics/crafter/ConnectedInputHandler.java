package com.zurrtum.create.content.kinetics.crafter;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.CrafterItemHandler;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class ConnectedInputHandler {

    public static boolean shouldConnect(Level world, BlockPos pos, Direction face, Direction direction) {
        BlockState refState = world.getBlockState(pos);
        if (!refState.hasProperty(HORIZONTAL_FACING))
            return false;
        Direction refDirection = refState.getValue(HORIZONTAL_FACING);
        if (direction.getAxis() == refDirection.getAxis())
            return false;
        if (face == refDirection)
            return false;
        BlockState neighbour = world.getBlockState(pos.relative(direction));
        if (!neighbour.is(AllBlocks.MECHANICAL_CRAFTER))
            return false;
        return refDirection == neighbour.getValue(HORIZONTAL_FACING);
    }

    public static void toggleConnection(Level world, BlockPos pos, BlockPos pos2) {
        MechanicalCrafterBlockEntity crafter1 = CrafterHelper.getCrafter(world, pos);
        MechanicalCrafterBlockEntity crafter2 = CrafterHelper.getCrafter(world, pos2);

        if (crafter1 == null || crafter2 == null)
            return;

        BlockPos controllerPos1 = crafter1.getBlockPos().offset(crafter1.input.data.getFirst());
        BlockPos controllerPos2 = crafter2.getBlockPos().offset(crafter2.input.data.getFirst());

        if (controllerPos1.equals(controllerPos2)) {
            MechanicalCrafterBlockEntity controller = CrafterHelper.getCrafter(world, controllerPos1);

            Set<BlockPos> positions = controller.input.data.stream().map(controllerPos1::offset).collect(Collectors.toSet());
            List<BlockPos> frontier = new LinkedList<>();
            List<BlockPos> splitGroup = new ArrayList<>();

            frontier.add(pos2);
            positions.remove(pos2);
            positions.remove(pos);
            while (!frontier.isEmpty()) {
                BlockPos current = frontier.removeFirst();
                for (Direction direction : Iterate.directions) {
                    BlockPos next = current.relative(direction);
                    if (!positions.remove(next))
                        continue;
                    splitGroup.add(next);
                    frontier.add(next);
                }
            }

            initAndAddAll(world, crafter1, positions);
            initAndAddAll(world, crafter2, splitGroup);

            crafter1.setChanged();
            crafter1.connectivityChanged();
            crafter2.setChanged();
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

        world.setBlock(crafter1.getBlockPos(), crafter1.getBlockState(), Block.UPDATE_ALL);

        crafter1.setChanged();
        crafter1.connectivityChanged();
        crafter2.setChanged();
        crafter2.connectivityChanged();
    }

    public static void initAndAddAll(Level world, MechanicalCrafterBlockEntity crafter, Collection<BlockPos> positions) {
        crafter.input = new ConnectedInput();
        positions.forEach(splitPos -> {
            modifyAndUpdate(
                world, splitPos, input -> {
                    input.attachTo(crafter.getBlockPos(), splitPos);
                    crafter.input.data.add(splitPos.subtract(crafter.getBlockPos()));
                }
            );
        });
    }

    public static void connectControllers(Level world, MechanicalCrafterBlockEntity crafter1, MechanicalCrafterBlockEntity crafter2) {

        crafter1.input.data.forEach(offset -> {
            BlockPos connectedPos = crafter1.getBlockPos().offset(offset);
            modifyAndUpdate(
                world, connectedPos, input -> {
                }
            );
        });

        crafter2.input.data.forEach(offset -> {
            if (offset.equals(BlockPos.ZERO))
                return;
            BlockPos connectedPos = crafter2.getBlockPos().offset(offset);
            modifyAndUpdate(
                world, connectedPos, input -> {
                    input.attachTo(crafter1.getBlockPos(), connectedPos);
                    crafter1.input.data.add(BlockPos.ZERO.subtract(input.data.getFirst()));
                }
            );
        });

        crafter2.input.attachTo(crafter1.getBlockPos(), crafter2.getBlockPos());
        crafter1.input.data.add(BlockPos.ZERO.subtract(crafter2.input.data.getFirst()));
    }

    private static void modifyAndUpdate(Level world, BlockPos pos, Consumer<ConnectedInput> callback) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof MechanicalCrafterBlockEntity crafter))
            return;

        callback.accept(crafter.input);
        crafter.setChanged();
        crafter.connectivityChanged();
    }

    public static class ConnectedInput {
        private static final Comparator<BlockPos> Y_COMPARATOR = Comparator.<BlockPos>comparingInt(BlockPos::getY).reversed();
        private static final Comparator<BlockPos> NORTH_COMPARATOR = Y_COMPARATOR.thenComparing(Comparator.<BlockPos>comparingInt(BlockPos::getX)
            .reversed());
        private static final Comparator<BlockPos> SOUTH_COMPARATOR = Y_COMPARATOR.thenComparingInt(BlockPos::getX);
        private static final Comparator<BlockPos> EAST_COMPARATOR = Y_COMPARATOR.thenComparing(Comparator.<BlockPos>comparingInt(BlockPos::getZ)
            .reversed());
        private static final Comparator<BlockPos> WEST_COMPARATOR = Y_COMPARATOR.thenComparingInt(BlockPos::getZ);

        boolean isController;
        List<BlockPos> data = Collections.synchronizedList(new ArrayList<>());

        public ConnectedInput() {
            isController = true;
            data.add(BlockPos.ZERO);
        }

        public void attachTo(BlockPos controllerPos, BlockPos myPos) {
            isController = false;
            data.clear();
            data.add(controllerPos.subtract(myPos));
        }

        public Container getItemHandler(Level world, BlockPos pos) {
            return new ConnectedInventory(getInventories(world, pos));
        }

        public CrafterItemHandler[] getInventories(Level world, BlockPos pos) {
            if (!isController) {
                BlockPos controllerPos = pos.offset(data.getFirst());
                ConnectedInput input = CrafterHelper.getInput(world, controllerPos);
                if (input == this || input == null || !input.isController)
                    return new CrafterItemHandler[0];
                return input.getInventories(world, controllerPos);
            }

            Comparator<BlockPos> invOrdering = switch (world.getBlockState(pos)
                .getValueOrElse(MechanicalCrafterBlock.HORIZONTAL_FACING, Direction.SOUTH)) {
                case NORTH -> NORTH_COMPARATOR;
                case EAST -> EAST_COMPARATOR;
                case WEST -> WEST_COMPARATOR;
                default -> SOUTH_COMPARATOR;
            };

            return data.stream().sorted(invOrdering).map(l -> CrafterHelper.getCrafter(world, pos.offset(l))).filter(Objects::nonNull)
                .map(MechanicalCrafterBlockEntity::getInventory).toArray(CrafterItemHandler[]::new);
        }

        public void write(ValueOutput view) {
            view.putBoolean("Controller", isController);
            view.store("Data", CreateCodecs.BLOCK_POS_LIST_CODEC, data);
        }

        public void read(ValueInput view) {
            isController = view.getBooleanOr("Controller", false);
            data.clear();
            view.read("Data", CreateCodecs.BLOCK_POS_LIST_CODEC).ifPresent(data::addAll);

            // nbt got wiped -> reset
            if (data.isEmpty()) {
                isController = true;
                data.add(BlockPos.ZERO);
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
        public int[] getSlotsForFace(Direction side) {
            return slots;
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return itemHandler[slot].canPlaceItemThroughFace(0, stack, dir);
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        @Override
        public ItemStack onExtract(ItemStack stack) {
            return removeMaxSize(stack, CrafterItemHandler.LIMIT);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot >= size) {
                return ItemStack.EMPTY;
            }
            return itemHandler[slot].getStack();
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= size) {
                return;
            }
            CrafterItemHandler handler = itemHandler[slot];
            handler.setStack(stack);
            handler.setChanged();
        }
    }
}
