package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.function.BiConsumer;

public class TrackTargetingBlockItem extends BlockItem {

    private final EdgePointType<?> type;

    public TrackTargetingBlockItem(Block pBlock, Settings pProperties, EdgePointType<?> type) {
        super(pBlock, pProperties);
        this.type = type;
    }

    public static TrackTargetingBlockItem station(Block pBlock, Settings pProperties) {
        return new TrackTargetingBlockItem(pBlock, pProperties, EdgePointType.STATION);
    }

    public static TrackTargetingBlockItem signal(Block pBlock, Settings pProperties) {
        return new TrackTargetingBlockItem(pBlock, pProperties, EdgePointType.SIGNAL);
    }

    public static TrackTargetingBlockItem observer(Block pBlock, Settings pProperties) {
        return new TrackTargetingBlockItem(pBlock, pProperties, EdgePointType.OBSERVER);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext pContext) {
        ItemStack stack = pContext.getStack();
        BlockPos pos = pContext.getBlockPos();
        World level = pContext.getWorld();
        BlockState state = level.getBlockState(pos);
        PlayerEntity player = pContext.getPlayer();

        if (player == null)
            return ActionResult.FAIL;

        if (player.isSneaking() && stack.contains(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
            if (level.isClient())
                return ActionResult.SUCCESS;
            player.sendMessage(Text.translatable("create.track_target.clear"), true);
            stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
            stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
            stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, .5f);
            return ActionResult.SUCCESS;
        }

        if (state.getBlock() instanceof ITrackBlock track) {
            if (level.isClient())
                return ActionResult.SUCCESS;

            Vec3d lookAngle = player.getRotationVector();
            boolean front = track.getNearestTrackAxis(level, pos, state, lookAngle).getSecond() == AxisDirection.POSITIVE;
            EdgePointType<?> type = getType(stack);

            MutableObject<OverlapResult> result = new MutableObject<>(null);
            withGraphLocation(level, pos, front, null, type, (overlap, location) -> result.setValue(overlap));

            if (result.getValue().feedback != null) {
                player.sendMessage(Text.translatable("create." + result.getValue().feedback).formatted(Formatting.RED), true);
                AllSoundEvents.DENY.play(level, null, pos, .5f, 1);
                return ActionResult.FAIL;
            }

            stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS, pos);
            stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, front);
            stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
            player.sendMessage(Text.translatable("create.track_target.set"), true);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1);
            return ActionResult.SUCCESS;
        }

        if (!stack.contains(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
            player.sendMessage(Text.translatable("create.track_target.missing").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putBoolean("TargetDirection", stack.getOrDefault(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, false));

        BlockPos selectedPos = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
        BlockPos placedPos = pos.offset(pContext.getSide(), state.isReplaceable() ? 0 : 1);

        boolean bezier = stack.contains(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);

        if (!selectedPos.isWithinDistance(placedPos, bezier ? AllConfigs.server().trains.maxTrackPlacementLength.get() + 16 : 16)) {
            player.sendMessage(Text.translatable("create.track_target.too_far").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        if (bezier) {
            BezierTrackPointLocation bezierTrackPointLocation = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
            NbtCompound bezierNbt = new NbtCompound();
            bezierNbt.putInt("Segment", bezierTrackPointLocation.segment());
            bezierNbt.put("Key", BlockPos.CODEC, bezierTrackPointLocation.curveTarget().subtract(placedPos));
            blockEntityData.put("Bezier", bezierNbt);
        }

        blockEntityData.put("TargetTrack", BlockPos.CODEC, selectedPos.subtract(placedPos));
        blockEntityData.put("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, ((IBE<?>) this.getBlock()).getBlockEntityType());

        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(blockEntityData));
        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);

        ActionResult useOn = super.useOnBlock(pContext);
        stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);

        if (level.isClient() || useOn == ActionResult.FAIL)
            return useOn;

        ItemStack itemInHand = player.getStackInHand(pContext.getHand());
        if (!itemInHand.isEmpty()) {
            itemInHand.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
            itemInHand.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
            itemInHand.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
        }
        player.sendMessage(Text.translatable("create.track_target.success").formatted(Formatting.GREEN), true);

        if (type == EdgePointType.SIGNAL)
            AllAdvancements.SIGNAL.trigger((ServerPlayerEntity) player);

        return useOn;
    }

    public EdgePointType<?> getType(ItemStack stack) {
        return type;
    }

    public enum OverlapResult {

        VALID,
        OCCUPIED("track_target.occupied"),
        JUNCTION("track_target.no_junctions"),
        NO_TRACK("track_target.invalid");

        public String feedback;

        OverlapResult() {
        }

        OverlapResult(String feedback) {
            this.feedback = feedback;
        }

    }

    public static void withGraphLocation(
        World level,
        BlockPos pos,
        boolean front,
        BezierTrackPointLocation targetBezier,
        EdgePointType<?> type,
        BiConsumer<OverlapResult, TrackGraphLocation> callback
    ) {

        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof ITrackBlock track)) {
            callback.accept(OverlapResult.NO_TRACK, null);
            return;
        }

        List<Vec3d> trackAxes = track.getTrackAxes(level, pos, state);
        if (targetBezier == null && trackAxes.size() > 1) {
            callback.accept(OverlapResult.JUNCTION, null);
            return;
        }

        AxisDirection targetDirection = front ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        TrackGraphLocation location = targetBezier != null ? TrackGraphHelper.getBezierGraphLocationAt(
            level,
            pos,
            targetDirection,
            targetBezier
        ) : TrackGraphHelper.getGraphLocationAt(level, pos, targetDirection, trackAxes.getFirst());

        if (location == null) {
            callback.accept(OverlapResult.NO_TRACK, null);
            return;
        }

        Couple<TrackNode> nodes = location.edge.map(location.graph::locateNode);
        TrackEdge edge = location.graph.getConnection(nodes);
        if (edge == null)
            return;

        EdgeData edgeData = edge.getEdgeData();
        double edgePosition = location.position;

        for (TrackEdgePoint edgePoint : edgeData.getPoints()) {
            double otherEdgePosition = edgePoint.getLocationOn(edge);
            double distance = Math.abs(edgePosition - otherEdgePosition);
            if (distance > .75)
                continue;
            if (edgePoint.canCoexistWith(type, front) && distance < .25)
                continue;

            callback.accept(OverlapResult.OCCUPIED, location);
            return;
        }

        callback.accept(OverlapResult.VALID, location);
    }

}
