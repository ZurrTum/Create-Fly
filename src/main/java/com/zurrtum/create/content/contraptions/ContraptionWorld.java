package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ContraptionWorld extends WrappedLevel {
    final Contraption contraption;
    private final int minY;
    private final int height;

    public ContraptionWorld(World world, Contraption contraption) {
        super(world);

        this.contraption = contraption;

        // Include 1 block above/below contraption height range to avoid certain edge-case Starlight crashes with
        // downward-facing mechanical pistons.
        minY = nextMultipleOf16(contraption.bounds.minY - 1);
        height = nextMultipleOf16(contraption.bounds.maxY + 1) - minY;
    }

    // https://math.stackexchange.com/questions/291468
    private static int nextMultipleOf16(double a) {
        return (((Math.abs((int) a) - 1) | 15) + 1) * MathHelper.sign(a);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        StructureTemplate.StructureBlockInfo blockInfo = contraption.getBlocks().get(pos);

        if (blockInfo != null)
            return blockInfo.state();

        return Blocks.AIR.getDefaultState();
    }

    @Override
    public void playSoundClient(
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundCategory category,
        float volume,
        float pitch,
        boolean distanceDelay
    ) {
        level.playSoundClient(x, y, z, sound, category, volume, pitch, distanceDelay);
    }

    // Ensure that we provide accurate information about ContraptionWorld height to mods (such as Starlight) which
    // expect Levels to only have blocks located in chunks within their height range.

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getBottomY() {
        return minY;
    }
}
