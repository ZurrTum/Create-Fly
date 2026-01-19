package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ToolboxMountedStorageType extends MountedItemStorageType<ToolboxMountedStorage> {
    public ToolboxMountedStorageType() {
        super(ToolboxMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public ToolboxMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        return be instanceof ToolboxBlockEntity toolbox ? ToolboxMountedStorage.fromToolbox(toolbox) : null;
    }
}
