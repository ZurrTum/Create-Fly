package com.zurrtum.create.content.kinetics.belt;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.*;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;

import static com.zurrtum.create.content.kinetics.belt.BeltPart.MIDDLE;
import static com.zurrtum.create.content.kinetics.belt.BeltSlope.HORIZONTAL;
import static net.minecraft.core.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.core.Direction.AxisDirection.POSITIVE;

public class BeltBlockEntity extends KineticBlockEntity implements Clearable {
    public Map<Entity, TransportedEntityInfo> passengers;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<DyeColor> color;
    public int beltLength;
    public int index;
    public CasingType casing;
    public boolean covered;

    protected BlockPos controller;
    protected BeltInventory inventory;
    public ItemHandlerBeltSegment itemHandler;
    public VersionedInventoryTrackerBehaviour invVersionTracker;

    public CompoundTag trackerUpdateTag;

    public enum CasingType implements StringRepresentable {
        NONE,
        ANDESITE,
        BRASS;

        public static final Codec<CasingType> CODEC = StringRepresentable.fromEnum(CasingType::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BELT, pos, state);
        controller = BlockPos.ZERO;
        itemHandler = null;
        casing = CasingType.NONE;
        color = Optional.empty();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::canInsertFrom).setInsertionHandler(this::tryInsertingFromSide)
            .considerOccupiedWhen(this::isOccupied));
        behaviours.add(new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems).withStackPlacement(this::getWorldPositionOf));
        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
    }

    @Override
    public void tick() {
        // Init belt
        if (beltLength == 0)
            BeltBlock.initBelt(level, worldPosition);

        super.tick();

        if (!level.getBlockState(worldPosition).is(AllBlocks.BELT))
            return;

        initializeItemHandler();

        // Move Items
        if (!isController())
            return;

        invalidateRenderBoundingBox();

        getInventory().tick();

        if (getSpeed() == 0)
            return;

        // Move Entities
        if (passengers == null)
            passengers = new HashMap<>();

        List<Entity> toRemove = new ArrayList<>();
        passengers.forEach((entity, info) -> {
            boolean canBeTransported = BeltMovementHandler.canBeTransported(entity);
            boolean leftTheBelt = info.getTicksSinceLastCollision() > ((getBlockState().getValue(BeltBlock.SLOPE) != HORIZONTAL) ? 3 : 1);
            if (!canBeTransported || leftTheBelt) {
                toRemove.add(entity);
                return;
            }

            info.tick();
            BeltMovementHandler.transportEntity(this, entity, info);
        });
        toRemove.forEach(passengers::remove);
    }

    @Override
    public float calculateStressApplied() {
        if (!isController())
            return 0;
        return super.calculateStressApplied();
    }

    @Override
    public AABB createRenderBoundingBox() {
        if (!isController())
            return super.createRenderBoundingBox();
        else
            return super.createRenderBoundingBox().inflate(beltLength + 1);
    }

    public void initializeItemHandler() {
        if (level.isClientSide() || itemHandler != null)
            return;
        if (beltLength == 0 || controller == null)
            return;
        if (!level.isLoaded(controller))
            return;
        BlockEntity be = level.getBlockEntity(controller);
        if (be == null || !(be instanceof BeltBlockEntity))
            return;
        BeltInventory inventory = ((BeltBlockEntity) be).getInventory();
        if (inventory == null)
            return;
        itemHandler = new ItemHandlerBeltSegment(inventory, index);
    }

    @Override
    public void clearContent() {
        if (inventory != null) {
            inventory.getTransportedItems().clear();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (isController())
            getInventory().ejectAll();
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (controller != null)
            view.store("Controller", BlockPos.CODEC, controller);
        view.putBoolean("IsController", isController());
        view.putInt("Length", beltLength);
        view.putInt("Index", index);
        view.store("Casing", CasingType.CODEC, casing);
        view.putBoolean("Covered", covered);

        color.ifPresent(dyeColor -> view.store("Dye", DyeColor.CODEC, dyeColor));

        if (isController())
            getInventory().write(view.child("Inventory"));
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);

        if (view.getBooleanOr("IsController", false))
            controller = worldPosition;

        color = view.read("Dye", DyeColor.CODEC);

        if (!wasMoved) {
            if (!isController())
                controller = view.read("Controller", BlockPos.CODEC).orElse(null);
            index = view.getIntOr("Index", 0);
            beltLength = view.getIntOr("Length", 0);
        }

        if (isController())
            getInventory().read(view.childOrEmpty("Inventory"));

        CasingType casingBefore = casing;
        boolean coverBefore = covered;
        casing = view.read("Casing", CasingType.CODEC).orElse(CasingType.NONE);
        covered = view.getBooleanOr("Covered", false);

        if (!clientPacket)
            return;

        if (casingBefore == casing && coverBefore == covered)
            return;
        if (hasLevel())
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
    }

    @Override
    public void clearKineticInformation() {
        super.clearKineticInformation();
        beltLength = 0;
        index = 0;
        controller = null;
        trackerUpdateTag = new CompoundTag();
    }

    public boolean applyColor(DyeColor colorIn) {
        if (colorIn == null) {
            if (!color.isPresent())
                return false;
        } else if (color.isPresent() && color.get() == colorIn)
            return false;
        if (level.isClientSide())
            return true;

        for (BlockPos blockPos : BeltBlock.getBeltChain(level, getController())) {
            BeltBlockEntity belt = BeltHelper.getSegmentBE(level, blockPos);
            if (belt == null)
                continue;
            belt.color = Optional.ofNullable(colorIn);
            belt.setChanged();
            belt.sendData();
        }

        return true;
    }

    public BeltBlockEntity getControllerBE() {
        if (controller == null)
            return null;
        if (!level.isLoaded(controller))
            return null;
        BlockEntity be = level.getBlockEntity(controller);
        if (be == null || !(be instanceof BeltBlockEntity))
            return null;
        return (BeltBlockEntity) be;
    }

    public void setController(BlockPos controller) {
        this.controller = controller;
    }

    public BlockPos getController() {
        return controller == null ? worldPosition : controller;
    }

    public boolean isController() {
        return controller != null && worldPosition.getX() == controller.getX() && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    public float getBeltMovementSpeed() {
        return getSpeed() / 480f;
    }

    public float getDirectionAwareBeltMovementSpeed() {
        int offset = getBeltFacing().getAxisDirection().getStep();
        if (getBeltFacing().getAxis() == Axis.X)
            offset *= -1;
        return getBeltMovementSpeed() * offset;
    }

    public boolean hasPulley() {
        if (!getBlockState().is(AllBlocks.BELT))
            return false;
        return getBlockState().getValue(BeltBlock.PART) != MIDDLE;
    }

    protected boolean isLastBelt() {
        if (getSpeed() == 0)
            return false;

        Direction direction = getBeltFacing();
        if (getBlockState().getValue(BeltBlock.SLOPE) == BeltSlope.VERTICAL)
            return false;

        BeltPart part = getBlockState().getValue(BeltBlock.PART);
        if (part == MIDDLE)
            return false;

        boolean movingPositively = (getSpeed() > 0 == (direction.getAxisDirection().getStep() == 1)) ^ direction.getAxis() == Axis.X;
        return part == BeltPart.START ^ movingPositively;
    }

    public Vec3i getMovementDirection(boolean firstHalf) {
        return this.getMovementDirection(firstHalf, false);
    }

    public Vec3i getBeltChainDirection() {
        return this.getMovementDirection(true, true);
    }

    protected Vec3i getMovementDirection(boolean firstHalf, boolean ignoreHalves) {
        if (getSpeed() == 0)
            return BlockPos.ZERO;

        final BlockState blockState = getBlockState();
        final Direction beltFacing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        final BeltSlope slope = blockState.getValue(BeltBlock.SLOPE);
        final BeltPart part = blockState.getValue(BeltBlock.PART);
        final Axis axis = beltFacing.getAxis();

        Direction movementFacing = Direction.get(axis == Axis.X ? NEGATIVE : POSITIVE, axis);
        boolean notHorizontal = blockState.getValue(BeltBlock.SLOPE) != HORIZONTAL;
        if (getSpeed() < 0)
            movementFacing = movementFacing.getOpposite();
        Vec3i movement = movementFacing.getUnitVec3i();

        boolean slopeBeforeHalf = (part == BeltPart.END) == (beltFacing.getAxisDirection() == POSITIVE);
        boolean onSlope = notHorizontal && (part == MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
        boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

        if (!onSlope)
            return movement;

        return new Vec3i(movement.getX(), movingUp ? 1 : -1, movement.getZ());
    }

    public Direction getMovementFacing() {
        Axis axis = getBeltFacing().getAxis();
        return Direction.fromAxisAndDirection(axis, getBeltMovementSpeed() < 0 ^ axis == Axis.X ? NEGATIVE : POSITIVE);
    }

    public Direction getBeltFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public BeltInventory getInventory() {
        if (!isController()) {
            BeltBlockEntity controllerBE = getControllerBE();
            if (controllerBE != null)
                return controllerBE.getInventory();
            return null;
        }
        if (inventory == null) {
            inventory = new BeltInventory(this);
        }
        return inventory;
    }

    private void applyToAllItems(float maxDistanceFromCenter, Function<TransportedItemStack, TransportedResult> processFunction) {
        BeltBlockEntity controller = getControllerBE();
        if (controller == null)
            return;
        BeltInventory inventory = controller.getInventory();
        if (inventory != null)
            inventory.applyToEachWithin(index + .5f, maxDistanceFromCenter, processFunction);
    }

    private Vec3 getWorldPositionOf(TransportedItemStack transported) {
        BeltBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return Vec3.ZERO;
        return BeltHelper.getVectorForOffset(controllerBE, transported.beltPosition);
    }

    public void setCasingType(CasingType type) {
        if (casing == type)
            return;

        BlockState blockState = getBlockState();
        boolean shouldBlockHaveCasing = type != CasingType.NONE;

        if (level.isClientSide()) {
            casing = type;
            level.setBlock(worldPosition, blockState.setValue(BeltBlock.CASING, shouldBlockHaveCasing), 0);
            AllClientHandle.INSTANCE.queueUpdate(this);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
            return;
        }

        if (casing != CasingType.NONE)
            level.levelEvent(
                LevelEvent.PARTICLES_DESTROY_BLOCK,
                worldPosition,
                Block.getId(casing == CasingType.ANDESITE ? AllBlocks.ANDESITE_CASING.defaultBlockState() : AllBlocks.BRASS_CASING.defaultBlockState())
            );
        if (blockState.getValue(BeltBlock.CASING) != shouldBlockHaveCasing)
            KineticBlockEntity.switchToBlockState(level, worldPosition, blockState.setValue(BeltBlock.CASING, shouldBlockHaveCasing));
        casing = type;
        setChanged();
        sendData();
    }

    private boolean canInsertFrom(Direction side) {
        if (getSpeed() == 0)
            return false;
        BlockState state = getBlockState();
        if (state.hasProperty(BeltBlock.SLOPE) && (state.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS || state.getValue(BeltBlock.SLOPE) == BeltSlope.VERTICAL))
            return false;
        return getMovementFacing() != side.getOpposite();
    }

    private boolean isOccupied(Direction side) {
        BeltBlockEntity nextBeltController = getControllerBE();
        if (nextBeltController == null)
            return true;
        BeltInventory nextInventory = nextBeltController.getInventory();
        if (nextInventory == null)
            return true;
        if (getSpeed() == 0)
            return true;
        if (getMovementFacing() == side.getOpposite())
            return true;
        if (!nextInventory.canInsertAtFromSide(index, side))
            return true;
        return false;
    }

    private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
        BeltBlockEntity nextBeltController = getControllerBE();
        ItemStack inserted = transportedStack.stack;
        ItemStack empty = ItemStack.EMPTY;

        if (!BeltBlock.canTransportObjects(getBlockState()))
            return inserted;
        if (nextBeltController == null)
            return inserted;
        BeltInventory nextInventory = nextBeltController.getInventory();
        if (nextInventory == null)
            return inserted;

        BlockEntity teAbove = level.getBlockEntity(worldPosition.above());
        if (teAbove instanceof BrassTunnelBlockEntity tunnelBE) {
            if (tunnelBE.hasDistributionBehaviour()) {
                if (!tunnelBE.getStackToDistribute().isEmpty())
                    return inserted;
                if (!tunnelBE.testFlapFilter(side.getOpposite(), inserted))
                    return inserted;
                if (!simulate) {
                    BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);
                    tunnelBE.setStackToDistribute(inserted, side.getOpposite());
                }
                return empty;
            }
        }

        if (isOccupied(side))
            return inserted;
        if (simulate)
            return empty;

        transportedStack = transportedStack.copy();
        transportedStack.beltPosition = index + .5f - Math.signum(getDirectionAwareBeltMovementSpeed()) / 16f;

        Direction movementFacing = getMovementFacing();
        if (!side.getAxis().isVertical()) {
            if (movementFacing != side) {
                transportedStack.sideOffset = side.getAxisDirection().getStep() * .675f;
                if (side.getAxis() == Axis.X)
                    transportedStack.sideOffset *= -1;
            } else {
                // This creates a smoother transition from belt to belt
                float extraOffset = transportedStack.prevBeltPosition != 0 && BeltHelper.getSegmentBE(
                    level,
                    worldPosition.relative(movementFacing.getOpposite())
                ) != null ? .26f : 0;
                transportedStack.beltPosition = getDirectionAwareBeltMovementSpeed() > 0 ? index - extraOffset : index + 1 + extraOffset;
            }
        }

        transportedStack.prevSideOffset = transportedStack.sideOffset;
        transportedStack.insertedAt = index;
        transportedStack.insertedFrom = side;
        transportedStack.prevBeltPosition = transportedStack.beltPosition;

        BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);

        nextInventory.addItem(transportedStack);
        nextBeltController.setChanged();
        nextBeltController.sendData();
        return empty;
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return state.hasProperty(BeltBlock.SLOPE) && (state.getValue(BeltBlock.SLOPE) == BeltSlope.UPWARD || state.getValue(BeltBlock.SLOPE) == BeltSlope.DOWNWARD);
    }

    @Override
    public float propagateRotationTo(
        KineticBlockEntity target,
        BlockState stateFrom,
        BlockState stateTo,
        BlockPos diff,
        boolean connectedViaAxes,
        boolean connectedViaCogs
    ) {
        if (target instanceof BeltBlockEntity && !connectedViaAxes)
            return getController().equals(((BeltBlockEntity) target).getController()) ? 1 : 0;
        return 0;
    }

    public void invalidateItemHandler() {
        itemHandler = null;
    }

    public boolean shouldSkipVanillaRender() {
        if (level == null)
            return !isController();
        BlockState state = getBlockState();
        return state == null || !state.hasProperty(BeltBlock.PART) || state.getValue(BeltBlock.PART) != BeltPart.START;
    }

    public void setCovered(boolean blockCoveringBelt) {
        if (blockCoveringBelt == covered)
            return;
        covered = blockCoveringBelt;
        notifyUpdate();
    }
}
