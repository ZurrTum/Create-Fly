package com.zurrtum.create.client.content.kinetics;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class KineticDebugger {
    public static boolean rainbowDebug = false;

    public static void tick(MinecraftClient mc) {
        if (!isActive()) {
            if (KineticBlockEntityRenderer.rainbowMode) {
                KineticBlockEntityRenderer.rainbowMode = false;
                SuperByteBufferCache.getInstance().invalidate();
            }
            return;
        }

        KineticBlockEntity be = getSelectedBE(mc);
        if (be == null)
            return;

        World world = mc.world;
        BlockPos toOutline = be.hasSource() ? be.source : be.getPos();
        BlockState state = be.getCachedState();
        VoxelShape shape = world.getBlockState(toOutline).getSidesShape(world, toOutline);

        if (be.getTheoreticalSpeed() != 0 && !shape.isEmpty())
            Outliner.getInstance().chaseAABB("kineticSource", shape.getBoundingBox().offset(toOutline)).lineWidth(1 / 16f)
                .colored(be.hasSource() ? Color.generateFromLong(be.network).getRGB() : 0xffcc00);

        if (state.getBlock() instanceof IRotate rotate) {
            Axis axis = rotate.getRotationAxis(state);
            Vec3d vec = Vec3d.of(Direction.get(AxisDirection.POSITIVE, axis).getVector());
            Vec3d center = VecHelper.getCenterOf(be.getPos());
            Outliner.getInstance().showLine("rotationAxis", center.add(vec), center.subtract(vec)).lineWidth(1 / 16f);
        }

    }

    public static boolean isActive() {
        return isF3DebugModeActive() && KineticDebugger.rainbowDebug;
    }

    public static boolean isF3DebugModeActive() {
        return MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud();
    }

    public static KineticBlockEntity getSelectedBE(MinecraftClient mc) {
        HitResult obj = mc.crosshairTarget;
        if (obj == null)
            return null;
        ClientWorld world = mc.world;
        if (world == null)
            return null;
        if (!(obj instanceof BlockHitResult ray))
            return null;

        BlockEntity be = world.getBlockEntity(ray.getBlockPos());
        if (!(be instanceof KineticBlockEntity))
            return null;

        return (KineticBlockEntity) be;
    }

}
