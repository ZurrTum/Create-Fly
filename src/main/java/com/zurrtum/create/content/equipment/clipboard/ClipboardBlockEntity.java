package com.zurrtum.create.content.equipment.clipboard;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.component.ClipboardContent;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    public void onEditedBy(Player player) {
        lastEdit = player.getUUID();
        notifyUpdate();
        updateWrittenState();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level.isClientSide())
            AllClientHandle.INSTANCE.advertiseToAddressHelper(this);
    }

    public void updateWrittenState() {
        BlockState blockState = getBlockState();
        if (!blockState.is(AllBlocks.CLIPBOARD))
            return;
        if (level.isClientSide())
            return;
        boolean isWritten = blockState.getValue(ClipboardBlock.WRITTEN);
        boolean shouldBeWritten = components().has(AllDataComponents.CLIPBOARD_CONTENT);
        if (isWritten == shouldBeWritten)
            return;
        level.setBlockAndUpdate(worldPosition, blockState.setValue(ClipboardBlock.WRITTEN, shouldBeWritten));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket) {
            view.store("components", DataComponentMap.CODEC, components());
        }
        if (lastEdit != null)
            view.store("LastEdit", UUIDUtil.CODEC, lastEdit);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket) {
            view.read("components", DataComponentMap.CODEC).ifPresent(this::setComponents);
            UUID lastEdit = view.read("LastEdit", UUIDUtil.CODEC).orElse(null);
            AllClientHandle.INSTANCE.updateClipboardScreen(
                lastEdit,
                worldPosition,
                components().getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY)
            );
        }
    }
}
