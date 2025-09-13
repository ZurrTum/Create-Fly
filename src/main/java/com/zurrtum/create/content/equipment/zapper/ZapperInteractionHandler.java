package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;

import java.util.Objects;

public class ZapperInteractionHandler {
    public static boolean leftClickingBlocksWithTheZapperSelectsTheBlock(ServerPlayerEntity player, ItemStack heldItem) {
        return heldItem.getItem() instanceof ZapperItem && trySelect(heldItem, player);
    }

    public static boolean trySelect(ItemStack stack, PlayerEntity player) {
        if (player.isSneaking())
            return false;

        World world = player.getWorld();
        Vec3d start = player.getPos().add(0, player.getStandingEyeHeight(), 0);
        Vec3d range = player.getRotationVector().multiply(getRange(stack));
        BlockHitResult raytrace = world.raycast(new RaycastContext(start, start.add(range), ShapeType.OUTLINE, FluidHandling.NONE, player));
        BlockPos pos = raytrace.getBlockPos();
        if (pos == null)
            return false;

        world.setBlockBreakingInfo(player.getId(), pos, -1);
        BlockState newState = world.getBlockState(pos);

        if (BlockHelper.getRequiredItem(newState).isEmpty())
            return false;
        if (newState.hasBlockEntity() && !newState.isIn(AllBlockTags.SAFE_NBT))
            return false;
        if (newState.contains(Properties.DOUBLE_BLOCK_HALF))
            return false;
        if (newState.contains(Properties.ATTACHED))
            return false;
        if (newState.contains(Properties.HANGING))
            return false;
        if (newState.contains(Properties.BED_PART))
            return false;
        if (newState.contains(Properties.STAIR_SHAPE))
            newState = newState.with(Properties.STAIR_SHAPE, StairShape.STRAIGHT);
        if (newState.contains(Properties.PERSISTENT))
            newState = newState.with(Properties.PERSISTENT, true);
        if (newState.contains(Properties.WATERLOGGED))
            newState = newState.with(Properties.WATERLOGGED, false);

        NbtCompound data = null;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            data = blockEntity.createNbtWithIdentifyingData(world.getRegistryManager());
            data.remove("x");
            data.remove("y");
            data.remove("z");
            data.remove("id");
        }

        if (stack.contains(AllDataComponents.SHAPER_BLOCK_USED) && stack.get(AllDataComponents.SHAPER_BLOCK_USED) == newState && Objects.equals(
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

        AllSoundEvents.CONFIRM.playOnServer(world, player.getBlockPos());
        return true;
    }

    public static int getRange(ItemStack stack) {
        if (stack.getItem() instanceof ZapperItem)
            return ((ZapperItem) stack.getItem()).getZappingRange(stack);
        return 0;
    }
}