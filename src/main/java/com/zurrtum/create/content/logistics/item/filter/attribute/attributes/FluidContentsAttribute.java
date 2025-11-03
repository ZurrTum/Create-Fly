package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FluidContentsAttribute(@Nullable Fluid fluid) implements ItemAttribute {
    public static final MapCodec<FluidContentsAttribute> CODEC = Registries.FLUID.getCodec()
        .xmap(FluidContentsAttribute::new, FluidContentsAttribute::fluid).fieldOf("value");

    public static final PacketCodec<RegistryByteBuf, FluidContentsAttribute> PACKET_CODEC = CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.FLUID)
        .xmap(FluidContentsAttribute::new, FluidContentsAttribute::fluid);

    @Override
    public boolean appliesTo(ItemStack itemStack, World level) {
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
            Block block = fluid.getDefaultState().getBlockState().getBlock();
            if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
                parameter = Text.translatable(Util.createTranslationKey("block", Registries.FLUID.getId(fluid)));
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
        public @NotNull ItemAttribute createAttribute() {
            return new FluidContentsAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
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
        public PacketCodec<? super RegistryByteBuf, ? extends ItemAttribute> packetCodec() {
            return PACKET_CODEC;
        }
    }
}