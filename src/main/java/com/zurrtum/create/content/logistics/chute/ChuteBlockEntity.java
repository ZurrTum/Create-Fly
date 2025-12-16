package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlock;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ChuteBlockEntity extends SmartBlockEntity {
    public float pull;
    public float push;

    ItemStack item;
    public LerpedFloat itemPosition;
    public ChuteItemHandler itemHandler;
    boolean canPickUpItems;

    public float bottomPullDistance;
    float beltBelowOffset;
    TransportedItemStackHandlerBehaviour beltBelow;
    boolean updateAirFlow;
    int airCurrentUpdateCooldown;
    int entitySearchCooldown;

    VersionedInventoryTrackerBehaviour invVersionTracker;

    private final EnumMap<Direction, Supplier<Container>> capCaches = new EnumMap<>(Direction.class);

    public ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        item = ItemStack.EMPTY;
        itemPosition = LerpedFloat.linear();
        itemHandler = new ChuteItemHandler(this);
        canPickUpItems = false;
        bottomPullDistance = 0;
        updateAirFlow = true;
    }

    public ChuteBlockEntity(BlockPos pos, BlockState state) {
        this(AllBlockEntityTypes.CHUTE, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen((d) -> canDirectlyInsertCached()));
        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CHUTE);
    }

    // Cached per-tick, useful when a lot of items are waiting on top of it
    public boolean canDirectlyInsertCached() {
        return canPickUpItems;
    }

    private boolean canDirectlyInsert() {
        BlockState blockState = getBlockState();
        BlockState blockStateAbove = level.getBlockState(worldPosition.above());
        if (!AbstractChuteBlock.isChute(blockState))
            return false;
        if (AbstractChuteBlock.getChuteFacing(blockStateAbove) == Direction.DOWN)
            return false;
        if (getItemMotion() > 0 && getInputChutes().isEmpty())
            return false;
        return AbstractChuteBlock.isOpenChute(blockState);
    }

    @Override
    public void initialize() {
        super.initialize();
        onAdded();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -3, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide())
            canPickUpItems = canDirectlyInsert();

        boolean clientSide = level != null && level.isClientSide() && !isVirtual();
        float itemMotion = getItemMotion();
        if (itemMotion != 0 && level != null && level.isClientSide())
            spawnParticles(itemMotion);
        tickAirStreams(itemMotion);

        if (item.isEmpty() && !clientSide) {
            if (itemMotion < 0)
                handleInputFromAbove();
            if (itemMotion > 0)
                handleInputFromBelow();
            return;
        }

        float nextOffset = itemPosition.getValue() + itemMotion;

        if (itemMotion < 0) {
            if (nextOffset < .5f) {
                if (!handleDownwardOutput(true))
                    nextOffset = .5f;
                else if (nextOffset < 0) {
                    handleDownwardOutput(clientSide);
                    nextOffset = itemPosition.getValue();
                }
            }
        } else if (itemMotion > 0) {
            if (nextOffset > .5f) {
                if (!handleUpwardOutput(true))
                    nextOffset = .5f;
                else if (nextOffset > 1) {
                    handleUpwardOutput(clientSide);
                    nextOffset = itemPosition.getValue();
                }
            }
        }

        itemPosition.setValue(nextOffset);
    }

    private void updateAirFlow(float itemSpeed) {
        updateAirFlow = false;
        if (itemSpeed > 0 && level != null && !level.isClientSide()) {
            float speed = pull - push;
            beltBelow = null;

            float maxPullDistance;
            if (speed >= 128)
                maxPullDistance = 3;
            else if (speed >= 64)
                maxPullDistance = 2;
            else if (speed >= 32)
                maxPullDistance = 1;
            else
                maxPullDistance = Mth.lerpInt(speed / 32, 0, 1);

            if (AbstractChuteBlock.isChute(level.getBlockState(worldPosition.below())))
                maxPullDistance = 0;
            float flowLimit = maxPullDistance;
            if (flowLimit > 0)
                flowLimit = AirCurrent.getFlowLimit(level, worldPosition, maxPullDistance, Direction.DOWN);

            for (int i = 1; i <= flowLimit + 1; i++) {
                TransportedItemStackHandlerBehaviour behaviour = BlockEntityBehaviour.get(
                    level,
                    worldPosition.below(i),
                    TransportedItemStackHandlerBehaviour.TYPE
                );
                if (behaviour == null)
                    continue;
                beltBelow = behaviour;
                beltBelowOffset = i - 1;
                break;
            }
            this.bottomPullDistance = Math.max(0, flowLimit);
        }
        sendData();
    }

    private void findEntities(float itemSpeed) {
        if (bottomPullDistance <= 0 && !getItem().isEmpty() || itemSpeed <= 0 || level == null || level.isClientSide())
            return;
        if (!canActivate())
            return;
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        AABB searchArea = new AABB(center.add(0, -bottomPullDistance - 0.5, 0), center.add(0, -0.5, 0)).inflate(.45f);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, searchArea)) {
            if (!itemEntity.isAlive())
                continue;
            ItemStack entityItem = itemEntity.getItem();
            if (!canAcceptItem(entityItem))
                continue;
            setItem(entityItem.copy(), (float) (itemEntity.getBoundingBox().getCenter().y - worldPosition.getY()));
            itemEntity.discard();
            break;
        }
    }

    private void extractFromBelt(float itemSpeed) {
        if (itemSpeed <= 0 || level == null || level.isClientSide())
            return;
        if (getItem().isEmpty() && beltBelow != null) {
            beltBelow.handleCenteredProcessingOnAllItems(
                .5f, ts -> {
                    if (canAcceptItem(ts.stack)) {
                        setItem(ts.stack.copy(), -beltBelowOffset);
                        return TransportedResult.removeItem();
                    }
                    return TransportedResult.doNothing();
                }
            );
        }
    }

    private void tickAirStreams(float itemSpeed) {
        if (!level.isClientSide() && airCurrentUpdateCooldown-- <= 0) {
            airCurrentUpdateCooldown = AllConfigs.server().kinetics.fanBlockCheckRate.get();
            updateAirFlow = true;
        }

        if (updateAirFlow) {
            updateAirFlow(itemSpeed);
        }

        if (entitySearchCooldown-- <= 0 && item.isEmpty()) {
            entitySearchCooldown = 5;
            findEntities(itemSpeed);
        }

        extractFromBelt(itemSpeed);
    }

    public void blockBelowChanged() {
        updateAirFlow = true;
    }

    private void spawnParticles(float itemMotion) {
        // todo: reduce the amount of particles
        if (level == null)
            return;
        BlockState blockState = getBlockState();
        boolean up = itemMotion > 0;
        float absMotion = up ? itemMotion : -itemMotion;
        if (blockState == null || !AbstractChuteBlock.isChute(blockState))
            return;
        if (push == 0 && pull == 0)
            return;

        if (up && AbstractChuteBlock.isOpenChute(blockState) && BlockHelper.noCollisionInSpace(level, worldPosition.above()))
            spawnAirFlow(1, 2, absMotion, .5f);

        if (AbstractChuteBlock.getChuteFacing(blockState) != Direction.DOWN)
            return;

        if (AbstractChuteBlock.isTransparentChute(blockState))
            spawnAirFlow(up ? 0 : 1, up ? 1 : 0, absMotion, 1);

        if (!up && BlockHelper.noCollisionInSpace(level, worldPosition.below()))
            spawnAirFlow(0, -1, absMotion, .5f);

        if (up && canActivate() && bottomPullDistance > 0) {
            spawnAirFlow(-bottomPullDistance, 0, absMotion, 2);
            spawnAirFlow(-bottomPullDistance, 0, absMotion, 2);
        }
    }

    private void spawnAirFlow(float verticalStart, float verticalEnd, float motion, float drag) {
        if (level == null)
            return;
        AirParticleData airParticleData = new AirParticleData(drag, motion);
        Vec3 origin = Vec3.atLowerCornerOf(worldPosition);
        float xOff = level.getRandom().nextFloat() * .5f + .25f;
        float zOff = level.getRandom().nextFloat() * .5f + .25f;
        Vec3 v = origin.add(xOff, verticalStart, zOff);
        Vec3 d = origin.add(xOff, verticalEnd, zOff).subtract(v);
        if (level.getRandom().nextFloat() < 2 * motion)
            level.addAlwaysVisibleParticle(airParticleData, v.x, v.y, v.z, d.x, d.y, d.z);
    }

    private void handleInputFromAbove() {
        handleInput(grabCapability(Direction.UP), 1);
    }

    private void handleInputFromBelow() {
        handleInput(grabCapability(Direction.DOWN), 0);
    }

    private void handleInput(@Nullable Container inv, float startLocation) {
        if (inv == null)
            return;
        if (!canActivate())
            return;
        if (invVersionTracker.stillWaiting(inv))
            return;
        Predicate<ItemStack> canAccept = this::canAcceptItem;
        ItemStack extracted;
        if (getExtractionMode() == ExtractionCountMode.UPTO) {
            extracted = inv.extract(canAccept, getExtractionAmount());
        } else {
            extracted = inv.preciseExtract(canAccept, getExtractionAmount());
        }
        if (!extracted.isEmpty()) {
            setItem(extracted, startLocation);
            return;
        }
        invVersionTracker.awaitNewVersion(inv);
    }

    private boolean handleDownwardOutput(boolean simulate) {
        BlockState blockState = getBlockState();
        ChuteBlockEntity targetChute = getTargetChute(blockState);
        Direction direction = AbstractChuteBlock.getChuteFacing(blockState);

        if (level == null || direction == null || !this.canActivate())
            return false;
        Container capBelow = grabCapability(Direction.DOWN);
        if (capBelow != null) {
            if (level.isClientSide() && !isVirtual())
                return false;
            if (invVersionTracker.stillWaiting(capBelow))
                return false;
            if (!simulate) {
                int insert = capBelow.insertExist(item, Direction.UP);
                if (insert != 0) {
                    int count = item.getCount();
                    if (insert == count) {
                        setItem(ItemStack.EMPTY, itemPosition.getValue(0));
                    } else {
                        item.setCount(count - insert);
                        setItem(item, itemPosition.getValue(0));
                    }
                    return true;
                }
            } else if (capBelow.countSpace(item, 1, Direction.UP) != 0) {
                return true;
            }
            invVersionTracker.awaitNewVersion(capBelow);
            if (direction == Direction.DOWN)
                return false;
        }

        if (targetChute != null) {
            boolean canInsert = targetChute.canAcceptItem(item);
            if (!simulate && canInsert) {
                targetChute.setItem(item, direction == Direction.DOWN ? 1 : .51f);
                setItem(ItemStack.EMPTY);
            }
            return canInsert;
        }

        // Diagonal chutes cannot drop items
        if (direction.getAxis().isHorizontal())
            return false;

        if (FunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.below())) == Direction.DOWN)
            return false;
        if (Block.canSupportRigidBlock(level, worldPosition.below()))
            return false;

        if (!simulate) {
            Vec3 dropVec = VecHelper.getCenterOf(worldPosition).add(0, -12 / 16f, 0);
            ItemEntity dropped = new ItemEntity(level, dropVec.x, dropVec.y, dropVec.z, item.copy());
            dropped.setDefaultPickUpDelay();
            dropped.setDeltaMovement(0, -.25f, 0);
            level.addFreshEntity(dropped);
            setItem(ItemStack.EMPTY);
        }

        return true;
    }

    private boolean handleUpwardOutput(boolean simulate) {
        BlockState stateAbove = level.getBlockState(worldPosition.above());

        if (level == null || !this.canActivate())
            return false;

        if (AbstractChuteBlock.isOpenChute(getBlockState())) {
            Container capAbove = grabCapability(Direction.UP);
            if (capAbove != null) {
                if (level.isClientSide() && !isVirtual() && !ChuteBlock.isChute(stateAbove))
                    return false;
                if (invVersionTracker.stillWaiting(capAbove))
                    return false;
                if (!simulate) {
                    int insert = capAbove.insertExist(item, Direction.UP);
                    if (insert != 0) {
                        int count = item.getCount();
                        if (insert == count) {
                            item = ItemStack.EMPTY;
                        } else {
                            item.setCount(count - insert);
                        }
                        return true;
                    }
                } else if (capAbove.countSpace(item, 1, Direction.UP) != 0) {
                    return true;
                }
                invVersionTracker.awaitNewVersion(capAbove);
                return false;
            }
        }

        ChuteBlockEntity bestOutput = null;
        List<ChuteBlockEntity> inputChutes = getInputChutes();
        for (ChuteBlockEntity targetChute : inputChutes) {
            if (!targetChute.canAcceptItem(item))
                continue;
            float itemMotion = targetChute.getItemMotion();
            if (itemMotion < 0)
                continue;
            if (bestOutput == null || bestOutput.getItemMotion() < itemMotion) {
                bestOutput = targetChute;
            }
        }

        if (bestOutput != null) {
            if (!simulate) {
                bestOutput.setItem(item, 0);
                setItem(ItemStack.EMPTY);
            }
            return true;
        }

        if (FunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.above())) == Direction.UP)
            return false;
        if (BlockHelper.hasBlockSolidSide(stateAbove, level, worldPosition.above(), Direction.DOWN))
            return false;
        if (!inputChutes.isEmpty())
            return false;

        if (!simulate) {
            Vec3 dropVec = VecHelper.getCenterOf(worldPosition).add(0, 8 / 16f, 0);
            ItemEntity dropped = new ItemEntity(level, dropVec.x, dropVec.y, dropVec.z, item.copy());
            dropped.setDefaultPickUpDelay();
            dropped.setDeltaMovement(0, getItemMotion() * 2, 0);
            level.addFreshEntity(dropped);
            setItem(ItemStack.EMPTY);
        }
        return true;
    }

    protected boolean canAcceptItem(ItemStack stack) {
        return item.isEmpty();
    }

    protected int getExtractionAmount() {
        return 16;
    }

    protected ExtractionCountMode getExtractionMode() {
        return ExtractionCountMode.UPTO;
    }

    protected boolean canActivate() {
        return true;
    }

    private boolean canAcceptBlockEntity(BlockEntity be, @Nullable Direction opposite) {
        if (be instanceof ChuteBlockEntity) {
            return opposite == Direction.UP && be instanceof SmartChuteBlockEntity && !(getItemMotion() > 0);
        }
        return true;
    }

    private @Nullable Container grabCapability(@NotNull Direction side) {
        BlockPos pos = this.worldPosition.relative(side);
        if (level == null)
            return null;
        Supplier<Container> supplier = capCaches.get(side);
        if (supplier == null) {
            Direction opposite = side.getOpposite();
            if (level instanceof ServerLevel serverLevel) {
                Supplier<Container> cache = ItemHelper.getInventoryCache(serverLevel, pos, opposite, this::canAcceptBlockEntity);
                capCaches.put(side, cache);
                return cache.get();
            } else {
                BlockEntity be = level.getBlockEntity(pos);
                if (canAcceptBlockEntity(be, side)) {
                    return ItemHelper.getInventory(level, pos, null, be, opposite);
                }
                return null;
            }
        } else {
            return supplier.get();
        }
    }

    public void setItem(ItemStack stack) {
        setItem(stack, getItemMotion() < 0 ? 1 : 0);
    }

    public void setItem(ItemStack stack, float insertionPos) {
        item = stack;
        itemPosition.startWithValue(insertionPos);
        invVersionTracker.reset();
        if (!level.isClientSide()) {
            notifyUpdate();
            award(AllAdvancements.CHUTE);
        }
    }

    @Override
    public void invalidate() {
        capCaches.clear();
        super.invalidate();
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (!item.isEmpty()) {
            view.store("Item", ItemStack.CODEC, item);
        }
        view.putFloat("ItemPosition", itemPosition.getValue());
        view.putFloat("Pull", pull);
        view.putFloat("Push", push);
        view.putFloat("BottomAirFlowDistance", bottomPullDistance);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        ItemStack previousItem = item;
        item = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        itemPosition.startWithValue(view.getFloatOr("ItemPosition", 0));
        pull = view.getFloatOr("Pull", 0);
        push = view.getFloatOr("Push", 0);
        bottomPullDistance = view.getFloatOr("BottomAirFlowDistance", 0);
        super.read(view, clientPacket);

        if (hasLevel() && level != null && level.isClientSide() && !ItemStack.matches(previousItem, item) && !item.isEmpty()) {
            if (level.getRandom().nextInt(3) != 0)
                return;
            Vec3 p = VecHelper.getCenterOf(worldPosition);
            p = VecHelper.offsetRandomly(p, level.getRandom(), .5f);
            Vec3 m = Vec3.ZERO;
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, item), p.x, p.y, p.z, m.x, m.y, m.z);
        }
    }

    public float getItemMotion() {
        // Chutes per second
        final float fanSpeedModifier = 1 / 64f;
        final float maxItemSpeed = 20f;
        final float gravity = 4f;

        float motion = (push + pull) * fanSpeedModifier;
        return (Mth.clamp(motion, -maxItemSpeed, maxItemSpeed) + (motion <= 0 ? -gravity : 0)) / 20f;
    }

    @Override
    public void destroy() {
        super.destroy();
        ChuteBlockEntity targetChute = getTargetChute(getBlockState());
        List<ChuteBlockEntity> inputChutes = getInputChutes();
        if (!item.isEmpty() && level != null)
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), item);
        setRemoved();
        if (targetChute != null) {
            targetChute.updatePull();
            targetChute.propagatePush();
        }
        inputChutes.forEach(c -> c.updatePush(inputChutes.size()));
    }

    public void onAdded() {
        refreshBlockState();
        updatePull();
        ChuteBlockEntity targetChute = getTargetChute(getBlockState());
        if (targetChute != null)
            targetChute.propagatePush();
        else
            updatePush(1);
    }

    public void updatePull() {
        float totalPull = calculatePull();
        if (pull == totalPull)
            return;
        pull = totalPull;
        updateAirFlow = true;
        sendData();
        ChuteBlockEntity targetChute = getTargetChute(getBlockState());
        if (targetChute != null)
            targetChute.updatePull();
    }

    public void updatePush(int branchCount) {
        float totalPush = calculatePush(branchCount);
        if (push == totalPush)
            return;
        updateAirFlow = true;
        push = totalPush;
        sendData();
        propagatePush();
    }

    public void propagatePush() {
        List<ChuteBlockEntity> inputs = getInputChutes();
        inputs.forEach(c -> c.updatePush(inputs.size()));
    }

    protected float calculatePull() {
        BlockState blockStateAbove = level.getBlockState(worldPosition.above());
        if (blockStateAbove.is(AllBlocks.ENCASED_FAN) && blockStateAbove.getValue(EncasedFanBlock.FACING) == Direction.DOWN) {
            BlockEntity be = level.getBlockEntity(worldPosition.above());
            if (be instanceof EncasedFanBlockEntity fan && !be.isRemoved()) {
                return fan.getSpeed();
            }
        }

        float totalPull = 0;
        for (Direction d : Iterate.directions) {
            ChuteBlockEntity inputChute = getInputChute(d);
            if (inputChute == null)
                continue;
            totalPull += inputChute.pull;
        }
        return totalPull;
    }

    protected float calculatePush(int branchCount) {
        if (level == null)
            return 0;
        BlockState blockStateBelow = level.getBlockState(worldPosition.below());
        if (blockStateBelow.is(AllBlocks.ENCASED_FAN) && blockStateBelow.getValue(EncasedFanBlock.FACING) == Direction.UP) {
            BlockEntity be = level.getBlockEntity(worldPosition.below());
            if (be instanceof EncasedFanBlockEntity fan && !be.isRemoved()) {
                return fan.getSpeed();
            }
        }

        ChuteBlockEntity targetChute = getTargetChute(getBlockState());
        if (targetChute == null)
            return 0;
        return targetChute.push / branchCount;
    }

    @Nullable
    private ChuteBlockEntity getTargetChute(BlockState state) {
        if (level == null)
            return null;
        Direction targetDirection = AbstractChuteBlock.getChuteFacing(state);
        if (targetDirection == null)
            return null;
        BlockPos chutePos = worldPosition.below();
        if (targetDirection.getAxis().isHorizontal())
            chutePos = chutePos.relative(targetDirection.getOpposite());
        BlockState chuteState = level.getBlockState(chutePos);
        if (!AbstractChuteBlock.isChute(chuteState))
            return null;
        BlockEntity be = level.getBlockEntity(chutePos);
        if (be instanceof ChuteBlockEntity)
            return (ChuteBlockEntity) be;
        return null;
    }

    private List<ChuteBlockEntity> getInputChutes() {
        List<ChuteBlockEntity> inputs = new LinkedList<>();
        for (Direction d : Iterate.directions) {
            ChuteBlockEntity inputChute = getInputChute(d);
            if (inputChute == null)
                continue;
            inputs.add(inputChute);
        }
        return inputs;
    }

    @Nullable
    private ChuteBlockEntity getInputChute(Direction direction) {
        if (level == null || direction == Direction.DOWN)
            return null;
        direction = direction.getOpposite();
        BlockPos chutePos = worldPosition.above();
        if (direction.getAxis().isHorizontal())
            chutePos = chutePos.relative(direction);
        BlockState chuteState = level.getBlockState(chutePos);
        Direction chuteFacing = AbstractChuteBlock.getChuteFacing(chuteState);
        if (chuteFacing != direction)
            return null;
        BlockEntity be = level.getBlockEntity(chutePos);
        if (be instanceof ChuteBlockEntity && !be.isRemoved())
            return (ChuteBlockEntity) be;
        return null;
    }

    public ItemStack getItem() {
        return item;
    }
}
