package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DrillMovementBehaviour extends BlockBreakingMovementBehaviour {
    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.get(DrillBlock.FACING).getOpposite());
    }

    @Override
    public Vec3d getActiveAreaOffset(MovementContext context) {
        return Vec3d.of(context.state.get(DrillBlock.FACING).getVector()).multiply(.65f);
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    @Override
    protected DamageSource getDamageSource(World level) {
        return AllDamageSources.get(level).drill;
    }

    @Override
    public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
        return super.canBreak(world, breakingPos, state) && !state.getCollisionShape(world, breakingPos)
            .isEmpty() && !state.isIn(AllBlockTags.TRACKS);
    }
}
