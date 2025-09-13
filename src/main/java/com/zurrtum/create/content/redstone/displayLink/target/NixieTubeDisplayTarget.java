package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlock;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class NixieTubeDisplayTarget extends SingleLineDisplayTarget {

    @Override
    protected void acceptLine(MutableText text, DisplayLinkContext context) {
        NixieTubeBlock.walkNixies(
            context.level(), context.getTargetPos(), (currentPos, rowPosition) -> {
                BlockEntity blockEntity = context.level().getBlockEntity(currentPos);
                if (blockEntity instanceof NixieTubeBlockEntity nixie)
                    nixie.displayCustomText(text, rowPosition);
            }
        );
    }

    @Override
    protected int getWidth(DisplayLinkContext context) {
        MutableInt count = new MutableInt(0);
        NixieTubeBlock.walkNixies(context.level(), context.getTargetPos(), (currentPos, rowPosition) -> count.add(2));
        return count.intValue();
    }

    public Box getMultiblockBounds(WorldAccess level, BlockPos pos) {
        MutableObject<BlockPos> start = new MutableObject<>(null);
        MutableObject<BlockPos> end = new MutableObject<>(null);
        NixieTubeBlock.walkNixies(
            level, pos, (currentPos, rowPosition) -> {
                end.setValue(currentPos);
                if (start.getValue() == null)
                    start.setValue(currentPos);
            }
        );

        BlockPos diffToCurrent = start.getValue().subtract(pos);
        BlockPos diff = end.getValue().subtract(start.getValue());

        return super.getMultiblockBounds(level, pos).offset(diffToCurrent).stretch(Vec3d.of(diff));
    }
}
