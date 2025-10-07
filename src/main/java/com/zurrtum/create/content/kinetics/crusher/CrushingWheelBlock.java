package com.zurrtum.create.content.kinetics.crusher;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import static com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlock.VALID;

public class CrushingWheelBlock extends RotatedPillarKineticBlock implements IBE<CrushingWheelBlockEntity> {

    public CrushingWheelBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.CRUSHING_WHEEL_COLLISION_SHAPE;
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld worldIn, BlockPos pos, boolean isMoving) {
        for (Direction d : Iterate.directions) {
            if (d.getAxis() == state.get(AXIS))
                continue;
            if (worldIn.getBlockState(pos.offset(d)).isOf(AllBlocks.CRUSHING_WHEEL_CONTROLLER))
                worldIn.removeBlock(pos.offset(d), isMoving);
        }

        super.onStateReplaced(state, worldIn, pos, isMoving);
    }

    public void updateControllers(BlockState state, World world, BlockPos pos, Direction side) {
        if (side.getAxis() == state.get(AXIS))
            return;
        if (world == null)
            return;

        BlockPos controllerPos = pos.offset(side);
        BlockPos otherWheelPos = pos.offset(side, 2);

        boolean controllerExists = world.getBlockState(controllerPos).isOf(AllBlocks.CRUSHING_WHEEL_CONTROLLER);
        boolean controllerIsValid = controllerExists && world.getBlockState(controllerPos).get(VALID);
        Direction controllerOldDirection = controllerExists ? world.getBlockState(controllerPos).get(CrushingWheelControllerBlock.FACING) : null;

        boolean controllerShouldExist = false;
        boolean controllerShouldBeValid = false;
        Direction controllerNewDirection = Direction.DOWN;

        BlockState otherState = world.getBlockState(otherWheelPos);
        if (otherState.isOf(AllBlocks.CRUSHING_WHEEL)) {
            controllerShouldExist = true;

            CrushingWheelBlockEntity be = getBlockEntity(world, pos);
            CrushingWheelBlockEntity otherBE = getBlockEntity(world, otherWheelPos);

            if (be != null && otherBE != null && (be.getSpeed() > 0) != (otherBE.getSpeed() > 0) && be.getSpeed() != 0 && otherBE.getSpeed() != 0) {
                Axis wheelAxis = state.get(AXIS);
                Axis sideAxis = side.getAxis();
                int controllerADO = Math.round(Math.signum(be.getSpeed())) * side.getDirection().offset();
                Vec3d controllerDirVec = new Vec3d(
                    wheelAxis == Axis.X ? 1 : 0,
                    wheelAxis == Axis.Y ? 1 : 0,
                    wheelAxis == Axis.Z ? 1 : 0
                ).crossProduct(new Vec3d(sideAxis == Axis.X ? 1 : 0, sideAxis == Axis.Y ? 1 : 0, sideAxis == Axis.Z ? 1 : 0));

                controllerNewDirection = Direction.getFacing(
                    controllerDirVec.x * controllerADO,
                    controllerDirVec.y * controllerADO,
                    controllerDirVec.z * controllerADO
                );

                controllerShouldBeValid = true;
            }
            if (otherState.get(AXIS) != state.get(AXIS))
                controllerShouldExist = false;
        }

        if (!controllerShouldExist) {
            if (controllerExists)
                world.setBlockState(controllerPos, Blocks.AIR.getDefaultState());
            return;
        }

        if (!controllerExists) {
            if (!world.getBlockState(controllerPos).isReplaceable())
                return;
            world.setBlockState(
                controllerPos,
                AllBlocks.CRUSHING_WHEEL_CONTROLLER.getDefaultState().with(VALID, controllerShouldBeValid)
                    .with(CrushingWheelControllerBlock.FACING, controllerNewDirection)
            );
        } else if (controllerIsValid != controllerShouldBeValid || controllerOldDirection != controllerNewDirection) {
            world.setBlockState(
                controllerPos,
                world.getBlockState(controllerPos).with(VALID, controllerShouldBeValid)
                    .with(CrushingWheelControllerBlock.FACING, controllerNewDirection)
            );
        }

        AllBlocks.CRUSHING_WHEEL_CONTROLLER.updateSpeed(world.getBlockState(controllerPos), world, controllerPos);

    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn, EntityCollisionHandler handler, boolean bl) {
        if (entityIn.getY() < pos.getY() + 1.25f || !entityIn.isOnGround())
            return;

        float speed = getBlockEntityOptional(worldIn, pos).map(CrushingWheelBlockEntity::getSpeed).orElse(0f);

        double x = 0;
        double z = 0;

        if (state.get(AXIS) == Axis.X) {
            z = speed / 20f;
            x += (pos.getX() + .5f - entityIn.getX()) * .1f;
        }
        if (state.get(AXIS) == Axis.Z) {
            x = speed / -20f;
            z += (pos.getZ() + .5f - entityIn.getZ()) * .1f;
        }
        entityIn.setVelocity(entityIn.getVelocity().add(x, 0, z));
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        for (Direction direction : Iterate.directions) {
            BlockPos neighbourPos = pos.offset(direction);
            BlockState neighbourState = worldIn.getBlockState(neighbourPos);
            Axis stateAxis = state.get(AXIS);
            if (neighbourState.isOf(AllBlocks.CRUSHING_WHEEL_CONTROLLER) && direction.getAxis() != stateAxis)
                return false;
            if (!neighbourState.isOf(AllBlocks.CRUSHING_WHEEL))
                continue;
            if (neighbourState.get(AXIS) != stateAxis || stateAxis != direction.getAxis())
                return false;
        }

        return true;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS);
    }

    @Override
    public float getParticleTargetRadius() {
        return 1.125f;
    }

    @Override
    public float getParticleInitialRadius() {
        return 1f;
    }

    @Override
    public Class<CrushingWheelBlockEntity> getBlockEntityClass() {
        return CrushingWheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CrushingWheelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CRUSHING_WHEEL;
    }

}
