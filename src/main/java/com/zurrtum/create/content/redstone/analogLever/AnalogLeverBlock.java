package com.zurrtum.create.content.redstone.analogLever;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

public class AnalogLeverBlock extends WallMountedBlock implements IBE<AnalogLeverBlockEntity> {

    public static final MapCodec<AnalogLeverBlock> CODEC = createCodec(AnalogLeverBlock::new);
    private final Function<BlockState, VoxelShape> shapeFunction;

    public AnalogLeverBlock(Settings p_i48402_1_) {
        super(p_i48402_1_);
        this.shapeFunction = this.createShapeFunction();
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        Map<BlockFace, Map<Direction, VoxelShape>> map = VoxelShapes.createBlockFaceHorizontalFacingShapeMap(Block.createCuboidZShape(
            6.0,
            8.0,
            10.0,
            16.0
        ));
        return this.createShapeFunction(state -> map.get(state.get(FACE)).get(state.get(FACING)));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeFunction.apply(state);
    }

    public static boolean onBlockActivated(Hand hand, BlockState state, ItemStack stack) {
        if (hand == Hand.OFF_HAND || stack.isOf(AllItems.WRENCH)) {
            return false;
        }
        return state.getBlock() instanceof AnalogLeverBlock;
    }

    @Override
    public ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World worldIn,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (worldIn.isClient) {
            addParticles(state, worldIn, pos, 1.0F);
            return ActionResult.SUCCESS;
        }

        return onBlockEntityUse(
            worldIn, pos, be -> {
                boolean sneak = player.isSneaking();
                be.changeState(sneak);
                float f = .25f + ((be.state + 5) / 15f) * .5f;
                worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.2F, f);
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return getBlockEntityOptional(blockAccess, pos).map(al -> al.state).orElse(0);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return getDirection(blockState) == side ? getWeakRedstonePower(blockState, blockAccess, pos, side) : 0;
    }

    @Override
    public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        withBlockEntityDo(
            worldIn, pos, be -> {
                if (be.state != 0 && rand.nextFloat() < 0.25F)
                    addParticles(stateIn, worldIn, pos, 0.5F);
            }
        );
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld worldIn, BlockPos pos, boolean isMoving) {
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

    private static void addParticles(BlockState state, WorldAccess worldIn, BlockPos pos, float alpha) {
        Direction direction = state.get(FACING).getOpposite();
        Direction direction1 = getDirection(state).getOpposite();
        double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getOffsetX() + 0.2D * (double) direction1.getOffsetX();
        double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getOffsetY() + 0.2D * (double) direction1.getOffsetY();
        double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getOffsetZ() + 0.2D * (double) direction1.getOffsetZ();
        worldIn.addParticleClient(new DustParticleEffect(0xFF0000, alpha), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    static void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAlways(pos, state.getBlock(), null);
        world.updateNeighborsAlways(pos.offset(getDirection(state).getOpposite()), state.getBlock(), null);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(FACING, FACE));
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected @NotNull MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }
}
