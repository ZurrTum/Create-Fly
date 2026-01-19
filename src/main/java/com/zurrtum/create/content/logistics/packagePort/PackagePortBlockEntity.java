package com.zurrtum.create.content.logistics.packagePort;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PackagePortBlockEntity extends SmartBlockEntity implements MenuProvider, Clearable {

    public boolean acceptsPackages;
    public String addressFilter;
    public PackagePortTarget target;
    public PackagePortInventory inventory;

    protected AnimatedContainerBehaviour<PackagePortMenu> openTracker;

    public PackagePortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        addressFilter = "";
        acceptsPackages = true;
        inventory = new PackagePortInventory();
    }

    public boolean isBackedUp() {
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++)
            if (inventory.getItem(i).isEmpty())
                return false;
        return true;
    }

    public void filterChanged() {
        if (target != null) {
            target.deregister(this, level, worldPosition);
            target.register(this, level, worldPosition);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (target != null)
            target.register(this, level, worldPosition);
    }

    public String getFilterString() {
        return acceptsPackages ? addressFilter : null;
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (target != null)
            view.store("Target", PackagePortTarget.CODEC, target);
        view.putString("AddressFilter", addressFilter);
        view.putBoolean("AcceptsPackages", acceptsPackages);
        inventory.write(view);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        inventory.read(view);
        PackagePortTarget prevTarget = target;
        target = view.read("Target", PackagePortTarget.CODEC).orElse(null);
        addressFilter = view.getStringOr("AddressFilter", "");
        acceptsPackages = view.getBooleanOr("AcceptsPackages", false);
        if (clientPacket && prevTarget != target)
            invalidateRenderBoundingBox();
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void clearContent() {
        inventory.clearContent();
    }

    @Override
    public void destroy() {
        if (target != null)
            target.deregister(this, level, worldPosition);
        super.destroy();
        Containers.dropContents(level, worldPosition, inventory);
    }

    public void drop(ItemStack box) {
        if (box.isEmpty())
            return;
        Block.popResource(level, worldPosition, box);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(openTracker = new AnimatedContainerBehaviour<>(this, PackagePortMenu.class));
        openTracker.onOpenChanged(this::onOpenChange);
    }

    protected abstract void onOpenChange(boolean open);

    public InteractionResult use(Player player) {
        if (player == null || player.isCrouching())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (FakePlayerHandler.has(player))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        ItemStack mainHandItem = player.getMainHandItem();
        boolean clipboard = mainHandItem.is(AllItems.CLIPBOARD);

        if (level.isClientSide()) {
            if (!clipboard)
                onOpenedManually();
            return InteractionResult.SUCCESS;
        }

        if (clipboard) {
            addAddressToClipboard(player, mainHandItem);
            return InteractionResult.SUCCESS;
        }

        openHandledScreen((ServerPlayer) player);
        return InteractionResult.SUCCESS;
    }

    protected void onOpenedManually() {
    }

    private void addAddressToClipboard(Player player, ItemStack mainHandItem) {
        if (addressFilter == null || addressFilter.isBlank())
            return;

        ClipboardContent clipboard = mainHandItem.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
        List<List<ClipboardEntry>> list = ClipboardEntry.readAll(clipboard);
        for (List<ClipboardEntry> page : list) {
            for (ClipboardEntry entry : page) {
                String existing = entry.text.getString();
                if (existing.equals("#" + addressFilter) || existing.equals("# " + addressFilter))
                    return;
            }
        }

        List<ClipboardEntry> page = null;

        for (List<ClipboardEntry> freePage : list) {
            if (freePage.size() > 11)
                continue;
            page = freePage;
            break;
        }

        if (page == null) {
            page = new ArrayList<>();
            list.add(page);
        }

        page.add(new ClipboardEntry(false, Component.literal("#" + addressFilter)));
        player.displayClientMessage(Component.translatable("create.clipboard.address_added", addressFilter), true);

        clipboard = clipboard.setPages(list).setType(ClipboardType.WRITTEN);
        mainHandItem.set(AllDataComponents.CLIPBOARD_CONTENT, clipboard);
    }

    @Override
    public MenuBase<?> createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer, RegistryFriendlyByteBuf extraData) {
        extraData.writeBlockPos(worldPosition);
        return new PackagePortMenu(pContainerId, pPlayerInventory, this);
    }

    public int getComparatorOutput() {
        if (inventory == null) {
            return 0;
        } else {
            int itemsFound = 0;
            float proportion = 0.0F;

            int size = inventory.getContainerSize();
            for (int j = 0; j < size; ++j) {
                ItemStack itemstack = inventory.getItem(j);

                if (!itemstack.isEmpty()) {
                    proportion += (float) itemstack.getCount() / (float) itemstack.getMaxStackSize();
                    ++itemsFound;
                }
            }

            proportion = proportion / (float) size;
            return Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
        }
    }

    public class PackagePortInventory implements SidedItemInventory {
        public static final int[] SLOTS = SlotRangeCache.get(18);
        public final NonNullList<ItemStack> stacks = NonNullList.withSize(18, ItemStack.EMPTY);
        private boolean receive = true;

        @Override
        public int[] getSlotsForFace(Direction side) {
            return SLOTS;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            String filterString = getFilterString();
            if (receive) {
                return filterString != null && PackageItem.matchAddress(stack, filterString);
            } else {
                return filterString == null || !PackageItem.matchAddress(stack, filterString);
            }
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            String filterString = getFilterString();
            if (receive) {
                return filterString == null || !PackageItem.matchAddress(stack, filterString);
            } else {
                return filterString != null && PackageItem.matchAddress(stack, filterString);
            }
        }

        public void receiveMode() {
            this.receive = true;
        }

        public void sendMode() {
            this.receive = false;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return PackageItem.isPackage(stack);
        }

        @Override
        public int getContainerSize() {
            return 18;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot >= 18) {
                return ItemStack.EMPTY;
            }
            return stacks.get(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 18) {
                return;
            }
            stacks.set(slot, stack);
        }

        @Override
        public void setChanged() {
            notifyUpdate();
        }

        public void write(ValueOutput view) {
            ValueOutput.TypedOutputList<ItemStackWithSlot> list = view.list("Inventory", ItemStackWithSlot.CODEC);
            for (int i = 0; i < 18; i++) {
                ItemStack stack = stacks.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                list.add(new ItemStackWithSlot(i, stack));
            }
        }

        public void read(ValueInput view) {
            ValueInput.TypedInputList<ItemStackWithSlot> list = view.listOrEmpty("Inventory", ItemStackWithSlot.CODEC);
            for (int i = 0; i < 18; i++) {
                stacks.set(i, ItemStack.EMPTY);
            }
            for (ItemStackWithSlot slot : list) {
                stacks.set(slot.slot(), slot.stack());
            }
        }
    }
}
