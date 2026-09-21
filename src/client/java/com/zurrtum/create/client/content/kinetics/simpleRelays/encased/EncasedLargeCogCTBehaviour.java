package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

public class EncasedLargeCogCTBehaviour extends EncasedCTBehaviour {
    public EncasedLargeCogCTBehaviour(CTSpriteShiftEntry shift) {
        super(shift);
    }

    @Override
    protected boolean reverseUVs(BlockState state, Direction face) {
        return state.getValue(AXIS).isHorizontal() && face.getAxis()
            .isHorizontal() && face.getAxisDirection() == AxisDirection.POSITIVE;
    }

    @Override
    @Nullable
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        Axis axis = state.getValue(AXIS);
        if (axis == direction.getAxis() && state.getValue(
            direction.getAxisDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT :
                EncasedCogwheelBlock.BOTTOM_SHAFT)) {
            return null;
        }
        return super.getShift(state, direction, sprite);
    }
}
