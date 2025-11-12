package com.zurrtum.create.impl.contraption;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.contraption.BlockMovementChecks.*;
import com.zurrtum.create.api.contraption.ContraptionMovementSetting;
import com.zurrtum.create.content.contraptions.actors.AttachedActorBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.bearing.*;
import com.zurrtum.create.content.contraptions.chassis.AbstractChassisBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlockEntity;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleExtenderBlock;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.fan.NozzleBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.vault.ItemVaultBlock;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlock;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;

public class BlockMovementChecksImpl {
    private static final List<MovementNecessaryCheck> MOVEMENT_NECESSARY_CHECKS = new ArrayList<>();
    private static final List<MovementAllowedCheck> MOVEMENT_ALLOWED_CHECKS = new ArrayList<>();
    private static final List<BrittleCheck> BRITTLE_CHECKS = new ArrayList<>();
    private static final List<AttachedCheck> ATTACHED_CHECKS = new ArrayList<>();
    private static final List<NotSupportiveCheck> NOT_SUPPORTIVE_CHECKS = new ArrayList<>();

    // registration adds to the start so newer ones are queried first
    // synchronize these so they're safe to call in async mod init

    public static synchronized void registerMovementNecessaryCheck(MovementNecessaryCheck check) {
        MOVEMENT_NECESSARY_CHECKS.addFirst(check);
    }

    public static synchronized void registerMovementAllowedCheck(MovementAllowedCheck check) {
        MOVEMENT_ALLOWED_CHECKS.addFirst(check);
    }

    public static synchronized void registerBrittleCheck(BrittleCheck check) {
        BRITTLE_CHECKS.addFirst(check);
    }

    public static synchronized void registerAttachedCheck(AttachedCheck check) {
        ATTACHED_CHECKS.addFirst(check);
    }

    public static synchronized void registerNotSupportiveCheck(NotSupportiveCheck check) {
        NOT_SUPPORTIVE_CHECKS.addFirst(check);
    }

    // queries

