package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.equipment.zapper.ZapperItem;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.FlattenTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public enum TerrainTools implements StringIdentifiable {
    Fill,
    Place,
    Replace,
    Clear,
    Overlay,
    Flatten;

    public static final Codec<TerrainTools> CODEC = StringIdentifiable.createCodec(TerrainTools::values);
    public static final PacketCodec<ByteBuf, TerrainTools> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(TerrainTools.class);
    public final String translationKey;

    TerrainTools() {
        this.translationKey = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String asString() {
        return translationKey;
    }

    public boolean requiresSelectedBlock() {
        return this != Clear && this != Flatten;
    }

    public void run(
        World world,
        List<BlockPos> targetPositions,
        Direction facing,
        @Nullable BlockState paintedState,
        @Nullable NbtCompound data,
        PlayerEntity player
    ) {
        switch (this) {
            case Clear:
                targetPositions.forEach(p -> world.setBlockState(p, Blocks.AIR.getDefaultState()));
                break;
            case Fill:
                targetPositions.forEach(p -> {
                    BlockState toReplace = world.getBlockState(p);
                    if (!isReplaceable(toReplace))
                        return;
                    world.setBlockState(p, paintedState);
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

                    p = p.up();

                    BlockState toReplace = world.getBlockState(p);
                    if (!isReplaceable(toReplace))
                        return;
                    world.setBlockState(p, paintedState);
                    ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
                });
                break;
            case Place:
                targetPositions.forEach(p -> {
                    world.setBlockState(p, paintedState);
                    ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
                });
                break;
            case Replace:
                targetPositions.forEach(p -> {
                    BlockState toReplace = world.getBlockState(p);
                    if (isReplaceable(toReplace))
                        return;
                    world.setBlockState(p, paintedState);
                    ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
                });
                break;
        }
    }

    public static boolean isReplaceable(BlockState toReplace) {
        return toReplace.isReplaceable();
    }
}
