package com.zurrtum.create.content.contraptions.mounted;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockEntity.CartMovementMode;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Queue;

import static com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock.RAIL_SHAPE;

public class MountedContraption extends Contraption {

    public CartMovementMode rotationMode;
    public AbstractMinecartEntity connectedCart;

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
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        BlockState state = world.getBlockState(pos);
        if (!state.contains(RAIL_SHAPE))
            return false;
        if (!searchMovedStructure(world, pos, null))
            return false;

        Axis axis = state.get(RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.X : Axis.Z;
        addBlock(
            world,
            pos,
            Pair.of(new StructureBlockInfo(pos, AllBlocks.MINECART_ANCHOR.getDefaultState().with(Properties.HORIZONTAL_AXIS, axis), null), null)
        );

        return blocks.size() != 1;
    }

    @Override
    protected boolean addToInitialFrontier(World world, BlockPos pos, Direction direction, Queue<BlockPos> frontier) {
        frontier.clear();
        frontier.add(pos.up());
        return true;
    }

    @Override
    protected Pair<StructureBlockInfo, BlockEntity> capture(World world, BlockPos pos) {
        Pair<StructureBlockInfo, BlockEntity> pair = super.capture(world, pos);
        StructureBlockInfo capture = pair.getKey();
        if (!capture.state().isOf(AllBlocks.CART_ASSEMBLER))
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
            for (AbstractMinecartEntity abstractMinecartEntity : world.getNonSpectatingEntities(AbstractMinecartEntity.class, new Box(pos))) {
                if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
                    break;
                connectedCart = abstractMinecartEntity;
                connectedCart.setPosition(pos.getX() + .5, pos.getY(), pos.getZ() + .5f);
            }
        }

        return anchorSwap;
    }

    @Override
    protected boolean movementAllowed(BlockState state, World world, BlockPos pos) {
        if (!pos.equals(anchor) && state.isOf(AllBlocks.CART_ASSEMBLER))
            return testSecondaryCartAssembler(world, pos);
        return super.movementAllowed(state, world, pos);
    }

    protected boolean testSecondaryCartAssembler(World world, BlockPos pos) {
        for (Axis axis : Iterate.axes) {
            if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
                continue;
            for (AbstractMinecartEntity abstractMinecartEntity : world.getNonSpectatingEntities(AbstractMinecartEntity.class, new Box(pos))) {
                if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
                    break;
                return true;
            }
        }
        return false;
    }

    @Override
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.put("RotationMode", CartMovementMode.CODEC, rotationMode);
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        view.read("RotationMode", CartMovementMode.CODEC).ifPresent(mode -> rotationMode = mode);
        super.read(world, view, spawnData);
    }

    @Override
    protected boolean customBlockPlacement(WorldAccess world, BlockPos pos, BlockState state) {
        return state.isOf(AllBlocks.MINECART_ANCHOR);
    }

    @Override
    protected boolean customBlockRemoval(WorldAccess world, BlockPos pos, BlockState state) {
        return state.isOf(AllBlocks.MINECART_ANCHOR);
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        return true;
    }

    public void addExtraInventories(Entity cart) {
        if (cart instanceof Inventory inventory)
            storage.attachExternal(inventory);
    }


}
