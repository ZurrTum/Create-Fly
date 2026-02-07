package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.items.EnchantmentExtend;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<Enchantment> wrap(
        Function<RecordCodecBuilder.Instance<Enchantment>, ? extends App<RecordCodecBuilder.Mu<Enchantment>, Enchantment>> builder,
        Operation<Codec<Enchantment>> original
    ) {
        return new EnchantmentExtend(original.call(builder));
    }
}
