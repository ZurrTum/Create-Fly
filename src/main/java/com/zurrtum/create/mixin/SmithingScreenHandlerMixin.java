package com.zurrtum.create.mixin;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreenHandler.class)
public class SmithingScreenHandlerMixin {
    @Inject(method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;unlockLastRecipe(Lnet/minecraft/entity/player/PlayerEntity;Ljava/util/List;)V"))
    private void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if ((stack.isOf(AllItems.CARDBOARD_HELMET) || stack.isOf(AllItems.CARDBOARD_CHESTPLATE) || stack.isOf(AllItems.CARDBOARD_LEGGINGS) || stack.isOf(
            AllItems.CARDBOARD_BOOTS)) && player instanceof ServerPlayerEntity serverPlayer) {
            AllAdvancements.CARDBOARD_ARMOR_TRIM.trigger(serverPlayer);
        }
    }
}
