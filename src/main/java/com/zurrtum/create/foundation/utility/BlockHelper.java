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
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.LOGGER;

public class BlockHelper {
    private static final List<IntegerProperty> COUNT_STATES = List.of(
        BlockStateProperties.EGGS,
        BlockStateProperties.PICKLES,
        BlockStateProperties.CANDLES
    );

    public static final List<Block> VINELIKE_BLOCKS = List.of(Blocks.VINE, Blocks.GLOW_LICHEN);

    public static final List<BooleanProperty> VINELIKE_STATES = List.of(
        BlockStateProperties.UP,
        BlockStateProperties.NORTH,
        BlockStateProperties.EAST,
        BlockStateProperties.SOUTH,
        BlockStateProperties.WEST,
        BlockStateProperties.DOWN
    );

    public static BlockState setZeroAge(BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.AGE_1))
            return blockState.setValue(BlockStateProperties.AGE_1, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_2))
            return blockState.setValue(BlockStateProperties.AGE_2, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_3))
            return blockState.setValue(BlockStateProperties.AGE_3, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_5))
            return blockState.setValue(BlockStateProperties.AGE_5, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_7))
            return blockState.setValue(BlockStateProperties.AGE_7, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_15))
            return blockState.setValue(BlockStateProperties.AGE_15, 0);
        if (blockState.hasProperty(BlockStateProperties.AGE_25))
            return blockState.setValue(BlockStateProperties.AGE_25, 0);
        if (blockState.hasProperty(BlockStateProperties.LEVEL_HONEY))
            return blockState.setValue(BlockStateProperties.LEVEL_HONEY, 0);
        if (blockState.hasProperty(BlockStateProperties.HATCH))
            return blockState.setValue(BlockStateProperties.HATCH, 0);
        if (blockState.hasProperty(BlockStateProperties.STAGE))
            return blockState.setValue(BlockStateProperties.STAGE, 0);
        if (blockState.is(BlockTags.CAULDRONS))
            return Blocks.CAULDRON.defaultBlockState();
        if (blockState.hasProperty(BlockStateProperties.LEVEL_COMPOSTER))
            return blockState.setValue(BlockStateProperties.LEVEL_COMPOSTER, 0);
        if (blockState.hasProperty(BlockStateProperties.EXTENDED))
            return blockState.setValue(BlockStateProperties.EXTENDED, false);
        return blockState;
    }

    public static int findAndRemoveInInventory(BlockState block, Player player, int amount) {
        int amountFound = 0;
        Item required = getRequiredItem(block).getItem();

        boolean needsTwo = block.hasProperty(BlockStateProperties.SLAB_TYPE) && block.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;

        if (needsTwo)
            amount *= 2;

        for (IntegerProperty property : COUNT_STATES)
            if (block.hasProperty(property))
                amount *= block.getValue(property);

        if (VINELIKE_BLOCKS.contains(block.getBlock())) {
            int vineCount = 0;

            for (BooleanProperty vineState : VINELIKE_STATES) {
                if (block.hasProperty(vineState) && block.getValue(vineState)) {
                    vineCount++;
                }
            }

            amount += vineCount - 1;
        }

        {
            // Try held Item first
            int preferredSlot = player.getInventory().getSelectedSlot();
            ItemStack itemstack = player.getInventory().getItem(preferredSlot);
            int count = itemstack.getCount();
            if (itemstack.getItem() == required && count > 0) {
                int taken = Math.min(count, amount - amountFound);
                player.getInventory().setItem(preferredSlot, new ItemStack(itemstack.getItem(), count - taken));
                amountFound += taken;
            }
        }

        // Search inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            if (amountFound == amount)
                break;

            ItemStack itemstack = player.getInventory().getItem(i);
            int count = itemstack.getCount();
            if (itemstack.getItem() == required && count > 0) {
                int taken = Math.min(count, amount - amountFound);
                player.getInventory().setItem(i, new ItemStack(itemstack.getItem(), count - taken));
                amountFound += taken;
            }
        }

        if (needsTwo) {
            // Give back 1 if uneven amount was removed
            if (amountFound % 2 != 0)
                player.getInventory().add(new ItemStack(required));
            amountFound /= 2;
        }

        return amountFound;
    }

    private static final Pair<ItemStack, List<Runnable>> EMPTY_FIND = Pair.of(ItemStack.EMPTY, List.of());

    public static Pair<ItemStack, List<Runnable>> findInInventory(BlockState replace, BlockState block, Player player) {
        int amount = 1;
        int amountFound = 0;
        Item required = getRequiredItem(block).getItem();

        boolean needsTwo = false;

        boolean replaceable = replace.canBeReplaced();
        if (block.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            SlabType type = block.getValue(BlockStateProperties.SLAB_TYPE);
            if (replaceable) {
                if (type == SlabType.DOUBLE) {
                    needsTwo = true;
                    amount = 2;
                }
            } else {
                SlabType replaceType = replace.getValue(BlockStateProperties.SLAB_TYPE);
                if (replaceType == type || replaceType == SlabType.DOUBLE) {
                    amount = 0;
                }
            }
        } else if (VINELIKE_BLOCKS.contains(block.getBlock())) {
            replaceable = replaceable && !replace.is(block.getBlock());
            int vineCount = 0;

            for (BooleanProperty vineState : VINELIKE_STATES) {
                if (block.hasProperty(vineState) && block.getValue(vineState) && (replaceable || !replace.getValue(vineState))) {
                    vineCount++;
                }
            }

            amount += vineCount - 1;
        } else {
            for (IntegerProperty property : COUNT_STATES) {
                if (block.hasProperty(property)) {
                    amount = block.getValue(property);
                    if (!replaceable) {
                        amount -= replace.getValue(property);
                    }
                    break;
                }
            }
        }

        Inventory inventory = player.getInventory();
        List<Runnable> task = new ArrayList<>();
        int preferredSlot = inventory.getSelectedSlot();
        if (amountFound != amount) {
            // Try held Item first
            ItemStack itemstack = inventory.getItem(preferredSlot);
            int count = itemstack.getCount();
            if (itemstack.getItem() == required && count > 0) {
                int taken = Math.min(count, amount - amountFound);
                if (count == taken) {
                    task.add(() -> inventory.setItem(preferredSlot, ItemStack.EMPTY));
                } else {
                    task.add(() -> itemstack.setCount(count - taken));
                }
                amountFound += taken;
            }
        }

        // Search inventory
        if (amountFound != amount) {
            for (int i = 0, size = inventory.getContainerSize(); i < size; ++i) {
                if (i == preferredSlot) {
                    continue;
                }
                ItemStack itemstack = inventory.getItem(i);
                int count = itemstack.getCount();
                if (itemstack.getItem() == required && count > 0) {
                    int taken = Math.min(count, amount - amountFound);
                    final int slot = i;
                    if (count == taken) {
                        task.add(() -> inventory.setItem(slot, ItemStack.EMPTY));
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
        task.add(inventory::setChanged);
        return Pair.of(new ItemStack(required, amountFound), task);
    }

    public static ItemStack getRequiredItem(BlockState state) {
        ItemStack itemStack = new ItemStack(state.getBlock());
        Item item = itemStack.getItem();
        if (item == Items.FARMLAND || item == Items.DIRT_PATH)
            itemStack = new ItemStack(Items.DIRT);
        return itemStack;
    }

    public static void destroyBlock(Level world, BlockPos pos, float effectChance) {
        destroyBlock(world, pos, effectChance, stack -> Block.popResource(world, pos, stack));
    }

    public static void destroyBlock(Level world, BlockPos pos, float effectChance, Consumer<ItemStack> droppedItemCallback) {
        destroyBlockAs(world, pos, null, ItemStack.EMPTY, effectChance, droppedItemCallback);
    }

    public static void destroyBlockAs(
        Level world,
        BlockPos pos,
        @Nullable Player player,
        ItemStack usedTool,
        float effectChance,
        Consumer<ItemStack> droppedItemCallback
    ) {
        FluidState fluidState = world.getFluidState(pos);
        BlockState state = world.getBlockState(pos);

        if (world.random.nextFloat() < effectChance)
            world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;

        if (player != null) {
            //TODO
            //            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
            //            NeoForge.EVENT_BUS.post(event);
            //            if (event.isCanceled())
            //                return;

            usedTool.mineBlock(world, state, pos, player);
            player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        }

        //TODO check restoringBlockSnapshots
        if (world instanceof ServerLevel serverLevel && serverLevel.getGameRules()
            .getBoolean(GameRules.RULE_DOBLOCKDROPS) && (player == null || !player.isCreative())) {
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, usedTool);
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
            Registry<Enchantment> enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            if (state.getBlock() instanceof IceBlock && usedTool.getEnchantments()
                .getLevel(enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH)) == 0) {
                if (!world.dimensionType().ultraWarm()) {
                    BlockState below = world.getBlockState(pos.below());
                    if (below.blocksMotion() || below.liquid()) {
                        fluidState = IceBlock.meltsInto().getFluidState();
                    }
                }
            }

            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
        }

        world.setBlockAndUpdate(pos, fluidState.createLegacyBlock());
    }

    public static boolean isSolidWall(BlockGetter reader, BlockPos fromPos, Direction toDirection) {
        return hasBlockSolidSide(
            reader.getBlockState(fromPos.relative(toDirection)),
            reader,
            fromPos.relative(toDirection),
            toDirection.getOpposite()
        );
    }

    public static boolean noCollisionInSpace(BlockGetter reader, BlockPos pos) {
        return reader.getBlockState(pos).getCollisionShape(reader, pos).isEmpty();
    }

    private static void placeRailWithoutUpdate(Level world, BlockState state, BlockPos target) {
        LevelChunk chunk = world.getChunkAt(target);
        int idx = chunk.getSectionIndex(target.getY());
        LevelChunkSection chunksection = chunk.getSection(idx);
        if (chunksection == null) {
            chunksection = new LevelChunkSection(world.palettedContainerFactory());
            chunk.getSections()[idx] = chunksection;
        }
        BlockState old = chunksection.setBlockState(
            SectionPos.sectionRelative(target.getX()),
            SectionPos.sectionRelative(target.getY()),
            SectionPos.sectionRelative(target.getZ()),
            state
        );
        chunk.markUnsaved();
        markAndNotifyBlock(world, target, chunk, old, state, 82);

        world.setBlock(target, state, Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON);
        BlockPos down = target.below();
        Block sourceBlock = world.getBlockState(down).getBlock();
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, world, target, sourceBlock, down, false);
        }
        world.neighborChanged(target, sourceBlock, null);
    }

    public static void markAndNotifyBlock(Level world, BlockPos pos, LevelChunk worldChunk, BlockState blockState, BlockState state, int flags) {
        BlockState blockState2 = world.getBlockState(pos);
        if (blockState2 == state) {
            if (blockState != blockState2) {
                world.setBlocksDirty(pos, blockState, blockState2);
            }

            if ((flags & Block.UPDATE_CLIENTS) != 0 && (!world.isClientSide() || (flags & Block.UPDATE_INVISIBLE) == 0) && (world.isClientSide() || worldChunk.getFullStatus() != null && worldChunk.getFullStatus()
                .isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
                world.sendBlockUpdated(pos, blockState, state, flags);
            }

            if ((flags & Block.UPDATE_NEIGHBORS) != 0) {
                world.updateNeighborsAt(pos, blockState.getBlock());
                if (!world.isClientSide() && state.hasAnalogOutputSignal()) {
                    world.updateNeighbourForOutputSignal(pos, state.getBlock());
                }
            }

            if ((flags & Block.UPDATE_KNOWN_SHAPE) == 0) {
                int i = flags & ~(Block.UPDATE_NEIGHBORS | Block.UPDATE_SUPPRESS_DROPS);
                blockState.updateIndirectNeighbourShapes(world, pos, i, 512 - 1);
                state.updateNeighbourShapes(world, pos, i, 512 - 1);
                state.updateIndirectNeighbourShapes(world, pos, i, 512 - 1);
            }

            world.updatePOIOnBlockStateChange(pos, blockState, blockState2);
        }
    }

    public static CompoundTag prepareBlockEntityData(Level level, BlockState blockState, BlockEntity blockEntity) {
        CompoundTag data = null;
        if (blockEntity == null)
            return null;
        RegistryAccess access = level.registryAccess();
        SafeNbtWriter writer = SafeNbtWriterRegistry.REGISTRY.get(blockEntity.getType());
        if (blockState.is(AllBlockTags.SAFE_NBT)) {
            data = blockEntity.saveWithFullMetadata(access);
        } else if (writer != null) {
            data = new CompoundTag();
            writer.writeSafe(blockEntity, data, access);
        } else if (blockEntity instanceof PartialSafeNBT safeNbtBE) {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
                TagValueOutput view = TagValueOutput.createWithContext(logging, access);
                safeNbtBE.writeSafe(view);
                data = view.buildResult();
            }
            //TODO
            //        } else if (Mods.FRAMEDBLOCKS.contains(blockState.getBlock())) {
            //            data = FramedBlocksInSchematics.prepareBlockEntityData(blockState, blockEntity);
        }

        return NBTProcessors.process(blockState, blockEntity, data, true);
    }

    public static void placeSchematicBlock(Level world, BlockState state, BlockPos target, ItemStack stack, @Nullable CompoundTag data) {
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
        if (state.hasProperty(BlockStateProperties.EXTENDED))
            state = state.setValue(BlockStateProperties.EXTENDED, Boolean.FALSE);
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE);

        if (block == Blocks.COMPOSTER) {
            state = Blocks.COMPOSTER.defaultBlockState();
            //TODO
            //        } else if (block != Blocks.SEA_PICKLE && block instanceof SpecialPlantable specialPlantable) {
            //            alreadyPlaced = true;
            //            if (specialPlantable.canPlacePlantAtPosition(stack, world, target, null))
            //                specialPlantable.spawnPlantAtPosition(stack, world, target, null);
        } else if (state.is(BlockTags.CAULDRONS)) {
            state = Blocks.CAULDRON.defaultBlockState();
        }

        if (world.dimensionType().ultraWarm() && state.getFluidState().is(FluidTags.WATER)) {
            int i = target.getX();
            int j = target.getY();
            int k = target.getZ();
            world.playSound(
                null,
                target,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.5F,
                2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F
            );

            for (int l = 0; l < 8; ++l) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0D, 0.0D, 0.0D);
            }
            Block.dropResources(state, world, target);
            return;
        }

        //noinspection StatementWithEmptyBody
        if (alreadyPlaced) {
            // pass
        } else if (state.getBlock() instanceof BaseRailBlock) {
            placeRailWithoutUpdate(world, state, target);
        } else if (state.is(AllBlocks.BELT)) {
            world.setBlock(target, state, Block.UPDATE_CLIENTS);
        } else {
            world.setBlock(target, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }

        if (data != null) {
            if (existingBlockEntity instanceof IMergeableBE mergeable) {
                BlockEntity loaded = BlockEntity.loadStatic(target, state, data, world.registryAccess());
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
                        data.store("Controller", BlockPos.CODEC, imbe.getController());
                try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
                    blockEntity.loadWithComponents(TagValueInput.create(logging, world.registryAccess(), data));
                }
            }
        }

        try {
            state.getBlock().setPlacedBy(world, target, state, null, stack);
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

    public static boolean hasBlockSolidSide(BlockState state, BlockGetter blockGetter, BlockPos pos, Direction dir) {
        return !state.is(BlockTags.LEAVES) && Block.isFaceFull(state.getCollisionShape(blockGetter, pos), dir);
    }

    public static boolean extinguishFire(Level world, @Nullable Player player, BlockPos pos, Direction dir) {
        pos = pos.relative(dir);
        if (world.getBlockState(pos).getBlock() == Blocks.FIRE) {
            world.levelEvent(player, LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
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
        if (fromState.hasProperty(property) && toState.hasProperty(property)) {
            return toState.setValue(property, fromState.getValue(property));
        }
        return toState;
    }

    public static boolean isNotUnheated(BlockState state) {
        if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT)) {
            return state.getValue(CampfireBlock.LIT);
        }
        if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return state.getValue(BlazeBurnerBlock.HEAT_LEVEL) != HeatLevel.NONE;
        }
        return true;
    }

    public static InteractionResult invokeUse(BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult ray) {
        InteractionResult iteminteractionresult = state.useItemOn(player.getItemInHand(hand), level, player, hand, ray);
        if (iteminteractionresult.consumesAction()) {
            return iteminteractionresult;
        }

        if (iteminteractionresult == InteractionResult.TRY_WITH_EMPTY_HAND && hand == InteractionHand.MAIN_HAND) {
            InteractionResult interactionresult = state.useWithoutItem(level, player, ray);
            if (interactionresult.consumesAction()) {
                return interactionresult;
            }
        }

        return InteractionResult.PASS;
    }
}