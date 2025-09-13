package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record AutoRequestData(
    PackageOrderWithCrafts encodedRequest, String encodedTargetAddress, BlockPos targetOffset, String targetDim, boolean isValid
) {

    public static final Codec<AutoRequestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        PackageOrderWithCrafts.CODEC.fieldOf("encoded_request").forGetter(AutoRequestData::encodedRequest),
        Codec.STRING.fieldOf("encoded_target_address").forGetter(AutoRequestData::encodedTargetAddress),
        BlockPos.CODEC.fieldOf("target_offset").forGetter(AutoRequestData::targetOffset),
        Codec.STRING.fieldOf("target_dim").forGetter(AutoRequestData::targetDim),
        Codec.BOOL.fieldOf("is_valid").forGetter(AutoRequestData::isValid)
    ).apply(instance, AutoRequestData::new));

    public static final PacketCodec<RegistryByteBuf, AutoRequestData> STREAM_CODEC = PacketCodec.tuple(
        PackageOrderWithCrafts.STREAM_CODEC,
        AutoRequestData::encodedRequest,
        PacketCodecs.STRING,
        AutoRequestData::encodedTargetAddress,
        BlockPos.PACKET_CODEC,
        AutoRequestData::targetOffset,
        PacketCodecs.STRING,
        AutoRequestData::targetDim,
        PacketCodecs.BOOLEAN,
        AutoRequestData::isValid,
        AutoRequestData::new
    );

    public AutoRequestData() {
        this(PackageOrderWithCrafts.empty(), "", BlockPos.ORIGIN, "null", false);
    }

    public void writeToItem(BlockPos position, ItemStack itemStack) {
        Mutable mutable = new Mutable(this);
        mutable.targetOffset = position.add(targetOffset);
        itemStack.set(AllDataComponents.AUTO_REQUEST_DATA, mutable.toImmutable());
    }

    public static AutoRequestData readFromItem(World level, PlayerEntity player, BlockPos position, ItemStack itemStack) {
        AutoRequestData requestData = itemStack.get(AllDataComponents.AUTO_REQUEST_DATA);
        if (requestData == null)
            return null;

        Mutable mutable = new Mutable(requestData);

        mutable.targetOffset = mutable.targetOffset.subtract(position);
        mutable.isValid = mutable.targetOffset.isWithinDistance(BlockPos.ZERO, 128) && requestData.targetDim.equals(level.getRegistryKey().getValue()
            .toString());

        if (player != null) {
            MutableText message = mutable.isValid ? Text.translatable("create.redstone_requester.keeper_connected")
                .formatted(Formatting.WHITE) : Text.translatable("create.redstone_requester.keeper_too_far_away").formatted(Formatting.RED);
            player.sendMessage(message, true);
        }

        return mutable.toImmutable();
    }

    public static class Mutable {
        public PackageOrderWithCrafts encodedRequest = PackageOrderWithCrafts.empty();
        public String encodedTargetAddress = "";
        public BlockPos targetOffset = BlockPos.ORIGIN;
        public String targetDim = "null";
        public boolean isValid = false;

        public Mutable() {
        }

        public Mutable(AutoRequestData data) {
            encodedRequest = data.encodedRequest;
            encodedTargetAddress = data.encodedTargetAddress;
            targetOffset = data.targetOffset;
            targetDim = data.targetDim;
            isValid = data.isValid;
        }

        public AutoRequestData toImmutable() {
            return new AutoRequestData(encodedRequest, encodedTargetAddress, targetOffset, targetDim, isValid);
        }
    }
}
