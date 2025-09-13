package com.zurrtum.create.catnip.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockFace extends Pair<BlockPos, Direction> {
    public static Codec<BlockFace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos")
            .forGetter(BlockFace::getPos), Direction.CODEC.fieldOf("direction").forGetter(BlockFace::getFace)
    ).apply(instance, BlockFace::new));

    public static PacketCodec<PacketByteBuf, BlockFace> STREAM_CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        BlockFace::getPos,
        Direction.PACKET_CODEC,
        BlockFace::getFace,
        BlockFace::new
    );

    public BlockFace(BlockPos first, Direction second) {
        super(first, second);
    }

    public boolean isEquivalent(BlockFace other) {
        if (equals(other))
            return true;
        return getConnectedPos().equals(other.getPos()) && getPos().equals(other.getConnectedPos());
    }

    public BlockPos getPos() {
        return getFirst();
    }

    public Direction getFace() {
        return getSecond();
    }

    public Direction getOppositeFace() {
        return getSecond().getOpposite();
    }

    public BlockFace getOpposite() {
        return new BlockFace(getConnectedPos(), getOppositeFace());
    }

    public BlockPos getConnectedPos() {
        return getPos().offset(getFace());
    }

    public NbtCompound serializeNBT() {
        NbtCompound compoundNBT = new NbtCompound();
        compoundNBT.put("Pos", BlockPos.CODEC, getPos());
        NBTHelper.writeEnum(compoundNBT, "Face", getFace());
        return compoundNBT;
    }

    public static BlockFace fromNBT(NbtCompound compound) {
        return new BlockFace(NBTHelper.readBlockPos(compound, "Pos"), NBTHelper.readEnum(compound, "Face", Direction.class));
    }

}
