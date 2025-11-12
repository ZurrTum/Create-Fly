package com.zurrtum.create.client.content.equipment.extendoGrip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ExtendoGripRenderHandler {

    public static float mainHandAnimation;
    public static float lastMainHandAnimation;
    public static boolean holding;
    private static final ItemStackRenderState state = new ItemStackRenderState();

    public static void tick(Minecraft mc) {
        lastMainHandAnimation = mainHandAnimation;
        mainHandAnimation *= Mth.clamp(mainHandAnimation, 0.8f, 0.99f);

        holding = false;
        if (!getRenderedOffHandStack(mc).is(AllItems.EXTENDO_GRIP))
            return;
        ItemStack main = getRenderedMainHandStack(mc);
        if (main.isEmpty())
            return;
        if (!(main.getItem() instanceof BlockItem))
            return;
        mc.getItemModelResolver().updateForTopItem(state, main, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, null, 0);
        if (!state.usesBlockLight())
            return;
        holding = true;
    }

    public static boolean onRenderPlayerHand(
        ItemStack heldItem,
        Minecraft mc,
        EntityRenderDispatcher entityRenderDispatcher,
        PoseStack ms,
        SubmitNodeCollector queue,
        int light,
        InteractionHand hand,
        float equipProgress,
        float swingProgress
    ) {
        ItemStack offhandItem = getRenderedOffHandStack(mc);
        boolean inOffhand = offhandItem.is(AllItems.EXTENDO_GRIP);
        boolean inHeldItem = heldItem.is(AllItems.EXTENDO_GRIP);
        if (!inOffhand && !inHeldItem)
            return false;
        LocalPlayer player = mc.player;
        boolean rightHand = hand == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;

        var msr = TransformStack.of(ms);
        float flip = rightHand ? 1.0F : -1.0F;
        boolean blockItem = heldItem.getItem() instanceof BlockItem;
        equipProgress = blockItem ? 0 : equipProgress / 4;

        ms.pushPose();
        if (hand == InteractionHand.MAIN_HAND) {
            if (1 - swingProgress > mainHandAnimation && swingProgress > 0 && swingProgress < 0.1)
                mainHandAnimation = 0.95f;

            ms.translate(flip * (0.64000005F - .1f), -0.4F + equipProgress * -0.6F, -0.71999997F + .3f);

            ms.pushPose();
            msr.rotateYDegrees(flip * 75.0F);
            ms.translate(flip * -1.0F, 3.6F, 3.5F);
            msr.rotateZDegrees(flip * 120).rotateXDegrees(200).rotateYDegrees(flip * -135.0F);
            ms.translate(flip * 5.6F, 0.0F, 0.0F);
            msr.rotateYDegrees(flip * 40.0F);
            ms.translate(flip * 0.05f, -0.3f, -0.3f);

            AvatarRenderer<AbstractClientPlayer> playerrenderer = entityRenderDispatcher.getPlayerRenderer(player);
            Identifier texture = player.getSkin().body().texturePath();
            if (rightHand)
                playerrenderer.renderRightHand(ms, queue, light, texture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
            else
                playerrenderer.renderLeftHand(ms, queue, light, texture, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
            ms.popPose();

            // Render gun
            ms.pushPose();
            ms.translate(flip * -0.1f, 0, -0.3f);
            state.clear();
            state.displayContext = rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            ItemModel model = mc.getModelManager().getItemModel((inOffhand ? offhandItem : heldItem).get(DataComponents.ITEM_MODEL));
            model.update(state, inHeldItem && inOffhand ? null : heldItem, mc.getItemModelResolver(), state.displayContext, mc.level, player, 0);
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
        ms.popPose();
        return true;
    }

    private static ItemStack getRenderedMainHandStack(Minecraft mc) {
        return mc.getEntityRenderDispatcher().getItemInHandRenderer().mainHandItem;
    }

    private static ItemStack getRenderedOffHandStack(Minecraft mc) {
        return mc.getEntityRenderDispatcher().getItemInHandRenderer().offHandItem;
    }

}
