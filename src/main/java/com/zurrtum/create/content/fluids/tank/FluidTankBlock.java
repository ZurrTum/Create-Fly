package com.zurrtum.create.content.fluids.tank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeFluidTankInventory;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.blockEntity.ComparatorUtil;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.fluid.FluidHelper.FluidExchange;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Locale;

public class FluidTankBlock extends Block implements IWrenchable, IBE<FluidTankBlockEntity>, FluidInventoryProvider<FluidTankBlockEntity> {
    public static final BooleanProperty TOP = BooleanProperty.of("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.of("bottom");
    public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);
    public static final IntProperty LIGHT_LEVEL = Properties.LEVEL_15;

    private boolean creative;

    public static FluidTankBlock regular(Settings p_i48440_1_) {
        return new FluidTankBlock(p_i48440_1_, false);
    }

    public static FluidTankBlock creative(Settings p_i48440_1_) {
        return new FluidTankBlock(p_i48440_1_, true);
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    protected FluidTankBlock(Settings p_i48440_1_, boolean creative) {
        super(p_i48440_1_);
        this.creative = creative;
        setDefaultState(getDefaultState().with(TOP, true).with(BOTTOM, true).with(SHAPE, Shape.WINDOW).with(LIGHT_LEVEL, 0));
    }

    public static boolean isTank(BlockState state) {
        return state.getBlock() instanceof FluidTankBlock;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        withBlockEntityDo(world, pos, FluidTankBlockEntity::updateConnectivity);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> p_206840_1_) {
        p_206840_1_.add(TOP, BOTTOM, SHAPE, LIGHT_LEVEL);
    }

    public static int getLight(BlockState state) {
        return state.get(LIGHT_LEVEL);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        withBlockEntityDo(context.getWorld(), context.getBlockPos(), FluidTankBlockEntity::toggleWindows);
        return ActionResult.SUCCESS;
    }

    static final VoxelShape CAMPFIRE_SMOKE_CLIP = Block.createCuboidShape(0, 4, 0, 16, 16, 16);

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        if (pContext == ShapeContext.absent())
            return CAMPFIRE_SMOKE_CLIP;
        return pState.getOutlineShape(pLevel, pPos);
    }

    @Override
    public VoxelShape getSidesShape(BlockState pState, BlockView pReader, BlockPos pPos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        if (pDirection == Direction.DOWN && pNeighborState.getBlock() != this)
            withBlockEntityDo(pLevel, pCurrentPos, FluidTankBlockEntity::updateBoilerTemperature);
        return pState;
    }

    @Override
    public FluidInventory getFluidInventory(WorldAccess world, BlockPos pos, BlockState state, FluidTankBlockEntity blockEntity, Direction context) {
        if (blockEntity.fluidCapability == null) {
            blockEntity.refreshCapability();
        }
        return blockEntity.fluidCapability;
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
        boolean onClient = level.isClient();

        if (stack.isEmpty())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!player.isCreative() && !creative)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        FluidExchange exchange = null;
        FluidTankBlockEntity be = ConnectivityHandler.partAt(getBlockEntityType(), level, pos);
        if (be == null)
            return ActionResult.FAIL;

