package com.zurrtum.create.client.content.decoration.palettes;

import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WeatheredIronWindowCTBehaviour extends ConnectedTextureBehaviour.Base {

    private List<CTSpriteShiftEntry> shifts;

    public WeatheredIronWindowCTBehaviour() {
        this.shifts = List.of(
            AllSpriteShifts.OLD_FACTORY_WINDOW_1,
            AllSpriteShifts.OLD_FACTORY_WINDOW_2,
            AllSpriteShifts.OLD_FACTORY_WINDOW_3,
            AllSpriteShifts.OLD_FACTORY_WINDOW_4
        );
    }

    @Override
    public @Nullable CTSpriteShiftEntry getShift(BlockState state, Random rand, Direction direction, @NotNull Sprite sprite) {
        if (direction.getAxis() == Axis.Y || sprite == null)
            return null;
        CTSpriteShiftEntry entry = shifts.get(rand.nextInt(shifts.size()));
        if (entry.getOriginal() == sprite)
            return entry;
        return super.getShift(state, rand, direction, sprite);
    }

    @Override
    public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        return null;
    }

    @Override
    public @Nullable CTType getDataType(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
        return AllCTTypes.RECTANGLE;
    }

}
