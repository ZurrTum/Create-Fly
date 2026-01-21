package com.zurrtum.create.client.content.contraptions.chassis;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.contraptions.chassis.ChassisBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ChassisRangeDisplay {

    private static final int DISPLAY_TIME = 200;
    private static @Nullable GroupEntry lastHoveredGroup = null;

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
            return Pair.of(be.getBlockPos(), 1);
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

    public static void tick(Minecraft mc) {
        Player player = mc.player;
        Level world = mc.level;
        boolean hasWrench = player.getMainHandItem().is(AllItems.WRENCH);

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

        HitResult over = mc.hitResult;
        if (!(over instanceof BlockHitResult ray))
            return;
        BlockPos pos = ray.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved())
            return;
        if (!(blockEntity instanceof ChassisBlockEntity chassisBlockEntity))
            return;

        boolean ctrl = AllKeys.hasControlDown();

        if (ctrl) {
            GroupEntry existingGroupForPos = getExistingGroupForPos(pos);
            if (existingGroupForPos != null) {
                for (ChassisBlockEntity included : existingGroupForPos.includedBEs)
                    entries.remove(included.getBlockPos());
                existingGroupForPos.timer = DISPLAY_TIME;
                return;
            }
        }

        if (!entries.containsKey(pos) || ctrl)
            display(chassisBlockEntity);
        else {
            entries.get(pos).timer = DISPLAY_TIME;
        }
    }

    private static boolean tickEntry(Level world, Entry entry, boolean hasWrench) {
        ChassisBlockEntity chassisBlockEntity = entry.be;
        Level beWorld = chassisBlockEntity.getLevel();

        if (chassisBlockEntity.isRemoved() || beWorld == null || beWorld != world || !world.isLoaded(chassisBlockEntity.getBlockPos())) {
            return true;
        }

        if (!hasWrench && entry.timer > 20) {
            entry.timer = 20;
            return false;
        }

        entry.timer--;
        return entry.timer == 0;
    }

    public static void display(ChassisBlockEntity chassis) {

        // Display a group and kill any selections of its contained chassis blocks
        if (AllKeys.hasControlDown()) {
            GroupEntry hoveredGroup = new GroupEntry(chassis);

            for (ChassisBlockEntity included : hoveredGroup.includedBEs)
                Outliner.getInstance().remove(Pair.of(included.getBlockPos(), 1));

            groupEntries.forEach(entry -> Outliner.getInstance().remove(entry.getOutlineKey()));
            groupEntries.clear();
            entries.clear();
            groupEntries.add(hoveredGroup);
            return;
        }

        // Display an individual chassis and kill any group selections that contained it
        BlockPos pos = chassis.getBlockPos();
        GroupEntry entry = getExistingGroupForPos(pos);
        if (entry != null)
            Outliner.getInstance().remove(entry.getOutlineKey());

        groupEntries.clear();
        entries.clear();
        entries.put(pos, new Entry(chassis));

    }

    @Nullable
    private static GroupEntry getExistingGroupForPos(BlockPos pos) {
        for (GroupEntry groupEntry : groupEntries)
            for (ChassisBlockEntity chassis : groupEntry.includedBEs)
                if (pos.equals(chassis.getBlockPos()))
                    return groupEntry;
        return null;
    }

}
