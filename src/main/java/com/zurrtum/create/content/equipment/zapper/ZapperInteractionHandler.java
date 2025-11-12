package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.utility.BlockHelper;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ZapperInteractionHandler {
    public static boolean leftClickingBlocksWithTheZapperSelectsTheBlock(ServerPlayer player, ItemStack heldItem) {
        return heldItem.getItem() instanceof ZapperItem && trySelect(heldItem, player);
    }

    public static boolean trySelect(ItemStack stack, Player player) {
        if (player.isShiftKeyDown())
            return false;

        Level world = player.level();
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 range = player.getLookAngle().scale(getRange(stack));
        BlockHitResult raytrace = world.clip(new ClipContext(start, start.add(range), Block.OUTLINE, Fluid.NONE, player));
        BlockPos pos = raytrace.getBlockPos();
        if (pos == null)
            return false;

        world.destroyBlockProgress(player.getId(), pos, -1);
        BlockState newState = world.getBlockState(pos);

        if (BlockHelper.getRequiredItem(newState).isEmpty())
            return false;
        if (newState.hasBlockEntity() && !newState.is(AllBlockTags.SAFE_NBT))
            return false;
        if (newState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))
            return false;
        if (newState.hasProperty(BlockStateProperties.ATTACHED))
            return false;
        if (newState.hasProperty(BlockStateProperties.HANGING))
            return false;
        if (newState.hasProperty(BlockStateProperties.BED_PART))
            return false;
        if (newState.hasProperty(BlockStateProperties.STAIRS_SHAPE))
            newState = newState.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.STRAIGHT);
        if (newState.hasProperty(BlockStateProperties.PERSISTENT))
            newState = newState.setValue(BlockStateProperties.PERSISTENT, true);
        if (newState.hasProperty(BlockStateProperties.WATERLOGGED))
            newState = newState.setValue(BlockStateProperties.WATERLOGGED, false);

        CompoundTag data = null;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            data = blockEntity.saveWithFullMetadata(world.registryAccess());
            data.remove("x");
            data.remove("y");
            data.remove("z");
            data.remove("id");
        }

        if (stack.has(AllDataComponents.SHAPER_BLOCK_USED) && stack.get(AllDataComponents.SHAPER_BLOCK_USED) == newState && Objects.equals(
            data,
            stack.get(AllDataComponents.SHAPER_BLOCK_DATA)
        )) {
            return false;
        }

        stack.set(AllDataComponents.SHAPER_BLOCK_USED, newState);
        if (data == null)
            stack.remove(AllDataComponents.SHAPER_BLOCK_DATA);
        else
            stack.set(AllDataComponents.SHAPER_BLOCK_DATA, data);

        AllSoundEvents.CONFIRM.playOnServer(world, player.blockPosition());
        return true;
    }

    public static int getRange(ItemStack stack) {
        if (stack.getItem() instanceof ZapperItem)
            return ((ZapperItem) stack.getItem()).getZappingRange(stack);
        return 0;
    }
}