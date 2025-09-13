package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CreativeCrateMountedStorageType extends MountedItemStorageType<CreativeCrateMountedStorage> {
    public CreativeCrateMountedStorageType() {
        super(CreativeCrateMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public CreativeCrateMountedStorage mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof CreativeCrateBlockEntity crate) {
            ItemStack supplied = crate.filtering.getFilter();
            return new CreativeCrateMountedStorage(supplied);
        }

        return null;
    }
}
