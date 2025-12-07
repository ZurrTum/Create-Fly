package com.zurrtum.create.content.contraptions.actors.harvester;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class HarvesterMovementBehaviour extends MovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            context.state.get(HarvesterBlock.FACING).getOpposite()
        );
    }

    @Override
    public Vec3d getActiveAreaOffset(MovementContext context) {
        return Vec3d.of(context.state.get(HarvesterBlock.FACING).getVector()).multiply(.45);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        World world = context.world;
        if (world.isClient)
            return;

        BlockState stateVisited = world.getBlockState(pos);
        if (stateVisited.isAir() || stateVisited.isIn(AllBlockTags.NON_HARVESTABLE))
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

        if (stateVisited.isIn(BlockTags.LEAVES)) {
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
                    stack.decrement(1);
                    seedSubtracted.setTrue();
                }
                collectOrDropItem(context, stack);
            }
        );

        BlockState cutCrop = cutCrop(world, pos, stateVisited);
        world.setBlockState(pos, cutCrop.canPlaceAt(world, pos) ? cutCrop : Blocks.AIR.getDefaultState());
    }

    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        boolean harvestPartial = AllConfigs.server().kinetics.harvestPartiallyGrown.get();
        boolean replant = AllConfigs.server().kinetics.harvesterReplants.get();

        if (state.getBlock() instanceof CropBlock crop) {
            if (harvestPartial)
                return state != crop.withAge(0) || !replant;
            return crop.isMature(state);
        }

        if (state.getCollisionShape(world, pos).isEmpty() || state.getBlock() instanceof CocoaBlock) {
            for (Property<?> property : state.getProperties()) {
                if (!(property instanceof IntProperty ageProperty))
                    continue;
                if (!property.getName().equals(Properties.AGE_1.getName()))
                    continue;
                int age = state.get(ageProperty);
                if (state.getBlock() instanceof SweetBerryBushBlock && age <= 1 && replant)
                    continue;
                if (age == 0 && replant || !harvestPartial && (ageProperty.getValues().size() - 1 != age))
                    continue;
                return true;
            }
        }

        return false;
    }

    public boolean isValidOther(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock)
            return false;
        if (state.getBlock() instanceof SugarCaneBlock)
            return true;
        if (state.isIn(BlockTags.LEAVES))
            return true;
        if (state.getBlock() instanceof CocoaBlock)
            return state.get(CocoaBlock.AGE) == CocoaBlock.MAX_AGE;

        if (state.getCollisionShape(world, pos).isEmpty()) {
            if (state.getBlock() instanceof AbstractPlantStemBlock)
                return true;

            for (Property<?> property : state.getProperties()) {
                if (!(property instanceof IntProperty))
                    continue;
                if (!property.getName().equals(Properties.AGE_1.getName()))
                    continue;
                return false;
            }

            if (state.getBlock() instanceof PlantBlock) {
                return true;
            }
            //TODO
            //            if (state.getBlock() instanceof SpecialPlantable)
            //                return true;
        }

        return false;
    }

    private BlockState cutCrop(World world, BlockPos pos, BlockState state) {
        if (!AllConfigs.server().kinetics.harvesterReplants.get()) {
            if (state.getFluidState().isEmpty())
                return Blocks.AIR.getDefaultState();
            return state.getFluidState().getBlockState();
        }

        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            BlockState newState = crop.withAge(0);
            if (!newState.isOf(block))
                return newState;
            IntProperty ageProperty = crop.getAgeProperty();
            return state.with(ageProperty, 0);
        }
        if (block == Blocks.SWEET_BERRY_BUSH) {
            return state.with(Properties.AGE_3, 1);
        }
        if (state.isIn(AllBlockTags.SUGAR_CANE_VARIANTS) || block instanceof AbstractPlantStemBlock) {
            if (state.getFluidState().isEmpty())
                return Blocks.AIR.getDefaultState();
            return state.getFluidState().getBlockState();
        }
        if (state.getCollisionShape(world, pos).isEmpty() || block instanceof CocoaBlock) {
            for (Property<?> property : state.getProperties()) {
                if (!(property instanceof IntProperty))
                    continue;
                if (!property.getName().equals(Properties.AGE_1.getName()))
                    continue;
                return state.with((IntProperty) property, 0);
            }
        }

        if (state.getFluidState().isEmpty())
            return Blocks.AIR.getDefaultState();
        return state.getFluidState().getBlockState();
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
