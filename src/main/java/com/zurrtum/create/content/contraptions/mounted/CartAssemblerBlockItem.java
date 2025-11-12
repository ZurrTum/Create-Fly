package com.zurrtum.create.content.contraptions.mounted;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.redstone.rail.ControllerRailBlock;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.NotNull;

public class CartAssemblerBlockItem extends BlockItem {

    public CartAssemblerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (tryPlaceAssembler(context)) {
            context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    public boolean tryPlaceAssembler(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Player player = context.getPlayer();

        if (player == null)
            return false;
        if (!(block instanceof BaseRailBlock abstractRailBlock)) {
            player.displayClientMessage(Component.translatable("create.block.cart_assembler.invalid"), true);
            return false;
        }

        RailShape shape = state.getValue(abstractRailBlock.getShapeProperty());
        if (shape != RailShape.EAST_WEST && shape != RailShape.NORTH_SOUTH)
            return false;

        BlockState newState = AllBlocks.CART_ASSEMBLER.defaultBlockState().setValue(CartAssemblerBlock.RAIL_SHAPE, shape);
        CartAssembleRailType newType = null;
        for (CartAssembleRailType type : CartAssembleRailType.values())
            if (type.matches(state))
                newType = type;
        if (newType == null)
            return false;
        if (world.isClientSide())
            return true;

        newState = newState.setValue(CartAssemblerBlock.RAIL_TYPE, newType);
        if (state.hasProperty(ControllerRailBlock.BACKWARDS))
            newState = newState.setValue(CartAssemblerBlock.BACKWARDS, state.getValue(ControllerRailBlock.BACKWARDS));
        else {
            Direction direction = player.getMotionDirection();
            newState = newState.setValue(CartAssemblerBlock.BACKWARDS, direction.getAxisDirection() == AxisDirection.POSITIVE);
        }

        world.setBlockAndUpdate(pos, newState);
        if (!player.isCreative())
            context.getItemInHand().shrink(1);

        AdvancementBehaviour.setPlacedBy(world, pos, player);
        return true;
    }
}
