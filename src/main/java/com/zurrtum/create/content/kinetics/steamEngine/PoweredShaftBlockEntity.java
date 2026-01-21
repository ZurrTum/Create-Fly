package com.zurrtum.create.content.kinetics.steamEngine;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class PoweredShaftBlockEntity extends GeneratingKineticBlockEntity {

    public @Nullable BlockPos enginePos;
    public float engineEfficiency;
    public int movementDirection;
    public int initialTicks;
    public @Nullable Block capacityKey;

    public PoweredShaftBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.POWERED_SHAFT, pos, state);
        movementDirection = 1;
        initialTicks = 3;
    }

    @Override
    public void tick() {
        super.tick();
        if (initialTicks > 0)
            initialTicks--;
    }

    public void update(BlockPos sourcePos, int direction, float efficiency) {
        enginePos = worldPosition.subtract(sourcePos);
        float prev = engineEfficiency;
        engineEfficiency = efficiency;
        int prevDirection = this.movementDirection;
        if (Mth.equal(efficiency, prev) && prevDirection == direction)
            return;

        capacityKey = level.getBlockState(sourcePos).getBlock();
        this.movementDirection = direction;
        updateGeneratedRotation();
    }

    public void remove(BlockPos sourcePos) {
        if (!isPoweredBy(sourcePos))
            return;

        enginePos = null;
        engineEfficiency = 0;
        movementDirection = 0;
        capacityKey = null;
        updateGeneratedRotation();
    }

    public boolean canBePoweredBy(BlockPos globalPos) {
        return initialTicks == 0 && (enginePos == null || isPoweredBy(globalPos));
    }

    public boolean isPoweredBy(BlockPos globalPos) {
        BlockPos key = worldPosition.subtract(globalPos);
        return key.equals(enginePos);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        view.putInt("Direction", movementDirection);
        if (initialTicks > 0)
            view.putInt("Warmup", initialTicks);
        if (enginePos != null && capacityKey != null) {
            view.store("EnginePos", BlockPos.CODEC, enginePos);
            view.putFloat("EnginePower", engineEfficiency);
            view.store("EngineType", BuiltInRegistries.BLOCK.byNameCodec(), capacityKey);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        movementDirection = view.getIntOr("Direction", 0);
        initialTicks = view.getIntOr("Warmup", 0);

        view.read("EnginePos", BlockPos.CODEC).ifPresentOrElse(
            pos -> {
                enginePos = pos;
                engineEfficiency = view.getFloatOr("EnginePower", 0);
                capacityKey = view.read("EngineType", BuiltInRegistries.BLOCK.byNameCodec()).orElse(null);
            }, () -> {
                enginePos = null;
                engineEfficiency = 0;
            }
        );
    }

    @Override
    public float getGeneratedSpeed() {
        return getCombinedCapacity() > 0 ? movementDirection * 16 * getSpeedModifier() : 0;
    }

    private float getCombinedCapacity() {
        return capacityKey == null ? 0 : (float) (engineEfficiency * BlockStressValues.getCapacity(capacityKey));
    }

    private int getSpeedModifier() {
        return (int) (1 + (engineEfficiency >= 1 ? 3 : Math.min(2, Math.floor(engineEfficiency * 4))));
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = getCombinedCapacity() / getSpeedModifier();
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public int getRotationAngleOffset(Axis axis) {
        int combinedCoords = axis.choose(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
    }

}
