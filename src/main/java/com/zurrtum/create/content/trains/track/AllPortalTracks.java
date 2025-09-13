package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.train.PortalTrackProvider;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Portal;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

/**
 * Manages portal track integrations for various dimensions and mods within the Create mod.
 * <p>
 * Portals must be entered from the side and must lead to a different dimension than the one entered from.
 * This class handles the registration and functionality of portal tracks for standard and modded portals.
 * </p>
 */
public class AllPortalTracks {
    /**
     * Registers a portal track integration for a given block identified by its {@link Identifier}, if it exists.
     * If it does not, a warning will be logged.
     *
     * @param id       The resource location of the portal block.
     * @param provider The portal track provider for the block.
     */
    public static void tryRegisterIntegration(Identifier id, PortalTrackProvider provider) {
        if (Registries.BLOCK.containsId(id)) {
            Block block = Registries.BLOCK.get(id);
            PortalTrackProvider.REGISTRY.register(block, provider);
        } else {
            Create.LOGGER.warn("Portal for integration wasn't found: {}. Compat outdated?", id);
        }
    }

    /**
     * Registers a simple portal track integration for a given block identified by its {@link Identifier}, if it exists.
     * If it does not, a warning will be logged.
     * <p>
     * Note: This only allows registering integrations that go from the Overworld to another dimension and vice versa.
     *
     * @param portalBlockId The resource location of the portal block.
     * @param dimensionId   The resource location of the dimension to travel to
     */
    private static void tryRegisterSimpleInteraction(Identifier portalBlockId, Identifier dimensionId) {
        RegistryKey<World> levelKey = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
        tryRegisterSimpleInteraction(portalBlockId, levelKey);
    }

    /**
     * Registers a simple portal track integration for a given block identified by its {@link Identifier}, if it exists.
     * If it does not, a warning will be logged.
     * <p>
     * Note: This only allows registering integrations that go from the Overworld to another dimension and vice versa.
     *
     * @param portalBlockId The resource location of the portal block.
     * @param levelKey      The resource key of the dimension to travel to
     */
    private static void tryRegisterSimpleInteraction(Identifier portalBlockId, RegistryKey<World> levelKey) {
        tryRegisterSimpleInteraction(Registries.BLOCK.get(portalBlockId), levelKey);
    }

    /**
     * Registers a simple portal track integration for a given block identified by its {@link Block}.
     * <p>
     * Note: This only allows registering integrations that go from the Overworld to another dimension and vice versa.
     *
     * @param portalBlock The portal block.
     * @param levelKey    The resource key of the dimension to travel to
     */
    private static void tryRegisterSimpleInteraction(Block portalBlock, RegistryKey<World> levelKey) {
        PortalTrackProvider p = (level, face) -> PortalTrackProvider.fromPortal(level, face, World.OVERWORLD, levelKey, (Portal) portalBlock);
        PortalTrackProvider.REGISTRY.register(portalBlock, p);
    }

    // Built-in handlers

    /**
     * Registers default portal track integrations for built-in dimensions and mods.
     * This includes the Nether, the Aether (if loaded) and the end (if betterend is loaded).
     */
    public static void register() {
        tryRegisterSimpleInteraction(Blocks.NETHER_PORTAL, World.NETHER);

        //TODO
        //        if (Mods.AETHER.isLoaded()) {
        //            tryRegisterSimpleInteraction(Mods.AETHER.rl("aether_portal"), Mods.AETHER.rl("the_aether"));
        //        }
        //
        //        if (Mods.AETHER_II.isLoaded()) {
        //            tryRegisterSimpleInteraction(Mods.AETHER_II.rl("aether_portal"), Mods.AETHER_II.rl("aether_highlands"));
        //        }
        //
        //        if (Mods.BETTEREND.isLoaded()) {
        //            tryRegisterSimpleInteraction(Mods.BETTEREND.rl("end_portal_block"), Level.END);
        //        }
    }

    public static PortalTrackProvider.Exit fromPortal(
        ServerWorld level,
        BlockFace inboundTrack,
        RegistryKey<World> firstDimension,
        RegistryKey<World> secondDimension,
        Portal portal
    ) {
        RegistryKey<World> resourceKey = level.getRegistryKey() == secondDimension ? firstDimension : secondDimension;

        MinecraftServer minecraftServer = level.getServer();
        ServerWorld otherLevel = minecraftServer.getWorld(resourceKey);

        if (otherLevel == null)
            return null;

        BlockPos portalPos = inboundTrack.getConnectedPos();
        BlockState portalState = level.getBlockState(portalPos);

        SuperGlueEntity probe = new SuperGlueEntity(level, new Box(portalPos));
        probe.setYaw(inboundTrack.getFace().getPositiveHorizontalDegrees());

        TeleportTarget dimensiontransition = portal.createTeleportTarget(level, probe, probe.getBlockPos());
        if (dimensiontransition == null)
            return null;

        if (!minecraftServer.isWorldAllowed(dimensiontransition.world()))
            return null;

        BlockPos otherPortalPos = BlockPos.ofFloored(dimensiontransition.position());
        BlockState otherPortalState = otherLevel.getBlockState(otherPortalPos);
        if (!otherPortalState.isOf(portalState.getBlock()))
            return null;

        Direction targetDirection = inboundTrack.getFace();
        if (targetDirection.getAxis() == otherPortalState.get(Properties.HORIZONTAL_AXIS))
            targetDirection = targetDirection.rotateYClockwise();
        BlockPos otherPos = otherPortalPos.offset(targetDirection);
        return new PortalTrackProvider.Exit(otherLevel, new BlockFace(otherPos, targetDirection.getOpposite()));
    }
}
