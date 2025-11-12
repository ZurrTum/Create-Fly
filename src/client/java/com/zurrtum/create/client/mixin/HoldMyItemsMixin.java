package com.zurrtum.create.client.mixin;

import com.holdmylua.source.LuaTestHMI;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.item.UncontainableBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(ItemInHandRenderer.class)
public class HoldMyItemsMixin {
    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void renderItem(
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        CallbackInfo ci
    ) {
        Item item = stack.getItem();
        if (!LuaTestHMI.renderAsBlock.getOrDefault(item.toString(), true) && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MOD_ID)) {
            if (item == AllItems.LINKED_CONTROLLER) {
                displayContext = switch (displayContext) {
                    case THIRD_PERSON_LEFT_HAND -> ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                    case THIRD_PERSON_RIGHT_HAND -> ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                    default -> displayContext;
                };
                Minecraft mc = Minecraft.getInstance();
                boolean rightHanded = mc.options.mainHand().get() == HumanoidArm.RIGHT;
                ItemDisplayContext mainHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                ItemDisplayContext offHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                boolean noControllerInMain = !mc.player.getMainHandItem().is(AllItems.LINKED_CONTROLLER);
                if (displayContext == mainHand || (displayContext == offHand && noControllerInMain)) {
                    matrices.translate(0, 0.1f, 0.1f);
                }
            } else if (item != AllItems.BELT_CONNECTOR && !(item instanceof UncontainableBlockItem)) {
                matrices.mulPose(Axis.XP.rotationDegrees(-75));
                matrices.mulPose(Axis.ZP.rotationDegrees(-50));
                matrices.mulPose(Axis.YP.rotationDegrees(-15));
                matrices.translate(0.1f, -0.2f, 0.2f);
            }
        }
    }
}
