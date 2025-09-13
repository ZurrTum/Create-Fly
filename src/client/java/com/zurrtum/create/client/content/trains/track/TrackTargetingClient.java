package com.zurrtum.create.client.content.trains.track;

import com.google.common.base.Objects;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.content.trains.GlobalRailwayManagerClient;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;

public class TrackTargetingClient {

    static BlockPos lastHovered;
    static boolean lastDirection;
    static EdgePointType<?> lastType;
    static BezierTrackPointLocation lastHoveredBezierSegment;

    static OverlapResult lastResult;
    static TrackGraphLocation lastLocation;

    public static void clientTick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        Vec3d lookAngle = player.getRotationVector();

        BlockPos hovered = null;
        boolean direction = false;
        EdgePointType<?> type = null;
        BezierTrackPointLocation hoveredBezier = null;

        ItemStack stack = player.getMainHandStack();
        if (stack.getItem() instanceof TrackTargetingBlockItem ttbi)
            type = ttbi.getType(stack);

        if (type == EdgePointType.SIGNAL)
            GlobalRailwayManagerClient.tickSignalOverlay(mc);

        boolean alreadySelected = stack.contains(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);

        if (type != null) {
            BezierPointSelection bezierSelection = TrackBlockOutline.result;

            if (alreadySelected) {
                hovered = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
                direction = stack.getOrDefault(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, false);
                if (stack.contains(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER)) {
                    hoveredBezier = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
                }

            } else if (bezierSelection != null) {
                hovered = bezierSelection.blockEntity().getPos();
                hoveredBezier = bezierSelection.loc();
                direction = lookAngle.dotProduct(bezierSelection.direction()) < 0;

            } else {
                HitResult hitResult = mc.crosshairTarget;
                if (hitResult != null && hitResult.getType() == Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos pos = blockHitResult.getBlockPos();
                    BlockState blockState = mc.world.getBlockState(pos);
                    if (blockState.getBlock() instanceof ITrackBlock track) {
                        direction = track.getNearestTrackAxis(mc.world, pos, blockState, lookAngle).getSecond() == AxisDirection.POSITIVE;
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
            mc.world, hovered, direction, hoveredBezier, type, (result, location) -> {
                lastResult = result;
                lastLocation = location;
            }
        );
    }

    public static void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera) {
        if (lastLocation == null || lastResult.feedback != null)
            return;

        BlockPos pos = lastHovered;
        int light = WorldRenderer.getLightmapCoordinates(mc.world, pos);
        AxisDirection direction = lastDirection ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;

        RenderedTrackOverlayType type = lastType == EdgePointType.SIGNAL ? RenderedTrackOverlayType.SIGNAL : lastType == EdgePointType.OBSERVER ? RenderedTrackOverlayType.OBSERVER : RenderedTrackOverlayType.STATION;

        BlockState state = mc.world.getBlockState(pos);
        if (!(state.getBlock() instanceof ITrackBlock track))
            return;
        TrackBlockRenderer renderer = AllTrackRenders.get(track);
        if (renderer != null) {
            ms.push();
            TransformStack.of(ms).translate(Vec3d.of(pos).subtract(camera));
            renderer.render(
                mc.world,
                state,
                pos,
                direction,
                lastHoveredBezierSegment,
                ms,
                buffer,
                light,
                OverlayTexture.DEFAULT_UV,
                type,
                1 + 1 / 16f
            );
            ms.pop();
        }
    }

}
