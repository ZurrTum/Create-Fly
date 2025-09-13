package com.zurrtum.create.content.fluids.tank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
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
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FluidTankItem extends BlockItem {

    public FluidTankItem(Block p_i48527_1_, Settings p_i48527_2_) {
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
            nbt.remove("Luminosity");
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
            if (nbt.contains("TankContent")) {
                FluidStack fluid = FluidStack.fromNbt(minecraftserver.getRegistryManager(), nbt.getCompound("TankContent"));
                if (!fluid.isEmpty()) {
                    fluid.setAmount(Math.min(FluidTankBlockEntity.getCapacityMultiplier(), fluid.getAmount()));
                    nbt.put("TankContent", fluid.toNbt(minecraftserver.getRegistryManager()));
                }
            }
            nbt.put("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, ((IBE<?>) this.getBlock()).getBlockEntityType());
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
        if (!face.getAxis().isVertical())
            return;
        ItemStack stack = ctx.getStack();
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockPos placedOnPos = pos.offset(face.getOpposite());
        BlockState placedOnState = world.getBlockState(placedOnPos);

        if (!FluidTankBlock.isTank(placedOnState))
            return;
        if (SymmetryWandItem.presentInHotbar(player))
            return;
        boolean creative = getBlock().equals(AllBlocks.CREATIVE_FLUID_TANK);
        FluidTankBlockEntity tankAt = ConnectivityHandler.partAt(
            creative ? AllBlockEntityTypes.CREATIVE_FLUID_TANK : AllBlockEntityTypes.FLUID_TANK,
            world,
            placedOnPos
        );
        if (tankAt == null)
            return;
        FluidTankBlockEntity controllerBE = tankAt.getControllerBE();
        if (controllerBE == null)
            return;

        int width = controllerBE.width;
        if (width == 1)
            return;

        int tanksToPlace = 0;
        BlockPos startPos = face == Direction.DOWN ? controllerBE.getPos().down() : controllerBE.getPos().up(controllerBE.height);

        if (startPos.getY() != pos.getY())
            return;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
                BlockState blockState = world.getBlockState(offsetPos);
                if (FluidTankBlock.isTank(blockState))
                    continue;
                if (!blockState.isReplaceable())
                    return;
                tanksToPlace++;
            }
        }

        if (!player.isCreative() && stack.getCount() < tanksToPlace)
            return;

        ItemPlacementSoundContext context = new ItemPlacementSoundContext(ctx, 0.1f, 1.5f, SILENCED_METAL.getPlaceSound());
        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
                BlockState blockState = world.getBlockState(offsetPos);
                if (FluidTankBlock.isTank(blockState))
                    continue;
                super.place(context.offset(offsetPos, face));
            }
        }
    }

    // Tanks are less noisy when placed in batch
    public static final BlockSoundGroup SILENCED_METAL = new BlockSoundGroup(
        0.1F,
        1.5F,
        SoundEvents.BLOCK_METAL_BREAK,
        SoundEvents.BLOCK_METAL_STEP,
        SoundEvents.BLOCK_METAL_PLACE,
        SoundEvents.BLOCK_METAL_HIT,
        SoundEvents.BLOCK_METAL_FALL
    );
}
