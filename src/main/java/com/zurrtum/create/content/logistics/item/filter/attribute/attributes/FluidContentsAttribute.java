package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FluidContentsAttribute(@Nullable Fluid fluid) implements ItemAttribute {
    public static final MapCodec<FluidContentsAttribute> CODEC = BuiltInRegistries.FLUID.byNameCodec()
        .xmap(FluidContentsAttribute::new, FluidContentsAttribute::fluid).fieldOf("value");

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContentsAttribute> PACKET_CODEC = CatnipStreamCodecBuilders.nullable(
        CatnipStreamCodecs.FLUID).map(FluidContentsAttribute::new, FluidContentsAttribute::fluid);

    @Override
    public boolean appliesTo(ItemStack itemStack, Level level) {
        try (FluidItemInventory capability = FluidHelper.getFluidInventory(itemStack)) {
            if (capability != null) {
                for (int i = 0, size = capability.size(); i < size; i++) {
                    if (capability.getStack(i).getFluid() == fluid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getTranslationKey() {
        return "has_fluid";
    }

    @Override
    public Object[] getTranslationParameters() {
        Object parameter = "";
        if (fluid != null) {
            Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
            if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
                parameter = Component.translatable(Util.makeDescriptionId("block", BuiltInRegistries.FLUID.getKey(fluid)));
            } else {
                parameter = block.getName();
            }
        }
        return new Object[]{parameter};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.HAS_FLUID;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public ItemAttribute createAttribute() {
            return new FluidContentsAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
            List<ItemAttribute> list = new ArrayList<>();

            try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack)) {
                if (capability != null) {
                    for (int i = 0, size = capability.size(); i < size; i++) {
                        list.add(new FluidContentsAttribute(capability.getStack(i).getFluid()));
                    }
                }
            }

            return list;
        }

        @Override
        public MapCodec<? extends ItemAttribute> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> packetCodec() {
            return PACKET_CODEC;
        }
    }
}