package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    public boolean ring(Level world, BlockPos pos, Direction direction) {
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
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (!clientPacket || ringingTicks != 0 || !isRinging)
            return;
        view.store("Ringing", Direction.CODEC, ringDirection);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
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
