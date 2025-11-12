package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        BlockPos offset = pos.relative(facing);
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
        return pos.equals(anchor.relative(facing.getOpposite()));
    }

    @Override
    public void addBlock(Level level, BlockPos pos, Pair<StructureBlockInfo, BlockEntity> capture) {
        BlockPos localPos = pos.subtract(anchor);
        if (!getBlocks().containsKey(localPos) && getSailBlock(capture).is(AllBlockTags.WINDMILL_SAILS))
            sailBlocks++;
        super.addBlock(level, pos, capture);
    }

    private BlockState getSailBlock(Pair<StructureBlockInfo, BlockEntity> capture) {
        BlockState state = capture.getKey().state();
        if (state.is(AllBlocks.COPYCAT_PANEL) && capture.getRight() instanceof CopycatBlockEntity cbe)
            return cbe.getMaterial();
        return state;
    }

    @Override
    public void write(ValueOutput view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putInt("Sails", sailBlocks);
        view.putInt("Facing", facing.get3DDataValue());
    }

    @Override
    public void read(Level world, ValueInput view, boolean spawnData) {
        sailBlocks = view.getIntOr("Sails", 0);
        facing = Direction.from3DDataValue(view.getIntOr("Facing", 0));
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
