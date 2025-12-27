package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.api.schematic.nbt.PartialSafeNBT;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.redstone.RoseQuartzLampBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.Optional;

public class CopycatBlockEntity extends SmartBlockEntity implements SpecialBlockEntityItemRequirement, TransformableBlockEntity, PartialSafeNBT {

    private BlockState material;
    private ItemStack consumedItem;

    public CopycatBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.COPYCAT, pos, state);
        material = AllBlocks.COPYCAT_BASE.defaultBlockState();
        consumedItem = ItemStack.EMPTY;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        Block.popResource(level, pos, consumedItem);
    }

    public BlockState getMaterial() {
        return material;
    }

    public boolean hasCustomMaterial() {
        return !getMaterial().is(AllBlocks.COPYCAT_BASE);
    }

    public void setMaterial(BlockState blockState) {
        BlockState wrapperState = getBlockState();

        if (!material.is(blockState.getBlock()))
            for (Direction side : Iterate.directions) {
                BlockPos neighbour = worldPosition.relative(side);
                BlockState neighbourState = level.getBlockState(neighbour);
                if (neighbourState != wrapperState)
                    continue;
                if (!(level.getBlockEntity(neighbour) instanceof CopycatBlockEntity cbe))
                    continue;
                BlockState otherMaterial = cbe.getMaterial();
                if (!otherMaterial.is(blockState.getBlock()))
                    continue;
                blockState = otherMaterial;
                break;
            }

        material = blockState;
        if (!level.isClientSide()) {
            notifyUpdate();
            return;
        }
        redraw();
    }

    public boolean cycleMaterial() {
        if (material.hasProperty(TrapDoorBlock.HALF) && material.getValueOrElse(TrapDoorBlock.OPEN, false))
            setMaterial(material.cycle(TrapDoorBlock.HALF));
        else if (material.hasProperty(BlockStateProperties.FACING))
            setMaterial(material.cycle(BlockStateProperties.FACING));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            setMaterial(material.setValue(
                BlockStateProperties.HORIZONTAL_FACING,
                material.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()
            ));
        else if (material.hasProperty(BlockStateProperties.AXIS))
            setMaterial(material.cycle(BlockStateProperties.AXIS));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
            setMaterial(material.cycle(BlockStateProperties.HORIZONTAL_AXIS));
        else if (material.hasProperty(BlockStateProperties.LIT))
            setMaterial(material.cycle(BlockStateProperties.LIT));
        else if (material.hasProperty(RoseQuartzLampBlock.POWERING))
            setMaterial(material.cycle(RoseQuartzLampBlock.POWERING));
        else
            return false;

        return true;
    }

    public ItemStack getConsumedItem() {
        return consumedItem;
    }

    public void setConsumedItem(ItemStack stack) {
        consumedItem = stack.copyWithCount(1);
        setChanged();
    }

    private void redraw() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            level.getChunkSource().getLightEngine().checkBlock(worldPosition);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state) {
        if (consumedItem.isEmpty())
            return ItemRequirement.NONE;
        return new ItemRequirement(ItemUseType.CONSUME, consumedItem);
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        material = transform.apply(material);
        notifyUpdate();
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);

        consumedItem = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        Optional<BlockState> state = view.read("Material", BlockState.CODEC);
        if (state.isEmpty()) {
            consumedItem = ItemStack.EMPTY;
            return;
        }

        BlockState prevMaterial = material;
        material = state.get();

        // Validate Material
        if (!clientPacket) {
            BlockState blockState = getBlockState();
            if (blockState == null)
                return;
            if (!(blockState.getBlock() instanceof CopycatBlock cb))
                return;
            BlockState acceptedBlockState = cb.getAcceptedBlockState(level, worldPosition, consumedItem, null);
            if (acceptedBlockState != null && material.is(acceptedBlockState.getBlock()))
                return;
            consumedItem = ItemStack.EMPTY;
            material = AllBlocks.COPYCAT_BASE.defaultBlockState();
        }

        if (clientPacket && prevMaterial != material)
            redraw();
    }

    @Override
    public void writeSafe(ValueOutput view) {
        super.writeSafe(view);

        ItemStack stackWithoutComponents = new ItemStack(consumedItem.typeHolder(), consumedItem.getCount(), DataComponentPatch.EMPTY);

        write(view, stackWithoutComponents, material);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        write(view, consumedItem, material);
    }

    protected void write(ValueOutput view, ItemStack stack, BlockState material) {
        if (!stack.isEmpty()) {
            view.store("Item", ItemStack.CODEC, stack);
        }
        view.store("Material", BlockState.CODEC, material);
    }
}