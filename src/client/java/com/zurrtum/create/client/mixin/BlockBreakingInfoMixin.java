package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.foundation.block.render.BlockDestructionProgressExtension;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(BlockBreakingInfo.class)
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
