package com.zurrtum.create.content.kinetics.fan;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World.ExplosionSourceType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NozzleBlockEntity extends SmartBlockEntity {

    private final List<Entity> pushingEntities = new ArrayList<>();
    private float range;
    private boolean pushing;
    private BlockPos fanPos;

    public NozzleBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.NOZZLE, pos, state);
        setLazyTickRate(5);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (!clientPacket)
            return;
        view.putFloat("Range", range);
        view.putBoolean("Pushing", pushing);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (!clientPacket)
            return;
        range = view.getFloat("Range", 0);
        pushing = view.getBoolean("Pushing", false);
    }

    @Override
    public void initialize() {
        fanPos = pos.offset(getCachedState().get(NozzleBlock.FACING).getOpposite());
        super.initialize();
    }

    @Override
    public void tick() {
        super.tick();

        float range = calcRange();
        if (this.range != range)
            setRange(range);

        Vec3d center = VecHelper.getCenterOf(pos);
        if (world.isClient() && range != 0) {
            if (world.random.nextInt(MathHelper.clamp((AllConfigs.server().kinetics.fanPushDistance.get() - (int) range), 1, 10)) == 0) {
                Vec3d start = VecHelper.offsetRandomly(center, world.random, pushing ? 1 : range / 2);
                Vec3d motion = center.subtract(start).normalize()
                    .multiply(MathHelper.clamp(range * (pushing ? .025f : 1f), 0, .5f) * (pushing ? -1 : 1));
                world.addParticleClient(ParticleTypes.POOF, start.x, start.y, start.z, motion.x, motion.y, motion.z);
            }
        }

        for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();
            Vec3d diff = entity.getEntityPos().subtract(center);

            if (!(entity instanceof PlayerEntity) && world.isClient())
                continue;

            double distance = diff.length();
            if (distance > range || entity.isSneaking() || AirCurrent.isPlayerCreativeFlying(entity)) {
                iterator.remove();
                continue;
            }

            if (!pushing && distance < 1.5f)
                continue;

            float factor = (entity instanceof ItemEntity) ? 1 / 128f : 1 / 32f;
            Vec3d pushVec = diff.normalize().multiply((range - distance) * (pushing ? 1 : -1));
            entity.setVelocity(entity.getVelocity().add(pushVec.multiply(factor)));
            entity.fallDistance = 0;
            entity.velocityModified = true;
        }

    }

    public void setRange(float range) {
        this.range = range;
        if (range == 0)
            pushingEntities.clear();
        sendData();
    }

    private float calcRange() {
        BlockEntity be = world.getBlockEntity(fanPos);
        if (!(be instanceof IAirCurrentSource source))
            return 0;

        if (source.getAirCurrent() == null)
            return 0;
        if (source.getSpeed() == 0)
            return 0;
        pushing = source.getAirFlowDirection() == source.getAirflowOriginSide();
        return source.getMaxDistance();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        if (range == 0)
            return;

        Vec3d center = VecHelper.getCenterOf(pos);
        Box bb = new Box(center, center).expand(range / 2f);

        for (Entity entity : world.getNonSpectatingEntities(Entity.class, bb)) {
            Vec3d diff = entity.getEntityPos().subtract(center);

            double distance = diff.length();
            if (distance > range || entity.isSneaking() || AirCurrent.isPlayerCreativeFlying(entity))
                continue;

            boolean canSee = canSee(entity);
            if (!canSee) {
                pushingEntities.remove(entity);
                continue;
            }

            if (!pushingEntities.contains(entity))
                pushingEntities.add(entity);
        }

        for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();
            if (entity.isAlive())
                continue;
            iterator.remove();
        }

        if (!pushing && pushingEntities.size() > 256 && !world.isClient()) {
            world.createExplosion(null, center.x, center.y, center.z, 2, ExplosionSourceType.NONE);
            for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext(); ) {
                Entity entity = iterator.next();
                entity.discard();
                iterator.remove();
            }
        }

    }

    private boolean canSee(Entity entity) {
        RaycastContext context = new RaycastContext(
            entity.getEntityPos(),
            VecHelper.getCenterOf(pos),
            ShapeType.COLLIDER,
            FluidHandling.NONE,
            entity
        );
        return pos.equals(world.raycast(context).getBlockPos());
    }

}
