package com.zurrtum.create.content.contraptions.piston;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.TranslatingContraption;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.*;
import static net.minecraft.state.property.Properties.FACING;

public class PistonContraption extends TranslatingContraption {

    protected int extensionLength;
    protected int initialExtensionProgress;
    protected Direction orientation;

    private Box pistonExtensionCollisionBox;
    private boolean retract;

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.PISTON;
    }

    public PistonContraption() {
    }

    public PistonContraption(Direction direction, boolean retract) {
        orientation = direction;
        this.retract = retract;
    }

    @Override
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        if (!collectExtensions(world, pos, orientation))
            return false;
        int count = blocks.size();
        if (!searchMovedStructure(world, anchor, retract ? orientation.getOpposite() : orientation))
            return false;
        if (blocks.size() == count) { // no new blocks added
            bounds = pistonExtensionCollisionBox;
        } else {
            bounds = bounds.union(pistonExtensionCollisionBox);
        }
        startMoving(world);
        return true;
    }

    private boolean collectExtensions(World world, BlockPos pos, Direction direction) throws AssemblyException {
        List<StructureBlockInfo> poles = new ArrayList<>();
        BlockPos actualStart = pos;
        BlockState nextBlock = world.getBlockState(actualStart.offset(direction));
        int extensionsInFront = 0;
        BlockState blockState = world.getBlockState(pos);
        boolean sticky = isStickyPiston(blockState);

        if (!isPiston(blockState))
            return false;

        if (blockState.get(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
            while (PistonExtensionPoleBlock.PlacementHelper.get()
                .matchesAxis(nextBlock, direction.getAxis()) || isPistonHead(nextBlock) && nextBlock.get(FACING) == direction) {

                actualStart = actualStart.offset(direction);
                poles.add(new StructureBlockInfo(actualStart, nextBlock.with(FACING, direction), null));
                extensionsInFront++;

                if (isPistonHead(nextBlock))
                    break;

                nextBlock = world.getBlockState(actualStart.offset(direction));
                if (extensionsInFront > MechanicalPistonBlock.maxAllowedPistonPoles())
                    throw AssemblyException.tooManyPistonPoles();
            }
        }

        if (extensionsInFront == 0)
            poles.add(new StructureBlockInfo(
                pos,
                AllBlocks.MECHANICAL_PISTON_HEAD.getDefaultState().with(FACING, direction)
                    .with(Properties.PISTON_TYPE, sticky ? PistonType.STICKY : PistonType.DEFAULT),
                null
            ));
        else
            poles.add(new StructureBlockInfo(pos, AllBlocks.PISTON_EXTENSION_POLE.getDefaultState().with(FACING, direction), null));

        BlockPos end = pos;
        nextBlock = world.getBlockState(end.offset(direction.getOpposite()));
        int extensionsInBack = 0;

        while (PistonExtensionPoleBlock.PlacementHelper.get().matchesAxis(nextBlock, direction.getAxis())) {
            end = end.offset(direction.getOpposite());
            poles.add(new StructureBlockInfo(end, nextBlock.with(FACING, direction), null));
            extensionsInBack++;
            nextBlock = world.getBlockState(end.offset(direction.getOpposite()));

            if (extensionsInFront + extensionsInBack > MechanicalPistonBlock.maxAllowedPistonPoles())
                throw AssemblyException.tooManyPistonPoles();
        }

        anchor = pos.offset(direction, initialExtensionProgress + 1);
        extensionLength = extensionsInBack + extensionsInFront;
        initialExtensionProgress = extensionsInFront;
        pistonExtensionCollisionBox = new Box(
            Vec3d.of(BlockPos.ZERO.offset(direction, -1)),
            Vec3d.of(BlockPos.ZERO.offset(direction, -extensionLength - 1))
        ).stretch(1, 1, 1);

        if (extensionLength == 0)
            throw AssemblyException.noPistonPoles();

        bounds = new Box(0, 0, 0, 0, 0, 0);

        for (StructureBlockInfo pole : poles) {
            BlockPos relPos = pole.pos().offset(direction, -extensionsInFront);
            BlockPos localPos = relPos.subtract(anchor);
            getBlocks().put(localPos, new StructureBlockInfo(localPos, pole.state(), null));
            //pistonExtensionCollisionBox = pistonExtensionCollisionBox.union(new AABB(localPos));
        }

        return true;
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return pistonExtensionCollisionBox.contains(VecHelper.getCenterOf(pos.subtract(anchor)));
    }

    @Override
    protected boolean addToInitialFrontier(World world, BlockPos pos, Direction direction, Queue<BlockPos> frontier) throws AssemblyException {
        frontier.clear();
        boolean sticky = isStickyPiston(world.getBlockState(pos.offset(orientation, -1)));
        boolean retracting = direction != orientation;
        if (retracting && !sticky)
            return true;
        for (int offset = 0; offset <= AllConfigs.server().kinetics.maxChassisRange.get(); offset++) {
            if (offset == 1 && retracting)
                return true;
            BlockPos currentPos = pos.offset(orientation, offset + initialExtensionProgress);
            if (retracting && world.isOutOfHeightLimit(currentPos))
                return true;
            if (!world.isPosLoaded(currentPos))
                throw AssemblyException.unloadedChunk(currentPos);
            BlockState state = world.getBlockState(currentPos);
            if (!BlockMovementChecks.isMovementNecessary(state, world, currentPos))
                return true;
            if (BlockMovementChecks.isBrittle(state) && !(state.getBlock() instanceof DyedCarpetBlock))
                return true;
            if (isPistonHead(state) && state.get(FACING) == direction.getOpposite())
                return true;
            if (!BlockMovementChecks.isMovementAllowed(state, world, currentPos))
                if (retracting)
                    return true;
                else
                    throw AssemblyException.unmovableBlock(currentPos, state);
            if (retracting && state.getPistonBehavior() == PistonBehavior.PUSH_ONLY)
                return true;
            frontier.add(currentPos);
            if (BlockMovementChecks.isNotSupportive(state, orientation))
                return true;
        }
        return true;
    }

    @Override
    public void addBlock(World level, BlockPos pos, Pair<StructureBlockInfo, BlockEntity> capture) {
        super.addBlock(level, pos.offset(orientation, -initialExtensionProgress), capture);
    }

    @Override
    public BlockPos toLocalPos(BlockPos globalPos) {
        return globalPos.subtract(anchor).offset(orientation, -initialExtensionProgress);
    }

    @Override
    protected boolean customBlockPlacement(WorldAccess world, BlockPos pos, BlockState state) {
        BlockPos pistonPos = anchor.offset(orientation, -1);
        BlockState pistonState = world.getBlockState(pistonPos);
        BlockEntity be = world.getBlockEntity(pistonPos);
        if (pos.equals(pistonPos)) {
            if (be == null || be.isRemoved())
                return true;
            if (!isExtensionPole(state) && isPiston(pistonState))
                world.setBlockState(pistonPos, pistonState.with(MechanicalPistonBlock.STATE, PistonState.RETRACTED), 3 | 16);
            return true;
        }
        return false;
    }

    @Override
    protected boolean customBlockRemoval(WorldAccess world, BlockPos pos, BlockState state) {
        BlockPos pistonPos = anchor.offset(orientation, -1);
        BlockState blockState = world.getBlockState(pos);
        if (pos.equals(pistonPos) && isPiston(blockState)) {
            world.setBlockState(pos, blockState.with(MechanicalPistonBlock.STATE, PistonState.MOVING), 66 | 16);
            return true;
        }
        return false;
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        super.read(world, view, spawnData);
        initialExtensionProgress = view.getInt("InitialLength", 0);
        extensionLength = view.getInt("ExtensionLength", 0);
        orientation = view.read("Orientation", Direction.CODEC).orElseThrow();
    }

    @Override
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putInt("InitialLength", initialExtensionProgress);
        view.putInt("ExtensionLength", extensionLength);
        view.put("Orientation", Direction.CODEC, orientation);
    }

}
