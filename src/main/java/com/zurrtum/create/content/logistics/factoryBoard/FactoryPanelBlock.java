package com.zurrtum.create.content.logistics.factoryBoard;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.BreakControlBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FactoryPanelBlock extends WallMountedBlock implements ProperWaterloggedBlock, IBE<FactoryPanelBlockEntity>, IWrenchable, SpecialBlockItemRequirement, BreakControlBlock {
    public static final MapCodec<FactoryPanelBlock> CODEC = createCodec(FactoryPanelBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    public FactoryPanelBlock(Settings p_53182_) {
        super(p_53182_);
        setDefaultState(getDefaultState().with(WATERLOGGED, false).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACE, FACING, WATERLOGGED, POWERED));
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return canAttachLenient(pLevel, pPos, getDirection(pState).getOpposite());
    }

    public static boolean canAttachLenient(WorldView pReader, BlockPos pPos, Direction pDirection) {
        BlockPos blockpos = pPos.offset(pDirection);
        return !pReader.getBlockState(blockpos).getCollisionShape(pReader, blockpos).isEmpty();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        if (stateForPlacement == null)
            return null;
        if (stateForPlacement.get(FACE) == BlockFace.FLOOR)
            stateForPlacement = stateForPlacement.with(FACING, stateForPlacement.get(FACING).getOpposite());

        World level = pContext.getWorld();
        BlockPos pos = pContext.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        FactoryPanelBlockEntity fpbe = getBlockEntity(level, pos);

        Vec3d location = pContext.getHitPos();
        if (blockState.isOf(this) && location != null && fpbe != null) {
            if (!level.isClient()) {
                PanelSlot targetedSlot = getTargetedSlot(pos, blockState, location);
                ItemStack panelItem = FactoryPanelBlockItem.fixCtrlCopiedStack(pContext.getStack());
                UUID networkFromStack = LogisticallyLinkedBlockItem.networkFromStack(panelItem);
                PlayerEntity pPlayer = pContext.getPlayer();

                if (fpbe.addPanel(targetedSlot, networkFromStack) && pPlayer != null) {
                    pPlayer.sendMessage(Text.translatable("create.logistically_linked.connected"), true);

                    if (!pPlayer.isCreative()) {
                        panelItem.decrement(1);
                        if (panelItem.isEmpty())
                            pPlayer.setStackInHand(pContext.getHand(), ItemStack.EMPTY);
                    }
                }
            }
            stateForPlacement = blockState;
        }

        return withWater(stateForPlacement, pContext);
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        PanelSlot slot = getTargetedSlot(pos, state, context.getHitPos());

        if (!(world instanceof ServerWorld))
            return ActionResult.SUCCESS;

        return onBlockEntityUse(
            world, pos, be -> {
                ServerFactoryPanelBehaviour behaviour = be.panels.get(slot);
                if (behaviour == null || !behaviour.isActive())
                    return ActionResult.SUCCESS;

                //TODO
                //                BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
                //                NeoForge.EVENT_BUS.post(event);
                //                if (event.isCanceled())
                //                    return ActionResult.SUCCESS;

                if (!be.removePanel(slot))
                    return ActionResult.SUCCESS;

                if (!player.isCreative())
                    player.getInventory().offerOrDrop(AllItems.FACTORY_GAUGE.getDefaultStack());

                IWrenchable.playRemoveSound(world, pos);
                if (be.activePanels() == 0)
                    world.breakBlock(pos, false);

                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        if (pPlacer == null)
            return;
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
        double range = pPlacer.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 1;
        HitResult hitResult = pPlacer.raycast(range, 1, false);
        Vec3d location = hitResult.getPos();
        if (location == null)
            return;
        PanelSlot initialSlot = getTargetedSlot(pPos, pState, location);
        withBlockEntityDo(pLevel, pPos, fpbe -> fpbe.addPanel(initialSlot, LogisticallyLinkedBlockItem.networkFromStack(pStack)));
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
        if (player == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient)
            return ActionResult.SUCCESS;
        if (!stack.isOf(AllItems.FACTORY_GAUGE))
            return ActionResult.SUCCESS;
        Vec3d location = hitResult.getPos();
        if (location == null)
            return ActionResult.SUCCESS;

        if (!FactoryPanelBlockItem.isTuned(stack)) {
            AllSoundEvents.DENY.playOnServer(level, pos);
            player.sendMessage(Text.translatable("create.factory_panel.tune_before_placing"), true);
            return ActionResult.FAIL;
        }

        PanelSlot newSlot = getTargetedSlot(pos, state, location);
        withBlockEntityDo(
            level, pos, fpbe -> {
                if (!fpbe.addPanel(newSlot, LogisticallyLinkedBlockItem.networkFromStack(FactoryPanelBlockItem.fixCtrlCopiedStack(stack))))
                    return;
                player.sendMessage(Text.translatable("create.logistically_linked.connected"), true);
                level.playSound(null, pos, soundGroup.getPlaceSound(), SoundCategory.BLOCKS);
                if (player.isCreative())
                    return;
                stack.decrement(1);
                if (stack.isEmpty())
                    player.setStackInHand(hand, ItemStack.EMPTY);
            }
        );
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, World level, BlockPos pos, PlayerEntity player) {
        return !tryDestroySubPanelFirst(state, level, pos, player);
    }

    private boolean tryDestroySubPanelFirst(BlockState state, World level, BlockPos pos, PlayerEntity player) {
        double range = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 1;
        HitResult hitResult = player.raycast(range, 1, false);
        Vec3d location = hitResult.getPos();
        PanelSlot destroyedSlot = getTargetedSlot(pos, state, location);
        return ActionResult.SUCCESS == onBlockEntityUse(
            level, pos, fpbe -> {
                if (fpbe.activePanels() < 2)
                    return ActionResult.FAIL;
                if (!fpbe.removePanel(destroyedSlot))
                    return ActionResult.FAIL;
                if (!player.isCreative())
                    dropStack(level, pos, AllItems.FACTORY_GAUGE.getDefaultStack());
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public boolean emitsRedstonePower(BlockState pState) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState pBlockState, BlockView pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.get(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState pBlockState, BlockView pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.get(POWERED) && getDirection(pBlockState) == pSide ? 15 : 0;
    }

    @Override
    public boolean canReplace(BlockState pState, ItemPlacementContext pUseContext) {
        if (!pUseContext.getStack().isOf(AllItems.FACTORY_GAUGE))
            return false;
        Vec3d location = pUseContext.getHitPos();

        BlockPos pos = pUseContext.getBlockPos();
        PanelSlot slot = getTargetedSlot(pos, pState, location);
        FactoryPanelBlockEntity blockEntity = getBlockEntity(pUseContext.getWorld(), pos);

        if (blockEntity == null)
            return false;
        return !blockEntity.panels.get(slot).isActive();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        if (pContext instanceof EntityShapeContext ecc && ecc.getEntity() == null)
            return getOutlineShape(pState, pLevel, pPos, pContext);
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        FactoryPanelBlockEntity blockEntity = getBlockEntity(pLevel, pPos);
        if (blockEntity != null)
            return blockEntity.getShape();
        return AllShapes.FACTORY_PANEL_FALLBACK.get(getDirection(pState));
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pFacing,
        BlockPos pFacingPos,
        BlockState pFacingState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return super.getStateForNeighborUpdate(pState, pLevel, tickView, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    public static Direction connectedDirection(BlockState state) {
        return getDirection(state);
    }

    public static PanelSlot getTargetedSlot(BlockPos pos, BlockState blockState, Vec3d clickLocation) {
        double bestDistance = Double.MAX_VALUE;
        PanelSlot bestSlot = PanelSlot.BOTTOM_LEFT;
        Vec3d localClick = clickLocation.subtract(Vec3d.of(pos));
        float xRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getXRot(blockState);
        float yRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getYRot(blockState);

        for (PanelSlot slot : PanelSlot.values()) {
            Vec3d vec = new Vec3d(.25 + slot.xOffset * .5, 0, .25 + slot.yOffset * .5);
            vec = VecHelper.rotateCentered(vec, 180, Axis.Y);
            vec = VecHelper.rotateCentered(vec, xRot + 90, Axis.X);
            vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);

            double diff = vec.squaredDistanceTo(localClick);
            if (diff > bestDistance)
                continue;
            bestDistance = diff;
            bestSlot = slot;
        }

        return bestSlot;
    }

    @Override
    public Class<FactoryPanelBlockEntity> getBlockEntityClass() {
        return FactoryPanelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FactoryPanelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.FACTORY_PANEL;
    }

    public static float getXRot(BlockState state) {
        BlockFace face = state.get(FactoryPanelBlock.FACE, BlockFace.FLOOR);
        return face == BlockFace.CEILING ? MathHelper.PI / 2 : face == BlockFace.FLOOR ? -MathHelper.PI / 2 : 0;
    }

    public static float getYRot(BlockState state) {
        Direction facing = state.get(FactoryPanelBlock.FACING, Direction.SOUTH);
        BlockFace face = state.get(FactoryPanelBlock.FACE, BlockFace.FLOOR);
        return (face == BlockFace.CEILING ? MathHelper.PI : 0) + AngleHelper.rad(AngleHelper.horizontalAngle(facing));
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        return ItemRequirement.NONE;
    }

    @Override
    protected @NotNull MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }
}
