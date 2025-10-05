package com.zurrtum.create.content.redstone.link.controller;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.ArrayList;

public class LecternControllerBlock extends LecternBlock implements IBE<LecternControllerBlockEntity>, SpecialBlockItemRequirement {

    public LecternControllerBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(HAS_BOOK, true));
    }

    @Override
    public Class<LecternControllerBlockEntity> getBlockEntityClass() {
        return LecternControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LecternControllerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.LECTERN_CONTROLLER;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.createBlockEntity(pos, state);
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
        if (!player.isSneaking() && LecternControllerBlockEntity.playerInRange(player, level, pos)) {
            if (!level.isClient())
                withBlockEntityDo(level, pos, be -> be.tryStartUsing(player));
            return ActionResult.SUCCESS;
        }

        if (player.isSneaking()) {
            if (!level.isClient())
                replaceWithLectern(state, level, pos);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 15;
    }

    public void replaceLectern(BlockState lecternState, World world, BlockPos pos, ItemStack controller) {
        world.setBlockState(pos, getDefaultState().with(FACING, lecternState.get(FACING)).with(POWERED, lecternState.get(POWERED)));
        withBlockEntityDo(world, pos, be -> be.setController(controller));
    }

    public void replaceWithLectern(BlockState state, World world, BlockPos pos) {
        AllSoundEvents.CONTROLLER_TAKE.playOnServer(world, pos);
        world.setBlockState(pos, Blocks.LECTERN.getDefaultState().with(FACING, state.get(FACING)).with(POWERED, state.get(POWERED)));
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return Items.LECTERN.getDefaultStack();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        ArrayList<ItemStack> requiredItems = new ArrayList<>();
        requiredItems.add(new ItemStack(Blocks.LECTERN));
        requiredItems.add(new ItemStack(AllItems.LINKED_CONTROLLER));
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, requiredItems);
    }
}
