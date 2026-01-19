package com.zurrtum.create.client.content.decoration.palettes;

import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

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
    public @Nullable CTSpriteShiftEntry getShift(BlockState state, RandomSource rand, Direction direction, TextureAtlasSprite sprite) {
        if (direction.getAxis() == Axis.Y || sprite == null)
            return null;
        CTSpriteShiftEntry entry = shifts.get(rand.nextInt(shifts.size()));
        if (entry.getOriginal() == sprite)
            return entry;
        return super.getShift(state, rand, direction, sprite);
    }

    @Override
    public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        return null;
    }

    @Override
    public @Nullable CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        return AllCTTypes.RECTANGLE;
    }

}
