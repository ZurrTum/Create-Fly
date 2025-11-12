package com.zurrtum.create.infrastructure.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.component.BottleType;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.zurrtum.create.Create.LOGGER;

public class FluidStack implements DataComponentHolder {
    public static final FluidStack EMPTY = new FluidStack(null);
    @SuppressWarnings("deprecation")
    public static final Codec<Holder<Fluid>> FLUID_ENTRY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
        .validate(entry -> entry.is(Fluids.EMPTY.builtInRegistryHolder()) ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(
            entry));
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_ENTRY_PACKET_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);
    public static final MapCodec<FluidStack> MAP_CODEC = MapCodec.recursive(
        "FluidStack", codec -> RecordCodecBuilder.mapCodec(instance -> instance.group(
            FLUID_ENTRY_CODEC.fieldOf("id").forGetter(FluidStack::getRegistryEntry),
            ExtraCodecs.POSITIVE_INT.fieldOf("amount").orElse(1).forGetter(FluidStack::getAmount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(stack -> stack.components.asPatch())
        ).apply(instance, FluidStack::new))
    );
    public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
    public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
        .xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_PACKET_CODEC = new StreamCodec<RegistryFriendlyByteBuf, FluidStack>() {
        public FluidStack decode(RegistryFriendlyByteBuf registryByteBuf) {
            int i = registryByteBuf.readVarInt();
            if (i <= 0) {
                return FluidStack.EMPTY;
            } else {
                Holder<Fluid> registryEntry = FLUID_ENTRY_PACKET_CODEC.decode(registryByteBuf);
                DataComponentPatch componentChanges = DataComponentPatch.STREAM_CODEC.decode(registryByteBuf);
                return new FluidStack(registryEntry, i, componentChanges);
            }
        }

        public void encode(RegistryFriendlyByteBuf registryByteBuf, FluidStack fluidStack) {
            if (fluidStack.isEmpty()) {
                registryByteBuf.writeVarInt(0);
            } else {
                registryByteBuf.writeVarInt(fluidStack.getAmount());
                FLUID_ENTRY_PACKET_CODEC.encode(registryByteBuf, fluidStack.getRegistryEntry());
                DataComponentPatch.STREAM_CODEC.encode(registryByteBuf, fluidStack.components.asPatch());
            }
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> PACKET_CODEC = new StreamCodec<RegistryFriendlyByteBuf, FluidStack>() {
        public FluidStack decode(RegistryFriendlyByteBuf registryByteBuf) {
            FluidStack fluidStack = FluidStack.OPTIONAL_PACKET_CODEC.decode(registryByteBuf);
            if (fluidStack.isEmpty()) {
                throw new DecoderException("Empty FluidStack not allowed");
            } else {
                return fluidStack;
            }
        }

        public void encode(RegistryFriendlyByteBuf registryByteBuf, FluidStack fluidStack) {
            if (fluidStack.isEmpty()) {
                throw new EncoderException("Empty FluidStack not allowed");
            } else {
                FluidStack.OPTIONAL_PACKET_CODEC.encode(registryByteBuf, fluidStack);
            }
        }
    };
    private final PatchedDataComponentMap components;
    private final Fluid fluid;
    private int amount;

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    public FluidStack(Fluid fluid, int amount, PatchedDataComponentMap components) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components;
    }

    private FluidStack(@Nullable Void v) {
        fluid = null;
        components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch changes) {
        this(fluid.value(), amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, changes));
    }

    public FluidStack(Fluid fluid, int amount, DataComponentPatch changes) {
        this(fluid, amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, changes));
    }

    public FluidStack(Fluid fluid, long amount, DataComponentPatch changes) {
        this(fluid, (int) amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, changes));
    }

    public static boolean areFluidsAndComponentsEqual(FluidStack stack, FluidStack otherStack) {
        if (!stack.isOf(otherStack.getFluid())) {
            return false;
        } else {
            return stack.isEmpty() && otherStack.isEmpty() || Objects.equals(stack.components, otherStack.components);
        }
    }

    public static boolean areFluidsAndComponentsEqualIgnoreCapacity(FluidStack stack, FluidStack otherStack) {
        if (stack.isOf(otherStack.getFluid())) {
            PatchedDataComponentMap stackComponents = stack.directComponents();
            PatchedDataComponentMap otherStackComponents = otherStack.directComponents();
            if (stackComponents == otherStackComponents) {
                return true;
            }
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> stackComponentMap = stackComponents.patch;
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> otherStackComponentMap = otherStackComponents.patch;
            if (stackComponentMap == otherStackComponentMap) {
                return true;
            }
            int stackComponentCount = stackComponentMap.size();
            if (stackComponentMap.containsKey(AllDataComponents.FLUID_MAX_CAPACITY)) {
                stackComponentCount--;
            }
            int otherStackComponentCount = otherStackComponentMap.size();
            boolean hasMaxCapacityComponent = false;
            if (otherStackComponentMap.containsKey(AllDataComponents.FLUID_MAX_CAPACITY)) {
                otherStackComponentCount--;
                hasMaxCapacityComponent = true;
            }
            if (stackComponentCount != otherStackComponentCount) {
                return false;
            }
            if (hasMaxCapacityComponent) {
                ObjectSet<Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>>> stackComponentSet = stackComponentMap.reference2ObjectEntrySet();
                for (Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> componentEntry : otherStackComponentMap.reference2ObjectEntrySet()) {
                    if (!stackComponentSet.contains(componentEntry) && componentEntry.getKey() != AllDataComponents.FLUID_MAX_CAPACITY) {
                        return false;
                    }
                }
                return true;
            }
            return stackComponentMap.reference2ObjectEntrySet().containsAll(otherStackComponentMap.reference2ObjectEntrySet());
        }
        return false;
    }

    public static Optional<FluidStack> fromNbt(HolderLookup.Provider registries, Tag nbt) {
        return CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), nbt)
            .resultOrPartial(error -> LOGGER.error("Tried to load invalid fluid: '{}'", error));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static FluidStack fromNbt(HolderLookup.Provider registries, Optional<CompoundTag> nbt) {
        return nbt.flatMap(n -> fromNbt(registries, n)).orElse(FluidStack.EMPTY);
    }

    public static int hashCode(@Nullable FluidStack stack) {
        if (stack != null) {
            int i = 31 + stack.getFluid().hashCode();
            return 31 * i + stack.getComponents().hashCode();
        } else {
            return 0;
        }
    }

    public void applyComponentsFrom(DataComponentMap map) {
        components.setAll(map);
    }

    public void capAmount(int maxCount) {
        if (!isEmpty() && getAmount() > maxCount) {
            setAmount(maxCount);
        }
    }

    public FluidStack copy() {
        if (isEmpty()) {
            return EMPTY;
        } else {
            return new FluidStack(fluid, amount, components.copy());
        }
    }

    public FluidStack copyWithAmount(int amount) {
        if (isEmpty()) {
            return EMPTY;
        } else {
            return new FluidStack(fluid, amount, components.copy());
        }
    }

    public void decrement(int amount) {
        increment(-amount);
    }

    public PatchedDataComponentMap directComponents() {
        return components;
    }

    public FluidStack directCopy(int amount) {
        return new FluidStack(fluid, amount, components.copy());
    }

    public int getAmount() {
        return isEmpty() ? 0 : amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public DataComponentPatch getComponentChanges() {
        return !isEmpty() ? components.asPatch() : DataComponentPatch.EMPTY;
    }

    @Override
    public DataComponentMap getComponents() {
        return !isEmpty() ? components : DataComponentMap.EMPTY;
    }

    public Fluid getFluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public int getMaxAmount() {
        return getOrDefault(AllDataComponents.FLUID_MAX_CAPACITY, Integer.MAX_VALUE);
    }

    public Component getName() {
        if (fluid == AllFluids.POTION) {
            PotionContents contents = getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            ItemLike itemFromBottleType = PotionFluidHandler.itemFromBottleType(getOrDefault(
                AllDataComponents.POTION_FLUID_BOTTLE_TYPE,
                BottleType.REGULAR
            ));
            return contents.getName(itemFromBottleType.asItem().getDescriptionId() + ".effect.");
        }
        Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
        if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
            return Component.translatable(Util.makeDescriptionId("block", BuiltInRegistries.FLUID.getKey(fluid)));
        } else {
            return block.getName();
        }
    }

    @SuppressWarnings("deprecation")
    public Holder<Fluid> getRegistryEntry() {
        return getFluid().builtInRegistryHolder();
    }

    public void increment(int amount) {
        setAmount(getAmount() + amount);
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == Fluids.EMPTY || amount <= 0;
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return getFluid().builtInRegistryHolder().is(tag);
    }

    public boolean isOf(Fluid fluid) {
        return getFluid() == fluid;
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> type) {
        return this.components.remove(type);
    }

    @Nullable
    public <T> T set(DataComponentType<T> type, @Nullable T value) {
        return components.set(type, value);
    }

    public FluidStack split(int amount) {
        int i = Math.min(amount, getAmount());
        FluidStack stack = copyWithAmount(i);
        decrement(i);
        return stack;
    }

    public Tag toNbt(HolderLookup.Provider registries) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        } else {
            return CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }
    }

    public String toString() {
        return getAmount() + " " + BuiltInRegistries.FLUID.wrapAsHolder(getFluid()).getRegisteredName();
    }
}
