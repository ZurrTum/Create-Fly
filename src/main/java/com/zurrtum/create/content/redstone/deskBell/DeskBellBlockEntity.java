package com.zurrtum.create.content.redstone.deskBell;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DeskBellBlockEntity extends SmartBlockEntity {

    public LerpedFloat animation = LerpedFloat.linear().startWithValue(0);

    public boolean ding;

    int blockStateTimer;
    public float animationOffset;

    public DeskBellBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.DESK_BELL, pos, state);
        blockStateTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();
        animation.tickChaser();

        if (level.isClientSide())
            return;
        if (blockStateTimer == 0)
            return;

        blockStateTimer--;

        if (blockStateTimer > 0)
            return;
        BlockState blockState = getBlockState();
        if (blockState.getValue(DeskBellBlock.POWERED))
            AllBlocks.DESK_BELL.unPress(blockState, level, worldPosition);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket && ding)
            view.putBoolean("Ding", true);
        ding = false;
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket && view.getBooleanOr("Ding", false))
            ding();
    }

    public void ding() {
        if (!level.isClientSide()) {
            blockStateTimer = 20;
            ding = true;
            sendData();
            return;
        }

        animationOffset = level.getRandom().nextFloat() * 2 * Mth.PI;
        animation.startWithValue(1).chase(0, 0.05, Chaser.LINEAR);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

}