package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class ToolboxHandler {

    public static final WorldAttached<WeakHashMap<BlockPos, ToolboxBlockEntity>> toolboxes = new WorldAttached<>(w -> new WeakHashMap<>());

    public static void onLoad(ToolboxBlockEntity be) {
        toolboxes.get(be.getWorld()).put(be.getPos(), be);
    }

    public static void onUnload(ToolboxBlockEntity be) {
        toolboxes.get(be.getWorld()).remove(be.getPos());
    }

    static int validationTimer = 20;

    public static void entityTick(Entity entity, World world) {
        if (world.isClient())
            return;
        if (!(world instanceof ServerWorld))
            return;
        if (!(entity instanceof ServerPlayerEntity player))
            return;
        if (entity.age % validationTimer != 0)
            return;

        NbtCompound compound = AllSynchedDatas.TOOLBOX.get(player);
        if (compound.isEmpty())
            return;

        boolean sendData = false;
        for (int i = 0; i < 9; i++) {
            String key = String.valueOf(i);
            if (!compound.contains(key))
                continue;

            NbtCompound data = compound.getCompoundOrEmpty(key);
            BlockPos pos = data.get("Pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
            int slot = data.getInt("Slot", 0);

            if (!world.isPosLoaded(pos))
                continue;
            if (!(world.getBlockState(pos).getBlock() instanceof ToolboxBlock)) {
                compound.remove(key);
                sendData = true;
                continue;
            }

            BlockEntity prevBlockEntity = world.getBlockEntity(pos);
            if (prevBlockEntity instanceof ToolboxBlockEntity toolbox)
                toolbox.connectPlayer(slot, player, i);
        }

        if (sendData)
            syncData(player, compound);
    }

    public static void syncData(PlayerEntity player, NbtCompound data) {
        AllSynchedDatas.TOOLBOX.set(player, data, true);
    }

    public static List<ToolboxBlockEntity> getNearest(WorldAccess world, PlayerEntity player, int maxAmount) {
        Vec3d location = player.getEntityPos();
        double maxRange = getMaxRange(player);
        return toolboxes.get(world).keySet().stream().filter(p -> distance(location, p) < maxRange * maxRange)
            .sorted(Comparator.comparingDouble(p -> distance(location, p))).limit(maxAmount).map(toolboxes.get(world)::get)
            .filter(ToolboxBlockEntity::isFullyInitialized).collect(Collectors.toList());
    }

    public static void unequip(PlayerEntity player, int hotbarSlot, boolean keepItems) {
        NbtCompound compound = AllSynchedDatas.TOOLBOX.get(player);
        World world = player.getEntityWorld();
        String key = String.valueOf(hotbarSlot);
        if (!compound.contains(key))
            return;

        NbtCompound prevData = compound.getCompoundOrEmpty(key);
        BlockPos prevPos = prevData.get("Pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        int prevSlot = prevData.getInt("Slot", 0);

        BlockEntity prevBlockEntity = world.getBlockEntity(prevPos);
        if (prevBlockEntity instanceof ToolboxBlockEntity toolbox) {
            toolbox.unequip(prevSlot, player, hotbarSlot, keepItems || !ToolboxHandler.withinRange(player, toolbox));
        }
        compound.remove(key);
    }

    public static boolean withinRange(PlayerEntity player, ToolboxBlockEntity box) {
        if (player.getEntityWorld() != box.getWorld())
            return false;
        double maxRange = getMaxRange(player);
        return distance(player.getEntityPos(), box.getPos()) < maxRange * maxRange;
    }

    public static double distance(Vec3d location, BlockPos p) {
        return location.squaredDistanceTo(p.getX() + 0.5f, p.getY(), p.getZ() + 0.5f);
    }

    public static double getMaxRange(PlayerEntity player) {
        return AllConfigs.server().equipment.toolboxRange.get().doubleValue();
    }

}
