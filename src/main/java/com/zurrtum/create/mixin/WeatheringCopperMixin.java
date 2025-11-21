package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableBiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatheringCopper.class)
public interface WeatheringCopperMixin {
    @WrapOperation(method = "method_34740", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableBiMap;builder()Lcom/google/common/collect/ImmutableBiMap$Builder;", remap = false))
    private static ImmutableBiMap.Builder<Block, Block> addOxidizable(Operation<ImmutableBiMap.Builder<Block, Block>> original) {
        ImmutableBiMap.Builder<Block, Block> builder = original.call();
        builder.put(AllBlocks.COPPER_SHINGLES, AllBlocks.EXPOSED_COPPER_SHINGLES);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLES, AllBlocks.WEATHERED_COPPER_SHINGLES);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLES, AllBlocks.OXIDIZED_COPPER_SHINGLES);
        builder.put(AllBlocks.COPPER_SHINGLE_SLAB, AllBlocks.EXPOSED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLE_SLAB, AllBlocks.WEATHERED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLE_SLAB, AllBlocks.OXIDIZED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.COPPER_SHINGLE_STAIRS, AllBlocks.EXPOSED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLE_STAIRS, AllBlocks.WEATHERED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLE_STAIRS, AllBlocks.OXIDIZED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.COPPER_TILES, AllBlocks.EXPOSED_COPPER_TILES);
        builder.put(AllBlocks.EXPOSED_COPPER_TILES, AllBlocks.WEATHERED_COPPER_TILES);
        builder.put(AllBlocks.WEATHERED_COPPER_TILES, AllBlocks.OXIDIZED_COPPER_TILES);
        builder.put(AllBlocks.COPPER_TILE_SLAB, AllBlocks.EXPOSED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.EXPOSED_COPPER_TILE_SLAB, AllBlocks.WEATHERED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.WEATHERED_COPPER_TILE_SLAB, AllBlocks.OXIDIZED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.COPPER_TILE_STAIRS, AllBlocks.EXPOSED_COPPER_TILE_STAIRS);
        builder.put(AllBlocks.EXPOSED_COPPER_TILE_STAIRS, AllBlocks.WEATHERED_COPPER_TILE_STAIRS);
        builder.put(AllBlocks.WEATHERED_COPPER_TILE_STAIRS, AllBlocks.OXIDIZED_COPPER_TILE_STAIRS);
        return builder;
    }
}
