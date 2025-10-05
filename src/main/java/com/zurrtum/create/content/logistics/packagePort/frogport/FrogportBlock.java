package com.zurrtum.create.content.logistics.packagePort.frogport;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class FrogportBlock extends Block implements IBE<FrogportBlockEntity>, IWrenchable, ItemInventoryProvider<FrogportBlockEntity> {

    public FrogportBlock(Settings pProperties) {
        super(pProperties);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, FrogportBlockEntity blockEntity, Direction context) {
        return blockEntity.inventory;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.PACKAGE_PORT;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        if (pPlacer == null)
            return;
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
        withBlockEntityDo(
            pLevel, pPos, be -> {
                Vec3d diff = VecHelper.getCenterOf(pPos).subtract(pPlacer.getEntityPos());
                be.passiveYaw = (float) (MathHelper.atan2(diff.x, diff.z) * MathHelper.DEGREES_PER_RADIAN);
                be.passiveYaw = Math.round(be.passiveYaw / 11.25f) * 11.25f;
                be.notifyUpdate();
            }
        );
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        return onBlockEntityUseItemOn(level, pos, be -> be.use(player));
    }

    @Override
    public Class<FrogportBlockEntity> getBlockEntityClass() {
        return FrogportBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FrogportBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.PACKAGE_FROGPORT;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public boolean hasComparatorOutput(BlockState pState) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World pLevel, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(pLevel, pPos).map(PackagePortBlockEntity::getComparatorOutput).orElse(0);
    }

}
