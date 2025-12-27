package com.zurrtum.create.content.redstone.analogLever;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AnalogLeverBlockEntity extends SmartBlockEntity {

    int state = 0;
    int lastChange;
    public LerpedFloat clientState;

    public AnalogLeverBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ANALOG_LEVER, pos, state);
        clientState = LerpedFloat.linear();
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("State", state);
        view.putInt("ChangeTimer", lastChange);
        super.write(view, clientPacket);
    }


    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        state = view.getIntOr("State", 0);
        lastChange = view.getIntOr("ChangeTimer", 0);
        clientState.chase(state, 0.2f, Chaser.EXP);
        super.read(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        if (lastChange > 0) {
            lastChange--;
            if (lastChange == 0)
                updateOutput();
        }
        if (level.isClientSide())
            clientState.tickChaser();
    }

    @Override
    public void initialize() {
        super.initialize();

    }

    private void updateOutput() {
        AnalogLeverBlock.updateNeighbors(getBlockState(), level, worldPosition);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public void changeState(boolean back) {
        int prevState = state;
        state += back ? -1 : 1;
        state = Mth.clamp(state, 0, 15);
        if (prevState != state)
            lastChange = 15;
        sendData();
    }

    public int getState() {
        return state;
    }
}
