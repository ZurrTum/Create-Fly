package com.zurrtum.create.content.decoration.steamWhistle;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.lang.ref.WeakReference;
import java.util.List;

public class WhistleBlockEntity extends SmartBlockEntity {

    public WeakReference<FluidTankBlockEntity> source;
    public int pitch;

    public WhistleBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.STEAM_WHISTLE, pos, state);
        source = new WeakReference<>(null);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.STEAM_WHISTLE);
    }

    public void updatePitch() {
        BlockPos currentPos = pos.up();
        int newPitch;
        for (newPitch = 0; newPitch <= 24; newPitch += 2) {
            BlockState blockState = world.getBlockState(currentPos);
            if (!blockState.isOf(AllBlocks.STEAM_WHISTLE_EXTENSION))
                break;
            if (blockState.get(WhistleExtenderBlock.SHAPE) == WhistleExtenderShape.SINGLE) {
                newPitch++;
                break;
            }
            currentPos = currentPos.up();
        }
        if (pitch == newPitch)
            return;
        pitch = newPitch;

        notifyUpdate();

        FluidTankBlockEntity tank = getTank();
        if (tank != null && tank.boiler != null)
            tank.boiler.checkPipeOrganAdvancement(tank);
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient()) {
            if (isPowered())
                award(AllAdvancements.STEAM_WHISTLE);
        }
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        view.putInt("Pitch", pitch);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        pitch = view.getInt("Pitch", 0);
        super.read(view, clientPacket);
    }

    public boolean isPowered() {
        return getCachedState().get(WhistleBlock.POWERED, false);
    }

    public WhistleSize getOctave() {
        return getCachedState().get(WhistleBlock.SIZE, WhistleSize.MEDIUM);
    }

    public int getPitchId() {
        return pitch + 100 * getCachedState().get(WhistleBlock.SIZE, WhistleSize.MEDIUM).ordinal();
    }

    public FluidTankBlockEntity getTank() {
        FluidTankBlockEntity tank = source.get();
        if (tank == null || tank.isRemoved()) {
            if (tank != null)
                source = new WeakReference<>(null);
            Direction facing = WhistleBlock.getAttachedDirection(getCachedState());
            BlockEntity be = world.getBlockEntity(pos.offset(facing));
            if (be instanceof FluidTankBlockEntity tankBe)
                source = new WeakReference<>(tank = tankBe);
        }
        if (tank == null)
            return null;
        return tank.getControllerBE();
    }

}
