package com.zurrtum.create.content.schematics.table;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.utility.IInteractionChecker;
import com.zurrtum.create.infrastructure.items.ItemInventory;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SchematicTableBlockEntity extends SmartBlockEntity implements MenuProvider, IInteractionChecker {

    public SchematicTableInventory inventory;
    public boolean isUploading;
    public String uploadingSchematic;
    public float uploadingProgress;
    public boolean sendUpdate;

    public class SchematicTableInventory implements ItemInventory {
        ItemStack left = ItemStack.EMPTY;
        ItemStack right = ItemStack.EMPTY;

        @Override
        public int getContainerSize() {
            return 2;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot >= 2) {
                return ItemStack.EMPTY;
            }
            return slot == 0 ? left : right;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 2) {
                return;
            }
            if (slot == 0) {
                left = stack;
            } else {
                right = stack;
            }
        }

        public void write(ValueOutput view) {
            ValueOutput.TypedOutputList<ItemStack> list = view.list("Inventory", ItemStack.OPTIONAL_CODEC);
            list.add(left);
            list.add(right);
        }

        public void read(ValueInput view) {
            java.util.Iterator<ItemStack> iterator = view.listOrEmpty("Inventory", ItemStack.OPTIONAL_CODEC).iterator();
            if (iterator.hasNext()) {
                left = iterator.next();
                if (iterator.hasNext()) {
                    right = iterator.next();
                }
            }
        }

        @Override
        public void setChanged() {
            SchematicTableBlockEntity.this.setChanged();
        }
    }

    public SchematicTableBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SCHEMATIC_TABLE, pos, state);
        inventory = new SchematicTableInventory();
        uploadingSchematic = null;
        uploadingProgress = 0;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        Containers.dropContents(level, pos, inventory);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        inventory.read(view);
        super.read(view, clientPacket);
        if (!clientPacket)
            return;
        if (view.getBooleanOr("Uploading", false)) {
            isUploading = true;
            uploadingSchematic = view.getStringOr("Schematic", "");
            uploadingProgress = view.getFloatOr("Progress", 0);
        } else {
            isUploading = false;
            uploadingSchematic = null;
            uploadingProgress = 0;
        }
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        inventory.write(view);
        super.write(view, clientPacket);
        if (clientPacket && isUploading) {
            view.putBoolean("Uploading", true);
            view.putString("Schematic", uploadingSchematic);
            view.putFloat("Progress", uploadingProgress);
        }
    }

    @Override
    public void tick() {
        // Update Client block entity
        if (sendUpdate) {
            sendUpdate = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 6);
        }
    }

    public void startUpload(String schematic) {
        isUploading = true;
        uploadingProgress = 0;
        uploadingSchematic = schematic;
        sendUpdate = true;
        inventory.setItem(0, ItemStack.EMPTY);
    }

    public void finishUpload() {
        isUploading = false;
        uploadingProgress = 0;
        uploadingSchematic = null;
        sendUpdate = true;
    }

    @Override
    public SchematicTableMenu createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        sendToMenu(extraData);
        return new SchematicTableMenu(id, inv, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("create.gui.schematicTable.title");
    }

    @Override
    public boolean canPlayerUse(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

}
