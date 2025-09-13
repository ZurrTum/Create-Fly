package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBogeyStyles;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class StandardBogeyBlockEntity extends AbstractBogeyBlockEntity {

    public StandardBogeyBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BOGEY, pos, state);
    }

    @Override
    public BogeyStyle getDefaultStyle() {
        return AllBogeyStyles.STANDARD;
    }
}
