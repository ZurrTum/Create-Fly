package com.zurrtum.create.client.content.contraptions.mounted;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CartAssemblerValueBoxTransform extends CenteredSideValueBoxTransform {
    public CartAssemblerValueBoxTransform() {
        super((state, d) -> {
            if (d.getAxis().isVertical())
                return false;
            if (!state.contains(CartAssemblerBlock.RAIL_SHAPE))
                return false;
            RailShape railShape = state.get(CartAssemblerBlock.RAIL_SHAPE);
            return (d.getAxis() == Direction.Axis.X) == (railShape == RailShape.NORTH_SOUTH);
        });
    }

    @Override
    protected Vec3d getSouthLocation() {
        return VecHelper.voxelSpace(8, 7, 17.5);
    }
}
