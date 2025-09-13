package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

public abstract class CopycatBlock extends Block implements IBE<CopycatBlockEntity>, IWrenchable, ResistanceControlBlock, SlipperinessControlBlock, EnchantingControlBlock, AppearanceControlBlock, SoundControlBlock, LightControlBlock {

    public CopycatBlock(Settings pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(World p_153212_, BlockState p_153213_, BlockEntityType<S> p_153214_) {
        return null;
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        onWrenched(state, context);
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        return onBlockEntityUse(
            context.getWorld(), context.getBlockPos(), ufte -> {
                ItemStack consumedItem = ufte.getConsumedItem();
                if (!ufte.hasCustomMaterial())
                    return ActionResult.PASS;
                PlayerEntity player = context.getPlayer();
                if (!player.isCreative())
                    player.getInventory().offerOrDrop(consumedItem);
                context.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, context.getBlockPos(), Block.getRawIdFromState(ufte.getCachedState()));
                ufte.setMaterial(AllBlocks.COPYCAT_BASE.getDefaultState());
                ufte.setConsumedItem(ItemStack.EMPTY);
                return ActionResult.SUCCESS;
            }
        );
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

        Direction face = hitResult.getSide();
        BlockState materialIn = getAcceptedBlockState(level, pos, stack, face);

        if (materialIn != null)
            materialIn = prepareMaterial(level, pos, state, player, hand, hitResult, materialIn);
        if (materialIn == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        BlockState material = materialIn;
        return onBlockEntityUseItemOn(
            level, pos, ufte -> {
                if (ufte.getMaterial().isOf(material.getBlock())) {
                    if (!ufte.cycleMaterial())
                        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                    ufte.getWorld().playSound(null, ufte.getPos(), SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .75f, .95f);
                    return ActionResult.SUCCESS;
                }
                if (ufte.hasCustomMaterial())
                    return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                if (level.isClient())
                    return ActionResult.SUCCESS;

                ufte.setMaterial(material);
                ufte.setConsumedItem(stack);
                ufte.getWorld().playSound(null, ufte.getPos(), material.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1, .75f);

                if (player.isCreative())
                    return ActionResult.SUCCESS;

                stack.decrement(1);
                if (stack.isEmpty())
                    player.setStackInHand(hand, ItemStack.EMPTY);
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer == null)
            return;
        ItemStack offhandItem = pPlacer.getStackInHand(Hand.OFF_HAND);
        BlockState appliedState = getAcceptedBlockState(pLevel, pPos, offhandItem, Direction.getEntityFacingOrder(pPlacer)[0]);

        if (appliedState == null)
            return;
        withBlockEntityDo(
            pLevel, pPos, ufte -> {
                if (ufte.hasCustomMaterial())
                    return;

                ufte.setMaterial(appliedState);
                ufte.setConsumedItem(offhandItem);

                if (pPlacer instanceof PlayerEntity player && player.isCreative())
                    return;
                offhandItem.decrement(1);
                if (offhandItem.isEmpty())
                    pPlacer.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            }
        );
    }

    @Nullable
    public BlockState getAcceptedBlockState(World pLevel, BlockPos pPos, ItemStack item, Direction face) {
        if (!(item.getItem() instanceof BlockItem bi))
            return null;

        Block block = bi.getBlock();
        if (block instanceof CopycatBlock)
            return null;

        BlockState appliedState = block.getDefaultState();
        boolean hardCodedAllow = isAcceptedRegardless(appliedState);

        if (!appliedState.isIn(AllBlockTags.COPYCAT_ALLOW) && !hardCodedAllow) {

            if (appliedState.isIn(AllBlockTags.COPYCAT_DENY))
                return null;
            if (block instanceof BlockEntityProvider)
                return null;
            if (block instanceof StairsBlock)
                return null;

            if (pLevel != null) {
                VoxelShape shape = appliedState.getOutlineShape(pLevel, pPos);
                if (shape.isEmpty() || !shape.getBoundingBox().equals(VoxelShapes.fullCube().getBoundingBox()))
                    return null;

                VoxelShape collisionShape = appliedState.getCollisionShape(pLevel, pPos);
                if (collisionShape.isEmpty())
                    return null;
            }
        }

        if (face != null) {
            Axis axis = face.getAxis();

            if (appliedState.contains(Properties.FACING))
                appliedState = appliedState.with(Properties.FACING, face);
            if (appliedState.contains(Properties.HORIZONTAL_FACING) && axis != Axis.Y)
                appliedState = appliedState.with(Properties.HORIZONTAL_FACING, face);
            if (appliedState.contains(Properties.AXIS))
                appliedState = appliedState.with(Properties.AXIS, axis);
            if (appliedState.contains(Properties.HORIZONTAL_AXIS) && axis != Axis.Y)
                appliedState = appliedState.with(Properties.HORIZONTAL_AXIS, axis);
        }

        return appliedState;
    }

    public boolean isAcceptedRegardless(BlockState material) {
        return false;
    }

    public BlockState prepareMaterial(
        World pLevel,
        BlockPos pPos,
        BlockState pState,
        PlayerEntity pPlayer,
        Hand pHand,
        BlockHitResult pHit,
        BlockState material
    ) {
        return material;
    }

    @Override
    public BlockState onBreak(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
        super.onBreak(pLevel, pPos, pState, pPlayer);
        if (pPlayer.isCreative())
            withBlockEntityDo(pLevel, pPos, ufte -> ufte.setConsumedItem(ItemStack.EMPTY));
        return pState;
    }

    @Override
    public Class<CopycatBlockEntity> getBlockEntityClass() {
        return CopycatBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CopycatBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.COPYCAT;
    }

    // Connected Textures

    @Override
    public BlockState getAppearance(
        BlockState state,
        BlockRenderView level,
        BlockPos pos,
        Direction side,
        @Nullable BlockState queryState,
        @Nullable BlockPos queryPos
    ) {
        if (isIgnoredConnectivitySide(level, state, side, pos, queryPos))
            return state;

        BlockState material = getMaterial(level, pos);
        return material != null ? material : AllBlocks.COPYCAT_BASE.getDefaultState();
    }

    public boolean isIgnoredConnectivitySide(
        BlockRenderView reader,
        BlockState state,
        Direction face,
        @Nullable BlockPos fromPos,
        @Nullable BlockPos toPos
    ) {
        return false;
    }

    public abstract boolean canConnectTexturesToward(BlockRenderView reader, BlockPos fromPos, BlockPos toPos, BlockState state);

    //

    public static BlockState getMaterial(BlockView reader, BlockPos targetPos) {
        if (reader.getBlockEntity(targetPos) instanceof CopycatBlockEntity cbe)
            return cbe.getMaterial();
        return Blocks.AIR.getDefaultState();
    }

    public boolean canFaceBeOccluded(BlockState state, Direction face) {
        return false;
    }

    public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
        return false;
    }

    // Wrapped properties

    @Override
    public BlockSoundGroup getSoundGroup(WorldView level, BlockPos pos) {
        return getMaterial(level, pos).getSoundGroup();
    }

    @Override
    public float getSlipperiness(WorldView world, BlockPos pos) {
        BlockState state = getMaterial(world, pos);
        Block material = state.getBlock();
        if (material instanceof SlipperinessControlBlock block) {
            return block.getSlipperiness(world, pos);
        }
        return material.getSlipperiness();
    }

    @Override
    public int getLuminance(BlockView world, BlockPos pos) {
        return getMaterial(world, pos).getLuminance();
    }

    @Override
    public float getResistance(BlockView level, BlockPos pos) {
        return getMaterial(level, pos).getBlock().getBlastResistance();
    }

    @Override
    protected ItemStack getPickStack(WorldView level, BlockPos pos, BlockState state, boolean includeData) {
        BlockState material = getMaterial(level, pos);
        if (material.isOf(AllBlocks.COPYCAT_BASE))
            return new ItemStack(this);
        return material.getPickStack(level, pos, includeData);
    }

    @Override
    public BlockState getEnchantmentPowerProvider(World world, BlockPos pos) {
        return getMaterial(world, pos);
    }

    @Override
    public void onLandedUpon(World pLevel, BlockState pState, BlockPos pPos, Entity pEntity, double p_152430_) {
        BlockState material = getMaterial(pLevel, pPos);
        material.getBlock().onLandedUpon(pLevel, material, pPos, pEntity, p_152430_);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState pState, PlayerEntity pPlayer, BlockView pLevel, BlockPos pPos) {
        return getMaterial(pLevel, pPos).calcBlockBreakingDelta(pPlayer, pLevel, pPos);
    }
}