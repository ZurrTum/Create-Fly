package com.zurrtum.create.content.kinetics;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.levelWrappers.WorldHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.world.WorldAccess;

import java.util.HashMap;
import java.util.Map;

public class TorquePropagator {

    static Map<WorldAccess, Map<Long, KineticNetwork>> networks = new HashMap<>();

    public void onLoadWorld(WorldAccess world) {
        networks.put(world, new HashMap<>());
        Create.LOGGER.debug("Prepared Kinetic Network Space for " + WorldHelper.getDimensionID(world));
    }

    public void onUnloadWorld(WorldAccess world) {
        networks.remove(world);
        Create.LOGGER.debug("Removed Kinetic Network Space for " + WorldHelper.getDimensionID(world));
    }

    public KineticNetwork getOrCreateNetworkFor(KineticBlockEntity be) {
        Long id = be.network;
        KineticNetwork network;
        Map<Long, KineticNetwork> map = networks.computeIfAbsent(be.getWorld(), $ -> new HashMap<>());
        if (id == null)
            return null;

        if (!map.containsKey(id)) {
            network = new KineticNetwork();
            network.id = be.network;
            map.put(id, network);
        }
        network = map.get(id);
        return network;
    }

}