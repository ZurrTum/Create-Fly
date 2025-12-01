package com.zurrtum.create.content.redstone.displayLink;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class DisplayLinkBlockEntity extends LinkWithBulbBlockEntity implements TransformableBlockEntity {

    public BlockPos targetOffset;

    public DisplaySource activeSource;
    private NbtCompound sourceConfig;

    public DisplayTarget activeTarget;
    public int targetLine;

    public int refreshTicks;
    public AbstractComputerBehaviour computerBehaviour;
    public FactoryPanelSupportBehaviour factoryPanelSupport;

    public DisplayLinkBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.DISPLAY_LINK, pos, state);
        targetOffset = BlockPos.ORIGIN;
        sourceConfig = new NbtCompound();
        targetLine = 0;
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
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
        if (world.isClient())
            return;

        refreshTicks++;
        if (refreshTicks < activeSource.getPassiveRefreshTicks() || !activeSource.shouldPassiveReset())
            return;
        tickSource();
    }

    public void tickSource() {
        refreshTicks = 0;
        if (getCachedState().get(DisplayLinkBlock.POWERED, true))
            return;
        if (!world.isClient())
            updateGatheredData();
    }

    public void onNoLongerPowered() {
        if (activeSource == null)
            return;
        refreshTicks = 0;
        activeSource.onSignalReset(new DisplayLinkContext(world, this));
        updateGatheredData();
    }

    public void updateGatheredData() {
        BlockPos sourcePosition = getSourcePosition();
        BlockPos targetPosition = getTargetPosition();

        if (!world.isPosLoaded(targetPosition) || !world.isPosLoaded(sourcePosition))
            return;

        DisplayTarget target = DisplayTarget.get(world, targetPosition);
        List<DisplaySource> sources = DisplaySource.getAll(world, sourcePosition);
        boolean notify = false;

        if (activeTarget != target) {
            activeTarget = target;
            notify = true;
        }

        if (activeSource != null && !sources.contains(activeSource)) {
            activeSource = null;
            sourceConfig = new NbtCompound();
            notify = true;
        }

        if (notify)
            notifyUpdate();
        if (activeSource == null || activeTarget == null)
            return;

        DisplayLinkContext context = new DisplayLinkContext(world, this);
        activeSource.transferData(context, activeTarget, targetLine);
        sendPulseNextSync();
        sendData();

        award(AllAdvancements.DISPLAY_LINK);
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        writeGatheredData(view);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        writeGatheredData(view);
        if (clientPacket && activeTarget != null) {
            Identifier id = CreateRegistries.DISPLAY_TARGET.getId(this.activeTarget);
            if (id != null) {
                view.put("TargetType", Identifier.CODEC, id);
            }
        }
    }

    private void writeGatheredData(WriteView view) {
        view.put("TargetOffset", BlockPos.CODEC, targetOffset);
        view.putInt("TargetLine", targetLine);

        if (activeSource != null) {
            NbtCompound data = sourceConfig.copy();
            Identifier id = CreateRegistries.DISPLAY_SOURCE.getId(this.activeSource);
            if (id != null) {
                data.put("Id", Identifier.CODEC, id);
            }
            view.put("Source", NbtCompound.CODEC, data);
        }
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        targetOffset = view.read("TargetOffset", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        targetLine = view.getInt("TargetLine", 0);

        if (clientPacket) {
            view.read("TargetType", Identifier.CODEC).ifPresent(id -> activeTarget = DisplayTarget.get(id));
        }
        view.read("Source", NbtCompound.CODEC).ifPresent(data -> {
            activeSource = DisplaySource.get(data.get("Id", Identifier.CODEC).orElse(null));
            sourceConfig = activeSource != null ? data.copy() : new NbtCompound();
        });
    }

    public void target(BlockPos targetPosition) {
        this.targetOffset = targetPosition.subtract(pos);
    }

    public BlockPos getSourcePosition() {
        for (FactoryPanelPosition position : factoryPanelSupport.getLinkedPanels())
            return position.pos();
        return pos.offset(getDirection());
    }

    public NbtCompound getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(NbtCompound sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public Direction getDirection() {
        return getCachedState().get(DisplayLinkBlock.FACING, Direction.UP).getOpposite();
    }

    public BlockPos getTargetPosition() {
        return pos.add(targetOffset);
    }

    private static final Vec3d bulbOffset = VecHelper.voxelSpace(11, 7, 5);
    private static final Vec3d bulbOffsetVertical = VecHelper.voxelSpace(5, 7, 11);

    @Override
    public Vec3d getBulbOffset(BlockState state) {
        if (state.get(DisplayLinkBlock.FACING, Direction.UP).getAxis().isVertical())
            return bulbOffsetVertical;
        return bulbOffset;
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        targetOffset = transform.applyWithoutOffset(targetOffset);
        notifyUpdate();
    }

}
