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
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DeployerHandler {
    private static final Map<BlockPos, List<ItemEntity>> CAPTURED_BLOCK_DROPS = new HashMap<>();
    public static final Map<BlockPos, List<ItemEntity>> CAPTURED_BLOCK_DROPS_VIEW = Collections.unmodifiableMap(CAPTURED_BLOCK_DROPS);

    private static final class ItemUseWorld extends WrappedLevel implements ServerLevelAccessor {
        private final Direction face;
        private final BlockPos pos;
        boolean rayMode = false;

        private ItemUseWorld(ServerLevel level, Direction face, BlockPos pos) {
            super(level);
            this.face = face;
            this.pos = pos;
        }

        @Override
        public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
            return getLevel().getCurrentDifficultyAt(pos);
        }

        @Override
        public ServerLevel getLevel() {
            // This is safe, we always pass ServerLevel in the constructor
            return (ServerLevel) level;
        }

        @Override
        public BlockHitResult clip(ClipContext context) {
            rayMode = true;
            BlockHitResult rayTraceBlocks = super.clip(context);
            rayMode = false;
            return rayTraceBlocks;
        }

        @Override
        public BlockState getBlockState(BlockPos position) {
            if (rayMode && (pos.relative(face.getOpposite(), 3).equals(position) || pos.relative(face.getOpposite(), 1).equals(position)))
                return Blocks.BEDROCK.defaultBlockState();
            return level.getBlockState(position);
        }
    }

    static boolean shouldActivate(ItemStack held, Level world, BlockPos targetPos, @Nullable Direction facing) {
        if (held.getItem() instanceof BlockItem)
            if (world.getBlockState(targetPos).getBlock() == ((BlockItem) held.getItem()).getBlock())
                return false;

        if (held.getItem() instanceof BucketItem bucketItem) {
            Fluid fluid = bucketItem.content;
            if (fluid != Fluids.EMPTY && world.getFluidState(targetPos).getType() == fluid)
                return false;
        }

        return held.isEmpty() || facing != Direction.DOWN || BlockEntityBehaviour.get(
            world,
            targetPos,
            TransportedItemStackHandlerBehaviour.TYPE
        ) == null;
    }

    static void activate(DeployerPlayer player, Vec3 vec, BlockPos clickedPos, Vec3 extensionVector, Mode mode) {
        ServerPlayer serverPlayer = player.cast();
        HashMultimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        ItemStack stack = serverPlayer.getMainHandItem();
        stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()
            .forEach(e -> attributeModifiers.put(e.attribute(), e.modifier()));

        serverPlayer.getAttributes().addTransientAttributeModifiers(attributeModifiers);
        activateInner(player, vec, clickedPos, extensionVector, mode);
        serverPlayer.getAttributes().removeAttributeModifiers(attributeModifiers);
    }

    private static void activateInner(DeployerPlayer player, Vec3 vec, BlockPos clickedPos, Vec3 extensionVector, Mode mode) {
        ServerPlayer serverPlayer = player.cast();
        Vec3 rayOrigin = vec.add(extensionVector.scale(3 / 2f + 1 / 64f));
        Vec3 rayTarget = vec.add(extensionVector.scale(5 / 2f - 1 / 64f));
        serverPlayer.setPos(rayOrigin.x, rayOrigin.y, rayOrigin.z);
        BlockPos pos = BlockPos.containing(vec);
        ItemStack stack = serverPlayer.getMainHandItem();
        Item item = stack.getItem();

        // Check for entities
        final ServerLevel level = serverPlayer.level();
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(clickedPos)).stream()
            .filter(e -> !(e instanceof AbstractContraptionEntity)).toList();
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!entities.isEmpty()) {
            Entity entity = entities.get(level.random.nextInt(entities.size()));
            List<ItemStack> capturedDrops = new ArrayList<>();
            boolean success = false;
            AllSynchedDatas.CAPTURE_DROPS.set(entity, Optional.of(capturedDrops));

            // Use on entity
            if (mode == Mode.USE) {
                InteractionResult cancelResult = null;
                //TODO
                //                ActionResult cancelResult = CommonHooks.onInteractEntity(player, entity, hand);
                //                if (cancelResult == ActionResult.FAIL) {
                //                    entity.captureDrops(null);
                //                    return;
                //                }
                if (cancelResult == null) {
                    if (entity.interact(serverPlayer, hand).consumesAction()) {
                        if (entity instanceof AbstractVillager villager) {
                            if (villager.getTradingPlayer() == serverPlayer)
                                villager.setTradingPlayer(null);
                        }
                        success = true;
                    } else if (entity instanceof LivingEntity livingEntity && stack.interactLivingEntity(serverPlayer, livingEntity, hand)
                        .consumesAction())
                        success = true;
                }
                if (!success && entity instanceof Player playerEntity) {
                    if (stack.has(DataComponents.FOOD)) {
                        FoodProperties foodProperties = stack.get(DataComponents.FOOD);
                        if (foodProperties != null && playerEntity.canEat(foodProperties.canAlwaysEat())) {
                            ItemStack copy = stack.copy();
                            serverPlayer.setItemInHand(hand, stack.finishUsingItem(level, playerEntity));
                            player.setSpawnedItemEffects(copy);
                            success = true;
                        }
                    }
                    if (!success && stack.is(AllItemTags.DEPLOYABLE_DRINK)) {
                        player.setSpawnedItemEffects(stack.copy());
                        serverPlayer.setItemInHand(hand, stack.finishUsingItem(level, playerEntity));
                        success = true;
                    }
                }
            }

            // Punch entity
            if (mode == Mode.PUNCH) {
                serverPlayer.resetAttackStrengthTicker();
                serverPlayer.attack(entity);
                success = true;
            }

            AllSynchedDatas.CAPTURE_DROPS.set(entity, Optional.empty());
            capturedDrops.forEach(e -> serverPlayer.getInventory().placeItemBackInInventory(e));
            if (success)
                return;
        }

        // Shoot ray
        ClipContext rayTraceContext = new ClipContext(
            rayOrigin,
            rayTarget,
            Block.OUTLINE,
            net.minecraft.world.level.ClipContext.Fluid.NONE,
            serverPlayer
        );
        BlockHitResult result = level.clip(rayTraceContext);
        if (result.getBlockPos() != clickedPos)
            result = new BlockHitResult(result.getLocation(), result.getDirection(), clickedPos, result.isInside());
        BlockState clickedState = level.getBlockState(clickedPos);
        Direction face = result.getDirection();
        if (face == null)
            face = Direction.getApproximateNearest(extensionVector.x, extensionVector.y, extensionVector.z).getOpposite();

        // Left click
        if (mode == Mode.PUNCH) {
            if (!level.mayInteract(serverPlayer, clickedPos))
                return;
            if (clickedState.getShape(level, clickedPos).isEmpty()) {
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

            float progress = clickedState.getDestroyProgress(serverPlayer, level, clickedPos) * 16;
            float before = 0;
            Pair<BlockPos, Float> blockBreakingProgress = player.getBlockBreakingProgress();
            if (blockBreakingProgress != null)
                before = blockBreakingProgress.getValue();
            progress += before;
            level.playSound(null, clickedPos, clickedState.getSoundType().getHitSound(), SoundSource.NEUTRAL, .25f, 1);

            if (progress >= 1) {
                tryHarvestBlock(player, player.getInteractionManager(), clickedPos);
                level.destroyBlockProgress(serverPlayer.getId(), clickedPos, -1);
                player.setBlockBreakingProgress(null);
                return;
            }
            if (progress <= 0) {
                player.setBlockBreakingProgress(null);
                return;
            }

            if ((int) (before * 10) != (int) (progress * 10))
                level.destroyBlockProgress(serverPlayer.getId(), clickedPos, (int) (progress * 10));
            player.setBlockBreakingProgress(Pair.of(clickedPos, progress));
            return;
        }

        // Right click
        UseOnContext itemusecontext = new UseOnContext(serverPlayer, hand, result);
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

        boolean holdingSomething = !serverPlayer.getMainHandItem().isEmpty();
        boolean flag1 = !(serverPlayer.isShiftKeyDown() && holdingSomething) || !serverPlayer.getMainHandItem().isEmpty();

        // Use on block
        if (flag1 && safeOnUse(clickedState, level, clickedPos, player, hand, result).consumesAction())
            return;
        if (stack.isEmpty())
            return;
        if (item instanceof CartAssemblerBlockItem && clickedState.canBeReplaced(new BlockPlaceContext(itemusecontext)))
            return;

        // Reposition fire placement for convenience
        if (item == Items.FLINT_AND_STEEL) {
            Direction newFace = result.getDirection();
            BlockPos newPos = result.getBlockPos();
            if (!BaseFireBlock.canBePlacedAt(level, clickedPos, newFace))
                newFace = Direction.UP;
            if (clickedState.isAir())
                newPos = newPos.relative(face.getOpposite());
            result = new BlockHitResult(result.getLocation(), newFace, newPos, result.isInside());
            itemusecontext = new UseOnContext(serverPlayer, hand, result);
        }

        // 'Inert' item use behaviour & block placement
        InteractionResult onItemUse = stack.useOn(itemusecontext);
        if (onItemUse.consumesAction()) {
            if (item instanceof BlockItem bi && (bi.getBlock() instanceof BaseRailBlock || bi.getBlock() instanceof ITrackBlock))
                player.setPlacedTracks(true);
            return;
        }

        if (item == Items.ENDER_PEARL)
            return;
        if (item.builtInRegistryHolder().is(AllItemTags.DEPLOYABLE_DRINK))
            return;

        // buckets create their own ray, We use a fake wall to contain the active area
        Level itemUseWorld = level;
        if (item instanceof BucketItem || item instanceof SandPaperItem)
            itemUseWorld = new ItemUseWorld(level, face, pos);

        InteractionResult onItemRightClick = item.use(itemUseWorld, serverPlayer, hand);

        if (onItemRightClick.consumesAction() && item instanceof MobBucketItem bucketItem)
            bucketItem.checkExtraContent(serverPlayer, level, stack, clickedPos);

        if (onItemRightClick instanceof InteractionResult.Success success) {
            ItemStack resultStack = success.heldItemTransformedTo();
            if (resultStack != null && resultStack != stack || resultStack.getCount() != stack.getCount() || resultStack.getUseDuration(serverPlayer) > 0 || resultStack.getDamageValue() != stack.getDamageValue()) {
                serverPlayer.setItemInHand(hand, resultStack);
            }
        }

        if (stack.getItem() instanceof SandPaperItem) {
            SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
            if (component != null) {
                player.setSpawnedItemEffects(component.item());
                AllSoundEvents.SANDING_SHORT.playOnServer(level, pos, .25f, 1f);
            }
        }

        if (!serverPlayer.getUseItem().isEmpty())
            serverPlayer.setItemInHand(hand, stack.finishUsingItem(level, serverPlayer));

        serverPlayer.stopUsingItem();
    }

    public static boolean tryHarvestBlock(DeployerPlayer player, ServerPlayerGameMode interactionManager, BlockPos pos) {
        // <> PlayerInteractionManager#tryHarvestBlock

        ServerPlayer serverPlayer = player.cast();
        ServerLevel world = serverPlayer.level();
        BlockState blockstate = world.getBlockState(pos);
        GameType gameType = interactionManager.getGameModeForPlayer();

        //TODO
        //        if (CommonHooks.fireBlockBreak(world, gameType, player, pos, blockstate).isCanceled())
        //            return false;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (serverPlayer.blockActionRestricted(world, pos, gameType))
            return false;

        ItemStack prevHeldItem = serverPlayer.getMainHandItem();
        ItemStack heldItem = prevHeldItem.copy();

        boolean canHarvest = serverPlayer.hasCorrectToolForDrops(blockstate) && serverPlayer.mayBuild();
        prevHeldItem.mineBlock(world, blockstate, pos, player.cast());

        BlockPos posUp = pos.above();
        BlockState stateUp = world.getBlockState(posUp);
        if (blockstate.getBlock() instanceof DoublePlantBlock && blockstate.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER && stateUp.getBlock() == blockstate.getBlock() && stateUp.getValue(
            DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            // hack to prevent DoublePlantBlock from dropping a duplicate item
            world.setBlock(
                pos,
                Blocks.AIR.defaultBlockState(),
                net.minecraft.world.level.block.Block.UPDATE_ALL | net.minecraft.world.level.block.Block.UPDATE_SUPPRESS_DROPS
            );
            world.setBlock(
                posUp,
                Blocks.AIR.defaultBlockState(),
                net.minecraft.world.level.block.Block.UPDATE_ALL | net.minecraft.world.level.block.Block.UPDATE_SUPPRESS_DROPS
            );
        } else {
            blockstate.getBlock().playerWillDestroy(world, pos, blockstate, player.cast());
            if (!world.setBlock(pos, world.getFluidState(pos).getType().defaultFluidState().createLegacyBlock(), world.isClientSide() ? 11 : 3))
                return true;
        }

        blockstate.getBlock().destroy(world, pos, blockstate);
        if (!canHarvest)
            return true;

        net.minecraft.world.level.block.Block.getDrops(blockstate, world, pos, blockEntity, player.cast(), prevHeldItem)
            .forEach(item -> serverPlayer.getInventory().placeItemBackInInventory(item));
        blockstate.spawnAfterBreak(world, pos, prevHeldItem, true);
        return true;
    }

    public static InteractionResult safeOnUse(
        BlockState state,
        Level world,
        BlockPos pos,
        DeployerPlayer player,
        InteractionHand hand,
        BlockHitResult ray
    ) {
        List<ItemEntity> drops = new ArrayList<>(4);
        CAPTURED_BLOCK_DROPS.put(pos, drops);
        try {
            InteractionResult result = BlockHelper.invokeUse(state, world, player.cast(), hand, ray);
            for (ItemEntity itemEntity : drops)
                player.cast().getInventory().placeItemBackInInventory(itemEntity.getItem());
            return result;
        } finally {
            CAPTURED_BLOCK_DROPS.remove(pos);
        }
    }

}
