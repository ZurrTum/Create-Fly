package com.zurrtum.create.content.contraptions.piston;

import com.zurrtum.create.*;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Locale;

public class MechanicalPistonBlock extends DirectionalAxisKineticBlock implements IBE<MechanicalPistonBlockEntity>, NeighborUpdateListeningBlock {

    public static final EnumProperty<PistonState> STATE = EnumProperty.of("state", PistonState.class);
    protected boolean isSticky;

    public static MechanicalPistonBlock normal(Settings properties) {
        return new MechanicalPistonBlock(properties, false);
    }

    public static MechanicalPistonBlock sticky(Settings properties) {
        return new MechanicalPistonBlock(properties, true);
    }

    protected MechanicalPistonBlock(Settings properties, boolean sticky) {
        super(properties);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(STATE, PistonState.RETRACTED));
        isSticky = sticky;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(STATE);
        super.appendProperties(builder);
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (!player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (player.isSneaking())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!stack.isIn(AllItemTags.SLIME_BALLS)) {
            if (stack.isEmpty()) {
                withBlockEntityDo(level, pos, be -> be.assembleNextTick = true);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (state.get(STATE) != PistonState.RETRACTED)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        Direction direction = state.get(FACING);
        if (hitResult.getSide() != direction)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (((MechanicalPistonBlock) state.getBlock()).isSticky)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient) {
            Vec3d vec = hitResult.getPos();
            level.addParticleClient(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
            return ActionResult.SUCCESS;
        }
        AllSoundEvents.SLIME_ADDED.playOnServer(level, pos, .5f, 1);
        if (!player.isCreative())
            stack.decrement(1);
        level.setBlockState(
            pos,
            AllBlocks.STICKY_MECHANICAL_PISTON.getDefaultState().with(FACING, direction)
                .with(AXIS_ALONG_FIRST_COORDINATE, state.get(AXIS_ALONG_FIRST_COORDINATE))
        );
        return ActionResult.SUCCESS;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        Direction direction = state.get(FACING);
        if (!fromPos.equals(pos.offset(direction.getOpposite())))
            return;
        if (!world.isClient && !world.getBlockTickScheduler().isTicking(pos, this))
            world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
        Direction direction = state.get(FACING);
        BlockState pole = worldIn.getBlockState(pos.offset(direction.getOpposite()));
        if (!pole.isOf(AllBlocks.PISTON_EXTENSION_POLE)) {
            if (pole.isOf(Blocks.AIR)) {
                withBlockEntityDo(
                    worldIn, pos, be -> {
                        if (be.running) {
                            return;
                        }
                        float speed = be.getSpeed();
                        if (speed == 0) {
                            return;
                        }
                        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, direction.getAxis());
                        Direction movementOppositeDirection = speed > 0 ^ direction.getAxis() != Direction.Axis.Z ? positive.getOpposite() : positive;
                        if (movementOppositeDirection == direction) {
                            be.assembleNextTick = true;
                        }
                    }
                );
            }
            return;
        }
        if (pole.get(PistonExtensionPoleBlock.FACING).getAxis() != direction.getAxis())
            return;
        withBlockEntityDo(
            worldIn, pos, be -> {
                if (!be.running) {
                    float speed = be.getSpeed();
                    if (speed != 0) {
                        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, direction.getAxis());
                        Direction movementDirection = speed > 0 ^ direction.getAxis() != Direction.Axis.Z ? positive : positive.getOpposite();
                        if (movementDirection == direction) {
                            be.assembleNextTick = true;
                        }
                    }
                }
                if (be.lastException == null)
                    return;
                be.lastException = null;
                be.sendData();
            }
        );
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (state.get(STATE) != PistonState.RETRACTED)
            return ActionResult.PASS;
        return super.onWrenched(state, context);
    }

    public enum PistonState implements StringIdentifiable {
        RETRACTED,
        MOVING,
        EXTENDED;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public BlockState onBreak(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        Direction direction = state.get(FACING);
        BlockPos pistonHead = null;
        BlockPos pistonBase = pos;
        boolean dropBlocks = player == null || !player.isCreative();

        Integer maxPoles = maxAllowedPistonPoles();
        for (int offset = 1; offset < maxPoles; offset++) {
            BlockPos currentPos = pos.offset(direction, offset);
            BlockState block = worldIn.getBlockState(currentPos);

            if (isExtensionPole(block) && direction.getAxis() == block.get(Properties.FACING).getAxis())
                continue;

            if (isPistonHead(block) && block.get(Properties.FACING) == direction) {
                pistonHead = currentPos;
            }

            break;
        }

        if (pistonHead != null && pistonBase != null) {
            BlockPos.stream(pistonBase, pistonHead).filter(p -> !p.equals(pos)).forEach(p -> worldIn.breakBlock(p, dropBlocks));
        }

        for (int offset = 1; offset < maxPoles; offset++) {
            BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
            BlockState block = worldIn.getBlockState(currentPos);

            if (isExtensionPole(block) && direction.getAxis() == block.get(Properties.FACING).getAxis()) {
                worldIn.breakBlock(currentPos, dropBlocks);
                continue;
            }

            break;
        }

        return super.onBreak(worldIn, pos, state, player);
    }

    public static int maxAllowedPistonPoles() {
        return AllConfigs.server().kinetics.maxPistonPoles.get();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {

        if (state.get(STATE) == PistonState.EXTENDED)
            return AllShapes.MECHANICAL_PISTON_EXTENDED.get(state.get(FACING));

        if (state.get(STATE) == PistonState.MOVING)
            return AllShapes.MECHANICAL_PISTON.get(state.get(FACING));

        return VoxelShapes.fullCube();
    }

    @Override
    public Class<MechanicalPistonBlockEntity> getBlockEntityClass() {
        return MechanicalPistonBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalPistonBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MECHANICAL_PISTON;
    }

    public static boolean isPiston(BlockState state) {
        return state.isOf(AllBlocks.MECHANICAL_PISTON) || isStickyPiston(state);
    }

    public static boolean isStickyPiston(BlockState state) {
        return state.isOf(AllBlocks.STICKY_MECHANICAL_PISTON);
    }

    public static boolean isExtensionPole(BlockState state) {
        return state.isOf(AllBlocks.PISTON_EXTENSION_POLE);
    }

    public static boolean isPistonHead(BlockState state) {
        return state.isOf(AllBlocks.MECHANICAL_PISTON_HEAD);
    }
}
