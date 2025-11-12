package com.zurrtum.create.mixin;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public class SmithingScreenHandlerMixin {
    @Inject(method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;awardUsedRecipes(Lnet/minecraft/world/entity/player/Player;Ljava/util/List;)V"))
    private void onTakeOutput(Player player, ItemStack stack, CallbackInfo ci) {
        if ((stack.is(AllItems.CARDBOARD_HELMET) || stack.is(AllItems.CARDBOARD_CHESTPLATE) || stack.is(AllItems.CARDBOARD_LEGGINGS) || stack.is(
            AllItems.CARDBOARD_BOOTS)) && player instanceof ServerPlayer serverPlayer) {
            AllAdvancements.CARDBOARD_ARMOR_TRIM.trigger(serverPlayer);
        }
    }
}
