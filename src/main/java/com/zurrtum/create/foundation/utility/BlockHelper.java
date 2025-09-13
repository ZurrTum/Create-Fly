package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.schematic.nbt.PartialSafeNBT;
import com.zurrtum.create.api.schematic.nbt.SafeNbtWriterRegistry;
import com.zurrtum.create.api.schematic.nbt.SafeNbtWriterRegistry.SafeNbtWriter;
import com.zurrtum.create.api.schematic.state.SchematicStateFilter;
import com.zurrtum.create.api.schematic.state.SchematicStateFilterRegistry;
import com.zurrtum.create.api.schematic.state.SchematicStateFilterRegistry.StateFilter;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.blockEntity.IMergeableBE;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.LOGGER;

public class BlockHelper {
    private static final List<IntProperty> COUNT_STATES = List.of(Properties.EGGS, Properties.PICKLES, Properties.CANDLES);

    public static final List<Block> VINELIKE_BLOCKS = List.of(Blocks.VINE, Blocks.GLOW_LICHEN);

    public static final List<BooleanProperty> VINELIKE_STATES = List.of(
        Properties.UP,
        Properties.NORTH,
        Properties.EAST,
        Properties.SOUTH,
        Properties.WEST,
        Properties.DOWN
    );

    public static BlockState setZeroAge(BlockState blockState) {
        if (blockState.contains(Properties.AGE_1))
            return blockState.with(Properties.AGE_1, 0);
        if (blockState.contains(Properties.AGE_2))
            return blockState.with(Properties.AGE_2, 0);
        if (blockState.contains(Properties.AGE_3))
            return blockState.with(Properties.AGE_3, 0);
        if (blockState.contains(Properties.AGE_5))
            return blockState.with(Properties.AGE_5, 0);
        if (blockState.contains(Properties.AGE_7))
            return blockState.with(Properties.AGE_7, 0);
        if (blockState.contains(Properties.AGE_15))
            return blockState.with(Properties.AGE_15, 0);
        if (blockState.contains(Properties.AGE_25))
            return blockState.with(Properties.AGE_25, 0);
        if (blockState.contains(Properties.HONEY_LEVEL))
            return blockState.with(Properties.HONEY_LEVEL, 0);
        if (blockState.contains(Properties.HATCH))
            return blockState.with(Properties.HATCH, 0);
        if (blockState.contains(Properties.STAGE))
            return blockState.with(Properties.STAGE, 0);
        if (blockState.isIn(BlockTags.CAULDRONS))
            return Blocks.CAULDRON.getDefaultState();
        if (blockState.contains(Properties.LEVEL_8))
            return blockState.with(Properties.LEVEL_8, 0);
        if (blockState.contains(Properties.EXTENDED))
            return blockState.with(Properties.EXTENDED, false);
        return blockState;
    }

    public static int findAndRemoveInInventory(BlockState block, PlayerEntity player, int amount) {
        int amountFound = 0;
        Item required = getRequiredItem(block).getItem();

        boolean needsTwo = block.contains(Properties.SLAB_TYPE) && block.get(Properties.SLAB_TYPE) == SlabType.DOUBLE;

        if (needsTwo)
            amount *= 2;

        for (IntProperty property : COUNT_STATES)
            if (block.contains(property))
                amount *= block.get(property);

        if (VINELIKE_BLOCKS.contains(block.getBlock())) {
            int vineCount = 0;

            for (BooleanProperty vineState : VINELIKE_STATES) {
                if (block.contains(vineState) && block.get(vineState)) {
                    vineCount++;
                }
            }

            amount += vineCount - 1;
        }

        {
            // Try held Item first
            int preferredSlot = player.getInventory().getSelectedSlot();
            ItemStack itemstack = player.getInventory().getStack(preferredSlot);
            int count = itemstack.getCount();
            if (itemstack.getItem() == required && count > 0) {
                int taken = Math.min(count, amount - amountFound);
                player.getInventory().setStack(preferredSlot, new ItemStack(itemstack.getItem(), count - taken));
                amountFound += taken;
            }
        }

        // Search inventory
        for (int i = 0; i < player.getInventory().size(); ++i) {
            if (amountFound == amount)
                break;

            ItemStack itemstack = player.getInventory().getStack(i);
            int count = itemstack.getCount();
            if (itemstack.getItem() == required && count > 0) {
                int taken = Math.min(count, amount - amountFound);
                player.getInventory().setStack(i, new ItemStack(itemstack.getItem(), count - taken));
                amountFound += taken;
            }
        }

        if (needsTwo) {
            // Give back 1 if uneven amount was removed
            if (amountFound % 2 != 0)
                player.getInventory().insertStack(new ItemStack(required));
            amountFound /= 2;
        }

        return amountFound;
    }

