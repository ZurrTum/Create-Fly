package com.zurrtum.create.client;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.block.render.MultiPosDestructionHandler;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class AllExtensions {
    public static final Map<Block, BiFunction<BlockRenderView, BlockPos, BlockRenderLayer>> LAYER = new IdentityHashMap<>();
    public static final Map<Block, MultiPosDestructionHandler> MULTI_POS = new IdentityHashMap<>();
    public static final Set<Block> BIG_OUTLINE = new HashSet<>();
    public static final Map<Item, ArmPose> ARM_POSE = new IdentityHashMap<>();

    public static void register() {
        MULTI_POS.put(
            AllBlocks.BELT, (level, pos, state, progress) -> {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof BeltBlockEntity belt) {
                    return new HashSet<>(BeltBlock.getBeltChain(level, belt.getController()));
                }
                return null;
            }
        );
        MULTI_POS.put(
            AllBlocks.WATER_WHEEL_STRUCTURAL, (level, pos, state, progress) -> {
                if (!AllBlocks.WATER_WHEEL_STRUCTURAL.stillValid(level, pos, state, false))
                    return null;
                HashSet<BlockPos> set = new HashSet<>();
                set.add(WaterWheelStructuralBlock.getMaster(level, pos, state));
                return set;
            }
        );
        MULTI_POS.put(
            AllBlocks.TRACK, (level, pos, state, progress) -> {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TrackBlockEntity track) {
                    return new HashSet<>(track.getConnections().keySet());
                }
                return null;
            }
        );
        BIG_OUTLINE.add(AllBlocks.CHAIN_CONVEYOR);
        BIG_OUTLINE.add(AllBlocks.ANDESITE_DOOR);
        BIG_OUTLINE.add(AllBlocks.BRASS_DOOR);
        BIG_OUTLINE.add(AllBlocks.COPPER_DOOR);
        BIG_OUTLINE.add(AllBlocks.TRAIN_DOOR);
        BIG_OUTLINE.add(AllBlocks.FRAMED_GLASS_DOOR);
        BIG_OUTLINE.add(AllBlocks.TRACK);
        BIG_OUTLINE.add(AllBlocks.WHITE_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.ORANGE_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.MAGENTA_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.LIGHT_BLUE_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.YELLOW_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.LIME_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.PINK_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.GRAY_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.LIGHT_GRAY_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.CYAN_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.PURPLE_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.BLUE_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.BROWN_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.GREEN_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.RED_TABLE_CLOTH);
        BIG_OUTLINE.add(AllBlocks.BLACK_TABLE_CLOTH);
        ARM_POSE.put(AllItems.POTATO_CANNON, ArmPose.CROSSBOW_HOLD);
        ARM_POSE.put(AllItems.WORLDSHAPER, ArmPose.CROSSBOW_HOLD);
        LAYER.put(AllBlocks.COPYCAT_STEP, CopycatModel::getLayer);
        LAYER.put(AllBlocks.COPYCAT_PANEL, CopycatModel::getLayer);
    }
}
