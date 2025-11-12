package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.s2c.EjectorItemSpawnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class EjectorItemEntity extends ItemEntity {
    private boolean alive;
    public EntityLauncher launcher;
    public Direction direction;
    @Nullable
    public Pair<Vec3, BlockPos> earlyTarget;
    public float earlyTargetTime;
    public int progress;
    public RenderData data;

    public EjectorItemEntity(Level world, EjectorBlockEntity ejector, ItemStack stack) {
        super(AllEntityTypes.EJECTOR_ITEM, world);
        BlockPos pos = ejector.getBlockPos();
        setPos(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        setItem(stack);
        if (level().isClientSide()) {
            data = new RenderData();
        }
        loadLauncher(ejector);
    }

    public EjectorItemEntity(EntityType<? extends EjectorItemEntity> type, Level world) {
        super(type, world);
        if (level().isClientSide()) {
            data = new RenderData();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        return new EjectorItemSpawnPacket(this, entityTrackerEntry);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        EjectorItemSpawnPacket spawnPacket = (EjectorItemSpawnPacket) packet;
        alive = spawnPacket.getAlive();
        progress = spawnPacket.getProgress();
        if (!alive) {
            if (level().getBlockEntity(blockPosition()) instanceof EjectorBlockEntity ejector) {
                loadLauncher(ejector);
                return;
            }
            if (spawnPacket.hasLauncher()) {
                launcher = spawnPacket.getLauncher();
                direction = spawnPacket.getDirection();
                data.calcRotate(direction.getOpposite());
            } else {
                alive = true;
            }
        }
    }

    private void loadLauncher(EjectorBlockEntity ejector) {
        launcher = ejector.launcher;
        Direction facing = ejector.getFacing();
        direction = facing.getOpposite();
        if (level().isClientSide()) {
            data.calcRotate(facing);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        view.putBoolean("Alive", alive);
        view.putInt("Progress", progress);
        if (!alive && !(level().getBlockEntity(blockPosition()) instanceof EjectorBlockEntity)) {
            view.store("Launcher", EntityLauncher.CODEC, launcher);
            view.store("Direction", Direction.CODEC, direction);
        }
        super.addAdditionalSaveData(view);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        alive = view.getBooleanOr("Alive", false);
        progress = view.getIntOr("Progress", 0);
        if (!alive) {
            if (level().getBlockEntity(blockPosition()) instanceof EjectorBlockEntity ejector) {
                loadLauncher(ejector);
            } else {
                view.read("Launcher", EntityLauncher.CODEC).ifPresentOrElse(this::setLauncher, this::setIsAlive);
                view.read("Direction", Direction.CODEC).ifPresentOrElse(this::setDirection, this::setIsAlive);
                if (!alive && direction != null && level().isClientSide()) {
                    data.calcRotate(direction.getOpposite());
                }
            }
        }
        super.readAdditionalSaveData(view);
    }

    private void setLauncher(EntityLauncher launcher) {
        this.launcher = launcher;
    }

    private void setDirection(Direction direction) {
        this.direction = direction;
    }

    private void setIsAlive() {
        this.alive = true;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void playerTouch(Player player) {
        if (alive) {
            super.playerTouch(player);
        }
    }

    @Override
    public void tick() {
        boolean isClient = level().isClientSide();
        if (alive) {
            if (isClient) {
                data.tick();
            }
            super.tick();
            return;
        }
        boolean hit = scanTrajectoryForObstacles(isClient, progress + 1);
        float totalTime = Math.max(3, (float) launcher.getTotalFlyingTicks());
        if (hit) {
            placeItemAtTarget(isClient, Math.min(earlyTargetTime, totalTime));
            return;
        }
        if (progress + 2 >= totalTime) {
            if (isClient) {
                data.calcAnimateOffset(totalTime);
            }
            placeItemAtTarget(isClient, totalTime);
            return;
        }
        progress++;
    }

    private void placeItemAtTarget(boolean isClient, float maxTime) {
        DirectBeltInputBehaviour targetOpenInv = getTargetOpenInv();
        if (targetOpenInv != null) {
            ItemStack remainder = targetOpenInv.handleInsertion(getItem(), Direction.UP, isClient);
            if (remainder.isEmpty()) {
                discard();
                return;
            }
            setItem(remainder);
        }
        alive = true;
        Vec3 ejectVec = earlyTarget != null ? earlyTarget.getFirst() : getLaunchedItemLocation(maxTime);
        Vec3 ejectMotionVec = getLaunchedItemMotion(maxTime);
        setPos(ejectVec.x, ejectVec.y, ejectVec.z);
        setOldPos();
        setDeltaMovement(ejectMotionVec);
    }

    private DirectBeltInputBehaviour getTargetOpenInv() {
        BlockPos targetPos = earlyTarget != null ? earlyTarget.getSecond() : blockPosition().above(launcher.getVerticalDistance())
            .relative(getNearestViewDirection(), Math.max(1, launcher.getHorizontalDistance()));
        return BlockEntityBehaviour.get(level(), targetPos, DirectBeltInputBehaviour.TYPE);
    }

    private boolean scanTrajectoryForObstacles(boolean isClient, float time) {
        Vec3 source = getLaunchedItemLocation(time);
        Vec3 target = getLaunchedItemLocation(time + 1);
        if (isClient) {
            data.calcRenderBox(source, target);
        }

        Level world = level();
        BlockHitResult rayTraceBlocks = world.clip(new ClipContext(
            source,
            target,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            CollisionContext.empty()
        ));
        boolean miss = rayTraceBlocks.getType() == HitResult.Type.MISS;

        if (!miss && rayTraceBlocks.getType() == HitResult.Type.BLOCK) {
            BlockState blockState = world.getBlockState(rayTraceBlocks.getBlockPos());
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
        return true;
    }

    public Vec3 getLaunchedItemLocation(float time) {
        return launcher.getGlobalPos(time, direction, position());
    }

    public Vec3 getLaunchedItemMotion(float time) {
        return launcher.getGlobalVelocity(time, direction).scale(.5f);
    }

    @Override
    public void moveOrInterpolateTo(Vec3 pos, float yaw, float pitch) {
    }

    public class RenderData {
        public float rotateY;
        public AABB renderBox = getBoundingBox();
        public int initAge = -1;
        public float animateOffset = -0.125f;

        public void calcRotate(Direction facing) {
            rotateY = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        }

        public void calcRenderBox(Vec3 source, Vec3 target) {
            renderBox = new AABB(source.x - 1, source.y - 1, source.z - 1, target.x, target.y, target.z);
        }

        public void calcAnimateOffset(float totalTime) {
            animateOffset += (totalTime - progress - 1) / 4;
        }

        public void tick() {
            if (initAge == -1) {
                if (onGround()) {
                    initAge = tickCount;
                }
            } else if (animateOffset < 0) {
                animateOffset = Math.min(animateOffset + 0.005f, 0);
            }
        }
    }
}