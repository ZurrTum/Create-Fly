package com.zurrtum.create.client.content.equipment.extendoGrip;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ExtendoGripRenderHandler {

    public static float mainHandAnimation;
    public static float lastMainHandAnimation;
    public static boolean holding;
    private static final ItemRenderState state = new ItemRenderState();

    public static void tick(MinecraftClient mc) {
        lastMainHandAnimation = mainHandAnimation;
        mainHandAnimation *= MathHelper.clamp(mainHandAnimation, 0.8f, 0.99f);

        holding = false;
        if (!getRenderedOffHandStack(mc).isOf(AllItems.EXTENDO_GRIP))
            return;
        ItemStack main = getRenderedMainHandStack(mc);
        if (main.isEmpty())
            return;
        if (!(main.getItem() instanceof BlockItem))
            return;
        mc.getItemModelManager().clearAndUpdate(state, main, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, null, 0);
        if (!state.isSideLit())
            return;
        holding = true;
    }

    public static boolean onRenderPlayerHand(
        ItemStack heldItem,
        MinecraftClient mc,
        EntityRenderManager entityRenderDispatcher,
        MatrixStack ms,
        OrderedRenderCommandQueue queue,
        int light,
        Hand hand,
        float equipProgress,
        float swingProgress
    ) {
        ItemStack offhandItem = getRenderedOffHandStack(mc);
        boolean inOffhand = offhandItem.isOf(AllItems.EXTENDO_GRIP);
        boolean inHeldItem = heldItem.isOf(AllItems.EXTENDO_GRIP);
        if (!inOffhand && !inHeldItem)
            return false;
        ClientPlayerEntity player = mc.player;
        boolean rightHand = hand == Hand.MAIN_HAND ^ player.getMainArm() == Arm.LEFT;

        var msr = TransformStack.of(ms);
        float flip = rightHand ? 1.0F : -1.0F;
        boolean blockItem = heldItem.getItem() instanceof BlockItem;
        equipProgress = blockItem ? 0 : equipProgress / 4;

        ms.push();
        if (hand == Hand.MAIN_HAND) {
            if (1 - swingProgress > mainHandAnimation && swingProgress > 0 && swingProgress < 0.1)
                mainHandAnimation = 0.95f;

            ms.translate(flip * (0.64000005F - .1f), -0.4F + equipProgress * -0.6F, -0.71999997F + .3f);

            ms.push();
            msr.rotateYDegrees(flip * 75.0F);
            ms.translate(flip * -1.0F, 3.6F, 3.5F);
            msr.rotateZDegrees(flip * 120).rotateXDegrees(200).rotateYDegrees(flip * -135.0F);
            ms.translate(flip * 5.6F, 0.0F, 0.0F);
            msr.rotateYDegrees(flip * 40.0F);
            ms.translate(flip * 0.05f, -0.3f, -0.3f);

            PlayerEntityRenderer<AbstractClientPlayerEntity> playerrenderer = entityRenderDispatcher.getPlayerRenderer(player);
            Identifier texture = player.getSkin().body().texturePath();
            if (rightHand)
                playerrenderer.renderRightArm(ms, queue, light, texture, player.isModelPartVisible(PlayerModelPart.RIGHT_SLEEVE));
            else
                playerrenderer.renderLeftArm(ms, queue, light, texture, player.isModelPartVisible(PlayerModelPart.LEFT_SLEEVE));
            ms.pop();

            // Render gun
            ms.push();
            ms.translate(flip * -0.1f, 0, -0.3f);
            state.clear();
            state.displayContext = rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            ItemModel model = mc.getBakedModelManager().getItemModel((inOffhand ? offhandItem : heldItem).get(DataComponentTypes.ITEM_MODEL));
            model.update(state, inHeldItem && inOffhand ? null : heldItem, mc.getItemModelManager(), state.displayContext, mc.world, player, 0);
            state.render(ms, queue, light, OverlayTexture.DEFAULT_UV, 0);
            ms.pop();
        }
        ms.pop();
        return true;
    }

    private static ItemStack getRenderedMainHandStack(MinecraftClient mc) {
        return mc.getEntityRenderDispatcher().getHeldItemRenderer().mainHand;
    }

    private static ItemStack getRenderedOffHandStack(MinecraftClient mc) {
        return mc.getEntityRenderDispatcher().getHeldItemRenderer().offHand;
    }

}
