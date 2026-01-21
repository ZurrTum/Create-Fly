package com.zurrtum.create.content.fluids.tank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FluidTankItem extends BlockItem {

    public FluidTankItem(Block p_i48527_1_, Properties p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult initialResult = super.place(ctx);
        if (!initialResult.consumesAction())
            return initialResult;
        tryMultiPlace(ctx);
        return initialResult;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
        BlockPos blockPos,
        Level level,
        @Nullable Player player,
        ItemStack itemStack,
        BlockState blockState
    ) {
        MinecraftServer minecraftserver = level.getServer();
        if (minecraftserver == null)
            return false;
        TypedEntityData<BlockEntityType<?>> data = itemStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTagWithoutId();
            nbt.remove("Luminosity");
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
            if (nbt.contains("TankContent")) {
                FluidStack fluid = FluidStack.fromNbt(minecraftserver.registryAccess(), nbt.getCompound("TankContent"));
                if (!fluid.isEmpty()) {
                    fluid.setAmount(Math.min(FluidTankBlockEntity.getCapacityMultiplier(), fluid.getAmount()));
                    nbt.put("TankContent", fluid.toNbt(minecraftserver.registryAccess()));
                }
            }
            itemStack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(((IBE<?>) this.getBlock()).getBlockEntityType(), nbt));
        }
        return super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
    }

    private void tryMultiPlace(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null)
            return;
        if (player.isShiftKeyDown())
            return;
        Direction face = ctx.getClickedFace();
        if (!face.getAxis().isVertical())
            return;
        ItemStack stack = ctx.getItemInHand();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos placedOnPos = pos.relative(face.getOpposite());
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
        BlockPos startPos = face == Direction.DOWN ? controllerBE.getBlockPos().below() : controllerBE.getBlockPos().above(controllerBE.height);

        if (startPos.getY() != pos.getY())
            return;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
                BlockState blockState = world.getBlockState(offsetPos);
                if (FluidTankBlock.isTank(blockState))
                    continue;
                if (!blockState.canBeReplaced())
                    return;
                tanksToPlace++;
            }
        }

        if (!player.isCreative() && stack.getCount() < tanksToPlace)
            return;

        ItemPlacementSoundContext context = new ItemPlacementSoundContext(ctx, 0.1f, 1.5f, SILENCED_METAL.getPlaceSound());
        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
                BlockState blockState = world.getBlockState(offsetPos);
                if (FluidTankBlock.isTank(blockState))
                    continue;
                super.place(context.offset(offsetPos, face));
            }
        }
    }

    // Tanks are less noisy when placed in batch
    public static final SoundType SILENCED_METAL = new SoundType(
        0.1F,
        1.5F,
        SoundEvents.METAL_BREAK,
        SoundEvents.METAL_STEP,
        SoundEvents.METAL_PLACE,
        SoundEvents.METAL_HIT,
        SoundEvents.METAL_FALL
    );
}
