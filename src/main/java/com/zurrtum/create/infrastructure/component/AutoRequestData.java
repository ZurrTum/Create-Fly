package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

    public static final StreamCodec<RegistryFriendlyByteBuf, AutoRequestData> STREAM_CODEC = StreamCodec.composite(
        PackageOrderWithCrafts.STREAM_CODEC,
        AutoRequestData::encodedRequest,
        ByteBufCodecs.STRING_UTF8,
        AutoRequestData::encodedTargetAddress,
        BlockPos.STREAM_CODEC,
        AutoRequestData::targetOffset,
        ByteBufCodecs.STRING_UTF8,
        AutoRequestData::targetDim,
        ByteBufCodecs.BOOL,
        AutoRequestData::isValid,
        AutoRequestData::new
    );

    public AutoRequestData() {
        this(PackageOrderWithCrafts.empty(), "", BlockPos.ZERO, "null", false);
    }

    public void writeToItem(BlockPos position, ItemStack itemStack) {
        Mutable mutable = new Mutable(this);
        mutable.targetOffset = position.offset(targetOffset);
        itemStack.set(AllDataComponents.AUTO_REQUEST_DATA, mutable.toImmutable());
    }

    public static AutoRequestData readFromItem(Level level, Player player, BlockPos position, ItemStack itemStack) {
        AutoRequestData requestData = itemStack.get(AllDataComponents.AUTO_REQUEST_DATA);
        if (requestData == null)
            return null;

        Mutable mutable = new Mutable(requestData);

        mutable.targetOffset = mutable.targetOffset.subtract(position);
        mutable.isValid = mutable.targetOffset.closerThan(BlockPos.ZERO, 128) && requestData.targetDim.equals(level.dimension().location()
            .toString());

        if (player != null) {
            MutableComponent message = mutable.isValid ? Component.translatable("create.redstone_requester.keeper_connected")
                .withStyle(ChatFormatting.WHITE) : Component.translatable("create.redstone_requester.keeper_too_far_away").withStyle(ChatFormatting.RED);
            player.displayClientMessage(message, true);
        }

        return mutable.toImmutable();
    }

    public static class Mutable {
        public PackageOrderWithCrafts encodedRequest = PackageOrderWithCrafts.empty();
        public String encodedTargetAddress = "";
        public BlockPos targetOffset = BlockPos.ZERO;
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
