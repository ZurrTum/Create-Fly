package com.zurrtum.create.content.redstone.deskBell;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

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

        if (world.isClient)
            return;
        if (blockStateTimer == 0)
            return;

        blockStateTimer--;

        if (blockStateTimer > 0)
            return;
        BlockState blockState = getCachedState();
        if (blockState.get(DeskBellBlock.POWERED))
            AllBlocks.DESK_BELL.unPress(blockState, world, pos);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket && ding)
            view.putBoolean("Ding", true);
        ding = false;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket && view.getBoolean("Ding", false))
            ding();
    }

    public void ding() {
        if (!world.isClient) {
            blockStateTimer = 20;
            ding = true;
            sendData();
            return;
        }

        animationOffset = world.random.nextFloat() * 2 * MathHelper.PI;
        animation.startWithValue(1).chase(0, 0.05, Chaser.LINEAR);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

}