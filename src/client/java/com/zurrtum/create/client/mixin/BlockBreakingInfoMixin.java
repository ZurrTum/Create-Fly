package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.foundation.block.render.BlockDestructionProgressExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;

@Mixin(BlockDestructionProgress.class)
public class BlockBreakingInfoMixin implements BlockDestructionProgressExtension {
    @Unique
    private Set<BlockPos> create$extraPositions;

    @Override
    public Set<BlockPos> create$getExtraPositions() {
        return create$extraPositions;
    }

    @Override
    public void create$setExtraPositions(Set<BlockPos> positions) {
        create$extraPositions = positions;
    }
}
