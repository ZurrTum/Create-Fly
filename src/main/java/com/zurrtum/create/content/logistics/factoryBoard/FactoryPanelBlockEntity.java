package com.zurrtum.create.content.logistics.factoryBoard;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

public class FactoryPanelBlockEntity extends SmartBlockEntity {
    public EnumMap<PanelSlot, ServerFactoryPanelBehaviour> panels;

    public boolean redraw;
    public boolean restocker;
    public VoxelShape lastShape;

    public FactoryPanelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.FACTORY_PANEL, pos, state);
        restocker = false;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(8);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        panels = new EnumMap<>(PanelSlot.class);
        redraw = true;
        for (PanelSlot slot : PanelSlot.values()) {
            ServerFactoryPanelBehaviour e = new ServerFactoryPanelBehaviour(this, slot);
            panels.put(slot, e);
            behaviours.add(e);
        }
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.FACTORY_GAUGE);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level.isClientSide())
            return;

        if (activePanels() == 0)
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());

        BlockState state = getBlockState();
        if (state.is(AllBlocks.FACTORY_GAUGE)) {
            boolean shouldBeRestocker = level.getBlockState(worldPosition.relative(FactoryPanelBlock.connectedDirection(state).getOpposite()))
                .is(AllBlocks.PACKAGER);
            if (restocker == shouldBeRestocker)
                return;
            restocker = shouldBeRestocker;
            redraw = true;
            sendData();
        }
    }

    @Nullable
    public PackagerBlockEntity getRestockedPackager() {
        BlockState state = getBlockState();
        if (!restocker || !state.is(AllBlocks.FACTORY_GAUGE))
            return null;
        BlockPos packagerPos = worldPosition.relative(FactoryPanelBlock.connectedDirection(state).getOpposite());
        if (!level.isLoaded(packagerPos))
            return null;
        BlockEntity be = level.getBlockEntity(packagerPos);
        if (!(be instanceof PackagerBlockEntity pbe))
            return null;
        if (pbe instanceof RepackagerBlockEntity)
            return null;
        return pbe;
    }

    public int activePanels() {
        int result = 0;
        for (ServerFactoryPanelBehaviour panelBehaviour : panels.values())
            if (panelBehaviour.isActive())
                result++;
        return result;
    }

    @Override
    public void remove() {
        for (ServerFactoryPanelBehaviour panelBehaviour : panels.values())
            if (panelBehaviour.isActive())
                panelBehaviour.disconnectAll();
        super.remove();
    }

    @Override
    public void destroy() {
        super.destroy();
        int panelCount = activePanels();
        if (panelCount > 1)
            Block.popResource(level, worldPosition, new ItemStack(AllItems.FACTORY_GAUGE, panelCount - 1));
    }

    public boolean addPanel(PanelSlot slot, UUID frequency) {
        ServerFactoryPanelBehaviour behaviour = panels.get(slot);
        if (behaviour != null && !behaviour.isActive()) {
            behaviour.enable();
            if (frequency != null)
                behaviour.setNetwork(frequency);
            redraw = true;
            lastShape = null;

            if (activePanels() > 1) {
                SoundType soundType = getBlockState().getSoundType();
                level.playSound(
                    null,
                    worldPosition,
                    soundType.getPlaceSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F,
                    soundType.getPitch() * 0.8F
                );
            }

            return true;
        }
        return false;
    }

    public boolean removePanel(PanelSlot slot) {
        ServerFactoryPanelBehaviour behaviour = panels.get(slot);
        if (behaviour != null && behaviour.isActive()) {
            behaviour.disable();
            redraw = true;
            lastShape = null;

            if (activePanels() > 0) {
                SoundType soundType = getBlockState().getSoundType();
                level.playSound(
                    null,
                    worldPosition,
                    soundType.getBreakSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F,
                    soundType.getPitch() * 0.8F
                );
            }

            return true;
        }
        return false;
    }

    public VoxelShape getShape() {
        if (lastShape != null)
            return lastShape;

        float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(getBlockState()) + 90;
        float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(getBlockState());
        Direction connectedDirection = FactoryPanelBlock.connectedDirection(getBlockState());
        Vec3 inflateAxes = VecHelper.axisAlingedPlaneOf(connectedDirection);

        lastShape = Shapes.empty();

        for (ServerFactoryPanelBehaviour behaviour : panels.values()) {
            if (!behaviour.isActive())
                continue;
            FactoryPanelPosition panelPosition = behaviour.getPanelPosition();
            Vec3 vec = new Vec3(.25 + panelPosition.slot().xOffset * .5, 1 / 16f, .25 + panelPosition.slot().yOffset * .5);
            vec = VecHelper.rotateCentered(vec, 180, Axis.Y);
            vec = VecHelper.rotateCentered(vec, xRot, Axis.X);
            vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);
            AABB bb = new AABB(vec, vec).inflate(1 / 16f).inflate(inflateAxes.x * 3 / 16f, inflateAxes.y * 3 / 16f, inflateAxes.z * 3 / 16f);
            lastShape = Shapes.or(lastShape, Shapes.create(bb));
        }

        return lastShape;
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        restocker = view.getBooleanOr("Restocker", false);
        if (clientPacket && view.getBooleanOr("Redraw", false)) {
            lastShape = null;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
        }
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Restocker", restocker);
        if (clientPacket && redraw) {
            view.putBoolean("Redraw", true);
            redraw = false;
        }
    }
}
