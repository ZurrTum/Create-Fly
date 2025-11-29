package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class StickerBlockEntity extends SmartBlockEntity {
    public LerpedFloat piston;
    public boolean update;

    public AbstractComputerBehaviour computerBehaviour;

    public StickerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.STICKER, pos, state);
        piston = LerpedFloat.linear();
        update = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!world.isClient())
            return;
        piston.startWithValue(isBlockStateExtended() ? 1 : 0);
    }

    public boolean isBlockStateExtended() {
        BlockState blockState = getCachedState();
        return blockState.isOf(AllBlocks.STICKER) && blockState.get(StickerBlock.EXTENDED);
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient())
            return;
        piston.tickChaser();

        if (isAttachedToBlock() && piston.getValue(0) != piston.getValue() && piston.getValue() == 1) {
            AllClientHandle.INSTANCE.spawnSuperGlueParticles(world, pos, getCachedState().get(StickerBlock.FACING), true);
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
        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.STICKER))
            return false;
        Direction direction = blockState.get(StickerBlock.FACING);
        return SuperGlueEntity.isValidFace(world, pos.offset(direction), direction.getOpposite());
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            update = true;
    }

    public void playSound(boolean attach) {
        AllSoundEvents.SLIME_ADDED.play(world, AllClientHandle.INSTANCE.getPlayer(), pos, 0.35f, attach ? 0.75f : 0.2f);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        computerBehaviour.removePeripheral();
    }
}
