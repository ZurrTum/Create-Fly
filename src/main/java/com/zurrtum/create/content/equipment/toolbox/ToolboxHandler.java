package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.infrastructure.config.AllConfigs;

import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class ToolboxHandler {

    public static final WorldAttached<WeakHashMap<BlockPos, ToolboxBlockEntity>> toolboxes = new WorldAttached<>(w -> new WeakHashMap<>());

    public static void onLoad(ToolboxBlockEntity be) {
        toolboxes.get(be.getLevel()).put(be.getBlockPos(), be);
    }

    public static void onUnload(ToolboxBlockEntity be) {
        toolboxes.get(be.getLevel()).remove(be.getBlockPos());
    }

    static int validationTimer = 20;

    public static void entityTick(Entity entity, Level world) {
        if (world.isClientSide())
            return;
        if (!(world instanceof ServerLevel))
            return;
        if (!(entity instanceof ServerPlayer player))
            return;
        if (entity.tickCount % validationTimer != 0)
            return;

        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        if (compound.isEmpty())
            return;

        boolean sendData = false;
        for (int i = 0; i < 9; i++) {
            String key = String.valueOf(i);
            if (!compound.contains(key))
                continue;

            CompoundTag data = compound.getCompoundOrEmpty(key);
            BlockPos pos = data.read("Pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
            int slot = data.getIntOr("Slot", 0);

            if (!world.isLoaded(pos))
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

    public static void syncData(Player player, CompoundTag data) {
        AllSynchedDatas.TOOLBOX.set(player, data, true);
    }

    public static List<ToolboxBlockEntity> getNearest(LevelAccessor world, Player player, int maxAmount) {
        Vec3 location = player.position();
        double maxRange = getMaxRange(player);
        return toolboxes.get(world).keySet().stream().filter(p -> distance(location, p) < maxRange * maxRange)
            .sorted(Comparator.comparingDouble(p -> distance(location, p))).limit(maxAmount).map(toolboxes.get(world)::get)
            .filter(ToolboxBlockEntity::isFullyInitialized).collect(Collectors.toList());
    }

    public static void unequip(Player player, int hotbarSlot, boolean keepItems) {
        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        Level world = player.level();
        String key = String.valueOf(hotbarSlot);
        if (!compound.contains(key))
            return;

        CompoundTag prevData = compound.getCompoundOrEmpty(key);
        BlockPos prevPos = prevData.read("Pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
        int prevSlot = prevData.getIntOr("Slot", 0);

        BlockEntity prevBlockEntity = world.getBlockEntity(prevPos);
        if (prevBlockEntity instanceof ToolboxBlockEntity toolbox) {
            toolbox.unequip(prevSlot, player, hotbarSlot, keepItems || !ToolboxHandler.withinRange(player, toolbox));
        }
        compound.remove(key);
    }

    public static boolean withinRange(Player player, ToolboxBlockEntity box) {
        if (player.level() != box.getLevel())
            return false;
        double maxRange = getMaxRange(player);
        return distance(player.position(), box.getBlockPos()) < maxRange * maxRange;
    }

    public static double distance(Vec3 location, BlockPos p) {
        return location.distanceToSqr(p.getX() + 0.5f, p.getY(), p.getZ() + 0.5f);
    }

    public static double getMaxRange(Player player) {
        return AllConfigs.server().equipment.toolboxRange.get().doubleValue();
    }

}
