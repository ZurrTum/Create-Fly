package com.zurrtum.create.content.trains;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.content.trains.display.GlobalTrainDisplayData;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackGraphSync;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import com.zurrtum.create.infrastructure.packet.s2c.RemoveTrainPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class GlobalRailwayManager {

    public Map<UUID, TrackGraph> trackNetworks;
    public Map<UUID, SignalEdgeGroup> signalEdgeGroups;
    public Map<UUID, Train> trains;
    public TrackGraphSync sync;

    private List<Train> movingTrains;
    private List<Train> waitingTrains;

    private @Nullable RailwaySavedData savedData;

    public int version;

    public GlobalRailwayManager() {
        cleanUp();
    }

    public void playerLogin(MinecraftServer server, ServerPlayer player) {
        loadTrackData(server);
        for (TrackGraph g : trackNetworks.values()) {
            sync.sendFullGraphTo(g, player);
        }
        ArrayList<SignalEdgeGroup> asList = new ArrayList<>(signalEdgeGroups.values());
        sync.sendEdgeGroups(asList.stream().map(g -> g.id).toList(), asList.stream().map(g -> g.color).toList(), player);
        for (Train train : trains.values())
            player.connection.send(new AddTrainPacket(train));
    }

    public void levelLoaded(MinecraftServer server) {
        cleanUp();
        savedData = null;
        loadTrackData(server);
    }

    private void loadTrackData(MinecraftServer server) {
        if (savedData != null)
            return;
        savedData = RailwaySavedData.load(server);
        trains = savedData.getTrains();
        trackNetworks = savedData.getTrackNetworks();
        signalEdgeGroups = savedData.getSignalBlocks();
        movingTrains.addAll(trains.values());
    }

    public void cleanUp() {
        trackNetworks = new HashMap<>();
        signalEdgeGroups = new HashMap<>();
        trains = new HashMap<>();
        sync = new TrackGraphSync();
        movingTrains = new LinkedList<>();
        waitingTrains = new LinkedList<>();
        GlobalTrainDisplayData.statusByDestination.clear();
    }

    public void markTracksDirty() {
        if (savedData != null)
            savedData.setDirty();
    }

    public void addTrain(Train train) {
        trains.put(train.id, train);
        movingTrains.add(train);
    }

    public void removeTrain(UUID id) {
        Train removed = trains.remove(id);
        if (removed == null)
            return;
        movingTrains.remove(removed);
        waitingTrains.remove(removed);
    }

    //

    public TrackGraph getOrCreateGraph(UUID graphID, int netId) {
        return trackNetworks.computeIfAbsent(
            graphID, uid -> {
                TrackGraph trackGraph = new TrackGraph(graphID);
                trackGraph.setNetId(netId);
                return trackGraph;
            }
        );
    }

    public void putGraphWithDefaultGroup(MinecraftServer server, TrackGraph graph) {
        SignalEdgeGroup group = new SignalEdgeGroup(graph.id);
        signalEdgeGroups.put(graph.id, group.asFallback());
        sync.edgeGroupCreated(server, graph.id, group.color);
        putGraph(graph);
    }

    public void putGraph(TrackGraph graph) {
        trackNetworks.put(graph.id, graph);
        markTracksDirty();
    }

    public void removeGraphAndGroup(MinecraftServer server, TrackGraph graph) {
        signalEdgeGroups.remove(graph.id);
        sync.edgeGroupRemoved(server, graph.id);
        removeGraph(graph);
    }

    public void removeGraph(TrackGraph graph) {
        trackNetworks.remove(graph.id);
        markTracksDirty();
    }

    public void updateSplitGraph(LevelAccessor level, TrackGraph graph) {
        Set<TrackGraph> disconnected = graph.findDisconnectedGraphs(level, null);
        MinecraftServer server = level.getServer();
        for (TrackGraph d : disconnected) {
            putGraphWithDefaultGroup(server, d);
        }
        if (!disconnected.isEmpty()) {
            sync.graphSplit(graph, disconnected);
            markTracksDirty();
        }
    }

    @Nullable
    public TrackGraph getGraph(TrackNodeLocation vertex) {
        if (trackNetworks == null)
            return null;
        for (TrackGraph railGraph : trackNetworks.values())
            if (railGraph.locateNode(vertex) != null)
                return railGraph;
        return null;
    }

    public List<TrackGraph> getGraphs(TrackNodeLocation vertex) {
        if (trackNetworks == null)
            return Collections.emptyList();
        ArrayList<TrackGraph> intersecting = new ArrayList<>();
        for (TrackGraph railGraph : trackNetworks.values())
            if (railGraph.locateNode(vertex) != null)
                intersecting.add(railGraph);
        return intersecting;
    }

    public void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD)
            return;

        for (SignalEdgeGroup group : signalEdgeGroups.values()) {
            group.trains.clear();
            group.reserved = null;
        }

        MinecraftServer server = level.getServer();
        for (TrackGraph graph : trackNetworks.values()) {
            graph.tickPoints(server, true);
            graph.resolveIntersectingEdgeGroups(level);
        }

        tickTrains(level);

        for (TrackGraph graph : trackNetworks.values()) {
            graph.tickPoints(server, false);
        }

        GlobalTrainDisplayData.updateTick = level.getGameTime() % 100 == 0;
        if (GlobalTrainDisplayData.updateTick)
            GlobalTrainDisplayData.refresh();

        //		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_H) && AllKeys.altDown())
        //			for (TrackGraph trackGraph : trackNetworks.values())
        //				TrackGraphVisualizer.debugViewSignalData(trackGraph);
        //		if (AllKeys.isKeyDown(GLFW.GLFW_KEY_J) && AllKeys.altDown())
        //			for (TrackGraph trackGraph : trackNetworks.values())
        //				TrackGraphVisualizer.debugViewNodes(trackGraph);
    }

    private void tickTrains(Level level) {
        // keeping two lists ensures a tick order starting at longest waiting
        for (Train train : waitingTrains)
            train.earlyTick(level);
        for (Train train : movingTrains)
            train.earlyTick(level);
        for (Train train : waitingTrains)
            train.tick(level);
        for (Train train : movingTrains)
            train.tick(level);

        PlayerList playerManager = level.getServer().getPlayerList();
        for (Iterator<Train> iterator = waitingTrains.iterator(); iterator.hasNext(); ) {
            Train train = iterator.next();

            if (train.invalid) {
                iterator.remove();
                trains.remove(train.id);
                playerManager.broadcastAll(new RemoveTrainPacket(train));
                continue;
            }

            if (train.navigation.waitingForSignal != null)
                continue;
            movingTrains.add(train);
            iterator.remove();
        }

        for (Iterator<Train> iterator = movingTrains.iterator(); iterator.hasNext(); ) {
            Train train = iterator.next();

            if (train.invalid) {
                iterator.remove();
                trains.remove(train.id);
                playerManager.broadcastAll(new RemoveTrainPacket(train));
                continue;
            }

            if (train.navigation.waitingForSignal == null)
                continue;
            waitingTrains.add(train);
            iterator.remove();
        }

    }

    public GlobalRailwayManager sided(@Nullable LevelAccessor level) {
        if (level != null && !level.isClientSide())
            return this;
        return AllClientHandle.INSTANCE.getGlobalRailwayManager();
    }

}
