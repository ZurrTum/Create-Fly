package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ToolboxMountedStorageType extends MountedItemStorageType<ToolboxMountedStorage> {
    public ToolboxMountedStorageType() {
        super(ToolboxMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public ToolboxMountedStorage mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        return be instanceof ToolboxBlockEntity toolbox ? ToolboxMountedStorage.fromToolbox(toolbox) : null;
    }
}
