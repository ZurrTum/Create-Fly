package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.content.equipment.armor.AllEquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(targets = "net.minecraft.client.render.entity.equipment.EquipmentRenderer$TrimSpriteKey")
public class TrimSpriteKeyMixin {
    @Final
    @Shadow
    private RegistryKey<EquipmentAsset> equipmentAssetId;

    @ModifyReturnValue(method = "getTexture()Lnet/minecraft/util/Identifier;", at = @At("RETURN"))
    private Identifier getTexture(Identifier id) {
        if (equipmentAssetId == AllEquipmentAssetKeys.CARDBOARD && id.getNamespace().equals("minecraft")) {
            return Identifier.of(MOD_ID, id.getPath());
        }
        return id;
    }
}
