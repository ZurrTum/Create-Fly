package com.zurrtum.create.api.equipment.potatoCannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;

// TODO: 1.21.7 - Move into api package
public interface PotatoProjectileBlockHitAction {
    Codec<PotatoProjectileBlockHitAction> CODEC = CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION.byNameCodec()
        .dispatch(PotatoProjectileBlockHitAction::codec, Function.identity());

    boolean execute(LevelAccessor level, ItemStack projectile, BlockHitResult ray);

    MapCodec<? extends PotatoProjectileBlockHitAction> codec();
}
