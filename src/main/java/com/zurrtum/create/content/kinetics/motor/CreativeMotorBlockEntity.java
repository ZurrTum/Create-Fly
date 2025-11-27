package com.zurrtum.create.content.kinetics.motor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerKineticScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CreativeMotorBlockEntity extends GeneratingKineticBlockEntity {

    public static final int DEFAULT_SPEED = 16;
    public static final int MAX_SPEED = 256;

    protected ServerScrollValueBehaviour generatedSpeed;
    public AbstractComputerBehaviour computerBehaviour;

    public CreativeMotorBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MOTOR, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        int max = MAX_SPEED;
        generatedSpeed = new ServerKineticScrollValueBehaviour(this);
        generatedSpeed.between(-max, max);
        generatedSpeed.setValue(DEFAULT_SPEED);
        generatedSpeed.withCallback(i -> this.updateGeneratedRotation());
        behaviours.add(generatedSpeed);
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!getCachedState().isOf(AllBlocks.CREATIVE_MOTOR))
            return 0;
        return convertToDirection(generatedSpeed.getValue(), getCachedState().get(CreativeMotorBlock.FACING));
    }

    public ServerScrollValueBehaviour getGeneratedSpeedBehaviour() {
        return generatedSpeed;
    }

}
