package com.zurrtum.create.content.contraptions.elevator;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement.ElevatorFloorSelection;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.content.contraptions.pulley.PulleyContraption;
import com.zurrtum.create.content.redstone.contact.RedstoneContactBlock;
import com.zurrtum.create.infrastructure.packet.s2c.ElevatorFloorListPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ElevatorContraption extends PulleyContraption {

    protected ColumnCoords column;
    protected int contactYOffset;
    public boolean arrived;

    private int namesListVersion = -1;
    public List<IntAttached<Couple<String>>> namesList = ImmutableList.of();
    public int clientYTarget;

    public int maxContactY;
    public int minContactY;

    // during assembly only
    private int contacts;

    public ElevatorContraption() {
        super();
    }

    public ElevatorContraption(int initialOffset) {
        super(initialOffset);
    }

    @Override
    public void tickStorage(AbstractContraptionEntity entity) {
        super.tickStorage(entity);

        if (entity.tickCount % 10 != 0)
            return;

        ColumnCoords coords = getGlobalColumn();
        ElevatorColumn column = ElevatorColumn.get(entity.level(), coords);

        if (column == null)
            return;
        if (column.namesListVersion == namesListVersion)
            return;

        namesList = column.compileNamesList();
        namesListVersion = column.namesListVersion;
        if (entity.level() instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource().sendToTrackingPlayers(entity, new ElevatorFloorListPacket(entity, namesList));
        }
    }

    @Override
    protected void disableActorOnStart(MovementContext context) {
    }

    public ColumnCoords getGlobalColumn() {
        return column.relative(anchor);
    }

    @Nullable
    public Integer getCurrentTargetY(Level level) {
        ColumnCoords coords = getGlobalColumn();
        ElevatorColumn column = ElevatorColumn.get(level, coords);
        if (column == null)
            return null;
        if (!column.isTargetAvailable())
            return null;
        int targetedYLevel = column.getTargetedYLevel();
        if (isTargetUnreachable(targetedYLevel))
            return null;
        return targetedYLevel;
    }

    public boolean isTargetUnreachable(int contactY) {
        return contactY < minContactY || contactY > maxContactY;
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        if (!searchMovedStructure(world, pos, null))
            return false;
        if (blocks.size() <= 0)
            return false;
        if (contacts == 0)
            throw new AssemblyException(Component.translatable("create.gui.assembly.exception.no_contacts"));
        if (contacts > 1)
            throw new AssemblyException(Component.translatable("create.gui.assembly.exception.too_many_contacts"));
        ElevatorColumn column = ElevatorColumn.get(world, getGlobalColumn());
        if (column != null && column.isActive())
            throw new AssemblyException(Component.translatable("create.gui.assembly.exception.column_conflict"));
        startMoving(world);
        return true;
    }

    @Override
    protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        if (!blockState.is(AllBlocks.REDSTONE_CONTACT))
            return super.capture(world, pos);

        Direction facing = blockState.getValue(RedstoneContactBlock.FACING);
        if (facing.getAxis() == Axis.Y)
            return super.capture(world, pos);

        contacts++;
        BlockPos local = toLocalPos(pos.relative(facing));
        column = new ColumnCoords(local.getX(), local.getZ(), facing.getOpposite());
        contactYOffset = local.getY();

        return super.capture(world, pos);
    }

    public int getContactYOffset() {
        return contactYOffset;
    }

    public void broadcastFloorData(Level level, BlockPos contactPos) {
        ElevatorColumn column = ElevatorColumn.get(level, getGlobalColumn());
        if (!(level.getBlockEntity(contactPos) instanceof ElevatorContactBlockEntity ecbe))
            return;
        if (column != null)
            column.floorReached(level, ecbe.shortName);
    }

    @Override
    public void write(ValueOutput view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putBoolean("Arrived", arrived);
        view.store("Column", ColumnCoords.CODEC, column);
        view.putInt("ContactY", contactYOffset);
        view.putInt("MaxContactY", maxContactY);
        view.putInt("MinContactY", minContactY);
    }

    @Override
    public void read(Level world, ValueInput view, boolean spawnData) {
        arrived = view.getBooleanOr("Arrived", false);
        column = view.read("Column", ColumnCoords.CODEC).orElseThrow();
        contactYOffset = view.getIntOr("ContactY", 0);
        maxContactY = view.getIntOr("MaxContactY", 0);
        minContactY = view.getIntOr("MinContactY", 0);
        super.read(world, view, spawnData);
    }

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.ELEVATOR;
    }

    public void setClientYTarget(int clientYTarget) {
        if (this.clientYTarget == clientYTarget)
            return;

        this.clientYTarget = clientYTarget;
        syncControlDisplays();
    }

    public void syncControlDisplays() {
        if (namesList.isEmpty())
            return;
        for (int i = 0; i < namesList.size(); i++)
            if (namesList.get(i).getFirst() == clientYTarget)
                setAllControlsToFloor(i);
    }

    public void setAllControlsToFloor(int floorIndex) {
        for (MutablePair<StructureBlockInfo, @Nullable MovementContext> pair : actors)
            if (pair.right != null && pair.right.temporaryData instanceof ElevatorFloorSelection efs)
                efs.currentIndex = floorIndex;
    }

}
