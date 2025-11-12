package com.zurrtum.create.content.contraptions.minecart.capability;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.Vec3;

public class CapabilityMinecartController {

    /* Global map of loaded carts */

    public static WorldAttached<Map<UUID, MinecartController>> loadedMinecartsByUUID;
    public static WorldAttached<Set<UUID>> loadedMinecartsWithCoupling;
    static WorldAttached<List<AbstractMinecart>> queuedAdditions;
    static WorldAttached<List<UUID>> queuedUnloads;

    static {
        loadedMinecartsByUUID = new WorldAttached<>($ -> new HashMap<>());
        loadedMinecartsWithCoupling = new WorldAttached<>($ -> new HashSet<>());
        queuedAdditions = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
        queuedUnloads = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
    }

    public static void tick(Level world) {
        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        List<AbstractMinecart> queued = queuedAdditions.get(world);
        List<UUID> queuedRemovals = queuedUnloads.get(world);
        Set<UUID> cartsWithCoupling = loadedMinecartsWithCoupling.get(world);
        Set<UUID> keySet = carts.keySet();

        for (UUID removal : queuedRemovals) {
            keySet.remove(removal);
            cartsWithCoupling.remove(removal);
        }

        for (AbstractMinecart cart : queued) {
            UUID uniqueID = cart.getUUID();

            if (world.isClientSide() && carts.containsKey(uniqueID)) {
                MinecartController minecartController = carts.get(uniqueID);
                if (minecartController != null) {
                    AbstractMinecart minecartEntity = minecartController.cart();
                    if (minecartEntity != null && minecartEntity.getId() != cart.getId())
                        continue; // Away with you, Fake Entities!
                }
            }

            cartsWithCoupling.remove(uniqueID);

            AllSynchedDatas.MINECART_CONTROLLER.get(cart).ifPresent(controller -> {
                carts.put(uniqueID, controller);
                if (controller.isLeadingCoupling())
                    cartsWithCoupling.add(uniqueID);
                if (!world.isClientSide())
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
        if (!(entity instanceof AbstractMinecart))
            return;
        AllSynchedDatas.MINECART_CONTROLLER.get(entity).ifPresent(MinecartController::tick);
    }

    public static void onChunkUnloaded(LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        Level world = chunk.getLevel();
        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        for (MinecartController minecartController : carts.values()) {
            if (minecartController == null)
                continue;
            if (!minecartController.isPresent())
                continue;
            AbstractMinecart cart = minecartController.cart();
            if (cart.chunkPosition().equals(chunkPos))
                queuedUnloads.get(world).add(cart.getUUID());
        }
    }

    protected static void onCartRemoved(Level world, AbstractMinecart entity) {
        AllSynchedDatas.MINECART_CONTROLLER.set(entity, Optional.empty());

        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        List<UUID> unloads = queuedUnloads.get(world);
        UUID uniqueID = entity.getUUID();
        if (!carts.containsKey(uniqueID) || unloads.contains(uniqueID))
            return;
        if (world.isClientSide())
            return;
        handleKilledMinecart(world, carts.get(uniqueID), entity.position());
    }

    protected static void handleKilledMinecart(Level world, MinecartController controller, Vec3 removedPos) {
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
            AbstractMinecart cart = nextController.cart();
            if (cart == null)
                continue;

            Vec3 itemPos = cart.position().add(removedPos).scale(.5f);
            ItemEntity itemEntity = new ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, AllItems.MINECART_COUPLING.getDefaultInstance());
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        }
    }

    @Nullable
    public static MinecartController getIfPresent(Level world, UUID cartId) {
        Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
        if (carts == null)
            return null;
        if (!carts.containsKey(cartId))
            return null;
        return carts.get(cartId);
    }

    /* Capability management */

    public static void attach(EntityAccess entity) {
        if (!(entity instanceof AbstractMinecart abstractMinecart))
            return;

        MinecartController controller = new MinecartController(abstractMinecart);
        AllSynchedDatas.MINECART_CONTROLLER.set(abstractMinecart, Optional.of(controller));
        queuedAdditions.get(abstractMinecart.level()).add(abstractMinecart);
    }

    public static void onEntityDeath(Level world, Entity entity) {
        if (entity instanceof AbstractMinecart abstractMinecart)
            onCartRemoved(world, abstractMinecart);
    }
}
