package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class BearingContraption extends Contraption {

    protected int sailBlocks;
    protected Direction facing;

    private boolean isWindmill;

    public BearingContraption() {
    }

    public BearingContraption(boolean isWindmill, Direction facing) {
        this.isWindmill = isWindmill;
        this.facing = facing;
    }

    @Override
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        BlockPos offset = pos.offset(facing);
        if (!searchMovedStructure(world, offset, null))
            return false;
        startMoving(world);
        expandBoundsAroundAxis(facing.getAxis());
        if (isWindmill && sailBlocks < AllConfigs.server().kinetics.minimumWindmillSails.get())
            throw AssemblyException.notEnoughSails(sailBlocks);
        return !blocks.isEmpty();
    }

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.BEARING;
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return pos.equals(anchor.offset(facing.getOpposite()));
    }

    @Override
    public void addBlock(World level, BlockPos pos, Pair<StructureBlockInfo, BlockEntity> capture) {
        BlockPos localPos = pos.subtract(anchor);
        if (!getBlocks().containsKey(localPos) && getSailBlock(capture).isIn(AllBlockTags.WINDMILL_SAILS))
            sailBlocks++;
        super.addBlock(level, pos, capture);
    }

    private BlockState getSailBlock(Pair<StructureBlockInfo, BlockEntity> capture) {
        BlockState state = capture.getKey().state();
        if (state.isOf(AllBlocks.COPYCAT_PANEL) && capture.getRight() instanceof CopycatBlockEntity cbe)
            return cbe.getMaterial();
        return state;
    }

    @Override
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putInt("Sails", sailBlocks);
        view.putInt("Facing", facing.getIndex());
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        sailBlocks = view.getInt("Sails", 0);
        facing = Direction.byIndex(view.getInt("Facing", 0));
        super.read(world, view, spawnData);
    }

    public int getSailBlocks() {
        return sailBlocks;
    }

    public Direction getFacing() {
        return facing;
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        if (facing.getOpposite() == this.facing && BlockPos.ZERO.equals(localPos))
            return false;
        return facing.getAxis() == this.facing.getAxis();
    }

}
