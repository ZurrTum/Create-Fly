package com.zurrtum.create.client.catnip.math;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VecHelper {
    // https://forums.minecraftforge.net/topic/88562-116solved-3d-to-2d-conversion/?do=findComment&comment=413573
    // slightly modified
    public static Vec3d projectToPlayerView(Vec3d target, float partialTicks) {
        /*
         * The (centered) location on the screen of the given 3d point in the world.
         * Result is (dist right of center screen, dist up from center screen, if < 0,
         * then in front of view plane)
         */
        Camera ari = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camera_pos = ari.getPos();
        Quaternionf camera_rotation_conj = new Quaternionf(ari.getRotation());
        camera_rotation_conj.conjugate();

        Vector3f result3f = new Vector3f((float) (camera_pos.x - target.x), (float) (camera_pos.y - target.y), (float) (camera_pos.z - target.z));
        result3f.rotate(camera_rotation_conj);

        // ----- compensate for view bobbing (if active) -----
        // the following code adapted from GameRenderer::applyBobbing (to invert it)
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.getBobView().getValue()) {
            Entity renderViewEntity = mc.getCameraEntity();
            if (renderViewEntity instanceof ClientPlayerEntity playerEntity) {
                float walkDist_modified = playerEntity.distanceMoved;

                float f = walkDist_modified - playerEntity.lastDistanceMoved;
                float f1 = -(walkDist_modified + f * partialTicks);
                float f2 = MathHelper.lerp(partialTicks, playerEntity.lastStrideDistance, playerEntity.strideDistance);
                Quaternionf q2 = RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F);
                q2.conjugate();
                result3f.rotate(q2);

                Quaternionf q1 = RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F);
                q1.conjugate();
                result3f.rotate(q1);

                Vector3f bob_translation = new Vector3f(
                    (MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F),
                    (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)),
                    0.0f
                );
                bob_translation.set(bob_translation.x(), -bob_translation.y(), bob_translation.z());// this is weird but hey, if it works
                result3f.add(bob_translation);
            }
        }

        // ----- adjust for fov -----
        float fov = mc.gameRenderer.getFov(ari, partialTicks, true);

        float half_height = (float) mc.getWindow().getScaledHeight() / 2;
        float scale_factor = half_height / (result3f.z() * (float) Math.tan(Math.toRadians(fov / 2)));
        return new Vec3d(-result3f.x() * scale_factor, result3f.y() * scale_factor, result3f.z());
    }
}
