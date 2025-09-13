package com.zurrtum.create.content.kinetics.crafter;

import com.zurrtum.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class CrafterHelper {

    public static MechanicalCrafterBlockEntity getCrafter(BlockRenderView reader, BlockPos pos) {
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        if (blockEntity instanceof MechanicalCrafterBlockEntity mechanicalCrafterBlockEntity) {
            return mechanicalCrafterBlockEntity;
        }
        return null;
    }

    public static ConnectedInput getInput(BlockRenderView reader, BlockPos pos) {
        MechanicalCrafterBlockEntity crafter = getCrafter(reader, pos);
        return crafter == null ? null : crafter.input;
    }

    public static boolean areCraftersConnected(BlockRenderView reader, BlockPos pos, BlockPos otherPos) {
        ConnectedInput input1 = getInput(reader, pos);
        ConnectedInput input2 = getInput(reader, otherPos);

        if (input1 == null || input2 == null)
            return false;
        if (input1.data.isEmpty() || input2.data.isEmpty())
            return false;
        try {
            if (pos.add(input1.data.getFirst()).equals(otherPos.add(input2.data.getFirst())))
                return true;
        } catch (IndexOutOfBoundsException e) {
            // race condition. data somehow becomes empty between the last 2 if statements
        }

        return false;
    }

}
