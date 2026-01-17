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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SymmetryWandItem extends Item {

    public SymmetryWandItem(Settings properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();
        if (player == null)
            return ActionResult.PASS;
        ItemStack wand = player.getStackInHand(context.getHand());
        player.getItemCooldownManager().set(wand, 5);
        checkComponents(wand);

        // Shift -> open GUI
        if (player.isSneaking()) {
            if (player.getWorld().isClient) {
                AllClientHandle.INSTANCE.openSymmetryWandScreen(wand, context.getHand());
                player.getItemCooldownManager().set(wand, 5);
            }
            return ActionResult.SUCCESS;
        }

        if (context.getWorld().isClient || context.getHand() != Hand.MAIN_HAND)
            return ActionResult.SUCCESS;

        pos = pos.offset(context.getSide());
        SymmetryMirror previousElement = wand.get(AllDataComponents.SYMMETRY_WAND);

        // No Shift -> Make / Move Mirror
        wand.set(AllDataComponents.SYMMETRY_WAND_ENABLE, true);
        Vec3d pos3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        SymmetryMirror newElement = new PlaneMirror(pos3d);

        if (previousElement instanceof EmptyMirror) {
            newElement.setOrientation((player.getHorizontalFacing() == Direction.NORTH || player.getHorizontalFacing() == Direction.SOUTH) ? PlaneMirror.Align.XY.ordinal() : PlaneMirror.Align.YZ.ordinal());
            newElement.enable = true;
            wand.set(AllDataComponents.SYMMETRY_WAND_ENABLE, true);
        } else {
            previousElement.setPosition(pos3d);

            if (previousElement instanceof PlaneMirror) {
                previousElement.setOrientation((player.getHorizontalFacing() == Direction.NORTH || player.getHorizontalFacing() == Direction.SOUTH) ? PlaneMirror.Align.XY.ordinal() : PlaneMirror.Align.YZ.ordinal());
            }

            if (previousElement instanceof CrossPlaneMirror) {
                float rotation = player.getHeadYaw();
                float abs = Math.abs(rotation % 90);
                boolean diagonal = abs > 22 && abs < 45 + 22;
                previousElement.setOrientation(diagonal ? CrossPlaneMirror.Align.D.ordinal() : CrossPlaneMirror.Align.Y.ordinal());
            }

            newElement = previousElement;
        }

        wand.set(AllDataComponents.SYMMETRY_WAND, newElement);

        player.setStackInHand(context.getHand(), wand);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack wand = playerIn.getStackInHand(handIn);
        checkComponents(wand);

        // Shift -> Open GUI
        if (playerIn.isSneaking()) {
            if (worldIn.isClient) {
                AllClientHandle.INSTANCE.openSymmetryWandScreen(wand, handIn);
                playerIn.getItemCooldownManager().set(wand, 5);
            }
            return ActionResult.SUCCESS;
        }

        // No Shift -> Clear Mirror
        wand.set(AllDataComponents.SYMMETRY_WAND_ENABLE, false);
        return ActionResult.SUCCESS.withNewHandStack(wand);
    }

    private static void checkComponents(ItemStack wand) {
        if (!wand.contains(AllDataComponents.SYMMETRY_WAND)) {
            wand.set(AllDataComponents.SYMMETRY_WAND, new EmptyMirror(new Vec3d(0, 0, 0)));
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
        ServerWorld world,
        ItemStack wand,
        PlayerEntity player,
        BlockPos pos,
        BlockState block,
        Vec3d hitPos,
        boolean canReplaceExisting,
        Direction side,
        Hand hand
    ) {
        checkComponents(wand);
        if (!isEnabled(wand))
            return;
        if (!BlockItem.BLOCK_ITEMS.containsKey(block.getBlock()))
            return;

        Map<BlockPos, Pair<Direction, BlockState>> blockSet = new HashMap<>();
        blockSet.put(pos, Pair.of(side, block));
        SymmetryMirror symmetry = wand.get(AllDataComponents.SYMMETRY_WAND);

        Vec3d mirrorPos = symmetry.getPosition();
        if (mirrorPos.distanceTo(Vec3d.of(pos)) > AllConfigs.server().equipment.maxSymmetryWandRange.get())
            return;

        symmetry.process(blockSet);
        BlockPos to = BlockPos.ofFloored(mirrorPos);
        List<BlockPos> targets = new ArrayList<>();
        targets.add(pos);

        double y = hitPos.getY();
        for (BlockPos position : blockSet.keySet()) {
            if (position.equals(pos))
                continue;

            if (world.canPlace(block, position, ShapeContext.of(player))) {
                Pair<Direction, BlockState> pair = blockSet.get(position);
                Direction direction = pair.getFirst();
                BlockState blockState = pair.getSecond();
                for (Direction face : Iterate.directions)
                    blockState = blockState.getStateForNeighborUpdate(
                        world,
                        world,
                        position,
                        face,
                        position.offset(face),
                        world.getBlockState(position.offset(face)),
                        world.random
                    );

                if (player.isCreative()) {
                    world.setBlockState(position, blockState);
                    targets.add(position);
                    continue;
                }

                BlockState toReplace = world.getBlockState(position);
                if (toReplace.getHardness(world, position) == -1)
                    continue;

                ItemStack current;
                List<Runnable> tasks;
                if (blockState.isOf(AllBlocks.CART_ASSEMBLER)) {
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
                if (!(toReplace.isReplaceable() || toReplace.canReplace(placementContext)))
                    continue;

                wand.set(AllDataComponents.SYMMETRY_WAND_SIMULATE, true);
                ActionResult actionResult = ActionResult.FAIL;
                int count = 0;
                for (int size = current.getCount(); count < size; count++) {
                    actionResult = current.useOnBlock(placementContext);
                    if (!actionResult.isAccepted()) {
                        break;
                    }
                }
                wand.set(AllDataComponents.SYMMETRY_WAND_SIMULATE, false);
                if (actionResult.isAccepted()) {
                    targets.add(position);
                    tasks.forEach(Runnable::run);
                } else if (count != 0) {
                    targets.add(position);
                    tasks.forEach(Runnable::run);
                    player.getInventory().offerOrDrop(placementContext.getStack());
                }
            }
        }

        world.getChunkManager().sendToNearbyPlayers(player, new SymmetryEffectPacket(to, targets));
    }

    private static boolean isHoldingBlock(PlayerEntity player, BlockState block) {
        ItemStack itemBlock = BlockHelper.getRequiredItem(block);
        return player.isHolding(itemBlock.getItem());
    }

    public static void remove(ServerWorld world, ItemStack wand, PlayerEntity player, BlockPos pos, BlockState ogBlock) {
        BlockState air = Blocks.AIR.getDefaultState();
        checkComponents(wand);
        if (!isEnabled(wand))
            return;

        Set<BlockPos> positions = new HashSet<>();
        positions.add(pos);
        SymmetryMirror symmetry = wand.get(AllDataComponents.SYMMETRY_WAND);

        Vec3d mirrorPos = symmetry.getPosition();
        if (mirrorPos.distanceTo(Vec3d.of(pos)) > AllConfigs.server().equipment.maxSymmetryWandRange.get())
            return;

        symmetry.process(positions);

        BlockPos to = BlockPos.ofFloored(mirrorPos);
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
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, position, Block.getRawIdFromState(blockstate));
                world.setBlockState(position, air, Block.NOTIFY_ALL);

                if (noCreative) {
                    ItemStack stack = player.getMainHandStack();
                    if (!stack.isEmpty())
                        stack.postMine(world, blockstate, position, player);
                    BlockEntity blockEntity = blockstate.hasBlockEntity() ? world.getBlockEntity(position) : null;
                    Block.dropStacks(blockstate, world, pos, blockEntity, player, stack); // Add fortune, silk touch and other loot modifiers
                }
            }
        }

        world.getChunkManager().sendToNearbyPlayers(player, new SymmetryEffectPacket(to, targets));
    }

    public static boolean presentInHotbar(PlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0, size = PlayerInventory.getHotbarSize(); i < size; i++)
            if (inv.getStack(i).isOf(AllItems.WAND_OF_SYMMETRY))
                return true;
        return false;
    }

}
