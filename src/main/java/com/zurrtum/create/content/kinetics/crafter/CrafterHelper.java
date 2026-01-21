package com.zurrtum.create.content.kinetics.crafter;

import com.zurrtum.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class CrafterHelper {
    @Nullable
    public static MechanicalCrafterBlockEntity getCrafter(BlockAndTintGetter reader, BlockPos pos) {
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        if (blockEntity instanceof MechanicalCrafterBlockEntity mechanicalCrafterBlockEntity) {
            return mechanicalCrafterBlockEntity;
        }
        return null;
    }

    @Nullable
    public static ConnectedInput getInput(BlockAndTintGetter reader, BlockPos pos) {
        MechanicalCrafterBlockEntity crafter = getCrafter(reader, pos);
        return crafter == null ? null : crafter.input;
    }

    public static boolean areCraftersConnected(BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos) {
        ConnectedInput input1 = getInput(reader, pos);
        ConnectedInput input2 = getInput(reader, otherPos);

        if (input1 == null || input2 == null)
            return false;
        if (input1.data.isEmpty() || input2.data.isEmpty())
            return false;
        try {
            if (pos.offset(input1.data.getFirst()).equals(otherPos.offset(input2.data.getFirst())))
                return true;
        } catch (IndexOutOfBoundsException e) {
            // race condition. data somehow becomes empty between the last 2 if statements
        }

        return false;
    }

}
