package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class HorizontalCTBehaviour extends ConnectedTextureBehaviour.Base {

    protected CTSpriteShiftEntry topShift;
    protected CTSpriteShiftEntry layerShift;

    public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift) {
        this(layerShift, null);
    }

    public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
        this.layerShift = layerShift;
        this.topShift = topShift;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        return direction.getAxis().isHorizontal() ? layerShift : topShift;
    }

}