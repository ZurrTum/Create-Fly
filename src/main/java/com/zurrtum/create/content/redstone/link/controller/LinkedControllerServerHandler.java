package com.zurrtum.create.content.redstone.link.controller;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.redstone.link.IRedstoneLinkable;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.*;

public class LinkedControllerServerHandler {

    public static WorldAttached<Map<UUID, Collection<ManualFrequencyEntry>>> receivedInputs = new WorldAttached<>($ -> new HashMap<>());
    static final int TIMEOUT = 30;

    public static void tick(WorldAccess world) {
        Map<UUID, Collection<ManualFrequencyEntry>> map = receivedInputs.get(world);
        for (Iterator<Map.Entry<UUID, Collection<ManualFrequencyEntry>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {

            Map.Entry<UUID, Collection<ManualFrequencyEntry>> entry = iterator.next();
            Collection<ManualFrequencyEntry> list = entry.getValue();

            for (Iterator<ManualFrequencyEntry> entryIterator = list.iterator(); entryIterator.hasNext(); ) {
                ManualFrequencyEntry manualFrequencyEntry = entryIterator.next();
                manualFrequencyEntry.decrement();
                if (!manualFrequencyEntry.isAlive()) {
                    Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(world, manualFrequencyEntry);
                    entryIterator.remove();
                }
            }

            if (list.isEmpty())
                iterator.remove();
        }
    }

    public static void receivePressed(WorldAccess world, BlockPos pos, UUID uniqueID, List<Couple<Frequency>> collect, boolean pressed) {
        Map<UUID, Collection<ManualFrequencyEntry>> map = receivedInputs.get(world);
        Collection<ManualFrequencyEntry> list = map.computeIfAbsent(uniqueID, $ -> new ArrayList<>());

        WithNext:
        for (Couple<Frequency> activated : collect) {
            for (ManualFrequencyEntry entry : list) {
                if (entry.getSecond().equals(activated)) {
                    if (!pressed)
                        entry.setFirst(0);
                    else
                        entry.updatePosition(pos);
                    continue WithNext;
                }
            }

            if (!pressed)
                continue;

            ManualFrequencyEntry entry = new ManualFrequencyEntry(pos, activated);
            Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(world, entry);
            list.add(entry);

            for (IRedstoneLinkable linkable : Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(world, entry))
                if (linkable instanceof ServerLinkBehaviour lb && lb.isListening()) {
                    if (world.getPlayerByUuid(uniqueID) instanceof ServerPlayerEntity player) {
                        AllAdvancements.LINKED_CONTROLLER.trigger(player);
                    }
                }
        }
    }

    public static class ManualFrequencyEntry extends IntAttached<Couple<Frequency>> implements IRedstoneLinkable {

        private BlockPos pos;

        public ManualFrequencyEntry(BlockPos pos, Couple<Frequency> second) {
            super(TIMEOUT, second);
            this.pos = pos;
        }

        public void updatePosition(BlockPos pos) {
            this.pos = pos;
            setFirst(TIMEOUT);
        }

        @Override
        public int getTransmittedStrength() {
            return isAlive() ? 15 : 0;
        }

        @Override
        public boolean isAlive() {
            return getFirst() > 0;
        }

        @Override
        public BlockPos getLocation() {
            return pos;
        }

        @Override
        public void setReceivedStrength(int power) {
        }

        @Override
        public boolean isListening() {
            return false;
        }

        @Override
        public Couple<Frequency> getNetworkKey() {
            return getSecond();
        }

    }

}
