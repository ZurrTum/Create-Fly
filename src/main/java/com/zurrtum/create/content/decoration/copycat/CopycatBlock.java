package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class CopycatBlock extends Block implements IBE<CopycatBlockEntity>, IWrenchable, ResistanceControlBlock, SlipperinessControlBlock, EnchantingControlBlock, AppearanceControlBlock, SoundControlBlock, LightControlBlock, SelfEmissiveLightingBlock {

    public CopycatBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<S> p_153214_) {
        return null;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        onWrenched(state, context);
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return onBlockEntityUse(
            context.getLevel(), context.getClickedPos(), ufte -> {
                ItemStack consumedItem = ufte.getConsumedItem();
                if (!ufte.hasCustomMaterial())
                    return InteractionResult.PASS;
                Player player = context.getPlayer();
                if (!player.isCreative())
                    player.getInventory().placeItemBackInInventory(consumedItem);
                context.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, context.getClickedPos(), Block.getId(ufte.getBlockState()));
                ufte.setMaterial(AllBlocks.COPYCAT_BASE.defaultBlockState());
                ufte.setConsumedItem(ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (player == null)
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        Direction face = hitResult.getDirection();
        BlockState materialIn = getAcceptedBlockState(level, pos, stack, face);

        if (materialIn != null)
            materialIn = prepareMaterial(level, pos, state, player, hand, hitResult, materialIn);
        if (materialIn == null)
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        BlockState material = materialIn;
        return onBlockEntityUseItemOn(
            level, pos, ufte -> {
                if (ufte.getMaterial().is(material.getBlock())) {
                    if (!ufte.cycleMaterial())
                        return InteractionResult.TRY_WITH_EMPTY_HAND;
                    ufte.getLevel().playSound(null, ufte.getBlockPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .75f, .95f);
                    return InteractionResult.SUCCESS;
                }
                if (ufte.hasCustomMaterial())
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                if (level.isClientSide())
                    return InteractionResult.SUCCESS;

                ufte.setMaterial(material);
                ufte.setConsumedItem(stack);
                ufte.getLevel().playSound(null, ufte.getBlockPos(), material.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1, .75f);

                if (player.isCreative())
                    return InteractionResult.SUCCESS;

                stack.shrink(1);
                if (stack.isEmpty())
                    player.setItemInHand(hand, ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer == null)
            return;
        ItemStack offhandItem = pPlacer.getItemInHand(InteractionHand.OFF_HAND);
        BlockState appliedState = getAcceptedBlockState(pLevel, pPos, offhandItem, Direction.orderedByNearest(pPlacer)[0]);

        if (appliedState == null)
            return;
        withBlockEntityDo(
            pLevel, pPos, ufte -> {
                if (ufte.hasCustomMaterial())
                    return;

                ufte.setMaterial(appliedState);
                ufte.setConsumedItem(offhandItem);

                if (pPlacer instanceof Player player && player.isCreative())
                    return;
                offhandItem.shrink(1);
                if (offhandItem.isEmpty())
                    pPlacer.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        );
    }

    @Nullable
    public BlockState getAcceptedBlockState(@Nullable Level pLevel, BlockPos pPos, ItemStack item, @Nullable Direction face) {
        if (!(item.getItem() instanceof BlockItem bi))
            return null;

        Block block = bi.getBlock();
        if (block instanceof CopycatBlock)
            return null;

        BlockState appliedState = block.defaultBlockState();
        boolean hardCodedAllow = isAcceptedRegardless(appliedState);

        if (!appliedState.is(AllBlockTags.COPYCAT_ALLOW) && !hardCodedAllow) {

            if (appliedState.is(AllBlockTags.COPYCAT_DENY))
                return null;
            if (block instanceof EntityBlock)
                return null;
            if (block instanceof StairBlock)
                return null;

            if (pLevel != null) {
                VoxelShape shape = appliedState.getShape(pLevel, pPos);
                if (shape.isEmpty() || !shape.bounds().equals(Shapes.block().bounds()))
                    return null;

                VoxelShape collisionShape = appliedState.getCollisionShape(pLevel, pPos);
                if (collisionShape.isEmpty())
                    return null;
            }
        }

        if (face != null) {
            Axis axis = face.getAxis();

            if (appliedState.hasProperty(BlockStateProperties.FACING))
                appliedState = appliedState.setValue(BlockStateProperties.FACING, face);
            if (appliedState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && axis != Axis.Y)
                appliedState = appliedState.setValue(BlockStateProperties.HORIZONTAL_FACING, face);
            if (appliedState.hasProperty(BlockStateProperties.AXIS))
                appliedState = appliedState.setValue(BlockStateProperties.AXIS, axis);
            if (appliedState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS) && axis != Axis.Y)
                appliedState = appliedState.setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
        }

        return appliedState;
    }

    public boolean isAcceptedRegardless(BlockState material) {
        return false;
    }

    public BlockState prepareMaterial(
        Level pLevel,
        BlockPos pPos,
        BlockState pState,
        Player pPlayer,
        InteractionHand pHand,
        BlockHitResult pHit,
        BlockState material
    ) {
        return material;
    }

    @Override
    public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
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
        BlockAndTintGetter level,
        BlockPos pos,
        Direction side,
        @Nullable BlockState queryState,
        @Nullable BlockPos queryPos
    ) {
        if (isIgnoredConnectivitySide(level, state, side, pos, queryPos))
            return state;

        BlockState material = getMaterial(level, pos);
        return material != null ? material : AllBlocks.COPYCAT_BASE.defaultBlockState();
    }

    public boolean isIgnoredConnectivitySide(
        BlockAndTintGetter reader,
        BlockState state,
        Direction face,
        @Nullable BlockPos fromPos,
        @Nullable BlockPos toPos
    ) {
        return false;
    }

    public abstract boolean canConnectTexturesToward(BlockAndTintGetter reader, BlockPos fromPos, BlockPos toPos, BlockState state);

    //

    public static BlockState getMaterial(BlockGetter reader, BlockPos targetPos) {
        if (reader.getBlockEntity(targetPos) instanceof CopycatBlockEntity cbe)
            return cbe.getMaterial();
        return Blocks.AIR.defaultBlockState();
    }

    public boolean canFaceBeOccluded(BlockState state, Direction face) {
        return false;
    }

    public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
        return false;
    }

    // Wrapped properties

    @Override
    public SoundType getSoundGroup(LevelReader level, BlockPos pos) {
        return getMaterial(level, pos).getSoundType();
    }

    @Override
    public float getSlipperiness(LevelReader world, BlockPos pos) {
        BlockState state = getMaterial(world, pos);
        Block material = state.getBlock();
        if (material instanceof SlipperinessControlBlock block) {
            return block.getSlipperiness(world, pos);
        }
        return material.getFriction();
    }

    @Override
    public int getLuminance(BlockGetter world, BlockPos pos) {
        return getMaterial(world, pos).getLightEmission();
    }

    @Override
    public float getResistance(BlockGetter level, BlockPos pos) {
        return getMaterial(level, pos).getBlock().getExplosionResistance();
    }

    public static boolean hasEmissiveLighting(BlockState state, BlockGetter level, BlockPos pos) {
        return getMaterial(level, pos).emissiveRendering(level, pos);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        BlockState material = getMaterial(level, pos);
        if (material.is(AllBlocks.COPYCAT_BASE))
            return new ItemStack(this);
        return material.getCloneItemStack(level, pos, includeData);
    }

    @Override
    public BlockState getEnchantmentPowerProvider(Level world, BlockPos pos) {
        return getMaterial(world, pos);
    }

    @Override
    public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, double p_152430_) {
        BlockState material = getMaterial(pLevel, pPos);
        material.getBlock().fallOn(pLevel, material, pPos, pEntity, p_152430_);
    }

    @Override
    public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
        return getMaterial(pLevel, pPos).getDestroyProgress(pPlayer, pLevel, pPos);
    }
}