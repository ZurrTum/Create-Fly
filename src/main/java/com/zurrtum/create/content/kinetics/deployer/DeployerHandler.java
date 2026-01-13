package com.zurrtum.create.content.kinetics.deployer;

import com.google.common.collect.HashMultimap;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockItem;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DeployerHandler {
    private static final Map<BlockPos, List<ItemEntity>> CAPTURED_BLOCK_DROPS = new HashMap<>();
    public static final Map<BlockPos, List<ItemEntity>> CAPTURED_BLOCK_DROPS_VIEW = Collections.unmodifiableMap(CAPTURED_BLOCK_DROPS);

    private static final class ItemUseWorld extends WrappedLevel implements ServerWorldAccess {
        private final Direction face;
        private final BlockPos pos;
        boolean rayMode = false;

        private ItemUseWorld(ServerWorld level, Direction face, BlockPos pos) {
            super(level);
            this.face = face;
            this.pos = pos;
        }

        @Override
        public ServerWorld toServerWorld() {
            // This is safe, we always pass ServerLevel in the constructor
            return (ServerWorld) level;
        }

        @Override
        public BlockHitResult raycast(RaycastContext context) {
            rayMode = true;
            BlockHitResult rayTraceBlocks = super.raycast(context);
            rayMode = false;
            return rayTraceBlocks;
        }

        @Override
        public BlockState getBlockState(BlockPos position) {
            if (rayMode && (pos.offset(face.getOpposite(), 3).equals(position) || pos.offset(face.getOpposite(), 1).equals(position)))
                return Blocks.BEDROCK.getDefaultState();
            return level.getBlockState(position);
        }
    }

    static boolean shouldActivate(ItemStack held, World world, BlockPos targetPos, @Nullable Direction facing) {
        if (held.getItem() instanceof BlockItem)
            if (world.getBlockState(targetPos).getBlock() == ((BlockItem) held.getItem()).getBlock())
                return false;

        if (held.getItem() instanceof BucketItem bucketItem) {
            Fluid fluid = bucketItem.fluid;
            if (fluid != Fluids.EMPTY && world.getFluidState(targetPos).getFluid() == fluid)
                return false;
        }

        return held.isEmpty() || facing != Direction.DOWN || BlockEntityBehaviour.get(
            world,
            targetPos,
            TransportedItemStackHandlerBehaviour.TYPE
        ) == null;
    }

    static void activate(DeployerPlayer player, Vec3d vec, BlockPos clickedPos, Vec3d extensionVector, Mode mode) {
        ServerPlayerEntity serverPlayer = player.cast();
        HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifiers = HashMultimap.create();
        ItemStack stack = serverPlayer.getMainHandStack();
        stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()
            .forEach(e -> attributeModifiers.put(e.attribute(), e.modifier()));

        serverPlayer.getAttributes().addTemporaryModifiers(attributeModifiers);
        activateInner(player, vec, clickedPos, extensionVector, mode);
        serverPlayer.getAttributes().removeModifiers(attributeModifiers);
    }

    private static void activateInner(DeployerPlayer player, Vec3d vec, BlockPos clickedPos, Vec3d extensionVector, Mode mode) {
        ServerPlayerEntity serverPlayer = player.cast();
        Vec3d rayOrigin = vec.add(extensionVector.multiply(3 / 2f + 1 / 64f));
        Vec3d rayTarget = vec.add(extensionVector.multiply(5 / 2f - 1 / 64f));
        serverPlayer.setPosition(rayOrigin.x, rayOrigin.y, rayOrigin.z);
        BlockPos pos = BlockPos.ofFloored(vec);
        ItemStack stack = serverPlayer.getMainHandStack();
        Item item = stack.getItem();

        // Check for entities
        final ServerWorld level = serverPlayer.getWorld();
        List<Entity> entities = level.getNonSpectatingEntities(Entity.class, new Box(clickedPos)).stream()
            .filter(e -> !(e instanceof AbstractContraptionEntity)).toList();
        Hand hand = Hand.MAIN_HAND;
        if (!entities.isEmpty()) {
            Entity entity = entities.get(level.random.nextInt(entities.size()));
            List<ItemStack> capturedDrops = new ArrayList<>();
            boolean success = false;
            AllSynchedDatas.CAPTURE_DROPS.set(entity, Optional.of(capturedDrops));

            // Use on entity
            if (mode == Mode.USE) {
                ActionResult cancelResult = null;
                //TODO
                //                ActionResult cancelResult = CommonHooks.onInteractEntity(player, entity, hand);
                //                if (cancelResult == ActionResult.FAIL) {
                //                    entity.captureDrops(null);
                //                    return;
                //                }
                if (cancelResult == null) {
                    if (entity.interact(serverPlayer, hand).isAccepted()) {
                        if (entity instanceof MerchantEntity villager) {
                            if (villager.getCustomer() == serverPlayer)
                                villager.setCustomer(null);
                        }
                        success = true;
                    } else if (entity instanceof LivingEntity livingEntity && stack.useOnEntity(serverPlayer, livingEntity, hand).isAccepted())
                        success = true;
                }
                if (!success && entity instanceof PlayerEntity playerEntity) {
                    if (stack.contains(DataComponentTypes.FOOD)) {
                        FoodComponent foodProperties = stack.get(DataComponentTypes.FOOD);
                        if (foodProperties != null && playerEntity.canConsume(foodProperties.canAlwaysEat())) {
                            ItemStack copy = stack.copy();
                            serverPlayer.setStackInHand(hand, stack.finishUsing(level, playerEntity));
                            player.setSpawnedItemEffects(copy);
                            success = true;
                        }
                    }
                    if (!success && stack.isIn(AllItemTags.DEPLOYABLE_DRINK)) {
                        player.setSpawnedItemEffects(stack.copy());
                        serverPlayer.setStackInHand(hand, stack.finishUsing(level, playerEntity));
                        success = true;
                    }
                }
            }

            // Punch entity
            if (mode == Mode.PUNCH) {
                serverPlayer.resetLastAttackedTicks();
                serverPlayer.attack(entity);
                success = true;
            }

            AllSynchedDatas.CAPTURE_DROPS.set(entity, Optional.empty());
            capturedDrops.forEach(e -> serverPlayer.getInventory().offerOrDrop(e));
            if (success)
                return;
        }

        // Shoot ray
        RaycastContext rayTraceContext = new RaycastContext(rayOrigin, rayTarget, ShapeType.OUTLINE, FluidHandling.NONE, serverPlayer);
        BlockHitResult result = level.raycast(rayTraceContext);
        if (result.getBlockPos() != clickedPos)
            result = new BlockHitResult(result.getPos(), result.getSide(), clickedPos, result.isInsideBlock());
        BlockState clickedState = level.getBlockState(clickedPos);
        Direction face = result.getSide();
        if (face == null)
            face = Direction.getFacing(extensionVector.x, extensionVector.y, extensionVector.z).getOpposite();

        // Left click
        if (mode == Mode.PUNCH) {
            if (!level.canEntityModifyAt(serverPlayer, clickedPos))
                return;
            if (clickedState.getOutlineShape(level, clickedPos).isEmpty()) {
                player.setBlockBreakingProgress(null);
                return;
            }
            //TODO
            //            LeftClickBlock event = CommonHooks.onLeftClickBlock(player, clickedPos, face, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
            //            if (event.isCanceled())
            //                return;
            if (BlockHelper.extinguishFire(level, serverPlayer, clickedPos, face))
                return;
            //TODO
            //            if (event.getUseBlock() != TriState.FALSE)
            //                clickedState.attack(level, clickedPos, player);
            if (stack.isEmpty())
                return;

            float progress = clickedState.calcBlockBreakingDelta(serverPlayer, level, clickedPos) * 16;
            float before = 0;
            Pair<BlockPos, Float> blockBreakingProgress = player.getBlockBreakingProgress();
            if (blockBreakingProgress != null)
                before = blockBreakingProgress.getValue();
            progress += before;
            level.playSound(null, clickedPos, clickedState.getSoundGroup().getHitSound(), SoundCategory.NEUTRAL, .25f, 1);

            if (progress >= 1) {
                tryHarvestBlock(player, player.getInteractionManager(), clickedPos);
                level.setBlockBreakingInfo(serverPlayer.getId(), clickedPos, -1);
                player.setBlockBreakingProgress(null);
                return;
            }
            if (progress <= 0) {
                player.setBlockBreakingProgress(null);
                return;
            }

            if ((int) (before * 10) != (int) (progress * 10))
                level.setBlockBreakingInfo(serverPlayer.getId(), clickedPos, (int) (progress * 10));
            player.setBlockBreakingProgress(Pair.of(clickedPos, progress));
            return;
        }

        // Right click
        ItemUsageContext itemusecontext = new ItemUsageContext(serverPlayer, hand, result);
        //TODO
        //        TriState useBlock = TriState.DEFAULT;
        //        TriState useItem = TriState.DEFAULT;
        //        if (!clickedState.getShape(level, clickedPos).isEmpty()) {
        //            RightClickBlock event = CommonHooks.onRightClickBlock(player, hand, clickedPos, result);
        //            useBlock = event.getUseBlock();
        //            useItem = event.getUseItem();
        //        }

        // Item has custom active use
        //        if (useItem != TriState.FALSE) {
        //            ActionResult actionresult = stack.onItemUseFirst(itemusecontext);
        //            if (actionresult != ActionResult.PASS)
        //                return;
        //        }

        boolean holdingSomething = !serverPlayer.getMainHandStack().isEmpty();
        boolean flag1 = !(serverPlayer.isSneaking() && holdingSomething) || !serverPlayer.getMainHandStack().isEmpty();

        // Use on block
        if (flag1 && safeOnUse(clickedState, level, clickedPos, player, hand, result).isAccepted())
            return;
        if (stack.isEmpty())
            return;
        if (item instanceof CartAssemblerBlockItem && clickedState.canReplace(new ItemPlacementContext(itemusecontext)))
            return;

        // Reposition fire placement for convenience
        if (item == Items.FLINT_AND_STEEL) {
            Direction newFace = result.getSide();
            BlockPos newPos = result.getBlockPos();
            if (!AbstractFireBlock.canPlaceAt(level, clickedPos, newFace))
                newFace = Direction.UP;
            if (clickedState.isAir())
                newPos = newPos.offset(face.getOpposite());
            result = new BlockHitResult(result.getPos(), newFace, newPos, result.isInsideBlock());
            itemusecontext = new ItemUsageContext(serverPlayer, hand, result);
        }

        // 'Inert' item use behaviour & block placement
        ActionResult onItemUse = stack.useOnBlock(itemusecontext);
        if (onItemUse.isAccepted()) {
            if (item instanceof BlockItem bi && (bi.getBlock() instanceof AbstractRailBlock || bi.getBlock() instanceof ITrackBlock))
                player.setPlacedTracks(true);
            return;
        }

        if (item == Items.ENDER_PEARL)
            return;
        if (item.getRegistryEntry().isIn(AllItemTags.DEPLOYABLE_DRINK))
            return;

        // buckets create their own ray, We use a fake wall to contain the active area
        World itemUseWorld = level;
        if (item instanceof BucketItem || item instanceof SandPaperItem)
            itemUseWorld = new ItemUseWorld(level, face, pos);

        ActionResult onItemRightClick = item.use(itemUseWorld, serverPlayer, hand);

        if (onItemRightClick.isAccepted() && item instanceof EntityBucketItem bucketItem)
            bucketItem.onEmptied(serverPlayer, level, stack, clickedPos);

        if (onItemRightClick instanceof ActionResult.Success success) {
            ItemStack resultStack = success.getNewHandStack();
            if (resultStack != null && resultStack != stack || resultStack.getCount() != stack.getCount() || resultStack.getMaxUseTime(serverPlayer) > 0 || resultStack.getDamage() != stack.getDamage()) {
                serverPlayer.setStackInHand(hand, resultStack);
            }
        }

        if (stack.getItem() instanceof SandPaperItem) {
            SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
            if (component != null) {
                player.setSpawnedItemEffects(component.item());
                AllSoundEvents.SANDING_SHORT.playOnServer(level, pos, .25f, 1f);
            }
        }

        if (!serverPlayer.getActiveItem().isEmpty())
            serverPlayer.setStackInHand(hand, stack.finishUsing(level, serverPlayer));

        serverPlayer.clearActiveItem();
    }

    public static boolean tryHarvestBlock(DeployerPlayer player, ServerPlayerInteractionManager interactionManager, BlockPos pos) {
        // <> PlayerInteractionManager#tryHarvestBlock

        ServerPlayerEntity serverPlayer = player.cast();
        ServerWorld world = serverPlayer.getWorld();
        BlockState blockstate = world.getBlockState(pos);
        GameMode gameType = interactionManager.getGameMode();

        //TODO
        //        if (CommonHooks.fireBlockBreak(world, gameType, player, pos, blockstate).isCanceled())
        //            return false;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (serverPlayer.isBlockBreakingRestricted(world, pos, gameType))
            return false;

        ItemStack prevHeldItem = serverPlayer.getMainHandStack();
        ItemStack heldItem = prevHeldItem.copy();

        boolean canHarvest = serverPlayer.canHarvest(blockstate) && serverPlayer.canModifyBlocks();
        prevHeldItem.postMine(world, blockstate, pos, player.cast());

        BlockPos posUp = pos.up();
        BlockState stateUp = world.getBlockState(posUp);
        if (blockstate.getBlock() instanceof TallPlantBlock && blockstate.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER && stateUp.getBlock() == blockstate.getBlock() && stateUp.get(
            TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            // hack to prevent DoublePlantBlock from dropping a duplicate item
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
            world.setBlockState(posUp, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
        } else {
            blockstate.getBlock().onBreak(world, pos, blockstate, player.cast());
            if (!world.setBlockState(pos, world.getFluidState(pos).getFluid().getDefaultState().getBlockState(), world.isClient ? 11 : 3))
                return true;
        }

        blockstate.getBlock().onBroken(world, pos, blockstate);
        if (!canHarvest)
            return true;

        Block.getDroppedStacks(blockstate, world, pos, blockEntity, player.cast(), prevHeldItem)
            .forEach(item -> serverPlayer.getInventory().offerOrDrop(item));
        blockstate.onStacksDropped(world, pos, prevHeldItem, true);
        return true;
    }

    public static ActionResult safeOnUse(BlockState state, World world, BlockPos pos, DeployerPlayer player, Hand hand, BlockHitResult ray) {
        List<ItemEntity> drops = new ArrayList<>(4);
        CAPTURED_BLOCK_DROPS.put(pos, drops);
        try {
            ActionResult result = BlockHelper.invokeUse(state, world, player.cast(), hand, ray);
            for (ItemEntity itemEntity : drops)
                player.cast().getInventory().offerOrDrop(itemEntity.getStack());
            return result;
        } finally {
            CAPTURED_BLOCK_DROPS.remove(pos);
        }
    }

}
