package com.zurrtum.create.content.logistics.depot;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.funnel.AbstractFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorAwardPacket;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorElytraPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class EjectorBlockEntity extends KineticBlockEntity {

    ServerScrollValueBehaviour maxStackSize;
    public DepotBehaviour depotBehaviour;
    public EntityLauncher launcher;
    LerpedFloat lidProgress;
    boolean powered;
    boolean launch;
    State state;

    // item collision
    @Nullable
    public Pair<Vec3, BlockPos> earlyTarget;
    public float earlyTargetTime;
    // runtime stuff
    int scanCooldown;
    ItemStack trackedItem;

    public enum State implements StringRepresentable {
        CHARGED,
        LAUNCHING,
        RETRACTING;

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public EjectorBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.WEIGHTED_EJECTOR, pos, state);
        launcher = new EntityLauncher(1, 0);
        lidProgress = LerpedFloat.linear().startWithValue(1);
        this.state = State.RETRACTING;
        powered = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(depotBehaviour = new DepotBehaviour(this));

        maxStackSize = new ServerScrollValueBehaviour(this).between(0, 64);
        behaviours.add(maxStackSize);

        depotBehaviour.maxStackSize = () -> maxStackSize.getValue();
        depotBehaviour.canAcceptItems = () -> state == State.CHARGED;
        depotBehaviour.canFunnelsPullFrom = side -> side != getFacing();
        depotBehaviour.enableMerging();
        depotBehaviour.addSubBehaviours(behaviours);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateSignal();
    }

    public void activate() {
        launch = true;
        nudgeEntities();
    }

    protected boolean cannotLaunch() {
        return state != State.CHARGED && !(level.isClientSide() && state == State.LAUNCHING);
    }

    public void activateDeferred() {
        if (cannotLaunch())
            return;
        Direction facing = getFacing();
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition).inflate(-1 / 16f, 0, -1 / 16f));

        // Launch Items
        boolean doLogic = !level.isClientSide() || isVirtual();
        if (doLogic)
            launchItems();

        // Launch Entities
        for (Entity entity : entities) {
            boolean isPlayerEntity = entity instanceof Player;
            if (!entity.isAlive())
                continue;
            if (entity instanceof ItemEntity)
                continue;
            if (entity instanceof PackageEntity)
                continue;
            if (entity.getPistonPushReaction() == PushReaction.IGNORE)
                continue;

            entity.setOnGround(false);

            if (isPlayerEntity != level.isClientSide())
                continue;

            entity.setPos(worldPosition.getX() + .5f, worldPosition.getY() + 1, worldPosition.getZ() + .5f);
            launcher.applyMotion(entity, facing);

            if (!isPlayerEntity)
                continue;

            Player playerEntity = (Player) entity;

            if (launcher.getHorizontalDistance() * launcher.getHorizontalDistance() + launcher.getVerticalDistance() * launcher.getVerticalDistance() >= 25 * 25)
                AllClientHandle.INSTANCE.sendPacket(new EjectorAwardPacket(worldPosition));

            if (!(playerEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA))
                continue;

            playerEntity.setXRot(-35);
            playerEntity.setYRot(facing.toYRot());
            playerEntity.setDeltaMovement(playerEntity.getDeltaMovement().scale(.75f));
            deployElytra(playerEntity);
            AllClientHandle.INSTANCE.sendPacket(new EjectorElytraPacket(worldPosition));
        }

        if (doLogic) {
            lidProgress.chase(1, .8f, Chaser.EXP);
            state = State.LAUNCHING;
            if (!level.isClientSide()) {
                level.playSound(null, worldPosition, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, .35f, 1f);
                level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, .1f, 1.4f);
            }
        }
    }

    public void deployElytra(Player playerEntity) {
        EntityHack.setElytraFlying(playerEntity);
    }

    protected void launchItems() {
        ItemStack heldItemStack = depotBehaviour.getHeldItemStack();
        Direction funnelFacing = getFacing().getOpposite();

        if (AbstractFunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.above())) == funnelFacing) {
            DirectBeltInputBehaviour directOutput = getBehaviour(DirectBeltInputBehaviour.TYPE);

            if (depotBehaviour.heldItem != null) {
                ItemStack remainder = directOutput.tryExportingToBeltFunnel(heldItemStack, funnelFacing, false);
                if (remainder == null)
                    ;
                else if (remainder.isEmpty())
                    depotBehaviour.removeHeldItem();
                else if (remainder.getCount() != heldItemStack.getCount())
                    depotBehaviour.heldItem.stack = remainder;
            }

            for (Iterator<TransportedItemStack> iterator = depotBehaviour.incoming.iterator(); iterator.hasNext(); ) {
                TransportedItemStack transportedItemStack = iterator.next();
                ItemStack stack = transportedItemStack.stack;
                ItemStack remainder = directOutput.tryExportingToBeltFunnel(stack, funnelFacing, false);
                if (remainder == null)
                    ;
                else if (remainder.isEmpty())
                    iterator.remove();
                else if (!ItemStack.isSameItem(remainder, stack))
                    transportedItemStack.stack = remainder;
            }

            boolean change = false;
            Container outputs = depotBehaviour.processingOutputBuffer;
            for (int i = 0, size = outputs.getContainerSize(); i < size; i++) {
                ItemStack remainder = directOutput.tryExportingToBeltFunnel(outputs.getItem(i), funnelFacing, false);
                if (remainder != null) {
                    outputs.setItem(i, remainder);
                    change = true;
                }
            }
            if (change) {
                outputs.setChanged();
            }
            return;
        }

        if (!level.isClientSide())
            for (Direction d : Iterate.directions) {
                BlockState blockState = level.getBlockState(worldPosition.relative(d));
                if (!(blockState.getBlock() instanceof ObserverBlock))
                    continue;
                if (blockState.getValue(ObserverBlock.FACING) != d.getOpposite())
                    continue;
                blockState.updateShape(level, level, worldPosition.relative(d), d.getOpposite(), worldPosition, blockState, level.getRandom());
            }

        if (depotBehaviour.heldItem != null) {
            addToLaunchedItems(heldItemStack);
            depotBehaviour.removeHeldItem();
        }

        for (TransportedItemStack transportedItemStack : depotBehaviour.incoming)
            addToLaunchedItems(transportedItemStack.stack);
        depotBehaviour.incoming.clear();

        boolean change = false;
        Container outputs = depotBehaviour.processingOutputBuffer;
        for (int i = 0, size = outputs.getContainerSize(); i < size; i++) {
            ItemStack stack = outputs.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            addToLaunchedItems(stack);
            outputs.setItem(i, ItemStack.EMPTY);
            change = true;
        }
        if (change) {
            outputs.setChanged();
        }
    }

    protected void addToLaunchedItems(ItemStack stack) {
        if ((!level.isClientSide() || isVirtual()) && trackedItem == null && scanCooldown == 0) {
            scanCooldown = AllConfigs.server().kinetics.ejectorScanInterval.get();
            trackedItem = stack;
        }
        EjectorItemEntity item = new EjectorItemEntity(level, this, stack);
        level.addFreshEntity(item);
    }

    public Direction getFacing() {
        BlockState blockState = getBlockState();
        if (!blockState.is(AllBlocks.WEIGHTED_EJECTOR))
            return Direction.UP;
        Direction facing = blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
        return facing;
    }

    @Override
    public void tick() {
        super.tick();

        boolean doLogic = !level.isClientSide() || isVirtual();
        State prevState = state;

        if (scanCooldown > 0)
            scanCooldown--;

        if (launch) {
            launch = false;
            activateDeferred();
        }

        if (state == State.LAUNCHING) {
            lidProgress.chase(1, .8f, Chaser.EXP);
            lidProgress.tickChaser();
            if (lidProgress.getValue() > 1 - 1 / 16f && doLogic) {
                state = State.RETRACTING;
                lidProgress.setValue(1);
            }
        }

        if (state == State.CHARGED) {
            lidProgress.setValue(0);
            lidProgress.updateChaseSpeed(0);
            if (doLogic)
                ejectIfTriggered();
        }

        if (state == State.RETRACTING) {
            if (lidProgress.getChaseTarget() == 1 && !lidProgress.settled()) {
                lidProgress.tickChaser();
            } else {
                lidProgress.updateChaseTarget(0);
                lidProgress.updateChaseSpeed(0);
                if (lidProgress.getValue() == 0 && doLogic) {
                    state = State.CHARGED;
                    lidProgress.setValue(0);
                    sendData();
                }

                float value = Mth.clamp(lidProgress.getValue() - getWindUpSpeed(), 0, 1);
                lidProgress.setValue(value);

                int soundRate = (int) (1 / (getWindUpSpeed() * 5)) + 1;
                float volume = .125f;
                float pitch = 1.5f - lidProgress.getValue();
                if (((int) level.getGameTime()) % soundRate == 0 && doLogic)
                    level.playSound(null, worldPosition, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS, volume, pitch);
            }
        }

        if (state != prevState)
            notifyUpdate();
    }

    private boolean scanTrajectoryForObstacles(int time) {
        if (time <= 2)
            return false;

        Vec3 source = getLaunchedItemLocation(time);
        Vec3 target = getLaunchedItemLocation(time + 1);

        BlockHitResult rayTraceBlocks = level.clip(new ClipContext(source, target, Block.COLLIDER, Fluid.NONE, CollisionContext.empty()));
        boolean miss = rayTraceBlocks.getType() == Type.MISS;

        if (!miss && rayTraceBlocks.getType() == Type.BLOCK) {
            BlockState blockState = level.getBlockState(rayTraceBlocks.getBlockPos());
            if (FunnelBlock.isFunnel(blockState) && blockState.hasProperty(FunnelBlock.EXTRACTING) && blockState.getValue(FunnelBlock.EXTRACTING))
                miss = true;
        }

        if (miss) {
            if (earlyTarget != null && earlyTargetTime < time + 1) {
                earlyTarget = null;
                earlyTargetTime = 0;
            }
            return false;
        }

        Vec3 vec = rayTraceBlocks.getLocation();
        earlyTarget = Pair.of(vec.add(Vec3.atLowerCornerOf(rayTraceBlocks.getDirection().getUnitVec3i()).scale(.25f)), rayTraceBlocks.getBlockPos());
        earlyTargetTime = (float) (time + (source.distanceTo(vec) / source.distanceTo(target)));
        sendData();
        return true;
    }

    protected void nudgeEntities() {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(worldPosition).inflate(-1 / 16f, 0, -1 / 16f))) {
            if (!entity.isAlive())
                continue;
            if (entity.getPistonPushReaction() == PushReaction.IGNORE)
                continue;
            if (!(entity instanceof Player))
                entity.setPos(entity.getX(), entity.getY() + .125f, entity.getZ());
        }
    }

    protected void ejectIfTriggered() {
        if (powered)
            return;
        int presentStackSize = depotBehaviour.getPresentStackSize();
        if (presentStackSize == 0)
            return;
        if (presentStackSize < maxStackSize.getValue())
            return;
        if (depotBehaviour.heldItem != null && depotBehaviour.heldItem.beltPosition < .49f)
            return;

        Direction funnelFacing = getFacing().getOpposite();
        ItemStack held = depotBehaviour.getHeldItemStack();
        if (AbstractFunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.above())) == funnelFacing) {
            DirectBeltInputBehaviour directOutput = getBehaviour(DirectBeltInputBehaviour.TYPE);
            if (depotBehaviour.heldItem != null) {
                ItemStack tryFunnel = directOutput.tryExportingToBeltFunnel(held, funnelFacing, true);
                if (tryFunnel == null || !tryFunnel.isEmpty())
                    return;
            }
        }

        DirectBeltInputBehaviour targetOpenInv = getTargetOpenInv();

        // Do not eject if target cannot accept held item
        if (targetOpenInv != null && depotBehaviour.heldItem != null && targetOpenInv.handleInsertion(held, Direction.UP, true)
            .getCount() == held.getCount())
            return;

        activate();
        notifyUpdate();
    }

    public DirectBeltInputBehaviour getTargetOpenInv() {
        BlockPos targetPos = earlyTarget != null ? earlyTarget.getSecond() : worldPosition.above(launcher.getVerticalDistance())
            .relative(getFacing(), Math.max(1, launcher.getHorizontalDistance()));
        return BlockEntityBehaviour.get(level, targetPos, DirectBeltInputBehaviour.TYPE);
    }

    public Vec3 getLaunchedItemLocation(float time) {
        return launcher.getGlobalPos(time, getFacing().getOpposite(), worldPosition);
    }

    public Vec3 getLaunchedItemMotion(float time) {
        Vec3 pos = launcher.getGlobalVelocity(time, getFacing().getOpposite()).scale(.5f);
        return new Vec3(
            (int) (Mth.clamp(pos.x, -3.9, 3.9) * 8000.0) / 8000.0,
            (int) (Mth.clamp(pos.y, -3.9, 3.9) * 8000.0) / 8000.0,
            (int) (Mth.clamp(pos.z, -3.9, 3.9) * 8000.0) / 8000.0
        );
    }

    public float getWindUpSpeed() {
        int hd = launcher.getHorizontalDistance();
        int vd = launcher.getVerticalDistance();

        float speedFactor = Math.abs(getSpeed()) / 256f;
        float distanceFactor;
        if (hd == 0 && vd == 0)
            distanceFactor = 1;
        else
            distanceFactor = 1 * Mth.sqrt(hd * hd + vd * vd);
        return speedFactor / distanceFactor;
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("HorizontalDistance", launcher.getHorizontalDistance());
        view.putInt("VerticalDistance", launcher.getVerticalDistance());
        view.putBoolean("Powered", powered);
        view.store("State", State.CODEC, state);
        lidProgress.write(view.child("Lid"));

        if (earlyTarget != null) {
            view.store("EarlyTarget", Vec3.CODEC, earlyTarget.getFirst());
            view.store("EarlyTargetPos", BlockPos.CODEC, earlyTarget.getSecond());
            view.putFloat("EarlyTargetTime", earlyTargetTime);
        }
    }

    @Override
    public void writeSafe(ValueOutput view) {
        super.writeSafe(view);
        view.putInt("HorizontalDistance", launcher.getHorizontalDistance());
        view.putInt("VerticalDistance", launcher.getVerticalDistance());
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        int horizontalDistance = view.getIntOr("HorizontalDistance", 0);
        int verticalDistance = view.getIntOr("VerticalDistance", 0);

        if (launcher.getHorizontalDistance() != horizontalDistance || launcher.getVerticalDistance() != verticalDistance) {
            launcher.set(horizontalDistance, verticalDistance);
            launcher.clamp(AllConfigs.server().kinetics.maxEjectorDistance.get());
        }

        powered = view.getBooleanOr("Powered", false);
        state = view.read("State", State.CODEC).orElse(State.RETRACTING);
        lidProgress.read(view.childOrEmpty("Lid"), false);

        earlyTarget = null;
        earlyTargetTime = 0;
        view.read("EarlyTarget", Vec3.CODEC).ifPresent(vec3d -> {
            earlyTarget = Pair.of(vec3d, view.read("EarlyTargetPos", BlockPos.CODEC).orElseThrow());
            earlyTargetTime = view.getFloatOr("EarlyTargetTime", 0);
        });

        float forceAngle = view.getFloatOr("ForceAngle", -1);
        if (forceAngle != -1) {
            lidProgress.startWithValue(forceAngle);
        }
    }

    public void updateSignal() {
        boolean shoudPower = level.hasNeighborSignal(worldPosition);
        if (shoudPower == powered)
            return;
        powered = shoudPower;
        sendData();
    }

    public void setTarget(int horizontalDistance, int verticalDistance) {
        launcher.set(Math.max(1, horizontalDistance), verticalDistance);
        sendData();
    }

    public BlockPos getTargetPosition() {
        BlockState blockState = getBlockState();
        if (!blockState.is(AllBlocks.WEIGHTED_EJECTOR))
            return worldPosition;
        Direction facing = blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
        return worldPosition.relative(facing, launcher.getHorizontalDistance()).above(launcher.getVerticalDistance());
    }

    public float getLidProgress(float pt) {
        return lidProgress.getValue(pt);
    }

    public State getState() {
        return state;
    }

    private static abstract class EntityHack extends Entity {

        public EntityHack(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
            super(p_i48580_1_, p_i48580_2_);
        }

        public static void setElytraFlying(Entity e) {
            SynchedEntityData data = e.getEntityData();
            data.set(DATA_SHARED_FLAGS_ID, (byte) (data.get(DATA_SHARED_FLAGS_ID) | 1 << 7));
        }

    }
}