package com.zurrtum.create.content.logistics.vault;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class ItemVaultItem extends BlockItem {

    public ItemVaultItem(Block p_i48527_1_, Settings p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public ActionResult place(ItemPlacementContext ctx) {
        ActionResult initialResult = super.place(ctx);
        if (!initialResult.isAccepted())
            return initialResult;
        tryMultiPlace(ctx);
        return initialResult;
    }

    @Override
    protected boolean postPlacement(BlockPos blockPos, World level, PlayerEntity player, ItemStack itemStack, BlockState blockState) {
        MinecraftServer minecraftserver = level.getServer();
        if (minecraftserver == null)
            return false;
        NbtComponent blockEntityData = itemStack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            NbtCompound nbt = blockEntityData.copyNbt();
            nbt.remove("Length");
            nbt.remove("Size");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
            nbt.put("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, ((IBE<?>) getBlock()).getBlockEntityType());
            itemStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
        }
        return super.postPlacement(blockPos, level, player, itemStack, blockState);
    }

    private void tryMultiPlace(ItemPlacementContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null)
            return;
        if (player.isSneaking())
            return;
        Direction face = ctx.getSide();
        ItemStack stack = ctx.getStack();
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockPos placedOnPos = pos.offset(face.getOpposite());
        BlockState placedOnState = world.getBlockState(placedOnPos);

        if (!ItemVaultBlock.isVault(placedOnState))
            return;
        if (SymmetryWandItem.presentInHotbar(player))
            return;
        ItemVaultBlockEntity tankAt = ConnectivityHandler.partAt(AllBlockEntityTypes.ITEM_VAULT, world, placedOnPos);
        if (tankAt == null)
            return;
        ItemVaultBlockEntity controllerBE = tankAt.getControllerBE();
        if (controllerBE == null)
            return;

        int width = controllerBE.radius;
        if (width == 1)
            return;

        int tanksToPlace = 0;
        Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(placedOnState);
        if (vaultBlockAxis == null)
            return;
        if (face.getAxis() != vaultBlockAxis)
            return;

        Direction vaultFacing = Direction.from(vaultBlockAxis, Direction.AxisDirection.POSITIVE);
        BlockPos startPos = face == vaultFacing.getOpposite() ? controllerBE.getPos().offset(vaultFacing.getOpposite()) : controllerBE.getPos()
            .offset(vaultFacing, controllerBE.length);

        if (VecHelper.getCoordinate(startPos, vaultBlockAxis) != VecHelper.getCoordinate(pos, vaultBlockAxis))
            return;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = vaultBlockAxis == Axis.X ? startPos.add(0, xOffset, zOffset) : startPos.add(xOffset, zOffset, 0);
                BlockState blockState = world.getBlockState(offsetPos);
                if (ItemVaultBlock.isVault(blockState))
                    continue;
                if (!blockState.isReplaceable())
                    return;
                tanksToPlace++;
            }
        }

        if (!player.isCreative() && stack.getCount() < tanksToPlace)
            return;

        ItemPlacementSoundContext context = new ItemPlacementSoundContext(ctx, 0.1f, 1.5f, null);
        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = vaultBlockAxis == Axis.X ? startPos.add(0, xOffset, zOffset) : startPos.add(xOffset, zOffset, 0);
                BlockState blockState = world.getBlockState(offsetPos);
                if (ItemVaultBlock.isVault(blockState))
                    continue;
                super.place(context.offset(offsetPos, face));
            }
        }
    }

}
