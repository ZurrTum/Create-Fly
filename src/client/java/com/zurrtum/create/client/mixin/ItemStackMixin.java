package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.ponder.foundation.PonderTooltipHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @ModifyReturnValue(method = "getTooltipLines", at = @At("RETURN"))
    private List<Component> appendTooltip(List<Component> tooltip, @Local(argsOnly = true) Player player) {
        PonderTooltipHandler.addToTooltip(tooltip, (ItemStack) (Object) this);
        if (!AllConfigs.client().tooltips.get() || player == null)
            return tooltip;
        TooltipModifier modifier = TooltipModifier.REGISTRY.get(getItem());
        if (modifier != null) {
            modifier.modify(tooltip, player);
        }
        return tooltip;
    }
}
