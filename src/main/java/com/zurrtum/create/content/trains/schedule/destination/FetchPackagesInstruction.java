package com.zurrtum.create.content.trains.schedule.destination;

import com.zurrtum.create.catnip.data.Glob;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime.State;
import com.zurrtum.create.content.trains.station.GlobalPackagePort;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

public class FetchPackagesInstruction extends TextScheduleInstruction {
    public FetchPackagesInstruction(Identifier id) {
        super(id);
    }

    public String getFilter() {
        return getLabelText();
    }

    public String getFilterForRegex() {
        if (getFilter().isBlank())
            return Glob.toRegexPattern("*", "");
        return Glob.toRegexPattern(getFilter(), "");
    }

    @Override
    public boolean supportsConditions() {
        return true;
    }

    @Override
    public DiscoveredPath start(ScheduleRuntime runtime, World level) {
        MinecraftServer server = level.getServer();
        if (server == null)
            return null;

        String regex = getFilterForRegex();
        boolean anyMatch = false;
        ArrayList<GlobalStation> validStations = new ArrayList<>();
        Train train = runtime.train;

        if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
            train.status.missingConductor();
            runtime.startCooldown();
            return null;
        }

        for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
            ServerWorld dimLevel = server.getWorld(globalStation.blockEntityDimension);
            if (dimLevel == null)
                continue;

            for (Map.Entry<BlockPos, GlobalPackagePort> entry : globalStation.connectedPorts.entrySet()) {
                GlobalPackagePort port = entry.getValue();
                BlockPos pos = entry.getKey();

                Inventory postboxInventory = port.offlineBuffer;
                if (dimLevel.isPosLoaded(pos) && dimLevel.getBlockEntity(pos) instanceof PostboxBlockEntity ppbe)
                    postboxInventory = ppbe.inventory;

                for (ItemStack stack : postboxInventory) {
                    if (!PackageItem.isPackage(stack))
                        continue;
                    if (PackageItem.matchAddress(stack, port.address))
                        continue;
                    try {
                        if (!PackageItem.getAddress(stack).matches(regex))
                            continue;
                        anyMatch = true;
                        validStations.add(globalStation);
                    } catch (PatternSyntaxException ignored) {
                    }
                }
            }
        }

        if (validStations.isEmpty()) {
            runtime.startCooldown();
            runtime.state = State.PRE_TRANSIT;
            runtime.currentEntry++;
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