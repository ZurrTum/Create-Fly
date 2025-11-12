package com.zurrtum.create.content.contraptions.gantry;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.TranslatingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GantryContraption extends TranslatingContraption {

    protected Direction facing;

    public GantryContraption() {
    }

    public GantryContraption(Direction facing) {
        this.facing = facing;
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        if (!searchMovedStructure(world, pos, null))
            return false;
        startMoving(world);
        return true;
    }

    @Override
    public void write(ValueOutput view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.store("Facing", Direction.CODEC, facing);
    }

    @Override
    public void read(Level world, ValueInput view, boolean spawnData) {
        facing = view.read("Facing", Direction.CODEC).orElse(Direction.DOWN);
        super.read(world, view, spawnData);
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return super.isAnchoringBlockAt(pos.relative(facing));
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
        return super.shouldUpdateAfterMovement(info) && !info.state().is(AllBlocks.GANTRY_CARRIAGE);
    }

}
