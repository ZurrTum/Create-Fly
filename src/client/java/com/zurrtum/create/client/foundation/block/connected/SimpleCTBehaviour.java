package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SimpleCTBehaviour extends ConnectedTextureBehaviour.Base {

    protected CTSpriteShiftEntry shift;

    public SimpleCTBehaviour(CTSpriteShiftEntry shift) {
        this.shift = shift;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        return shift;
    }

}
