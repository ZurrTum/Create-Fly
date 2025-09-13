package com.zurrtum.create.content.redstone.contact;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;

public class ContactMovementBehaviour extends MovementBehaviour {

    @Override
    public Vec3d getActiveAreaOffset(MovementContext context) {
        return Vec3d.of(context.state.get(RedstoneContactBlock.FACING).getVector()).multiply(.65f);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        BlockState block = context.state;
        World world = context.world;

        if (world.isClient)
            return;
        if (context.firstMovement)
            return;

        deactivateLastVisitedContact(context);
        BlockState visitedState = world.getBlockState(pos);
        if (!visitedState.isOf(AllBlocks.REDSTONE_CONTACT) && !visitedState.isOf(AllBlocks.ELEVATOR_CONTACT))
            return;

        Vec3d contact = Vec3d.of(block.get(RedstoneContactBlock.FACING).getVector());
        contact = context.rotation.apply(contact);
        Direction direction = Direction.getFacing(contact.x, contact.y, contact.z);

        if (visitedState.get(RedstoneContactBlock.FACING) != direction.getOpposite())
            return;

        if (visitedState.isOf(AllBlocks.REDSTONE_CONTACT))
            world.setBlockState(pos, visitedState.with(RedstoneContactBlock.POWERED, true));
        if (visitedState.isOf(AllBlocks.ELEVATOR_CONTACT) && context.contraption instanceof ElevatorContraption ec)
            ec.broadcastFloorData(world, pos);

        context.data.put("lastContact", BlockPos.CODEC, pos);
    }

    @Override
    public void stopMoving(MovementContext context) {
        deactivateLastVisitedContact(context);
    }

    @Override
    public void cancelStall(MovementContext context) {
        super.cancelStall(context);
        deactivateLastVisitedContact(context);
    }

    public void deactivateLastVisitedContact(MovementContext context) {
        if (!context.data.contains("lastContact"))
            return;

        BlockPos last = context.data.get("lastContact", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        context.data.remove("lastContact");
        BlockState blockState = context.world.getBlockState(last);

        if (blockState.isOf(AllBlocks.REDSTONE_CONTACT))
            context.world.scheduleBlockTick(last, AllBlocks.REDSTONE_CONTACT, 1, TickPriority.NORMAL);
    }

}
