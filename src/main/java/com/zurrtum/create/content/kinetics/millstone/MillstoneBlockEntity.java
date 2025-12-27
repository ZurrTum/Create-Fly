package com.zurrtum.create.content.kinetics.millstone;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MillstoneBlockEntity extends KineticBlockEntity implements Clearable {
    public MillstoneInventoryHandler capability;
    public int timer;
    private MillingRecipe lastRecipe;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MILLSTONE, pos, state);
        capability = new MillstoneInventoryHandler();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this));
        super.addBehaviours(behaviours);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.MILLSTONE);
    }

    @Override
    public void tick() {
        super.tick();

        if (getSpeed() == 0)
            return;
        for (int i = 1, size = capability.getContainerSize(); i < size; i++) {
            ItemStack stack = capability.getItem(i);
            if (stack.getCount() == capability.getMaxStackSize(stack)) {
                return;
            }
        }

        if (timer > 0) {
            timer -= getProcessingSpeed();

            if (level.isClientSide()) {
                spawnParticles();
                return;
            }
            if (timer <= 0)
                process();
            return;
        } else if (level.isClientSide()) {
            return;
        }

        ItemStack stack = capability.getItem(0);
        if (stack.isEmpty())
            return;

        SingleRecipeInput input = new SingleRecipeInput(stack);
        if (lastRecipe == null || !lastRecipe.matches(input, level)) {
            Optional<RecipeHolder<MillingRecipe>> recipe = ((ServerLevel) level).recipeAccess().getRecipeFor(AllRecipeTypes.MILLING, input, level);
            if (recipe.isEmpty()) {
                timer = 100;
                sendData();
            } else {
                lastRecipe = recipe.get().value();
                timer = lastRecipe.time();
                sendData();
            }
            return;
        }

        timer = lastRecipe.time();
        sendData();
    }

    @Override
    public void clearContent() {
        capability.clearContent();
    }

    @Override
    public void destroy() {
        super.destroy();
        Containers.dropContents(level, worldPosition, capability);
    }

    private void process() {
        ItemStack stack = capability.getItem(0);
        SingleRecipeInput input = new SingleRecipeInput(stack);

        if (lastRecipe == null || !lastRecipe.matches(input, level)) {
            Optional<RecipeHolder<MillingRecipe>> recipe = ((ServerLevel) level).recipeAccess().getRecipeFor(AllRecipeTypes.MILLING, input, level);
            if (recipe.isEmpty())
                return;
            lastRecipe = recipe.get().value();
        }

        ItemStack recipeRemainder = stack.getItem().getCraftingRemainder();
        stack.shrink(1);
        capability.setItem(0, stack);
        capability.outputAllowInsertion();
        List<ItemStack> list = lastRecipe.assemble(input, level.random);
        if (!recipeRemainder.isEmpty()) {
            list.add(recipeRemainder);
        }
        capability.insert(list);
        capability.outputForbidInsertion();

        award(AllAdvancements.MILLSTONE);

        sendData();
        setChanged();
    }

    public void spawnParticles() {
        ItemStack stackInSlot = capability.getItem(0);
        if (stackInSlot.isEmpty())
            return;

        ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, stackInSlot);
        float angle = level.random.nextFloat() * 360;
        Vec3 offset = new Vec3(0, 0, 0.5f);
        offset = VecHelper.rotate(offset, angle, Axis.Y);
        Vec3 target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y);

        Vec3 center = offset.add(VecHelper.getCenterOf(worldPosition));
        target = VecHelper.offsetRandomly(target.subtract(offset), level.random, 1 / 128f);
        level.addParticle(data, center.x, center.y, center.z, target.x, target.y, target.z);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("Timer", timer);
        capability.write(view);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        timer = view.getIntOr("Timer", 0);
        capability.read(view);
        super.read(view, clientPacket);
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    private boolean canProcess(ItemStack stack) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        if (lastRecipe != null && lastRecipe.matches(input, level))
            return true;
        Optional<RecipeHolder<MillingRecipe>> recipe = ((ServerLevel) level).recipeAccess().getRecipeFor(AllRecipeTypes.MILLING, input, level);
        if (recipe.isEmpty()) {
            return false;
        }
        lastRecipe = recipe.get().value();
        return true;
    }

    public class MillstoneInventoryHandler implements SidedItemInventory {
        private static final int[] SLOTS = SlotRangeCache.get(10);
        private final NonNullList<ItemStack> stacks = NonNullList.withSize(10, ItemStack.EMPTY);
        private boolean check = true;

        public void outputAllowInsertion() {
            check = false;
        }

        public void outputForbidInsertion() {
            check = true;
        }

        @Override
        public int getContainerSize() {
            return 10;
        }

        @Override
        public int[] getSlotsForFace(Direction side) {
            return SLOTS;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return !check || canProcess(stack);
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return check ? slot == 0 : slot > 0;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return slot != 0;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot >= 10) {
                return ItemStack.EMPTY;
            }
            return stacks.get(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 10) {
                return;
            }
            stacks.set(slot, stack);
        }

        @Override
        public void setChanged() {
            MillstoneBlockEntity.this.setChanged();
        }

        public void write(ValueOutput view) {
            ValueOutput.TypedOutputList<ItemStack> list = view.list("Inventory", ItemStack.OPTIONAL_CODEC);
            list.add(stacks.getFirst());
            for (int i = 1; i < 10; i++) {
                ItemStack stack = stacks.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                list.add(stack);
            }
        }

        public void read(ValueInput view) {
            List<ItemStack> list = view.listOrEmpty("Inventory", ItemStack.OPTIONAL_CODEC).stream().toList();
            int i = 0;
            for (ItemStack itemStack : list) {
                stacks.set(i++, itemStack);
            }
            for (; i < 10; i++) {
                setItem(i, ItemStack.EMPTY);
            }
        }
    }

}