    public static boolean isMovementNecessary(BlockState state, Level world, BlockPos pos) {
        for (MovementNecessaryCheck check : MOVEMENT_NECESSARY_CHECKS) {
            CheckResult result = check.isMovementNecessary(state, world, pos);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return BlockMovementChecksImpl.isMovementNecessaryFallback(state, world, pos);
    }

    public static boolean isMovementAllowed(BlockState state, Level world, BlockPos pos) {
        for (MovementAllowedCheck check : MOVEMENT_ALLOWED_CHECKS) {
            CheckResult result = check.isMovementAllowed(state, world, pos);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return BlockMovementChecksImpl.isMovementAllowedFallback(state, world, pos);
    }

    public static boolean isBrittle(BlockState state) {
        for (BrittleCheck check : BRITTLE_CHECKS) {
            CheckResult result = check.isBrittle(state);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return BlockMovementChecksImpl.isBrittleFallback(state);
    }

    public static boolean isBlockAttachedTowards(BlockState state, Level world, BlockPos pos, Direction direction) {
        for (AttachedCheck check : ATTACHED_CHECKS) {
            CheckResult result = check.isBlockAttachedTowards(state, world, pos, direction);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return BlockMovementChecksImpl.isBlockAttachedTowardsFallback(state, world, pos, direction);
    }

    public static boolean isNotSupportive(BlockState state, Direction facing) {
        for (NotSupportiveCheck check : NOT_SUPPORTIVE_CHECKS) {
            CheckResult result = check.isNotSupportive(state, facing);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return BlockMovementChecksImpl.isNotSupportiveFallback(state, facing);
    }

    // fallbacks

    private static boolean isMovementNecessaryFallback(BlockState state, Level world, BlockPos pos) {
        if (BlockMovementChecks.isBrittle(state))
            return true;
        if (state.is(AllBlockTags.MOVABLE_EMPTY_COLLIDER))
            return true;
        if (state.getCollisionShape(world, pos).isEmpty())
            return false;

        return !state.canBeReplaced();
    }

    private static boolean isMovementAllowedFallback(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof AbstractChassisBlock)
            return true;
        if (state.getDestroySpeed(world, pos) == -1)
            return false;
        if (state.is(AllBlockTags.RELOCATION_NOT_SUPPORTED))
            return false;
        if (state.is(AllBlockTags.NON_MOVABLE))
            return false;
        if (ContraptionMovementSetting.get(state) == ContraptionMovementSetting.UNMOVABLE)
            return false;

        // Move controllers only when they aren't moving
        if (block instanceof MechanicalPistonBlock && state.getValue(MechanicalPistonBlock.STATE) != MechanicalPistonBlock.PistonState.MOVING)
            return true;
        if (block instanceof MechanicalBearingBlock) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof MechanicalBearingBlockEntity)
                return !((MechanicalBearingBlockEntity) be).isRunning();
        }
        if (block instanceof ClockworkBearingBlock) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ClockworkBearingBlockEntity cbe)
                return !cbe.isRunning();
        }
        if (block instanceof PulleyBlock) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof PulleyBlockEntity pulley)
                return !pulley.running;
        }

        if (state.is(AllBlocks.BELT))
            return true;
        if (state.getBlock() instanceof GrindstoneBlock)
            return true;
        if (state.getBlock() instanceof ITrackBlock)
            return false;
        if (state.getBlock() instanceof StationBlock)
            return false;
        return state.getPistonPushReaction() != PushReaction.BLOCK;
    }

    private static boolean isBrittleFallback(BlockState state) {
        Block block = state.getBlock();
        if (state.hasProperty(BlockStateProperties.HANGING))
            return true;

        if (block instanceof LadderBlock)
            return true;
        if (block instanceof BaseTorchBlock)
            return true;
        if (block instanceof SignBlock)
            return true;
        if (block instanceof BasePressurePlateBlock)
            return true;
        if (block instanceof FaceAttachedHorizontalDirectionalBlock && !(block instanceof GrindstoneBlock)/* && !(block instanceof PackagerLinkBlock)*/)
            return true;
        if (block instanceof CartAssemblerBlock)
            return false;
        if (block instanceof BaseRailBlock)
            return true;
        if (block instanceof DiodeBlock)
            return true;
        if (block instanceof RedStoneWireBlock)
            return true;
        if (block instanceof WoolCarpetBlock)
            return true;
        if (block instanceof WhistleBlock)
            return true;
        if (block instanceof WhistleExtenderBlock)
            return true;
        if (block instanceof BeltFunnelBlock)
            return true;
        return state.is(AllBlockTags.BRITTLE);
    }

    private static boolean isBlockAttachedTowardsFallback(BlockState state, Level world, BlockPos pos, Direction direction) {
        Block block = state.getBlock();
        if (block instanceof LadderBlock)
            return state.getValue(LadderBlock.FACING) == direction.getOpposite();
        if (block instanceof WallTorchBlock)
            return state.getValue(WallTorchBlock.FACING) == direction.getOpposite();
        if (block instanceof WallSignBlock)
            return state.getValue(WallSignBlock.FACING) == direction.getOpposite();
        if (block instanceof StandingSignBlock)
            return direction == Direction.DOWN;
        if (block instanceof BasePressurePlateBlock)
            return direction == Direction.DOWN;
        if (block instanceof DoorBlock) {
            if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER && direction == Direction.UP)
                return true;
            return direction == Direction.DOWN;
        }
        if (block instanceof BedBlock) {
            Direction facing = state.getValue(BedBlock.FACING);
            if (state.getValue(BedBlock.PART) == BedPart.HEAD)
                facing = facing.getOpposite();
            return direction == facing;
        }
        if (block instanceof RedstoneLinkBlock)
            return direction.getOpposite() == state.getValue(RedstoneLinkBlock.FACING);
        if (block instanceof FlowerPotBlock)
            return direction == Direction.DOWN;
        if (block instanceof DiodeBlock)
            return direction == Direction.DOWN;
        if (block instanceof RedStoneWireBlock)
            return direction == Direction.DOWN;
        if (block instanceof WoolCarpetBlock)
            return direction == Direction.DOWN;
        if (block instanceof RedstoneWallTorchBlock)
            return state.getValue(RedstoneWallTorchBlock.FACING) == direction.getOpposite();
        if (block instanceof BaseTorchBlock)
            return direction == Direction.DOWN;
        if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
            AttachFace attachFace = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
            if (attachFace == AttachFace.CEILING)
                return direction == Direction.UP;
            if (attachFace == AttachFace.FLOOR)
                return direction == Direction.DOWN;
            if (attachFace == AttachFace.WALL)
                return direction.getOpposite() == state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
        }
        if (state.hasProperty(BlockStateProperties.HANGING))
            return direction == (state.getValue(BlockStateProperties.HANGING) ? Direction.UP : Direction.DOWN);
        if (block instanceof BaseRailBlock)
            return direction == Direction.DOWN;
        if (block instanceof AttachedActorBlock)
            return direction == state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        if (block instanceof HandCrankBlock)
            return direction == state.getValue(HandCrankBlock.FACING).getOpposite();
        if (block instanceof NozzleBlock)
            return direction == state.getValue(NozzleBlock.FACING).getOpposite();
        if (block instanceof BellBlock) {
            BellAttachType attachment = state.getValue(BlockStateProperties.BELL_ATTACHMENT);
            if (attachment == BellAttachType.FLOOR)
                return direction == Direction.DOWN;
            if (attachment == BellAttachType.CEILING)
                return direction == Direction.UP;
            return direction == state.getValue(HorizontalDirectionalBlock.FACING);
        }
        if (state.getBlock() instanceof SailBlock)
            return direction.getAxis() != state.getValue(SailBlock.FACING).getAxis();
        if (state.getBlock() instanceof FluidTankBlock)
            return ConnectivityHandler.isConnected(world, pos, pos.relative(direction));
        if (state.getBlock() instanceof ItemVaultBlock)
            return ConnectivityHandler.isConnected(world, pos, pos.relative(direction));
        if (state.is(AllBlocks.STICKER) && state.getValue(StickerBlock.EXTENDED)) {
            return direction == state.getValue(StickerBlock.FACING) && !BlockMovementChecks.isNotSupportive(
                world.getBlockState(pos.relative(direction)),
                direction.getOpposite()
            );
        }
        //        if (block instanceof AbstractBogeyBlock<?> bogey)
        //            return bogey.getStickySurfaces(world, pos, state).contains(direction);
        if (block instanceof WhistleBlock)
            return direction == (state.getValue(WhistleBlock.WALL) ? state.getValue(WhistleBlock.FACING) : Direction.DOWN);
        if (block instanceof WhistleExtenderBlock)
            return direction == Direction.DOWN;
        return false;
    }

    private static boolean isNotSupportiveFallback(BlockState state, Direction facing) {
        if (state.is(AllBlocks.MECHANICAL_DRILL))
            return state.getValue(BlockStateProperties.FACING) == facing;
        if (state.is(AllBlocks.MECHANICAL_BEARING))
            return state.getValue(BlockStateProperties.FACING) == facing;

        if (state.is(AllBlocks.CART_ASSEMBLER))
            return facing == Direction.DOWN;
        if (state.is(AllBlocks.MECHANICAL_SAW))
            return state.getValue(BlockStateProperties.FACING) == facing;
        if (state.is(AllBlocks.PORTABLE_STORAGE_INTERFACE))
            return state.getValue(PortableStorageInterfaceBlock.FACING) == facing;
        if (state.getBlock() instanceof AttachedActorBlock/* && !state.isOf(AllBlocks.MECHANICAL_ROLLER)*/)
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
        if (state.is(AllBlocks.ROPE_PULLEY))
            return facing == Direction.DOWN;
        if (state.getBlock() instanceof WoolCarpetBlock)
            return facing == Direction.UP;
        if (state.getBlock() instanceof SailBlock)
            return facing.getAxis() == state.getValue(SailBlock.FACING).getAxis();
        if (state.is(AllBlocks.PISTON_EXTENSION_POLE))
            return facing.getAxis() != state.getValue(BlockStateProperties.FACING).getAxis();
        if (state.is(AllBlocks.MECHANICAL_PISTON_HEAD))
            return facing.getAxis() != state.getValue(BlockStateProperties.FACING).getAxis();
        if (state.is(AllBlocks.STICKER) && !state.getValue(StickerBlock.EXTENDED))
            return facing == state.getValue(StickerBlock.FACING);
        if (state.getBlock() instanceof SlidingDoorBlock)
            return false;
        return BlockMovementChecks.isBrittle(state);
    }
}