package com.zurrtum.create.content.schematics.table;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.utility.IInteractionChecker;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SchematicTableBlockEntity extends SmartBlockEntity implements MenuProvider, IInteractionChecker, Clearable {

    public SchematicTableInventory inventory;
    public boolean isUploading;
    public String uploadingSchematic;
    public float uploadingProgress;
    public boolean sendUpdate;

    public class SchematicTableInventory implements ItemInventory {
        ItemStack left = ItemStack.EMPTY;
        ItemStack right = ItemStack.EMPTY;

        @Override
        public int size() {
            return 2;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot >= 2) {
                return ItemStack.EMPTY;
            }
            return slot == 0 ? left : right;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= 2) {
                return;
            }
            if (slot == 0) {
                left = stack;
            } else {
                right = stack;
            }
        }

        public void write(WriteView view) {
            WriteView.ListAppender<ItemStack> list = view.getListAppender("Inventory", ItemStack.OPTIONAL_CODEC);
            list.add(left);
            list.add(right);
        }

        public void read(ReadView view) {
            java.util.Iterator<ItemStack> iterator = view.getTypedListView("Inventory", ItemStack.OPTIONAL_CODEC).iterator();
            if (iterator.hasNext()) {
                left = iterator.next();
                if (iterator.hasNext()) {
                    right = iterator.next();
                }
            }
        }

        @Override
        public void markDirty() {
            SchematicTableBlockEntity.this.markDirty();
        }
    }

    public SchematicTableBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SCHEMATIC_TABLE, pos, state);
        inventory = new SchematicTableInventory();
        uploadingSchematic = null;
        uploadingProgress = 0;
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        ItemScatterer.spawn(world, pos, inventory);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        inventory.read(view);
        super.read(view, clientPacket);
        if (!clientPacket)
            return;
        if (view.getBoolean("Uploading", false)) {
            isUploading = true;
            uploadingSchematic = view.getString("Schematic", "");
            uploadingProgress = view.getFloat("Progress", 0);
        } else {
            isUploading = false;
            uploadingSchematic = null;
            uploadingProgress = 0;
        }
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        inventory.write(view);
        super.write(view, clientPacket);
        if (clientPacket && isUploading) {
            view.putBoolean("Uploading", true);
            view.putString("Schematic", uploadingSchematic);
            view.putFloat("Progress", uploadingProgress);
        }
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public void tick() {
        // Update Client block entity
        if (sendUpdate) {
            sendUpdate = false;
            world.updateListeners(pos, getCachedState(), getCachedState(), 6);
        }
    }

    public void startUpload(String schematic) {
        isUploading = true;
        uploadingProgress = 0;
        uploadingSchematic = schematic;
        sendUpdate = true;
        inventory.setStack(0, ItemStack.EMPTY);
    }

    public void finishUpload() {
        isUploading = false;
        uploadingProgress = 0;
        uploadingSchematic = null;
        sendUpdate = true;
    }

    @Override
    public SchematicTableMenu createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        sendToMenu(extraData);
        return new SchematicTableMenu(id, inv, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("create.gui.schematicTable.title");
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null || world.getBlockEntity(pos) != this) {
            return false;
        }
        return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

}
