package com.zurrtum.create.content.processing.burner;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlazeBurnerBlockEntity extends SmartBlockEntity {

    public static final int MAX_HEAT_CAPACITY = 10000;
    public static final int INSERTION_THRESHOLD = 500;

    public LerpedFloat headAnimation;
    public boolean stockKeeper;
    public boolean isCreative;
    public boolean goggles;
    public boolean hat;

    protected FuelType activeFuel;
    protected int remainingBurnTime;
    public LerpedFloat headAngle;


    public BlazeBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.HEATER, pos, state);
        activeFuel = FuelType.NONE;
        remainingBurnTime = 0;
        headAnimation = LerpedFloat.linear();
        headAngle = LerpedFloat.angular();
        isCreative = false;
        goggles = false;
        stockKeeper = false;

        headAngle.startWithValue((AngleHelper.horizontalAngle(state.getValueOrElse(BlazeBurnerBlock.FACING, Direction.SOUTH)) + 180) % 360);
    }

    public FuelType getActiveFuel() {
        return activeFuel;
    }

    public int getRemainingBurnTime() {
        return remainingBurnTime;
    }

    public boolean isCreative() {
        return isCreative;
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            AllClientHandle.INSTANCE.tickBlazeBurnerAnimation(this);
            if (!isVirtual())
                spawnParticles(getHeatLevelFromBlock(), 1);
            return;
        }

        if (isCreative)
            return;

        if (remainingBurnTime > 0)
            remainingBurnTime--;

        if (activeFuel == FuelType.NORMAL)
            updateBlockState();
        if (remainingBurnTime > 0)
            return;

        if (activeFuel == FuelType.SPECIAL) {
            activeFuel = FuelType.NORMAL;
            remainingBurnTime = MAX_HEAT_CAPACITY / 2;
        } else
            activeFuel = FuelType.NONE;

        updateBlockState();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        stockKeeper = getStockTicker(level, worldPosition) != null;
    }

    @Nullable
    public static StockTickerBlockEntity getStockTicker(LevelAccessor level, BlockPos pos) {
        for (Direction direction : Iterate.horizontalDirections) {
            if (level instanceof Level l && !l.isLoaded(pos))
                return null;
            BlockState blockState = level.getBlockState(pos.relative(direction));
            if (!blockState.is(AllBlocks.STOCK_TICKER))
                continue;
            if (level.getBlockEntity(pos.relative(direction)) instanceof StockTickerBlockEntity stbe)
                return stbe;
        }
        return null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (!isCreative) {
            view.putInt("fuelLevel", activeFuel.ordinal());
            view.putInt("burnTimeRemaining", remainingBurnTime);
        } else
            view.putBoolean("isCreative", true);
        if (goggles)
            view.putBoolean("Goggles", true);
        if (hat)
            view.putBoolean("TrainHat", true);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        activeFuel = FuelType.values()[view.getIntOr("fuelLevel", 0)];
        remainingBurnTime = view.getIntOr("burnTimeRemaining", 0);
        isCreative = view.getBooleanOr("isCreative", false);
        goggles = view.getBooleanOr("Goggles", false);
        hat = view.getBooleanOr("TrainHat", false);
        super.read(view, clientPacket);
    }

    public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
        return BlazeBurnerBlock.getHeatLevelOf(getBlockState());
    }

    public BlazeBurnerBlock.HeatLevel getHeatLevelForRender() {
        HeatLevel heatLevel = getHeatLevelFromBlock();
        if (!heatLevel.isAtLeast(HeatLevel.FADING) && stockKeeper)
            return HeatLevel.FADING;
        return heatLevel;
    }

    public void updateBlockState() {
        setBlockHeat(getHeatLevel());
    }

    protected void setBlockHeat(HeatLevel heat) {
        HeatLevel inBlockState = getHeatLevelFromBlock();
        if (inBlockState == heat)
            return;
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
        notifyUpdate();
    }

    /**
     * @return true if the heater updated its burn time and an item should be
     * consumed
     */
    protected boolean tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate) {
        if (isCreative)
            return false;

        FuelType newFuel = FuelType.NONE;
        int newBurnTime;

        if (itemStack.typeHolder().is(AllItemTags.BLAZE_BURNER_FUEL_SPECIAL)) {
            newBurnTime = 3200;
            newFuel = FuelType.SPECIAL;
        } else {
            newBurnTime = level.fuelValues().burnDuration(itemStack);
            if (newBurnTime > 0) {
                newFuel = FuelType.NORMAL;
            } else if (itemStack.typeHolder().is(AllItemTags.BLAZE_BURNER_FUEL_REGULAR)) {
                newBurnTime = 1600; // Same as coal
                newFuel = FuelType.NORMAL;
            }
        }

        if (newFuel == FuelType.NONE)
            return false;
        if (newFuel.ordinal() < activeFuel.ordinal())
            return false;

        if (newFuel == activeFuel) {
            if (remainingBurnTime <= INSERTION_THRESHOLD) {
                newBurnTime += remainingBurnTime;
            } else if (forceOverflow && newFuel == FuelType.NORMAL) {
                if (remainingBurnTime < MAX_HEAT_CAPACITY) {
                    newBurnTime = Math.min(remainingBurnTime + newBurnTime, MAX_HEAT_CAPACITY);
                } else {
                    newBurnTime = remainingBurnTime;
                }
            } else {
                return false;
            }
        }

        if (simulate)
            return true;

        activeFuel = newFuel;
        remainingBurnTime = newBurnTime;

        if (level.isClientSide()) {
            spawnParticleBurst(activeFuel == FuelType.SPECIAL);
            return true;
        }

        HeatLevel prev = getHeatLevelFromBlock();
        playSound();
        updateBlockState();

        if (prev != getHeatLevelFromBlock())
            level.playSound(
                null,
                worldPosition,
                SoundEvents.BLAZE_AMBIENT,
                SoundSource.BLOCKS,
                .125f + level.getRandom().nextFloat() * .125f,
                1.15f - level.getRandom().nextFloat() * .25f
            );

        return true;
    }

    protected void applyCreativeFuel() {
        activeFuel = FuelType.NONE;
        remainingBurnTime = 0;
        isCreative = true;

        HeatLevel next = getHeatLevelFromBlock().nextActiveLevel();

        if (level.isClientSide()) {
            spawnParticleBurst(next.isAtLeast(HeatLevel.SEETHING));
            return;
        }

        playSound();
        if (next == HeatLevel.FADING)
            next = next.nextActiveLevel();
        setBlockHeat(next);
    }

    public boolean isCreativeFuel(ItemStack stack) {
        return stack.is(AllItems.CREATIVE_BLAZE_CAKE);
    }

    public boolean isValidBlockAbove() {
        if (isVirtual())
            return false;
        BlockState blockState = level.getBlockState(worldPosition.above());
        return BasinBlock.isBasin(level, worldPosition.above()) || blockState.getBlock() instanceof FluidTankBlock;
    }

    protected void playSound() {
        level.playSound(
            null,
            worldPosition,
            SoundEvents.BLAZE_SHOOT,
            SoundSource.BLOCKS,
            .125f + level.getRandom().nextFloat() * .125f,
            .75f - level.getRandom().nextFloat() * .25f
        );
    }

    protected HeatLevel getHeatLevel() {
        HeatLevel level = HeatLevel.SMOULDERING;
        switch (activeFuel) {
            case SPECIAL -> level = HeatLevel.SEETHING;
            case NORMAL -> {
                boolean lowPercent = (double) remainingBurnTime / MAX_HEAT_CAPACITY < 0.0125;
                level = lowPercent ? HeatLevel.FADING : HeatLevel.KINDLED;
            }
        }
        return level;
    }

    protected void spawnParticles(HeatLevel heatLevel, double burstMult) {
        if (level == null)
            return;
        if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE)
            return;

        RandomSource r = level.getRandom();

        Vec3 c = VecHelper.getCenterOf(worldPosition);
        Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f).multiply(1, 0, 1));

        if (r.nextInt(4) != 0)
            return;

        boolean empty = level.getBlockState(worldPosition.above()).getCollisionShape(level, worldPosition.above()).isEmpty();

        if (empty || r.nextInt(8) == 0)
            level.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);

        double yMotion = empty ? .0625f : r.nextDouble() * .0125f;
        Vec3 v2 = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f).multiply(1, .25f, 1).normalize()
            .scale((empty ? .25f : .5) + r.nextDouble() * .125f)).add(0, .5, 0);

        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            level.addParticle(ParticleTypes.FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        }
    }

    public void spawnParticleBurst(boolean soulFlame) {
        Vec3 c = VecHelper.getCenterOf(worldPosition);
        RandomSource r = level.getRandom();
        for (int i = 0; i < 20; i++) {
            Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, r, .5f).multiply(1, .25f, 1).normalize();
            Vec3 v = c.add(offset.scale(.5 + r.nextDouble() * .125f)).add(0, .125, 0);
            Vec3 m = offset.scale(1 / 32f);

            level.addParticle(soulFlame ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, v.x, v.y, v.z, m.x, m.y, m.z);
        }
    }

    public enum FuelType {
        NONE,
        NORMAL,
        SPECIAL
    }

}
