package com.zurrtum.create.content.redstone.diodes;

import com.mojang.serialization.Codec;
import com.zurrtum.create.content.equipment.clipboard.ClipboardCloneable;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerBrassDiodeScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Optional;

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
        return MathHelper.clamp(state, 0, max) / (float) max;
    }

    public boolean isIdle() {
        return state == 0;
    }

    @Override
    public void tick() {
        super.tick();
        boolean powered = getCachedState().get(AbstractRedstoneGateBlock.POWERED);
        boolean powering = getCachedState().get(POWERING);
        boolean atMax = state >= maxState.getValue();
        boolean atMin = state <= 0;
        updateState(powered, powering, atMax, atMin);
    }

    protected abstract void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin);

    private void onMaxDelayChanged(int newMax) {
        state = MathHelper.clamp(state, 0, newMax);
        sendData();
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        state = view.getInt("State", 0);
        super.read(view, clientPacket);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("State", state);
        super.write(view, clientPacket);
    }

    @Override
    public String getClipboardKey() {
        return "Block";
    }

    @Override
    public boolean readFromClipboard(ReadView view, PlayerEntity player, Direction side, boolean simulate) {
        Optional<Boolean> inverted = view.read("Inverted", Codec.BOOL);
        if (inverted.isEmpty())
            return false;
        if (simulate)
            return true;
        BlockState blockState = getCachedState();
        if (blockState.get(BrassDiodeBlock.INVERTED) != inverted.get())
            world.setBlockState(pos, blockState.cycle(BrassDiodeBlock.INVERTED));
        return true;
    }

    @Override
    public boolean writeToClipboard(WriteView view, Direction side) {
        view.put("Inverted", Codec.BOOL, getCachedState().get(BrassDiodeBlock.INVERTED, false));
        return true;
    }

}