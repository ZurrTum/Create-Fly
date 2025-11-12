package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FakeTrackBlockEntity extends SyncedBlockEntity {

    int keepAlive;

    public FakeTrackBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.FAKE_TRACK, pos, state);
        keepAlive();
    }

    public void randomTick() {
        keepAlive--;
        if (keepAlive > 0)
            return;
        level.removeBlock(worldPosition, false);
    }

    public void keepAlive() {
        keepAlive = 3;
    }


}
