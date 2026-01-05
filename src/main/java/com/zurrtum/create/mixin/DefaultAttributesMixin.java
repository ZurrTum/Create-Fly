package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllEntityAttributes;
import com.zurrtum.create.api.registry.CreateRegisterPlugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultAttributes.class)
public class DefaultAttributesMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", remap = false))
    private static ImmutableMap<EntityType<? extends LivingEntity>, AttributeSupplier> addAttributes(
        ImmutableMap.Builder<EntityType<? extends LivingEntity>, AttributeSupplier> builder,
        Operation<ImmutableMap<EntityType<? extends LivingEntity>, AttributeSupplier>> original
    ) {
        CreateRegisterPlugin.registerEntityAttributes();
        AllEntityAttributes.ATTRIBUTES.forEach((type, factory) -> {
            builder.put(type, factory.get().build());
        });
        return original.call(builder);
    }
}
