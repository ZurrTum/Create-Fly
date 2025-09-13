package com.zurrtum.create.content.equipment.clipboard;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public class ClipboardBlockEntity extends SmartBlockEntity {

    public ItemStack dataContainer;
    private UUID lastEdit;

    public ClipboardBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CLIPBOARD, pos, state);
        dataContainer = AllItems.CLIPBOARD.getDefaultStack();
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
        boolean shouldBeWritten = !dataContainer.getComponentChanges().isEmpty();
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
        if (!dataContainer.isEmpty()) {
            view.put("Item", ItemStack.CODEC, dataContainer);
        }
        if (clientPacket && lastEdit != null)
            view.put("LastEdit", Uuids.INT_STREAM_CODEC, lastEdit);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        dataContainer = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (!dataContainer.isOf(AllItems.CLIPBOARD))
            dataContainer = AllItems.CLIPBOARD.getDefaultStack();

        if (clientPacket) {
            UUID lastEdit = view.read("LastEdit", Uuids.INT_STREAM_CODEC).orElse(null);
            AllClientHandle.INSTANCE.updateClipboardScreen(lastEdit, pos, dataContainer);
        }
    }
}