    private static final Pair<ItemStack, List<Runnable>> EMPTY_FIND = Pair.of(ItemStack.EMPTY, List.of());

    public static Pair<ItemStack, List<Runnable>> findInInventory(BlockState replace, BlockState block, PlayerEntity player) {
        int amount = 1;
        int amountFound = 0;
        Item required = getRequiredItem(block).getItem();

        boolean needsTwo = false;

        boolean replaceable = replace.isReplaceable();
        if (block.contains(Properties.SLAB_TYPE)) {
            SlabType type = block.get(Properties.SLAB_TYPE);
            if (replaceable) {
                if (type == SlabType.DOUBLE) {
                    needsTwo = true;
                    amount = 2;
                }
            } else {
                SlabType replaceType = replace.get(Properties.SLAB_TYPE);
                if (replaceType == type || replaceType == SlabType.DOUBLE) {
                    amount = 0;
                }
            }
        } else if (VINELIKE_BLOCKS.contains(block.getBlock())) {
            replaceable = replaceable && !replace.isOf(block.getBlock());
            int vineCount = 0;

            for (BooleanProperty vineState : VINELIKE_STATES) {
                if (block.contains(vineState) && block.get(vineState) && (replaceable || !replace.get(vineState))) {
                    vineCount++;
                }
            }

            amount += vineCount - 1;
        } else {
            for (IntProperty property : COUNT_STATES) {
                if (block.contains(property)) {
                    amount = block.get(property);
                    if (!replaceable) {
                        amount -= replace.get(property);
                    }
                    break;
                }
            }
        }

        PlayerInventory inventory = player.getInventory();
        List<Runnable> task = new ArrayList<>();
        int preferredSlot = inventory.getSelectedSlot();
        if (amountFound != amount) {
            // Try held Item first
            ItemStack itemstack = inventory.getStack(preferredSlot);
            int count = itemstack.getCount();
            if (itemstack.getItem() == required && count > 0) {
                int taken = Math.min(count, amount - amountFound);
                if (count == taken) {
                    task.add(() -> inventory.setStack(preferredSlot, ItemStack.EMPTY));
                } else {
                    task.add(() -> itemstack.setCount(count - taken));
                }
                amountFound += taken;
            }
        }

        // Search inventory
        if (amountFound != amount) {
            for (int i = 0, size = inventory.size(); i < size; ++i) {
                if (i == preferredSlot) {
                    continue;
                }
                ItemStack itemstack = inventory.getStack(i);
                int count = itemstack.getCount();
                if (itemstack.getItem() == required && count > 0) {
                    int taken = Math.min(count, amount - amountFound);
                    final int slot = i;
                    if (count == taken) {
                        task.add(() -> inventory.setStack(slot, ItemStack.EMPTY));
                    } else {
                        task.add(() -> itemstack.setCount(count - taken));
                    }
                    amountFound += taken;
                    if (amountFound == amount)
                        break;
                }
            }
        }

        if (needsTwo && amountFound != 2) {
            amountFound = 0;
        }

        if (amountFound == 0) {
            return EMPTY_FIND;
        }
        task.add(inventory::markDirty);
        return Pair.of(new ItemStack(required, amountFound), task);
    }

    public static ItemStack getRequiredItem(BlockState state) {
        ItemStack itemStack = new ItemStack(state.getBlock());
        Item item = itemStack.getItem();
        if (item == Items.FARMLAND || item == Items.DIRT_PATH)
            itemStack = new ItemStack(Items.DIRT);
        return itemStack;
    }

    public static void destroyBlock(World world, BlockPos pos, float effectChance) {
        destroyBlock(world, pos, effectChance, stack -> Block.dropStack(world, pos, stack));
    }

