package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.ponder.foundation.PonderTooltipHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> appendTooltip(List<Text> tooltip, @Local(argsOnly = true) PlayerEntity player) {
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
