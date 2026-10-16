package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

public class ConfigPlacementFilter implements PlacementFilter {
    public static final ConfigPlacementFilter INSTANCE = new ConfigPlacementFilter();
    public static final MapCodec<ConfigPlacementFilter> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return !AllConfigs.common().worldGen.disable.get();
    }

    @Override
    public MapCodec<? extends PlacementFilter> codec() {
        return CODEC;
    }
}
