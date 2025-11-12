package com.zurrtum.create.foundation.blockEntity.behaviour.inventory;

import com.google.common.base.Predicates;
import com.zurrtum.create.api.packager.InventoryIdentifier;
import com.zurrtum.create.content.logistics.packager.IdentifiedInventory;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.item.ItemHelper.ExtractionCountMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class InvManipulationBehaviour extends CapManipulationBehaviourBase<Container, InvManipulationBehaviour> {

    // Extra types available for multibehaviour
    public static final BehaviourType<InvManipulationBehaviour>

        TYPE = new BehaviourType<>(), EXTRACT = new BehaviourType<>(), INSERT = new BehaviourType<>();

    private final BehaviourType<InvManipulationBehaviour> behaviourType;

    public static InvManipulationBehaviour forExtraction(SmartBlockEntity be, InterfaceProvider target) {
        return new InvManipulationBehaviour(EXTRACT, be, target);
    }

    public static InvManipulationBehaviour forInsertion(SmartBlockEntity be, InterfaceProvider target) {
        return new InvManipulationBehaviour(INSERT, be, target);
    }

    public InvManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target) {
        this(TYPE, be, target);
    }

    private InvManipulationBehaviour(BehaviourType<InvManipulationBehaviour> type, SmartBlockEntity be, InterfaceProvider target) {
        super(be, target);
        behaviourType = type;
    }

    @Nullable
    public IdentifiedInventory getIdentifiedInventory() {
        Container inventory = this.getInventory();
        if (inventory == null)
            return null;

        InventoryIdentifier identifier = InventoryIdentifier.get(this.getLevel(), this.getTarget().getOpposite());
        return new IdentifiedInventory(identifier, inventory);
    }

    @Override
    protected Container getCapability(Level world, BlockPos pos, BlockEntity blockEntity, @Nullable Direction side) {
        return ItemHelper.getInventory(world, pos, null, blockEntity, side);
    }

    public ItemStack extract() {
        return extract(getModeFromFilter(), getAmountFromFilter());
    }

    public ItemStack extract(ExtractionCountMode mode, int amount) {
        return extract(mode, amount, Predicates.alwaysTrue());
    }

    public ItemStack extract(ExtractionCountMode mode, int amount, Predicate<ItemStack> filter) {
        boolean shouldSimulate = simulateNext;
        simulateNext = false;

        if (getLevel().isClientSide())
            return ItemStack.EMPTY;
        Container inventory = targetCapability;
        if (inventory == null)
            return ItemStack.EMPTY;

        Predicate<ItemStack> test = getFilterTest(filter);
        if (shouldSimulate) {
            ItemStack extract = inventory.count(test, amount);
            int count = extract.getCount();
            if (mode == ExtractionCountMode.EXACTLY && count != amount) {
                return ItemStack.EMPTY;
            }
            int maxCount = extract.getMaxStackSize();
            if (count > maxCount) {
                extract.setCount(maxCount);
            }
            return extract;
        } else if (mode == ExtractionCountMode.UPTO) {
            ItemStack extract = inventory.count(test, amount);
            int count = inventory.extract(extract, Math.min(extract.getCount(), extract.getMaxStackSize()));
            extract.setCount(count);
            return extract;
        } else {
            return inventory.preciseExtract(test, amount);
        }
    }

    public ItemStack insert(ItemStack stack) {
        boolean shouldSimulate = simulateNext;
        simulateNext = false;
        Container inventory = targetCapability;
        if (inventory == null)
            return stack;
        int insert;
        if (shouldSimulate) {
            insert = inventory.countSpace(stack);
        } else {
            insert = inventory.insertExist(stack);
        }
        int count = stack.getCount();
        if (insert == count) {
            return ItemStack.EMPTY;
        } else if (insert == 0) {
            return stack;
        } else {
            return stack.copyWithCount(count - insert);
        }
    }

    protected Predicate<ItemStack> getFilterTest(Predicate<ItemStack> customFilter) {
        Predicate<ItemStack> test = customFilter;
        ServerFilteringBehaviour filter = blockEntity.getBehaviour(ServerFilteringBehaviour.TYPE);
        if (filter != null)
            test = customFilter.and(filter::test);
        return test;
    }

    @Override
    public BehaviourType<?> getType() {
        return behaviourType;
    }

}