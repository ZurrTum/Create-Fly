package com.zurrtum.create.client.content.trains.track;

import com.google.common.base.Objects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.content.trains.GlobalRailwayManagerClient;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class TrackTargetingClient {

    static BlockPos lastHovered;
    static boolean lastDirection;
    static EdgePointType<?> lastType;
    static BezierTrackPointLocation lastHoveredBezierSegment;

    static OverlapResult lastResult;
    static TrackGraphLocation lastLocation;

    public static void clientTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        Vec3 lookAngle = player.getLookAngle();

        BlockPos hovered = null;
        boolean direction = false;
        EdgePointType<?> type = null;
        BezierTrackPointLocation hoveredBezier = null;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof TrackTargetingBlockItem ttbi)
            type = ttbi.getType(stack);

        if (type == EdgePointType.SIGNAL)
            GlobalRailwayManagerClient.tickSignalOverlay(mc);

        boolean alreadySelected = stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);

        if (type != null) {
            BezierPointSelection bezierSelection = TrackBlockOutline.result;

            if (alreadySelected) {
                hovered = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
                direction = stack.getOrDefault(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, false);
                if (stack.has(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER)) {
                    hoveredBezier = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
                }

            } else if (bezierSelection != null) {
                hovered = bezierSelection.blockEntity().getBlockPos();
                hoveredBezier = bezierSelection.loc();
                direction = lookAngle.dot(bezierSelection.direction()) < 0;

            } else {
                HitResult hitResult = mc.hitResult;
                if (hitResult != null && hitResult.getType() == Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos pos = blockHitResult.getBlockPos();
                    BlockState blockState = mc.level.getBlockState(pos);
                    if (blockState.getBlock() instanceof ITrackBlock track) {
                        direction = track.getNearestTrackAxis(mc.level, pos, blockState, lookAngle).getSecond() == AxisDirection.POSITIVE;
                        hovered = pos;
                    }
                }
            }
        }

        if (hovered == null) {
            lastHovered = null;
            lastResult = null;
            lastLocation = null;
            lastHoveredBezierSegment = null;
            return;
        }

        if (Objects.equal(hovered, lastHovered) && Objects.equal(
            hoveredBezier,
            lastHoveredBezierSegment
        ) && direction == lastDirection && type == lastType)
            return;

        lastType = type;
        lastHovered = hovered;
        lastDirection = direction;
        lastHoveredBezierSegment = hoveredBezier;

        TrackTargetingBlockItem.withGraphLocation(
            mc.level, hovered, direction, hoveredBezier, type, (result, location) -> {
                lastResult = result;
                lastLocation = location;
            }
        );
    }

    public static void render(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
        if (lastLocation == null || lastResult.feedback != null)
            return;

        BlockPos pos = lastHovered;
        AxisDirection direction = lastDirection ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;

        RenderedTrackOverlayType type = lastType == EdgePointType.SIGNAL ? RenderedTrackOverlayType.SIGNAL : lastType == EdgePointType.OBSERVER ? RenderedTrackOverlayType.OBSERVER : RenderedTrackOverlayType.STATION;

        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof ITrackBlock track))
            return;
        TrackBlockRenderer renderer = AllTrackRenders.get(track);
        if (renderer != null) {
            renderer.getRenderState(
                mc.level,
                new Vec3(pos.getX() - camera.x(), pos.getY() - camera.y(), pos.getZ() - camera.z()),
                state,
                pos,
                direction,
                lastHoveredBezierSegment,
                type,
                1 + 1 / 16f
            ).render(ms, buffer);
        }
    }

}
