package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper.ExtractionCountMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SmartChuteBlockEntity extends ChuteBlockEntity implements Clearable {

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
        BlockState blockState = getBlockState();
        return blockState.hasProperty(SmartChuteBlock.POWERED) && !blockState.getValue(SmartChuteBlock.POWERED);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this).showCountWhen(this::isExtracting).withCallback($ -> invVersionTracker.reset()));
        super.addBehaviours(behaviours);
    }

    @Override
    public void clearContent() {
        filtering.setFilter(ItemStack.EMPTY);
    }

    private boolean isExtracting() {
        boolean up = getItemMotion() < 0;
        BlockPos chutePos = worldPosition.relative(up ? Direction.UP : Direction.DOWN);
        BlockState blockState = level.getBlockState(chutePos);
        return !AbstractChuteBlock.isChute(blockState) && !blockState.canBeReplaced();
    }
}
