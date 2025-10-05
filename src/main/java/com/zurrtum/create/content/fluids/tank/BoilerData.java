package com.zurrtum.create.content.fluids.tank;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.boiler.BoilerHeater;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import joptsimple.internal.Strings;
import net.minecraft.block.BlockState;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class BoilerData {

    static final int SAMPLE_RATE = 5;

    public static final int waterSupplyPerLevel = 10 * 81;
    private static final float passiveEngineEfficiency = 1 / 8f;

    // pooled water supply
    int gatheredSupply;
    float[] supplyOverTime = new float[10];
    int ticksUntilNextSample;
    int currentIndex;

    // heat score
    public boolean needsHeatLevelUpdate;
    public boolean passiveHeat;
    public int activeHeat;

    public float waterSupply;
    public int attachedEngines;
    public int attachedWhistles;

    // display
    public int maxHeatForSize = 0;
    public int maxHeatForWater = 0;
    public int minValue = 0;
    public int maxValue = 0;
    public boolean[] occludedDirections = {true, true, true, true};

    public LerpedFloat gauge = LerpedFloat.linear();

    // client only sound control

    // re-use the same lambda for each side
    private final SoundPool.Sound sound = (level, pos) -> {
        float volume = 3f / Math.max(2, attachedEngines / 6);
        float pitch = 1.18f - level.random.nextFloat() * .25f;
        level.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, volume, pitch, false);

        AllSoundEvents.STEAM.playAt(level, pos, volume / 16, .8f, false);
    };
    // separate pools for each side so they sound distinct when standing at corners of the boiler
    private final EnumMap<Direction, SoundPool> pools = new EnumMap<>(Direction.class);

    public void tick(FluidTankBlockEntity controller) {
        if (!isActive())
            return;
        World level = controller.getWorld();
        if (level.isClient()) {
            pools.values().forEach(p -> p.play(level));
            gauge.tickChaser();
            float current = gauge.getValue(1);
            if (current > 1 && level.random.nextFloat() < 1 / 2f)
                gauge.setValueNoUpdate(current + Math.min(-(current - 1) * level.random.nextFloat(), 0));
            return;
        }
        if (needsHeatLevelUpdate && updateTemperature(controller))
            controller.notifyUpdate();
        ticksUntilNextSample--;
        if (ticksUntilNextSample > 0)
            return;
        int capacity = controller.tankInventory.getMaxAmountPerStack();
        if (capacity == 0)
            return;

        ticksUntilNextSample = SAMPLE_RATE;
        supplyOverTime[currentIndex] = gatheredSupply / (float) SAMPLE_RATE;
        waterSupply = Math.max(waterSupply, supplyOverTime[currentIndex]);
        currentIndex = (currentIndex + 1) % supplyOverTime.length;
        gatheredSupply = 0;

        if (currentIndex == 0) {
            waterSupply = 0;
            for (float i : supplyOverTime)
                waterSupply = Math.max(i, waterSupply);
        }

        if (controller instanceof CreativeFluidTankBlockEntity)
            waterSupply = waterSupplyPerLevel * 20;

        if (getActualHeat(controller.getTotalTankSize()) == 18)
            controller.award(AllAdvancements.STEAM_ENGINE_MAXED);

        controller.notifyUpdate();
    }

    public void updateOcclusion(FluidTankBlockEntity controller) {
        if (!controller.getWorld().isClient())
            return;
        if (attachedEngines + attachedWhistles == 0)
            return;
        for (Direction d : Iterate.horizontalDirections) {
            Box aabb = new Box(controller.getPos()).offset(controller.width / 2f - .5f, 0, controller.width / 2f - .5f).contract(5f / 8);
            aabb = aabb.offset(d.getOffsetX() * (controller.width / 2f + 1 / 4f), 0, d.getOffsetZ() * (controller.width / 2f + 1 / 4f));
            aabb = aabb.expand(Math.abs(d.getOffsetZ()) / 2f, 0.25f, Math.abs(d.getOffsetX()) / 2f);
            occludedDirections[d.getHorizontalQuarterTurns()] = !controller.getWorld().isSpaceEmpty(aabb);
        }
    }

    public void queueSoundOnSide(BlockPos pos, Direction side) {
        SoundPool pool = pools.get(side);
        if (pool == null) {
            pool = new SoundPool(4, 2, sound);
            pools.put(side, pool);
        }
        pool.queueAt(pos);
    }

    public int getTheoreticalHeatLevel() {
        return activeHeat;
    }

    public int getMaxHeatLevelForBoilerSize(int boilerSize) {
        return (int) Math.min(18, boilerSize / 4);
    }

    public int getMaxHeatLevelForWaterSupply() {
        return (int) Math.min(18, MathHelper.ceil(waterSupply) / waterSupplyPerLevel);
    }

    public boolean isPassive() {
        return passiveHeat && maxHeatForSize > 0 && maxHeatForWater > 0;
    }

    public boolean isPassive(int boilerSize) {
        calcMinMaxForSize(boilerSize);
        return isPassive();
    }

    public float getEngineEfficiency(int boilerSize) {
        if (isPassive(boilerSize))
            return passiveEngineEfficiency / attachedEngines;
        if (activeHeat == 0)
            return 0;
        int actualHeat = getActualHeat(boilerSize);
        return attachedEngines <= actualHeat ? 1 : (float) actualHeat / attachedEngines;
    }

    private int getActualHeat(int boilerSize) {
        int forBoilerSize = getMaxHeatLevelForBoilerSize(boilerSize);
        int forWaterSupply = getMaxHeatLevelForWaterSupply();
        int actualHeat = Math.min(activeHeat, Math.min(forWaterSupply, forBoilerSize));
        return actualHeat;
    }

    public void calcMinMaxForSize(int boilerSize) {
        maxHeatForSize = getMaxHeatLevelForBoilerSize(boilerSize);
        maxHeatForWater = getMaxHeatLevelForWaterSupply();

        minValue = Math.min(passiveHeat ? 1 : activeHeat, Math.min(maxHeatForWater, maxHeatForSize));
        maxValue = Math.max(passiveHeat ? 1 : activeHeat, Math.max(maxHeatForWater, maxHeatForSize));
    }

    @NotNull
    public MutableText getHeatLevelTextComponent() {
        int boilerLevel = Math.min(activeHeat, Math.min(maxHeatForWater, maxHeatForSize));

        return isPassive() ? Text.translatable("create.boiler.passive") : (boilerLevel == 0 ? Text.translatable("create.boiler.idle") : boilerLevel == 18 ? Text.translatable(
            "create.boiler.max_lvl") : Text.translatable("create.boiler.lvl", String.valueOf(boilerLevel)));
    }

    public MutableText getSizeComponent(boolean forGoggles, boolean useBlocksAsBars, Formatting... styles) {
        return componentHelper("size", maxHeatForSize, forGoggles, useBlocksAsBars, styles);
    }

    public MutableText getWaterComponent(boolean forGoggles, boolean useBlocksAsBars, Formatting... styles) {
        return componentHelper("water", maxHeatForWater, forGoggles, useBlocksAsBars, styles);
    }

    public MutableText getHeatComponent(boolean forGoggles, boolean useBlocksAsBars, Formatting... styles) {
        return componentHelper("heat", passiveHeat ? 1 : activeHeat, forGoggles, useBlocksAsBars, styles);
    }

    private MutableText componentHelper(String label, int level, boolean forGoggles, boolean useBlocksAsBars, Formatting... styles) {
        MutableText base = useBlocksAsBars ? blockComponent(level) : barComponent(level);

        if (!forGoggles)
            return base;

        Formatting style1 = styles.length >= 1 ? styles[0] : Formatting.GRAY;
        Formatting style2 = styles.length >= 2 ? styles[1] : Formatting.DARK_GRAY;

        return Text.translatable("create.boiler." + label).formatted(style1)
            .append(Text.translatable("create.boiler." + label + "_dots").formatted(style2)).append(base);
    }

    private MutableText blockComponent(int level) {
        return Text.literal("█".repeat(minValue) + "▒".repeat(level - minValue) + "░".repeat(maxValue - level));
    }

    private MutableText barComponent(int level) {
        return Text.empty().append(bars(Math.max(0, minValue - 1), Formatting.DARK_GREEN)).append(bars(minValue > 0 ? 1 : 0, Formatting.GREEN))
            .append(bars(Math.max(0, level - minValue), Formatting.DARK_GREEN)).append(bars(Math.max(0, maxValue - level), Formatting.DARK_RED))
            .append(bars(Math.max(0, Math.min(18 - maxValue, ((maxValue / 5 + 1) * 5) - maxValue)), Formatting.DARK_GRAY));
    }

    private MutableText bars(int level, Formatting format) {
        return Text.literal(Strings.repeat('|', level)).formatted(format);
    }

    public boolean evaluate(FluidTankBlockEntity controller) {
        BlockPos controllerPos = controller.getPos();
        World level = controller.getWorld();
        int prevEngines = attachedEngines;
        int prevWhistles = attachedWhistles;
        attachedEngines = 0;
        attachedWhistles = 0;

        for (int yOffset = 0; yOffset < controller.height; yOffset++) {
            for (int xOffset = 0; xOffset < controller.width; xOffset++) {
                for (int zOffset = 0; zOffset < controller.width; zOffset++) {

                    BlockPos pos = controllerPos.add(xOffset, yOffset, zOffset);
                    BlockState blockState = level.getBlockState(pos);
                    if (!FluidTankBlock.isTank(blockState))
                        continue;
                    for (Direction d : Iterate.directions) {
                        BlockPos attachedPos = pos.offset(d);
                        BlockState attachedState = level.getBlockState(attachedPos);
                        if (attachedState.isOf(AllBlocks.STEAM_ENGINE) && SteamEngineBlock.getFacing(attachedState) == d)
                            attachedEngines++;
                        if (attachedState.isOf(AllBlocks.STEAM_WHISTLE) && WhistleBlock.getAttachedDirection(attachedState).getOpposite() == d)
                            attachedWhistles++;
                    }
                }
            }
        }

        needsHeatLevelUpdate = true;
        return prevEngines != attachedEngines || prevWhistles != attachedWhistles;
    }

    public void checkPipeOrganAdvancement(FluidTankBlockEntity controller) {
        AdvancementBehaviour behaviour = controller.getBehaviour(AdvancementBehaviour.TYPE);
        if (behaviour == null || !behaviour.isOwnerPresent())
            return;

        BlockPos controllerPos = controller.getPos();
        World level = controller.getWorld();
        Set<Integer> whistlePitches = new HashSet<>();

        for (int yOffset = 0; yOffset < controller.height; yOffset++) {
            for (int xOffset = 0; xOffset < controller.width; xOffset++) {
                for (int zOffset = 0; zOffset < controller.width; zOffset++) {

                    BlockPos pos = controllerPos.add(xOffset, yOffset, zOffset);
                    BlockState blockState = level.getBlockState(pos);
                    if (!FluidTankBlock.isTank(blockState))
                        continue;
                    for (Direction d : Iterate.directions) {
                        BlockPos attachedPos = pos.offset(d);
                        BlockState attachedState = level.getBlockState(attachedPos);
                        if (attachedState.isOf(AllBlocks.STEAM_WHISTLE) && WhistleBlock.getAttachedDirection(attachedState).getOpposite() == d) {
                            if (level.getBlockEntity(attachedPos) instanceof WhistleBlockEntity wbe)
                                whistlePitches.add(wbe.getPitchId());
                        }
                    }
                }
            }
        }

        if (whistlePitches.size() >= 12)
            controller.award(AllAdvancements.PIPE_ORGAN);
    }

    public boolean updateTemperature(FluidTankBlockEntity controller) {
        BlockPos controllerPos = controller.getPos();
        World level = controller.getWorld();
        needsHeatLevelUpdate = false;

        boolean prevPassive = passiveHeat;
        int prevActive = activeHeat;
        passiveHeat = false;
        activeHeat = 0;

        for (int xOffset = 0; xOffset < controller.width; xOffset++) {
            for (int zOffset = 0; zOffset < controller.width; zOffset++) {
                BlockPos pos = controllerPos.add(xOffset, -1, zOffset);
                BlockState blockState = level.getBlockState(pos);
                float heat = BoilerHeater.findHeat(level, pos, blockState);
                if (heat == 0) {
                    passiveHeat = true;
                } else if (heat > 0) {
                    activeHeat += heat;
                }
            }
        }

        passiveHeat &= activeHeat == 0;

        return prevActive != activeHeat || prevPassive != passiveHeat;
    }

    public boolean isActive() {
        return attachedEngines > 0 || attachedWhistles > 0;
    }

    public void clear() {
        waterSupply = 0;
        activeHeat = 0;
        passiveHeat = false;
        attachedEngines = 0;
        Arrays.fill(supplyOverTime, 0);
    }

    public void write(WriteView view) {
        view.putFloat("Supply", waterSupply);
        view.putInt("ActiveHeat", activeHeat);
        view.putBoolean("PassiveHeat", passiveHeat);
        view.putInt("Engines", attachedEngines);
        view.putInt("Whistles", attachedWhistles);
        view.putBoolean("Update", needsHeatLevelUpdate);
    }

    public void read(ReadView view, int boilerSize) {
        waterSupply = view.getFloat("Supply", 0);
        activeHeat = view.getInt("ActiveHeat", 0);
        passiveHeat = view.getBoolean("PassiveHeat", false);
        attachedEngines = view.getInt("Engines", 0);
        attachedWhistles = view.getInt("Whistles", 0);
        needsHeatLevelUpdate = view.getBoolean("Update", false);
        Arrays.fill(supplyOverTime, (int) waterSupply);

        int forBoilerSize = getMaxHeatLevelForBoilerSize(boilerSize);
        int forWaterSupply = getMaxHeatLevelForWaterSupply();
        int actualHeat = Math.min(activeHeat, Math.min(forWaterSupply, forBoilerSize));
        float target = isPassive(boilerSize) ? 1 / 8f : forBoilerSize == 0 ? 0 : actualHeat / (forBoilerSize * 1f);
        gauge.chase(target, 0.125f, Chaser.EXP);
    }

    public BoilerFluidHandler createHandler() {
        return new BoilerFluidHandler();
    }

    public class BoilerFluidHandler implements FluidInventory {
        private int fill;

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public FluidStack getStack(int slot) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getMaxAmountPerStack() {
            return 10 * BucketFluidInventory.CAPACITY;
        }

        @Override
        public void markDirty() {
            if (fill > 0) {
                gatheredSupply += fill;
                fill = 0;
            }
        }

        @Override
        public boolean isValid(int slot, FluidStack stack) {
            return FluidHelper.isWater(stack.getFluid());
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            fill += stack.getAmount();
        }
    }

}
