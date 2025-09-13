package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class ConfigPlacementFilter extends AbstractConditionalPlacementModifier {
    public static final ConfigPlacementFilter INSTANCE = new ConfigPlacementFilter();
    public static final MapCodec<ConfigPlacementFilter> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        return !AllConfigs.common().worldGen.disable.get();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return AllPlacementModifiers.CONFIG_FILTER;
    }
}
