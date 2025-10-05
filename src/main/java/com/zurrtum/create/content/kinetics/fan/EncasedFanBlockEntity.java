package com.zurrtum.create.content.kinetics.fan;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EncasedFanBlockEntity extends KineticBlockEntity implements IAirCurrentSource {

    public AirCurrent airCurrent;
    protected int airCurrentUpdateCooldown;
    protected int entitySearchCooldown;
    protected boolean updateAirFlow;

    public EncasedFanBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ENCASED_FAN, pos, state);
        airCurrent = new AirCurrent(this);
        updateAirFlow = true;
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.ENCASED_FAN, AllAdvancements.FAN_PROCESSING);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            airCurrent.rebuild();
    }

    @Override
    public AirCurrent getAirCurrent() {
        return airCurrent;
    }

    @Nullable
    @Override
    public World getAirCurrentWorld() {
        return world;
    }

    @Override
    public BlockPos getAirCurrentPos() {
        return pos;
    }

    @Override
    public Direction getAirflowOriginSide() {
        return getCachedState().get(EncasedFanBlock.FACING);
    }

    @Override
    public Direction getAirFlowDirection() {
        float speed = getSpeed();
        if (speed == 0)
            return null;
        Direction facing = getCachedState().get(Properties.FACING);
        speed = convertToDirection(speed, facing);
        return speed > 0 ? facing : facing.getOpposite();
    }

    @Override
    public void remove() {
        super.remove();
        updateChute();
    }

    @Override
    public boolean isSourceRemoved() {
        return removed;
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        updateAirFlow = true;
        updateChute();
    }

    public void updateChute() {
        Direction direction = getCachedState().get(EncasedFanBlock.FACING);
        if (!direction.getAxis().isVertical())
            return;
        BlockEntity poweredChute = world.getBlockEntity(pos.offset(direction));
        if (!(poweredChute instanceof ChuteBlockEntity chuteBE))
            return;
        if (direction == Direction.DOWN)
            chuteBE.updatePull();
        else
            chuteBE.updatePush(1);
    }

    public void blockInFrontChanged() {
        updateAirFlow = true;
    }

    @Override
    public void tick() {
        super.tick();

        boolean server = !world.isClient() || isVirtual();

        if (server && airCurrentUpdateCooldown-- <= 0) {
            airCurrentUpdateCooldown = AllConfigs.server().kinetics.fanBlockCheckRate.get();
            updateAirFlow = true;
        }

        if (updateAirFlow) {
            updateAirFlow = false;
            airCurrent.rebuild();
            if (airCurrent.maxDistance > 0)
                award(AllAdvancements.ENCASED_FAN);
            sendData();
        }

        if (getSpeed() == 0)
            return;

        if (entitySearchCooldown-- <= 0) {
            entitySearchCooldown = 5;
            airCurrent.findEntities();
        }

        airCurrent.tick();
    }

}