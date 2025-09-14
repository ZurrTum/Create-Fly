package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.track.TrackPlacement.PlacementInfo;
import com.zurrtum.create.infrastructure.component.ConnectingFrom;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class TrackBlockItem extends BlockItem {

    public TrackBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand usedHand) {
        ItemStack stack = player.getStackInHand(usedHand);
        if (player.isSneaking() && hasGlint(stack)) {
            return clearSelection(stack, world, player);
        } else {
            return super.use(world, player, usedHand);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext pContext) {
        ItemStack stack = pContext.getStack();
        BlockPos pos = pContext.getBlockPos();
        World level = pContext.getWorld();
        BlockState state = level.getBlockState(pos);
        PlayerEntity player = pContext.getPlayer();

        if (player == null)
            return super.useOnBlock(pContext);
        if (pContext.getHand() == Hand.OFF_HAND)
            return super.useOnBlock(pContext);

        Vec3d lookAngle = player.getRotationVector();

        if (!hasGlint(stack)) {
            if (state.getBlock() instanceof TrackBlock track && track.getTrackAxes(level, pos, state).size() > 1) {
                if (!level.isClient)
                    player.sendMessage(Text.translatable("create.track.junction_start").formatted(Formatting.RED), true);
                return ActionResult.SUCCESS;
            }

            if (level.getBlockEntity(pos) instanceof TrackBlockEntity tbe && tbe.isTilted()) {
                if (!level.isClient)
                    player.sendMessage(Text.translatable("create.track.turn_start").formatted(Formatting.RED), true);
                return ActionResult.SUCCESS;
            }

            if (select(level, pos, lookAngle, stack)) {
                level.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.75f, 1);
                return ActionResult.SUCCESS;
            }
            return super.useOnBlock(pContext);

        } else if (player.isSneaking()) {
            return clearSelection(stack, level, player);
        }

        boolean placing = !(state.getBlock() instanceof ITrackBlock);
        boolean extend = stack.getOrDefault(AllDataComponents.TRACK_EXTENDED_CURVE, false);
        stack.remove(AllDataComponents.TRACK_EXTENDED_CURVE);

        if (placing) {
            if (!state.isReplaceable())
                pos = pos.offset(pContext.getSide());
            state = getPlacementState(pContext);
            if (state == null)
                return ActionResult.FAIL;
        }

        ItemStack offhandItem = player.getOffHandStack();
        boolean hasGirder = offhandItem.isOf(AllItems.METAL_GIRDER);
        PlacementInfo info = TrackPlacement.tryConnect(level, player, pos, state, stack, hasGirder, extend);

        if (info.message != null && !level.isClient)
            player.sendMessage(Text.translatable("create." + info.message), true);
        if (!info.valid) {
            AllSoundEvents.DENY.playFrom(player, 1, 1);
            return ActionResult.FAIL;
        }

        if (level.isClient)
            return ActionResult.SUCCESS;

        stack = player.getMainHandStack();
        if (stack.isIn(AllItemTags.TRACKS)) {
            stack.remove(AllDataComponents.TRACK_CONNECTING_FROM);
            stack.remove(AllDataComponents.TRACK_EXTENDED_CURVE);
            player.setStackInHand(pContext.getHand(), stack);
        }

        BlockSoundGroup soundtype = state.getSoundGroup();
        if (soundtype != null)
            level.playSound(
                null,
                pos,
                soundtype.getPlaceSound(),
                SoundCategory.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F
            );

        return ActionResult.SUCCESS;
    }

    public static ActionResult clearSelection(ItemStack stack, World level, PlayerEntity player) {
        if (level.isClient) {
            level.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 0.75f, 1.0f);
        } else {
            player.sendMessage(Text.translatable("create.track.selection_cleared"), true);
            stack.remove(AllDataComponents.TRACK_CONNECTING_FROM);
        }
        return ActionResult.SUCCESS.withNewHandStack(stack);
    }

    public BlockState getPlacementState(ItemUsageContext pContext) {
        return getPlacementState(getPlacementContext(new ItemPlacementContext(pContext)));
    }

    public static boolean select(WorldAccess world, BlockPos pos, Vec3d lookVec, ItemStack heldItem) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof ITrackBlock track))
            return false;

        Pair<Vec3d, AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(world, pos, blockState, lookVec);
        Vec3d axis = nearestTrackAxis.getFirst().multiply(nearestTrackAxis.getSecond() == AxisDirection.POSITIVE ? -1 : 1);
        Vec3d end = track.getCurveStart(world, pos, blockState, axis);
        Vec3d normal = track.getUpNormal(world, pos, blockState).normalize();

        heldItem.set(AllDataComponents.TRACK_CONNECTING_FROM, new ConnectingFrom(pos, axis, normal, end));
        return true;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.contains(AllDataComponents.TRACK_CONNECTING_FROM);
    }

}
