package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.s2c.EjectorItemSpawnPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EjectorItemEntity extends ItemEntity {
    private boolean alive;
    public EntityLauncher launcher;
    public Direction direction;
    @Nullable
    public Pair<Vec3d, BlockPos> earlyTarget;
    public float earlyTargetTime;
    public int progress;
    public RenderData data;

    public EjectorItemEntity(World world, EjectorBlockEntity ejector, ItemStack stack) {
        super(AllEntityTypes.EJECTOR_ITEM, world);
        BlockPos pos = ejector.getPos();
        setPosition(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        setStack(stack);
        if (getWorld().isClient) {
            data = new RenderData();
        }
        loadLauncher(ejector);
    }

    public EjectorItemEntity(EntityType<? extends EjectorItemEntity> type, World world) {
        super(type, world);
        if (getWorld().isClient) {
            data = new RenderData();
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EjectorItemSpawnPacket(this, entityTrackerEntry);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        EjectorItemSpawnPacket spawnPacket = (EjectorItemSpawnPacket) packet;
        alive = spawnPacket.getAlive();
        progress = spawnPacket.getProgress();
        if (!alive) {
            if (getWorld().getBlockEntity(getBlockPos()) instanceof EjectorBlockEntity ejector) {
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
        if (getWorld().isClient) {
            data.calcRotate(facing);
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        view.putBoolean("Alive", alive);
        view.putInt("Progress", progress);
        if (!alive && !(getWorld().getBlockEntity(getBlockPos()) instanceof EjectorBlockEntity)) {
            view.put("Launcher", EntityLauncher.CODEC, launcher);
            view.put("Direction", Direction.CODEC, direction);
        }
        super.writeCustomData(view);
    }

    @Override
    public void readCustomData(ReadView view) {
        alive = view.getBoolean("Alive", false);
        progress = view.getInt("Progress", 0);
        if (!alive) {
            if (getWorld().getBlockEntity(getBlockPos()) instanceof EjectorBlockEntity ejector) {
                loadLauncher(ejector);
            } else {
                view.read("Launcher", EntityLauncher.CODEC).ifPresentOrElse(this::setLauncher, this::setIsAlive);
                view.read("Direction", Direction.CODEC).ifPresentOrElse(this::setDirection, this::setIsAlive);
                if (!alive && direction != null && getWorld().isClient) {
                    data.calcRotate(direction.getOpposite());
                }
            }
        }
        super.readCustomData(view);
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
    public ItemEntity copy() {
        ItemEntity copy = new ItemEntity(EntityType.ITEM, getWorld());
        copy.setStack(getStack().copy());
        copy.copyPositionAndRotation(this);
        copy.itemAge = itemAge;
        copy.uniqueOffset = uniqueOffset;
        return copy;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (alive) {
            super.onPlayerCollision(player);
        }
    }

    @Override
    public void tick() {
        boolean isClient = getWorld().isClient;
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
            ItemStack remainder = targetOpenInv.handleInsertion(getStack(), Direction.UP, isClient);
            if (remainder.isEmpty()) {
                discard();
                return;
            }
            setStack(remainder);
        }
        alive = true;
        Vec3d ejectVec = earlyTarget != null ? earlyTarget.getFirst() : getLaunchedItemLocation(maxTime);
        Vec3d ejectMotionVec = getLaunchedItemMotion(maxTime);
        setPosition(ejectVec.x, ejectVec.y, ejectVec.z);
        updateLastPosition();
        setVelocity(ejectMotionVec);
    }

    private DirectBeltInputBehaviour getTargetOpenInv() {
        BlockPos targetPos = earlyTarget != null ? earlyTarget.getSecond() : getBlockPos().up(launcher.getVerticalDistance())
            .offset(getFacing(), Math.max(1, launcher.getHorizontalDistance()));
        return BlockEntityBehaviour.get(getWorld(), targetPos, DirectBeltInputBehaviour.TYPE);
    }

    private boolean scanTrajectoryForObstacles(boolean isClient, float time) {
        Vec3d source = getLaunchedItemLocation(time);
        Vec3d target = getLaunchedItemLocation(time + 1);
        if (isClient) {
            data.calcRenderBox(source, target);
        }

        World world = getWorld();
        BlockHitResult rayTraceBlocks = world.raycast(new RaycastContext(
            source,
            target,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            ShapeContext.absent()
        ));
        boolean miss = rayTraceBlocks.getType() == HitResult.Type.MISS;

        if (!miss && rayTraceBlocks.getType() == HitResult.Type.BLOCK) {
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
        return true;
    }

    public Vec3d getLaunchedItemLocation(float time) {
        return launcher.getGlobalPos(time, direction, getPos());
    }

    public Vec3d getLaunchedItemMotion(float time) {
        return launcher.getGlobalVelocity(time, direction).multiply(.5f);
    }

    @Override
    public void updateTrackedPositionAndAngles(Vec3d pos, float yaw, float pitch) {
    }

    public class RenderData {
        public float rotateY;
        public Box renderBox = getBoundingBox();
        public int initAge = -1;
        public float animateOffset = -0.125f;

        public void calcRotate(Direction facing) {
            rotateY = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        }

        public void calcRenderBox(Vec3d source, Vec3d target) {
            renderBox = new Box(source.x - 1, source.y - 1, source.z - 1, target.x, target.y, target.z);
        }

        public void calcAnimateOffset(float totalTime) {
            animateOffset += (totalTime - progress - 1) / 4;
        }

        public void tick() {
            if (initAge == -1) {
                if (isOnGround()) {
                    initAge = age;
                }
            } else if (animateOffset < 0) {
                animateOffset = Math.min(animateOffset + 0.005f, 0);
            }
        }
    }
}