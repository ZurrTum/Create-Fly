package com.zurrtum.create.content.logistics.factoryBoard;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class FactoryPanelBlockItem extends LogisticallyLinkedBlockItem {

    public FactoryPanelBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public ActionResult place(ItemPlacementContext pContext) {
        ItemStack stack = pContext.getStack();

        if (!isTuned(stack)) {
            AllSoundEvents.DENY.playOnServer(pContext.getWorld(), pContext.getBlockPos());
            pContext.getPlayer().sendMessage(Text.translatable("create.factory_panel.tune_before_placing"), true);
            return ActionResult.FAIL;
        }

        return super.place(pContext);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World level, PlayerEntity player, ItemStack stack, BlockState state) {
        return super.postPlacement(pos, level, player, fixCtrlCopiedStack(stack), state);
    }

    public static ItemStack fixCtrlCopiedStack(ItemStack stack) {
        // Salvage frequency data from one of the panel slots
        if (isTuned(stack) && networkFromStack(stack) == null) {
            TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
            NbtCompound bet;
            BlockEntityType<?> type;
            if (data != null) {
                bet = data.copyNbtWithoutId();
                type = data.getType();
            } else {
                bet = new NbtCompound();
                type = AllBlockEntityTypes.PACKAGER_LINK;
            }
            UUID frequency = UUID.randomUUID();

            for (PanelSlot slot : PanelSlot.values()) {
                Optional<UUID> freq = bet.getCompound(slot.name().toLowerCase(Locale.ROOT)).flatMap(tag -> tag.get("Freq", Uuids.INT_STREAM_CODEC));
                if (freq.isPresent())
                    frequency = freq.get();
            }

            bet = new NbtCompound();
            bet.put("Freq", Uuids.INT_STREAM_CODEC, frequency);
            bet.put("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, ((IBE<?>) ((BlockItem) stack.getItem()).getBlock()).getBlockEntityType());
            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(type, bet));
        }

        return stack;
    }

}
