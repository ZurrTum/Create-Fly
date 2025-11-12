package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.ComparatorUtil;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class BacktankBlockEntity extends KineticBlockEntity implements Nameable {

    public int airLevel;
    public int airLevelTimer;
    private final Component defaultName;
    private Component customName;

    private int capacityEnchantLevel;

    private DataComponentPatch componentPatch;

    public BacktankBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BACKTANK, pos, state);
        defaultName = getDefaultName(state);
        componentPatch = DataComponentPatch.EMPTY;
    }

    public static Component getDefaultName(BlockState state) {
        if (state.is(AllBlocks.NETHERITE_BACKTANK)) {
            AllItems.NETHERITE_BACKTANK.getName();
        }

        return AllItems.COPPER_BACKTANK.getName();
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.BACKTANK);
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        if (getSpeed() != 0)
            award(AllAdvancements.BACKTANK);
    }

    @Override
    public void tick() {
        super.tick();
        if (getSpeed() == 0)
            return;

        BlockState state = getBlockState();
        BooleanProperty waterProperty = BlockStateProperties.WATERLOGGED;
        if (state.hasProperty(waterProperty) && state.getValue(waterProperty))
            return;

        if (airLevelTimer > 0) {
            airLevelTimer--;
            return;
        }

        int max = BacktankUtil.maxAir(capacityEnchantLevel);
        if (level.isClientSide()) {
            Vec3 centerOf = VecHelper.getCenterOf(worldPosition);
            Vec3 v = VecHelper.offsetRandomly(centerOf, level.random, .65f);
            Vec3 m = centerOf.subtract(v);
            if (airLevel != max)
                level.addParticle(new AirParticleData(1, .05f), v.x, v.y, v.z, m.x, m.y, m.z);
            return;
        }

        if (airLevel == max)
            return;

        int prevComparatorLevel = getComparatorOutput();
        float abs = Math.abs(getSpeed());
        int increment = Mth.clamp(((int) abs - 100) / 20, 1, 5);
        airLevel = Math.min(max, airLevel + increment);
        if (getComparatorOutput() != prevComparatorLevel && !level.isClientSide())
            level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
        if (airLevel == max)
            sendData();
        airLevelTimer = Mth.clamp((int) (128f - abs / 5f) - 108, 0, 20);
    }

    public int getComparatorOutput() {
        int max = BacktankUtil.maxAir(capacityEnchantLevel);
        return ComparatorUtil.fractionToRedstoneLevel(airLevel / (float) max);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("Air", airLevel);
        view.putInt("Timer", airLevelTimer);
        view.putInt("CapacityEnchantment", capacityEnchantLevel);

        if (customName != null)
            view.store("CustomName", ComponentSerialization.CODEC, customName);

        view.store("Components", DataComponentPatch.CODEC, componentPatch);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        int prev = airLevel;
        airLevel = view.getIntOr("Air", 0);
        airLevelTimer = view.getIntOr("Timer", 0);
        capacityEnchantLevel = view.getIntOr("CapacityEnchantment", 0);

        customName = view.read("CustomName", ComponentSerialization.CODEC).orElse(null);
        componentPatch = view.read("Components", DataComponentPatch.CODEC).orElse(DataComponentPatch.EMPTY);
        if (prev != 0 && prev != airLevel && airLevel == BacktankUtil.maxAir(capacityEnchantLevel) && clientPacket)
            playFilledEffect();
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        setAirLevel(componentInput.getOrDefault(AllDataComponents.BACKTANK_AIR, 0));
    }

    @Override
    protected void collectImplicitComponents(Builder components) {
        components.set(AllDataComponents.BACKTANK_AIR, airLevel);
    }

    protected void playFilledEffect() {
        AllSoundEvents.CONFIRM.playAt(level, worldPosition, 0.4f, 1, true);
        Vec3 baseMotion = new Vec3(.25, 0.1, 0);
        Vec3 baseVec = VecHelper.getCenterOf(worldPosition);
        for (int i = 0; i < 360; i += 10) {
            Vec3 m = VecHelper.rotate(baseMotion, i, Axis.Y);
            Vec3 v = baseVec.add(m.normalize().scale(.25f));

            level.addParticle(ParticleTypes.SPIT, v.x, v.y, v.z, m.x, m.y, m.z);
        }
    }

    @Override
    public Component getName() {
        return this.customName != null ? this.customName : defaultName;
    }

    public int getAirLevel() {
        return airLevel;
    }

    public void setAirLevel(int airLevel) {
        this.airLevel = airLevel;
        sendData();
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
    }

    public void setCapacityEnchantLevel(int capacityEnchantLevel) {
        this.capacityEnchantLevel = capacityEnchantLevel;
    }

    public void setComponentPatch(DataComponentPatch componentPatch) {
        this.componentPatch = componentPatch;
    }

    public DataComponentPatch getComponentPatch() {
        return componentPatch;
    }

}
