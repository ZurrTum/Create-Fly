package com.zurrtum.create.foundation.placement;

import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class PoleHelper<T extends Comparable<T>> implements IPlacementHelper {

    protected final Predicate<BlockState> statePredicate;
    protected final Property<T> property;
    protected final Function<BlockState, Direction.Axis> axisFunction;

    public PoleHelper(Predicate<BlockState> statePredicate, Function<BlockState, Direction.Axis> axisFunction, Property<T> property) {
        this.statePredicate = statePredicate;
        this.axisFunction = axisFunction;
        this.property = property;
    }

    public boolean matchesAxis(BlockState state, Direction.Axis axis) {
        if (!statePredicate.test(state))
            return false;

        return axisFunction.apply(state) == axis;
    }

    public int attachedPoles(World world, BlockPos pos, Direction direction) {
        BlockPos checkPos = pos.offset(direction);
        BlockState state = world.getBlockState(checkPos);
        int count = 0;
        while (matchesAxis(state, direction.getAxis())) {
            count++;
            checkPos = checkPos.offset(direction);
            state = world.getBlockState(checkPos);
        }
        return count;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return this.statePredicate;
    }

    @Override
    public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
        List<Direction> directions = IPlacementHelper.orderedByDistance(pos, ray.getPos(), dir -> dir.getAxis() == axisFunction.apply(state));
        for (Direction dir : directions) {
            int range = AllConfigs.server().equipment.placementAssistRange.get();
            if (player != null) {
                EntityAttributeInstance reach = player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
                //TODO
                //                if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier.id()))
                //                    range += 4;
            }
            int poles = attachedPoles(world, pos, dir);
            if (poles >= range)
                continue;

            BlockPos newPos = pos.offset(dir, poles + 1);
            BlockState newState = world.getBlockState(newPos);

            if (newState.isReplaceable())
                return PlacementOffset.success(newPos, bState -> bState.with(property, state.get(property)));

        }

        return PlacementOffset.fail();
    }
}
