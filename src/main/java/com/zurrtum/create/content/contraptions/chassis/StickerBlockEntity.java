package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

public class StickerBlockEntity extends SmartBlockEntity {
    public LerpedFloat piston;
    public boolean update;

    public StickerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.STICKER, pos, state);
        piston = LerpedFloat.linear();
        update = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!level.isClientSide())
            return;
        piston.startWithValue(isBlockStateExtended() ? 1 : 0);
    }

    public boolean isBlockStateExtended() {
        BlockState blockState = getBlockState();
        return blockState.is(AllBlocks.STICKER) && blockState.getValue(StickerBlock.EXTENDED);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide())
            return;
        piston.tickChaser();

        if (isAttachedToBlock() && piston.getValue(0) != piston.getValue() && piston.getValue() == 1) {
            AllClientHandle.INSTANCE.spawnSuperGlueParticles(level, worldPosition, getBlockState().getValue(StickerBlock.FACING), true);
            playSound(true);
        }

        if (!update)
            return;
        update = false;
        int target = isBlockStateExtended() ? 1 : 0;
        if (isAttachedToBlock() && target == 0 && piston.getChaseTarget() == 1)
            playSound(false);
        piston.chase(target, .4f, Chaser.LINEAR);

        AllClientHandle.INSTANCE.queueUpdate(this);
    }

    public boolean isAttachedToBlock() {
        BlockState blockState = getBlockState();
        if (!blockState.is(AllBlocks.STICKER))
            return false;
        Direction direction = blockState.getValue(StickerBlock.FACING);
        return SuperGlueEntity.isValidFace(level, worldPosition.relative(direction), direction.getOpposite());
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            update = true;
    }

    public void playSound(boolean attach) {
        AllSoundEvents.SLIME_ADDED.play(level, AllClientHandle.INSTANCE.getPlayer(), worldPosition, 0.35f, attach ? 0.75f : 0.2f);
    }
}
