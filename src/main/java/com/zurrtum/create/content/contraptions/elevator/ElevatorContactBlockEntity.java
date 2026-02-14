package com.zurrtum.create.content.contraptions.elevator;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControlBehaviour;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

public class ElevatorContactBlockEntity extends SmartBlockEntity {

    public DoorControlBehaviour doorControls;
    public ColumnCoords columnCoords;
    public boolean activateBlock;

    public String shortName;
    public String longName;

    public String lastReportedCurrentFloor = "";

    private int yTargetFromNBT = Integer.MIN_VALUE;
    private boolean deferNameGenerator;

    public ElevatorContactBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ELEVATOR_CONTACT, pos, state);
        shortName = "";
        longName = "";
        deferNameGenerator = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(doorControls = new DoorControlBehaviour(this));
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);

        view.putString("ShortName", shortName);
        view.putString("LongName", longName);

        view.putString("LastReportedCurrentFloor", lastReportedCurrentFloor);

        if (clientPacket)
            return;
        view.putBoolean("Activate", activateBlock);
        if (columnCoords == null)
            return;

        ElevatorColumn column = ElevatorColumn.get(world, columnCoords);
        if (column == null)
            return;
        view.putInt("ColumnTarget", column.getTargetedYLevel());
        if (column.isActive())
            view.putBoolean("ColumnActive", true);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);

        shortName = view.getString("ShortName", "");
        longName = view.getString("LongName", "");

        view.getOptionalString("LastReportedCurrentFloor").ifPresent(string -> lastReportedCurrentFloor = string);

        if (clientPacket)
            return;
        activateBlock = view.getBoolean("Activate", false);
        Optional<Integer> columnTarget = view.getOptionalInt("ColumnTarget");
        if (columnTarget.isEmpty())
            return;

        int target = columnTarget.get();
        boolean active = view.getBoolean("ColumnActive", false);

        if (columnCoords == null) {
            yTargetFromNBT = target;
            return;
        }

        ElevatorColumn column = ElevatorColumn.getOrCreate(world, columnCoords);
        column.target(target);
        column.setActive(active);
    }

    public void updateDisplayedFloor(String floor) {
        if (floor.equals(lastReportedCurrentFloor))
            return;
        lastReportedCurrentFloor = floor;
        DisplayLinkBlock.notifyGatherers(world, pos);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (world.isClient())
            return;
        columnCoords = ElevatorContactBlock.getColumnCoords(world, pos);
        if (columnCoords == null)
            return;
        ElevatorColumn column = ElevatorColumn.getOrCreate(world, columnCoords);
        column.add(pos);
        if (shortName.isBlank())
            deferNameGenerator = true;
        if (yTargetFromNBT == Integer.MIN_VALUE)
            return;
        column.target(yTargetFromNBT);
        yTargetFromNBT = Integer.MIN_VALUE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!deferNameGenerator)
            return;
        if (columnCoords != null)
            ElevatorColumn.getOrCreate(world, columnCoords).initNames(world);
        deferNameGenerator = false;
    }

    @Override
    public void invalidate() {
        if (columnCoords != null) {
            ElevatorColumn column = ElevatorColumn.get(world, columnCoords);
            if (column != null)
                column.remove(pos);
        }
        super.invalidate();
    }

    public void updateName(String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
        this.deferNameGenerator = false;
        notifyUpdate();

        ElevatorColumn column = ElevatorColumn.get(world, columnCoords);
        if (column != null)
            column.namesChanged();
    }

    public Couple<String> getNames() {
        return Couple.create(shortName, longName);
    }

}
