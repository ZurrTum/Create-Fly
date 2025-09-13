package com.zurrtum.create.content.contraptions.gantry;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.TranslatingContraption;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GantryContraption extends TranslatingContraption {

    protected Direction facing;

    public GantryContraption() {
    }

    public GantryContraption(Direction facing) {
        this.facing = facing;
    }

    @Override
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        if (!searchMovedStructure(world, pos, null))
            return false;
        startMoving(world);
        return true;
    }

    @Override
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.put("Facing", Direction.CODEC, facing);
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        facing = view.read("Facing", Direction.CODEC).orElse(Direction.DOWN);
        super.read(world, view, spawnData);
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return super.isAnchoringBlockAt(pos.offset(facing));
    }

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.GANTRY;
    }

    public Direction getFacing() {
        return facing;
    }

    @Override
    protected boolean shouldUpdateAfterMovement(StructureBlockInfo info) {
        return super.shouldUpdateAfterMovement(info) && !info.state().isOf(AllBlocks.GANTRY_CARRIAGE);
    }

}
