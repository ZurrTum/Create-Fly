package com.zurrtum.create.client.content.kinetics;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KineticDebugger {
    public static boolean rainbowDebug = false;

    public static void tick(Minecraft mc) {
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

        Level world = mc.level;
        BlockPos toOutline = be.hasSource() ? be.source : be.getBlockPos();
        BlockState state = be.getBlockState();
        VoxelShape shape = world.getBlockState(toOutline).getBlockSupportShape(world, toOutline);

        if (be.getTheoreticalSpeed() != 0 && !shape.isEmpty())
            Outliner.getInstance().chaseAABB("kineticSource", shape.bounds().move(toOutline)).lineWidth(1 / 16f)
                .colored(be.hasSource() ? Color.generateFromLong(be.network).getRGB() : 0xffcc00);

        if (state.getBlock() instanceof IRotate rotate) {
            Axis axis = rotate.getRotationAxis(state);
            Vec3 vec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis).getUnitVec3i());
            Vec3 center = VecHelper.getCenterOf(be.getBlockPos());
            Outliner.getInstance().showLine("rotationAxis", center.add(vec), center.subtract(vec)).lineWidth(1 / 16f);
        }

    }

    public static boolean isActive() {
        return isF3DebugModeActive() && KineticDebugger.rainbowDebug;
    }

    public static boolean isF3DebugModeActive() {
        return Minecraft.getInstance().getDebugOverlay().showDebugScreen();
    }

    public static KineticBlockEntity getSelectedBE(Minecraft mc) {
        HitResult obj = mc.hitResult;
        if (obj == null)
            return null;
        ClientLevel world = mc.level;
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
