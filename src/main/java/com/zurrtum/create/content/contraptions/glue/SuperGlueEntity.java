package com.zurrtum.create.content.contraptions.glue;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.schematic.requirement.SpecialEntityItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import com.zurrtum.create.content.contraptions.chassis.AbstractChassisBlock;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.packet.s2c.SuperGlueSpawnPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SuperGlueEntity extends Entity implements SpecialEntityItemRequirement {
    public static Box span(BlockPos startPos, BlockPos endPos) {
        return new Box(Vec3d.of(startPos), Vec3d.of(endPos)).stretch(1, 1, 1);
    }

    public static boolean isGlued(WorldAccess level, BlockPos blockPos, Direction direction, Set<SuperGlueEntity> cached) {
        BlockPos targetPos = blockPos.offset(direction);
        if (cached != null)
            for (SuperGlueEntity glueEntity : cached)
                if (glueEntity.contains(blockPos) && glueEntity.contains(targetPos))
                    return true;
        for (SuperGlueEntity glueEntity : level.getNonSpectatingEntities(SuperGlueEntity.class, span(blockPos, targetPos).expand(16))) {
            if (!glueEntity.contains(blockPos) || !glueEntity.contains(targetPos))
                continue;
            if (cached != null)
                cached.add(glueEntity);
            return true;
        }
        return false;
    }

    public static List<SuperGlueEntity> collectCropped(World level, Box bb) {
        List<SuperGlueEntity> glue = new ArrayList<>();
        for (SuperGlueEntity glueEntity : level.getNonSpectatingEntities(SuperGlueEntity.class, bb)) {
            Box glueBox = glueEntity.getBoundingBox();
            Box intersect = bb.intersection(glueBox);
            if (intersect.getLengthX() * intersect.getLengthY() * intersect.getLengthZ() == 0)
                continue;
            if (MathHelper.approximatelyEquals(intersect.getAverageSideLength(), 1))
                continue;
            glue.add(new SuperGlueEntity(level, intersect));
        }
        return glue;
    }

    public SuperGlueEntity(EntityType<? extends SuperGlueEntity> type, World world) {
        super(type, world);
    }

    public SuperGlueEntity(World world, Box boundingBox) {
        this(AllEntityTypes.SUPER_GLUE, world);
        setBoundingBox(boundingBox);
        resetPositionToBB();
    }

    public void resetPositionToBB() {
        Box bb = getBoundingBox();
        setPos(bb.getCenter().x, bb.minY, bb.getCenter().z);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    public static boolean isValidFace(World world, BlockPos pos, Direction direction) {
        BlockState state = world.getBlockState(pos);
        if (BlockMovementChecks.isBlockAttachedTowards(state, world, pos, direction))
            return true;
        if (!BlockMovementChecks.isMovementNecessary(state, world, pos))
            return false;
        return !BlockMovementChecks.isNotSupportive(state, direction);
    }

    public static boolean isSideSticky(World world, BlockPos pos, Direction direction) {
        BlockState state = world.getBlockState(pos);
        if (state.isOf(AllBlocks.STICKY_MECHANICAL_PISTON))
            return state.get(DirectionalKineticBlock.FACING) == direction;

        if (state.isOf(AllBlocks.STICKER))
            return state.get(FacingBlock.FACING) == direction;

        if (state.getBlock() == Blocks.SLIME_BLOCK)
            return true;
        if (state.getBlock() == Blocks.HONEY_BLOCK)
            return true;

        if (state.isOf(AllBlocks.CART_ASSEMBLER))
            return Direction.UP == direction;

        if (state.isOf(AllBlocks.GANTRY_CARRIAGE))
            return state.get(DirectionalKineticBlock.FACING) == direction;

        if (state.getBlock() instanceof BearingBlock) {
            return state.get(DirectionalKineticBlock.FACING) == direction;
        }

        if (state.getBlock() instanceof AbstractChassisBlock) {
            BooleanProperty glueableSide = ((AbstractChassisBlock) state.getBlock()).getGlueableSide(state, direction);
            if (glueableSide == null)
                return false;
            return state.get(glueableSide);
        }

        return false;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        lastPitch = getPitch();
        lastYaw = getYaw();
        //        walkDistO = walkDist;
        lastX = getX();
        lastY = getY();
        lastZ = getZ();

        if (getBoundingBox().getLengthX() == 0)
            discard();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        Box bb = getBoundingBox();
        setPos(x, y, z);
        Vec3d center = bb.getCenter();
        setBoundingBox(bb.offset(-center.x, -bb.minY, -center.z).offset(x, y, z));
    }

    @Override
    public void move(MovementType typeIn, Vec3d pos) {
        if (!getWorld().isClient && isAlive() && pos.lengthSquared() > 0.0D)
            discard();
    }

    @Override
    public void addVelocity(double x, double y, double z) {
        if (!getWorld().isClient && isAlive() && x * x + y * y + z * z > 0.0D)
            discard();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).withEyeHeight(0.0F);
    }

    public void playPlaceSound() {
        AllSoundEvents.SLIME_ADDED.playFrom(this, 0.5F, 0.75F);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    @Override
    public void writeCustomData(WriteView view) {
        Box box = getBoundingBox().offset(getPos().multiply(-1));
        view.put("Box", CreateCodecs.BOX_CODEC, box);
    }

    @Override
    public void readCustomData(ReadView view) {
        Box box = view.read("Box", CreateCodecs.BOX_CODEC).orElseThrow().offset(getPos());
        setBoundingBox(box);
    }

    public static void writeBoundingBox(NbtCompound compound, Box bb) {
        compound.put("From", VecHelper.writeNBT(new Vec3d(bb.minX, bb.minY, bb.minZ)));
        compound.put("To", VecHelper.writeNBT(new Vec3d(bb.maxX, bb.maxY, bb.maxZ)));
    }

    public static Box readBoundingBox(NbtCompound compound) {
        Vec3d from = VecHelper.readNBT(compound.getListOrEmpty("From"));
        Vec3d to = VecHelper.readNBT(compound.getListOrEmpty("To"));
        return new Box(from, to);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new SuperGlueSpawnPacket(this, entityTrackerEntry);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        setBoundingBox(((SuperGlueSpawnPacket) packet).getBox());
    }

    @Override
    protected boolean shouldSetPositionOnLoad() {
        return false;
    }

    @Override
    public float applyRotation(BlockRotation transformRotation) {
        Box bb = getBoundingBox().offset(getPos().multiply(-1));
        if (transformRotation == BlockRotation.CLOCKWISE_90 || transformRotation == BlockRotation.COUNTERCLOCKWISE_90)
            setBoundingBox(new Box(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX).offset(getPos()));
        return super.applyRotation(transformRotation);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightningBolt) {
    }

    @Override
    public void calculateDimensions() {
    }

    @Override
    public ItemRequirement getRequiredItems() {
        return new ItemRequirement(ItemUseType.DAMAGE, AllItems.SUPER_GLUE);
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    public boolean contains(BlockPos pos) {
        return getBoundingBox().contains(Vec3d.ofCenter(pos));
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    public void spawnParticles() {
        Box bb = getBoundingBox();
        Vec3d origin = new Vec3d(bb.minX, bb.minY, bb.minZ);
        Vec3d extents = new Vec3d(bb.getLengthX(), bb.getLengthY(), bb.getLengthZ());

        if (!(getWorld() instanceof ServerWorld slevel))
            return;

        for (Axis axis : Iterate.axes) {
            AxisDirection positive = AxisDirection.POSITIVE;
            double max = axis.choose(extents.x, extents.y, extents.z);
            Vec3d normal = Vec3d.of(Direction.from(axis, positive).getVector());
            for (Axis axis2 : Iterate.axes) {
                if (axis2 == axis)
                    continue;
                double max2 = axis2.choose(extents.x, extents.y, extents.z);
                Vec3d normal2 = Vec3d.of(Direction.from(axis2, positive).getVector());
                for (Axis axis3 : Iterate.axes) {
                    if (axis3 == axis2 || axis3 == axis)
                        continue;
                    double max3 = axis3.choose(extents.x, extents.y, extents.z);
                    Vec3d normal3 = Vec3d.of(Direction.from(axis3, positive).getVector());

                    for (int i = 0; i <= max * 2; i++) {
                        for (int o1 : Iterate.zeroAndOne) {
                            for (int o2 : Iterate.zeroAndOne) {
                                Vec3d v = origin.add(normal.multiply(i / 2f)).add(normal2.multiply(max2 * o1)).add(normal3.multiply(max3 * o2));

                                slevel.spawnParticles(ParticleTypes.ITEM_SLIME, v.x, v.y, v.z, 1, 0, 0, 0, 0);

                            }
                        }
                    }
                    break;
                }
                break;
            }
        }
    }
}
