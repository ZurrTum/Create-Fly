package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class ParrotPose {

    private static final ParrotEntity.Variant[] VARIANTS = new ParrotEntity.Variant[]{ParrotEntity.Variant.RED_BLUE, ParrotEntity.Variant.GREEN, ParrotEntity.Variant.YELLOW_BLUE, ParrotEntity.Variant.GRAY,}; // blue parrots are kinda hard to see

    public abstract void tick(PonderScene scene, ParrotEntity entity, Vec3d location);

    public ParrotEntity create(PonderLevel world) {
        ParrotEntity entity = new ParrotEntity(EntityType.PARROT, world);
        int nextInt = world.random.nextInt(VARIANTS.length);
        entity.setVariant(VARIANTS[nextInt]);
        return entity;
    }

    public static class DancePose extends ParrotPose {

        @Override
        public ParrotEntity create(PonderLevel world) {
            ParrotEntity entity = super.create(world);
            entity.setNearbySongPlaying(BlockPos.ORIGIN, true);
            return entity;
        }

        @Override
        public void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
            entity.lastYaw = entity.getYaw();
            entity.setYaw(entity.lastYaw - 2);
        }

    }

    public static class FlappyPose extends ParrotPose {

        @Override
        public void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
            double length = entity.getEntityPos().subtract(entity.lastRenderX, entity.lastRenderY, entity.lastRenderZ).length();
            entity.setOnGround(false);
            double phase = Math.min(length * 15, 8);
            float f = (float) ((PonderUI.ponderTicks % 100) * phase);
            entity.maxWingDeviation = MathHelper.sin(f) + 1;
            if (length == 0)
                entity.maxWingDeviation = 0;
        }

    }

    public static abstract class FaceVecPose extends ParrotPose {

        @Override
        public void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
            Vec3d p_200602_2_ = getFacedVec(scene);
            Vec3d Vector3d = location.add(entity.getCameraPosVec(0));
            double d0 = p_200602_2_.x - Vector3d.x;
            double d1 = p_200602_2_.y - Vector3d.y;
            double d2 = p_200602_2_.z - Vector3d.z;
            double d3 = MathHelper.sqrt((float) (d0 * d0 + d2 * d2));
            float targetPitch = MathHelper.wrapDegrees((float) -(MathHelper.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
            float targetYaw = MathHelper.wrapDegrees((float) -(MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) + 90);

            entity.setPitch(AngleHelper.angleLerp(.4f, entity.getPitch(), targetPitch));
            entity.setYaw(AngleHelper.angleLerp(.4f, entity.getYaw(), targetYaw));
        }

        protected abstract Vec3d getFacedVec(PonderScene scene);

    }

    public static class FacePointOfInterestPose extends FaceVecPose {

        @Override
        protected Vec3d getFacedVec(PonderScene scene) {
            return scene.getPointOfInterest();
        }

    }

    public static class FaceCursorPose extends FaceVecPose {

        @Override
        protected Vec3d getFacedVec(PonderScene scene) {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            Window w = minecraft.getWindow();
            double mouseX = minecraft.mouse.getX() * w.getScaledWidth() / w.getWidth();
            double mouseY = minecraft.mouse.getY() * w.getScaledHeight() / w.getHeight();
            return scene.getTransform().screenToScene(mouseX, mouseY, 300, 0);
        }

    }
}