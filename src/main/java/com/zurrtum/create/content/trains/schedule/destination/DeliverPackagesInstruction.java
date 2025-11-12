package com.zurrtum.create.content.trains.schedule.destination;

import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime.State;
import com.zurrtum.create.content.trains.station.GlobalPackagePort;
import com.zurrtum.create.content.trains.station.GlobalStation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DeliverPackagesInstruction extends ScheduleInstruction {
    public DeliverPackagesInstruction(Identifier id) {
        super(id);
    }

    @Override
    public boolean supportsConditions() {
        return true;
    }

    @Override
    @Nullable
    public DiscoveredPath start(ScheduleRuntime runtime, Level level) {
        boolean anyMatch = false;
        String firstPackage = null;
        ArrayList<GlobalStation> validStations = new ArrayList<>();
        Train train = runtime.train;

        if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
            train.status.missingConductor();
            runtime.startCooldown();
            return null;
        }

        for (Carriage carriage : train.carriages) {
            Container carriageInventory = carriage.storage.getAllItems();
            if (carriageInventory == null)
                continue;

            // Export to station
            for (ItemStack stack : carriageInventory) {
                if (!PackageItem.isPackage(stack))
                    continue;
                if (firstPackage == null)
                    firstPackage = PackageItem.getAddress(stack);
                for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
                    for (Map.Entry<BlockPos, GlobalPackagePort> port : globalStation.connectedPorts.entrySet()) {
                        if (!PackageItem.matchAddress(stack, port.getValue().address))
                            continue;
                        anyMatch = true;
                        validStations.add(globalStation);
                        break;
                    }
                }
            }
        }

        if (validStations.isEmpty()) {
            if (firstPackage != null) {
                train.status.failedPackageNoTarget(firstPackage);
                runtime.startCooldown();
            } else {
                runtime.state = State.PRE_TRANSIT;
                runtime.currentEntry++;
            }
            return null;
        }

        DiscoveredPath best = train.navigation.findPathTo(validStations, Double.MAX_VALUE);
        if (best == null) {
            if (anyMatch)
                train.status.failedNavigation();
            runtime.startCooldown();
            return null;
        }

        return best;
    }

}