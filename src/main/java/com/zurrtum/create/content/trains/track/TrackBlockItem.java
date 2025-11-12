package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.track.TrackPlacement.PlacementInfo;
import com.zurrtum.create.infrastructure.component.ConnectingFrom;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackBlockItem extends BlockItem {

    public TrackBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && isFoil(stack)) {
            return clearSelection(stack, world, player);
        } else {
            return super.use(world, player, usedHand);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = pContext.getPlayer();

        if (player == null)
            return super.useOn(pContext);
        if (pContext.getHand() == InteractionHand.OFF_HAND)
            return super.useOn(pContext);

        Vec3 lookAngle = player.getLookAngle();

        if (!isFoil(stack)) {
            if (state.getBlock() instanceof TrackBlock track && track.getTrackAxes(level, pos, state).size() > 1) {
                if (!level.isClientSide())
                    player.displayClientMessage(Component.translatable("create.track.junction_start").withStyle(ChatFormatting.RED), true);
                return InteractionResult.SUCCESS;
            }

            if (level.getBlockEntity(pos) instanceof TrackBlockEntity tbe && tbe.isTilted()) {
                if (!level.isClientSide())
                    player.displayClientMessage(Component.translatable("create.track.turn_start").withStyle(ChatFormatting.RED), true);
                return InteractionResult.SUCCESS;
            }

            if (select(level, pos, lookAngle, stack)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
                return InteractionResult.SUCCESS;
            }
            return super.useOn(pContext);

        } else if (player.isShiftKeyDown()) {
            return clearSelection(stack, level, player);
        }

        boolean placing = !(state.getBlock() instanceof ITrackBlock);
        boolean extend = stack.getOrDefault(AllDataComponents.TRACK_EXTENDED_CURVE, false);
        stack.remove(AllDataComponents.TRACK_EXTENDED_CURVE);

        if (placing) {
            if (!state.canBeReplaced())
                pos = pos.relative(pContext.getClickedFace());
            state = getPlacementState(pContext);
            if (state == null)
                return InteractionResult.FAIL;
        }

        ItemStack offhandItem = player.getOffhandItem();
        boolean hasGirder = offhandItem.is(AllItems.METAL_GIRDER);
        PlacementInfo info = TrackPlacement.tryConnect(level, player, pos, state, stack, hasGirder, extend);

        if (info.message != null && !level.isClientSide())
            player.displayClientMessage(Component.translatable("create." + info.message), true);
        if (!info.valid) {
            AllSoundEvents.DENY.playFrom(player, 1, 1);
            return InteractionResult.FAIL;
        }

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        stack = player.getMainHandItem();
        if (stack.is(AllItemTags.TRACKS)) {
            stack.remove(AllDataComponents.TRACK_CONNECTING_FROM);
            stack.remove(AllDataComponents.TRACK_EXTENDED_CURVE);
            player.setItemInHand(pContext.getHand(), stack);
        }

        SoundType soundtype = state.getSoundType();
        if (soundtype != null)
            level.playSound(
                null,
                pos,
                soundtype.getPlaceSound(),
                SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F
            );

        return InteractionResult.SUCCESS;
    }

    public static InteractionResult clearSelection(ItemStack stack, Level level, Player player) {
        if (level.isClientSide()) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.75f, 1.0f);
        } else {
            player.displayClientMessage(Component.translatable("create.track.selection_cleared"), true);
            stack.remove(AllDataComponents.TRACK_CONNECTING_FROM);
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    public BlockState getPlacementState(UseOnContext pContext) {
        return getPlacementState(updatePlacementContext(new BlockPlaceContext(pContext)));
    }

    public static boolean select(LevelAccessor world, BlockPos pos, Vec3 lookVec, ItemStack heldItem) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof ITrackBlock track))
            return false;

        Pair<Vec3, AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(world, pos, blockState, lookVec);
        Vec3 axis = nearestTrackAxis.getFirst().scale(nearestTrackAxis.getSecond() == AxisDirection.POSITIVE ? -1 : 1);
        Vec3 end = track.getCurveStart(world, pos, blockState, axis);
        Vec3 normal = track.getUpNormal(world, pos, blockState).normalize();

        heldItem.set(AllDataComponents.TRACK_CONNECTING_FROM, new ConnectingFrom(pos, axis, normal, end));
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(AllDataComponents.TRACK_CONNECTING_FROM);
    }

}
