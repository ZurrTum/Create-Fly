package com.zurrtum.create.client.catnip.levelWrappers;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.ColorResolver;
import org.jetbrains.annotations.Nullable;

public class WrappedClientLevel extends ClientWorld {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    protected World level;

    public WrappedClientLevel(
        World level
    ) {
        super(
            mc.getNetworkHandler(),
            mc.world.getLevelProperties(),
            level.getRegistryKey(),
            level.getDimensionEntry(),
            mc.getNetworkHandler().chunkLoadDistance,
            mc.world.getSimulationDistance(),
            mc.worldRenderer,
            level.isDebugWorld(),
            level.getBiomeAccess().seed,
            level.getSeaLevel()
        );
        this.level = level;
    }

    public static WrappedClientLevel of(World level) {
        return new WrappedClientLevel(level);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isChunkLoaded(BlockPos pos) {
        return level.isChunkLoaded(pos);
    }

    @Override
    public boolean isPosLoaded(BlockPos pos) {
        return level.isPosLoaded(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return level.getBlockState(pos);
    }

    @Override
    public BlockView getChunkAsView(int x, int z) {
        return level.getChunkAsView(x, z);
    }

    // FIXME: blockstate#getCollisionShape with WrappedClientWorld gives unreliable
    // data (maybe)

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return level.getLightLevel(type, pos);
    }

    @Override
    public int getLuminance(BlockPos pos) {
        return level.getLuminance(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return level.getFluidState(pos);
    }

    @Override
    public int getColor(BlockPos p_225525_1_, ColorResolver p_225525_2_) {
        return level.getColor(p_225525_1_, p_225525_2_);
    }

    // FIXME: Emissive Lighting might not light stuff properly

    @Override
    public void addParticleClient(
        ParticleEffect p_195594_1_,
        double p_195594_2_,
        double p_195594_4_,
        double p_195594_6_,
        double p_195594_8_,
        double p_195594_10_,
        double p_195594_12_
    ) {
        level.addParticleClient(p_195594_1_, p_195594_2_, p_195594_4_, p_195594_6_, p_195594_8_, p_195594_10_, p_195594_12_);
    }

    @Override
    public void addParticleClient(
        ParticleEffect p_195590_1_,
        boolean p_195590_2_,
        boolean canSpawnOnMinimal,
        double p_195590_3_,
        double p_195590_5_,
        double p_195590_7_,
        double p_195590_9_,
        double p_195590_11_,
        double p_195590_13_
    ) {
        level.addParticleClient(
            p_195590_1_,
            p_195590_2_,
            canSpawnOnMinimal,
            p_195590_3_,
            p_195590_5_,
            p_195590_7_,
            p_195590_9_,
            p_195590_11_,
            p_195590_13_
        );
    }

    @Override
    public void addImportantParticleClient(
        ParticleEffect p_195589_1_,
        double p_195589_2_,
        double p_195589_4_,
        double p_195589_6_,
        double p_195589_8_,
        double p_195589_10_,
        double p_195589_12_
    ) {
        level.addImportantParticleClient(p_195589_1_, p_195589_2_, p_195589_4_, p_195589_6_, p_195589_8_, p_195589_10_, p_195589_12_);
    }

    @Override
    public void addImportantParticleClient(
        ParticleEffect p_217404_1_,
        boolean p_217404_2_,
        double p_217404_3_,
        double p_217404_5_,
        double p_217404_7_,
        double p_217404_9_,
        double p_217404_11_,
        double p_217404_13_
    ) {
        level.addImportantParticleClient(p_217404_1_, p_217404_2_, p_217404_3_, p_217404_5_, p_217404_7_, p_217404_9_, p_217404_11_, p_217404_13_);
    }

    @Override
    public void playSoundClient(
        double p_184134_1_,
        double p_184134_3_,
        double p_184134_5_,
        SoundEvent p_184134_7_,
        SoundCategory p_184134_8_,
        float p_184134_9_,
        float p_184134_10_,
        boolean p_184134_11_
    ) {
        level.playSoundClient(p_184134_1_, p_184134_3_, p_184134_5_, p_184134_7_, p_184134_8_, p_184134_9_, p_184134_10_, p_184134_11_);
    }

    @Override
    public void playSound(
        @Nullable Entity p_184148_1_,
        double p_184148_2_,
        double p_184148_4_,
        double p_184148_6_,
        SoundEvent p_184148_8_,
        SoundCategory p_184148_9_,
        float p_184148_10_,
        float p_184148_11_
    ) {
        level.playSound(p_184148_1_, p_184148_2_, p_184148_4_, p_184148_6_, p_184148_8_, p_184148_9_, p_184148_10_, p_184148_11_);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos p_175625_1_) {
        return level.getBlockEntity(p_175625_1_);
    }

    public World getWrappedLevel() {
        return level;
    }
}
