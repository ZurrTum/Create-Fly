package com.zurrtum.create.client.content.equipment.armor;

import com.google.common.cache.Cache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.logistics.box.PackageRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public class CardboardArmorHandlerClient {
    private static final Cache<Integer, Integer> BOXES_PLAYERS_ARE_HIDING_AS = new TickBasedCache<>(20, true);
    private static final Random RANDOM = new Random();

    public static void keepCacheAliveDesignDespiteNotRendering(AbstractClientPlayer player) {
        if (!CardboardArmorHandler.testForStealth(player))
            return;
        try {
            getCurrentBoxIndex(player.getId());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static boolean playerRendersAsBoxWhenSneaking(
        AvatarRenderer<?> renderer,
        AvatarRenderState state,
        PoseStack ms,
        SubmitNodeCollector queue
    ) {
        if (state.pose != Pose.CROUCHING || !CardboardArmorHandler.isCardboardArmor(state.headEquipment) || !CardboardArmorHandler.isCardboardArmor(
            state.chestEquipment) || !CardboardArmorHandler.isCardboardArmor(state.legsEquipment) || !CardboardArmorHandler.isCardboardArmor(
            state.feetEquipment)) {
            return false;
        }

        CardboardRenderState renderState = (CardboardRenderState) state;
        if (renderState.create$isFlying())
            return false;

        if (renderState.create$isSkip())
            return true;

        ms.pushPose();

        Vec3 renderOffset = renderer.getRenderOffset(state);
        ms.translate(0, -renderOffset.y, 0);

        if (renderState.create$isOnGround()) {
            ms.translate(
                0, Math.min(
                    Math.abs(Mth.cos((AnimationTickHolder.getRenderTime() % 256) / 2.0f)) * -renderOffset.y,
                    renderState.create$getMovement() * 5
                ), 0
            );
        }

        float scale = state.scale;
        ms.scale(scale, scale, scale);

        try {
            PartialModel model = AllPartialModels.PACKAGES_TO_HIDE_AS.get(getCurrentBoxIndex(state.id));
            if (model != null) {
                PackageRenderer.getBoxRenderState(state.id, renderState.create$getInterpolatedYaw(), state.lightCoords, model).render(ms, queue);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ms.popPose();
        return true;
    }

    private static Integer getCurrentBoxIndex(int id) throws ExecutionException {
        return BOXES_PLAYERS_ARE_HIDING_AS.get(id, () -> RANDOM.nextInt(AllPartialModels.PACKAGES_TO_HIDE_AS.size()));
    }
}
