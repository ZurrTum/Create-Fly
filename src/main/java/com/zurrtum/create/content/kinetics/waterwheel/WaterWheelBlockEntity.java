package com.zurrtum.create.content.kinetics.waterwheel;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.WorldEvents;

import java.util.*;

public class WaterWheelBlockEntity extends GeneratingKineticBlockEntity {

    public static final Map<Axis, Set<BlockPos>> SMALL_OFFSETS = new EnumMap<>(Axis.class);
    public static final Map<Axis, Set<BlockPos>> LARGE_OFFSETS = new EnumMap<>(Axis.class);

    static {
        for (Axis axis : Iterate.axes) {
            HashSet<BlockPos> offsets = new HashSet<>();
            for (Direction d : Iterate.directions)
                if (d.getAxis() != axis)
                    offsets.add(BlockPos.ORIGIN.offset(d));
            SMALL_OFFSETS.put(axis, offsets);

            offsets = new HashSet<>();
            for (Direction d : Iterate.directions) {
                if (d.getAxis() == axis)
                    continue;
                BlockPos centralOffset = BlockPos.ORIGIN.offset(d, 2);
                offsets.add(centralOffset);
                for (Direction d2 : Iterate.directions) {
                    if (d2.getAxis() == axis)
                        continue;
                    if (d2.getAxis() == d.getAxis())
                        continue;
                    offsets.add(centralOffset.offset(d2));
                }
            }
            LARGE_OFFSETS.put(axis, offsets);
        }
    }

    public int flowScore;
    public BlockState material;

    public WaterWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        material = Blocks.SPRUCE_PLANKS.getDefaultState();
        setLazyTickRate(60);
    }

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        this(AllBlockEntityTypes.WATER_WHEEL, pos, state);
    }

    protected int getSize() {
        return 1;
    }

    protected Set<BlockPos> getOffsetsToCheck() {
        return (getSize() == 1 ? SMALL_OFFSETS : LARGE_OFFSETS).get(getAxis());
    }

    public ActionResult applyMaterialIfValid(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        BlockState material = blockItem.getBlock().getDefaultState();
        if (material == this.material)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!material.isIn(BlockTags.PLANKS))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (world.isClient() && !isVirtual())
            return ActionResult.SUCCESS;
        this.material = material;
        notifyUpdate();
        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(material));
        return ActionResult.SUCCESS;
    }

    protected Axis getAxis() {
        Axis axis = Axis.X;
        BlockState blockState = getCachedState();
        if (blockState.getBlock() instanceof IRotate irotate)
            axis = irotate.getRotationAxis(blockState);
        return axis;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        // Water can change flow direction without notifying neighbours
        determineAndApplyFlowScore();
    }

    public void determineAndApplyFlowScore() {
        Vec3d wheelPlane = Vec3d.of(new Vec3i(1, 1, 1).subtract(Direction.get(AxisDirection.POSITIVE, getAxis()).getVector()));

        int flowScore = 0;
        boolean lava = false;
        for (BlockPos blockPos : getOffsetsToCheck()) {
            BlockPos targetPos = blockPos.add(pos);
            Vec3d flowAtPos = getFlowVectorAtPosition(targetPos).multiply(wheelPlane);
            lava |= FluidHelper.isLava(world.getFluidState(targetPos).getFluid());

            if (flowAtPos.lengthSquared() == 0)
                continue;

            flowAtPos = flowAtPos.normalize();
            Vec3d normal = Vec3d.of(blockPos).normalize();

            Vec3d positiveMotion = VecHelper.rotate(normal, 90, getAxis());
            double dot = flowAtPos.dotProduct(positiveMotion);
            if (Math.abs(dot) > .5)
                flowScore += Math.signum(dot);
        }

        if (flowScore != 0 && !world.isClient())
            award(lava ? AllAdvancements.LAVA_WHEEL : AllAdvancements.WATER_WHEEL);

        setFlowScoreAndUpdate(flowScore);
    }

    public Vec3d getFlowVectorAtPosition(BlockPos pos) {
        FluidState fluid = world.getFluidState(pos);
        Vec3d vec = fluid.getVelocity(world, pos);
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() == Blocks.BUBBLE_COLUMN)
            vec = new Vec3d(0, blockState.get(BubbleColumnBlock.DRAG) ? -1 : 1, 0);
        return vec;
    }

    public void setFlowScoreAndUpdate(int score) {
        if (flowScore == score)
            return;
        flowScore = score;
        updateGeneratedRotation();
        markDirty();
    }

    private void redraw() {
        if (!isVirtual())
            AllClientHandle.INSTANCE.queueUpdate(this);
        if (hasWorld()) {
            world.updateListeners(getPos(), getCachedState(), getCachedState(), 16);
            world.getChunkManager().getLightingProvider().checkBlock(pos);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.LAVA_WHEEL, AllAdvancements.WATER_WHEEL);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        flowScore = view.getInt("FlowScore", 0);

        BlockState prevMaterial = this.material;
        Optional<BlockState> material = view.read("Material", BlockState.CODEC);
        if (material.isEmpty())
            return;

        this.material = material.get();
        if (this.material.isAir())
            this.material = Blocks.SPRUCE_PLANKS.getDefaultState();

        if (clientPacket && prevMaterial != this.material)
            redraw();
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        view.put("Material", BlockState.CODEC, material);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("FlowScore", flowScore);
        view.put("Material", BlockState.CODEC, material);
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(pos).expand(getSize());
    }

    @Override
    public float getGeneratedSpeed() {
        return MathHelper.clamp(flowScore, -1, 1) * 8 / getSize();
    }

}
