package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.zurrtum.create.foundation.recipe.ComponentsIngredient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Ingredient.class)
public class IngredientMixin {
    @Mutable
    @Shadow
    @Final
    public static Codec<Ingredient> CODEC;

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/PacketCodec;", ordinal = 0))
    private static PacketCodec<RegistryByteBuf, Ingredient> getPacketCodec(PacketCodec<RegistryByteBuf, Ingredient> packetCodec) {
        return new PacketCodec<>() {
            @Override
            public Ingredient decode(RegistryByteBuf buf) {
                int index = buf.readerIndex();
                if (buf.readVarInt() != -1) {
                    buf.readerIndex(index);
                    return packetCodec.decode(buf);
                }
                return ComponentsIngredient.PACKET_CODEC.decode(buf);
            }

            @Override
            public void encode(RegistryByteBuf buf, Ingredient value) {
                if (value instanceof ComponentsIngredient componentsIngredient) {
                    buf.writeVarInt(-1);
                    ComponentsIngredient.PACKET_CODEC.encode(buf, componentsIngredient);
                } else {
                    packetCodec.encode(buf, value);
                }
            }
        };
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/PacketCodec;", ordinal = 1))
    private static PacketCodec<RegistryByteBuf, Optional<Ingredient>> getIngredientPacketCodec(PacketCodec<RegistryByteBuf, Optional<Ingredient>> packetCodec) {
        return new PacketCodec<>() {
            @Override
            public Optional<Ingredient> decode(RegistryByteBuf buf) {
                int index = buf.readerIndex();
                if (buf.readVarInt() != -1) {
                    buf.readerIndex(index);
                    return packetCodec.decode(buf);
                }
                return Optional.of(ComponentsIngredient.PACKET_CODEC.decode(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, Optional<Ingredient> value) {
                if (value.isPresent() && value.get() instanceof ComponentsIngredient componentsIngredient) {
                    buf.writeVarInt(-1);
                    ComponentsIngredient.PACKET_CODEC.encode(buf, componentsIngredient);
                } else {
                    packetCodec.encode(buf, value);
                }
            }
        };
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void injectCodec(CallbackInfo ci) {
        Codec<Ingredient> codec = CODEC;
        CODEC = new Codec<>() {
            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<MapLike<T>> map = ops.getMap(input);
                if (map.isError()) {
                    return codec.decode(ops, input);
                }
                T type = map.getOrThrow().get(ComponentsIngredient.TYPE_KEY);
                if (type == null) {
                    return codec.decode(ops, input);
                }
                if (ops.getStringValue(type).getOrThrow().equals(ComponentsIngredient.STRING_ID)) {
                    return (DataResult) ComponentsIngredient.CODEC.decode(ops, input);
                }
                return codec.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
                if (input instanceof ComponentsIngredient componentsIngredient) {
                    return ComponentsIngredient.CODEC.encode(componentsIngredient, ops, prefix);
                }
                return codec.encode(input, ops, prefix);
            }
        };
    }
}
