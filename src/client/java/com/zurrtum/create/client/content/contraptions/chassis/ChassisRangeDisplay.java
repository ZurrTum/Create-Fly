package com.zurrtum.create.client.content.contraptions.chassis;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.contraptions.chassis.ChassisBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class ChassisRangeDisplay {

    private static final int DISPLAY_TIME = 200;
    private static GroupEntry lastHoveredGroup = null;

    private static class Entry {
        ChassisBlockEntity be;
        int timer;

        public Entry(ChassisBlockEntity be) {
            this.be = be;
            timer = DISPLAY_TIME;
            Outliner.getInstance().showCluster(getOutlineKey(), createSelection(be)).colored(0xFFFFFF).disableLineNormals().lineWidth(1 / 16f)
                .withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED);
        }

        protected Object getOutlineKey() {
            return Pair.of(be.getPos(), 1);
        }

        protected Set<BlockPos> createSelection(ChassisBlockEntity chassis) {
            Set<BlockPos> positions = new HashSet<>();
            List<BlockPos> includedBlockPositions = chassis.getIncludedBlockPositions(null, true);
            if (includedBlockPositions == null)
                return Collections.emptySet();
            positions.addAll(includedBlockPositions);
            return positions;
        }

    }

    private static class GroupEntry extends Entry {

        List<ChassisBlockEntity> includedBEs;

        public GroupEntry(ChassisBlockEntity be) {
            super(be);
        }

        @Override
        protected Object getOutlineKey() {
            return this;
        }

        @Override
        protected Set<BlockPos> createSelection(ChassisBlockEntity chassis) {
            Set<BlockPos> list = new HashSet<>();
            includedBEs = be.collectChassisGroup();
            if (includedBEs == null)
                return list;
            for (ChassisBlockEntity chassisBlockEntity : includedBEs)
                list.addAll(super.createSelection(chassisBlockEntity));
            return list;
        }

    }

    static Map<BlockPos, Entry> entries = new HashMap<>();
    static List<GroupEntry> groupEntries = new ArrayList<>();

    public static void tick(MinecraftClient mc) {
        PlayerEntity player = mc.player;
        World world = mc.world;
        boolean hasWrench = player.getMainHandStack().isOf(AllItems.WRENCH);

        for (Iterator<BlockPos> iterator = entries.keySet().iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            Entry entry = entries.get(pos);
            if (tickEntry(world, entry, hasWrench))
                iterator.remove();
            Outliner.getInstance().keep(entry.getOutlineKey());
        }

        for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext(); ) {
            GroupEntry group = iterator.next();
            if (tickEntry(world, group, hasWrench)) {
                iterator.remove();
                if (group == lastHoveredGroup)
                    lastHoveredGroup = null;
            }
            Outliner.getInstance().keep(group.getOutlineKey());
        }

        if (!hasWrench)
            return;

        HitResult over = mc.crosshairTarget;
        if (!(over instanceof BlockHitResult ray))
            return;
        BlockPos pos = ray.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved())
            return;
        if (!(blockEntity instanceof ChassisBlockEntity chassisBlockEntity))
            return;

        boolean ctrl = Screen.hasControlDown();

        if (ctrl) {
            GroupEntry existingGroupForPos = getExistingGroupForPos(pos);
            if (existingGroupForPos != null) {
                for (ChassisBlockEntity included : existingGroupForPos.includedBEs)
                    entries.remove(included.getPos());
                existingGroupForPos.timer = DISPLAY_TIME;
                return;
            }
        }

        if (!entries.containsKey(pos) || ctrl)
            display(chassisBlockEntity);
        else {
            if (!ctrl)
                entries.get(pos).timer = DISPLAY_TIME;
        }
    }

    private static boolean tickEntry(World world, Entry entry, boolean hasWrench) {
        ChassisBlockEntity chassisBlockEntity = entry.be;
        World beWorld = chassisBlockEntity.getWorld();

        if (chassisBlockEntity.isRemoved() || beWorld == null || beWorld != world || !world.isPosLoaded(chassisBlockEntity.getPos())) {
            return true;
        }

        if (!hasWrench && entry.timer > 20) {
            entry.timer = 20;
            return false;
        }

        entry.timer--;
        if (entry.timer == 0)
            return true;
        return false;
    }

    public static void display(ChassisBlockEntity chassis) {

        // Display a group and kill any selections of its contained chassis blocks
        if (Screen.hasControlDown()) {
            GroupEntry hoveredGroup = new GroupEntry(chassis);

            for (ChassisBlockEntity included : hoveredGroup.includedBEs)
                Outliner.getInstance().remove(Pair.of(included.getPos(), 1));

            groupEntries.forEach(entry -> Outliner.getInstance().remove(entry.getOutlineKey()));
            groupEntries.clear();
            entries.clear();
            groupEntries.add(hoveredGroup);
            return;
        }

        // Display an individual chassis and kill any group selections that contained it
        BlockPos pos = chassis.getPos();
        GroupEntry entry = getExistingGroupForPos(pos);
        if (entry != null)
            Outliner.getInstance().remove(entry.getOutlineKey());

        groupEntries.clear();
        entries.clear();
        entries.put(pos, new Entry(chassis));

    }

    private static GroupEntry getExistingGroupForPos(BlockPos pos) {
        for (GroupEntry groupEntry : groupEntries)
            for (ChassisBlockEntity chassis : groupEntry.includedBEs)
                if (pos.equals(chassis.getPos()))
                    return groupEntry;
        return null;
    }

}
