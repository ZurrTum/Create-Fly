package com.zurrtum.create.content.schematics.cannon;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class SchematicannonBlock extends Block implements IBE<SchematicannonBlockEntity> {

    public SchematicannonBlock(Settings properties) {
        super(properties);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.SCHEMATICANNON_SHAPE;
    }

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null) {
            withBlockEntityDo(
                level, pos, be -> {
                    be.defaultYaw = (-MathHelper.floor((entity.getYaw() + (entity.isSneaking() ? 180.0F : 0.0F)) * 16.0F / 360.0F + 0.5F) & 15) * 360.0F / 16.0F;
                }
            );
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        if (level.isClient())
            return ActionResult.SUCCESS;
        withBlockEntityDo(level, pos, be -> be.openHandledScreen((ServerPlayerEntity) player));
        return ActionResult.SUCCESS;
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        withBlockEntityDo(worldIn, pos, be -> be.neighbourCheckCooldown = 0);
    }

    @Override
    public Class<SchematicannonBlockEntity> getBlockEntityClass() {
        return SchematicannonBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SchematicannonBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SCHEMATICANNON;
    }

}
