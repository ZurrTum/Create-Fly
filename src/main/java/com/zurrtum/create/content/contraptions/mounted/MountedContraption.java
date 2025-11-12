package com.zurrtum.create.content.contraptions.mounted;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockEntity.CartMovementMode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Queue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import static com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock.RAIL_SHAPE;

public class MountedContraption extends Contraption {

    public CartMovementMode rotationMode;
    public AbstractMinecart connectedCart;

    public MountedContraption() {
        this(CartMovementMode.ROTATE);
    }

    public MountedContraption(CartMovementMode mode) {
        rotationMode = mode;
    }

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.MOUNTED;
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        BlockState state = world.getBlockState(pos);
        if (!state.hasProperty(RAIL_SHAPE))
            return false;
        if (!searchMovedStructure(world, pos, null))
            return false;

        Axis axis = state.getValue(RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.X : Axis.Z;
        addBlock(
            world,
            pos,
            Pair.of(
                new StructureBlockInfo(
                    pos,
                    AllBlocks.MINECART_ANCHOR.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, axis),
                    null
                ), null
            )
        );

        return blocks.size() != 1;
    }

    @Override
    protected boolean addToInitialFrontier(Level world, BlockPos pos, Direction direction, Queue<BlockPos> frontier) {
        frontier.clear();
        frontier.add(pos.above());
        return true;
    }

    @Override
    protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
        Pair<StructureBlockInfo, BlockEntity> pair = super.capture(world, pos);
        StructureBlockInfo capture = pair.getKey();
        if (!capture.state().is(AllBlocks.CART_ASSEMBLER))
            return pair;

        Pair<StructureBlockInfo, BlockEntity> anchorSwap = Pair.of(
            new StructureBlockInfo(
                pos,
                CartAssemblerBlock.createAnchor(capture.state()),
                null
            ), pair.getValue()
        );
        if (pos.equals(anchor) || connectedCart != null)
            return anchorSwap;

        for (Axis axis : Iterate.axes) {
            if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
                continue;
            for (AbstractMinecart abstractMinecartEntity : world.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos))) {
                if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
                    break;
                connectedCart = abstractMinecartEntity;
                connectedCart.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5f);
            }
        }

        return anchorSwap;
    }

    @Override
    protected boolean movementAllowed(BlockState state, Level world, BlockPos pos) {
        if (!pos.equals(anchor) && state.is(AllBlocks.CART_ASSEMBLER))
            return testSecondaryCartAssembler(world, pos);
        return super.movementAllowed(state, world, pos);
    }

    protected boolean testSecondaryCartAssembler(Level world, BlockPos pos) {
        for (Axis axis : Iterate.axes) {
            if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
                continue;
            for (AbstractMinecart abstractMinecartEntity : world.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos))) {
                if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
                    break;
                return true;
            }
        }
        return false;
    }

    @Override
    public void write(ValueOutput view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.store("RotationMode", CartMovementMode.CODEC, rotationMode);
    }

    @Override
    public void read(Level world, ValueInput view, boolean spawnData) {
        view.read("RotationMode", CartMovementMode.CODEC).ifPresent(mode -> rotationMode = mode);
        super.read(world, view, spawnData);
    }

    @Override
    protected boolean customBlockPlacement(LevelAccessor world, BlockPos pos, BlockState state) {
        return state.is(AllBlocks.MINECART_ANCHOR);
    }

    @Override
    protected boolean customBlockRemoval(LevelAccessor world, BlockPos pos, BlockState state) {
        return state.is(AllBlocks.MINECART_ANCHOR);
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        return true;
    }

    public void addExtraInventories(Entity cart) {
        if (cart instanceof Container inventory)
            storage.attachExternal(inventory);
    }


}
