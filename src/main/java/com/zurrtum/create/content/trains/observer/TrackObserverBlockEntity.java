package com.zurrtum.create.content.trains.observer;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TrackObserverBlockEntity extends SmartBlockEntity implements TransformableBlockEntity, Clearable {

    public TrackTargetingBehaviour<TrackObserver> edgePoint;

    private ServerFilteringBehaviour filtering;

    public @Nullable UUID passingTrainUUID;

    public TrackObserverBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TRACK_OBSERVER, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.OBSERVER));
        behaviours.add(filtering = new ServerFilteringBehaviour(this).withCallback(this::onFilterChanged));
    }

    private void onFilterChanged(ItemStack newFilter) {
        if (world.isClient())
            return;
        TrackObserver observer = getObserver();
        if (observer != null)
            observer.setFilterAndNotify(world, newFilter);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient())
            return;

        boolean shouldBePowered = false;
        TrackObserver observer = getObserver();
        if (observer != null)
            shouldBePowered = observer.isActivated();
        if (isBlockPowered() == shouldBePowered)
            return;

        if (observer != null) {
            AbstractComputerBehaviour computer = AbstractComputerBehaviour.get(this);
            if (computer != null) {
                computer.queueTrainPass(observer, shouldBePowered);
            }
        }

        BlockState blockState = getCachedState();
        if (blockState.contains(TrackObserverBlock.POWERED))
            world.setBlockState(pos, blockState.with(TrackObserverBlock.POWERED, shouldBePowered), Block.NOTIFY_ALL);
        DisplayLinkBlock.notifyGatherers(world, pos);
    }

    @Nullable
    public TrackObserver getObserver() {
        return edgePoint.getEdgePoint();
    }

    public ItemStack getFilter() {
        return filtering.getFilter();
    }

    public boolean isBlockPowered() {
        return getCachedState().get(TrackObserverBlock.POWERED, false);
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(Vec3d.of(pos), Vec3d.of(edgePoint.getGlobalPosition())).expand(2);
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        edgePoint.transform(be, transform);
    }

    @Override
    public void clear() {
        filtering.setFilter(ItemStack.EMPTY);
    }
}
