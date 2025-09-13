package com.zurrtum.create.content.equipment.toolbox;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToolboxMountedStorage extends WrapperMountedItemStorage<ToolboxInventory> {
    public static final MapCodec<ToolboxMountedStorage> CODEC = ToolboxInventory.CODEC.xmap(ToolboxMountedStorage::new, storage -> storage.wrapped)
        .fieldOf("value");

    protected ToolboxMountedStorage(MountedItemStorageType<?> type, ToolboxInventory wrapped) {
        super(type, wrapped);
    }

    protected ToolboxMountedStorage(ToolboxInventory wrapped) {
        this(AllMountedStorageTypes.TOOLBOX, wrapped);
    }

    @Override
    public void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ToolboxBlockEntity toolbox) {
            ItemHelper.copyContents(this, toolbox.inventory);
        }
    }

    @Override
    public boolean handleInteraction(ServerPlayerEntity player, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
        // The default impl will fail anyway, might as well cancel trying
        return false;
    }

    public static ToolboxMountedStorage fromToolbox(ToolboxBlockEntity toolbox) {
        // the inventory will send updates to the block entity, make an isolated copy to avoid that
        ToolboxInventory copy = new ToolboxInventory(null);
        ItemHelper.copyContents(toolbox.inventory, copy);

        List<ItemStack> from = toolbox.inventory.filters;
        List<ItemStack> to = copy.filters;
        for (int i = 0, size = from.size(); i < size; i++) {
            to.set(i, from.get(i).copy());
        }

        return new ToolboxMountedStorage(copy);
    }
}