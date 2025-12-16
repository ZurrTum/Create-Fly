package com.zurrtum.create.content.kinetics.gauge;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class GaugeBlockEntity extends KineticBlockEntity {
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public int color;

    public GaugeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putFloat("Value", dialTarget);
        view.putInt("Color", color);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        dialTarget = view.getFloatOr("Value", 0);
        color = view.getIntOr("Color", 0);
        super.read(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        prevDialState = dialState;
        dialState += (dialTarget - dialState) * .125f;
        if (dialState > 1 && level.getRandom().nextFloat() < 1 / 2f)
            dialState -= (dialState - 1) * level.getRandom().nextFloat();
    }
}