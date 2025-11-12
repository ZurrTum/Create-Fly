package com.zurrtum.create.api.equipment.potatoCannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

// TODO: 1.21.7 - Move into api package
public interface PotatoProjectileEntityHitAction {
    Codec<PotatoProjectileEntityHitAction> CODEC = CreateRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION.byNameCodec()
        .dispatch(PotatoProjectileEntityHitAction::codec, Function.identity());

    enum Type {
        PRE_HIT,
        ON_HIT
    }

    /**
     * @return true if the hit should be canceled if the type is {@link Type#PRE_HIT PRE_HIT},
     * true if this shouldn't recover the projectile if the type is {@link Type#ON_HIT ON_HIT}
     */
    boolean execute(ItemStack projectile, EntityHitResult ray, Type type);

    MapCodec<? extends PotatoProjectileEntityHitAction> codec();
}
