package com.zurrtum.create.content.equipment.symmetryWand;

import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SymmetryPlacementContext extends BlockPlaceContext {
    private final BlockState replace;
    private final BlockState state;

    public SymmetryPlacementContext(
        Level world,
        Player player,
        InteractionHand hand,
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
                new Vec3(position.getX() + 0.5 + direction.getStepX() * 0.5, y, position.getZ() + 0.5 + direction.getStepZ() * 0.5),
                direction,
                position,
                false
            )
        );
        if (!canReplaceExisting) {
            this.replaceClicked = false;
            this.relativePos = position;
        }
        this.replace = replace;
        this.state = state;
    }

    @Override
    public Direction[] getNearestLookingDirections() {
        Block block = state.getBlock();
        if (!BlockHelper.VINELIKE_BLOCKS.contains(block)) {
            return super.getNearestLookingDirections();
        }
        boolean replaceable = replace.canBeReplaced() && !replace.is(block);
        Direction[] vines = new Direction[6];
        Direction[] emptys = new Direction[6];
        int vinesCount = 0;
        int emptysCount = 0;

        for (BooleanProperty vineState : BlockHelper.VINELIKE_STATES) {
            Direction direction = getDirection(vineState);
            if (state.hasProperty(vineState) && state.getValue(vineState) && (replaceable || !replace.getValue(vineState))) {
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
