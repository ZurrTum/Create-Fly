package com.zurrtum.create.content.kinetics.simpleRelays;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.content.equipment.wrench.IWrenchableWithBracket;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Optional;

public abstract class AbstractSimpleShaftBlock extends AbstractShaftBlock implements IWrenchableWithBracket {

    public AbstractSimpleShaftBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        return IWrenchableWithBracket.super.onWrenched(state, context);
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean isMoving) {
        if (!isMoving) {
            removeBracket(world, pos, true).ifPresent(stack -> Block.dropStack(world, pos, stack));
        }
    }

    @Override
    public Optional<ItemStack> removeBracket(BlockView world, BlockPos pos, boolean inOnReplacedContext) {
        BracketedBlockEntityBehaviour behaviour = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (behaviour == null)
            return Optional.empty();
        BlockState bracket = behaviour.removeBracket(inOnReplacedContext);
        if (bracket == null)
            return Optional.empty();
        return Optional.of(new ItemStack(bracket.getBlock()));
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BRACKETED_KINETIC;
    }

}
