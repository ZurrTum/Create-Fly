package com.zurrtum.create.content.logistics.depot;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorAwardPacket;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorElytraPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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
    public Pair<Vec3d, BlockPos> earlyTarget;
    public float earlyTargetTime;
    // runtime stuff
    int scanCooldown;
    ItemStack trackedItem;

    public enum State implements StringIdentifiable {
        CHARGED,
        LAUNCHING,
        RETRACTING;

        public static final Codec<State> CODEC = StringIdentifiable.createCodec(State::values);

        @Override
        public String asString() {
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
        return state != State.CHARGED && !(world.isClient && state == State.LAUNCHING);
    }

    public void activateDeferred() {
        if (cannotLaunch())
            return;
        Direction facing = getFacing();
        List<Entity> entities = world.getNonSpectatingEntities(Entity.class, new Box(pos).expand(-1 / 16f, 0, -1 / 16f));

        // Launch Items
        boolean doLogic = !world.isClient || isVirtual();
        if (doLogic)
            launchItems();

        // Launch Entities
        for (Entity entity : entities) {
            boolean isPlayerEntity = entity instanceof PlayerEntity;
            if (!entity.isAlive())
                continue;
            if (entity instanceof ItemEntity)
                continue;
            if (entity instanceof PackageEntity)
                continue;
            if (entity.getPistonBehavior() == PistonBehavior.IGNORE)
                continue;

            entity.setOnGround(false);

            if (isPlayerEntity != world.isClient)
                continue;

            entity.setPosition(pos.getX() + .5f, pos.getY() + 1, pos.getZ() + .5f);
            launcher.applyMotion(entity, facing);

            if (!isPlayerEntity)
                continue;

            PlayerEntity playerEntity = (PlayerEntity) entity;

            if (launcher.getHorizontalDistance() * launcher.getHorizontalDistance() + launcher.getVerticalDistance() * launcher.getVerticalDistance() >= 25 * 25)
                AllClientHandle.INSTANCE.sendPacket(new EjectorAwardPacket(pos));

            if (!(playerEntity.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA))
                continue;

            playerEntity.setPitch(-35);
            playerEntity.setYaw(facing.getPositiveHorizontalDegrees());
            playerEntity.setVelocity(playerEntity.getVelocity().multiply(.75f));
            deployElytra(playerEntity);
            AllClientHandle.INSTANCE.sendPacket(new EjectorElytraPacket(pos));
        }

        if (doLogic) {
            lidProgress.chase(1, .8f, Chaser.EXP);
            state = State.LAUNCHING;
            if (!world.isClient) {
                world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, .35f, 1f);
                world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, .1f, 1.4f);
            }
        }
    }

    public void deployElytra(PlayerEntity playerEntity) {
        EntityHack.setElytraFlying(playerEntity);
    }

    protected void launchItems() {
        ItemStack heldItemStack = depotBehaviour.getHeldItemStack();
        Direction funnelFacing = getFacing().getOpposite();

        if (AbstractFunnelBlock.getFunnelFacing(world.getBlockState(pos.up())) == funnelFacing) {
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
                else if (!ItemStack.areItemsEqual(remainder, stack))
                    transportedItemStack.stack = remainder;
            }

            boolean change = false;
            Inventory outputs = depotBehaviour.processingOutputBuffer;
            for (int i = 0, size = outputs.size(); i < size; i++) {
                ItemStack remainder = directOutput.tryExportingToBeltFunnel(outputs.getStack(i), funnelFacing, false);
                if (remainder != null) {
                    outputs.setStack(i, remainder);
                    change = true;
                }
            }
            if (change) {
                outputs.markDirty();
            }
            return;
        }

        if (!world.isClient)
            for (Direction d : Iterate.directions) {
                BlockState blockState = world.getBlockState(pos.offset(d));
                if (!(blockState.getBlock() instanceof ObserverBlock))
                    continue;
                if (blockState.get(ObserverBlock.FACING) != d.getOpposite())
                    continue;
                blockState.getStateForNeighborUpdate(world, world, pos.offset(d), d.getOpposite(), pos, blockState, world.random);
            }

        if (depotBehaviour.heldItem != null) {
            addToLaunchedItems(heldItemStack);
            depotBehaviour.removeHeldItem();
        }

        for (TransportedItemStack transportedItemStack : depotBehaviour.incoming)
            addToLaunchedItems(transportedItemStack.stack);
        depotBehaviour.incoming.clear();

        boolean change = false;
        Inventory outputs = depotBehaviour.processingOutputBuffer;
        for (int i = 0, size = outputs.size(); i < size; i++) {
            ItemStack stack = outputs.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            addToLaunchedItems(stack);
            outputs.setStack(i, ItemStack.EMPTY);
            change = true;
        }
        if (change) {
            outputs.markDirty();
        }
    }

    protected void addToLaunchedItems(ItemStack stack) {
        if ((!world.isClient || isVirtual()) && trackedItem == null && scanCooldown == 0) {
            scanCooldown = AllConfigs.server().kinetics.ejectorScanInterval.get();
            trackedItem = stack;
        }
        EjectorItemEntity item = new EjectorItemEntity(world, this, stack);
        world.spawnEntity(item);
    }

    public Direction getFacing() {
        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.WEIGHTED_EJECTOR))
            return Direction.UP;
        Direction facing = blockState.get(EjectorBlock.HORIZONTAL_FACING);
        return facing;
    }

    @Override
    public void tick() {
        super.tick();

        boolean doLogic = !world.isClient || isVirtual();
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

                float value = MathHelper.clamp(lidProgress.getValue() - getWindUpSpeed(), 0, 1);
                lidProgress.setValue(value);

                int soundRate = (int) (1 / (getWindUpSpeed() * 5)) + 1;
                float volume = .125f;
                float pitch = 1.5f - lidProgress.getValue();
                if (((int) world.getTime()) % soundRate == 0 && doLogic)
                    world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, volume, pitch);
            }
        }

        if (state != prevState)
            notifyUpdate();
    }

    private boolean scanTrajectoryForObstacles(int time) {
        if (time <= 2)
            return false;

        Vec3d source = getLaunchedItemLocation(time);
        Vec3d target = getLaunchedItemLocation(time + 1);

        BlockHitResult rayTraceBlocks = world.raycast(new RaycastContext(
            source,
            target,
            ShapeType.COLLIDER,
            FluidHandling.NONE,
            ShapeContext.absent()
        ));
        boolean miss = rayTraceBlocks.getType() == Type.MISS;

        if (!miss && rayTraceBlocks.getType() == Type.BLOCK) {
            BlockState blockState = world.getBlockState(rayTraceBlocks.getBlockPos());
            if (FunnelBlock.isFunnel(blockState) && blockState.contains(FunnelBlock.EXTRACTING) && blockState.get(FunnelBlock.EXTRACTING))
                miss = true;
        }

        if (miss) {
            if (earlyTarget != null && earlyTargetTime < time + 1) {
                earlyTarget = null;
                earlyTargetTime = 0;
            }
            return false;
        }

        Vec3d vec = rayTraceBlocks.getPos();
        earlyTarget = Pair.of(vec.add(Vec3d.of(rayTraceBlocks.getSide().getVector()).multiply(.25f)), rayTraceBlocks.getBlockPos());
        earlyTargetTime = (float) (time + (source.distanceTo(vec) / source.distanceTo(target)));
        sendData();
        return true;
    }

    protected void nudgeEntities() {
        for (Entity entity : world.getNonSpectatingEntities(Entity.class, new Box(pos).expand(-1 / 16f, 0, -1 / 16f))) {
            if (!entity.isAlive())
                continue;
            if (entity.getPistonBehavior() == PistonBehavior.IGNORE)
                continue;
            if (!(entity instanceof PlayerEntity))
                entity.setPosition(entity.getX(), entity.getY() + .125f, entity.getZ());
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
        if (AbstractFunnelBlock.getFunnelFacing(world.getBlockState(pos.up())) == funnelFacing) {
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
        BlockPos targetPos = earlyTarget != null ? earlyTarget.getSecond() : pos.up(launcher.getVerticalDistance())
            .offset(getFacing(), Math.max(1, launcher.getHorizontalDistance()));
        return BlockEntityBehaviour.get(world, targetPos, DirectBeltInputBehaviour.TYPE);
    }

    public Vec3d getLaunchedItemLocation(float time) {
        return launcher.getGlobalPos(time, getFacing().getOpposite(), pos);
    }

    public Vec3d getLaunchedItemMotion(float time) {
        Vec3d pos = launcher.getGlobalVelocity(time, getFacing().getOpposite()).multiply(.5f);
        return new Vec3d(
            (int) (MathHelper.clamp(pos.x, -3.9, 3.9) * 8000.0) / 8000.0,
            (int) (MathHelper.clamp(pos.y, -3.9, 3.9) * 8000.0) / 8000.0,
            (int) (MathHelper.clamp(pos.z, -3.9, 3.9) * 8000.0) / 8000.0
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
            distanceFactor = 1 * MathHelper.sqrt(hd * hd + vd * vd);
        return speedFactor / distanceFactor;
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("HorizontalDistance", launcher.getHorizontalDistance());
        view.putInt("VerticalDistance", launcher.getVerticalDistance());
        view.putBoolean("Powered", powered);
        view.put("State", State.CODEC, state);
        lidProgress.write(view.get("Lid"));

        if (earlyTarget != null) {
            view.put("EarlyTarget", Vec3d.CODEC, earlyTarget.getFirst());
            view.put("EarlyTargetPos", BlockPos.CODEC, earlyTarget.getSecond());
            view.putFloat("EarlyTargetTime", earlyTargetTime);
        }
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        view.putInt("HorizontalDistance", launcher.getHorizontalDistance());
        view.putInt("VerticalDistance", launcher.getVerticalDistance());
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        int horizontalDistance = view.getInt("HorizontalDistance", 0);
        int verticalDistance = view.getInt("VerticalDistance", 0);

        if (launcher.getHorizontalDistance() != horizontalDistance || launcher.getVerticalDistance() != verticalDistance) {
            launcher.set(horizontalDistance, verticalDistance);
            launcher.clamp(AllConfigs.server().kinetics.maxEjectorDistance.get());
        }

        powered = view.getBoolean("Powered", false);
        state = view.read("State", State.CODEC).orElse(State.RETRACTING);
        lidProgress.read(view.getReadView("Lid"), false);

        earlyTarget = null;
        earlyTargetTime = 0;
        view.read("EarlyTarget", Vec3d.CODEC).ifPresent(vec3d -> {
            earlyTarget = Pair.of(vec3d, view.read("EarlyTargetPos", BlockPos.CODEC).orElseThrow());
            earlyTargetTime = view.getFloat("EarlyTargetTime", 0);
        });

        float forceAngle = view.getFloat("ForceAngle", -1);
        if (forceAngle != -1) {
            lidProgress.startWithValue(forceAngle);
        }
    }

    public void updateSignal() {
        boolean shoudPower = world.isReceivingRedstonePower(pos);
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
        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.WEIGHTED_EJECTOR))
            return pos;
        Direction facing = blockState.get(EjectorBlock.HORIZONTAL_FACING);
        return pos.offset(facing, launcher.getHorizontalDistance()).up(launcher.getVerticalDistance());
    }

    public float getLidProgress(float pt) {
        return lidProgress.getValue(pt);
    }

    public State getState() {
        return state;
    }

    private static abstract class EntityHack extends Entity {

        public EntityHack(EntityType<?> p_i48580_1_, World p_i48580_2_) {
            super(p_i48580_1_, p_i48580_2_);
        }

        public static void setElytraFlying(Entity e) {
            DataTracker data = e.getDataTracker();
            data.set(FLAGS, (byte) (data.get(FLAGS) | 1 << 7));
        }

    }
}