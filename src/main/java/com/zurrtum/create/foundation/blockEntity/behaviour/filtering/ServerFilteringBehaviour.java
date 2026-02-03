package com.zurrtum.create.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettingsHandleBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
    public void write(ValueOutput view, boolean clientPacket) {
        view.store("Filter", FilterItemStack.CODEC, filter);
        view.putInt("FilterAmount", count);
        view.putBoolean("UpTo", upTo);
        super.write(view, clientPacket);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        filter = view.read("Filter", FilterItemStack.CODEC).orElseGet(FilterItemStack::empty);
        count = view.getIntOr("FilterAmount", 0);
        upTo = view.getBooleanOr("UpTo", false);

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
        if (!upTo && !stack.isEmpty())
            count = Math.min(count, stack.getMaxStackSize());
        callback.accept(filter);
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings))
            return;
        count = Mth.clamp(settings.value(), 1, getMaxStackSize());
        upTo = settings.row() == 0;
        blockEntity.setChanged();
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
            Vec3 pos = VecHelper.getCenterOf(getPos());
            Level world = getLevel();
            world.addFreshEntity(new ItemEntity(world, pos.x, pos.y, pos.z, getFilter().copy()));
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
        return filter.getMaxStackSize();
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
        return !isActive() || filter.test(blockEntity.getLevel(), stack);
    }

    public boolean test(FluidStack stack) {
        return !isActive() || filter.test(blockEntity.getLevel(), stack);
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
    public boolean writeToClipboard(ValueOutput view, Direction side) {
        ValueSettingsHandleBehaviour.super.writeToClipboard(view, side);
        ItemStack filter = getFilter(side);
        view.store("Filter", ItemStack.OPTIONAL_CODEC, filter);
        return true;
    }

    @Override
    public boolean readFromClipboard(ValueInput view, Player player, Direction side, boolean simulate) {
        if (!mayInteract(player))
            return false;
        boolean upstreamResult = ValueSettingsHandleBehaviour.super.readFromClipboard(view, player, side, simulate);
        Optional<ItemStack> filterItem = view.read("Filter", ItemStack.OPTIONAL_CODEC);
        if (filterItem.isEmpty())
            return upstreamResult;
        if (simulate)
            return true;
        if (getLevel().isClientSide())
            return true;

        ItemStack refund = ItemStack.EMPTY;
        ItemStack filter = getFilter(side);
        if (filter.getItem() instanceof FilterItem && !player.isCreative())
            refund = filter.copy();

        ItemStack copied = filterItem.get();

        Inventory inventory = player.getInventory();
        if (copied.getItem() instanceof FilterItem filterType && !player.isCreative()) {
            if (refund.getItem() == filterType) {
                setFilter(side, copied);
                return true;
            } else if (inventory.extract(filterType.getDefaultInstance()) == 1 || !inventory.extract(stack -> stack.getItem() == filterType, 1)
                .isEmpty()) {
                if (!refund.isEmpty())
                    inventory.placeItemBackInInventory(refund);
                setFilter(side, copied);
                return true;
            }

            player.displayClientMessage(
                Component.translatable(
                    "create.logistics.filter.requires_item_in_inventory",
                    copied.getHoverName().copy().withStyle(ChatFormatting.WHITE)
                ).withStyle(ChatFormatting.RED), true
            );
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return false;
        }

        if (!refund.isEmpty())
            inventory.placeItemBackInInventory(refund);

        return setFilter(side, copied);
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        Level level = getLevel();
        BlockPos pos = getPos();
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack toApply = itemInHand.copy();

        if (!canShortInteract(toApply))
            return;
        if (level.isClientSide())
            return;

        ItemStack filter = getFilter(side);
        if (filter.getItem() instanceof FilterItem) {
            Inventory inventory = player.getInventory();
            if (!player.isCreative() || inventory.count(filter, 1) == 0)
                inventory.placeItemBackInInventory(filter.copy());
        }

        if (toApply.getItem() instanceof FilterItem)
            toApply.setCount(1);

        if (!setFilter(side, toApply)) {
            player.displayClientMessage(Component.translatable("create.logistics.filter.invalid_item"), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }

        if (!player.isCreative()) {
            if (toApply.getItem() instanceof FilterItem) {
                if (itemInHand.getCount() == 1)
                    player.setItemInHand(hand, ItemStack.EMPTY);
                else
                    itemInHand.shrink(1);
            }
        }

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    public boolean canShortInteract(ItemStack toApply) {
        if (toApply.is(AllItems.WRENCH))
            return false;
        return !toApply.is(AllItems.MECHANICAL_ARM);
    }

    public boolean isRecipeFilter() {
        return recipeFilter;
    }

    @Override
    public int netId() {
        return 1;
    }

    public static class CustomInteract extends ServerFilteringBehaviour {
        private final @Nullable List<Item> blackList;

        public CustomInteract(SmartBlockEntity be, List<Item> blackList) {
            super(be);
            this.blackList = blackList;
        }

        public CustomInteract(SmartBlockEntity be) {
            this(be, null);
        }

        @Override
        public boolean canShortInteract(ItemStack toApply) {
            if (blackList != null) {
                for (Item item : blackList) {
                    if (toApply.is(item)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
