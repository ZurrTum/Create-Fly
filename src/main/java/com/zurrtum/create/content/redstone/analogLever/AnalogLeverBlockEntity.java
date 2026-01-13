package com.zurrtum.create.content.redstone.analogLever;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class AnalogLeverBlockEntity extends SmartBlockEntity {

    int state = 0;
    int lastChange;
    public LerpedFloat clientState;

    public AnalogLeverBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ANALOG_LEVER, pos, state);
        clientState = LerpedFloat.linear();
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("State", state);
        view.putInt("ChangeTimer", lastChange);
        super.write(view, clientPacket);
    }


    @Override
    protected void read(ReadView view, boolean clientPacket) {
        state = view.getInt("State", 0);
        lastChange = view.getInt("ChangeTimer", 0);
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
        if (world.isClient())
            clientState.tickChaser();
    }

    @Override
    public void initialize() {
        super.initialize();

    }

    private void updateOutput() {
        AnalogLeverBlock.updateNeighbors(getCachedState(), world, pos);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public void changeState(boolean back) {
        int prevState = state;
        state += back ? -1 : 1;
        state = MathHelper.clamp(state, 0, 15);
        if (prevState != state)
            lastChange = 15;
        sendData();
    }

    public int getState() {
        return state;
    }
}
