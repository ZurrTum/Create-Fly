package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CreativeCrateMountedStorageType extends MountedItemStorageType<CreativeCrateMountedStorage> {
    public CreativeCrateMountedStorageType() {
        super(CreativeCrateMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public CreativeCrateMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof CreativeCrateBlockEntity crate) {
            ItemStack supplied = crate.filtering.getFilter();
            return new CreativeCrateMountedStorage(supplied);
        }

        return null;
    }
}
