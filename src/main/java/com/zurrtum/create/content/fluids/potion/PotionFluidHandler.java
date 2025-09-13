package com.zurrtum.create.content.fluids.potion;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.BottleFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PotionFluidHandler {
    private static final Text NO_EFFECT = Text.translatable("effect.none").formatted(Formatting.GRAY);

    public static boolean isPotionItem(ItemStack stack) {
        return stack.getItem() instanceof PotionItem && !(stack.getItem().getRecipeRemainder().getItem() instanceof BucketItem) && !stack.isIn(
            AllItemTags.NOT_POTION);
    }

    public static Pair<FluidStack, ItemStack> emptyPotion(ItemStack stack, boolean simulate) {
        FluidStack fluid = getFluidFromPotionItem(stack);
        if (!simulate)
            stack.decrement(1);
        return Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE));
    }

    //TODO
    //    public static FluidIngredient potionIngredient(Holder<Potion> potion, int amount) {
    //        return FluidIngredient.fromFluidStack(FluidHelper.copyStackWithAmount(PotionFluidHandler
    //            .getFluidFromPotionItem(PotionContents.createItemStack(Items.POTION, potion)), amount));
    //    }

    public static FluidStack getFluidFromPotionItem(ItemStack stack) {
        PotionContentsComponent potion = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        BottleType bottleTypeFromItem = bottleTypeFromItem(stack.getItem());
        if (potion.matches(Potions.WATER) && potion.customEffects().isEmpty() && bottleTypeFromItem == BottleType.REGULAR)
            return new FluidStack(Fluids.WATER, BottleFluidInventory.CAPACITY);
        FluidStack fluid = getFluidFromPotion(potion, bottleTypeFromItem, BottleFluidInventory.CAPACITY);
        fluid.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleTypeFromItem);
        return fluid;
    }

    public static FluidStack getFluidFromPotion(PotionContentsComponent potionContents, BottleType bottleType, int amount) {
        if (potionContents.matches(Potions.WATER) && bottleType == BottleType.REGULAR)
            return new FluidStack(Fluids.WATER, amount);
        return getFluidStack(amount, potionContents, bottleType);
    }

    public static FluidStack getFluidStack(int amount, PotionContentsComponent potionContents, BottleType bottleType) {
        FluidStack fluidStack = new FluidStack(AllFluids.POTION, amount);
        addPotionToFluidStack(fluidStack, potionContents);
        fluidStack.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleType);
        return fluidStack;
    }

    public static void addPotionToFluidStack(FluidStack fs, PotionContentsComponent potionContents) {
        if (potionContents == PotionContentsComponent.DEFAULT) {
            fs.remove(DataComponentTypes.POTION_CONTENTS);
            return;
        }
        fs.set(DataComponentTypes.POTION_CONTENTS, potionContents);
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
        potionStack.set(DataComponentTypes.POTION_CONTENTS, availableFluid.get(DataComponentTypes.POTION_CONTENTS));
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
