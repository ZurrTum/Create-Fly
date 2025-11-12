package com.zurrtum.create.content.redstone.contact;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;

public class ContactMovementBehaviour extends MovementBehaviour {

    @Override
    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.atLowerCornerOf(context.state.getValue(RedstoneContactBlock.FACING).getUnitVec3i()).scale(.65f);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        BlockState block = context.state;
        Level world = context.world;

        if (world.isClientSide())
            return;
        if (context.firstMovement)
            return;

        deactivateLastVisitedContact(context);
        BlockState visitedState = world.getBlockState(pos);
        if (!visitedState.is(AllBlocks.REDSTONE_CONTACT) && !visitedState.is(AllBlocks.ELEVATOR_CONTACT))
            return;

        Vec3 contact = Vec3.atLowerCornerOf(block.getValue(RedstoneContactBlock.FACING).getUnitVec3i());
        contact = context.rotation.apply(contact);
        Direction direction = Direction.getApproximateNearest(contact.x, contact.y, contact.z);

        if (visitedState.getValue(RedstoneContactBlock.FACING) != direction.getOpposite())
            return;

        if (visitedState.is(AllBlocks.REDSTONE_CONTACT))
            world.setBlockAndUpdate(pos, visitedState.setValue(RedstoneContactBlock.POWERED, true));
        if (visitedState.is(AllBlocks.ELEVATOR_CONTACT) && context.contraption instanceof ElevatorContraption ec)
            ec.broadcastFloorData(world, pos);

        context.data.store("lastContact", BlockPos.CODEC, pos);
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

        BlockPos last = context.data.read("lastContact", BlockPos.CODEC).orElse(BlockPos.ZERO);
        context.data.remove("lastContact");
        BlockState blockState = context.world.getBlockState(last);

        if (blockState.is(AllBlocks.REDSTONE_CONTACT))
            context.world.scheduleTick(last, AllBlocks.REDSTONE_CONTACT, 1, TickPriority.NORMAL);
    }

}
