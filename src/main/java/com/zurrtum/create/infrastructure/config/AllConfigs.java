package com.zurrtum.create.infrastructure.config;

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.infrastructure.packet.s2c.ServerConfigPacket;
import net.minecraft.resource.SynchronousResourceReloader;

import static com.zurrtum.create.Create.MOD_ID;

public class AllConfigs {
    private static CCommon common;
    private static CServer server;

    public static final SynchronousResourceReloader LISTENER = resourceManager -> {
        ServerConfigPacket.CACHE = null;
        server.reload(null);
    };

    public static CCommon common() {
        return common;
    }

    public static CServer server() {
        return server;
    }

    public static void register() {
        common = Builder.create(CCommon::new, MOD_ID, "common");
        server = Builder.create(CServer::new, MOD_ID, "server");

        CStress stress = server().kinetics.stressValues;
        BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
        BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
    }
}
