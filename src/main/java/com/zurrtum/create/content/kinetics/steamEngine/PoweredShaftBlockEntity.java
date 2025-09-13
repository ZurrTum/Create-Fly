package com.zurrtum.create.content.kinetics.steamEngine;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class PoweredShaftBlockEntity extends GeneratingKineticBlockEntity {

    public BlockPos enginePos;
    public float engineEfficiency;
    public int movementDirection;
    public int initialTicks;
    public Block capacityKey;

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
        BlockPos key = pos.subtract(sourcePos);
        enginePos = key;
        float prev = engineEfficiency;
        engineEfficiency = efficiency;
        int prevDirection = this.movementDirection;
        if (MathHelper.approximatelyEquals(efficiency, prev) && prevDirection == direction)
            return;

        capacityKey = world.getBlockState(sourcePos).getBlock();
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
        BlockPos key = pos.subtract(globalPos);
        return key.equals(enginePos);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        view.putInt("Direction", movementDirection);
        if (initialTicks > 0)
            view.putInt("Warmup", initialTicks);
        if (enginePos != null && capacityKey != null) {
            view.put("EnginePos", BlockPos.CODEC, enginePos);
            view.putFloat("EnginePower", engineEfficiency);
            view.put("EngineType", Registries.BLOCK.getCodec(), capacityKey);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        movementDirection = view.getInt("Direction", 0);
        initialTicks = view.getInt("Warmup", 0);

        view.read("EnginePos", BlockPos.CODEC).ifPresentOrElse(
            pos -> {
                enginePos = pos;
                engineEfficiency = view.getFloat("EnginePower", 0);
                capacityKey = view.read("EngineType", Registries.BLOCK.getCodec()).orElse(null);
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
        int combinedCoords = axis.choose(pos.getX(), pos.getY(), pos.getZ());
        return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
    }

}
