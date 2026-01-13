package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper.ExtractionCountMode;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class SmartChuteBlockEntity extends ChuteBlockEntity {

    ServerFilteringBehaviour filtering;

    public SmartChuteBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SMART_CHUTE, pos, state);
    }

    @Override
    protected boolean canAcceptItem(ItemStack stack) {
        return super.canAcceptItem(stack) && canActivate() && filtering.test(stack);
    }

    @Override
    protected int getExtractionAmount() {
        return filtering.isCountVisible() && !filtering.anyAmount() ? filtering.getAmount() : 64;
    }

    @Override
    protected ExtractionCountMode getExtractionMode() {
        return filtering.isCountVisible() && !filtering.anyAmount() && !filtering.upTo ? ExtractionCountMode.EXACTLY : ExtractionCountMode.UPTO;
    }

    @Override
    protected boolean canActivate() {
        BlockState blockState = getCachedState();
        return blockState.contains(SmartChuteBlock.POWERED) && !blockState.get(SmartChuteBlock.POWERED);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this).showCountWhen(this::isExtracting).withCallback($ -> invVersionTracker.reset()));
        super.addBehaviours(behaviours);
    }

    private boolean isExtracting() {
        boolean up = getItemMotion() < 0;
        BlockPos chutePos = pos.offset(up ? Direction.UP : Direction.DOWN);
        BlockState blockState = world.getBlockState(chutePos);
        return !AbstractChuteBlock.isChute(blockState) && !blockState.isReplaceable();
    }

}
