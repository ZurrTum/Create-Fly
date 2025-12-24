package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.s2c.FunnelFlapPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.lang.ref.WeakReference;
import java.util.List;

public class FunnelBlockEntity extends SmartBlockEntity implements Clearable {

    private ServerFilteringBehaviour filtering;
    private InvManipulationBehaviour invManipulation;
    private VersionedInventoryTrackerBehaviour invVersionTracker;
    private int extractionCooldown;

    private WeakReference<Entity> lastObserved; // In-world Extractors only

    public LerpedFloat flap;

    enum Mode {
        INVALID,
        PAUSED,
        COLLECT,
        PUSHING_TO_BELT,
        TAKING_FROM_BELT,
        EXTRACT
    }

    public FunnelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.FUNNEL, pos, state);
        extractionCooldown = 0;
        flap = createChasingFlap();
    }

    Mode determineCurrentMode() {
        BlockState state = getBlockState();
        if (!FunnelBlock.isFunnel(state))
            return Mode.INVALID;
        if (state.getValueOrElse(BlockStateProperties.POWERED, false))
            return Mode.PAUSED;
        if (state.getBlock() instanceof BeltFunnelBlock) {
            Shape shape = state.getValue(BeltFunnelBlock.SHAPE);
            if (shape == Shape.PULLING)
                return Mode.TAKING_FROM_BELT;
            if (shape == Shape.PUSHING)
                return Mode.PUSHING_TO_BELT;

            BeltBlockEntity belt = BeltHelper.getSegmentBE(level, worldPosition.below());
            if (belt != null)
                return belt.getMovementFacing() == state.getValue(BeltFunnelBlock.HORIZONTAL_FACING) ? Mode.PUSHING_TO_BELT : Mode.TAKING_FROM_BELT;
            return Mode.INVALID;
        }
        if (state.getBlock() instanceof FunnelBlock)
            return state.getValue(FunnelBlock.EXTRACTING) ? Mode.EXTRACT : Mode.COLLECT;

        return Mode.INVALID;
    }

    @Override
    public void tick() {
        super.tick();
        flap.tickChaser();
        Mode mode = determineCurrentMode();
        if (level.isClientSide())
            return;

        // Redstone resets the extraction cooldown
        if (mode == Mode.PAUSED)
            extractionCooldown = 0;
        if (mode == Mode.TAKING_FROM_BELT)
            return;

        if (extractionCooldown > 0) {
            extractionCooldown--;
            return;
        }

        if (mode == Mode.PUSHING_TO_BELT)
            activateExtractingBeltFunnel();
        if (mode == Mode.EXTRACT)
            activateExtractor();
    }

    private void activateExtractor() {
        if (invVersionTracker.stillWaiting(invManipulation))
            return;

        BlockState blockState = getBlockState();
        Direction facing = AbstractFunnelBlock.getFunnelFacing(blockState);

        if (facing == null)
            return;

        // Check if last item is still blocking the extractor
        Entity lastEntity = lastObserved != null ? lastObserved.get() : null;
        if (lastEntity != null && lastEntity.isAlive()) {
            AABB area = getEntityOverflowScanningArea();
            if (lastEntity.getBoundingBox().intersects(area))
                return;
            lastObserved = null;
        }

        int amountToExtract = getAmountToExtract();
        ExtractionCountMode mode = getModeToExtract();
        ItemStack stack = invManipulation.simulate().extract(mode, amountToExtract);
        if (stack.isEmpty()) {
            invVersionTracker.awaitNewVersion(invManipulation);
            return;
        }

        // Only scan for blocking entities if there's something to extract
        AABB area = getEntityOverflowScanningArea();
        for (Entity entity : level.getEntities(null, area)) {
            if (entity instanceof ItemEntity || entity instanceof PackageEntity) {
                lastObserved = new WeakReference<>(entity);
                return;
            }
        }

        // Extract
        stack = invManipulation.extract(mode, amountToExtract);
        if (stack.isEmpty())
            return;

        flap(false);
        onTransfer(stack);

        Vec3 outputPos = VecHelper.getCenterOf(worldPosition);
        boolean vertical = facing.getAxis().isVertical();
        boolean up = facing == Direction.UP;

        outputPos = outputPos.add(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(vertical ? up ? .15f : .5f : .25f));
        if (!vertical)
            outputPos = outputPos.subtract(0, .45f, 0);

        Vec3 motion = Vec3.ZERO;
        if (up)
            motion = new Vec3(0, 4 / 16f, 0);

        ItemEntity item = new ItemEntity(level, outputPos.x, outputPos.y, outputPos.z, stack.copy());
        item.setDefaultPickUpDelay();
        item.setDeltaMovement(motion);
        level.addFreshEntity(item);
        lastObserved = new WeakReference<>(item);

        startCooldown();
    }

    static final AABB coreBB = new AABB(BlockPos.ZERO);

    private AABB getEntityOverflowScanningArea() {
        Direction facing = AbstractFunnelBlock.getFunnelFacing(getBlockState());
        AABB bb = coreBB.move(worldPosition);
        if (facing == null || facing == Direction.UP)
            return bb;
        return bb.expandTowards(0, -1, 0);
    }

    private void activateExtractingBeltFunnel() {
        if (invVersionTracker.stillWaiting(invManipulation))
            return;

        BlockState blockState = getBlockState();
        Direction facing = blockState.getValue(BeltFunnelBlock.HORIZONTAL_FACING);
        DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(level, worldPosition.below(), DirectBeltInputBehaviour.TYPE);

        if (inputBehaviour == null)
            return;
        if (!inputBehaviour.canInsertFromSide(facing))
            return;
        if (inputBehaviour.isOccupied(facing))
            return;

        int amountToExtract = getAmountToExtract();
        ExtractionCountMode mode = getModeToExtract();
        MutableBoolean deniedByInsertion = new MutableBoolean(false);
        ItemStack stack = invManipulation.extract(
            mode, amountToExtract, s -> {
                ItemStack handleInsertion = inputBehaviour.handleInsertion(s, facing, true);
                if (handleInsertion.isEmpty())
                    return true;
                deniedByInsertion.setTrue();
                return false;
            }
        );
        if (stack.isEmpty()) {
            if (deniedByInsertion.isFalse())
                invVersionTracker.awaitNewVersion(invManipulation.getInventory());
            return;
        }
        flap(false);
        onTransfer(stack);
        inputBehaviour.handleInsertion(stack, facing, false);
        startCooldown();
    }

    public int getAmountToExtract() {
        if (!supportsAmountOnFilter())
            return 64;
        int amountToExtract = invManipulation.getAmountFromFilter();
        if (!filtering.isActive())
            amountToExtract = 1;
        return amountToExtract;
    }

    public ExtractionCountMode getModeToExtract() {
        if (!supportsAmountOnFilter() || !filtering.isActive())
            return ExtractionCountMode.UPTO;
        return invManipulation.getModeFromFilter();
    }

    private int startCooldown() {
        return extractionCooldown = AllConfigs.server().logistics.defaultExtractionTimer.get();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        invManipulation = new InvManipulationBehaviour(this, (w, p, s) -> new BlockFace(p, AbstractFunnelBlock.getFunnelFacing(s).getOpposite()));
        behaviours.add(invManipulation);

        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));

        filtering = new ServerFilteringBehaviour(this);
        filtering.showCountWhen(this::supportsAmountOnFilter);
        filtering.onlyActiveWhen(this::supportsFiltering);
        //        filtering.withCallback($ -> invVersionTracker.reset());
        behaviours.add(filtering);

        behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput)
            .setInsertionHandler(this::handleDirectBeltInput));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.FUNNEL);
    }

    private boolean supportsAmountOnFilter() {
        BlockState blockState = getBlockState();
        boolean beltFunnelsupportsAmount = false;
        if (blockState.getBlock() instanceof BeltFunnelBlock) {
            Shape shape = blockState.getValue(BeltFunnelBlock.SHAPE);
            if (shape == Shape.PUSHING)
                beltFunnelsupportsAmount = true;
            else
                beltFunnelsupportsAmount = BeltHelper.getSegmentBE(level, worldPosition.below()) != null;
        }
        boolean extractor = blockState.getBlock() instanceof FunnelBlock && blockState.getValue(FunnelBlock.EXTRACTING);
        return beltFunnelsupportsAmount || extractor;
    }

    private boolean supportsDirectBeltInput(Direction side) {
        BlockState blockState = getBlockState();
        if (blockState == null)
            return false;
        if (!(blockState.getBlock() instanceof FunnelBlock))
            return false;
        if (blockState.getValue(FunnelBlock.EXTRACTING))
            return false;
        return FunnelBlock.getFunnelFacing(blockState) == Direction.UP;
    }

    private boolean supportsFiltering() {
        BlockState blockState = getBlockState();
        return blockState.is(AllBlocks.BRASS_BELT_FUNNEL) || blockState.is(AllBlocks.BRASS_FUNNEL);
    }

    private ItemStack handleDirectBeltInput(TransportedItemStack stack, Direction side, boolean simulate) {
        ItemStack inserted = stack.stack;
        if (!filtering.test(inserted))
            return inserted;
        if (determineCurrentMode() == Mode.PAUSED)
            return inserted;
        if (simulate)
            invManipulation.simulate();
        if (!simulate)
            onTransfer(inserted);
        return invManipulation.insert(inserted);
    }

    public void flap(boolean inward) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            FunnelFlapPacket packet = new FunnelFlapPacket(this, inward);
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)) {
                player.connection.send(packet);
            }
        } else {
            flap.setValue(inward ? -1 : 1);
            AllSoundEvents.FUNNEL_FLAP.playAt(level, worldPosition, 1, 1, true);
        }
    }

    public boolean hasFlap() {
        BlockState blockState = getBlockState();
        return AbstractFunnelBlock.getFunnelFacing(blockState).getAxis().isHorizontal();
    }

    public float getFlapOffset() {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof BeltFunnelBlock))
            return -1 / 16f;
        return switch (blockState.getValue(BeltFunnelBlock.SHAPE)) {
            case EXTENDED -> 8 / 16f;
            case PULLING, PUSHING -> -2 / 16f;
            default -> 0;
        };
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("TransferCooldown", extractionCooldown);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        extractionCooldown = view.getIntOr("TransferCooldown", 0);

        if (clientPacket)
            AllClientHandle.INSTANCE.queueUpdate(this);
    }

    @Override
    public void clearContent() {
        filtering.setFilter(ItemStack.EMPTY);
    }

    public void onTransfer(ItemStack stack) {
        AllBlocks.SMART_OBSERVER.onFunnelTransfer(level, worldPosition, stack);
        award(AllAdvancements.FUNNEL);
    }

    private LerpedFloat createChasingFlap() {
        return LerpedFloat.linear().startWithValue(.25f).chase(0, .05f, Chaser.EXP);
    }
}