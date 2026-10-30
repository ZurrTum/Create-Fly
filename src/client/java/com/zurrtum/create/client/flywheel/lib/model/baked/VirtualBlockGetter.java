package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import java.util.function.ToIntFunction;

public abstract class VirtualBlockGetter implements BlockAndTintGetter {
    public static final CardinalLighting FULL_LIGHTING = new CardinalLighting(1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
    protected final VirtualLightEngine lightEngine;

    public VirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
        lightEngine = new VirtualLightEngine(blockLightFunc, skyLightFunc, this);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return FULL_LIGHTING;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return lightEngine;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        Biome plainsBiome = Minecraft.getInstance().getConnection().registryAccess().lookupOrThrow(Registries.BIOME)
            .getValueOrThrow(Biomes.PLAINS);
        return resolver.getColor(plainsBiome, pos.getX(), pos.getZ());
    }
}
