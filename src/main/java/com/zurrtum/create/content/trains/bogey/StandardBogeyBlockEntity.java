package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBogeyStyles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class StandardBogeyBlockEntity extends AbstractBogeyBlockEntity {

    public StandardBogeyBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BOGEY, pos, state);
    }

    @Override
    public BogeyStyle getDefaultStyle() {
        return AllBogeyStyles.STANDARD;
    }
}
