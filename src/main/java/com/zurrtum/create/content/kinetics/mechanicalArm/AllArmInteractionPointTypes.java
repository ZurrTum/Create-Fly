package com.zurrtum.create.content.kinetics.mechanicalArm;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.CrafterItemHandler;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import com.zurrtum.create.content.logistics.chute.AbstractChuteBlock;
import com.zurrtum.create.content.logistics.funnel.AbstractFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlock;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import static com.zurrtum.create.Create.MOD_ID;

public class AllArmInteractionPointTypes {
    public static final BasinType BASIN = register("basin", new BasinType());
    public static final BeltType BELT = register("belt", new BeltType());
    public static final BlazeBurnerType BLAZE_BURNER = register("blaze_burner", new BlazeBurnerType());
    public static final ChuteType CHUTE = register("chute", new ChuteType());
    public static final CrafterType CRAFTER = register("crafter", new CrafterType());
    public static final CrushingWheelsType CRUSHING_WHEELS = register("crushing_wheels", new CrushingWheelsType());
    public static final DeployerType DEPLOYER = register("deployer", new DeployerType());
    public static final DepotType DEPOT = register("depot", new DepotType());
    public static final FunnelType FUNNEL = register("funnel", new FunnelType());
    public static final MillstoneType MILLSTONE = register("millstone", new MillstoneType());
    public static final PackagerType PACKAGER = register("packager", new PackagerType());
    public static final SawType SAW = register("saw", new SawType());
    public static final CampfireType CAMPFIRE = register("campfire", new CampfireType());
    public static final ComposterType COMPOSTER = register("composter", new ComposterType());
    public static final JukeboxType JUKEBOX = register("jukebox", new JukeboxType());
    public static final RespawnAnchorType RESPAWN_ANCHOR = register("respawn_anchor", new RespawnAnchorType());

