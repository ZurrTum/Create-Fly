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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
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
    protected Box createRenderBoundingBox() {
        return new Box(pos).expand(8);
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
        if (world.isClient())
            return;

        if (activePanels() == 0)
            world.setBlockState(pos, Blocks.AIR.getDefaultState());

        BlockState state = getCachedState();
        if (state.isOf(AllBlocks.FACTORY_GAUGE)) {
            boolean shouldBeRestocker = world.getBlockState(pos.offset(FactoryPanelBlock.connectedDirection(state).getOpposite()))
                .isOf(AllBlocks.PACKAGER);
            if (restocker == shouldBeRestocker)
                return;
            restocker = shouldBeRestocker;
            redraw = true;
            sendData();
        }
    }

    @Nullable
    public PackagerBlockEntity getRestockedPackager() {
        BlockState state = getCachedState();
        if (!restocker || !state.isOf(AllBlocks.FACTORY_GAUGE))
            return null;
        BlockPos packagerPos = pos.offset(FactoryPanelBlock.connectedDirection(state).getOpposite());
        if (!world.isPosLoaded(packagerPos))
            return null;
        BlockEntity be = world.getBlockEntity(packagerPos);
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
            Block.dropStack(world, pos, new ItemStack(AllItems.FACTORY_GAUGE, panelCount - 1));
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
                BlockSoundGroup soundType = getCachedState().getSoundGroup();
                world.playSound(
                    null,
                    pos,
                    soundType.getPlaceSound(),
                    SoundCategory.BLOCKS,
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
                BlockSoundGroup soundType = getCachedState().getSoundGroup();
                world.playSound(
                    null,
                    pos,
                    soundType.getBreakSound(),
                    SoundCategory.BLOCKS,
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

        float xRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getXRot(getCachedState()) + 90;
        float yRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getYRot(getCachedState());
        Direction connectedDirection = FactoryPanelBlock.connectedDirection(getCachedState());
        Vec3d inflateAxes = VecHelper.axisAlingedPlaneOf(connectedDirection);

        lastShape = VoxelShapes.empty();

        for (ServerFactoryPanelBehaviour behaviour : panels.values()) {
            if (!behaviour.isActive())
                continue;
            FactoryPanelPosition panelPosition = behaviour.getPanelPosition();
            Vec3d vec = new Vec3d(.25 + panelPosition.slot().xOffset * .5, 1 / 16f, .25 + panelPosition.slot().yOffset * .5);
            vec = VecHelper.rotateCentered(vec, 180, Axis.Y);
            vec = VecHelper.rotateCentered(vec, xRot, Axis.X);
            vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);
            Box bb = new Box(vec, vec).expand(1 / 16f).expand(inflateAxes.x * 3 / 16f, inflateAxes.y * 3 / 16f, inflateAxes.z * 3 / 16f);
            lastShape = VoxelShapes.union(lastShape, VoxelShapes.cuboid(bb));
        }

        return lastShape;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        restocker = view.getBoolean("Restocker", false);
        if (clientPacket && view.getBoolean("Redraw", false)) {
            lastShape = null;
            world.updateListeners(getPos(), getCachedState(), getCachedState(), 16);
        }
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Restocker", restocker);
        if (clientPacket && redraw) {
            view.putBoolean("Redraw", true);
            redraw = false;
        }
    }
}
