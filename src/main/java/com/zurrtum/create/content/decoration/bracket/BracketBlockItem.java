package com.zurrtum.create.content.decoration.bracket;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class BracketBlockItem extends BlockItem {

    public BracketBlockItem(Block p_i48527_1_, Settings p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        BracketBlock bracketBlock = getBracketBlock();
        PlayerEntity player = context.getPlayer();

        BracketedBlockEntityBehaviour behaviour = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);

        if (behaviour == null)
            return ActionResult.FAIL;
        if (!behaviour.canHaveBracket())
            return ActionResult.FAIL;
        if (world.isClient)
            return ActionResult.SUCCESS;

        Optional<BlockState> suitableBracket = bracketBlock.getSuitableBracket(state, context.getSide());
        if (!suitableBracket.isPresent() && player != null)
            suitableBracket = bracketBlock.getSuitableBracket(state, Direction.getEntityFacingOrder(player)[0].getOpposite());
        if (!suitableBracket.isPresent())
            return ActionResult.SUCCESS;

        BlockState bracket = behaviour.getBracket();
        BlockState newBracket = suitableBracket.get();

        if (bracket == newBracket)
            return ActionResult.SUCCESS;

        world.playSound(null, pos, newBracket.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 0.75f, 1);
        behaviour.applyBracket(newBracket);

        if (player == null || !player.isCreative()) {
            context.getStack().decrement(1);
            if (bracket != null) {
                ItemStack returnedStack = new ItemStack(bracket.getBlock());
                if (player == null)
                    Block.dropStack(world, pos, returnedStack);
                else
                    player.getInventory().offerOrDrop(returnedStack);
            }
        }
        return ActionResult.SUCCESS;
    }

    private BracketBlock getBracketBlock() {
        return (BracketBlock) getBlock();
    }

}
