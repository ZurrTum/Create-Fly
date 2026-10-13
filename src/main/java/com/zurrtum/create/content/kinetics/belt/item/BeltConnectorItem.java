package com.zurrtum.create.content.kinetics.belt.item;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.LinkedList;
import java.util.List;

public class BeltConnectorItem extends BlockItem {

    public BeltConnectorItem(Properties properties) {
        super(AllBlocks.BELT, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player playerEntity = context.getPlayer();
        ItemStack heldStack = context.getItemInHand();

        if (playerEntity != null && playerEntity.isShiftKeyDown()) {
            heldStack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            return InteractionResult.SUCCESS;
        }

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean validAxis = validateAxis(world, pos);

        if (world.isClientSide()) {
            return validAxis ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        BlockPos firstPulley = null;

        // Remove first if no longer existant or valid
        if (heldStack.has(AllDataComponents.BELT_FIRST_SHAFT)) {
            firstPulley = heldStack.get(AllDataComponents.BELT_FIRST_SHAFT);
            if (!validateAxis(world, firstPulley) || !firstPulley.closerThan(pos, maxLength() * 2)) {
                heldStack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            }
        }

        if (!validAxis || playerEntity == null) {
            return InteractionResult.FAIL;
        }

        if (heldStack.has(AllDataComponents.BELT_FIRST_SHAFT)) {

            if (!canConnect(world, firstPulley, pos)) {
                return InteractionResult.FAIL;
            }

            if (firstPulley != null && !firstPulley.equals(pos)) {
                createSortedBelts(world, firstPulley, pos);
                if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
                    AllAdvancements.BELT.trigger(serverPlayerEntity);
                }
                if (!playerEntity.isCreative()) {
                    heldStack.shrink(1);
                }
            }

            if (!heldStack.isEmpty()) {
                heldStack.remove(AllDataComponents.BELT_FIRST_SHAFT);
                playerEntity.getCooldowns().addCooldown(heldStack, 5);
            }
            return InteractionResult.SUCCESS;
        }

        heldStack.set(AllDataComponents.BELT_FIRST_SHAFT, pos);
        playerEntity.getCooldowns().addCooldown(heldStack, 5);
        return InteractionResult.SUCCESS;
    }

    private static void createSortedBelts(Level world, BlockPos start, BlockPos end) {
        world.playSound(
            null,
            BlockPos.containing(VecHelper.getCenterOf(start.offset(end)).scale(0.5f)),
            SoundEvents.WOOL_PLACE,
            SoundSource.BLOCKS,
            0.5F,
            1.0F
        );
        MutableBlockPos currentPos = new MutableBlockPos();
        Direction facing;
        int diffX = end.getX() - start.getX();
        int diffY = end.getY() - start.getY();
        int diffZ = end.getZ() - start.getZ();
        int count, moveX, moveY, moveZ;
        BlockState currentState;
        BeltSlope slope;
        if (diffY != 0) {
            if (diffX != 0) {
                boolean negative = diffX < 0;
                boolean upward = diffY > 0 == negative;
                currentPos.set(negative ? start : end);
                currentState = world.getBlockState(currentPos);
                slope = upward ? BeltSlope.UPWARD : BeltSlope.DOWNWARD;
                moveY = upward ? 1 : -1;
                facing = Direction.WEST;
                moveX = -1;
                moveZ = 0;
                count = Math.abs(diffX);
            } else if (diffZ != 0) {
                boolean negative = diffZ < 0;
                boolean upward = diffY > 0 == negative;
                currentPos.set(negative ? start : end);
                currentState = world.getBlockState(currentPos);
                slope = upward ? BeltSlope.UPWARD : BeltSlope.DOWNWARD;
                moveY = upward ? 1 : -1;
                facing = Direction.NORTH;
                moveX = 0;
                moveZ = -1;
                count = Math.abs(diffZ);
            } else {
                currentPos.set(diffY > 0 ? start : end);
                currentState = world.getBlockState(currentPos);
                slope = BeltSlope.VERTICAL;
                moveY = 1;
                Axis axis = currentState.getValue(AbstractSimpleShaftBlock.AXIS);
                facing = axis == Axis.X ? Direction.SOUTH : Direction.EAST;
                moveX = moveZ = 0;
                count = Math.abs(diffY);
            }
        } else {
            moveY = 0;
            if (diffX != 0) {
                currentPos.set(diffX > 0 ? start : end);
                facing = Direction.EAST;
                moveX = 1;
                moveZ = 0;
                count = Math.abs(diffX);
            } else {
                currentPos.set(diffZ > 0 ? start : end);
                facing = Direction.SOUTH;
                moveX = 0;
                moveZ = 1;
                count = Math.abs(diffZ);
            }
            currentState = world.getBlockState(currentPos);
            Axis axis = currentState.getValue(AbstractSimpleShaftBlock.AXIS);
            slope = axis == Axis.Y ? BeltSlope.SIDEWAYS : BeltSlope.HORIZONTAL;
        }
        if (currentState.getDestroySpeed(world, currentPos) == -1) {
            return;
        }
        if (!currentState.canBeReplaced()) {
            world.destroyBlock(currentPos, false);
        }
        BlockState beltBlock = AllBlocks.BELT.defaultBlockState();
        KineticBlockEntity.switchToBlockState(
            world, currentPos, ProperWaterloggedBlock.withWater(
                world,
                beltBlock.setValue(BeltBlock.SLOPE, slope).setValue(BeltBlock.PART, BeltPart.START)
                    .setValue(BeltBlock.HORIZONTAL_FACING, facing),
                currentPos
            )
        );
        if (count > 1) {
            for (int i = 1; i < count; i++) {
                currentState = world.getBlockState(currentPos.move(moveX, moveY, moveZ));
                if (currentState.getDestroySpeed(world, currentPos) == -1) {
                    moveX = -moveX;
                    moveY = -moveY;
                    moveZ = -moveZ;
                    for (int j = 0; j < i; j++) {
                        if (world.getBlockState(currentPos.move(moveX, moveY, moveZ)).is(AllBlocks.BELT)) {
                            world.destroyBlock(currentPos, false);
                        }
                    }
                    return;
                }
                if (!currentState.canBeReplaced()) {
                    world.destroyBlock(currentPos, false);
                }
                KineticBlockEntity.switchToBlockState(
                    world, currentPos, ProperWaterloggedBlock.withWater(
                        world,
                        beltBlock.setValue(BeltBlock.SLOPE, slope).setValue(
                            BeltBlock.PART,
                            ShaftBlock.isShaft(currentState) ? BeltPart.PULLEY : BeltPart.MIDDLE
                        ).setValue(BeltBlock.HORIZONTAL_FACING, facing),
                        currentPos
                    )
                );
            }
        }
        currentState = world.getBlockState(currentPos.move(moveX, moveY, moveZ));
        if (currentState.getDestroySpeed(world, currentPos) == -1) {
            moveX = -moveX;
            moveY = -moveY;
            moveZ = -moveZ;
            for (int i = 0; i < count; i++) {
                if (world.getBlockState(currentPos.move(moveX, moveY, moveZ)).is(AllBlocks.BELT)) {
                    world.destroyBlock(currentPos, false);
                }
            }
            return;
        }
        if (!currentState.canBeReplaced()) {
            world.destroyBlock(currentPos, false);
        }
        KineticBlockEntity.switchToBlockState(
            world, currentPos, ProperWaterloggedBlock.withWater(
                world,
                beltBlock.setValue(BeltBlock.SLOPE, slope).setValue(BeltBlock.PART, BeltPart.END)
                    .setValue(BeltBlock.HORIZONTAL_FACING, facing),
                currentPos
            )
        );
    }

    public static void createBelts(Level world, BlockPos start, BlockPos end) {
        world.playSound(
            null,
            BlockPos.containing(VecHelper.getCenterOf(start.offset(end)).scale(0.5f)),
            SoundEvents.WOOL_PLACE,
            SoundSource.BLOCKS,
            0.5F,
            1.0F
        );

        BeltSlope slope = getSlopeBetween(start, end);
        Direction facing = getFacingFromTo(start, end);

        BlockPos diff = end.subtract(start);
        if (diff.getX() == diff.getZ()) {
            facing = Direction.get(
                facing.getAxisDirection(),
                world.getBlockState(start).getValue(BlockStateProperties.AXIS) == Axis.X ? Axis.Z : Axis.X
            );
        }

        List<BlockPos> beltsToCreate = getBeltChainBetween(start, end, slope, facing);
        BlockState beltBlock = AllBlocks.BELT.defaultBlockState();
        boolean failed = false;

        for (BlockPos pos : beltsToCreate) {
            BlockState existingBlock = world.getBlockState(pos);
            if (existingBlock.getDestroySpeed(world, pos) == -1) {
                failed = true;
                break;
            }

            BeltPart part = pos.equals(start) ? BeltPart.START : pos.equals(end) ? BeltPart.END : BeltPart.MIDDLE;
            BlockState shaftState = world.getBlockState(pos);
            boolean pulley = ShaftBlock.isShaft(shaftState);
            if (part == BeltPart.MIDDLE && pulley) {
                part = BeltPart.PULLEY;
            }
            if (pulley && shaftState.getValue(AbstractSimpleShaftBlock.AXIS) == Axis.Y) {
                slope = BeltSlope.SIDEWAYS;
            }

            if (!existingBlock.canBeReplaced()) {
                world.destroyBlock(pos, false);
            }

            KineticBlockEntity.switchToBlockState(
                world, pos, ProperWaterloggedBlock.withWater(
                    world,
                    beltBlock.setValue(BeltBlock.SLOPE, slope).setValue(BeltBlock.PART, part)
                        .setValue(BeltBlock.HORIZONTAL_FACING, facing),
                    pos
                )
            );
        }

        if (!failed) {
            return;
        }

        for (BlockPos pos : beltsToCreate) {
            if (world.getBlockState(pos).is(AllBlocks.BELT)) {
                world.destroyBlock(pos, false);
            }
        }
    }

    private static Direction getFacingFromTo(BlockPos start, BlockPos end) {
        Axis beltAxis = start.getX() == end.getX() ? Axis.Z : Axis.X;
        BlockPos diff = end.subtract(start);
        AxisDirection axisDirection = AxisDirection.POSITIVE;

        if (diff.getX() == 0 && diff.getZ() == 0) {
            axisDirection = diff.getY() > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        } else {
            axisDirection =
                beltAxis.choose(diff.getX(), 0, diff.getZ()) > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        }

        return Direction.get(axisDirection, beltAxis);
    }

    private static BeltSlope getSlopeBetween(BlockPos start, BlockPos end) {
        BlockPos diff = end.subtract(start);

        if (diff.getY() != 0) {
            if (diff.getZ() != 0 || diff.getX() != 0) {
                return diff.getY() > 0 ? BeltSlope.UPWARD : BeltSlope.DOWNWARD;
            }
            return BeltSlope.VERTICAL;
        }
        return BeltSlope.HORIZONTAL;
    }

    private static List<BlockPos> getBeltChainBetween(
        BlockPos start,
        BlockPos end,
        BeltSlope slope,
        Direction direction
    ) {
        List<BlockPos> positions = new LinkedList<>();
        int limit = 1000;
        BlockPos current = start;

        do {
            positions.add(current);

            if (slope == BeltSlope.VERTICAL) {
                current = current.above(direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1);
                continue;
            }

            current = current.relative(direction);
            if (slope != BeltSlope.HORIZONTAL) {
                current = current.above(slope == BeltSlope.UPWARD ? 1 : -1);
            }

        } while (!current.equals(end) && limit-- > 0);

        positions.add(end);
        return positions;
    }

    public static boolean canConnect(Level world, BlockPos first, BlockPos second) {
        if (!world.isLoaded(first) || !world.isLoaded(second)) {
            return false;
        }
        if (!second.closerThan(first, maxLength())) {
            return false;
        }

        BlockPos diff = second.subtract(first);
        Axis shaftAxis = world.getBlockState(first).getValue(BlockStateProperties.AXIS);

        int x = diff.getX();
        int y = diff.getY();
        int z = diff.getZ();
        int sames = (Math.abs(x) == Math.abs(y) ? 1 : 0) + (Math.abs(y) == Math.abs(z) ? 1 : 0) + (
            Math.abs(z) == Math.abs(x) ? 1 : 0);

        if (shaftAxis.choose(x, y, z) != 0) {
            return false;
        }
        if (sames != 1) {
            return false;
        }
        if (shaftAxis != world.getBlockState(second).getValue(BlockStateProperties.AXIS)) {
            return false;
        }
        if (shaftAxis == Axis.Y && x != 0 && z != 0) {
            return false;
        }

        BlockEntity blockEntity = world.getBlockEntity(first);
        BlockEntity blockEntity2 = world.getBlockEntity(second);

        if (!(blockEntity instanceof KineticBlockEntity)) {
            return false;
        }
        if (!(blockEntity2 instanceof KineticBlockEntity)) {
            return false;
        }

        float speed1 = ((KineticBlockEntity) blockEntity).getTheoreticalSpeed();
        float speed2 = ((KineticBlockEntity) blockEntity2).getTheoreticalSpeed();
        if (Math.signum(speed1) != Math.signum(speed2) && speed1 != 0 && speed2 != 0) {
            return false;
        }

        BlockPos step = BlockPos.containing(
            Math.signum(diff.getX()),
            Math.signum(diff.getY()),
            Math.signum(diff.getZ())
        );
        int limit = 1000;
        for (BlockPos currentPos = first.offset(step); !currentPos.equals(second) && limit-- > 0; currentPos = currentPos.offset(
            step)) {
            BlockState blockState = world.getBlockState(currentPos);
            if (ShaftBlock.isShaft(blockState) && blockState.getValue(AbstractSimpleShaftBlock.AXIS) == shaftAxis) {
                continue;
            }
            if (!blockState.canBeReplaced()) {
                return false;
            }
        }

        return true;

    }

    public static Integer maxLength() {
        return AllConfigs.server().kinetics.maxBeltLength.get();
    }

    public static boolean validateAxis(Level world, BlockPos pos) {
        if (!world.isLoaded(pos)) {
            return false;
        }
        if (!ShaftBlock.isShaft(world.getBlockState(pos))) {
            return false;
        }
        return true;
    }

}
