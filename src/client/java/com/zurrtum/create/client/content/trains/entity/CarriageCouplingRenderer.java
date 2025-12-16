package com.zurrtum.create.client.content.trains.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

public class CarriageCouplingRenderer {
    public static void renderAll(Minecraft client, PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        Collection<Train> trains = Create.RAILWAYS.trains.values();
        VertexConsumer vb = buffer.getBuffer(RenderTypes.solidMovingBlock());
        BlockState air = Blocks.AIR.defaultBlockState();
        float partialTicks = AnimationTickHolder.getPartialTicks();
        Level level = client.level;

        for (Train train : trains) {
            List<Carriage> carriages = train.carriages;
            for (int i = 0, end = carriages.size() - 1; i < end; i++) {
                Carriage carriage = carriages.get(i);
                CarriageContraptionEntity entity = carriage.getDimensional(level).entity.get();
                Carriage carriage2 = carriages.get(i + 1);
                CarriageContraptionEntity entity2 = carriage.getDimensional(level).entity.get();

                if (entity == null || entity2 == null)
                    continue;

                CarriageBogey bogey1 = carriage.trailingBogey();
                CarriageBogey bogey2 = carriage2.leadingBogey();
                Vec3 anchor = bogey1.couplingAnchors.getSecond();
                Vec3 anchor2 = bogey2.couplingAnchors.getFirst();

                if (anchor == null || anchor2 == null)
                    continue;
                if (!anchor.closerThan(camera, 64))
                    continue;

                int lightCoords = getPackedLightCoords(entity, partialTicks);
                int lightCoords2 = getPackedLightCoords(entity2, partialTicks);

                double diffX = anchor2.x - anchor.x;
                double diffY = anchor2.y - anchor.y;
                double diffZ = anchor2.z - anchor.z;
                float yRot = AngleHelper.deg(Mth.atan2(diffZ, diffX)) + 90;
                float xRot = AngleHelper.deg(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));

                Vec3 position = entity.getPosition(partialTicks);
                Vec3 position2 = entity2.getPosition(partialTicks);

                ms.pushPose();

                {
                    ms.pushPose();
                    ms.translate(anchor.x - camera.x, anchor.y - camera.y, anchor.z - camera.z);
                    CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air).rotateYDegrees(-yRot).rotateXDegrees(xRot).light(lightCoords)
                        .renderInto(ms.last(), vb);

                    float margin = 3 / 16f;
                    double couplingDistance = train.carriageSpacing.get(i) - 2 * margin - bogey1.type.getConnectorAnchorOffset(bogey1.isUpsideDown()).z - bogey2.type.getConnectorAnchorOffset(
                        bogey2.isUpsideDown()).z;
                    int couplingSegments = (int) Math.round(couplingDistance * 4);
                    double stretch = ((anchor2.distanceTo(anchor) - 2 * margin) * 4) / couplingSegments;
                    for (int j = 0; j < couplingSegments; j++) {
                        CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_CABLE, air).rotateYDegrees(-yRot + 180).rotateXDegrees(-xRot)
                            .translate(0, 0, margin + 2 / 16f).scale(1, 1, (float) stretch).translate(0, 0, j / 4f).light(lightCoords)
                            .renderInto(ms.last(), vb);
                    }
                    ms.popPose();
                }

                {
                    ms.pushPose();
                    Vec3 translation = position2.subtract(position).add(anchor2).subtract(camera);
                    ms.translate(translation.x, translation.y, translation.z);
                    CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air).rotateYDegrees(-yRot + 180).rotateXDegrees(-xRot)
                        .light(lightCoords2).renderInto(ms.last(), vb);
                    ms.popPose();
                }

                ms.popPose();
            }
        }

    }

    public static int getPackedLightCoords(Entity pEntity, float pPartialTicks) {
        BlockPos blockpos = BlockPos.containing(pEntity.getLightProbePosition(pPartialTicks));
        return LightCoordsUtil.pack(getBlockLightLevel(pEntity, blockpos), getSkyLightLevel(pEntity, blockpos));
    }

    protected static int getSkyLightLevel(Entity pEntity, BlockPos pPos) {
        return pEntity.level().getBrightness(LightLayer.SKY, pPos);
    }

    protected static int getBlockLightLevel(Entity pEntity, BlockPos pPos) {
        return pEntity.isOnFire() ? 15 : pEntity.level().getBrightness(LightLayer.BLOCK, pPos);
    }
}
