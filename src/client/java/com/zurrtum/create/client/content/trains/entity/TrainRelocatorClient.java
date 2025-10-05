package com.zurrtum.create.client.content.trains.entity;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TrainRelocator;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import com.zurrtum.create.infrastructure.packet.c2s.TrainRelocationPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrainRelocatorClient {

    static WeakReference<CarriageContraptionEntity> hoveredEntity = new WeakReference<>(null);
    static UUID relocatingTrain;
    static Vec3d relocatingOrigin;
    static int relocatingEntityId;

    static BlockPos lastHoveredPos;
    static BezierTrackPointLocation lastHoveredBezierSegment;
    static Boolean lastHoveredResult;
    static List<Vec3d> toVisualise = new ArrayList<>();

    public static boolean onClicked(MinecraftClient mc) {
        if (relocatingTrain == null)
            return false;

        ClientPlayerEntity player = mc.player;
        if (player == null)
            return false;
        if (player.isSpectator())
            return false;

        if (!player.getEntityPos().isInRange(relocatingOrigin, 24) || player.isSneaking()) {
            relocatingTrain = null;
            player.sendMessage(CreateLang.translateDirect("train.relocate.abort").formatted(Formatting.RED), true);
            return false;
        }

        if (player.hasVehicle())
            return false;
        if (mc.world == null)
            return false;
        Train relocating = getRelocating();
        if (relocating != null) {
            Boolean relocate = relocateClient(mc, relocating, false);
            if (relocate != null) {
                if (relocate) {
                    relocatingTrain = null;
                }
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Boolean relocateClient(MinecraftClient mc, Train relocating, boolean simulate) {
        HitResult hitResult = mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockhit))
            return null;

        BlockPos blockPos = blockhit.getBlockPos();
        BezierTrackPointLocation hoveredBezier = null;

        boolean upsideDown = relocating.carriages.getFirst().leadingBogey().isUpsideDown();
        Vec3d offset = upsideDown ? new Vec3d(0, -0.5, 0) : Vec3d.ZERO;

        if (simulate && !toVisualise.isEmpty() && lastHoveredResult != null) {
            for (int i = 0; i < toVisualise.size() - 1; i++) {
                Vec3d vec1 = toVisualise.get(i).add(offset);
                Vec3d vec2 = toVisualise.get(i + 1).add(offset);
                Outliner.getInstance().showLine(Pair.of(relocating, i), vec1.add(0, -.925f, 0), vec2.add(0, -.925f, 0))
                    .colored(lastHoveredResult || i != toVisualise.size() - 2 ? 0x95CD41 : 0xEA5C2B).disableLineNormals()
                    .lineWidth(i % 2 == 1 ? 1 / 6f : 1 / 4f);
            }
        }

        BezierPointSelection bezierSelection = TrackBlockOutline.result;
        if (bezierSelection != null) {
            blockPos = bezierSelection.blockEntity().getPos();
            hoveredBezier = bezierSelection.loc();
        }

        if (simulate) {
            if (lastHoveredPos != null && lastHoveredPos.equals(blockPos) && Objects.equals(lastHoveredBezierSegment, hoveredBezier))
                return lastHoveredResult;
            lastHoveredPos = blockPos;
            lastHoveredBezierSegment = hoveredBezier;
            toVisualise.clear();
        }

        BlockState blockState = mc.world.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof ITrackBlock))
            return lastHoveredResult = null;

        Vec3d lookAngle = mc.player.getRotationVector();
        boolean direction = bezierSelection != null && lookAngle.dotProduct(bezierSelection.direction()) < 0;
        boolean result = TrainRelocator.relocate(relocating, mc.world, blockPos, hoveredBezier, direction, lookAngle, toVisualise);
        if (!simulate && result) {
            relocating.carriages.forEach(c -> c.forEachPresentEntity(e -> e.nonDamageTicks = 10));
            mc.player.networkHandler.sendPacket(new TrainRelocationPacket(
                relocatingTrain,
                blockPos,
                lookAngle,
                relocatingEntityId,
                direction,
                hoveredBezier
            ));
        }

        return lastHoveredResult = result;
    }

    public static void clientTick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;

        if (player == null)
            return;
        if (player.hasVehicle())
            return;
        ClientWorld world = mc.world;
        if (world == null)
            return;

        if (relocatingTrain != null) {
            Train relocating = getRelocating();
            if (relocating == null) {
                relocatingTrain = null;
                return;
            }

            Entity entity = world.getEntityById(relocatingEntityId);
            if (entity instanceof AbstractContraptionEntity ce && Math.abs(ce.getLerpedPos(0).subtract(ce.getLerpedPos(1))
                .lengthSquared()) > 1 / 1024d) {
                player.sendMessage(CreateLang.translateDirect("train.cannot_relocate_moving").formatted(Formatting.RED), true);
                relocatingTrain = null;
                return;
            }

            if (!player.getMainHandStack().isOf(AllItems.WRENCH)) {
                player.sendMessage(CreateLang.translateDirect("train.relocate.abort").formatted(Formatting.RED), true);
                relocatingTrain = null;
                return;
            }

            if (!player.getEntityPos().isInRange(relocatingOrigin, 24)) {
                player.sendMessage(CreateLang.translateDirect("train.relocate.too_far").formatted(Formatting.RED), true);
                return;
            }

            Boolean success = relocateClient(mc, relocating, true);
            if (success == null)
                player.sendMessage(CreateLang.translateDirect("train.relocate", relocating.name), true);
            else if (success)
                player.sendMessage(CreateLang.translateDirect("train.relocate.valid").formatted(Formatting.GREEN), true);
            else
                player.sendMessage(CreateLang.translateDirect("train.relocate.invalid").formatted(Formatting.RED), true);
            return;
        }

        Couple<Vec3d> rayInputs = ContraptionHandlerClient.getRayInputs(mc, player);
        Vec3d origin = rayInputs.getFirst();
        Vec3d target = rayInputs.getSecond();

        CarriageContraptionEntity currentEntity = hoveredEntity.get();
        if (currentEntity != null) {
            if (ContraptionHandlerClient.rayTraceContraption(origin, target, currentEntity) != null)
                return;
            hoveredEntity = new WeakReference<>(null);
        }

        Box aabb = new Box(origin, target);
        List<CarriageContraptionEntity> intersectingContraptions = world.getNonSpectatingEntities(CarriageContraptionEntity.class, aabb);

        for (CarriageContraptionEntity contraptionEntity : intersectingContraptions) {
            if (ContraptionHandlerClient.rayTraceContraption(origin, target, contraptionEntity) == null)
                continue;
            hoveredEntity = new WeakReference<>(contraptionEntity);
        }
    }

    public static boolean carriageWrenched(Vec3d vec3, CarriageContraptionEntity entity) {
        Train train = getTrainFromEntity(entity);
        if (train == null)
            return false;
        relocatingOrigin = vec3;
        relocatingTrain = train.id;
        relocatingEntityId = entity.getId();
        return true;
    }

    public static boolean addToTooltip(List<Text> tooltip) {
        Train train = getTrainFromEntity(hoveredEntity.get());
        if (train != null && train.derailed) {
            TooltipHelper.addHint(tooltip, "hint.derailed_train");
            return true;
        }
        return false;
    }

    private static Train getRelocating() {
        return relocatingTrain == null ? null : Create.RAILWAYS.trains.get(relocatingTrain);
    }

    private static Train getTrainFromEntity(CarriageContraptionEntity carriageContraptionEntity) {
        if (carriageContraptionEntity == null)
            return null;
        Carriage carriage = carriageContraptionEntity.getCarriage();
        if (carriage == null)
            return null;
        return carriage.train;
    }

}
