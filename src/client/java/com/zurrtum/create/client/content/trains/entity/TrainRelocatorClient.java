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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrainRelocatorClient {

    static WeakReference<CarriageContraptionEntity> hoveredEntity = new WeakReference<>(null);
    static UUID relocatingTrain;
    static Vec3 relocatingOrigin;
    static int relocatingEntityId;

    static BlockPos lastHoveredPos;
    static BezierTrackPointLocation lastHoveredBezierSegment;
    static Boolean lastHoveredResult;
    static List<Vec3> toVisualise = new ArrayList<>();

    public static boolean onClicked(Minecraft mc) {
        if (relocatingTrain == null)
            return false;

        LocalPlayer player = mc.player;
        if (player == null)
            return false;
        if (player.isSpectator())
            return false;

        if (!player.position().closerThan(relocatingOrigin, 24) || player.isShiftKeyDown()) {
            relocatingTrain = null;
            player.displayClientMessage(CreateLang.translateDirect("train.relocate.abort").withStyle(ChatFormatting.RED), true);
            return false;
        }

        if (player.isPassenger())
            return false;
        if (mc.level == null)
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
    public static Boolean relocateClient(Minecraft mc, Train relocating, boolean simulate) {
        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockhit))
            return null;

        BlockPos blockPos = blockhit.getBlockPos();
        BezierTrackPointLocation hoveredBezier = null;

        boolean upsideDown = relocating.carriages.getFirst().leadingBogey().isUpsideDown();
        Vec3 offset = upsideDown ? new Vec3(0, -0.5, 0) : Vec3.ZERO;

        if (simulate && !toVisualise.isEmpty() && lastHoveredResult != null) {
            for (int i = 0; i < toVisualise.size() - 1; i++) {
                Vec3 vec1 = toVisualise.get(i).add(offset);
                Vec3 vec2 = toVisualise.get(i + 1).add(offset);
                Outliner.getInstance().showLine(Pair.of(relocating, i), vec1.add(0, -.925f, 0), vec2.add(0, -.925f, 0))
                    .colored(lastHoveredResult || i != toVisualise.size() - 2 ? 0x95CD41 : 0xEA5C2B).disableLineNormals()
                    .lineWidth(i % 2 == 1 ? 1 / 6f : 1 / 4f);
            }
        }

        BezierPointSelection bezierSelection = TrackBlockOutline.result;
        if (bezierSelection != null) {
            blockPos = bezierSelection.blockEntity().getBlockPos();
            hoveredBezier = bezierSelection.loc();
        }

        if (simulate) {
            if (lastHoveredPos != null && lastHoveredPos.equals(blockPos) && Objects.equals(lastHoveredBezierSegment, hoveredBezier))
                return lastHoveredResult;
            lastHoveredPos = blockPos;
            lastHoveredBezierSegment = hoveredBezier;
            toVisualise.clear();
        }

        BlockState blockState = mc.level.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof ITrackBlock))
            return lastHoveredResult = null;

        Vec3 lookAngle = mc.player.getLookAngle();
        boolean direction = bezierSelection != null && lookAngle.dot(bezierSelection.direction()) < 0;
        boolean result = TrainRelocator.relocate(relocating, mc.level, blockPos, hoveredBezier, direction, lookAngle, toVisualise);
        if (!simulate && result) {
            relocating.carriages.forEach(c -> c.forEachPresentEntity(e -> e.nonDamageTicks = 10));
            mc.player.connection.send(new TrainRelocationPacket(relocatingTrain, blockPos, lookAngle, relocatingEntityId, direction, hoveredBezier));
        }

        return lastHoveredResult = result;
    }

    public static void clientTick(Minecraft mc) {
        LocalPlayer player = mc.player;

        if (player == null)
            return;
        if (player.isPassenger())
            return;
        ClientLevel world = mc.level;
        if (world == null)
            return;

        if (relocatingTrain != null) {
            Train relocating = getRelocating();
            if (relocating == null) {
                relocatingTrain = null;
                return;
            }

            Entity entity = world.getEntity(relocatingEntityId);
            if (entity instanceof AbstractContraptionEntity ce && Math.abs(ce.getPosition(0).subtract(ce.getPosition(1)).lengthSqr()) > 1 / 1024d) {
                player.displayClientMessage(CreateLang.translateDirect("train.cannot_relocate_moving").withStyle(ChatFormatting.RED), true);
                relocatingTrain = null;
                return;
            }

            if (!player.getMainHandItem().is(AllItems.WRENCH)) {
                player.displayClientMessage(CreateLang.translateDirect("train.relocate.abort").withStyle(ChatFormatting.RED), true);
                relocatingTrain = null;
                return;
            }

            if (!player.position().closerThan(relocatingOrigin, 24)) {
                player.displayClientMessage(CreateLang.translateDirect("train.relocate.too_far").withStyle(ChatFormatting.RED), true);
                return;
            }

            Boolean success = relocateClient(mc, relocating, true);
            if (success == null)
                player.displayClientMessage(CreateLang.translateDirect("train.relocate", relocating.name), true);
            else if (success)
                player.displayClientMessage(CreateLang.translateDirect("train.relocate.valid").withStyle(ChatFormatting.GREEN), true);
            else
                player.displayClientMessage(CreateLang.translateDirect("train.relocate.invalid").withStyle(ChatFormatting.RED), true);
            return;
        }

        Couple<Vec3> rayInputs = ContraptionHandlerClient.getRayInputs(mc, player);
        Vec3 origin = rayInputs.getFirst();
        Vec3 target = rayInputs.getSecond();

        CarriageContraptionEntity currentEntity = hoveredEntity.get();
        if (currentEntity != null) {
            if (ContraptionHandlerClient.rayTraceContraption(origin, target, currentEntity) != null)
                return;
            hoveredEntity = new WeakReference<>(null);
        }

        AABB aabb = new AABB(origin, target);
        List<CarriageContraptionEntity> intersectingContraptions = world.getEntitiesOfClass(CarriageContraptionEntity.class, aabb);

        for (CarriageContraptionEntity contraptionEntity : intersectingContraptions) {
            if (ContraptionHandlerClient.rayTraceContraption(origin, target, contraptionEntity) == null)
                continue;
            hoveredEntity = new WeakReference<>(contraptionEntity);
        }
    }

    public static boolean carriageWrenched(Vec3 vec3, CarriageContraptionEntity entity) {
        Train train = getTrainFromEntity(entity);
        if (train == null)
            return false;
        relocatingOrigin = vec3;
        relocatingTrain = train.id;
        relocatingEntityId = entity.getId();
        return true;
    }

    public static boolean addToTooltip(List<Component> tooltip) {
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