        if (!(state.getBlock() instanceof FluidInventoryProvider<?> provider)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        FluidInventory tankCapability = provider.getFluidInventory(state, level, pos, be, null);
        if (tankCapability == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        FluidStack prevFluidInTank = tankCapability.getStack(0).copy();

        if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be))
            exchange = FluidExchange.ITEM_TO_TANK;
        else if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be))
            exchange = FluidExchange.TANK_TO_ITEM;

        if (exchange == null) {
            if (GenericItemEmptying.canItemBeEmptied(level, stack) || GenericItemFilling.canItemBeFilled(level, stack))
                return ActionResult.SUCCESS;
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        SoundEvent soundevent = null;
        BlockState fluidState = null;
        FluidStack fluidInTank = tankCapability.getStack(0);

        if (exchange == FluidExchange.ITEM_TO_TANK) {
            if (creative && !onClient) {
                FluidStack fluidInItem = GenericItemEmptying.emptyItem(level, stack, true).getFirst();
                if (!fluidInItem.isEmpty() && tankCapability instanceof CreativeFluidTankInventory) {
                    tankCapability.setStack(0, fluidInItem);
                    tankCapability.markDirty();
                }
            }

            Fluid fluid = fluidInTank.getFluid();
            fluidState = fluid.getDefaultState().getBlockState();
            soundevent = FluidHelper.getEmptySound(fluidInTank);
        }

        if (exchange == FluidExchange.TANK_TO_ITEM) {
            if (creative && !onClient)
                if (tankCapability instanceof CreativeFluidTankInventory)
                    tankCapability.setStack(0, FluidStack.EMPTY);

            Fluid fluid = prevFluidInTank.getFluid();
            fluidState = fluid.getDefaultState().getBlockState();
            soundevent = FluidHelper.getFillSound(prevFluidInTank);
        }

        if (soundevent != null && !onClient) {
            float pitch = MathHelper.clamp(1 - (1f * fluidInTank.getAmount() / (FluidTankBlockEntity.getCapacityMultiplier() * 16)), 0, 1);
            pitch /= 1.5f;
            pitch += .5f;
            pitch += (level.random.nextFloat() - .5f) / 4f;
            level.playSound(null, pos, soundevent, SoundCategory.BLOCKS, .5f, pitch);
        }

        if (!FluidStack.areFluidsAndComponentsEqual(fluidInTank, prevFluidInTank)) {
            FluidTankBlockEntity controllerBE = be.getControllerBE();
            if (controllerBE != null) {
                if (fluidState != null && onClient) {
                    BlockStateParticleEffect blockParticleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, fluidState);
                    float fluidLevel = (float) fluidInTank.getAmount() / tankCapability.getMaxAmountPerStack();

                    //TODO
                    //                    boolean reversed = fluidInTank.getFluid()
                    //                        .getFluidType()
                    //                        .isLighterThanAir();
                    if (false/*reversed*/)
                        fluidLevel = 1 - fluidLevel;

                    Vec3d vec = hitResult.getPos();
                    vec = new Vec3d(vec.x, controllerBE.getPos().getY() + fluidLevel * (controllerBE.height - .5f) + .25f, vec.z);
                    Vec3d motion = player.getEntityPos().subtract(vec).multiply(1 / 20f);
                    vec = vec.add(motion);
                    level.addParticleClient(blockParticleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
                    return ActionResult.SUCCESS;
                }

                controllerBE.sendDataImmediately();
                controllerBE.markDirty();
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public Class<FluidTankBlockEntity> getBlockEntityClass() {
        return FluidTankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
        return creative ? AllBlockEntityTypes.CREATIVE_FLUID_TANK : AllBlockEntityTypes.FLUID_TANK;
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        if (mirror == BlockMirror.NONE)
            return state;
        boolean x = mirror == BlockMirror.FRONT_BACK;
        switch (state.get(SHAPE)) {
            case WINDOW_NE:
                return state.with(SHAPE, x ? Shape.WINDOW_NW : Shape.WINDOW_SE);
            case WINDOW_NW:
                return state.with(SHAPE, x ? Shape.WINDOW_NE : Shape.WINDOW_SW);
            case WINDOW_SE:
                return state.with(SHAPE, x ? Shape.WINDOW_SW : Shape.WINDOW_NE);
            case WINDOW_SW:
                return state.with(SHAPE, x ? Shape.WINDOW_SE : Shape.WINDOW_NW);
            default:
                return state;
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        for (int i = 0; i < rotation.ordinal(); i++)
            state = rotateOnce(state);
        return state;
    }

    private BlockState rotateOnce(BlockState state) {
        switch (state.get(SHAPE)) {
            case WINDOW_NE:
                return state.with(SHAPE, Shape.WINDOW_SE);
            case WINDOW_NW:
                return state.with(SHAPE, Shape.WINDOW_NE);
            case WINDOW_SE:
                return state.with(SHAPE, Shape.WINDOW_SW);
            case WINDOW_SW:
                return state.with(SHAPE, Shape.WINDOW_NW);
            default:
                return state;
        }
    }

    public enum Shape implements StringIdentifiable {
        PLAIN,
        WINDOW,
        WINDOW_NW,
        WINDOW_SW,
        WINDOW_NE,
        WINDOW_SE;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos, Direction direction) {
        return getBlockEntityOptional(worldIn, pos).map(FluidTankBlockEntity::getControllerBE)
            .map(be -> ComparatorUtil.fractionToRedstoneLevel(be.getFillState())).orElse(0);
    }

    public static void updateBoilerState(BlockState pState, World pLevel, BlockPos tankPos) {
        BlockState tankState = pLevel.getBlockState(tankPos);
        if (!(tankState.getBlock() instanceof FluidTankBlock tank))
            return;
        FluidTankBlockEntity tankBE = tank.getBlockEntity(pLevel, tankPos);
        if (tankBE == null)
            return;
        FluidTankBlockEntity controllerBE = tankBE.getControllerBE();
        if (controllerBE == null)
            return;
        controllerBE.updateBoilerState();
    }

}
