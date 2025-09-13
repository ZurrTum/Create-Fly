package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class StabilizedContraption extends Contraption {

    private Direction facing;

    public StabilizedContraption() {
    }

    public StabilizedContraption(Direction facing) {
        this.facing = facing;
    }

    @Override
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        BlockPos offset = pos.offset(facing);
        if (!searchMovedStructure(world, offset, null))
            return false;
        startMoving(world);
        if (blocks.isEmpty())
            return false;
        return true;
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return false;
    }

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.STABILIZED;
    }

    @Override
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putInt("Facing", facing.getIndex());
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        facing = Direction.byIndex(view.getInt("Facing", 0));
        super.read(world, view, spawnData);
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        return false;
    }

    public Direction getFacing() {
        return facing;
    }

}
