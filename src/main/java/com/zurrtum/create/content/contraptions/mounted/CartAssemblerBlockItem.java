package com.zurrtum.create.content.contraptions.mounted;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.redstone.rail.ControllerRailBlock;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CartAssemblerBlockItem extends BlockItem {

    public CartAssemblerBlockItem(Block block, Settings properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (tryPlaceAssembler(context)) {
            context.getWorld().playSound(null, context.getBlockPos(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    public boolean tryPlaceAssembler(ItemUsageContext context) {
        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        PlayerEntity player = context.getPlayer();

        if (player == null)
            return false;
        if (!(block instanceof AbstractRailBlock abstractRailBlock)) {
            player.sendMessage(Text.translatable("create.block.cart_assembler.invalid"), true);
            return false;
        }

        RailShape shape = state.get(abstractRailBlock.getShapeProperty());
        if (shape != RailShape.EAST_WEST && shape != RailShape.NORTH_SOUTH)
            return false;

        BlockState newState = AllBlocks.CART_ASSEMBLER.getDefaultState().with(CartAssemblerBlock.RAIL_SHAPE, shape);
        CartAssembleRailType newType = null;
        for (CartAssembleRailType type : CartAssembleRailType.values())
            if (type.matches(state))
                newType = type;
        if (newType == null)
            return false;
        if (world.isClient())
            return true;

        newState = newState.with(CartAssemblerBlock.RAIL_TYPE, newType);
        if (state.contains(ControllerRailBlock.BACKWARDS))
            newState = newState.with(CartAssemblerBlock.BACKWARDS, state.get(ControllerRailBlock.BACKWARDS));
        else {
            Direction direction = player.getMovementDirection();
            newState = newState.with(CartAssemblerBlock.BACKWARDS, direction.getDirection() == AxisDirection.POSITIVE);
        }

        world.setBlockState(pos, newState);
        if (!player.isCreative())
            context.getStack().decrement(1);

        AdvancementBehaviour.setPlacedBy(world, pos, player);
        return true;
    }
}
