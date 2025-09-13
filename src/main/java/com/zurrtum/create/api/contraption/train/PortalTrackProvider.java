package com.zurrtum.create.api.contraption.train;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.trains.track.AllPortalTracks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Portal;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A provider for portal track connections.
 * Takes a track inbound through a portal and finds the exit location for the outbound track.
 */
@FunctionalInterface
public interface PortalTrackProvider {
    SimpleRegistry<Block, PortalTrackProvider> REGISTRY = SimpleRegistry.create();

    /**
     * Find the exit location for a track going through a portal.
     *
     * @param level the level of the inbound track
     * @param face  the face of the inbound track
     */
    Exit findExit(ServerWorld level, BlockFace face);

    /**
     * Checks if a given {@link BlockState} represents a supported portal block.
     *
     * @param state The block state to check.
     * @return {@code true} if the block state represents a supported portal; {@code false} otherwise.
     */
    static boolean isSupportedPortal(BlockState state) {
        return REGISTRY.get(state) != null;
    }

    /**
     * Retrieves the corresponding outbound track on the other side of a portal.
     *
     * @param level        The current {@link ServerWorld}.
     * @param inboundTrack The inbound track {@link BlockFace}.
     * @return the found outbound track, or null if one wasn't found.
     */
    @Nullable
    static Exit getOtherSide(ServerWorld level, BlockFace inboundTrack) {
        BlockPos portalPos = inboundTrack.getConnectedPos();
        BlockState portalState = level.getBlockState(portalPos);
        PortalTrackProvider provider = REGISTRY.get(portalState);
        return provider == null ? null : provider.findExit(level, inboundTrack);
    }

    /**
     * Find an exit location by using an {@link Portal} instance.
     *
     * @param level           The level of the inbound track
     * @param face            The face of the inbound track
     * @param firstDimension  The first dimension (typically the Overworld)
     * @param secondDimension The second dimension (e.g., Nether, Aether)
     * @param portal          The portal
     * @return A found exit, or null if one wasn't found
     */
    static Exit fromPortal(ServerWorld level, BlockFace face, RegistryKey<World> firstDimension, RegistryKey<World> secondDimension, Portal portal) {
        return AllPortalTracks.fromPortal(level, face, firstDimension, secondDimension, portal);
    }

    record Exit(ServerWorld level, BlockFace face) {
    }
}
