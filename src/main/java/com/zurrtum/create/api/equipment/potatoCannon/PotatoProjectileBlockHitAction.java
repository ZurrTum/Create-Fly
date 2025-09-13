package com.zurrtum.create.api.equipment.potatoCannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.WorldAccess;

import java.util.function.Function;

// TODO: 1.21.7 - Move into api package
public interface PotatoProjectileBlockHitAction {
    Codec<PotatoProjectileBlockHitAction> CODEC = CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION.getCodec()
        .dispatch(PotatoProjectileBlockHitAction::codec, Function.identity());

    boolean execute(WorldAccess level, ItemStack projectile, BlockHitResult ray);

    MapCodec<? extends PotatoProjectileBlockHitAction> codec();
}
