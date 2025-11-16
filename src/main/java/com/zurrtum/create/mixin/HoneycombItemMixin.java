package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableBiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllBlocks;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @WrapOperation(method = "lambda$static$0()Lcom/google/common/collect/BiMap;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableBiMap;builder()Lcom/google/common/collect/ImmutableBiMap$Builder;", remap = false))
    private static ImmutableBiMap.Builder<Block, Block> addWaxed(Operation<ImmutableBiMap.Builder<Block, Block>> original) {
        ImmutableBiMap.Builder<Block, Block> builder = original.call();
        builder.put(AllBlocks.COPPER_SHINGLES, AllBlocks.WAXED_COPPER_SHINGLES);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLES, AllBlocks.WAXED_EXPOSED_COPPER_SHINGLES);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLES, AllBlocks.WAXED_WEATHERED_COPPER_SHINGLES);
        builder.put(AllBlocks.OXIDIZED_COPPER_SHINGLES, AllBlocks.WAXED_OXIDIZED_COPPER_SHINGLES);
        builder.put(AllBlocks.COPPER_SHINGLE_SLAB, AllBlocks.WAXED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLE_SLAB, AllBlocks.WAXED_EXPOSED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLE_SLAB, AllBlocks.WAXED_WEATHERED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.OXIDIZED_COPPER_SHINGLE_SLAB, AllBlocks.WAXED_OXIDIZED_COPPER_SHINGLE_SLAB);
        builder.put(AllBlocks.COPPER_SHINGLE_STAIRS, AllBlocks.WAXED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.EXPOSED_COPPER_SHINGLE_STAIRS, AllBlocks.WAXED_EXPOSED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.WEATHERED_COPPER_SHINGLE_STAIRS, AllBlocks.WAXED_WEATHERED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.OXIDIZED_COPPER_SHINGLE_STAIRS, AllBlocks.WAXED_OXIDIZED_COPPER_SHINGLE_STAIRS);
        builder.put(AllBlocks.COPPER_TILES, AllBlocks.WAXED_COPPER_TILES);
        builder.put(AllBlocks.EXPOSED_COPPER_TILES, AllBlocks.WAXED_EXPOSED_COPPER_TILES);
        builder.put(AllBlocks.WEATHERED_COPPER_TILES, AllBlocks.WAXED_WEATHERED_COPPER_TILES);
        builder.put(AllBlocks.OXIDIZED_COPPER_TILES, AllBlocks.WAXED_OXIDIZED_COPPER_TILES);
        builder.put(AllBlocks.COPPER_TILE_SLAB, AllBlocks.WAXED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.EXPOSED_COPPER_TILE_SLAB, AllBlocks.WAXED_EXPOSED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.WEATHERED_COPPER_TILE_SLAB, AllBlocks.WAXED_WEATHERED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.OXIDIZED_COPPER_TILE_SLAB, AllBlocks.WAXED_OXIDIZED_COPPER_TILE_SLAB);
        builder.put(AllBlocks.COPPER_TILE_STAIRS, AllBlocks.WAXED_COPPER_TILE_STAIRS);
        builder.put(AllBlocks.EXPOSED_COPPER_TILE_STAIRS, AllBlocks.WAXED_EXPOSED_COPPER_TILE_STAIRS);
        builder.put(AllBlocks.WEATHERED_COPPER_TILE_STAIRS, AllBlocks.WAXED_WEATHERED_COPPER_TILE_STAIRS);
        builder.put(AllBlocks.OXIDIZED_COPPER_TILE_STAIRS, AllBlocks.WAXED_OXIDIZED_COPPER_TILE_STAIRS);
        return builder;
    }
}
