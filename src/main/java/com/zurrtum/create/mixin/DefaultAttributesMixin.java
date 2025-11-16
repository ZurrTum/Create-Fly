package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllEntityAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultAttributes.class)
public class DefaultAttributesMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "com/google/common/collect/ImmutableMap.builder()Lcom/google/common/collect/ImmutableMap$Builder;", remap = false))
    private static ImmutableMap.Builder<EntityType<? extends LivingEntity>, AttributeSupplier> addAttributes(Operation<ImmutableMap.Builder<EntityType<? extends LivingEntity>, AttributeSupplier>> original) {
        ImmutableMap.Builder<EntityType<? extends LivingEntity>, AttributeSupplier> builder = original.call();
        AllEntityAttributes.registerIfNeeded();
        AllEntityAttributes.ATTRIBUTES.forEach((type, factory) -> {
            builder.put(type, factory.get().build());
        });
        return builder;
    }
}
