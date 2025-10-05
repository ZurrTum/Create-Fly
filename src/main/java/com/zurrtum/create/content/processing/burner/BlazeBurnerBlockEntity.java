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
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
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

        headAngle.startWithValue((AngleHelper.horizontalAngle(state.get(BlazeBurnerBlock.FACING, Direction.SOUTH)) + 180) % 360);
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

        if (world.isClient()) {
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
        stockKeeper = getStockTicker(world, pos) != null;
    }

    @Nullable
    public static StockTickerBlockEntity getStockTicker(WorldAccess level, BlockPos pos) {
        for (Direction direction : Iterate.horizontalDirections) {
            if (level instanceof World l && !l.isPosLoaded(pos))
                return null;
            BlockState blockState = level.getBlockState(pos.offset(direction));
            if (!blockState.isOf(AllBlocks.STOCK_TICKER))
                continue;
            if (level.getBlockEntity(pos.offset(direction)) instanceof StockTickerBlockEntity stbe)
                return stbe;
        }
        return null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
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
    protected void read(ReadView view, boolean clientPacket) {
        activeFuel = FuelType.values()[view.getInt("fuelLevel", 0)];
        remainingBurnTime = view.getInt("burnTimeRemaining", 0);
        isCreative = view.getBoolean("isCreative", false);
        goggles = view.getBoolean("Goggles", false);
        hat = view.getBoolean("TrainHat", false);
        super.read(view, clientPacket);
    }

    public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
        return BlazeBurnerBlock.getHeatLevelOf(getCachedState());
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
        world.setBlockState(pos, getCachedState().with(BlazeBurnerBlock.HEAT_LEVEL, heat));
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

        if (itemStack.getRegistryEntry().isIn(AllItemTags.BLAZE_BURNER_FUEL_SPECIAL)) {
            newBurnTime = 3200;
            newFuel = FuelType.SPECIAL;
        } else {
            newBurnTime = world.getFuelRegistry().getFuelTicks(itemStack);
            if (newBurnTime > 0) {
                newFuel = FuelType.NORMAL;
            } else if (itemStack.getRegistryEntry().isIn(AllItemTags.BLAZE_BURNER_FUEL_REGULAR)) {
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

        if (world.isClient()) {
            spawnParticleBurst(activeFuel == FuelType.SPECIAL);
            return true;
        }

        HeatLevel prev = getHeatLevelFromBlock();
        playSound();
        updateBlockState();

        if (prev != getHeatLevelFromBlock())
            world.playSound(
                null,
                pos,
                SoundEvents.ENTITY_BLAZE_AMBIENT,
                SoundCategory.BLOCKS,
                .125f + world.random.nextFloat() * .125f,
                1.15f - world.random.nextFloat() * .25f
            );

        return true;
    }

    protected void applyCreativeFuel() {
        activeFuel = FuelType.NONE;
        remainingBurnTime = 0;
        isCreative = true;

        HeatLevel next = getHeatLevelFromBlock().nextActiveLevel();

        if (world.isClient()) {
            spawnParticleBurst(next.isAtLeast(HeatLevel.SEETHING));
            return;
        }

        playSound();
        if (next == HeatLevel.FADING)
            next = next.nextActiveLevel();
        setBlockHeat(next);
    }

    public boolean isCreativeFuel(ItemStack stack) {
        return stack.isOf(AllItems.CREATIVE_BLAZE_CAKE);
    }

    public boolean isValidBlockAbove() {
        if (isVirtual())
            return false;
        BlockState blockState = world.getBlockState(pos.up());
        return BasinBlock.isBasin(world, pos.up()) || blockState.getBlock() instanceof FluidTankBlock;
    }

    protected void playSound() {
        world.playSound(
            null,
            pos,
            SoundEvents.ENTITY_BLAZE_SHOOT,
            SoundCategory.BLOCKS,
            .125f + world.random.nextFloat() * .125f,
            .75f - world.random.nextFloat() * .25f
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
        if (world == null)
            return;
        if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE)
            return;

        Random r = world.getRandom();

        Vec3d c = VecHelper.getCenterOf(pos);
        Vec3d v = c.add(VecHelper.offsetRandomly(Vec3d.ZERO, r, .125f).multiply(1, 0, 1));

        if (r.nextInt(4) != 0)
            return;

        boolean empty = world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty();

        if (empty || r.nextInt(8) == 0)
            world.addParticleClient(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);

        double yMotion = empty ? .0625f : r.nextDouble() * .0125f;
        Vec3d v2 = c.add(VecHelper.offsetRandomly(Vec3d.ZERO, r, .5f).multiply(1, .25f, 1).normalize()
            .multiply((empty ? .25f : .5) + r.nextDouble() * .125f)).add(0, .5, 0);

        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            world.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            world.addParticleClient(ParticleTypes.FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        }
    }

    public void spawnParticleBurst(boolean soulFlame) {
        Vec3d c = VecHelper.getCenterOf(pos);
        Random r = world.random;
        for (int i = 0; i < 20; i++) {
            Vec3d offset = VecHelper.offsetRandomly(Vec3d.ZERO, r, .5f).multiply(1, .25f, 1).normalize();
            Vec3d v = c.add(offset.multiply(.5 + r.nextDouble() * .125f)).add(0, .125, 0);
            Vec3d m = offset.multiply(1 / 32f);

            world.addParticleClient(soulFlame ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, v.x, v.y, v.z, m.x, m.y, m.z);
        }
    }

    public enum FuelType {
        NONE,
        NORMAL,
        SPECIAL
    }

}
