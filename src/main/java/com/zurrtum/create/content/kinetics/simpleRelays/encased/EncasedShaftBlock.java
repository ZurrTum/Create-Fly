package com.zurrtum.create.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.decoration.encasing.EncasedBlock;
import com.zurrtum.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;

public class EncasedShaftBlock extends AbstractEncasedShaftBlock implements IBE<KineticBlockEntity>, SpecialBlockItemRequirement, EncasedBlock {

    private final Block casing;

    public EncasedShaftBlock(Settings properties, Block casing) {
        super(properties);
        this.casing = casing;
    }

    public static EncasedShaftBlock andesite(Settings properties) {
        return new EncasedShaftBlock(properties, AllBlocks.ANDESITE_CASING);
    }

    public static EncasedShaftBlock brass(Settings properties) {
        return new EncasedShaftBlock(properties, AllBlocks.BRASS_CASING);
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        if (context.getWorld().isClient())
            return ActionResult.SUCCESS;
        context.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, context.getBlockPos(), Block.getRawIdFromState(state));
        KineticBlockEntity.switchToBlockState(
            context.getWorld(),
            context.getBlockPos(),
            AllBlocks.SHAFT.getDefaultState().with(AXIS, state.get(AXIS))
        );
        return ActionResult.SUCCESS;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return casing.asItem().getDefaultStack();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), be);
    }

    @Override
    public Class<KineticBlockEntity> getBlockEntityClass() {
        return KineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ENCASED_SHAFT;
    }

    @Override
    public Block getCasing() {
        return casing;
    }

    @Override
    public void handleEncasing(BlockState state, World level, BlockPos pos, ItemStack heldItem, PlayerEntity player, Hand hand, BlockHitResult ray) {
        KineticBlockEntity.switchToBlockState(
            level,
            pos,
            getDefaultState().with(RotatedPillarKineticBlock.AXIS, state.get(RotatedPillarKineticBlock.AXIS))
        );
    }
}
