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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrackPlacementClient {
    static LerpedFloat animation = LerpedFloat.linear().startWithValue(0);
    static int lastLineCount = 0;

    static @Nullable BlockPos hintPos;
    static int hintAngle;
    static @Nullable Couple<List<BlockPos>> hints;

    static int extraTipWarmup;

    public static void clientTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = mc.hitResult;
        int restoreWarmup = extraTipWarmup;
        extraTipWarmup = 0;

        if (hitResult == null)
            return;
        if (hitResult.getType() != Type.BLOCK)
            return;

        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!stack.is(AllItemTags.TRACKS)) {
            stack = player.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
            if (!stack.is(AllItemTags.TRACKS))
                return;
        }

        if (!stack.hasFoil())
            return;

        TrackBlockItem blockItem = (TrackBlockItem) stack.getItem();
        Level level = player.level();
        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        if (!(hitState.getBlock() instanceof TrackBlock) && !hitState.canBeReplaced()) {
            pos = pos.relative(bhr.getDirection());
            hitState = blockItem.getPlacementState(new UseOnContext(player, hand, bhr));
            if (hitState == null)
                return;
        }

        if (!(hitState.getBlock() instanceof TrackBlock))
            return;

        extraTipWarmup = restoreWarmup;
        boolean maxTurns = mc.options.keySprint.isDown();
        PlacementInfo info = TrackPlacement.tryConnect(level, player, pos, hitState, stack, false, maxTurns);
        if (extraTipWarmup < 20)
            extraTipWarmup++;
        if (!info.valid || !TrackPlacement.hoveringMaxed && (info.end1Extent == 0 || info.end2Extent == 0))
            extraTipWarmup = 0;

        if (!player.isCreative() && (info.valid || !info.hasRequiredTracks || !info.hasRequiredPavement))
            BlueprintOverlayRenderer.displayTrackRequirements(info, player.getOffhandItem());

        if (info.valid)
            player.displayClientMessage(CreateLang.translateDirect("track.valid_connection").withStyle(ChatFormatting.GREEN), true);
        else if (info.message != null)
            player.displayClientMessage(
                CreateLang.translateDirect(info.message)
                    .withStyle(info.message.equals("track.second_point") ? ChatFormatting.WHITE : ChatFormatting.RED), true
            );

        if (bhr.getDirection() == Direction.UP) {
            Vec3 lookVec = player.getLookAngle();
            int lookAngle = (int) (22.5 + AngleHelper.deg(Mth.atan2(lookVec.z, lookVec.x)) % 360) / 8;

            if (!pos.equals(hintPos) || lookAngle != hintAngle) {
                hints = Couple.create(ArrayList::new);
                hintAngle = lookAngle;
                hintPos = pos;

                for (int xOffset = -2; xOffset <= 2; xOffset++) {
                    for (int zOffset = -2; zOffset <= 2; zOffset++) {
                        BlockPos offset = pos.offset(xOffset, 0, zOffset);
                        PlacementInfo adjInfo = TrackPlacement.tryConnect(level, player, offset, hitState, stack, false, maxTurns);
                        hints.get(adjInfo.valid).add(offset.below());
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
        Vec3 up = new Vec3(0, 4 / 16f, 0);

        {
            Vec3 v1 = info.end1;
            Vec3 a1 = info.axis1.normalize();
            Vec3 n1 = info.normal1.cross(a1).scale(15 / 16f);
            Vec3 o1 = a1.scale(0.125f);
            Vec3 ex1 = a1.scale((info.end1Extent - (info.curve == null && info.end1Extent > 0 ? 2 : 0)) * info.axis1.length());
            line(1, v1.add(n1).add(up), o1, ex1);
            line(2, v1.subtract(n1).add(up), o1, ex1);

            Vec3 v2 = info.end2;
            Vec3 a2 = info.axis2.normalize();
            Vec3 n2 = info.normal2.cross(a2).scale(15 / 16f);
            Vec3 o2 = a2.scale(0.125f);
            Vec3 ex2 = a2.scale(info.end2Extent * info.axis2.length());
            line(3, v2.add(n2).add(up), o2, ex2);
            line(4, v2.subtract(n2).add(up), o2, ex2);
        }

        BezierConnection bc = info.curve;
        if (bc == null)
            return;

        Vec3 previous1 = null;
        Vec3 previous2 = null;
        int railcolor = color;
        int segCount = bc.getSegmentCount();

        float s = animation.getValue() * 7 / 8f + 1 / 8f;
        float lw = animation.getValue() * 1 / 16f + 1 / 16f;
        Vec3 end1 = bc.starts.getFirst();
        Vec3 end2 = bc.starts.getSecond();
        Vec3 finish1 = end1.add(bc.axes.getFirst().scale(bc.getHandleLength()));
        Vec3 finish2 = end2.add(bc.axes.getSecond().scale(bc.getHandleLength()));
        String key = "curve";

        for (int i = 0; i <= segCount; i++) {
            float t = i / (float) segCount;
            Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
            Vec3 derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t).normalize();
            Vec3 normal = bc.getNormal(t).cross(derivative).scale(15 / 16f);
            Vec3 rail1 = result.add(normal).add(up);
            Vec3 rail2 = result.subtract(normal).add(up);

            if (previous1 != null) {
                Vec3 middle1 = rail1.add(previous1).scale(0.5f);
                Vec3 middle2 = rail2.add(previous2).scale(0.5f);
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

    private static void line(int id, Vec3 v1, Vec3 o1, Vec3 ex) {
        int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        Outliner.getInstance().showLine(Pair.of("start", id), v1.subtract(o1), v1.add(ex)).lineWidth(1 / 8f).disableLineNormals().colored(color);
    }

    @Nullable
    public static InteractionResult sendExtenderPacket(LocalPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(AllItemTags.TRACKS))
            return null;
        if (Minecraft.getInstance().options.keySprint.isDown()) {
            player.connection.send(new PlaceExtendedCurvePacket(hand == InteractionHand.MAIN_HAND, true));
            return InteractionResult.SUCCESS;
        }
        return null;
    }
}
