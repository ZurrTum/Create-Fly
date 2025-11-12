package com.zurrtum.create.content.logistics.factoryBoard;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.codec.CreateCodecs;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FactoryPanelBlockItem extends LogisticallyLinkedBlockItem {

    public FactoryPanelBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        ItemStack stack = pContext.getItemInHand();

        if (!isTuned(stack)) {
            AllSoundEvents.DENY.playOnServer(pContext.getLevel(), pContext.getClickedPos());
            pContext.getPlayer().displayClientMessage(Component.translatable("create.factory_panel.tune_before_placing"), true);
            return InteractionResult.FAIL;
        }

        return super.place(pContext);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
        return super.updateCustomBlockEntityTag(pos, level, player, fixCtrlCopiedStack(stack), state);
    }

    public static ItemStack fixCtrlCopiedStack(ItemStack stack) {
        // Salvage frequency data from one of the panel slots
        if (isTuned(stack) && networkFromStack(stack) == null) {
            TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag bet;
            BlockEntityType<?> type;
            if (data != null) {
                bet = data.copyTagWithoutId();
                type = data.type();
            } else {
                bet = new CompoundTag();
                type = AllBlockEntityTypes.PACKAGER_LINK;
            }
            UUID frequency = UUID.randomUUID();

            for (PanelSlot slot : PanelSlot.values()) {
                Optional<UUID> freq = bet.getCompound(slot.name().toLowerCase(Locale.ROOT)).flatMap(tag -> tag.read("Freq", UUIDUtil.CODEC));
                if (freq.isPresent())
                    frequency = freq.get();
            }

            bet = new CompoundTag();
            bet.store("Freq", UUIDUtil.CODEC, frequency);
            bet.store("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, ((IBE<?>) ((BlockItem) stack.getItem()).getBlock()).getBlockEntityType());
            stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(type, bet));
        }

        return stack;
    }

}
