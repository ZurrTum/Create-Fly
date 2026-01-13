package com.zurrtum.create.content.kinetics.crank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.block.BlockEntityKeepBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.Function;

public class ValveHandleBlock extends HandCrankBlock implements BlockEntityKeepBlock {

    public final DyeColor color;

    public static ValveHandleBlock copper(Settings properties) {
        return new ValveHandleBlock(properties, null);
    }

    public static Function<Settings, ValveHandleBlock> dyed(DyeColor color) {
        return properties -> new ValveHandleBlock(properties, color);
    }

    private ValveHandleBlock(Settings properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @Override
    public boolean keepBlockEntityWhenReplacedWith(BlockState state) {
        return AllBlockEntityTypes.VALVE_HANDLE.supports(state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.VALVE_HANDLE.get(pState.get(FACING));
    }

    public static ValveHandleBlock getColorBlock(DyeColor color) {
        return switch (color) {
            case null -> AllBlocks.COPPER_VALVE_HANDLE;
            case WHITE -> AllBlocks.WHITE_VALVE_HANDLE;
            case ORANGE -> AllBlocks.ORANGE_VALVE_HANDLE;
            case MAGENTA -> AllBlocks.MAGENTA_VALVE_HANDLE;
            case LIGHT_BLUE -> AllBlocks.LIGHT_BLUE_VALVE_HANDLE;
            case YELLOW -> AllBlocks.YELLOW_VALVE_HANDLE;
            case LIME -> AllBlocks.LIME_VALVE_HANDLE;
            case PINK -> AllBlocks.PINK_VALVE_HANDLE;
            case GRAY -> AllBlocks.GRAY_VALVE_HANDLE;
            case LIGHT_GRAY -> AllBlocks.LIGHT_GRAY_VALVE_HANDLE;
            case CYAN -> AllBlocks.CYAN_VALVE_HANDLE;
            case PURPLE -> AllBlocks.PURPLE_VALVE_HANDLE;
            case BLUE -> AllBlocks.BLUE_VALVE_HANDLE;
            case BROWN -> AllBlocks.BROWN_VALVE_HANDLE;
            case GREEN -> AllBlocks.GREEN_VALVE_HANDLE;
            case RED -> AllBlocks.RED_VALVE_HANDLE;
            case BLACK -> AllBlocks.BLACK_VALVE_HANDLE;
        };
    }

    public void clicked(World level, BlockPos pos, BlockState state, PlayerEntity player, Hand hand) {
        ItemStack heldItem = player.getStackInHand(hand);
        onUseWithItem(heldItem, state, level, pos, player, hand, null);
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack heldItem,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        DyeColor color = AllItemTags.getDyeColor(heldItem);

        if (color != null && color != this.color) {
            if (!level.isClient)
                level.setBlockState(pos, BlockHelper.copyProperties(state, getColorBlock(color).getDefaultState()));
            return ActionResult.SUCCESS;
        }

        onBlockEntityUse(
            level,
            pos,
            hcbe -> (hcbe instanceof ValveHandleBlockEntity vhbe) && vhbe.activate(player.isSneaking()) ? ActionResult.SUCCESS : ActionResult.PASS
        );
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    public BlockEntityType<? extends HandCrankBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.VALVE_HANDLE;
    }
}
