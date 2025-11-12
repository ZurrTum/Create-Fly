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
import java.lang.ref.WeakReference;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
        BlockPos currentPos = worldPosition.above();
        int newPitch;
        for (newPitch = 0; newPitch <= 24; newPitch += 2) {
            BlockState blockState = level.getBlockState(currentPos);
            if (!blockState.is(AllBlocks.STEAM_WHISTLE_EXTENSION))
                break;
            if (blockState.getValue(WhistleExtenderBlock.SHAPE) == WhistleExtenderShape.SINGLE) {
                newPitch++;
                break;
            }
            currentPos = currentPos.above();
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
        if (!level.isClientSide()) {
            if (isPowered())
                award(AllAdvancements.STEAM_WHISTLE);
        }
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        view.putInt("Pitch", pitch);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        pitch = view.getIntOr("Pitch", 0);
        super.read(view, clientPacket);
    }

    public boolean isPowered() {
        return getBlockState().getValueOrElse(WhistleBlock.POWERED, false);
    }

    public WhistleSize getOctave() {
        return getBlockState().getValueOrElse(WhistleBlock.SIZE, WhistleSize.MEDIUM);
    }

    public int getPitchId() {
        return pitch + 100 * getBlockState().getValueOrElse(WhistleBlock.SIZE, WhistleSize.MEDIUM).ordinal();
    }

    public FluidTankBlockEntity getTank() {
        FluidTankBlockEntity tank = source.get();
        if (tank == null || tank.isRemoved()) {
            if (tank != null)
                source = new WeakReference<>(null);
            Direction facing = WhistleBlock.getAttachedDirection(getBlockState());
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing));
            if (be instanceof FluidTankBlockEntity tankBe)
                source = new WeakReference<>(tank = tankBe);
        }
        if (tank == null)
            return null;
        return tank.getControllerBE();
    }

}
