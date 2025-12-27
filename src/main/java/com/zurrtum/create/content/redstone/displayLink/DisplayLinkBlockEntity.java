package com.zurrtum.create.content.redstone.displayLink;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DisplayLinkBlockEntity extends LinkWithBulbBlockEntity implements TransformableBlockEntity {

    public BlockPos targetOffset;

    public DisplaySource activeSource;
    private CompoundTag sourceConfig;

    public DisplayTarget activeTarget;
    public int targetLine;

    public int refreshTicks;
    public FactoryPanelSupportBehaviour factoryPanelSupport;

    public DisplayLinkBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.DISPLAY_LINK, pos, state);
        targetOffset = BlockPos.ZERO;
        sourceConfig = new CompoundTag();
        targetLine = 0;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(factoryPanelSupport = new FactoryPanelSupportBehaviour(this, () -> false, () -> false, this::updateGatheredData));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.DISPLAY_LINK, AllAdvancements.DISPLAY_BOARD);
    }

    @Override
    public void tick() {
        super.tick();

        if (isVirtual())
            return;
        if (activeSource == null)
            return;
        if (level.isClientSide())
            return;

        refreshTicks++;
        if (refreshTicks < activeSource.getPassiveRefreshTicks() || !activeSource.shouldPassiveReset())
            return;
        tickSource();
    }

    public void tickSource() {
        refreshTicks = 0;
        if (getBlockState().getValueOrElse(DisplayLinkBlock.POWERED, true))
            return;
        if (!level.isClientSide())
            updateGatheredData();
    }

    public void onNoLongerPowered() {
        if (activeSource == null)
            return;
        refreshTicks = 0;
        activeSource.onSignalReset(new DisplayLinkContext(level, this));
        updateGatheredData();
    }

    public void updateGatheredData() {
        BlockPos sourcePosition = getSourcePosition();
        BlockPos targetPosition = getTargetPosition();

        if (!level.isLoaded(targetPosition) || !level.isLoaded(sourcePosition))
            return;

        DisplayTarget target = DisplayTarget.get(level, targetPosition);
        List<DisplaySource> sources = DisplaySource.getAll(level, sourcePosition);
        boolean notify = false;

        if (activeTarget != target) {
            activeTarget = target;
            notify = true;
        }

        if (activeSource != null && !sources.contains(activeSource)) {
            activeSource = null;
            sourceConfig = new CompoundTag();
            notify = true;
        }

        if (notify)
            notifyUpdate();
        if (activeSource == null || activeTarget == null)
            return;

        DisplayLinkContext context = new DisplayLinkContext(level, this);
        activeSource.transferData(context, activeTarget, targetLine);
        sendPulseNextSync();
        sendData();

        award(AllAdvancements.DISPLAY_LINK);
    }

    @Override
    public void writeSafe(ValueOutput view) {
        super.writeSafe(view);
        writeGatheredData(view);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        writeGatheredData(view);
        if (clientPacket && activeTarget != null) {
            Identifier id = CreateRegistries.DISPLAY_TARGET.getKey(this.activeTarget);
            if (id != null) {
                view.store("TargetType", Identifier.CODEC, id);
            }
        }
    }

    private void writeGatheredData(ValueOutput view) {
        view.store("TargetOffset", BlockPos.CODEC, targetOffset);
        view.putInt("TargetLine", targetLine);

        if (activeSource != null) {
            CompoundTag data = sourceConfig.copy();
            Identifier id = CreateRegistries.DISPLAY_SOURCE.getKey(this.activeSource);
            if (id != null) {
                data.store("Id", Identifier.CODEC, id);
            }
            view.store("Source", CompoundTag.CODEC, data);
        }
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        targetOffset = view.read("TargetOffset", BlockPos.CODEC).orElse(BlockPos.ZERO);
        targetLine = view.getIntOr("TargetLine", 0);

        if (clientPacket) {
            view.read("TargetType", Identifier.CODEC).ifPresent(id -> activeTarget = DisplayTarget.get(id));
        }
        view.read("Source", CompoundTag.CODEC).ifPresent(data -> {
            activeSource = DisplaySource.get(data.read("Id", Identifier.CODEC).orElse(null));
            sourceConfig = activeSource != null ? data.copy() : new CompoundTag();
        });
    }

    public void target(BlockPos targetPosition) {
        this.targetOffset = targetPosition.subtract(worldPosition);
    }

    public BlockPos getSourcePosition() {
        for (FactoryPanelPosition position : factoryPanelSupport.getLinkedPanels())
            return position.pos();
        return worldPosition.relative(getDirection());
    }

    public CompoundTag getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(CompoundTag sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public Direction getDirection() {
        return getBlockState().getValueOrElse(DisplayLinkBlock.FACING, Direction.UP).getOpposite();
    }

    public BlockPos getTargetPosition() {
        return worldPosition.offset(targetOffset);
    }

    private static final Vec3 bulbOffset = VecHelper.voxelSpace(11, 7, 5);
    private static final Vec3 bulbOffsetVertical = VecHelper.voxelSpace(5, 7, 11);

    @Override
    public Vec3 getBulbOffset(BlockState state) {
        if (state.getValueOrElse(DisplayLinkBlock.FACING, Direction.UP).getAxis().isVertical())
            return bulbOffsetVertical;
        return bulbOffset;
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        targetOffset = transform.applyWithoutOffset(targetOffset);
        notifyUpdate();
    }

}
