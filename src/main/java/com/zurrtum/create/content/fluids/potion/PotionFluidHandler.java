package com.zurrtum.create.content.fluids.potion;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.BottleFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;

public class PotionFluidHandler {
    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

    public static boolean isPotionItem(ItemStack stack) {
        return stack.getItem() instanceof PotionItem && !(stack.getItem().getCraftingRemainder().getItem() instanceof BucketItem) && !stack.is(
            AllItemTags.NOT_POTION);
    }

    public static Pair<FluidStack, ItemStack> emptyPotion(ItemStack stack, boolean simulate) {
        FluidStack fluid = getFluidFromPotionItem(stack);
        if (!simulate)
            stack.shrink(1);
        return Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE));
    }

    //TODO
    //    public static FluidIngredient potionIngredient(Holder<Potion> potion, int amount) {
    //        return FluidIngredient.fromFluidStack(FluidHelper.copyStackWithAmount(PotionFluidHandler
    //            .getFluidFromPotionItem(PotionContents.createItemStack(Items.POTION, potion)), amount));
    //    }

    public static FluidStack getFluidFromPotionItem(ItemStack stack) {
        PotionContents potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        BottleType bottleTypeFromItem = bottleTypeFromItem(stack.getItem());
        if (potion.is(Potions.WATER) && potion.customEffects().isEmpty() && bottleTypeFromItem == BottleType.REGULAR)
            return new FluidStack(Fluids.WATER, BottleFluidInventory.CAPACITY);
        FluidStack fluid = getFluidFromPotion(potion, bottleTypeFromItem, BottleFluidInventory.CAPACITY);
        fluid.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleTypeFromItem);
        return fluid;
    }

    public static FluidIngredient getFluidIngredientFromPotion(PotionContents potionContents, BottleType bottleType, int amount) {
        if (potionContents.is(Potions.WATER) && bottleType == BottleType.REGULAR)
            return new FluidStackIngredient(Fluids.WATER, DataComponentPatch.EMPTY, amount);
        return getFluidIngredient(amount, potionContents, bottleType);
    }

    public static FluidStack getFluidFromPotion(PotionContents potionContents, BottleType bottleType, int amount) {
        if (potionContents.is(Potions.WATER) && bottleType == BottleType.REGULAR)
            return new FluidStack(Fluids.WATER, amount);
        return getFluidStack(amount, potionContents, bottleType);
    }

    public static FluidIngredient getFluidIngredient(int amount, PotionContents potionContents, BottleType bottleType) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        if (potionContents != PotionContents.EMPTY) {
            builder.set(DataComponents.POTION_CONTENTS, potionContents);
        }
        builder.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleType);
        return new FluidStackIngredient(AllFluids.POTION, builder.build(), amount);
    }

    public static FluidStack getFluidStack(int amount, PotionContents potionContents, BottleType bottleType) {
        FluidStack fluidStack = new FluidStack(AllFluids.POTION, amount);
        addPotionToFluidStack(fluidStack, potionContents);
        fluidStack.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleType);
        return fluidStack;
    }

    public static void addPotionToFluidStack(FluidStack fs, PotionContents potionContents) {
        if (potionContents == PotionContents.EMPTY) {
            fs.remove(DataComponents.POTION_CONTENTS);
            return;
        }
        fs.set(DataComponents.POTION_CONTENTS, potionContents);
    }

    public static BottleType bottleTypeFromItem(Item item) {
        if (item == Items.LINGERING_POTION)
            return BottleType.LINGERING;
        if (item == Items.SPLASH_POTION)
            return BottleType.SPLASH;
        return BottleType.REGULAR;
    }

    public static Item itemFromBottleType(BottleType type) {
        return switch (type) {
            case LINGERING -> Items.LINGERING_POTION;
            case SPLASH -> Items.SPLASH_POTION;
            default -> Items.POTION;
        };
    }

    public static int getRequiredAmountForFilledBottle(ItemStack stack, FluidStack availableFluid) {
        return BottleFluidInventory.CAPACITY;
    }

    public static ItemStack fillBottle(ItemStack stack, FluidStack availableFluid) {
        ItemStack potionStack = new ItemStack(itemFromBottleType(availableFluid.getOrDefault(
            AllDataComponents.POTION_FLUID_BOTTLE_TYPE,
            BottleType.REGULAR
        )));
        potionStack.set(DataComponents.POTION_CONTENTS, availableFluid.get(DataComponents.POTION_CONTENTS));
        return potionStack;
    }

    //TODO
    //    @OnlyIn(Dist.CLIENT)
    //    public static void addPotionTooltip(FluidStack fs, Consumer<Component> tooltipAdder, float durationFactor) {
    //        PotionContents contents = fs.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    //        Iterable<MobEffectInstance> effects = contents.getAllEffects();
    //
    //        List<Pair<Holder<Attribute>, AttributeModifier>> list = Lists.newArrayList();
    //
    //        boolean flag = true;
    //        for (MobEffectInstance mobeffectinstance : effects) {
    //            flag = false;
    //            MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
    //            Holder<MobEffect> holder = mobeffectinstance.getEffect();
    //            holder.value().createModifiers(mobeffectinstance.getAmplifier(), (h, m) -> list.add(Pair.of(h, m)));
    //            if (mobeffectinstance.getAmplifier() > 0) {
    //                mutablecomponent.append(" ").append(Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()).getString());
    //            }
    //
    //            if (!mobeffectinstance.endsWithin(20)) {
    //                mutablecomponent.append(" (").append(MobEffectUtil.formatDuration(
    //                    mobeffectinstance,
    //                    durationFactor,
    //                    Minecraft.getInstance().level.tickRateManager().tickrate()
    //                )).append(")");
    //            }
    //
    //            tooltipAdder.accept(mutablecomponent.withStyle(holder.value().getCategory().getTooltipFormatting()));
    //        }
    //
    //        if (flag)
    //            tooltipAdder.accept(NO_EFFECT);
    //
    //        if (!list.isEmpty()) {
    //            tooltipAdder.accept(CommonComponents.EMPTY);
    //            tooltipAdder.accept((Component.translatable("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));
    //
    //            for (Pair<Holder<Attribute>, AttributeModifier> pair : list) {
    //                AttributeModifier attributemodifier = pair.getSecond();
    //                double d1 = attributemodifier.amount();
    //                double d0;
    //                if (attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
    //                    d0 = attributemodifier.amount();
    //                } else {
    //                    d0 = attributemodifier.amount() * 100.0D;
    //                }
    //
    //                if (d1 > 0.0D) {
    //                    tooltipAdder.accept((Component.translatable(
    //                        "attribute.modifier.plus." + attributemodifier.operation().id(),
    //                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d0),
    //                        Component.translatable(pair.getFirst().value().getDescriptionId())
    //                    )).withStyle(ChatFormatting.BLUE));
    //                } else if (d1 < 0.0D) {
    //                    d0 = d0 * -1.0D;
    //                    tooltipAdder.accept((Component.translatable(
    //                        "attribute.modifier.take." + attributemodifier.operation().id(),
    //                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d0),
    //                        Component.translatable(pair.getFirst().value().getDescriptionId())
    //                    )).withStyle(ChatFormatting.RED));
    //                }
    //            }
    //        }
    //    }
}
