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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Optional;

public abstract class LaunchedItem {

    public int totalTicks;
    public int ticksRemaining;
    public BlockPos target;
    public ItemStack stack;

    private LaunchedItem(BlockPos start, BlockPos target, ItemStack stack) {
        this(target, stack, ticksForDistance(start, target), ticksForDistance(start, target));
    }

    private static int ticksForDistance(BlockPos start, BlockPos target) {
        return (int) (Math.max(10, Math.sqrt(Math.sqrt(target.getSquaredDistance(start))) * 4f));
    }

    LaunchedItem() {
    }

    private LaunchedItem(BlockPos target, ItemStack stack, int ticksLeft, int total) {
        this.target = target;
        this.stack = stack;
        this.totalTicks = total;
        this.ticksRemaining = ticksLeft;
    }

    public boolean update(World world) {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            return false;
        }
        if (world.isClient)
            return false;

        place(world);
        return true;
    }

    public void write(WriteView view) {
        view.putInt("TotalTicks", totalTicks);
        view.putInt("TicksLeft", ticksRemaining);
        if (!stack.isEmpty()) {
            view.put("Stack", ItemStack.CODEC, stack);
        }
        view.put("Target", BlockPos.CODEC, target);
    }

    public static LaunchedItem from(ReadView view, RegistryEntryLookup<Block> holderGetter) {
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

    abstract void place(World world);

    void read(ReadView view, RegistryEntryLookup<Block> holderGetter) {
        target = view.read("Target", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        ticksRemaining = view.getInt("TicksLeft", 0);
        totalTicks = view.getInt("TotalTicks", 0);
        stack = view.read("Stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    public static class ForBlockState extends LaunchedItem {
        public BlockState state;
        public NbtCompound data;

        ForBlockState() {
        }

        public ForBlockState(BlockPos start, BlockPos target, ItemStack stack, BlockState state, NbtCompound data) {
            super(start, target, stack);
            this.state = state;
            this.data = data;
        }

        @Override
        public void write(WriteView view) {
            super.write(view);
            view.put("BlockState", BlockState.CODEC, state);
            if (data != null) {
                data.remove("x");
                data.remove("y");
                data.remove("z");
                data.remove("id");
                view.put("Data", NbtCompound.CODEC, data);
            }
        }

        public static LaunchedItem from(ReadView view, RegistryEntryLookup<Block> holderGetter) {
            return view.read("BlockState", BlockState.CODEC).map(state -> {
                ForBlockState result = new ForBlockState();
                result.read(view, holderGetter, state);
                return result;
            }).orElse(null);
        }

        @Override
        void read(ReadView view, RegistryEntryLookup<Block> holderGetter) {
            read(view, holderGetter, view.read("BlockState", BlockState.CODEC).orElseGet(Blocks.AIR::getDefaultState));
        }

        private void read(ReadView view, RegistryEntryLookup<Block> holderGetter, BlockState state) {
            super.read(view, holderGetter);
            this.state = state;
            view.read("Data", NbtCompound.CODEC).ifPresent(nbt -> data = nbt);
        }

        @Override
        void place(World world) {
            BlockHelper.placeSchematicBlock(world, state, target, stack, data);
        }

    }

    public static class ForBelt extends ForBlockState {
        public int length;
        public CasingType[] casings;

        public ForBelt() {
        }

        @Override
        public void write(WriteView view) {
            super.write(view);
            view.put("Length", Codec.INT, length);
            view.putIntArray("Casing", Arrays.stream(casings).mapToInt(CasingType::ordinal).toArray());
        }

        public static LaunchedItem from(ReadView view, RegistryEntryLookup<Block> holderGetter) {
            return view.read("Length", Codec.INT).map(length -> {
                ForBelt result = new ForBelt();
                result.read(view, holderGetter, length);
                return result;
            }).orElse(null);
        }

        @Override
        void read(ReadView view, RegistryEntryLookup<Block> holderGetter) {
            read(view, holderGetter, view.read("Length", Codec.INT).orElse(0));
        }

        private void read(ReadView view, RegistryEntryLookup<Block> holderGetter, int length) {
            this.length = length;
            int[] intArray = view.getOptionalIntArray("Casing").orElseGet(() -> new int[0]);
            casings = new CasingType[length];
            for (int i = 0; i < casings.length; i++)
                casings[i] = i >= intArray.length ? CasingType.NONE : CasingType.values()[MathHelper.clamp(
                    intArray[i],
                    0,
                    CasingType.values().length - 1
                )];
            super.read(view, holderGetter);
        }

        public ForBelt(BlockPos start, BlockPos target, ItemStack stack, BlockState state, CasingType[] casings) {
            super(start, target, stack, state, null);
            this.casings = casings;
            this.length = casings.length;
        }

        @Override
        void place(World world) {
            boolean isStart = state.get(BeltBlock.PART) == BeltPart.START;
            BlockPos offset = BeltBlock.nextSegmentPosition(state, BlockPos.ORIGIN, isStart);
            int i = length - 1;
            Axis axis = state.get(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS ? Axis.Y : state.get(BeltBlock.HORIZONTAL_FACING).rotateYClockwise()
                .getAxis();
            world.setBlockState(target, AllBlocks.SHAFT.getDefaultState().with(AbstractSimpleShaftBlock.AXIS, axis));
            BeltConnectorItem.createBelts(world, target, target.add(offset.getX() * i, offset.getY() * i, offset.getZ() * i));

            for (int segment = 0; segment < length; segment++) {
                if (casings[segment] == CasingType.NONE)
                    continue;
                BlockPos casingTarget = target.add(offset.getX() * segment, offset.getY() * segment, offset.getZ() * segment);
                if (world.getBlockEntity(casingTarget) instanceof BeltBlockEntity bbe)
                    bbe.setCasingType(casings[segment]);
            }
        }

    }

    public static class ForEntity extends LaunchedItem {
        public Entity entity;
        private NbtCompound deferredTag;

        ForEntity() {
        }

        public ForEntity(BlockPos start, BlockPos target, ItemStack stack, Entity entity) {
            super(start, target, stack);
            this.entity = entity;
        }

        @Override
        public boolean update(World world) {
            if (deferredTag != null && entity == null) {
                try {
                    try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "LaunchedItem.ForEntity", Create.LOGGER)) {
                        ReadView view = NbtReadView.create(logging, world.getRegistryManager(), deferredTag);
                        Optional<Entity> loadEntityUnchecked = EntityType.getEntityFromData(view, world, SpawnReason.LOAD);
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
        public void write(WriteView view) {
            super.write(view);
            if (entity != null) {
                WriteView data = view.get("Entity");
                EntityType<?> entityType = entity.getType();
                Identifier id = EntityType.getId(entityType);
                if (id != null && entityType.isSaveable()) {
                    data.putString("id", id.toString());
                }
                entity.writeData(data);
            }
        }

        @Override
        void read(ReadView view, RegistryEntryLookup<Block> holderGetter) {
            super.read(view, holderGetter);
            view.read("Entity", NbtCompound.CODEC).ifPresent(nbt -> deferredTag = nbt);
        }

        @Override
        void place(World world) {
            if (entity != null)
                world.spawnEntity(entity);
        }

    }

}
