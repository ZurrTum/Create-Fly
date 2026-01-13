package com.zurrtum.create.content.equipment.clipboard;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public class ClipboardBlockEntity extends SmartBlockEntity {
    private UUID lastEdit;

    public ClipboardBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CLIPBOARD, pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateWrittenState();
    }

    public void onEditedBy(PlayerEntity player) {
        lastEdit = player.getUuid();
        notifyUpdate();
        updateWrittenState();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (world.isClient())
            AllClientHandle.INSTANCE.advertiseToAddressHelper(this);
    }

    public void updateWrittenState() {
        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.CLIPBOARD))
            return;
        if (world.isClient())
            return;
        boolean isWritten = blockState.get(ClipboardBlock.WRITTEN);
        boolean shouldBeWritten = getComponents().contains(AllDataComponents.CLIPBOARD_CONTENT);
        if (isWritten == shouldBeWritten)
            return;
        world.setBlockState(pos, blockState.with(ClipboardBlock.WRITTEN, shouldBeWritten));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket) {
            view.put("components", ComponentMap.CODEC, getComponents());
        }
        if (lastEdit != null)
            view.put("LastEdit", Uuids.INT_STREAM_CODEC, lastEdit);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket) {
            view.read("components", ComponentMap.CODEC).ifPresent(this::setComponents);
            UUID lastEdit = view.read("LastEdit", Uuids.INT_STREAM_CODEC).orElse(null);
            AllClientHandle.INSTANCE.updateClipboardScreen(
                lastEdit,
                pos,
                getComponents().getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY)
            );
        }
    }
}
