package com.zurrtum.create.client.mixin;

import com.holdmylua.source.LuaTestHMI;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.item.UncontainableBlockItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(HeldItemRenderer.class)
public class HoldMyItemsMixin {
    @WrapOperation(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V"))
    private void renderItem(
        ItemRenderer instance,
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int light,
        int overlay,
        int seed,
        Operation<Void> original
    ) {
        Item item = stack.getItem();
        if (!LuaTestHMI.renderAsBlock.getOrDefault(item.toString(), true) && Registries.ITEM.getId(item).getNamespace().equals(MOD_ID)) {
            if (item == AllItems.LINKED_CONTROLLER) {
                displayContext = switch (displayContext) {
                    case THIRD_PERSON_LEFT_HAND -> ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                    case THIRD_PERSON_RIGHT_HAND -> ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                    default -> displayContext;
                };
                MinecraftClient mc = MinecraftClient.getInstance();
                boolean rightHanded = mc.options.getMainArm().getValue() == Arm.RIGHT;
                ItemDisplayContext mainHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                ItemDisplayContext offHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                boolean noControllerInMain = !mc.player.getMainHandStack().isOf(AllItems.LINKED_CONTROLLER);
                if (displayContext == mainHand || (displayContext == offHand && noControllerInMain)) {
                    matrices.translate(0, 0.1f, 0.1f);
                }
            } else if (item != AllItems.BELT_CONNECTOR && !(item instanceof UncontainableBlockItem)) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-75));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-50));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-15));
                matrices.translate(0.1f, -0.2f, 0.2f);
            }
        }
        original.call(instance, entity, stack, displayContext, matrices, vertexConsumers, world, light, overlay, seed);
    }
}
