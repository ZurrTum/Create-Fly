package com.zurrtum.create.content.kinetics.crusher;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.List;

public class CrushingWheelBlockEntity extends KineticBlockEntity {
    public CrushingWheelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CRUSHING_WHEEL, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CRUSHING_WHEEL, AllAdvancements.CRUSHER_MAXED);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        fixControllers();
    }

    public void fixControllers() {
        for (Direction d : Iterate.directions)
            ((CrushingWheelBlock) getCachedState().getBlock()).updateControllers(getCachedState(), getWorld(), getPos(), d);
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(pos).expand(1);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        fixControllers();
    }
}
