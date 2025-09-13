package com.zurrtum.create.content.decoration.girder;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class GirderPlacementHelper implements IPlacementHelper {

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return stack -> stack.isOf(AllItems.METAL_GIRDER);
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return state -> state.isOf(AllBlocks.METAL_GIRDER) || state.isOf(AllBlocks.METAL_GIRDER_ENCASED_SHAFT);
    }

    private boolean canExtendToward(BlockState state, Direction side) {
        Axis axis = side.getAxis();
        if (state.getBlock() instanceof GirderBlock) {
            boolean x = state.get(GirderBlock.X);
            boolean z = state.get(GirderBlock.Z);
            if (!x && !z)
                return axis == Axis.Y;
            if (x && z)
                return true;
            return axis == (x ? Axis.X : Axis.Z);
        }

        if (state.getBlock() instanceof GirderEncasedShaftBlock)
            return axis != Axis.Y && axis != state.get(GirderEncasedShaftBlock.HORIZONTAL_AXIS);

        return false;
    }

    private int attachedPoles(World world, BlockPos pos, Direction direction) {
        BlockPos checkPos = pos.offset(direction);
        BlockState state = world.getBlockState(checkPos);
        int count = 0;
        while (canExtendToward(state, direction)) {
            count++;
            checkPos = checkPos.offset(direction);
            state = world.getBlockState(checkPos);
        }
        return count;
    }

    private BlockState withAxis(BlockState state, Axis axis) {
        if (state.getBlock() instanceof GirderBlock)
            return state.with(GirderBlock.X, axis == Axis.X).with(GirderBlock.Z, axis == Axis.Z).with(GirderBlock.AXIS, axis);
        if (state.getBlock() instanceof GirderEncasedShaftBlock && axis.isHorizontal())
            return state.with(GirderEncasedShaftBlock.HORIZONTAL_AXIS, axis == Axis.X ? Axis.Z : Axis.X);
        return state;
    }

    @Override
    public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
        List<Direction> directions = IPlacementHelper.orderedByDistance(pos, ray.getPos(), dir -> canExtendToward(state, dir));
        for (Direction dir : directions) {
            int range = AllConfigs.server().equipment.placementAssistRange.get();
            if (player != null) {
                EntityAttributeInstance reach = player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
                if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier.id()))
                    range += 4;
            }
            int poles = attachedPoles(world, pos, dir);
            if (poles >= range)
                continue;

            BlockPos newPos = pos.offset(dir, poles + 1);
            BlockState newState = world.getBlockState(newPos);

            if (!newState.isReplaceable())
                continue;

            return PlacementOffset.success(newPos, bState -> Block.postProcessState(withAxis(bState, dir.getAxis()), world, newPos));
        }

        return PlacementOffset.fail();
    }

}
