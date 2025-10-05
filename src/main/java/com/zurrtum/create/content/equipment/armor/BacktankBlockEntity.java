package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.ComparatorUtil;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap.Builder;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BacktankBlockEntity extends KineticBlockEntity implements Nameable {

    public int airLevel;
    public int airLevelTimer;
    private final Text defaultName;
    private Text customName;

    private int capacityEnchantLevel;

    private ComponentChanges componentPatch;

    public BacktankBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BACKTANK, pos, state);
        defaultName = getDefaultName(state);
        componentPatch = ComponentChanges.EMPTY;
    }

    public static Text getDefaultName(BlockState state) {
        if (state.isOf(AllBlocks.NETHERITE_BACKTANK)) {
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

        BlockState state = getCachedState();
        BooleanProperty waterProperty = Properties.WATERLOGGED;
        if (state.contains(waterProperty) && state.get(waterProperty))
            return;

        if (airLevelTimer > 0) {
            airLevelTimer--;
            return;
        }

        int max = BacktankUtil.maxAir(capacityEnchantLevel);
        if (world.isClient()) {
            Vec3d centerOf = VecHelper.getCenterOf(pos);
            Vec3d v = VecHelper.offsetRandomly(centerOf, world.random, .65f);
            Vec3d m = centerOf.subtract(v);
            if (airLevel != max)
                world.addParticleClient(new AirParticleData(1, .05f), v.x, v.y, v.z, m.x, m.y, m.z);
            return;
        }

        if (airLevel == max)
            return;

        int prevComparatorLevel = getComparatorOutput();
        float abs = Math.abs(getSpeed());
        int increment = MathHelper.clamp(((int) abs - 100) / 20, 1, 5);
        airLevel = Math.min(max, airLevel + increment);
        if (getComparatorOutput() != prevComparatorLevel && !world.isClient())
            world.updateComparators(pos, state.getBlock());
        if (airLevel == max)
            sendData();
        airLevelTimer = MathHelper.clamp((int) (128f - abs / 5f) - 108, 0, 20);
    }

    public int getComparatorOutput() {
        int max = BacktankUtil.maxAir(capacityEnchantLevel);
        return ComparatorUtil.fractionToRedstoneLevel(airLevel / (float) max);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("Air", airLevel);
        view.putInt("Timer", airLevelTimer);
        view.putInt("CapacityEnchantment", capacityEnchantLevel);

        if (customName != null)
            view.put("CustomName", TextCodecs.CODEC, customName);

        view.put("Components", ComponentChanges.CODEC, componentPatch);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        int prev = airLevel;
        airLevel = view.getInt("Air", 0);
        airLevelTimer = view.getInt("Timer", 0);
        capacityEnchantLevel = view.getInt("CapacityEnchantment", 0);

        customName = view.read("CustomName", TextCodecs.CODEC).orElse(null);
        componentPatch = view.read("Components", ComponentChanges.CODEC).orElse(ComponentChanges.EMPTY);
        if (prev != 0 && prev != airLevel && airLevel == BacktankUtil.maxAir(capacityEnchantLevel) && clientPacket)
            playFilledEffect();
    }

    @Override
    protected void readComponents(ComponentsAccess componentInput) {
        setAirLevel(componentInput.getOrDefault(AllDataComponents.BACKTANK_AIR, 0));
    }

    @Override
    protected void addComponents(Builder components) {
        components.add(AllDataComponents.BACKTANK_AIR, airLevel);
    }

    protected void playFilledEffect() {
        AllSoundEvents.CONFIRM.playAt(world, pos, 0.4f, 1, true);
        Vec3d baseMotion = new Vec3d(.25, 0.1, 0);
        Vec3d baseVec = VecHelper.getCenterOf(pos);
        for (int i = 0; i < 360; i += 10) {
            Vec3d m = VecHelper.rotate(baseMotion, i, Axis.Y);
            Vec3d v = baseVec.add(m.normalize().multiply(.25f));

            world.addParticleClient(ParticleTypes.SPIT, v.x, v.y, v.z, m.x, m.y, m.z);
        }
    }

    @Override
    public Text getName() {
        return this.customName != null ? this.customName : defaultName;
    }

    public int getAirLevel() {
        return airLevel;
    }

    public void setAirLevel(int airLevel) {
        this.airLevel = airLevel;
        sendData();
    }

    public void setCustomName(Text customName) {
        this.customName = customName;
    }

    public void setCapacityEnchantLevel(int capacityEnchantLevel) {
        this.capacityEnchantLevel = capacityEnchantLevel;
    }

    public void setComponentPatch(ComponentChanges componentPatch) {
        this.componentPatch = componentPatch;
    }

    public ComponentChanges getComponentPatch() {
        return componentPatch;
    }

}
