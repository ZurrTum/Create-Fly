package com.zurrtum.create.content.equipment.symmetryWand;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.CrossPlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.PlaneMirror;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.s2c.SymmetryEffectPacket;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SymmetryWandItem extends Item {

    public SymmetryWandItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null)
            return InteractionResult.PASS;
        ItemStack wand = player.getItemInHand(context.getHand());
        player.getCooldowns().addCooldown(wand, 5);
        checkComponents(wand);

        // Shift -> open GUI
        if (player.isShiftKeyDown()) {
            if (player.level().isClientSide()) {
                AllClientHandle.INSTANCE.openSymmetryWandScreen(wand, context.getHand());
            }
            return InteractionResult.SUCCESS;
        }

        if (context.getLevel().isClientSide() || context.getHand() != InteractionHand.MAIN_HAND)
            return InteractionResult.SUCCESS;

        pos = pos.relative(context.getClickedFace());
        SymmetryMirror previousElement = wand.get(AllDataComponents.SYMMETRY_WAND);

        // No Shift -> Make / Move Mirror
        wand.set(AllDataComponents.SYMMETRY_WAND_ENABLE, true);
        Vec3 pos3d = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        SymmetryMirror newElement = new PlaneMirror(pos3d);

        if (previousElement instanceof EmptyMirror) {
            newElement.setOrientation((player.getDirection() == Direction.NORTH || player.getDirection() == Direction.SOUTH) ? PlaneMirror.Align.XY.ordinal() : PlaneMirror.Align.YZ.ordinal());
            newElement.enable = true;
            wand.set(AllDataComponents.SYMMETRY_WAND_ENABLE, true);
        } else {
            previousElement.setPosition(pos3d);

            if (previousElement instanceof PlaneMirror) {
                previousElement.setOrientation((player.getDirection() == Direction.NORTH || player.getDirection() == Direction.SOUTH) ? PlaneMirror.Align.XY.ordinal() : PlaneMirror.Align.YZ.ordinal());
            }

            if (previousElement instanceof CrossPlaneMirror) {
                float rotation = player.getYHeadRot();
                float abs = Math.abs(rotation % 90);
                boolean diagonal = abs > 22 && abs < 45 + 22;
                previousElement.setOrientation(diagonal ? CrossPlaneMirror.Align.D.ordinal() : CrossPlaneMirror.Align.Y.ordinal());
            }

            newElement = previousElement;
        }

        wand.set(AllDataComponents.SYMMETRY_WAND, newElement);

        player.setItemInHand(context.getHand(), wand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack wand = playerIn.getItemInHand(handIn);
        checkComponents(wand);

        // Shift -> Open GUI
        if (playerIn.isShiftKeyDown()) {
            if (worldIn.isClientSide()) {
                AllClientHandle.INSTANCE.openSymmetryWandScreen(wand, handIn);
                playerIn.getCooldowns().addCooldown(wand, 5);
            }
            return InteractionResult.SUCCESS;
        }

        // No Shift -> Clear Mirror
        wand.set(AllDataComponents.SYMMETRY_WAND_ENABLE, false);
        return InteractionResult.SUCCESS.heldItemTransformedTo(wand);
    }

    private static void checkComponents(ItemStack wand) {
        if (!wand.has(AllDataComponents.SYMMETRY_WAND)) {
            wand.set(AllDataComponents.SYMMETRY_WAND, new EmptyMirror(new Vec3(0, 0, 0)));
            wand.set(AllDataComponents.SYMMETRY_WAND_ENABLE, false);
        }
    }

    public static boolean isEnabled(ItemStack stack) {
        checkComponents(stack);
        return stack.getOrDefault(AllDataComponents.SYMMETRY_WAND_ENABLE, false) && !stack.getOrDefault(
            AllDataComponents.SYMMETRY_WAND_SIMULATE,
            false
        );
    }

    public static SymmetryMirror getMirror(ItemStack stack) {
        checkComponents(stack);
        return stack.get(AllDataComponents.SYMMETRY_WAND);
    }

    public static void configureSettings(ItemStack stack, SymmetryMirror mirror) {
        checkComponents(stack);
        stack.set(AllDataComponents.SYMMETRY_WAND, mirror);
    }

    public static void apply(
        ServerLevel world,
        ItemStack wand,
        Player player,
        BlockPos pos,
        BlockState block,
        Vec3 hitPos,
        boolean canReplaceExisting,
        Direction side,
        InteractionHand hand
    ) {
        checkComponents(wand);
        if (!isEnabled(wand))
            return;
        if (!BlockItem.BY_BLOCK.containsKey(block.getBlock()))
            return;

        Map<BlockPos, Pair<Direction, BlockState>> blockSet = new HashMap<>();
        blockSet.put(pos, Pair.of(side, block));
        SymmetryMirror symmetry = wand.get(AllDataComponents.SYMMETRY_WAND);

        Vec3 mirrorPos = symmetry.getPosition();
        if (mirrorPos.distanceTo(Vec3.atLowerCornerOf(pos)) > AllConfigs.server().equipment.maxSymmetryWandRange.get())
            return;

        symmetry.process(blockSet);
        BlockPos to = BlockPos.containing(mirrorPos);
        List<BlockPos> targets = new ArrayList<>();
        targets.add(pos);

        double y = hitPos.y();
        for (BlockPos position : blockSet.keySet()) {
            if (position.equals(pos))
                continue;

            if (world.isUnobstructed(block, position, CollisionContext.of(player))) {
                Pair<Direction, BlockState> pair = blockSet.get(position);
                Direction direction = pair.getFirst();
                BlockState blockState = pair.getSecond();
                for (Direction face : Iterate.directions)
                    blockState = blockState.updateShape(
                        world,
                        world,
                        position,
                        face,
                        position.relative(face),
                        world.getBlockState(position.relative(face)),
                        world.getRandom()
                    );

                if (player.isCreative()) {
                    world.setBlockAndUpdate(position, blockState);
                    targets.add(position);
                    continue;
                }

                BlockState toReplace = world.getBlockState(position);
                if (toReplace.getDestroySpeed(world, position) == -1)
                    continue;

                ItemStack current;
                List<Runnable> tasks;
                if (blockState.is(AllBlocks.CART_ASSEMBLER)) {
                    BlockState railBlock = CartAssemblerBlock.getRailBlock(blockState);
                    Pair<ItemStack, List<Runnable>> findRail = BlockHelper.findInInventory(toReplace, railBlock, player);
                    ItemStack rail = findRail.getFirst();
                    if (rail.isEmpty()) {
                        continue;
                    }
                    tasks = findRail.getSecond();
                    Pair<ItemStack, List<Runnable>> findBlock = BlockHelper.findInInventory(toReplace, blockState, player);
                    ItemStack cartAssembler = findBlock.getFirst();
                    if (cartAssembler.isEmpty()) {
                        current = rail;
                    } else {
                        tasks.addAll(findBlock.getSecond());
                        current = cartAssembler;
                    }
                } else {
                    Pair<ItemStack, List<Runnable>> find = BlockHelper.findInInventory(toReplace, blockState, player);
                    current = find.getFirst();
                    if (current.isEmpty()) {
                        continue;
                    }
                    tasks = find.getSecond();
                }

                SymmetryPlacementContext placementContext = new SymmetryPlacementContext(
                    world,
                    player,
                    hand,
                    current,
                    position,
                    direction,
                    y,
                    canReplaceExisting,
                    toReplace,
                    blockState
                );
                if (!(toReplace.canBeReplaced() || toReplace.canBeReplaced(placementContext)))
                    continue;

                wand.set(AllDataComponents.SYMMETRY_WAND_SIMULATE, true);
                InteractionResult actionResult = InteractionResult.FAIL;
                int count = 0;
                for (int size = current.getCount(); count < size; count++) {
                    actionResult = current.useOn(placementContext);
                    if (!actionResult.consumesAction()) {
                        break;
                    }
                }
                wand.set(AllDataComponents.SYMMETRY_WAND_SIMULATE, false);
                if (actionResult.consumesAction()) {
                    targets.add(position);
                    tasks.forEach(Runnable::run);
                } else if (count != 0) {
                    targets.add(position);
                    tasks.forEach(Runnable::run);
                    player.getInventory().placeItemBackInInventory(placementContext.getItemInHand());
                }
            }
        }

        world.getChunkSource().sendToTrackingPlayersAndSelf(player, new SymmetryEffectPacket(to, targets));
    }

    private static boolean isHoldingBlock(Player player, BlockState block) {
        ItemStack itemBlock = BlockHelper.getRequiredItem(block);
        return player.isHolding(itemBlock.getItem());
    }

    public static void remove(ServerLevel world, ItemStack wand, Player player, BlockPos pos, BlockState ogBlock) {
        BlockState air = Blocks.AIR.defaultBlockState();
        checkComponents(wand);
        if (!isEnabled(wand))
            return;

        Set<BlockPos> positions = new HashSet<>();
        positions.add(pos);
        SymmetryMirror symmetry = wand.get(AllDataComponents.SYMMETRY_WAND);

        Vec3 mirrorPos = symmetry.getPosition();
        if (mirrorPos.distanceTo(Vec3.atLowerCornerOf(pos)) > AllConfigs.server().equipment.maxSymmetryWandRange.get())
            return;

        symmetry.process(positions);

        BlockPos to = BlockPos.containing(mirrorPos);
        List<BlockPos> targets = new ArrayList<>();

        targets.add(pos);
        boolean noCreative = !player.isCreative();
        for (BlockPos position : positions) {
            if (noCreative && ogBlock.getBlock() != world.getBlockState(position).getBlock())
                continue;
            if (position.equals(pos))
                continue;

            BlockState blockstate = world.getBlockState(position);
            if (!blockstate.isAir()) {
                targets.add(position);
                world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, position, Block.getId(blockstate));
                world.setBlock(position, air, Block.UPDATE_ALL);

                if (noCreative) {
                    ItemStack stack = player.getMainHandItem();
                    if (!stack.isEmpty())
                        stack.mineBlock(world, blockstate, position, player);
                    BlockEntity blockEntity = blockstate.hasBlockEntity() ? world.getBlockEntity(position) : null;
                    Block.dropResources(blockstate, world, pos, blockEntity, player, stack); // Add fortune, silk touch and other loot modifiers
                }
            }
        }

        world.getChunkSource().sendToTrackingPlayersAndSelf(player, new SymmetryEffectPacket(to, targets));
    }

    public static boolean presentInHotbar(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0, size = Inventory.getSelectionSize(); i < size; i++)
            if (inv.getItem(i).is(AllItems.WAND_OF_SYMMETRY))
                return true;
        return false;
    }

}
