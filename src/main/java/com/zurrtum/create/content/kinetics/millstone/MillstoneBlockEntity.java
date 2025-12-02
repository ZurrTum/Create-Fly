package com.zurrtum.create.content.kinetics.millstone;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MillstoneBlockEntity extends KineticBlockEntity {
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
        for (int i = 1, size = capability.size(); i < size; i++) {
            ItemStack stack = capability.getStack(i);
            if (stack.getCount() == capability.getMaxCount(stack)) {
                return;
            }
        }

        if (timer > 0) {
            timer -= getProcessingSpeed();

            if (world.isClient) {
                spawnParticles();
                return;
            }
            if (timer <= 0)
                process();
            return;
        } else if (world.isClient) {
            return;
        }

        ItemStack stack = capability.getStack(0);
        if (stack.isEmpty())
            return;

        SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
        if (lastRecipe == null || !lastRecipe.matches(input, world)) {
            Optional<RecipeEntry<MillingRecipe>> recipe = ((ServerWorld) world).getRecipeManager()
                .getFirstMatch(AllRecipeTypes.MILLING, input, world);
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
    public void destroy() {
        super.destroy();
        ItemScatterer.spawn(world, pos, capability);
    }

    private void process() {
        ItemStack stack = capability.getStack(0);
        SingleStackRecipeInput input = new SingleStackRecipeInput(stack);

        if (lastRecipe == null || !lastRecipe.matches(input, world)) {
            Optional<RecipeEntry<MillingRecipe>> recipe = ((ServerWorld) world).getRecipeManager()
                .getFirstMatch(AllRecipeTypes.MILLING, input, world);
            if (recipe.isEmpty())
                return;
            lastRecipe = recipe.get().value();
        }

        ItemStack recipeRemainder = stack.getItem().getRecipeRemainder();
        stack.decrement(1);
        capability.setStack(0, stack);
        capability.outputAllowInsertion();
        List<ItemStack> list = lastRecipe.craft(input, world.random);
        if (!recipeRemainder.isEmpty()) {
            list.add(recipeRemainder);
        }
        capability.insert(list);
        capability.outputForbidInsertion();

        award(AllAdvancements.MILLSTONE);

        sendData();
        markDirty();
    }

    public void spawnParticles() {
        ItemStack stackInSlot = capability.getStack(0);
        if (stackInSlot.isEmpty())
            return;

        ItemStackParticleEffect data = new ItemStackParticleEffect(ParticleTypes.ITEM, stackInSlot);
        float angle = world.random.nextFloat() * 360;
        Vec3d offset = new Vec3d(0, 0, 0.5f);
        offset = VecHelper.rotate(offset, angle, Axis.Y);
        Vec3d target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y);

        Vec3d center = offset.add(VecHelper.getCenterOf(pos));
        target = VecHelper.offsetRandomly(target.subtract(offset), world.random, 1 / 128f);
        world.addParticleClient(data, center.x, center.y, center.z, target.x, target.y, target.z);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("Timer", timer);
        capability.write(view);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        timer = view.getInt("Timer", 0);
        capability.read(view);
        super.read(view, clientPacket);
    }

    public int getProcessingSpeed() {
        return MathHelper.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    private boolean canProcess(ItemStack stack) {
        SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
        if (lastRecipe != null && lastRecipe.matches(input, world))
            return true;
        Optional<RecipeEntry<MillingRecipe>> recipe = ((ServerWorld) world).getRecipeManager().getFirstMatch(AllRecipeTypes.MILLING, input, world);
        if (recipe.isEmpty()) {
            return false;
        }
        lastRecipe = recipe.get().value();
        return true;
    }

    public class MillstoneInventoryHandler implements SidedItemInventory {
        private static final int[] SLOTS = SlotRangeCache.get(10);
        private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(10, ItemStack.EMPTY);
        private boolean check = true;

        public void outputAllowInsertion() {
            check = false;
        }

        public void outputForbidInsertion() {
            check = true;
        }

        @Override
        public int size() {
            return 10;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return SLOTS;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return !check || canProcess(stack);
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return check ? slot == 0 : slot > 0;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return slot != 0;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot >= 10) {
                return ItemStack.EMPTY;
            }
            return stacks.get(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= 10) {
                return;
            }
            stacks.set(slot, stack);
        }

        @Override
        public void markDirty() {
            MillstoneBlockEntity.this.markDirty();
        }

        public void write(WriteView view) {
            WriteView.ListAppender<ItemStack> list = view.getListAppender("Inventory", ItemStack.OPTIONAL_CODEC);
            list.add(stacks.getFirst());
            for (int i = 1; i < 10; i++) {
                ItemStack stack = stacks.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                list.add(stack);
            }
        }

        public void read(ReadView view) {
            List<ItemStack> list = view.getTypedListView("Inventory", ItemStack.OPTIONAL_CODEC).stream().toList();
            int i = 0;
            for (ItemStack itemStack : list) {
                stacks.set(i++, itemStack);
            }
            for (; i < 10; i++) {
                setStack(i, ItemStack.EMPTY);
            }
        }
    }

}
