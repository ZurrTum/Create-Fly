package com.zurrtum.create.client.catnip.math;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VecHelper {
    // https://forums.minecraftforge.net/topic/88562-116solved-3d-to-2d-conversion/?do=findComment&comment=413573
    // slightly modified
    public static Vec3 projectToPlayerView(Vec3 target, float partialTicks) {
        /*
         * The (centered) location on the screen of the given 3d point in the world.
         * Result is (dist right of center screen, dist up from center screen, if < 0,
         * then in front of view plane)
         */
        Camera ari = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camera_pos = ari.position();
        Quaternionf camera_rotation_conj = new Quaternionf(ari.rotation());
        camera_rotation_conj.conjugate();

        Vector3f result3f = new Vector3f((float) (camera_pos.x - target.x), (float) (camera_pos.y - target.y), (float) (camera_pos.z - target.z));
        result3f.rotate(camera_rotation_conj);

        // ----- compensate for view bobbing (if active) -----
        // the following code adapted from GameRenderer::applyBobbing (to invert it)
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.bobView().get()) {
            Entity renderViewEntity = mc.getCameraEntity();
            if (renderViewEntity instanceof LocalPlayer playerEntity) {
                ClientAvatarState clientPlayerLikeState = playerEntity.avatarState();
                float f = clientPlayerLikeState.getBackwardsInterpolatedWalkDistance(partialTicks);
                float g = clientPlayerLikeState.getInterpolatedBob(partialTicks);
                Quaternionf q2 = Axis.XP.rotationDegrees(Math.abs(Mth.cos(f * (float) Math.PI - 0.2F) * g) * 5.0F);
                q2.conjugate();
                result3f.rotate(q2);

                Quaternionf q1 = Axis.ZP.rotationDegrees(Mth.sin(f * (float) Math.PI) * g * 3.0F);
                q1.conjugate();
                result3f.rotate(q1);

                Vector3f bob_translation = new Vector3f(Mth.sin(f * (float) Math.PI) * g * 0.5F, -Math.abs(Mth.cos(f * (float) Math.PI) * g), 0.0f);
                bob_translation.set(bob_translation.x(), -bob_translation.y(), bob_translation.z());// this is weird but hey, if it works
                result3f.add(bob_translation);
            }
        }

        // ----- adjust for fov -----
        float fov = mc.gameRenderer.getFov(ari, partialTicks, true);

        float half_height = (float) mc.getWindow().getGuiScaledHeight() / 2;
        float scale_factor = half_height / (result3f.z() * (float) Math.tan(Math.toRadians(fov / 2)));
        return new Vec3(-result3f.x() * scale_factor, result3f.y() * scale_factor, result3f.z());
    }
}
