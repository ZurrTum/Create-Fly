package com.zurrtum.create.content.redstone.analogLever;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.block.IBE;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AnalogLeverBlock extends FaceAttachedHorizontalDirectionalBlock implements IBE<AnalogLeverBlockEntity> {

    public static final MapCodec<AnalogLeverBlock> CODEC = simpleCodec(AnalogLeverBlock::new);
    private final Function<BlockState, VoxelShape> shapeFunction;

    public AnalogLeverBlock(Properties p_i48402_1_) {
        super(p_i48402_1_);
        this.shapeFunction = this.createShapeFunction();
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        Map<AttachFace, Map<Direction, VoxelShape>> map = Shapes.rotateAttachFace(Block.boxZ(
            6.0,
            8.0,
            10.0,
            16.0
        ));
        return this.getShapeForEachState(state -> map.get(state.getValue(FACE)).get(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shapeFunction.apply(state);
    }

    public static boolean onBlockActivated(InteractionHand hand, BlockState state, ItemStack stack) {
        if (hand == InteractionHand.OFF_HAND || stack.is(AllItems.WRENCH)) {
            return false;
        }
        return state.getBlock() instanceof AnalogLeverBlock;
    }

    @Override
    public InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level worldIn,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (worldIn.isClientSide()) {
            addParticles(state, worldIn, pos, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return onBlockEntityUse(
            worldIn, pos, be -> {
                boolean sneak = player.isShiftKeyDown();
                be.changeState(sneak);
                float f = .25f + ((be.state + 5) / 15f) * .5f;
                worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.2F, f);
                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return getBlockEntityOptional(blockAccess, pos).map(al -> al.state).orElse(0);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return getConnectedDirection(blockState) == side ? getSignal(blockState, blockAccess, pos, side) : 0;
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        withBlockEntityDo(
            worldIn, pos, be -> {
                if (be.state != 0 && rand.nextFloat() < 0.25F)
                    addParticles(stateIn, worldIn, pos, 0.5F);
            }
        );
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving) {
        if (isMoving)
            return;
        withBlockEntityDo(
            worldIn, pos, be -> {
                if (be.state != 0)
                    updateNeighbors(state, worldIn, pos);
                worldIn.removeBlockEntity(pos);
            }
        );
    }

    private static void addParticles(BlockState state, LevelAccessor worldIn, BlockPos pos, float alpha) {
        Direction direction = state.getValue(FACING).getOpposite();
        Direction direction1 = getConnectedDirection(state).getOpposite();
        double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getStepX() + 0.2D * (double) direction1.getStepX();
        double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getStepY() + 0.2D * (double) direction1.getStepY();
        double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getStepZ() + 0.2D * (double) direction1.getStepZ();
        worldIn.addParticle(new DustParticleOptions(0xFF0000, alpha), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    static void updateNeighbors(BlockState state, Level world, BlockPos pos) {
        world.updateNeighborsAt(pos, state.getBlock(), null);
        world.updateNeighborsAt(pos.relative(getConnectedDirection(state).getOpposite()), state.getBlock(), null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, FACE));
    }

    @Override
    public Class<AnalogLeverBlockEntity> getBlockEntityClass() {
        return AnalogLeverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AnalogLeverBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ANALOG_LEVER;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected @NotNull MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
