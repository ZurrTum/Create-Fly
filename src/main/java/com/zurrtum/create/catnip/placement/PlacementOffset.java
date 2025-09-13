package com.zurrtum.create.catnip.placement;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PlacementOffset {

    private final boolean success;
    private Vec3i pos;
    private Function<BlockState, BlockState> stateTransform;
    @Nullable
    private BlockState ghostState;

    private PlacementOffset(boolean success) {
        this.success = success;
        this.pos = BlockPos.ZERO;
        this.stateTransform = Function.identity();
        this.ghostState = null;
    }

    public static PlacementOffset fail() {
        return new PlacementOffset(false);
    }

    public static PlacementOffset success() {
        return new PlacementOffset(true);
    }

    public static PlacementOffset success(Vec3i pos) {
        return success().at(pos);
    }

    public static PlacementOffset success(Vec3i pos, Function<BlockState, BlockState> transform) {
        return success().at(pos).withTransform(transform);
    }

    public PlacementOffset at(Vec3i pos) {
        this.pos = pos;
        return this;
    }

    public PlacementOffset withTransform(Function<BlockState, BlockState> stateTransform) {
        this.stateTransform = stateTransform;
        return this;
    }

    public PlacementOffset withGhostState(BlockState ghostState) {
        this.ghostState = ghostState;
        return this;
    }

    public boolean isSuccessful() {
        return success;
    }

    public Vec3i getPos() {
        return pos;
    }

    public BlockPos getBlockPos() {
        if (pos instanceof BlockPos)
            return (BlockPos) pos;

        return new BlockPos(pos);
    }

    public Function<BlockState, BlockState> getTransform() {
        return stateTransform;
    }

    public boolean hasGhostState() {
        return ghostState != null;
    }

    @Nullable
    public BlockState getGhostState() {
        return ghostState;
    }

    public boolean isReplaceable(World world) {
        if (!success)
            return false;

        return world.getBlockState(new BlockPos(pos)).isReplaceable();
    }

    public ActionResult placeInWorld(World world, BlockItem blockItem, PlayerEntity player, Hand hand) {

        if (!isReplaceable(world))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (world.isClient)
            return ActionResult.SUCCESS;

        BlockPos newPos = new BlockPos(pos);
        ItemStack stackBefore = player.getStackInHand(hand).copy();

        if (!world.canEntityModifyAt(player, newPos))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        BlockState newState = stateTransform.apply(blockItem.getBlock().getDefaultState());
        if (newState.contains(Properties.WATERLOGGED)) {
            FluidState fluidState = world.getFluidState(newPos);
            newState = newState.with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }

        world.setBlockState(newPos, newState);
        BlockSoundGroup soundtype = newState.getSoundGroup();
        world.playSound(
            null,
            newPos,
            soundtype.getPlaceSound(),
            SoundCategory.BLOCKS,
            (soundtype.getVolume() + 1.0F) / 2.0F,
            soundtype.getPitch() * 0.8F
        );
        world.emitGameEvent(GameEvent.BLOCK_PLACE, newPos, GameEvent.Emitter.of(player, newState));

        player.incrementStat(Stats.USED.getOrCreateStat(blockItem));
        newState.getBlock().onPlaced(world, newPos, newState, player, stackBefore);

        if (player instanceof ServerPlayerEntity serverPlayer)
            Criteria.PLACED_BLOCK.trigger(serverPlayer, newPos, player.getStackInHand(hand));

        if (!player.isCreative())
            player.getStackInHand(hand).decrement(1);

        return ActionResult.SUCCESS;
    }
}
