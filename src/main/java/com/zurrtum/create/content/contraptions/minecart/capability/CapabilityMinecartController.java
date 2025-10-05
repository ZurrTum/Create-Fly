package com.zurrtum.create.content.contraptions.minecart.capability;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.entity.EntityLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CapabilityMinecartController {

    /* Global map of loaded carts */

    public static WorldAttached<Map<UUID, MinecartController>> loadedMinecartsByUUID;
    public static WorldAttached<Set<UUID>> loadedMinecartsWithCoupling;
    static WorldAttached<List<AbstractMinecartEntity>> queuedAdditions;
    static WorldAttached<List<UUID>> queuedUnloads;

    static {
        loadedMinecartsByUUID = new WorldAttached<>($ -> new HashMap<>());
        loadedMinecartsWithCoupling = new WorldAttached<>($ -> new HashSet<>());
        queuedAdditions = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
        queuedUnloads = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
    }

    public static void tick(World world) {
        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        List<AbstractMinecartEntity> queued = queuedAdditions.get(world);
        List<UUID> queuedRemovals = queuedUnloads.get(world);
        Set<UUID> cartsWithCoupling = loadedMinecartsWithCoupling.get(world);
        Set<UUID> keySet = carts.keySet();

        for (UUID removal : queuedRemovals) {
            keySet.remove(removal);
            cartsWithCoupling.remove(removal);
        }

        for (AbstractMinecartEntity cart : queued) {
            UUID uniqueID = cart.getUuid();

            if (world.isClient() && carts.containsKey(uniqueID)) {
                MinecartController minecartController = carts.get(uniqueID);
                if (minecartController != null) {
                    AbstractMinecartEntity minecartEntity = minecartController.cart();
                    if (minecartEntity != null && minecartEntity.getId() != cart.getId())
                        continue; // Away with you, Fake Entities!
                }
            }

            cartsWithCoupling.remove(uniqueID);

            AllSynchedDatas.MINECART_CONTROLLER.get(cart).ifPresent(controller -> {
                carts.put(uniqueID, controller);
                if (controller.isLeadingCoupling())
                    cartsWithCoupling.add(uniqueID);
                if (!world.isClient())
                    controller.sendData();
            });
        }

        queuedRemovals.clear();
        queued.clear();

        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, MinecartController> entry : carts.entrySet()) {
            MinecartController controller = entry.getValue();
            if (controller != null && controller.isPresent())
                continue;
            toRemove.add(entry.getKey());
        }

        for (UUID uuid : toRemove) {
            keySet.remove(uuid);
            cartsWithCoupling.remove(uuid);
        }
    }

    public static void entityTick(Entity entity) {
        if (!(entity instanceof AbstractMinecartEntity))
            return;
        AllSynchedDatas.MINECART_CONTROLLER.get(entity).ifPresent(MinecartController::tick);
    }

    public static void onChunkUnloaded(WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        World world = chunk.getWorld();
        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        for (MinecartController minecartController : carts.values()) {
            if (minecartController == null)
                continue;
            if (!minecartController.isPresent())
                continue;
            AbstractMinecartEntity cart = minecartController.cart();
            if (cart.getChunkPos().equals(chunkPos))
                queuedUnloads.get(world).add(cart.getUuid());
        }
    }

    protected static void onCartRemoved(World world, AbstractMinecartEntity entity) {
        AllSynchedDatas.MINECART_CONTROLLER.set(entity, Optional.empty());

        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        List<UUID> unloads = queuedUnloads.get(world);
        UUID uniqueID = entity.getUuid();
        if (!carts.containsKey(uniqueID) || unloads.contains(uniqueID))
            return;
        if (world.isClient())
            return;
        handleKilledMinecart(world, carts.get(uniqueID), entity.getPos());
    }

    protected static void handleKilledMinecart(World world, MinecartController controller, Vec3d removedPos) {
        if (controller == null)
            return;
        for (boolean forward : Iterate.trueAndFalse) {
            Optional<MinecartController> next = CouplingHandler.getNextInCouplingChain(world, controller, forward);
            if (next.isEmpty())
                continue;

            MinecartController nextController = next.get();
            nextController.removeConnection(!forward);
            if (controller.hasContraptionCoupling(forward))
                continue;
            AbstractMinecartEntity cart = nextController.cart();
            if (cart == null)
                continue;

            Vec3d itemPos = cart.getPos().add(removedPos).multiply(.5f);
            ItemEntity itemEntity = new ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, AllItems.MINECART_COUPLING.getDefaultStack());
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
        }
    }

    @Nullable
    public static MinecartController getIfPresent(World world, UUID cartId) {
        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        if (carts == null)
            return null;
        if (!carts.containsKey(cartId))
            return null;
        return carts.get(cartId);
    }

    /* Capability management */

    public static void attach(EntityLike entity) {
        if (!(entity instanceof AbstractMinecartEntity abstractMinecart))
            return;

        MinecartController controller = new MinecartController(abstractMinecart);
        AllSynchedDatas.MINECART_CONTROLLER.set(abstractMinecart, Optional.of(controller));
        queuedAdditions.get(abstractMinecart.getEntityWorld()).add(abstractMinecart);
    }

    public static void onEntityDeath(World world, Entity entity) {
        if (entity instanceof AbstractMinecartEntity abstractMinecart)
            onCartRemoved(world, abstractMinecart);
    }
}
