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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

        if (entity.age % 10 != 0)
            return;

        ColumnCoords coords = getGlobalColumn();
        ElevatorColumn column = ElevatorColumn.get(entity.getEntityWorld(), coords);

        if (column == null)
            return;
        if (column.namesListVersion == namesListVersion)
            return;

        namesList = column.compileNamesList();
        namesListVersion = column.namesListVersion;
        if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().sendToOtherNearbyPlayers(entity, new ElevatorFloorListPacket(entity, namesList));
        }
    }

    @Override
    protected void disableActorOnStart(MovementContext context) {
    }

    public ColumnCoords getGlobalColumn() {
        return column.relative(anchor);
    }

    public Integer getCurrentTargetY(World level) {
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
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        if (!searchMovedStructure(world, pos, null))
            return false;
        if (blocks.size() <= 0)
            return false;
        if (contacts == 0)
            throw new AssemblyException(Text.translatable("create.gui.assembly.exception.no_contacts"));
        if (contacts > 1)
            throw new AssemblyException(Text.translatable("create.gui.assembly.exception.too_many_contacts"));
        ElevatorColumn column = ElevatorColumn.get(world, getGlobalColumn());
        if (column != null && column.isActive())
            throw new AssemblyException(Text.translatable("create.gui.assembly.exception.column_conflict"));
        startMoving(world);
        return true;
    }

    @Override
    protected Pair<StructureBlockInfo, BlockEntity> capture(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        if (!blockState.isOf(AllBlocks.REDSTONE_CONTACT))
            return super.capture(world, pos);

        Direction facing = blockState.get(RedstoneContactBlock.FACING);
        if (facing.getAxis() == Axis.Y)
            return super.capture(world, pos);

        contacts++;
        BlockPos local = toLocalPos(pos.offset(facing));
        column = new ColumnCoords(local.getX(), local.getZ(), facing.getOpposite());
        contactYOffset = local.getY();

        return super.capture(world, pos);
    }

    public int getContactYOffset() {
        return contactYOffset;
    }

    public void broadcastFloorData(World level, BlockPos contactPos) {
        ElevatorColumn column = ElevatorColumn.get(level, getGlobalColumn());
        if (!(level.getBlockEntity(contactPos) instanceof ElevatorContactBlockEntity ecbe))
            return;
        if (column != null)
            column.floorReached(level, ecbe.shortName);
    }

    @Override
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putBoolean("Arrived", arrived);
        view.put("Column", ColumnCoords.CODEC, column);
        view.putInt("ContactY", contactYOffset);
        view.putInt("MaxContactY", maxContactY);
        view.putInt("MinContactY", minContactY);
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        arrived = view.getBoolean("Arrived", false);
        column = view.read("Column", ColumnCoords.CODEC).orElseThrow();
        contactYOffset = view.getInt("ContactY", 0);
        maxContactY = view.getInt("MaxContactY", 0);
        minContactY = view.getInt("MinContactY", 0);
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
        for (MutablePair<StructureBlockInfo, MovementContext> pair : actors)
            if (pair.right != null && pair.right.temporaryData instanceof ElevatorFloorSelection efs)
                efs.currentIndex = floorIndex;
    }

}
