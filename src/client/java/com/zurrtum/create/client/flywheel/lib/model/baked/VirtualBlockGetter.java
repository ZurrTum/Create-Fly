package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.function.ToIntFunction;

public abstract class VirtualBlockGetter implements BlockRenderView {
    protected final VirtualLightEngine lightEngine;

    public VirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
        lightEngine = new VirtualLightEngine(blockLightFunc, skyLightFunc, this);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 1f;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return lightEngine;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver resolver) {
        Biome plainsBiome = MinecraftClient.getInstance().getNetworkHandler().getRegistryManager().getOrThrow(RegistryKeys.BIOME)
            .getValueOrThrow(BiomeKeys.PLAINS);
        return resolver.getColor(plainsBiome, pos.getX(), pos.getZ());
    }
}
