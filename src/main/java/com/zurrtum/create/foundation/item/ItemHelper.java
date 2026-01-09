package com.zurrtum.create.foundation.item;

import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemHelper {
    private static final Map<BlockPos, InventoryCache> INV_CACHE = new Object2ReferenceOpenHashMap<>();

    public static boolean sameItem(ItemStack stack, ItemStack otherStack) {
        return !otherStack.isEmpty() && stack.is(otherStack.getItem());
    }

    public static Predicate<ItemStack> sameItemPredicate(ItemStack stack) {
        return s -> sameItem(stack, s);
    }

    public static List<ItemStack> multipliedOutput(ItemStack out, int count) {
        if (out.isEmpty()) {
            return new ArrayList<>(0);
        }
        int total = count * out.getCount();
        int max = out.getMaxStackSize();
        int size = total / max;
        int remaining = total % max;
        boolean hasRemaining = remaining != 0;
        List<ItemStack> stacks = new ArrayList<>(hasRemaining ? size + 1 : size);
        stacks.add(out);
        if (size != 0) {
            out.setCount(max);
            for (int i = 1; i < size; i++) {
                stacks.add(out.copy());
            }
            if (hasRemaining) {
                stacks.add(out.copyWithCount(remaining));
            }
        } else {
            out.setCount(total);
        }
        return stacks;
    }

    public static List<ItemStack> multipliedOutput(List<ItemStack> out, int count) {
        if (out.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : out) {
            int total = count * stack.getCount();
            int max = stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
            int size = total / max;
            stacks.add(stack);
            if (size != 0) {
                stack.setCount(max);
                for (int i = 1; i < size; i++) {
                    stacks.add(stack.copy());
                }
                int remaining = total % max;
                if (remaining != 0) {
                    stacks.add(stack.copyWithCount(remaining));
                }
            } else {
                stack.setCount(total);
            }
        }
        return stacks;
    }

    public static void addToList(ItemStack stack, List<ItemStack> stacks) {
        for (ItemStack s : stacks) {
            if (!ItemStack.isSameItemSameComponents(stack, s))
                continue;
            int transferred = Math.min(s.getMaxStackSize() - s.getCount(), stack.getCount());
            s.grow(transferred);
            stack.shrink(transferred);
        }
        if (stack.getCount() > 0)
            stacks.add(stack);
    }

    public static <T extends IBE<? extends BlockEntity>> int calcRedstoneFromBlockEntity(T ibe, Level level, BlockPos pos) {
        return ibe.getBlockEntityOptional(level, pos).map(be -> getInventory(level, pos, null)).map(ItemHelper::calcRedstoneFromInventory).orElse(0);
    }

    public static int calcRedstoneFromInventory(@Nullable Container inv) {
        if (inv == null)
            return 0;
        int i = 0;
        float f = 0.0F;
        int totalSlots = inv.getContainerSize();

        for (int j = 0, size = totalSlots; j < size; ++j) {
            int slotLimit = inv.getMaxStackSize();
            if (slotLimit == 0) {
                totalSlots--;
                continue;
            }
            ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                f += (float) itemstack.getCount() / (float) Math.min(slotLimit, itemstack.getMaxStackSize());
                ++i;
            }
        }

        if (totalSlots == 0)
            return 0;

        f = f / totalSlots;
        return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
    }

    public static boolean matchIngredients(Ingredient i1, Ingredient i2) {
        if (i1 == i2)
            return true;
        HolderSet<Item> entries1 = i1.values;
        HolderSet<Item> entries2 = i2.values;
        Optional<TagKey<Item>> tag1 = entries1.unwrapKey();
        Optional<TagKey<Item>> tag2 = entries2.unwrapKey();
        if (tag1.isPresent()) {
            return tag2.map(tag -> tag.equals(tag1.get())).orElse(false);
        }
        if (tag2.isPresent()) {
            return false;
        }
        int size = entries1.size();
        if (size == entries2.size()) {
            for (int i = 0; i < size; i++)
                if (!entries1.contains(entries2.get(i)))
                    return false;
            return true;
        }
        return false;
    }

    public static boolean matchAllIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() <= 1)
            return true;
        Ingredient firstIngredient = ingredients.getFirst();
        for (int i = 1; i < ingredients.size(); i++)
            if (!matchIngredients(firstIngredient, ingredients.get(i)))
                return false;
        return true;
    }

    public static enum ExtractionCountMode {
        EXACTLY,
        UPTO
    }

    public static ItemStack extractItem(Container inventory, int slot, int amount, boolean simulate) {
        ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty() || (inventory instanceof WorldlyContainer sidedInventory && !sidedInventory.canTakeItemThroughFace(slot, stack, null))) {
            return ItemStack.EMPTY;
        }
        int extract = Math.min(amount, stack.getCount());
        if (simulate) {
            return stack.copyWithCount(extract);
        } else if (extract == amount) {
            inventory.setItem(slot, ItemStack.EMPTY);
            inventory.setChanged();
            return stack;
        } else {
            ItemStack result = stack.copy();
            result.setCount(extract);
            stack.shrink(extract);
            inventory.setChanged();
            return result;
        }
    }

    public static boolean canItemStackAmountsStack(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b) && a.getCount() + b.getCount() <= a.getMaxStackSize();
    }

    public static ItemStack fromItemEntity(Entity entityIn) {
        if (!entityIn.isAlive())
            return ItemStack.EMPTY;
        if (entityIn instanceof PackageEntity packageEntity) {
            return packageEntity.getBox();
        }
        return entityIn instanceof ItemEntity itemEntity ? itemEntity.getItem() : ItemStack.EMPTY;
    }

    public static void fillItemStackHandler(ItemContainerContents contents, ItemStackHandler inv) {
        List<ItemStack> itemStacks = contents.stream().toList();

        for (int i = 0; i < itemStacks.size(); i++) {
            inv.setItem(i, itemStacks.get(i));
        }
    }

    public static ItemContainerContents containerContentsFromHandler(ItemStackHandler handler) {
        return ItemContainerContents.fromItems(handler.getStacks());
    }

    public static ItemStack limitCountToMaxStackSize(ItemStack stack, boolean simulate) {
        int count = stack.getCount();
        int max = stack.getMaxStackSize();
        if (count <= max)
            return ItemStack.EMPTY;
        ItemStack remainder = stack.copyWithCount(count - max);
        if (!simulate)
            stack.setCount(max);
        return remainder;
    }

    public static void copyContents(Container from, Container to) {
        if (from.getContainerSize() != to.getContainerSize()) {
            throw new IllegalArgumentException("Slot count mismatch");
        }

        for (int slot = to.getContainerSize() - 1; slot >= 0; slot--) {
            to.setItem(slot, ItemStack.EMPTY);
        }

        for (int i = 0; i < from.getContainerSize(); i++) {
            to.setItem(i, from.getItem(i).copy());
        }
    }

    public static List<ItemStack> getNonEmptyStacks(ItemStackHandler handler) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0, size = handler.getContainerSize(); i < size; i++) {
            ItemStack stack = handler.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static Container getInventory(Level world, BlockPos pos, Direction direction) {
        return getInventory(world, pos, null, null, direction);
    }

    public static Container getInventory(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, Direction direction) {
        if (state == null) {
            state = blockEntity != null ? blockEntity.getBlockState() : world.getBlockState(pos);
        }
        Block block = state.getBlock();
        if (block instanceof ItemInventoryProvider<?> provider) {
            return provider.getInventory(state, world, pos, blockEntity, direction);
        }
        if (block instanceof WorldlyContainerHolder provider) {
            return provider.getContainer(state, world, pos);
        }
        if (blockEntity == null && state.hasBlockEntity()) {
            blockEntity = world.getBlockEntity(pos);
        }
        if (blockEntity instanceof Container inventory) {
            if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock chestBlock) {
                inventory = ChestBlock.getContainer(chestBlock, state, world, pos, true);
            }
            return inventory;
        }
        return AllTransfer.getInventory(world, pos, state, blockEntity, direction);
    }

    public static Supplier<Container> getInventoryCache(
        ServerLevel world,
        BlockPos pos,
        Direction direction,
        BiPredicate<BlockEntity, Direction> filter
    ) {
        InventoryCache cache = new InventoryCache(world, pos, direction, filter);
        INV_CACHE.put(pos, cache);
        return cache;
    }

    public static void invalidateInventoryCache(BlockPos pos) {
        InventoryCache cache = ItemHelper.INV_CACHE.get(pos);
        if (cache != null) {
            cache.invalidate();
        }
    }
}