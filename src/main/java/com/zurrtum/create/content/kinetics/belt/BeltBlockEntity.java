package com.zurrtum.create.content.kinetics.belt;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.*;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Clearable;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.WorldEvents;

import java.util.*;
import java.util.function.Function;

import static com.zurrtum.create.content.kinetics.belt.BeltPart.MIDDLE;
import static com.zurrtum.create.content.kinetics.belt.BeltSlope.HORIZONTAL;
import static net.minecraft.util.math.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.math.Direction.AxisDirection.POSITIVE;

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

    public NbtCompound trackerUpdateTag;

    public enum CasingType implements StringIdentifiable {
        NONE,
        ANDESITE,
        BRASS;

        public static final Codec<CasingType> CODEC = StringIdentifiable.createCodec(CasingType::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BELT, pos, state);
        controller = BlockPos.ORIGIN;
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
            BeltBlock.initBelt(world, pos);

        super.tick();

        if (!world.getBlockState(pos).isOf(AllBlocks.BELT))
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
            boolean leftTheBelt = info.getTicksSinceLastCollision() > ((getCachedState().get(BeltBlock.SLOPE) != HORIZONTAL) ? 3 : 1);
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
    public Box createRenderBoundingBox() {
        if (!isController())
            return super.createRenderBoundingBox();
        else
            return super.createRenderBoundingBox().expand(beltLength + 1);
    }

    public void initializeItemHandler() {
        if (world.isClient || itemHandler != null)
            return;
        if (beltLength == 0 || controller == null)
            return;
        if (!world.isPosLoaded(controller))
            return;
        BlockEntity be = world.getBlockEntity(controller);
        if (be == null || !(be instanceof BeltBlockEntity))
            return;
        BeltInventory inventory = ((BeltBlockEntity) be).getInventory();
        if (inventory == null)
            return;
        itemHandler = new ItemHandlerBeltSegment(inventory, index);
    }

    @Override
    public void clear() {
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
    public void write(WriteView view, boolean clientPacket) {
        if (controller != null)
            view.put("Controller", BlockPos.CODEC, controller);
        view.putBoolean("IsController", isController());
        view.putInt("Length", beltLength);
        view.putInt("Index", index);
        view.put("Casing", CasingType.CODEC, casing);
        view.putBoolean("Covered", covered);

        color.ifPresent(dyeColor -> view.put("Dye", DyeColor.CODEC, dyeColor));

        if (isController())
            getInventory().write(view.get("Inventory"));
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);

        if (view.getBoolean("IsController", false))
            controller = pos;

        color = view.read("Dye", DyeColor.CODEC);

        if (!wasMoved) {
            if (!isController())
                controller = view.read("Controller", BlockPos.CODEC).orElse(null);
            index = view.getInt("Index", 0);
            beltLength = view.getInt("Length", 0);
        }

        if (isController())
            getInventory().read(view.getReadView("Inventory"));

        CasingType casingBefore = casing;
        boolean coverBefore = covered;
        casing = view.read("Casing", CasingType.CODEC).orElse(CasingType.NONE);
        covered = view.getBoolean("Covered", false);

        if (!clientPacket)
            return;

        if (casingBefore == casing && coverBefore == covered)
            return;
        if (hasWorld())
            world.updateListeners(getPos(), getCachedState(), getCachedState(), 16);
    }

    @Override
    public void clearKineticInformation() {
        super.clearKineticInformation();
        beltLength = 0;
        index = 0;
        controller = null;
        trackerUpdateTag = new NbtCompound();
    }

    public boolean applyColor(DyeColor colorIn) {
        if (colorIn == null) {
            if (!color.isPresent())
                return false;
        } else if (color.isPresent() && color.get() == colorIn)
            return false;
        if (world.isClient())
            return true;

        for (BlockPos blockPos : BeltBlock.getBeltChain(world, getController())) {
            BeltBlockEntity belt = BeltHelper.getSegmentBE(world, blockPos);
            if (belt == null)
                continue;
            belt.color = Optional.ofNullable(colorIn);
            belt.markDirty();
            belt.sendData();
        }

        return true;
    }

    public BeltBlockEntity getControllerBE() {
        if (controller == null)
            return null;
        if (!world.isPosLoaded(controller))
            return null;
        BlockEntity be = world.getBlockEntity(controller);
        if (be == null || !(be instanceof BeltBlockEntity))
            return null;
        return (BeltBlockEntity) be;
    }

    public void setController(BlockPos controller) {
        this.controller = controller;
    }

    public BlockPos getController() {
        return controller == null ? pos : controller;
    }

    public boolean isController() {
        return controller != null && pos.getX() == controller.getX() && pos.getY() == controller.getY() && pos.getZ() == controller.getZ();
    }

    public float getBeltMovementSpeed() {
        return getSpeed() / 480f;
    }

    public float getDirectionAwareBeltMovementSpeed() {
        int offset = getBeltFacing().getDirection().offset();
        if (getBeltFacing().getAxis() == Axis.X)
            offset *= -1;
        return getBeltMovementSpeed() * offset;
    }

    public boolean hasPulley() {
        if (!getCachedState().isOf(AllBlocks.BELT))
            return false;
        return getCachedState().get(BeltBlock.PART) != MIDDLE;
    }

    protected boolean isLastBelt() {
        if (getSpeed() == 0)
            return false;

        Direction direction = getBeltFacing();
        if (getCachedState().get(BeltBlock.SLOPE) == BeltSlope.VERTICAL)
            return false;

        BeltPart part = getCachedState().get(BeltBlock.PART);
        if (part == MIDDLE)
            return false;

        boolean movingPositively = (getSpeed() > 0 == (direction.getDirection().offset() == 1)) ^ direction.getAxis() == Axis.X;
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

        final BlockState blockState = getCachedState();
        final Direction beltFacing = blockState.get(Properties.HORIZONTAL_FACING);
        final BeltSlope slope = blockState.get(BeltBlock.SLOPE);
        final BeltPart part = blockState.get(BeltBlock.PART);
        final Axis axis = beltFacing.getAxis();

        Direction movementFacing = Direction.get(axis == Axis.X ? NEGATIVE : POSITIVE, axis);
        boolean notHorizontal = blockState.get(BeltBlock.SLOPE) != HORIZONTAL;
        if (getSpeed() < 0)
            movementFacing = movementFacing.getOpposite();
        Vec3i movement = movementFacing.getVector();

        boolean slopeBeforeHalf = (part == BeltPart.END) == (beltFacing.getDirection() == POSITIVE);
        boolean onSlope = notHorizontal && (part == MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
        boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

        if (!onSlope)
            return movement;

        return new Vec3i(movement.getX(), movingUp ? 1 : -1, movement.getZ());
    }

    public Direction getMovementFacing() {
        Axis axis = getBeltFacing().getAxis();
        return Direction.from(axis, getBeltMovementSpeed() < 0 ^ axis == Axis.X ? NEGATIVE : POSITIVE);
    }

    public Direction getBeltFacing() {
        return getCachedState().get(Properties.HORIZONTAL_FACING);
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

    private Vec3d getWorldPositionOf(TransportedItemStack transported) {
        BeltBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return Vec3d.ZERO;
        return BeltHelper.getVectorForOffset(controllerBE, transported.beltPosition);
    }

    public void setCasingType(CasingType type) {
        if (casing == type)
            return;

        BlockState blockState = getCachedState();
        boolean shouldBlockHaveCasing = type != CasingType.NONE;

        if (world.isClient) {
            casing = type;
            world.setBlockState(pos, blockState.with(BeltBlock.CASING, shouldBlockHaveCasing), 0);
            AllClientHandle.INSTANCE.queueUpdate(this);
            world.updateListeners(pos, getCachedState(), getCachedState(), 16);
            return;
        }

        if (casing != CasingType.NONE)
            world.syncWorldEvent(
                WorldEvents.BLOCK_BROKEN,
                pos,
                Block.getRawIdFromState(casing == CasingType.ANDESITE ? AllBlocks.ANDESITE_CASING.getDefaultState() : AllBlocks.BRASS_CASING.getDefaultState())
            );
        if (blockState.get(BeltBlock.CASING) != shouldBlockHaveCasing)
            KineticBlockEntity.switchToBlockState(world, pos, blockState.with(BeltBlock.CASING, shouldBlockHaveCasing));
        casing = type;
        markDirty();
        sendData();
    }

    private boolean canInsertFrom(Direction side) {
        if (getSpeed() == 0)
            return false;
        BlockState state = getCachedState();
        if (state.contains(BeltBlock.SLOPE) && (state.get(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS || state.get(BeltBlock.SLOPE) == BeltSlope.VERTICAL))
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

        if (!BeltBlock.canTransportObjects(getCachedState()))
            return inserted;
        if (nextBeltController == null)
            return inserted;
        BeltInventory nextInventory = nextBeltController.getInventory();
        if (nextInventory == null)
            return inserted;

        BlockEntity teAbove = world.getBlockEntity(pos.up());
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
                transportedStack.sideOffset = side.getDirection().offset() * .675f;
                if (side.getAxis() == Axis.X)
                    transportedStack.sideOffset *= -1;
            } else {
                // This creates a smoother transition from belt to belt
                float extraOffset = transportedStack.prevBeltPosition != 0 && BeltHelper.getSegmentBE(
                    world,
                    pos.offset(movementFacing.getOpposite())
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
        nextBeltController.markDirty();
        nextBeltController.sendData();
        return empty;
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return state.contains(BeltBlock.SLOPE) && (state.get(BeltBlock.SLOPE) == BeltSlope.UPWARD || state.get(BeltBlock.SLOPE) == BeltSlope.DOWNWARD);
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
        if (world == null)
            return !isController();
        BlockState state = getCachedState();
        return state == null || !state.contains(BeltBlock.PART) || state.get(BeltBlock.PART) != BeltPart.START;
    }

    public void setCovered(boolean blockCoveringBelt) {
        if (blockCoveringBelt == covered)
            return;
        covered = blockCoveringBelt;
        notifyUpdate();
    }
}