    public static void destroyBlock(World world, BlockPos pos, float effectChance, Consumer<ItemStack> droppedItemCallback) {
        destroyBlockAs(world, pos, null, ItemStack.EMPTY, effectChance, droppedItemCallback);
    }

    public static void destroyBlockAs(
        World world,
        BlockPos pos,
        @Nullable PlayerEntity player,
        ItemStack usedTool,
        float effectChance,
        Consumer<ItemStack> droppedItemCallback
    ) {
        FluidState fluidState = world.getFluidState(pos);
        BlockState state = world.getBlockState(pos);

        if (world.random.nextFloat() < effectChance)
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;

        if (player != null) {
            //TODO
            //            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
            //            NeoForge.EVENT_BUS.post(event);
            //            if (event.isCanceled())
            //                return;

            usedTool.postMine(world, state, pos, player);
            player.incrementStat(Stats.MINED.getOrCreateStat(state.getBlock()));
        }

        //TODO check restoringBlockSnapshots
        if (world instanceof ServerWorld serverLevel && serverLevel.getGameRules()
            .getBoolean(GameRules.DO_TILE_DROPS) && (player == null || !player.isCreative())) {
            List<ItemStack> drops = Block.getDroppedStacks(state, serverLevel, pos, blockEntity, player, usedTool);
            if (player != null) {
                //TODO
                //                BlockDropsEvent event = new BlockDropsEvent(serverLevel, pos, state, blockEntity, List.of(), player, usedTool);
                //                NeoForge.EVENT_BUS.post(event);
                //                if (!event.isCanceled()) {
                //                    if (event.getDroppedExperience() > 0)
                //                        state.getBlock().popExperience(serverLevel, pos, event.getDroppedExperience());
                //                }
            }
            for (ItemStack itemStack : drops)
                droppedItemCallback.accept(itemStack);

            // Simulating IceBlock#playerDestroy. Not calling method directly as it would drop item
            // entities as a side-effect
            Registry<Enchantment> enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            if (state.getBlock() instanceof IceBlock && usedTool.getEnchantments()
                .getLevel(enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH)) == 0) {
                if (!world.getDimension().ultrawarm()) {
                    BlockState below = world.getBlockState(pos.down());
                    if (below.blocksMovement() || below.isLiquid()) {
                        fluidState = IceBlock.getMeltedState().getFluidState();
                    }
                }
            }

            state.onStacksDropped(serverLevel, pos, ItemStack.EMPTY, true);
        }

