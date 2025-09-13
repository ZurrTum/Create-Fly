package com.zurrtum.create.client.content.decoration;

import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.HorizontalCTBehaviour;
import com.zurrtum.create.content.decoration.MetalScaffoldingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class MetalScaffoldingCTBehaviour extends HorizontalCTBehaviour {

    protected CTSpriteShiftEntry insideShift;

    public MetalScaffoldingCTBehaviour(CTSpriteShiftEntry outsideShift, CTSpriteShiftEntry insideShift, CTSpriteShiftEntry topShift) {
        super(outsideShift, topShift);
        this.insideShift = insideShift;
    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }

    @Override
    protected boolean isBeingBlocked(BlockState state, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        return face.getAxis() == Axis.Y && super.isBeingBlocked(state, reader, pos, otherPos, face);
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        if (direction.getAxis() != Axis.Y && sprite == insideShift.getOriginal())
            return insideShift;
        return super.getShift(state, direction, sprite);
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        return super.connectsTo(state, other, reader, pos, otherPos, face) && state.get(MetalScaffoldingBlock.BOTTOM) && other.get(
            MetalScaffoldingBlock.BOTTOM);
    }

}
