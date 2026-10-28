package com.zurrtum.create.client.content.trains.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class CarriageCouplingRenderer {
    public static void renderAll(Minecraft client, PoseStack ms, SubmitNodeCollector queue, Vec3 camera) {
        Collection<Train> trains = Create.RAILWAYS.trains.values();
        CouplingRenderer render = null;
        for (Train train : trains) {
            List<Carriage> carriages = train.carriages;
            int size = carriages.size();
            if (size == 1) {
                continue;
            }
            if (render == null) {
                render = new CouplingRenderer(client.level);
            }
            Carriage nextCarriage = carriages.getFirst();
            for (int i = 0, end = size - 1; i < end; i++) {
                Carriage carriage = nextCarriage;
                nextCarriage = carriages.get(i + 1);
                CarriageContraptionEntity entity = render.getCarriageEntity(carriage);
                if (entity == null) {
                    continue;
                }
                CarriageBogey bogey1 = carriage.trailingBogey();
                Vec3 anchor = bogey1.couplingAnchors.getSecond();
                if (anchor == null || !anchor.closerThan(camera, 64)) {
                    continue;
                }
                CarriageBogey bogey2 = nextCarriage.leadingBogey();
                Vec3 anchor2 = bogey2.couplingAnchors.getFirst();
                if (anchor2 == null) {
                    continue;
                }
                render.updateLight(entity);
                double diffX = anchor2.x - anchor.x;
                double diffY = anchor2.y - anchor.y;
                double diffZ = anchor2.z - anchor.z;
                float yRadians = (float) -(Mth.atan2(diffZ, diffX) + Radians.DEG_90);
                float xRadians = (float) Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ));
                SuperByteBufferRenderState head = render.createHeadRenderState();

                ms.pushPose();
                ms.translate(anchor.x - camera.x, anchor.y - camera.y, anchor.z - camera.z);
                ms.pushPose();
                Pose pose = ms.last();
                pose.rotate(Axis.YP, yRadians);
                pose.rotate(Axis.XP, xRadians);
                head.submit(ms, queue);
                ms.popPose();

                double couplingDistance = train.carriageSpacing.get(i) - 0.375f - bogey1.type.getConnectorAnchorOffset(
                    bogey1.isUpsideDown()).z - bogey2.type.getConnectorAnchorOffset(bogey2.isUpsideDown()).z;
                int couplingSegments = (int) Math.round(couplingDistance * 4);
                float yRadians2 = yRadians + Radians.DEG_180;
                float xRadians2 = -xRadians;
                if (couplingSegments > 0) {
                    double stretch = (anchor2.distanceTo(anchor) - 0.375f) * 4 / couplingSegments;
                    pose = ms.last();
                    pose.rotate(Axis.YP, yRadians2);
                    pose.rotate(Axis.XP, xRadians2);
                    pose.translate(0, 0, 0.3125f);
                    pose.scale(1, 1, (float) stretch);
                    SuperByteBufferRenderState cable = render.createCableRenderState();
                    cable.submit(ms, queue);
                    for (int j = 1; j < couplingSegments; j++) {
                        pose.translate(0, 0, 0.25f);
                        cable.submit(ms, queue);
                    }
                }
                ms.popPose();

                ms.pushPose();
                pose = ms.last();
                Vec3 translation = anchor2.subtract(camera);
                pose.translate((float) translation.x, (float) translation.y, (float) translation.z);
                pose.rotate(Axis.YP, yRadians2);
                pose.rotate(Axis.XP, xRadians2);
                head.submit(ms, queue);
                ms.popPose();
            }
        }
    }

    private static class CouplingRenderer {
        private final Level level;
        private final SuperByteBuffer head;
        private final SuperByteBuffer cable;
        private final @Nullable CardinalLighting cardinalLighting;
        private final Int2ObjectMap<SuperByteBufferRenderState> headCache = new Int2ObjectOpenHashMap<>();
        private final Int2ObjectMap<SuperByteBufferRenderState> cableCache = new Int2ObjectOpenHashMap<>();
        private final float partialTicks = AnimationTickHolder.getPartialTicks();
        private int light;

        public CouplingRenderer(
            Level level
        ) {
            this.level = level;
            BlockState air = Blocks.AIR.defaultBlockState();
            head = CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air);
            cable = CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_CABLE, air);
            cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        }

        @Nullable
        public CarriageContraptionEntity getCarriageEntity(Carriage carriage) {
            return carriage.getDimensional(level).entity.get();
        }

        public void updateLight(Entity entity) {
            BlockPos pos = BlockPos.containing(entity.getLightProbePosition(partialTicks));
            light = LightCoordsUtil.pack(
                entity.isOnFire() ? 15 : level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos)
            );
        }

        public SuperByteBufferRenderState createHeadRenderState() {
            return headCache.computeIfAbsent(light, this::extractHeadRederState);
        }

        private SuperByteBufferRenderState extractHeadRederState(int light) {
            return head.cardinalLighting(cardinalLighting).light(light).extractRenderState();
        }

        public SuperByteBufferRenderState createCableRenderState() {
            return cableCache.computeIfAbsent(light, this::extractCableRenderState);
        }

        private SuperByteBufferRenderState extractCableRenderState(int light) {
            return cable.cardinalLighting(cardinalLighting).light(light).extractRenderState();
        }
    }
}
