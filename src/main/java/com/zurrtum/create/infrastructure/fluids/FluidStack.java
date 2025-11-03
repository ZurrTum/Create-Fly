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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.*;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.zurrtum.create.Create.LOGGER;

public class FluidStack implements ComponentHolder {
    public static final FluidStack EMPTY = new FluidStack(null);
    @SuppressWarnings("deprecation")
    public static final Codec<RegistryEntry<Fluid>> FLUID_ENTRY_CODEC = Registries.FLUID.getEntryCodec()
        .validate(entry -> entry.matches(Fluids.EMPTY.getRegistryEntry()) ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(
            entry));
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Fluid>> FLUID_ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.FLUID);
    public static final MapCodec<FluidStack> MAP_CODEC = MapCodec.recursive(
        "FluidStack", codec -> RecordCodecBuilder.mapCodec(instance -> instance.group(
            FLUID_ENTRY_CODEC.fieldOf("id").forGetter(FluidStack::getRegistryEntry),
            Codecs.POSITIVE_INT.fieldOf("amount").orElse(1).forGetter(FluidStack::getAmount),
            ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(stack -> stack.components.getChanges())
        ).apply(instance, FluidStack::new))
    );
    public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
    public static final Codec<FluidStack> OPTIONAL_CODEC = Codecs.optional(CODEC)
        .xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    public static final PacketCodec<RegistryByteBuf, FluidStack> OPTIONAL_PACKET_CODEC = new PacketCodec<RegistryByteBuf, FluidStack>() {
        public FluidStack decode(RegistryByteBuf registryByteBuf) {
            int i = registryByteBuf.readVarInt();
            if (i <= 0) {
                return FluidStack.EMPTY;
            } else {
                RegistryEntry<Fluid> registryEntry = FLUID_ENTRY_PACKET_CODEC.decode(registryByteBuf);
                ComponentChanges componentChanges = ComponentChanges.PACKET_CODEC.decode(registryByteBuf);
                return new FluidStack(registryEntry, i, componentChanges);
            }
        }

        public void encode(RegistryByteBuf registryByteBuf, FluidStack fluidStack) {
            if (fluidStack.isEmpty()) {
                registryByteBuf.writeVarInt(0);
            } else {
                registryByteBuf.writeVarInt(fluidStack.getAmount());
                FLUID_ENTRY_PACKET_CODEC.encode(registryByteBuf, fluidStack.getRegistryEntry());
                ComponentChanges.PACKET_CODEC.encode(registryByteBuf, fluidStack.components.getChanges());
            }
        }
    };
    public static final PacketCodec<RegistryByteBuf, FluidStack> PACKET_CODEC = new PacketCodec<RegistryByteBuf, FluidStack>() {
        public FluidStack decode(RegistryByteBuf registryByteBuf) {
            FluidStack fluidStack = FluidStack.OPTIONAL_PACKET_CODEC.decode(registryByteBuf);
            if (fluidStack.isEmpty()) {
                throw new DecoderException("Empty FluidStack not allowed");
            } else {
                return fluidStack;
            }
        }

        public void encode(RegistryByteBuf registryByteBuf, FluidStack fluidStack) {
            if (fluidStack.isEmpty()) {
                throw new EncoderException("Empty FluidStack not allowed");
            } else {
                FluidStack.OPTIONAL_PACKET_CODEC.encode(registryByteBuf, fluidStack);
            }
        }
    };
    private final MergedComponentMap components;
    private final Fluid fluid;
    private int amount;

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, new MergedComponentMap(ComponentMap.EMPTY));
    }

    public FluidStack(Fluid fluid, int amount, MergedComponentMap components) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components;
    }

    private FluidStack(@Nullable Void v) {
        fluid = null;
        components = new MergedComponentMap(ComponentMap.EMPTY);
    }

    public FluidStack(RegistryEntry<Fluid> fluid, int amount, ComponentChanges changes) {
        this(fluid.value(), amount, MergedComponentMap.create(ComponentMap.EMPTY, changes));
    }

    public FluidStack(Fluid fluid, int amount, ComponentChanges changes) {
        this(fluid, amount, MergedComponentMap.create(ComponentMap.EMPTY, changes));
    }

    public FluidStack(Fluid fluid, long amount, ComponentChanges changes) {
        this(fluid, (int) amount, MergedComponentMap.create(ComponentMap.EMPTY, changes));
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
            MergedComponentMap stackComponents = stack.directComponents();
            MergedComponentMap otherStackComponents = otherStack.directComponents();
            if (stackComponents == otherStackComponents) {
                return true;
            }
            Reference2ObjectMap<ComponentType<?>, Optional<?>> stackComponentMap = stackComponents.changedComponents;
            Reference2ObjectMap<ComponentType<?>, Optional<?>> otherStackComponentMap = otherStackComponents.changedComponents;
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
                ObjectSet<Reference2ObjectMap.Entry<ComponentType<?>, Optional<?>>> stackComponentSet = stackComponentMap.reference2ObjectEntrySet();
                for (Reference2ObjectMap.Entry<ComponentType<?>, Optional<?>> componentEntry : otherStackComponentMap.reference2ObjectEntrySet()) {
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

    public static Optional<FluidStack> fromNbt(RegistryWrapper.WrapperLookup registries, NbtElement nbt) {
        return CODEC.parse(registries.getOps(NbtOps.INSTANCE), nbt)
            .resultOrPartial(error -> LOGGER.error("Tried to load invalid fluid: '{}'", error));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static FluidStack fromNbt(RegistryWrapper.WrapperLookup registries, Optional<NbtCompound> nbt) {
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

    public void applyComponentsFrom(ComponentMap map) {
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

    public MergedComponentMap directComponents() {
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

    public ComponentChanges getComponentChanges() {
        return !isEmpty() ? components.getChanges() : ComponentChanges.EMPTY;
    }

    @Override
    public ComponentMap getComponents() {
        return !isEmpty() ? components : ComponentMap.EMPTY;
    }

    public Fluid getFluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public int getMaxAmount() {
        return getOrDefault(AllDataComponents.FLUID_MAX_CAPACITY, Integer.MAX_VALUE);
    }

    public Text getName() {
        if (fluid == AllFluids.POTION) {
            PotionContentsComponent contents = getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            ItemConvertible itemFromBottleType = PotionFluidHandler.itemFromBottleType(getOrDefault(
                AllDataComponents.POTION_FLUID_BOTTLE_TYPE,
                BottleType.REGULAR
            ));
            return contents.getName(itemFromBottleType.asItem().getTranslationKey() + ".effect.");
        }
        Block block = fluid.getDefaultState().getBlockState().getBlock();
        if (fluid != Fluids.EMPTY && block == Blocks.AIR) {
            return Text.translatable(Util.createTranslationKey("block", Registries.FLUID.getId(fluid)));
        } else {
            return block.getName();
        }
    }

    @SuppressWarnings("deprecation")
    public RegistryEntry<Fluid> getRegistryEntry() {
        return getFluid().getRegistryEntry();
    }

    public void increment(int amount) {
        setAmount(getAmount() + amount);
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == Fluids.EMPTY || amount <= 0;
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return getFluid().getRegistryEntry().isIn(tag);
    }

    public boolean isOf(Fluid fluid) {
        return getFluid() == fluid;
    }

    @Nullable
    public <T> T remove(ComponentType<? extends T> type) {
        return this.components.remove(type);
    }

    @Nullable
    public <T> T set(ComponentType<T> type, @Nullable T value) {
        return components.set(type, value);
    }

    public FluidStack split(int amount) {
        int i = Math.min(amount, getAmount());
        FluidStack stack = copyWithAmount(i);
        decrement(i);
        return stack;
    }

    public NbtElement toNbt(RegistryWrapper.WrapperLookup registries) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        } else {
            return CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), this).getOrThrow();
        }
    }

    public String toString() {
        return getAmount() + " " + Registries.FLUID.getEntry(getFluid()).getIdAsString();
    }
}
