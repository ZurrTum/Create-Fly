package com.zurrtum.create.infrastructure.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class FluidItemContext extends SingleStackStorage implements ContainerItemContext, AutoCloseable {
    private static final Deque<FluidItemContext> POOL = new ArrayDeque<>();
    private ItemStack stack;

    public static FluidItemContext of(ItemStack stack) {
        FluidItemContext ctx = POOL.pollFirst();
        if (ctx == null) {
            ctx = new FluidItemContext();
        }
        ctx.stack = stack;
        return ctx;
    }

    @Override
    public void close() {
        stack = ItemStack.EMPTY;
        POOL.addLast(this);
    }

    @Override
    protected ItemStack getStack() {
        return stack;
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return this;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return 0;
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Collections.emptyList();
    }
}
