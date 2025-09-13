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
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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

    public static boolean isMovementNecessary(BlockState state, World world, BlockPos pos) {
        for (MovementNecessaryCheck check : MOVEMENT_NECESSARY_CHECKS) {
            CheckResult result = check.isMovementNecessary(state, world, pos);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return BlockMovementChecksImpl.isMovementNecessaryFallback(state, world, pos);
    }

    public static boolean isMovementAllowed(BlockState state, World world, BlockPos pos) {
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

    public static boolean isBlockAttachedTowards(BlockState state, World world, BlockPos pos, Direction direction) {
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

    private static boolean isMovementNecessaryFallback(BlockState state, World world, BlockPos pos) {
        if (BlockMovementChecks.isBrittle(state))
            return true;
        if (state.isIn(AllBlockTags.MOVABLE_EMPTY_COLLIDER))
            return true;
        if (state.getCollisionShape(world, pos).isEmpty())
            return false;

        return !state.isReplaceable();
    }

    private static boolean isMovementAllowedFallback(BlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof AbstractChassisBlock)
            return true;
        if (state.getHardness(world, pos) == -1)
            return false;
        if (state.isIn(AllBlockTags.RELOCATION_NOT_SUPPORTED))
            return false;
        if (state.isIn(AllBlockTags.NON_MOVABLE))
            return false;
        if (ContraptionMovementSetting.get(state) == ContraptionMovementSetting.UNMOVABLE)
            return false;

        // Move controllers only when they aren't moving
        if (block instanceof MechanicalPistonBlock && state.get(MechanicalPistonBlock.STATE) != MechanicalPistonBlock.PistonState.MOVING)
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

        if (state.isOf(AllBlocks.BELT))
            return true;
        if (state.getBlock() instanceof GrindstoneBlock)
            return true;
        if (state.getBlock() instanceof ITrackBlock)
            return false;
        if (state.getBlock() instanceof StationBlock)
            return false;
        return state.getPistonBehavior() != PistonBehavior.BLOCK;
    }

    private static boolean isBrittleFallback(BlockState state) {
        Block block = state.getBlock();
        if (state.contains(Properties.HANGING))
            return true;

        if (block instanceof LadderBlock)
            return true;
        if (block instanceof AbstractTorchBlock)
            return true;
        if (block instanceof AbstractSignBlock)
            return true;
        if (block instanceof AbstractPressurePlateBlock)
            return true;
        if (block instanceof WallMountedBlock && !(block instanceof GrindstoneBlock)/* && !(block instanceof PackagerLinkBlock)*/)
            return true;
        if (block instanceof CartAssemblerBlock)
            return false;
        if (block instanceof AbstractRailBlock)
            return true;
        if (block instanceof AbstractRedstoneGateBlock)
            return true;
        if (block instanceof RedstoneWireBlock)
            return true;
        if (block instanceof DyedCarpetBlock)
            return true;
        if (block instanceof WhistleBlock)
            return true;
        if (block instanceof WhistleExtenderBlock)
            return true;
        if (block instanceof BeltFunnelBlock)
            return true;
        return state.isIn(AllBlockTags.BRITTLE);
    }

    private static boolean isBlockAttachedTowardsFallback(BlockState state, World world, BlockPos pos, Direction direction) {
        Block block = state.getBlock();
        if (block instanceof LadderBlock)
            return state.get(LadderBlock.FACING) == direction.getOpposite();
        if (block instanceof WallTorchBlock)
            return state.get(WallTorchBlock.FACING) == direction.getOpposite();
        if (block instanceof WallSignBlock)
            return state.get(WallSignBlock.FACING) == direction.getOpposite();
        if (block instanceof SignBlock)
            return direction == Direction.DOWN;
        if (block instanceof AbstractPressurePlateBlock)
            return direction == Direction.DOWN;
        if (block instanceof DoorBlock) {
            if (state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER && direction == Direction.UP)
                return true;
            return direction == Direction.DOWN;
        }
        if (block instanceof BedBlock) {
            Direction facing = state.get(BedBlock.FACING);
            if (state.get(BedBlock.PART) == BedPart.HEAD)
                facing = facing.getOpposite();
            return direction == facing;
        }
        if (block instanceof RedstoneLinkBlock)
            return direction.getOpposite() == state.get(RedstoneLinkBlock.FACING);
        if (block instanceof FlowerPotBlock)
            return direction == Direction.DOWN;
        if (block instanceof AbstractRedstoneGateBlock)
            return direction == Direction.DOWN;
        if (block instanceof RedstoneWireBlock)
            return direction == Direction.DOWN;
        if (block instanceof DyedCarpetBlock)
            return direction == Direction.DOWN;
        if (block instanceof WallRedstoneTorchBlock)
            return state.get(WallRedstoneTorchBlock.FACING) == direction.getOpposite();
        if (block instanceof AbstractTorchBlock)
            return direction == Direction.DOWN;
        if (block instanceof WallMountedBlock) {
            BlockFace attachFace = state.get(WallMountedBlock.FACE);
            if (attachFace == BlockFace.CEILING)
                return direction == Direction.UP;
            if (attachFace == BlockFace.FLOOR)
                return direction == Direction.DOWN;
            if (attachFace == BlockFace.WALL)
                return direction.getOpposite() == state.get(WallMountedBlock.FACING);
        }
        if (state.contains(Properties.HANGING))
            return direction == (state.get(Properties.HANGING) ? Direction.UP : Direction.DOWN);
        if (block instanceof AbstractRailBlock)
            return direction == Direction.DOWN;
        if (block instanceof AttachedActorBlock)
            return direction == state.get(HorizontalFacingBlock.FACING).getOpposite();
        if (block instanceof HandCrankBlock)
            return direction == state.get(HandCrankBlock.FACING).getOpposite();
        if (block instanceof NozzleBlock)
            return direction == state.get(NozzleBlock.FACING).getOpposite();
        if (block instanceof BellBlock) {
            Attachment attachment = state.get(Properties.ATTACHMENT);
            if (attachment == Attachment.FLOOR)
                return direction == Direction.DOWN;
            if (attachment == Attachment.CEILING)
                return direction == Direction.UP;
            return direction == state.get(HorizontalFacingBlock.FACING);
        }
        if (state.getBlock() instanceof SailBlock)
            return direction.getAxis() != state.get(SailBlock.FACING).getAxis();
        if (state.getBlock() instanceof FluidTankBlock)
            return ConnectivityHandler.isConnected(world, pos, pos.offset(direction));
        if (state.getBlock() instanceof ItemVaultBlock)
            return ConnectivityHandler.isConnected(world, pos, pos.offset(direction));
        if (state.isOf(AllBlocks.STICKER) && state.get(StickerBlock.EXTENDED)) {
            return direction == state.get(StickerBlock.FACING) && !BlockMovementChecks.isNotSupportive(
                world.getBlockState(pos.offset(direction)),
                direction.getOpposite()
            );
        }
        //        if (block instanceof AbstractBogeyBlock<?> bogey)
        //            return bogey.getStickySurfaces(world, pos, state).contains(direction);
        if (block instanceof WhistleBlock)
            return direction == (state.get(WhistleBlock.WALL) ? state.get(WhistleBlock.FACING) : Direction.DOWN);
        if (block instanceof WhistleExtenderBlock)
            return direction == Direction.DOWN;
        return false;
    }

    private static boolean isNotSupportiveFallback(BlockState state, Direction facing) {
        if (state.isOf(AllBlocks.MECHANICAL_DRILL))
            return state.get(Properties.FACING) == facing;
        if (state.isOf(AllBlocks.MECHANICAL_BEARING))
            return state.get(Properties.FACING) == facing;

        if (state.isOf(AllBlocks.CART_ASSEMBLER))
            return facing == Direction.DOWN;
        if (state.isOf(AllBlocks.MECHANICAL_SAW))
            return state.get(Properties.FACING) == facing;
        if (state.isOf(AllBlocks.PORTABLE_STORAGE_INTERFACE))
            return state.get(PortableStorageInterfaceBlock.FACING) == facing;
        if (state.getBlock() instanceof AttachedActorBlock/* && !state.isOf(AllBlocks.MECHANICAL_ROLLER)*/)
            return state.get(Properties.HORIZONTAL_FACING) == facing;
        if (state.isOf(AllBlocks.ROPE_PULLEY))
            return facing == Direction.DOWN;
        if (state.getBlock() instanceof DyedCarpetBlock)
            return facing == Direction.UP;
        if (state.getBlock() instanceof SailBlock)
            return facing.getAxis() == state.get(SailBlock.FACING).getAxis();
        if (state.isOf(AllBlocks.PISTON_EXTENSION_POLE))
            return facing.getAxis() != state.get(Properties.FACING).getAxis();
        if (state.isOf(AllBlocks.MECHANICAL_PISTON_HEAD))
            return facing.getAxis() != state.get(Properties.FACING).getAxis();
        if (state.isOf(AllBlocks.STICKER) && !state.get(StickerBlock.EXTENDED))
            return facing == state.get(StickerBlock.FACING);
        if (state.getBlock() instanceof SlidingDoorBlock)
            return false;
        return BlockMovementChecks.isBrittle(state);
    }
}