package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.api.schematic.nbt.PartialSafeNBT;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.redstone.RoseQuartzLampBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Optional;

public class CopycatBlockEntity extends SmartBlockEntity implements SpecialBlockEntityItemRequirement, TransformableBlockEntity, PartialSafeNBT {

    private BlockState material;
    private ItemStack consumedItem;

    public CopycatBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.COPYCAT, pos, state);
        material = AllBlocks.COPYCAT_BASE.getDefaultState();
        consumedItem = ItemStack.EMPTY;
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        Block.dropStack(world, pos, consumedItem);
    }

    public BlockState getMaterial() {
        return material;
    }

    public boolean hasCustomMaterial() {
        return !getMaterial().isOf(AllBlocks.COPYCAT_BASE);
    }

    public void setMaterial(BlockState blockState) {
        BlockState wrapperState = getCachedState();

        if (!material.isOf(blockState.getBlock()))
            for (Direction side : Iterate.directions) {
                BlockPos neighbour = pos.offset(side);
                BlockState neighbourState = world.getBlockState(neighbour);
                if (neighbourState != wrapperState)
                    continue;
                if (!(world.getBlockEntity(neighbour) instanceof CopycatBlockEntity cbe))
                    continue;
                BlockState otherMaterial = cbe.getMaterial();
                if (!otherMaterial.isOf(blockState.getBlock()))
                    continue;
                blockState = otherMaterial;
                break;
            }

        material = blockState;
        if (!world.isClient()) {
            notifyUpdate();
            return;
        }
        redraw();
    }

    public boolean cycleMaterial() {
        if (material.contains(TrapdoorBlock.HALF) && material.get(TrapdoorBlock.OPEN, false))
            setMaterial(material.cycle(TrapdoorBlock.HALF));
        else if (material.contains(Properties.FACING))
            setMaterial(material.cycle(Properties.FACING));
        else if (material.contains(Properties.HORIZONTAL_FACING))
            setMaterial(material.with(Properties.HORIZONTAL_FACING, material.get(Properties.HORIZONTAL_FACING).rotateYClockwise()));
        else if (material.contains(Properties.AXIS))
            setMaterial(material.cycle(Properties.AXIS));
        else if (material.contains(Properties.HORIZONTAL_AXIS))
            setMaterial(material.cycle(Properties.HORIZONTAL_AXIS));
        else if (material.contains(Properties.LIT))
            setMaterial(material.cycle(Properties.LIT));
        else if (material.contains(RoseQuartzLampBlock.POWERING))
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
        markDirty();
    }

    private void redraw() {
        if (world != null) {
            world.updateListeners(getPos(), getCachedState(), getCachedState(), 16);
            world.getChunkManager().getLightingProvider().checkBlock(pos);
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
    protected void read(ReadView view, boolean clientPacket) {
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
            BlockState blockState = getCachedState();
            if (blockState == null)
                return;
            if (!(blockState.getBlock() instanceof CopycatBlock cb))
                return;
            BlockState acceptedBlockState = cb.getAcceptedBlockState(world, pos, consumedItem, null);
            if (acceptedBlockState != null && material.isOf(acceptedBlockState.getBlock()))
                return;
            consumedItem = ItemStack.EMPTY;
            material = AllBlocks.COPYCAT_BASE.getDefaultState();
        }

        if (clientPacket && prevMaterial != material)
            redraw();
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);

        ItemStack stackWithoutComponents = new ItemStack(consumedItem.getRegistryEntry(), consumedItem.getCount(), ComponentChanges.EMPTY);

        write(view, stackWithoutComponents, material);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        write(view, consumedItem, material);
    }

    protected void write(WriteView view, ItemStack stack, BlockState material) {
        if (!stack.isEmpty()) {
            view.put("Item", ItemStack.CODEC, stack);
        }
        view.put("Material", BlockState.CODEC, material);
    }
}