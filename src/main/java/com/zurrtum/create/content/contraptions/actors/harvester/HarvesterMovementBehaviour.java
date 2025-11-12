package com.zurrtum.create.content.contraptions.actors.harvester;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class HarvesterMovementBehaviour extends MovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            context.state.getValue(HarvesterBlock.FACING).getOpposite()
        );
    }

    @Override
    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.atLowerCornerOf(context.state.getValue(HarvesterBlock.FACING).getUnitVec3i()).scale(.45);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        Level world = context.world;
        if (world.isClientSide())
            return;

        BlockState stateVisited = world.getBlockState(pos);
        if (stateVisited.isAir() || stateVisited.is(AllBlockTags.NON_HARVESTABLE))
            return;

        boolean notCropButCuttable = false;

        if (!isValidCrop(world, pos, stateVisited)) {
            if (isValidOther(world, pos, stateVisited))
                notCropButCuttable = true;
            else
                return;
        }

        ItemStack item = ItemStack.EMPTY;
        float effectChance = 1;

        if (stateVisited.is(BlockTags.LEAVES)) {
            item = new ItemStack(Items.SHEARS);
            effectChance = .45f;
        }

        MutableBoolean seedSubtracted = new MutableBoolean(notCropButCuttable);
        BlockState state = stateVisited;
        BlockHelper.destroyBlockAs(
            world, pos, null, item, effectChance, stack -> {
                if (AllConfigs.server().kinetics.harvesterReplants.get() && !seedSubtracted.getValue() && ItemHelper.sameItem(
                    stack,
                    new ItemStack(state.getBlock())
                )) {
                    stack.shrink(1);
                    seedSubtracted.setTrue();
                }
                dropItem(context, stack);
            }
        );

        BlockState cutCrop = cutCrop(world, pos, stateVisited);
        world.setBlockAndUpdate(pos, cutCrop.canSurvive(world, pos) ? cutCrop : Blocks.AIR.defaultBlockState());
    }

    public boolean isValidCrop(Level world, BlockPos pos, BlockState state) {
        boolean harvestPartial = AllConfigs.server().kinetics.harvestPartiallyGrown.get();
        boolean replant = AllConfigs.server().kinetics.harvesterReplants.get();

        if (state.getBlock() instanceof CropBlock crop) {
            if (harvestPartial)
                return state != crop.getStateForAge(0) || !replant;
            return crop.isMaxAge(state);
        }

        if (state.getCollisionShape(world, pos).isEmpty() || state.getBlock() instanceof CocoaBlock) {
            for (Property<?> property : state.getProperties()) {
                if (!(property instanceof IntegerProperty ageProperty))
                    continue;
                if (!property.getName().equals(BlockStateProperties.AGE_1.getName()))
                    continue;
                int age = state.getValue(ageProperty);
                if (state.getBlock() instanceof SweetBerryBushBlock && age <= 1 && replant)
                    continue;
                if (age == 0 && replant || !harvestPartial && (ageProperty.getPossibleValues().size() - 1 != age))
                    continue;
                return true;
            }
        }

        return false;
    }

    public boolean isValidOther(Level world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock)
            return false;
        if (state.getBlock() instanceof SugarCaneBlock)
            return true;
        if (state.is(BlockTags.LEAVES))
            return true;
        if (state.getBlock() instanceof CocoaBlock)
            return state.getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE;

        if (state.getCollisionShape(world, pos).isEmpty()) {
            if (state.getBlock() instanceof GrowingPlantHeadBlock)
                return true;

            for (Property<?> property : state.getProperties()) {
                if (!(property instanceof IntegerProperty))
                    continue;
                if (!property.getName().equals(BlockStateProperties.AGE_1.getName()))
                    continue;
                return false;
            }

            if (state.getBlock() instanceof VegetationBlock) {
                return true;
            }
            //TODO
            //            if (state.getBlock() instanceof SpecialPlantable)
            //                return true;
        }

        return false;
    }

    private BlockState cutCrop(Level world, BlockPos pos, BlockState state) {
        if (!AllConfigs.server().kinetics.harvesterReplants.get()) {
            if (state.getFluidState().isEmpty())
                return Blocks.AIR.defaultBlockState();
            return state.getFluidState().createLegacyBlock();
        }

        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            BlockState newState = crop.getStateForAge(0);
            if (!newState.is(block))
                return newState;
            IntegerProperty ageProperty = crop.getAgeProperty();
            return state.setValue(ageProperty, 0);
        }
        if (block == Blocks.SWEET_BERRY_BUSH) {
            return state.setValue(BlockStateProperties.AGE_3, 1);
        }
        if (state.is(AllBlockTags.SUGAR_CANE_VARIANTS) || block instanceof GrowingPlantHeadBlock) {
            if (state.getFluidState().isEmpty())
                return Blocks.AIR.defaultBlockState();
            return state.getFluidState().createLegacyBlock();
        }
        if (state.getCollisionShape(world, pos).isEmpty() || block instanceof CocoaBlock) {
            for (Property<?> property : state.getProperties()) {
                if (!(property instanceof IntegerProperty))
                    continue;
                if (!property.getName().equals(BlockStateProperties.AGE_1.getName()))
                    continue;
                return state.setValue((IntegerProperty) property, 0);
            }
        }

        if (state.getFluidState().isEmpty())
            return Blocks.AIR.defaultBlockState();
        return state.getFluidState().createLegacyBlock();
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