    private static <T extends ArmInteractionPointType> T register(String name, T type) {
        return Registry.register(CreateRegistries.ARM_INTERACTION_POINT_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name), type);
    }

    public static void register() {
    }

    public static class BasinType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return BasinBlock.isBasin(level, pos);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class BeltType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.BELT) && !(level.getBlockState(pos.above())
                .getBlock() instanceof BeltTunnelBlock) && BeltBlock.canTransportObjects(state);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BeltPoint(this, level, pos, state);
        }
    }

    public static class BlazeBurnerType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.BLAZE_BURNER);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BlazeBurnerPoint(this, level, pos, state);
        }
    }

    public static class ChuteType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return AbstractChuteBlock.isChute(state);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new TopFaceArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class CrafterType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.MECHANICAL_CRAFTER);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new CrafterPoint(this, level, pos, state);
        }
    }

    public static class CrushingWheelsType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.CRUSHING_WHEEL_CONTROLLER);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new CrushingWheelPoint(this, level, pos, state);
        }
    }

    public static class DeployerType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.DEPLOYER);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new DeployerPoint(this, level, pos, state);
        }
    }

    public static class DepotType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.DEPOT) || state.is(AllBlocks.WEIGHTED_EJECTOR) || state.is(AllBlocks.TRACK_STATION);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new DepotPoint(this, level, pos, state);
        }
    }

    public static class FunnelType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof AbstractFunnelBlock && !(state.hasProperty(FunnelBlock.EXTRACTING) && state.getValue(FunnelBlock.EXTRACTING)) && !(state.hasProperty(
                BeltFunnelBlock.SHAPE) && state.getValue(BeltFunnelBlock.SHAPE) == Shape.PUSHING);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new FunnelPoint(this, level, pos, state);
        }
    }

    public static class MillstoneType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.MILLSTONE);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class PackagerType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.PACKAGER) || state.is(AllBlocks.REPACKAGER);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class SawType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(AllBlocks.MECHANICAL_SAW) && state.getValue(SawBlock.FACING) == Direction.UP && ((KineticBlockEntity) level.getBlockEntity(
                pos)).getSpeed() != 0;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new DepotPoint(this, level, pos, state);
        }
    }

    public static class CampfireType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof CampfireBlock;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new CampfirePoint(this, level, pos, state);
        }
    }

    public static class ComposterType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(Blocks.COMPOSTER);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ComposterPoint(this, level, pos, state);
        }
    }

    public static class JukeboxType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(Blocks.JUKEBOX);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new JukeboxPoint(this, level, pos, state);
        }
    }

    public static class RespawnAnchorType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(Blocks.RESPAWN_ANCHOR);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new RespawnAnchorPoint(this, level, pos, state);
        }
    }

    //

    public static class DepositOnlyArmInteractionPoint extends ArmInteractionPoint {
        public DepositOnlyArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public void cycleMode() {
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 0;
        }
    }

    public static class TopFaceArmInteractionPoint extends ArmInteractionPoint {
        public TopFaceArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(pos).add(.5f, 1, .5f);
        }
    }

    public static class BeltPoint extends DepotPoint {
        public BeltPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public void keepAlive() {
            super.keepAlive();
            BeltBlockEntity beltBE = BeltHelper.getSegmentBE(level, pos);
            if (beltBE == null)
                return;
            TransportedItemStackHandlerBehaviour transport = beltBE.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
            if (transport == null)
                return;
            MutableBoolean found = new MutableBoolean(false);
            transport.handleProcessingOnAllItems(tis -> {
                if (found.isTrue())
                    return TransportedResult.doNothing();
                tis.lockedExternally = true;
                found.setTrue();
                return TransportedResult.doNothing();
            });
        }
    }

    public static class BlazeBurnerPoint extends DepositOnlyArmInteractionPoint {
        public BlazeBurnerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            ItemStack input = stack.copy();
            InteractionResult res = BlazeBurnerBlock.tryInsert(cachedState, level, pos, input, false, false, simulate);
            ItemStack remainder = ItemStack.EMPTY;
            if (res instanceof InteractionResult.Success success) {
                ItemStack newHandStack = success.heldItemTransformedTo();
                if (newHandStack != null && !newHandStack.isEmpty()) {
                    remainder = newHandStack;
                }
            }
            if (input.isEmpty()) {
                return remainder;
            } else {
                if (!simulate)
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
                return input;
            }
        }
    }

    public static class CrafterPoint extends ArmInteractionPoint {
        public CrafterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Direction getInteractionDirection() {
            return cachedState.getValueOrElse(MechanicalCrafterBlock.HORIZONTAL_FACING, Direction.SOUTH).getOpposite();
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return super.getInteractionPositionVector().add(Vec3.atLowerCornerOf(getInteractionDirection().getUnitVec3i()).scale(.5f));
        }

        @Override
        public void updateCachedState() {
            BlockState oldState = cachedState;
            super.updateCachedState();
            if (oldState != cachedState)
                cachedAngles = null;
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof MechanicalCrafterBlockEntity crafter))
                return ItemStack.EMPTY;
            CrafterItemHandler inventory = crafter.getInventory();
            ItemStack stack = inventory.getStack();
            int count = stack.getCount();
            if (count == 0) {
                return ItemStack.EMPTY;
            }
            if (amount >= count) {
                inventory.setStack(ItemStack.EMPTY);
            } else {
                stack.setCount(count - amount);
                stack = stack.copyWithCount(amount);
            }
            inventory.setChanged();
            return inventory.onExtract(stack);
        }
    }

    public static class DeployerPoint extends ArmInteractionPoint {
        public DeployerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Direction getInteractionDirection() {
            return cachedState.getValueOrElse(DeployerBlock.FACING, Direction.UP).getOpposite();
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return super.getInteractionPositionVector().add(Vec3.atLowerCornerOf(getInteractionDirection().getUnitVec3i()).scale(.65f));
        }

        @Override
        public void updateCachedState() {
            BlockState oldState = cachedState;
            super.updateCachedState();
            if (oldState != cachedState)
                cachedAngles = null;
        }
    }

    public static class DepotPoint extends ArmInteractionPoint {
        public DepotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(pos).add(.5f, 14 / 16f, .5f);
        }
    }

    public static class FunnelPoint extends DepositOnlyArmInteractionPoint {
        public FunnelPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            Direction funnelFacing = FunnelBlock.getFunnelFacing(cachedState);
            Vec3i normal = funnelFacing != null ? funnelFacing.getUnitVec3i() : Vec3i.ZERO;
            return VecHelper.getCenterOf(pos).add(Vec3.atLowerCornerOf(normal).scale(-.15f));
        }

        @Override
        protected Direction getInteractionDirection() {
            Direction funnelFacing = FunnelBlock.getFunnelFacing(cachedState);
            return funnelFacing != null ? funnelFacing.getOpposite() : Direction.UP;
        }

        @Override
        public void updateCachedState() {
            BlockState oldState = cachedState;
            super.updateCachedState();
            if (oldState != cachedState)
                cachedAngles = null;
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            ServerFilteringBehaviour filtering = BlockEntityBehaviour.get(level, pos, ServerFilteringBehaviour.TYPE);
            InvManipulationBehaviour inserter = BlockEntityBehaviour.get(level, pos, InvManipulationBehaviour.TYPE);
            if (cachedState.getValueOrElse(BlockStateProperties.POWERED, false))
                return stack;
            if (inserter == null)
                return stack;
            if (filtering != null && !filtering.test(stack))
                return stack;
            if (simulate)
                inserter.simulate();
            ItemStack insert = inserter.insert(stack);
            if (!simulate && insert.getCount() != stack.getCount()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof FunnelBlockEntity funnelBlockEntity) {
                    funnelBlockEntity.onTransfer(stack);
                    if (funnelBlockEntity.hasFlap())
                        funnelBlockEntity.flap(true);
                }
            }
            return insert;
        }
    }

    public static class CampfirePoint extends DepositOnlyArmInteractionPoint {
        public CampfirePoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof CampfireBlockEntity campfireBE))
                return stack;
            if (!level.recipeAccess().propertySet(RecipePropertySet.CAMPFIRE_INPUT).test(stack))
                return stack;
            if (simulate) {
                boolean hasSpace = false;
                for (ItemStack campfireStack : campfireBE.getItems()) {
                    if (campfireStack.isEmpty()) {
                        hasSpace = true;
                        break;
                    }
                }
                if (!hasSpace)
                    return stack;
                ItemStack remainder = stack.copy();
                remainder.shrink(1);
                return remainder;
            }
            ItemStack remainder = stack.copy();
            campfireBE.placeFood((ServerLevel) level, null, remainder);
            return remainder;
        }
    }

    public static class ComposterPoint extends ArmInteractionPoint {
        public ComposterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(pos).add(.5f, 13 / 16f, .5f);
        }

        @Nullable
        @Override
        protected Container getHandler(ArmBlockEntity armBlockEntity) {
            ComposterBlock composterBlock = (ComposterBlock) Blocks.COMPOSTER;
            return composterBlock.getContainer(cachedState, level, pos);
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            Container handler = getHandler(armBlockEntity);
            if (handler == null)
                return ItemStack.EMPTY;
            if (simulate) {
                return handler.count(stack -> true, amount, Direction.DOWN);
            }
            return handler.extract(stack -> true, amount, Direction.DOWN);
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 2;
        }
    }

    public static class JukeboxPoint extends TopFaceArmInteractionPoint {
        public JukeboxPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 1;
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            if (stack.get(DataComponents.JUKEBOX_PLAYABLE) == null)
                return stack;
            if (cachedState.getValueOrElse(JukeboxBlock.HAS_RECORD, true))
                return stack;
            if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukeboxBE))
                return stack;
            if (!jukeboxBE.getTheItem().isEmpty())
                return stack;
            ItemStack remainder = stack.copy();
            ItemStack toInsert = remainder.split(1);
            if (!simulate)
                jukeboxBE.setTheItem(toInsert);
            return remainder;
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            if (!cachedState.getValueOrElse(JukeboxBlock.HAS_RECORD, false))
                return ItemStack.EMPTY;
            if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukeboxBE))
                return ItemStack.EMPTY;
            if (!simulate)
                return jukeboxBE.removeItem(slot, amount);
            return jukeboxBE.getTheItem();
        }
    }

    public static class RespawnAnchorPoint extends DepositOnlyArmInteractionPoint {
        public RespawnAnchorPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(pos).add(.5f, 1, .5f);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            if (!stack.is(Items.GLOWSTONE))
                return stack;
            if (cachedState.getValueOrElse(RespawnAnchorBlock.CHARGE, 4) == 4)
                return stack;
            if (!simulate)
                RespawnAnchorBlock.charge(null, level, pos, cachedState);
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }
    }

    public static class CrushingWheelPoint extends DepositOnlyArmInteractionPoint {
        public CrushingWheelPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(pos).add(.5f, 1, .5f);
        }
    }
}