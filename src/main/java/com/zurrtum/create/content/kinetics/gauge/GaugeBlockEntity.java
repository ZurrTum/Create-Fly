package com.zurrtum.create.content.kinetics.gauge;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

public abstract class GaugeBlockEntity extends KineticBlockEntity {
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public int color;

    public GaugeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putFloat("Value", dialTarget);
        view.putInt("Color", color);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        dialTarget = view.getFloat("Value", 0);
        color = view.getInt("Color", 0);
        super.read(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        prevDialState = dialState;
        dialState += (dialTarget - dialState) * .125f;
        if (dialState > 1 && world.random.nextFloat() < 1 / 2f)
            dialState -= (dialState - 1) * world.random.nextFloat();
    }
}