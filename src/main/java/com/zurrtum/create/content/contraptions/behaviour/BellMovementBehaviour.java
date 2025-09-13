package com.zurrtum.create.content.contraptions.behaviour;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.equipment.bell.AbstractBellBlock;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlock;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import net.minecraft.block.Block;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BellMovementBehaviour extends MovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !(context.contraption instanceof CarriageContraption);
    }

    @Override
    public void tick(MovementContext context) {
        boolean moved = context.temporaryData instanceof Boolean b && b;
        Contraption contraption = context.contraption;

        if (contraption instanceof ElevatorContraption ec && !ec.arrived)
            context.temporaryData = Boolean.TRUE;
        else if (moved) {
            playSound(context);
            context.temporaryData = null;
        }
    }

    @Override
    public void onSpeedChanged(MovementContext context, Vec3d oldMotion, Vec3d motion) {
        if (context.contraption instanceof ElevatorContraption)
            return;

        double dotProduct = oldMotion.dotProduct(motion);
        if (dotProduct <= 0 && (context.relativeMotion.length() != 0) || context.firstMovement)
            playSound(context);
    }

    @Override
    public void stopMoving(MovementContext context) {
        if (context.position != null && isActive(context))
            playSound(context);
    }

    public static void playSound(MovementContext context) {
        World world = context.world;
        BlockPos pos = BlockPos.ofFloored(context.position);
        Block block = context.state.getBlock();

        if (context.state.isOf(AllBlocks.DESK_BELL)) {
            ((DeskBellBlock) block).playSound(null, world, pos);
        } else if (block instanceof AbstractBellBlock<?> bellBlock) {
            bellBlock.playSound(world, pos);
        } else {
            // Vanilla bell sound
            world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2f, 1f);
        }
    }

}