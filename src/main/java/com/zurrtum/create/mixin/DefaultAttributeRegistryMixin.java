package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllEntityAttributes;
import com.zurrtum.create.api.registry.CreateRegisterPlugin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultAttributeRegistry.class)
public class DefaultAttributeRegistryMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", remap = false))
    private static ImmutableMap<EntityType<? extends LivingEntity>, DefaultAttributeContainer> addAttributes(
        ImmutableMap.Builder<EntityType<? extends LivingEntity>, DefaultAttributeContainer> builder,
        Operation<ImmutableMap<EntityType<? extends LivingEntity>, DefaultAttributeContainer>> original
    ) {
        CreateRegisterPlugin.registerEntityAttributes();
        AllEntityAttributes.ATTRIBUTES.forEach((type, factory) -> {
            builder.put(type, factory.get().build());
        });
        return original.call(builder);
    }
}
