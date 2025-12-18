package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.crafty.eiv.common.overlay.ItemSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ItemSlot.class)
public class ItemSlotMixin {
    @Shadow(remap = false)
    private boolean hovered;

    @WrapOperation(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;getTooltipFromItem(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private List<Text> getTooltipFromItem(MinecraftClient client, ItemStack stack, Operation<List<Text>> original) {
        if (hovered) {
            return original.call(client, stack);
        }
        return List.of();
    }
}
