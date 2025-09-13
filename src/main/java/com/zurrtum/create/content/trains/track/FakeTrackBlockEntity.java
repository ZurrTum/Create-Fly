package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

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
        world.removeBlock(pos, false);
    }

    public void keepAlive() {
        keepAlive = 3;
    }


}
