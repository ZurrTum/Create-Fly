package com.zurrtum.create.content.fluids.hosePulley;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.zurrtum.create.content.fluids.transfer.FluidFillingBehaviour;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class HosePulleyBlockEntity extends KineticBlockEntity {

    LerpedFloat offset;
    boolean isMoving;

    private FluidDrainingBehaviour drainer;
    private FluidFillingBehaviour filler;
    public HosePulleyFluidHandler handler;
    public boolean infinite;

    public HosePulleyBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.HOSE_PULLEY, pos, state);
        offset = LerpedFloat.linear().startWithValue(0);
        isMoving = true;
        handler = new HosePulleyFluidHandler(this, filler, drainer, () -> pos.down((int) Math.ceil(offset.getValue())), () -> !this.isMoving);
    }

    @Override
    public void sendData() {
        infinite = filler.isInfinite() || drainer.isInfinite();
        super.sendData();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        drainer = new FluidDrainingBehaviour(this);
        filler = new FluidFillingBehaviour(this);
        behaviours.add(drainer);
        behaviours.add(filler);
        super.addBehaviours(behaviours);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.HOSE_PULLEY, AllAdvancements.HOSE_PULLEY_LAVA);
    }

    protected void onTankContentsChanged(FluidStack contents) {
        markDirty();
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        isMoving = true;
        if (getSpeed() == 0) {
            offset.forceNextSync();
            offset.setValue(Math.round(offset.getValue()));
            isMoving = false;
        }

        if (isMoving) {
            float newOffset = offset.getValue() + getMovementSpeed();
            if (newOffset < 0)
                isMoving = false;
            if (!world.getBlockState(pos.down((int) Math.ceil(newOffset))).isReplaceable()) {
                isMoving = false;
            }
            if (isMoving) {
                drainer.reset();
                filler.reset();
            }
        }

        super.onSpeedChanged(previousSpeed);
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().stretch(0, -offset.getValue(), 0);
    }

    @Override
    public void tick() {
        super.tick();
        float newOffset = offset.getValue() + getMovementSpeed();
        if (newOffset < 0) {
            newOffset = 0;
            isMoving = false;
        }
        if (!world.getBlockState(pos.down((int) Math.ceil(newOffset))).isReplaceable()) {
            newOffset = (int) newOffset;
            isMoving = false;
        }
        if (getSpeed() == 0)
            isMoving = false;

        offset.setValue(newOffset);
        invalidateRenderBoundingBox();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (world.isClient)
            return;
        if (isMoving)
            return;

        int ceil = (int) Math.ceil(offset.getValue() + getMovementSpeed());
        if (getMovementSpeed() > 0 && world.getBlockState(pos.down(ceil)).isReplaceable()) {
            isMoving = true;
            drainer.reset();
            filler.reset();
            return;
        }

        sendData();
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        if (clientPacket)
            offset.forceNextSync();
        offset.write(view.get("Offset"));
        handler.write(view);
        super.write(view, clientPacket);
        if (clientPacket)
            view.putBoolean("Infinite", infinite);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        offset.read(view.getReadView("Offset"), clientPacket);
        handler.read(view);
        super.read(view, clientPacket);
        if (clientPacket)
            infinite = view.getBoolean("Infinite", false);
    }

    public float getMovementSpeed() {
        float movementSpeed = convertToLinear(getSpeed());
        if (world.isClient)
            movementSpeed *= AllClientHandle.INSTANCE.getServerSpeed();
        return movementSpeed;
    }

    public float getInterpolatedOffset(float pt) {
        return Math.max(offset.getValue(pt), 3 / 16f);
    }
}
