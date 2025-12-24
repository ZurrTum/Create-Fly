package com.zurrtum.create.content.trains.observer;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.compat.computercraft.events.TrainPassEvent;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TrackObserverBlockEntity extends SmartBlockEntity implements TransformableBlockEntity, Clearable {

    public TrackTargetingBehaviour<TrackObserver> edgePoint;

    private ServerFilteringBehaviour filtering;

    public AbstractComputerBehaviour computerBehaviour;
    public @Nullable UUID passingTrainUUID;

    public TrackObserverBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TRACK_OBSERVER, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.OBSERVER));
        behaviours.add(filtering = new ServerFilteringBehaviour(this).withCallback(this::onFilterChanged));
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    private void onFilterChanged(ItemStack newFilter) {
        if (level.isClientSide())
            return;
        TrackObserver observer = getObserver();
        if (observer != null)
            observer.setFilterAndNotify(level, newFilter);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide())
            return;

        boolean shouldBePowered = false;
        TrackObserver observer = getObserver();
        if (observer != null)
            shouldBePowered = observer.isActivated();
        if (isBlockPowered() == shouldBePowered)
            return;

        if (observer != null && computerBehaviour.hasAttachedComputer()) {
            if (shouldBePowered)
                passingTrainUUID = observer.getCurrentTrain();
            if (passingTrainUUID != null) {
                computerBehaviour.prepareComputerEvent(new TrainPassEvent(Create.RAILWAYS.trains.get(passingTrainUUID), shouldBePowered));
                if (!shouldBePowered)
                    passingTrainUUID = null;
            }
        }

        BlockState blockState = getBlockState();
        if (blockState.hasProperty(TrackObserverBlock.POWERED))
            level.setBlock(worldPosition, blockState.setValue(TrackObserverBlock.POWERED, shouldBePowered), Block.UPDATE_ALL);
        DisplayLinkBlock.notifyGatherers(level, worldPosition);
    }

    @Nullable
    public TrackObserver getObserver() {
        return edgePoint.getEdgePoint();
    }

    public ItemStack getFilter() {
        return filtering.getFilter();
    }

    public boolean isBlockPowered() {
        return getBlockState().getValueOrElse(TrackObserverBlock.POWERED, false);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(Vec3.atLowerCornerOf(worldPosition), Vec3.atLowerCornerOf(edgePoint.getGlobalPosition())).inflate(2);
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        edgePoint.transform(be, transform);
    }

    @Override
    public void clearContent() {
        filtering.setFilter(ItemStack.EMPTY);
    }
}
