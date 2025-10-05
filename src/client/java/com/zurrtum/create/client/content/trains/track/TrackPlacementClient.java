package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockItem;
import com.zurrtum.create.content.trains.track.TrackPlacement;
import com.zurrtum.create.content.trains.track.TrackPlacement.PlacementInfo;
import com.zurrtum.create.infrastructure.packet.c2s.PlaceExtendedCurvePacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrackPlacementClient {
    static LerpedFloat animation = LerpedFloat.linear().startWithValue(0);
    static int lastLineCount = 0;

    static BlockPos hintPos;
    static int hintAngle;
    static Couple<List<BlockPos>> hints;

    static int extraTipWarmup;

    public static void clientTick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        ItemStack stack = player.getMainHandStack();
        HitResult hitResult = mc.crosshairTarget;
        int restoreWarmup = extraTipWarmup;
        extraTipWarmup = 0;

        if (hitResult == null)
            return;
        if (hitResult.getType() != Type.BLOCK)
            return;

        Hand hand = Hand.MAIN_HAND;
        if (!stack.isIn(AllItemTags.TRACKS)) {
            stack = player.getOffHandStack();
            hand = Hand.OFF_HAND;
            if (!stack.isIn(AllItemTags.TRACKS))
                return;
        }

        if (!stack.hasGlint())
            return;

        TrackBlockItem blockItem = (TrackBlockItem) stack.getItem();
        World level = player.getEntityWorld();
        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        if (!(hitState.getBlock() instanceof TrackBlock) && !hitState.isReplaceable()) {
            pos = pos.offset(bhr.getSide());
            hitState = blockItem.getPlacementState(new ItemUsageContext(player, hand, bhr));
            if (hitState == null)
                return;
        }

        if (!(hitState.getBlock() instanceof TrackBlock))
            return;

        extraTipWarmup = restoreWarmup;
        boolean maxTurns = mc.options.sprintKey.isPressed();
        PlacementInfo info = TrackPlacement.tryConnect(level, player, pos, hitState, stack, false, maxTurns);
        if (extraTipWarmup < 20)
            extraTipWarmup++;
        if (!info.valid || !TrackPlacement.hoveringMaxed && (info.end1Extent == 0 || info.end2Extent == 0))
            extraTipWarmup = 0;

        if (!player.isCreative() && (info.valid || !info.hasRequiredTracks || !info.hasRequiredPavement))
            BlueprintOverlayRenderer.displayTrackRequirements(info, player.getOffHandStack());

        if (info.valid)
            player.sendMessage(CreateLang.translateDirect("track.valid_connection").formatted(Formatting.GREEN), true);
        else if (info.message != null)
            player.sendMessage(
                CreateLang.translateDirect(info.message).formatted(info.message.equals("track.second_point") ? Formatting.WHITE : Formatting.RED),
                true
            );

        if (bhr.getSide() == Direction.UP) {
            Vec3d lookVec = player.getRotationVector();
            int lookAngle = (int) (22.5 + AngleHelper.deg(MathHelper.atan2(lookVec.z, lookVec.x)) % 360) / 8;

            if (!pos.equals(hintPos) || lookAngle != hintAngle) {
                hints = Couple.create(ArrayList::new);
                hintAngle = lookAngle;
                hintPos = pos;

                for (int xOffset = -2; xOffset <= 2; xOffset++) {
                    for (int zOffset = -2; zOffset <= 2; zOffset++) {
                        BlockPos offset = pos.add(xOffset, 0, zOffset);
                        PlacementInfo adjInfo = TrackPlacement.tryConnect(level, player, offset, hitState, stack, false, maxTurns);
                        hints.get(adjInfo.valid).add(offset.down());
                    }
                }
            }

            if (hints != null && !hints.either(Collection::isEmpty)) {
                Outliner.getInstance().showCluster("track_valid", hints.getFirst()).withFaceTexture(AllSpecialTextures.THIN_CHECKERED)
                    .colored(0x95CD41).lineWidth(0);
                Outliner.getInstance().showCluster("track_invalid", hints.getSecond()).withFaceTexture(AllSpecialTextures.THIN_CHECKERED)
                    .colored(0xEA5C2B).lineWidth(0);
            }
        }

        animation.chase(info.valid ? 1 : 0, 0.25, Chaser.EXP);
        animation.tickChaser();

        if (!info.valid) {
            info.end1Extent = 0;
            info.end2Extent = 0;
        }

        int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        Vec3d up = new Vec3d(0, 4 / 16f, 0);

        {
            Vec3d v1 = info.end1;
            Vec3d a1 = info.axis1.normalize();
            Vec3d n1 = info.normal1.crossProduct(a1).multiply(15 / 16f);
            Vec3d o1 = a1.multiply(0.125f);
            Vec3d ex1 = a1.multiply((info.end1Extent - (info.curve == null && info.end1Extent > 0 ? 2 : 0)) * info.axis1.length());
            line(1, v1.add(n1).add(up), o1, ex1);
            line(2, v1.subtract(n1).add(up), o1, ex1);

            Vec3d v2 = info.end2;
            Vec3d a2 = info.axis2.normalize();
            Vec3d n2 = info.normal2.crossProduct(a2).multiply(15 / 16f);
            Vec3d o2 = a2.multiply(0.125f);
            Vec3d ex2 = a2.multiply(info.end2Extent * info.axis2.length());
            line(3, v2.add(n2).add(up), o2, ex2);
            line(4, v2.subtract(n2).add(up), o2, ex2);
        }

        BezierConnection bc = info.curve;
        if (bc == null)
            return;

        Vec3d previous1 = null;
        Vec3d previous2 = null;
        int railcolor = color;
        int segCount = bc.getSegmentCount();

        float s = animation.getValue() * 7 / 8f + 1 / 8f;
        float lw = animation.getValue() * 1 / 16f + 1 / 16f;
        Vec3d end1 = bc.starts.getFirst();
        Vec3d end2 = bc.starts.getSecond();
        Vec3d finish1 = end1.add(bc.axes.getFirst().multiply(bc.getHandleLength()));
        Vec3d finish2 = end2.add(bc.axes.getSecond().multiply(bc.getHandleLength()));
        String key = "curve";

        for (int i = 0; i <= segCount; i++) {
            float t = i / (float) segCount;
            Vec3d result = VecHelper.bezier(end1, end2, finish1, finish2, t);
            Vec3d derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t).normalize();
            Vec3d normal = bc.getNormal(t).crossProduct(derivative).multiply(15 / 16f);
            Vec3d rail1 = result.add(normal).add(up);
            Vec3d rail2 = result.subtract(normal).add(up);

            if (previous1 != null) {
                Vec3d middle1 = rail1.add(previous1).multiply(0.5f);
                Vec3d middle2 = rail2.add(previous2).multiply(0.5f);
                Outliner.getInstance().showLine(Pair.of(key, i * 2), VecHelper.lerp(s, middle1, previous1), VecHelper.lerp(s, middle1, rail1))
                    .colored(railcolor).disableLineNormals().lineWidth(lw);
                Outliner.getInstance().showLine(Pair.of(key, i * 2 + 1), VecHelper.lerp(s, middle2, previous2), VecHelper.lerp(s, middle2, rail2))
                    .colored(railcolor).disableLineNormals().lineWidth(lw);
            }

            previous1 = rail1;
            previous2 = rail2;
        }

        for (int i = segCount + 1; i <= lastLineCount; i++) {
            Outliner.getInstance().remove(Pair.of(key, i * 2));
            Outliner.getInstance().remove(Pair.of(key, i * 2 + 1));
        }

        lastLineCount = segCount;
    }

    private static void line(int id, Vec3d v1, Vec3d o1, Vec3d ex) {
        int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        Outliner.getInstance().showLine(Pair.of("start", id), v1.subtract(o1), v1.add(ex)).lineWidth(1 / 8f).disableLineNormals().colored(color);
    }

    public static ActionResult sendExtenderPacket(ClientPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isIn(AllItemTags.TRACKS))
            return null;
        if (MinecraftClient.getInstance().options.sprintKey.isPressed()) {
            player.networkHandler.sendPacket(new PlaceExtendedCurvePacket(hand == Hand.MAIN_HAND, true));
            return ActionResult.SUCCESS;
        }
        return null;
    }
}
