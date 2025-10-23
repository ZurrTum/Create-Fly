package com.zurrtum.create.content.logistics.packagePort;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PackagePortBlockEntity extends SmartBlockEntity implements MenuProvider {

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
        for (int i = 0, size = inventory.size(); i < size; i++)
            if (inventory.getStack(i).isEmpty())
                return false;
        return true;
    }

    public void filterChanged() {
        if (target != null) {
            target.deregister(this, world, pos);
            target.register(this, world, pos);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (target != null)
            target.register(this, world, pos);
    }

    public String getFilterString() {
        return acceptsPackages ? addressFilter : null;
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (target != null)
            view.put("Target", PackagePortTarget.CODEC, target);
        view.putString("AddressFilter", addressFilter);
        view.putBoolean("AcceptsPackages", acceptsPackages);
        inventory.write(view);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        inventory.read(view);
        PackagePortTarget prevTarget = target;
        target = view.read("Target", PackagePortTarget.CODEC).orElse(null);
        addressFilter = view.getString("AddressFilter", "");
        acceptsPackages = view.getBoolean("AcceptsPackages", false);
        if (clientPacket && prevTarget != target)
            invalidateRenderBoundingBox();
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void destroy() {
        if (target != null)
            target.deregister(this, world, pos);
        super.destroy();
        ItemScatterer.spawn(world, pos, inventory);
    }

    public void drop(ItemStack box) {
        if (box.isEmpty())
            return;
        Block.dropStack(world, pos, box);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(openTracker = new AnimatedContainerBehaviour<>(this, PackagePortMenu.class));
        openTracker.onOpenChanged(this::onOpenChange);
    }

    protected abstract void onOpenChange(boolean open);

    public ActionResult use(PlayerEntity player) {
        if (player == null || player.isInSneakingPose())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (FakePlayerHandler.has(player))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        ItemStack mainHandItem = player.getMainHandStack();
        boolean clipboard = mainHandItem.isOf(AllItems.CLIPBOARD);

        if (world.isClient) {
            if (!clipboard)
                onOpenedManually();
            return ActionResult.SUCCESS;
        }

        if (clipboard) {
            addAddressToClipboard(player, mainHandItem);
            return ActionResult.SUCCESS;
        }

        openHandledScreen((ServerPlayerEntity) player);
        return ActionResult.SUCCESS;
    }

    protected void onOpenedManually() {
    }

    private void addAddressToClipboard(PlayerEntity player, ItemStack mainHandItem) {
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

        page.add(new ClipboardEntry(false, Text.literal("#" + addressFilter)));
        player.sendMessage(Text.translatable("create.clipboard.address_added", addressFilter), true);

        clipboard = clipboard.setPages(list).setType(ClipboardType.WRITTEN);
        mainHandItem.set(AllDataComponents.CLIPBOARD_CONTENT, clipboard);
    }

    @Override
    public MenuBase<?> createMenu(int pContainerId, PlayerInventory pPlayerInventory, PlayerEntity pPlayer, RegistryByteBuf extraData) {
        extraData.writeBlockPos(pos);
        return new PackagePortMenu(pContainerId, pPlayerInventory, this);
    }

    public int getComparatorOutput() {
        if (inventory == null) {
            return 0;
        } else {
            int itemsFound = 0;
            float proportion = 0.0F;

            int size = inventory.size();
            for (int j = 0; j < size; ++j) {
                ItemStack itemstack = inventory.getStack(j);

                if (!itemstack.isEmpty()) {
                    proportion += (float) itemstack.getCount() / (float) itemstack.getMaxCount();
                    ++itemsFound;
                }
            }

            proportion = proportion / (float) size;
            return MathHelper.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
        }
    }

    public class PackagePortInventory implements SidedItemInventory {
        public static final int[] SLOTS = SlotRangeCache.get(18);
        public final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(18, ItemStack.EMPTY);
        private boolean receive = true;

        @Override
        public int[] getAvailableSlots(Direction side) {
            return SLOTS;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            String filterString = getFilterString();
            if (receive) {
                return filterString != null && PackageItem.matchAddress(stack, filterString);
            } else {
                return filterString == null || !PackageItem.matchAddress(stack, filterString);
            }
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
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
        public boolean isValid(int slot, ItemStack stack) {
            return PackageItem.isPackage(stack);
        }

        @Override
        public int size() {
            return 18;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot >= 18) {
                return ItemStack.EMPTY;
            }
            return stacks.get(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= 18) {
                return;
            }
            stacks.set(slot, stack);
        }

        @Override
        public void markDirty() {
            notifyUpdate();
        }

        public void write(WriteView view) {
            WriteView.ListAppender<StackWithSlot> list = view.getListAppender("Inventory", StackWithSlot.CODEC);
            for (int i = 0; i < 18; i++) {
                ItemStack stack = stacks.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                list.add(new StackWithSlot(i, stack));
            }
        }

        public void read(ReadView view) {
            ReadView.TypedListReadView<StackWithSlot> list = view.getTypedListView("Inventory", StackWithSlot.CODEC);
            for (int i = 0; i < 18; i++) {
                stacks.set(i, ItemStack.EMPTY);
            }
            for (StackWithSlot slot : list) {
                stacks.set(slot.slot(), slot.stack());
            }
        }
    }
}