        world.setBlockState(pos, fluidState.getBlockState());
    }

    public static boolean isSolidWall(BlockView reader, BlockPos fromPos, Direction toDirection) {
        return hasBlockSolidSide(reader.getBlockState(fromPos.offset(toDirection)), reader, fromPos.offset(toDirection), toDirection.getOpposite());
    }

    public static boolean noCollisionInSpace(BlockView reader, BlockPos pos) {
        return reader.getBlockState(pos).getCollisionShape(reader, pos).isEmpty();
    }

    private static void placeRailWithoutUpdate(World world, BlockState state, BlockPos target) {
        WorldChunk chunk = world.getWorldChunk(target);
        int idx = chunk.getSectionIndex(target.getY());
        ChunkSection chunksection = chunk.getSection(idx);
        if (chunksection == null) {
            chunksection = new ChunkSection(world.getRegistryManager().getOrThrow(RegistryKeys.BIOME));
            chunk.getSectionArray()[idx] = chunksection;
        }
        BlockState old = chunksection.setBlockState(
            ChunkSectionPos.getLocalCoord(target.getX()),
            ChunkSectionPos.getLocalCoord(target.getY()),
            ChunkSectionPos.getLocalCoord(target.getZ()),
            state
        );
        chunk.markNeedsSaving();
        markAndNotifyBlock(world, target, chunk, old, state, 82);

        world.setBlockState(target, state, Block.NOTIFY_ALL | Block.FORCE_STATE | Block.MOVED);
        BlockPos down = target.down();
        Block sourceBlock = world.getBlockState(down).getBlock();
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, target, sourceBlock, down, false);
        }
        world.updateNeighbor(target, sourceBlock, null);
    }

    public static void markAndNotifyBlock(World world, BlockPos pos, WorldChunk worldChunk, BlockState blockState, BlockState state, int flags) {
        BlockState blockState2 = world.getBlockState(pos);
        if (blockState2 == state) {
            if (blockState != blockState2) {
                world.scheduleBlockRerenderIfNeeded(pos, blockState, blockState2);
            }

            if ((flags & Block.NOTIFY_LISTENERS) != 0 && (!world.isClient || (flags & Block.NO_REDRAW) == 0) && (world.isClient || worldChunk.getLevelType() != null && worldChunk.getLevelType()
                .isAfter(ChunkLevelType.BLOCK_TICKING))) {
                world.updateListeners(pos, blockState, state, flags);
            }

            if ((flags & Block.NOTIFY_NEIGHBORS) != 0) {
                world.updateNeighbors(pos, blockState.getBlock());
                if (!world.isClient && state.hasComparatorOutput()) {
                    world.updateComparators(pos, state.getBlock());
                }
            }

            if ((flags & Block.FORCE_STATE) == 0) {
                int i = flags & ~(Block.NOTIFY_NEIGHBORS | Block.SKIP_DROPS);
                blockState.prepare(world, pos, i, 512 - 1);
                state.updateNeighbors(world, pos, i, 512 - 1);
                state.prepare(world, pos, i, 512 - 1);
            }

            world.onBlockStateChanged(pos, blockState, blockState2);
        }
    }

    public static NbtCompound prepareBlockEntityData(World level, BlockState blockState, BlockEntity blockEntity) {
        NbtCompound data = null;
        if (blockEntity == null)
            return null;
        DynamicRegistryManager access = level.getRegistryManager();
        SafeNbtWriter writer = SafeNbtWriterRegistry.REGISTRY.get(blockEntity.getType());
        if (blockState.isIn(AllBlockTags.SAFE_NBT)) {
            data = blockEntity.createNbtWithIdentifyingData(access);
        } else if (writer != null) {
            data = new NbtCompound();
            writer.writeSafe(blockEntity, data, access);
        } else if (blockEntity instanceof PartialSafeNBT safeNbtBE) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER)) {
                NbtWriteView view = NbtWriteView.create(logging, access);
                safeNbtBE.writeSafe(view);
                data = view.getNbt();
            }
            //TODO
            //        } else if (Mods.FRAMEDBLOCKS.contains(blockState.getBlock())) {
            //            data = FramedBlocksInSchematics.prepareBlockEntityData(blockState, blockEntity);
        }

        return NBTProcessors.process(blockState, blockEntity, data, true);
    }

    public static void placeSchematicBlock(World world, BlockState state, BlockPos target, ItemStack stack, @Nullable NbtCompound data) {
        Block block = state.getBlock();
        BlockEntity existingBlockEntity = world.getBlockEntity(target);
        boolean alreadyPlaced = false;

        StateFilter filter = SchematicStateFilterRegistry.REGISTRY.get(state);
        if (filter != null) {
            state = filter.filterStates(existingBlockEntity, state);
        } else if (block instanceof SchematicStateFilter schematicStateFilter) {
            state = schematicStateFilter.filterStates(existingBlockEntity, state);
        }

        // Piston
        if (state.contains(Properties.EXTENDED))
            state = state.with(Properties.EXTENDED, Boolean.FALSE);
        if (state.contains(Properties.WATERLOGGED))
            state = state.with(Properties.WATERLOGGED, Boolean.FALSE);

        if (block == Blocks.COMPOSTER) {
            state = Blocks.COMPOSTER.getDefaultState();
            //TODO
            //        } else if (block != Blocks.SEA_PICKLE && block instanceof SpecialPlantable specialPlantable) {
            //            alreadyPlaced = true;
            //            if (specialPlantable.canPlacePlantAtPosition(stack, world, target, null))
            //                specialPlantable.spawnPlantAtPosition(stack, world, target, null);
        } else if (state.isIn(BlockTags.CAULDRONS)) {
            state = Blocks.CAULDRON.getDefaultState();
        }

        if (world.getDimension().ultrawarm() && state.getFluidState().isIn(FluidTags.WATER)) {
            int i = target.getX();
            int j = target.getY();
            int k = target.getZ();
            world.playSound(
                null,
                target,
                SoundEvents.BLOCK_FIRE_EXTINGUISH,
                SoundCategory.BLOCKS,
                0.5F,
                2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F
            );

            for (int l = 0; l < 8; ++l) {
                world.addParticleClient(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0D, 0.0D, 0.0D);
            }
            Block.dropStacks(state, world, target);
            return;
        }

        //noinspection StatementWithEmptyBody
        if (alreadyPlaced) {
            // pass
        } else if (state.getBlock() instanceof AbstractRailBlock) {
            placeRailWithoutUpdate(world, state, target);
        } else if (state.isOf(AllBlocks.BELT)) {
            world.setBlockState(target, state, Block.NOTIFY_LISTENERS);
        } else {
            world.setBlockState(target, state, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        }

        if (data != null) {
            if (existingBlockEntity instanceof IMergeableBE mergeable) {
                BlockEntity loaded = BlockEntity.createFromNbt(target, state, data, world.getRegistryManager());
                if (loaded != null) {
                    if (existingBlockEntity.getType().equals(loaded.getType())) {
                        mergeable.accept(loaded);
                        return;
                    }
                }
            }
            BlockEntity blockEntity = world.getBlockEntity(target);
            if (blockEntity != null) {
                data.putInt("x", target.getX());
                data.putInt("y", target.getY());
                data.putInt("z", target.getZ());
                if (blockEntity instanceof KineticBlockEntity kbe)
                    kbe.warnOfMovement();
                if (blockEntity instanceof IMultiBlockEntityContainer imbe)
                    if (!imbe.isController())
                        data.put("Controller", BlockPos.CODEC, imbe.getController());
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER)) {
                    blockEntity.read(NbtReadView.create(logging, world.getRegistryManager(), data));
                }
            }
        }

        try {
            state.getBlock().onPlaced(world, target, state, null, stack);
        } catch (Exception ignored) {
        }
    }

    public static double getBounceMultiplier(Block block) {
        if (block instanceof SlimeBlock)
            return 0.8D;
        if (block instanceof BedBlock)
            return 0.66 * 0.8D;
        return 0;
    }

    public static boolean hasBlockSolidSide(BlockState state, BlockView blockGetter, BlockPos pos, Direction dir) {
        return !state.isIn(BlockTags.LEAVES) && Block.isFaceFullSquare(state.getCollisionShape(blockGetter, pos), dir);
    }

    public static boolean extinguishFire(World world, @Nullable PlayerEntity player, BlockPos pos, Direction dir) {
        pos = pos.offset(dir);
        if (world.getBlockState(pos).getBlock() == Blocks.FIRE) {
            world.syncWorldEvent(player, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
            world.removeBlock(pos, false);
            return true;
        } else {
            return false;
        }
    }

    public static BlockState copyProperties(BlockState fromState, BlockState toState) {
        for (Property<?> property : fromState.getProperties()) {
            toState = copyProperty(property, fromState, toState);
        }
        return toState;
    }

    public static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState fromState, BlockState toState) {
        if (fromState.contains(property) && toState.contains(property)) {
            return toState.with(property, fromState.get(property));
        }
        return toState;
    }

    public static boolean isNotUnheated(BlockState state) {
        if (state.isIn(BlockTags.CAMPFIRES) && state.contains(CampfireBlock.LIT)) {
            return state.get(CampfireBlock.LIT);
        }
        if (state.contains(BlazeBurnerBlock.HEAT_LEVEL)) {
            return state.get(BlazeBurnerBlock.HEAT_LEVEL) != HeatLevel.NONE;
        }
        return true;
    }

    public static ActionResult invokeUse(BlockState state, World level, PlayerEntity player, Hand hand, BlockHitResult ray) {
        ActionResult iteminteractionresult = state.onUseWithItem(player.getStackInHand(hand), level, player, hand, ray);
        if (iteminteractionresult.isAccepted()) {
            return iteminteractionresult;
        }

        if (iteminteractionresult == ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION && hand == Hand.MAIN_HAND) {
            ActionResult interactionresult = state.onUse(level, player, ray);
            if (interactionresult.isAccepted()) {
                return interactionresult;
            }
        }

        return ActionResult.PASS;
    }
}