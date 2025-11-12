package com.zurrtum.create.content.equipment.toolbox;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.item.ItemHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ToolboxBlockEntity toolbox) {
            ItemHelper.copyContents(this, toolbox.inventory);
        }
    }

    @Override
    public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
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