package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.content.equipment.armor.AllEquipmentAssetKeys;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(targets = "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer$TrimSpriteKey")
public class TrimSpriteKeyMixin {
    @Final
    @Shadow
    private ResourceKey<EquipmentAsset> equipmentAssetId;

    @ModifyReturnValue(method = "spriteId()Lnet/minecraft/resources/ResourceLocation;", at = @At("RETURN"))
    private ResourceLocation getTexture(ResourceLocation id) {
        if (equipmentAssetId == AllEquipmentAssetKeys.CARDBOARD && id.getNamespace().equals("minecraft")) {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, id.getPath());
        }
        return id;
    }
}
