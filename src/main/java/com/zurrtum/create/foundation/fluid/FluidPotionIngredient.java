package com.zurrtum.create.foundation.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FluidPotionIngredient(Fluid fluid, BottleType type, PotionsPredicate potions,
                                    int amount) implements FluidIngredient {
    @Nullable
    public static FluidIngredient of(PotionIngredient ingredient, int amount) {
        Optional<Holder<Item>> item = ingredient.ingredient().getSingleItem();
        if (item.isEmpty()) {
            return null;
        }
        Optional<PotionsPredicate> optional = ingredient.potions();
        if (optional.isEmpty()) {
            return null;
        }
        BottleType type = PotionFluidHandler.bottleTypeFromItem(item.get().value());
        PotionsPredicate potions = optional.get();
        Optional<HolderSet<Potion>> potionHolder = potions.potions();
        Fluid fluid = AllFluids.POTION;
        if (potionHolder.isPresent()) {
            HolderSet<Potion> potionSet = potionHolder.get();
            if (potionSet.size() == 1 && potionSet.get(0) == Potions.WATER) {
                if (type == BottleType.REGULAR && potions.effects().isEmpty()) {
                    return new FluidStackIngredient(Fluids.WATER, DataComponentPatch.EMPTY, amount);
                }
                fluid = Fluids.WATER;
            }
        }
        return new FluidPotionIngredient(fluid, type, potions, amount);
    }

    @Override
    public boolean test(FluidStack stack) {
        return stack.isOf(fluid) && stack.getOrDefault(
            AllDataComponents.POTION_FLUID_BOTTLE_TYPE,
            BottleType.REGULAR
        ) == type && potions.matches(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
    }

    @Override
    public List<Fluid> getMatchingFluids() {
        return List.of(fluid);
    }

    @Override
    public List<FluidStack> getMatchingFluidStacks() {
        Optional<HolderSet<Potion>> holders = potions.potions();
        if (holders.isEmpty()) {
            return List.of();
        }
        List<FluidStack> list = new ArrayList<>();
        for (Holder<Potion> holder : holders.get()) {
            FluidStack stack = new FluidStack(fluid, amount);
            stack.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, type);
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
            list.add(stack);
        }
        return list;
    }

    @Override
    public FluidIngredientSerializer getSerializer() {
        return FluidIngredientSerializer.FLUID_POTION;
    }

    public record Serializer(String type) implements FluidIngredientSerializer {
        public static final MapCodec<FluidPotionIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidPotionIngredient::fluid),
            BottleType.CODEC.fieldOf("type").forGetter(FluidPotionIngredient::type),
            PotionsPredicate.CODEC.fieldOf("potions").forGetter(FluidPotionIngredient::potions),
            Codec.INT.optionalFieldOf("amount", 81000).forGetter(FluidPotionIngredient::amount)
        ).apply(instance, FluidPotionIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FluidPotionIngredient> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.FLUID),
            FluidPotionIngredient::fluid,
            BottleType.STREAM_CODEC,
            FluidPotionIngredient::type,
            PotionsPredicate.STREAM_CODEC,
            FluidPotionIngredient::potions,
            ByteBufCodecs.INT,
            FluidPotionIngredient::amount,
            FluidPotionIngredient::new
        );

        @Override
        public MapCodec<FluidPotionIngredient> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidPotionIngredient> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
