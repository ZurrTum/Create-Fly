package com.zurrtum.create.content.redstone.diodes;

import com.mojang.serialization.Codec;
import com.zurrtum.create.content.equipment.clipboard.ClipboardCloneable;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerBrassDiodeScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock.POWERING;

public abstract class BrassDiodeBlockEntity extends SmartBlockEntity implements ClipboardCloneable {

    public int state;
    ServerScrollValueBehaviour maxState;

    public BrassDiodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        maxState = new ServerBrassDiodeScrollValueBehaviour(this).between(2, 60 * 20 * 60);
        maxState.withCallback(this::onMaxDelayChanged);
        maxState.setValue(defaultValue());
        behaviours.add(maxState);
    }

    protected int defaultValue() {
        return 2;
    }

    public float getProgress() {
        int max = Math.max(2, maxState.getValue());
        return Mth.clamp(state, 0, max) / (float) max;
    }

    public boolean isIdle() {
        return state == 0;
    }

    @Override
    public void tick() {
        super.tick();
        boolean powered = getBlockState().getValue(DiodeBlock.POWERED);
        boolean powering = getBlockState().getValue(POWERING);
        boolean atMax = state >= maxState.getValue();
        boolean atMin = state <= 0;
        updateState(powered, powering, atMax, atMin);
    }

    protected abstract void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin);

    private void onMaxDelayChanged(int newMax) {
        state = Mth.clamp(state, 0, newMax);
        sendData();
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        state = view.getIntOr("State", 0);
        super.read(view, clientPacket);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("State", state);
        super.write(view, clientPacket);
    }

    @Override
    public String getClipboardKey() {
        return "Block";
    }

    @Override
    public boolean readFromClipboard(ValueInput view, Player player, Direction side, boolean simulate) {
        Optional<Boolean> inverted = view.read("Inverted", Codec.BOOL);
        if (inverted.isEmpty())
            return false;
        if (simulate)
            return true;
        BlockState blockState = getBlockState();
        if (blockState.getValue(BrassDiodeBlock.INVERTED) != inverted.get())
            level.setBlockAndUpdate(worldPosition, blockState.cycle(BrassDiodeBlock.INVERTED));
        return true;
    }

    @Override
    public boolean writeToClipboard(ValueOutput view, Direction side) {
        view.store("Inverted", Codec.BOOL, getBlockState().getValueOrElse(BrassDiodeBlock.INVERTED, false));
        return true;
    }

}