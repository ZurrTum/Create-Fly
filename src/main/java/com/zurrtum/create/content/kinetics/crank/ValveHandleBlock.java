package com.zurrtum.create.content.kinetics.crank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.utility.BlockHelper;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ValveHandleBlock extends HandCrankBlock {

    public final DyeColor color;

    public static ValveHandleBlock copper(Properties properties) {
        return new ValveHandleBlock(properties, null);
    }

    public static Function<Properties, ValveHandleBlock> dyed(DyeColor color) {
        return properties -> new ValveHandleBlock(properties, color);
    }

    private ValveHandleBlock(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.VALVE_HANDLE.get(pState.getValue(FACING));
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

    public void clicked(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        useItemOn(heldItem, state, level, pos, player, hand, null);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack heldItem,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        DyeColor color = AllItemTags.getDyeColor(heldItem);

        if (color != null && color != this.color) {
            if (!level.isClientSide())
                level.setBlockAndUpdate(pos, BlockHelper.copyProperties(state, getColorBlock(color).defaultBlockState()));
            return InteractionResult.SUCCESS;
        }

        onBlockEntityUse(
            level,
            pos,
            hcbe -> (hcbe instanceof ValveHandleBlockEntity vhbe) && vhbe.activate(player.isShiftKeyDown()) ? InteractionResult.SUCCESS : InteractionResult.PASS
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public BlockEntityType<? extends HandCrankBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.VALVE_HANDLE;
    }
}
