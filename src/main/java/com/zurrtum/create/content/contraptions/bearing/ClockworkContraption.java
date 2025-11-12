package com.zurrtum.create.content.contraptions.bearing;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.Contraption;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ClockworkContraption extends Contraption {

    protected Direction facing;
    public HandType handType;
    public int offset;
    private final Set<BlockPos> ignoreBlocks = new HashSet<>();

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.CLOCKWORK;
    }

    private void ignoreBlocks(Set<BlockPos> blocks, BlockPos anchor) {
        for (BlockPos blockPos : blocks)
            ignoreBlocks.add(anchor.offset(blockPos));
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return pos.equals(anchor.relative(facing.getOpposite(), offset + 1));
    }

    public static Pair<ClockworkContraption, ClockworkContraption> assembleClockworkAt(
        Level world,
        BlockPos pos,
        Direction direction
    ) throws AssemblyException {
        int hourArmBlocks = 0;

        ClockworkContraption hourArm = new ClockworkContraption();
        ClockworkContraption minuteArm = null;

        hourArm.facing = direction;
        hourArm.handType = HandType.HOUR;
        if (!hourArm.assemble(world, pos))
            return null;
        for (int i = 0; i < 16; i++) {
            BlockPos offsetPos = BlockPos.ZERO.relative(direction, i);
            if (hourArm.getBlocks().containsKey(offsetPos))
                continue;
            hourArmBlocks = i;
            break;
        }

        if (hourArmBlocks > 0) {
            minuteArm = new ClockworkContraption();
            minuteArm.facing = direction;
            minuteArm.handType = HandType.MINUTE;
            minuteArm.offset = hourArmBlocks;
            minuteArm.ignoreBlocks(hourArm.getBlocks().keySet(), hourArm.anchor);
            if (!minuteArm.assemble(world, pos))
                return null;
            if (minuteArm.getBlocks().isEmpty())
                minuteArm = null;
        }

        hourArm.startMoving(world);
        hourArm.expandBoundsAroundAxis(direction.getAxis());
        if (minuteArm != null) {
            minuteArm.startMoving(world);
            minuteArm.expandBoundsAroundAxis(direction.getAxis());
        }
        return Pair.of(hourArm, minuteArm);
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        return searchMovedStructure(world, pos, facing);
    }

    @Override
    public boolean searchMovedStructure(Level world, BlockPos pos, Direction direction) throws AssemblyException {
        return super.searchMovedStructure(world, pos.relative(direction, offset + 1), null);
    }

    @Override
    protected boolean moveBlock(Level world, Direction direction, Queue<BlockPos> frontier, Set<BlockPos> visited) throws AssemblyException {
        if (ignoreBlocks.contains(frontier.peek())) {
            frontier.poll();
            return true;
        }
        return super.moveBlock(world, direction, frontier, visited);
    }

    @Override
    public void write(ValueOutput view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.store("facing", Direction.CODEC, facing);
        view.store("handType", HandType.CODEC, handType);
        view.putInt("offset", offset);
    }

    @Override
    public void read(Level world, ValueInput view, boolean spawnData) {
        facing = view.read("facing", Direction.CODEC).orElse(Direction.DOWN);
        handType = view.read("handType", HandType.CODEC).orElse(HandType.HOUR);
        offset = view.getIntOr("offset", 0);
        super.read(world, view, spawnData);
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        if (BlockPos.ZERO.equals(localPos) || BlockPos.ZERO.equals(localPos.relative(facing)))
            return false;
        return facing.getAxis() == this.facing.getAxis();
    }

    public enum HandType implements StringRepresentable {
        HOUR,
        MINUTE;

        public static final Codec<HandType> CODEC = StringRepresentable.fromEnum(HandType::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
