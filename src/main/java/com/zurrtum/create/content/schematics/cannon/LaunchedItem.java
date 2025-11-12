package com.zurrtum.create.content.schematics.cannon;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.belt.item.BeltConnectorItem;
import com.zurrtum.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class LaunchedItem {

    public int totalTicks;
    public int ticksRemaining;
    public BlockPos target;
    public ItemStack stack;

    private LaunchedItem(BlockPos start, BlockPos target, ItemStack stack) {
        this(target, stack, ticksForDistance(start, target), ticksForDistance(start, target));
    }

    private static int ticksForDistance(BlockPos start, BlockPos target) {
        return (int) (Math.max(10, Math.sqrt(Math.sqrt(target.distSqr(start))) * 4f));
    }

    LaunchedItem() {
    }

    private LaunchedItem(BlockPos target, ItemStack stack, int ticksLeft, int total) {
        this.target = target;
        this.stack = stack;
        this.totalTicks = total;
        this.ticksRemaining = ticksLeft;
    }

    public boolean update(Level world) {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            return false;
        }
        if (world.isClientSide())
            return false;

        place(world);
        return true;
    }

    public void write(ValueOutput view) {
        view.putInt("TotalTicks", totalTicks);
        view.putInt("TicksLeft", ticksRemaining);
        if (!stack.isEmpty()) {
            view.store("Stack", ItemStack.CODEC, stack);
        }
        view.store("Target", BlockPos.CODEC, target);
    }

    public static LaunchedItem from(ValueInput view, HolderGetter<Block> holderGetter) {
        LaunchedItem item = ForBelt.from(view, holderGetter);
        if (item != null) {
            return item;
        }
        item = ForBlockState.from(view, holderGetter);
        if (item != null) {
            return item;
        }
        item = new LaunchedItem.ForEntity();
        item.read(view, holderGetter);
        return item;
    }

    abstract void place(Level world);

    void read(ValueInput view, HolderGetter<Block> holderGetter) {
        target = view.read("Target", BlockPos.CODEC).orElse(BlockPos.ZERO);
        ticksRemaining = view.getIntOr("TicksLeft", 0);
        totalTicks = view.getIntOr("TotalTicks", 0);
        stack = view.read("Stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    public static class ForBlockState extends LaunchedItem {
        public BlockState state;
        public CompoundTag data;

        ForBlockState() {
        }

        public ForBlockState(BlockPos start, BlockPos target, ItemStack stack, BlockState state, CompoundTag data) {
            super(start, target, stack);
            this.state = state;
            this.data = data;
        }

        @Override
        public void write(ValueOutput view) {
            super.write(view);
            view.store("BlockState", BlockState.CODEC, state);
            if (data != null) {
                data.remove("x");
                data.remove("y");
                data.remove("z");
                data.remove("id");
                view.store("Data", CompoundTag.CODEC, data);
            }
        }

        public static LaunchedItem from(ValueInput view, HolderGetter<Block> holderGetter) {
            return view.read("BlockState", BlockState.CODEC).map(state -> {
                ForBlockState result = new ForBlockState();
                result.read(view, holderGetter, state);
                return result;
            }).orElse(null);
        }

        @Override
        void read(ValueInput view, HolderGetter<Block> holderGetter) {
            read(view, holderGetter, view.read("BlockState", BlockState.CODEC).orElseGet(Blocks.AIR::defaultBlockState));
        }

        private void read(ValueInput view, HolderGetter<Block> holderGetter, BlockState state) {
            super.read(view, holderGetter);
            this.state = state;
            view.read("Data", CompoundTag.CODEC).ifPresent(nbt -> data = nbt);
        }

        @Override
        void place(Level world) {
            BlockHelper.placeSchematicBlock(world, state, target, stack, data);
        }

    }

    public static class ForBelt extends ForBlockState {
        public int length;
        public CasingType[] casings;

        public ForBelt() {
        }

        @Override
        public void write(ValueOutput view) {
            super.write(view);
            view.store("Length", Codec.INT, length);
            view.putIntArray("Casing", Arrays.stream(casings).mapToInt(CasingType::ordinal).toArray());
        }

        public static LaunchedItem from(ValueInput view, HolderGetter<Block> holderGetter) {
            return view.read("Length", Codec.INT).map(length -> {
                ForBelt result = new ForBelt();
                result.read(view, holderGetter, length);
                return result;
            }).orElse(null);
        }

        @Override
        void read(ValueInput view, HolderGetter<Block> holderGetter) {
            read(view, holderGetter, view.read("Length", Codec.INT).orElse(0));
        }

        private void read(ValueInput view, HolderGetter<Block> holderGetter, int length) {
            this.length = length;
            int[] intArray = view.getIntArray("Casing").orElseGet(() -> new int[0]);
            casings = new CasingType[length];
            for (int i = 0; i < casings.length; i++)
                casings[i] = i >= intArray.length ? CasingType.NONE : CasingType.values()[Mth.clamp(intArray[i], 0, CasingType.values().length - 1)];
            super.read(view, holderGetter);
        }

        public ForBelt(BlockPos start, BlockPos target, ItemStack stack, BlockState state, CasingType[] casings) {
            super(start, target, stack, state, null);
            this.casings = casings;
            this.length = casings.length;
        }

        @Override
        void place(Level world) {
            boolean isStart = state.getValue(BeltBlock.PART) == BeltPart.START;
            BlockPos offset = BeltBlock.nextSegmentPosition(state, BlockPos.ZERO, isStart);
            int i = length - 1;
            Axis axis = state.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS ? Axis.Y : state.getValue(BeltBlock.HORIZONTAL_FACING).getClockWise()
                .getAxis();
            world.setBlockAndUpdate(target, AllBlocks.SHAFT.defaultBlockState().setValue(AbstractSimpleShaftBlock.AXIS, axis));
            BeltConnectorItem.createBelts(world, target, target.offset(offset.getX() * i, offset.getY() * i, offset.getZ() * i));

            for (int segment = 0; segment < length; segment++) {
                if (casings[segment] == CasingType.NONE)
                    continue;
                BlockPos casingTarget = target.offset(offset.getX() * segment, offset.getY() * segment, offset.getZ() * segment);
                if (world.getBlockEntity(casingTarget) instanceof BeltBlockEntity bbe)
                    bbe.setCasingType(casings[segment]);
            }
        }

    }

    public static class ForEntity extends LaunchedItem {
        public Entity entity;
        private CompoundTag deferredTag;

        ForEntity() {
        }

        public ForEntity(BlockPos start, BlockPos target, ItemStack stack, Entity entity) {
            super(start, target, stack);
            this.entity = entity;
        }

        @Override
        public boolean update(Level world) {
            if (deferredTag != null && entity == null) {
                try {
                    try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(
                        () -> "LaunchedItem.ForEntity",
                        Create.LOGGER
                    )) {
                        ValueInput view = TagValueInput.create(logging, world.registryAccess(), deferredTag);
                        Optional<Entity> loadEntityUnchecked = EntityType.create(view, world, EntitySpawnReason.LOAD);
                        if (loadEntityUnchecked.isEmpty())
                            return true;
                        entity = loadEntityUnchecked.get();
                    }
                } catch (Exception var3) {
                    return true;
                }
                deferredTag = null;
            }
            return super.update(world);
        }

        @Override
        public void write(ValueOutput view) {
            super.write(view);
            if (entity != null) {
                ValueOutput data = view.child("Entity");
                EntityType<?> entityType = entity.getType();
                Identifier id = EntityType.getKey(entityType);
                if (id != null && entityType.canSerialize()) {
                    data.putString("id", id.toString());
                }
                entity.saveWithoutId(data);
            }
        }

        @Override
        void read(ValueInput view, HolderGetter<Block> holderGetter) {
            super.read(view, holderGetter);
            view.read("Entity", CompoundTag.CODEC).ifPresent(nbt -> deferredTag = nbt);
        }

        @Override
        void place(Level world) {
            if (entity != null)
                world.addFreshEntity(entity);
        }

    }

}
