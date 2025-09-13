package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public abstract class AbstractBellBlockEntity extends SmartBlockEntity {

    public static final int RING_DURATION = 74;

    public boolean isRinging;
    public int ringingTicks;
    public Direction ringDirection;

    public AbstractBellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public boolean ring(World world, BlockPos pos, Direction direction) {
        isRinging = true;
        ringingTicks = 0;
        ringDirection = direction;
        sendData();
        return true;
    }

    ;

    @Override
    public void tick() {
        super.tick();

        if (isRinging) {
            ++ringingTicks;
        }

        if (ringingTicks >= RING_DURATION) {
            isRinging = false;
            ringingTicks = 0;
        }
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (!clientPacket || ringingTicks != 0 || !isRinging)
            return;
        view.put("Ringing", Direction.CODEC, ringDirection);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (!clientPacket)
            return;
        view.read("Ringing", Direction.CODEC).ifPresent(direction -> {
            ringDirection = direction;
            ringingTicks = 0;
            isRinging = true;
        });
    }
}
