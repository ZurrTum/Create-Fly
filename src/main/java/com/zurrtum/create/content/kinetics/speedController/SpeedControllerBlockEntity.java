package com.zurrtum.create.content.kinetics.speedController;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerKineticScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SpeedControllerBlockEntity extends KineticBlockEntity {

    public static final int DEFAULT_SPEED = 16;
    public ServerScrollValueBehaviour targetSpeed;

    public boolean hasBracket;

    public SpeedControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER, pos, state);
        hasBracket = false;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        updateBracket();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        Integer max = AllConfigs.server().kinetics.maxRotationSpeed.get();

        targetSpeed = new ServerKineticScrollValueBehaviour(this);
        targetSpeed.between(-max, max);
        targetSpeed.setValue(DEFAULT_SPEED);
        targetSpeed.withCallback(i -> this.updateTargetRotation());
        behaviours.add(targetSpeed);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.SPEED_CONTROLLER);
    }

    private void updateTargetRotation() {
        if (hasNetwork())
            getOrCreateNetwork().remove(this);
        RotationPropagator.handleRemoved(level, worldPosition, this);
        removeSource();
        attachKinetics();

        if (isCogwheelPresent() && getSpeed() != 0)
            award(AllAdvancements.SPEED_CONTROLLER);
    }

    public static float getConveyedSpeed(KineticBlockEntity cogWheel, KineticBlockEntity speedControllerIn, boolean targetingController) {
        if (!(speedControllerIn instanceof SpeedControllerBlockEntity))
            return 0;

        float speed = speedControllerIn.getTheoreticalSpeed();
        float wheelSpeed = cogWheel.getTheoreticalSpeed();
        float desiredOutputSpeed = getDesiredOutputSpeed(cogWheel, speedControllerIn, targetingController);

        float compareSpeed = targetingController ? speed : wheelSpeed;
        if (desiredOutputSpeed >= 0 && compareSpeed >= 0)
            return Math.max(desiredOutputSpeed, compareSpeed);
        if (desiredOutputSpeed < 0 && compareSpeed < 0)
            return Math.min(desiredOutputSpeed, compareSpeed);

        return desiredOutputSpeed;
    }

    public static float getDesiredOutputSpeed(KineticBlockEntity cogWheel, KineticBlockEntity speedControllerIn, boolean targetingController) {
        SpeedControllerBlockEntity speedController = (SpeedControllerBlockEntity) speedControllerIn;
        float targetSpeed = speedController.targetSpeed.getValue();
        float speed = speedControllerIn.getTheoreticalSpeed();
        float wheelSpeed = cogWheel.getTheoreticalSpeed();

        if (targetSpeed == 0)
            return 0;
        if (targetingController && wheelSpeed == 0)
            return 0;
        if (!speedController.hasSource()) {
            if (targetingController)
                return targetSpeed;
            return 0;
        }

        boolean wheelPowersController = speedController.source.equals(cogWheel.getBlockPos());

        if (wheelPowersController) {
            if (targetingController)
                return targetSpeed;
            return wheelSpeed;
        }

        if (targetingController)
            return speed;
        return targetSpeed;
    }

    public void updateBracket() {
        if (level != null && level.isClientSide())
            hasBracket = isCogwheelPresent();
    }

    private boolean isCogwheelPresent() {
        BlockState stateAbove = level.getBlockState(worldPosition.above());
        return ICogWheel.isDedicatedCogWheel(stateAbove.getBlock()) && ICogWheel.isLargeCog(stateAbove) && stateAbove.getValue(CogWheelBlock.AXIS)
            .isHorizontal();
    }
}
