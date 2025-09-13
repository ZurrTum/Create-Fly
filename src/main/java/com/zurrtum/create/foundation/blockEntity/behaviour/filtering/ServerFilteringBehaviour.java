package com.zurrtum.create.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettingsHandleBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ServerFilteringBehaviour extends BlockEntityBehaviour<SmartBlockEntity> implements ValueSettingsHandleBehaviour {
    public static final BehaviourType<ServerFilteringBehaviour> TYPE = new BehaviourType<>();

    protected FilterItemStack filter;
    boolean showCount = false;
    public int count = 64;
    public boolean upTo = true;
    private Predicate<ItemStack> predicate = stack -> true;
    private Consumer<ItemStack> callback = stack -> {
    };
    private Supplier<Boolean> showCountPredicate = () -> showCount;
    private Supplier<Boolean> isActive = () -> true;
    boolean recipeFilter = false;
    public boolean fluidFilter = false;

    public ServerFilteringBehaviour(SmartBlockEntity be) {
        super(be);
        filter = FilterItemStack.empty();
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.put("Filter", FilterItemStack.CODEC, filter);
        view.putInt("FilterAmount", count);
        view.putBoolean("UpTo", upTo);
        super.write(view, clientPacket);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        filter = view.read("Filter", FilterItemStack.CODEC).orElseGet(FilterItemStack::empty);
        count = view.getInt("FilterAmount", 0);
        upTo = view.getBoolean("UpTo", false);

        // Migrate from previous behaviour
        if (count == 0) {
            upTo = true;
            count = getMaxStackSize();
        }

        super.read(view, clientPacket);
    }

    public ServerFilteringBehaviour withCallback(Consumer<ItemStack> filterCallback) {
        callback = filterCallback;
        return this;
    }

    public ServerFilteringBehaviour withPredicate(Predicate<ItemStack> filterPredicate) {
        predicate = filterPredicate;
        return this;
    }

    public ServerFilteringBehaviour forRecipes() {
        recipeFilter = true;
        return this;
    }

    public ServerFilteringBehaviour forFluids() {
        fluidFilter = true;
        return this;
    }

    public ServerFilteringBehaviour showCountWhen(Supplier<Boolean> condition) {
        showCountPredicate = condition;
        return this;
    }

    public ServerFilteringBehaviour showCount() {
        showCount = true;
        return this;
    }

    public boolean setFilter(Direction face, ItemStack stack) {
        return setFilter(stack);
    }

    public boolean setFilter(ItemStack stack) {
        ItemStack filter = stack.copy();
        if (!filter.isEmpty() && !predicate.test(filter))
            return false;
        this.filter = FilterItemStack.of(filter);
        if (!upTo)
            count = Math.min(count, stack.getMaxCount());
        callback.accept(filter);
        blockEntity.markDirty();
        blockEntity.sendData();
        return true;
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings))
            return;
        count = MathHelper.clamp(settings.value(), 1, getMaxStackSize());
        upTo = settings.row() == 0;
        blockEntity.markDirty();
        blockEntity.sendData();
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(upTo ? 0 : 1, count == 0 ? getMaxStackSize() : count);
    }

    @Override
    public void destroy() {
        if (filter.isFilterItem()) {
            Vec3d pos = VecHelper.getCenterOf(getPos());
            World world = getWorld();
            world.spawnEntity(new ItemEntity(world, pos.x, pos.y, pos.z, getFilter().copy()));
        }
        super.destroy();
    }

    @Override
    public ItemRequirement getRequiredItems() {
        if (filter.isFilterItem())
            return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, getFilter());

        return ItemRequirement.NONE;
    }

    public int getMaxStackSize() {
        return getMaxStackSize(getFilter());
    }

    public int getMaxStackSize(Direction face) {
        return getMaxStackSize(getFilter(face));
    }

    public int getMaxStackSize(ItemStack filter) {
        if (filter.isEmpty())
            return 64;
        return filter.getMaxCount();
    }

    public ItemStack getFilter(Direction side) {
        return getFilter();
    }

    public ItemStack getFilter() {
        return filter.item();
    }

    public boolean isCountVisible() {
        return showCountPredicate.get() && getMaxStackSize() > 1;
    }

    public boolean test(ItemStack stack) {
        return !isActive() || filter.test(blockEntity.getWorld(), stack);
    }

    public boolean test(FluidStack stack) {
        return !isActive() || filter.test(blockEntity.getWorld(), stack);
    }

    public boolean isActive() {
        return isActive.get();
    }

    public ServerFilteringBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
        isActive = condition;
        return this;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public int getAmount() {
        return count;
    }

    public boolean anyAmount() {
        return count == 0;
    }

    @Override
    public String getClipboardKey() {
        return "Filtering";
    }

    @Override
    public boolean writeToClipboard(WriteView view, Direction side) {
        ValueSettingsHandleBehaviour.super.writeToClipboard(view, side);
        ItemStack filter = getFilter(side);
        view.put("Filter", ItemStack.OPTIONAL_CODEC, filter);
        return true;
    }

    @Override
    public boolean readFromClipboard(ReadView view, PlayerEntity player, Direction side, boolean simulate) {
        if (!mayInteract(player))
            return false;
        boolean upstreamResult = ValueSettingsHandleBehaviour.super.readFromClipboard(view, player, side, simulate);
        Optional<ItemStack> filterItem = view.read("Filter", ItemStack.OPTIONAL_CODEC);
        if (filterItem.isEmpty())
            return upstreamResult;
        if (simulate)
            return true;
        if (getWorld().isClient)
            return true;

        ItemStack refund = ItemStack.EMPTY;
        ItemStack filter = getFilter(side);
        if (filter.getItem() instanceof FilterItem && !player.isCreative())
            refund = filter.copy();

        ItemStack copied = filterItem.get();

        PlayerInventory inventory = player.getInventory();
        if (copied.getItem() instanceof FilterItem filterType && !player.isCreative()) {
            if (refund.getItem() == filterType) {
                setFilter(side, copied);
                return true;
            } else if (inventory.extract(filterType.getDefaultStack()) == 1 || !inventory.extract(stack -> stack.getItem() == filterType, 1)
                .isEmpty()) {
                if (!refund.isEmpty())
                    inventory.offerOrDrop(refund);
                setFilter(side, copied);
                return true;
            }

            player.sendMessage(
                Text.translatable("create.logistics.filter.requires_item_in_inventory", copied.getName().copy().formatted(Formatting.WHITE))
                    .formatted(Formatting.RED), true
            );
            AllSoundEvents.DENY.playOnServer(player.getWorld(), player.getBlockPos(), 1, 1);
            return false;
        }

        if (!refund.isEmpty())
            inventory.offerOrDrop(refund);

        return setFilter(side, copied);
    }

    @Override
    public void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
        World level = getWorld();
        BlockPos pos = getPos();
        ItemStack itemInHand = player.getStackInHand(hand);
        ItemStack toApply = itemInHand.copy();

        if (!canShortInteract(toApply))
            return;
        if (level.isClient())
            return;

        ItemStack filter = getFilter(side);
        if (filter.getItem() instanceof FilterItem) {
            PlayerInventory inventory = player.getInventory();
            if (!player.isCreative() || inventory.count(filter, 1) == 0)
                inventory.offerOrDrop(filter.copy());
        }

        if (toApply.getItem() instanceof FilterItem)
            toApply.setCount(1);

        if (!setFilter(side, toApply)) {
            player.sendMessage(Text.translatable("create.logistics.filter.invalid_item"), true);
            AllSoundEvents.DENY.playOnServer(player.getWorld(), player.getBlockPos(), 1, 1);
            return;
        }

        if (!player.isCreative()) {
            if (toApply.getItem() instanceof FilterItem) {
                if (itemInHand.getCount() == 1)
                    player.setStackInHand(hand, ItemStack.EMPTY);
                else
                    itemInHand.decrement(1);
            }
        }

        level.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .1f);
    }

    public boolean canShortInteract(ItemStack toApply) {
        if (toApply.isOf(AllItems.WRENCH))
            return false;
        return !toApply.isOf(AllItems.MECHANICAL_ARM);
    }

    public boolean isRecipeFilter() {
        return recipeFilter;
    }

    @Override
    public int netId() {
        return 1;
    }
}
