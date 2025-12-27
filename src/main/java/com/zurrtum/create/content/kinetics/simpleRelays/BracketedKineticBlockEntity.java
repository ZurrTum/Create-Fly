package com.zurrtum.create.content.kinetics.simpleRelays;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BracketedKineticBlockEntity extends SimpleKineticBlockEntity implements TransformableBlockEntity {

    public BracketedKineticBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BRACKETED_KINETIC, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new BracketedBlockEntityBehaviour(this, state -> state.getBlock() instanceof AbstractSimpleShaftBlock));
        super.addBehaviours(behaviours);
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        BracketedBlockEntityBehaviour bracketBehaviour = getBehaviour(BracketedBlockEntityBehaviour.TYPE);
        if (bracketBehaviour != null) {
            bracketBehaviour.transformBracket(transform);
        }
    }

}
