package com.zurrtum.create.client.content.decoration.encasing;

import com.zurrtum.create.client.AllCasings;
import com.zurrtum.create.client.AllCasings.Entry;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class EncasedCTBehaviour extends ConnectedTextureBehaviour.Base {

    private final CTSpriteShiftEntry shift;

    public EncasedCTBehaviour(CTSpriteShiftEntry shift) {
        this.shift = shift;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        if (isBeingBlocked(state, reader, pos, otherPos, face))
            return false;
        Entry entry = AllCasings.get(state);
        Entry otherEntry = AllCasings.get(other);
        if (entry == null || otherEntry == null)
            return false;
        if (!entry.isSideValid(state, face) || !otherEntry.isSideValid(other, face))
            return false;
        return entry.getCasing() == otherEntry.getCasing();
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        return shift;
    }

}
