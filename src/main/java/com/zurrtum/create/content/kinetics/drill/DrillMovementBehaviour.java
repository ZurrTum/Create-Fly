package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DrillMovementBehaviour extends BlockBreakingMovementBehaviour {
    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            context.state.getValue(DrillBlock.FACING).getOpposite()
        );
    }

    @Override
    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.atLowerCornerOf(context.state.getValue(DrillBlock.FACING).getUnitVec3i()).scale(.65f);
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    @Override
    protected DamageSource getDamageSource(Level level) {
        return AllDamageSources.get(level).drill;
    }

    @Override
    public boolean canBreak(Level world, BlockPos breakingPos, BlockState state) {
        return super.canBreak(world, breakingPos, state) && !state.getCollisionShape(world, breakingPos).isEmpty() && !state.is(AllBlockTags.TRACKS);
    }
}
