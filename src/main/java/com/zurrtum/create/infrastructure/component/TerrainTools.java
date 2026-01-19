package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.equipment.zapper.ZapperItem;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.FlattenTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public enum TerrainTools implements StringRepresentable {
    Fill,
    Place,
    Replace,
    Clear,
    Overlay,
    Flatten;

    public static final Codec<TerrainTools> CODEC = StringRepresentable.fromEnum(TerrainTools::values);
    public static final StreamCodec<ByteBuf, TerrainTools> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(TerrainTools.class);
    public final String translationKey;

    TerrainTools() {
        this.translationKey = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return translationKey;
    }

    public boolean requiresSelectedBlock() {
        return this != Clear && this != Flatten;
    }

    public void run(
        Level world,
        List<BlockPos> targetPositions,
        Direction facing,
        @Nullable BlockState paintedState,
        @Nullable CompoundTag data,
        Player player
    ) {
        switch (this) {
            case Clear:
                targetPositions.forEach(p -> world.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState()));
                break;
            case Fill:
                targetPositions.forEach(p -> {
                    BlockState toReplace = world.getBlockState(p);
                    if (!isReplaceable(toReplace))
                        return;
                    world.setBlockAndUpdate(p, paintedState);
                    ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
                });
                break;
            case Flatten:
                FlattenTool.apply(world, targetPositions, facing);
                break;
            case Overlay:
                targetPositions.forEach(p -> {
                    BlockState toOverlay = world.getBlockState(p);
                    if (isReplaceable(toOverlay))
                        return;
                    if (toOverlay == paintedState)
                        return;

                    p = p.above();

                    BlockState toReplace = world.getBlockState(p);
                    if (!isReplaceable(toReplace))
                        return;
                    world.setBlockAndUpdate(p, paintedState);
                    ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
                });
                break;
            case Place:
                targetPositions.forEach(p -> {
                    world.setBlockAndUpdate(p, paintedState);
                    ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
                });
                break;
            case Replace:
                targetPositions.forEach(p -> {
                    BlockState toReplace = world.getBlockState(p);
                    if (isReplaceable(toReplace))
                        return;
                    world.setBlockAndUpdate(p, paintedState);
                    ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
                });
                break;
        }
    }

    public static boolean isReplaceable(BlockState toReplace) {
        return toReplace.canBeReplaced();
    }
}
