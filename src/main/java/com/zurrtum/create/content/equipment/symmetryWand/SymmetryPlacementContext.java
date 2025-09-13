package com.zurrtum.create.content.equipment.symmetryWand;

import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SymmetryPlacementContext extends ItemPlacementContext {
    private final BlockState replace;
    private final BlockState state;

    public SymmetryPlacementContext(
        World world,
        PlayerEntity player,
        Hand hand,
        ItemStack stack,
        BlockPos position,
        Direction direction,
        double y,
        boolean canReplaceExisting,
        BlockState replace,
        BlockState state
    ) {
        super(
            world, player, hand, stack, new BlockHitResult(
                new Vec3d(position.getX() + 0.5 + direction.getOffsetX() * 0.5, y, position.getZ() + 0.5 + direction.getOffsetZ() * 0.5),
                direction,
                position,
                false
            )
        );
        if (!canReplaceExisting) {
            this.canReplaceExisting = false;
            this.placementPos = position;
        }
        this.replace = replace;
        this.state = state;
    }

    @Override
    public Direction[] getPlacementDirections() {
        Block block = state.getBlock();
        if (!BlockHelper.VINELIKE_BLOCKS.contains(block)) {
            return super.getPlacementDirections();
        }
        boolean replaceable = replace.isReplaceable() && !replace.isOf(block);
        Direction[] vines = new Direction[6];
        Direction[] emptys = new Direction[6];
        int vinesCount = 0;
        int emptysCount = 0;

        for (BooleanProperty vineState : BlockHelper.VINELIKE_STATES) {
            Direction direction = getDirection(vineState);
            if (state.contains(vineState) && state.get(vineState) && (replaceable || !replace.get(vineState))) {
                vines[vinesCount++] = direction;
            } else {
                emptys[emptysCount++] = direction;
            }
        }
        System.arraycopy(emptys, 0, vines, vinesCount, emptysCount);
        return vines;
    }

    private static Direction getDirection(BooleanProperty property) {
        return switch (property.getName()) {
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            case "north" -> Direction.NORTH;
            case "east" -> Direction.EAST;
            case "south" -> Direction.SOUTH;
            case "west" -> Direction.WEST;
            default -> throw new IllegalArgumentException("Unexpected property: " + property);
        };
    }
}
