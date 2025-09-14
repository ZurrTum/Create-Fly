package com.zurrtum.create.client.content.logistics.packagePort;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.PackagePortPlacementPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PackagePortTargetSelectionHandler {

    public static PackagePortTarget activePackageTarget;
    public static Vec3d exactPositionOfTarget;
    public static boolean isPostbox;

    public static void flushSettings(ClientPlayerEntity player, BlockPos pos) {
        if (activePackageTarget == null) {
            CreateLang.translate("gui.package_port.not_targeting_anything").sendStatus(player);
            return;
        }

        if (validateDiff(exactPositionOfTarget, pos) == null) {
            activePackageTarget.relativePos = activePackageTarget.relativePos.subtract(pos);
            player.networkHandler.sendPacket(new PackagePortPlacementPacket(activePackageTarget, pos));
        }

        activePackageTarget = null;
        isPostbox = false;
    }

    public static boolean onUse(MinecraftClient mc) {
        HitResult hitResult = mc.crosshairTarget;

        if (hitResult == null || hitResult.getType() == Type.MISS)
            return false;
        if (!(hitResult instanceof BlockHitResult bhr))
            return false;

        BlockPos pos = bhr.getBlockPos();
        if (!(mc.world.getBlockEntity(pos) instanceof StationBlockEntity sbe))
            return false;
        if (sbe.edgePoint == null)
            return false;
        ItemStack mainHandItem = mc.player.getMainHandStack();
        if (!mainHandItem.isIn(AllItemTags.POSTBOXES))
            return false;

        PackagePortTargetSelectionHandler.exactPositionOfTarget = Vec3d.ofCenter(pos);
        PackagePortTargetSelectionHandler.activePackageTarget = new PackagePortTarget.TrainStationFrogportTarget(pos);
        PackagePortTargetSelectionHandler.isPostbox = true;
        return true;
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        ItemStack stack = player.getMainHandStack();
        boolean isPostbox = stack.isIn(AllItemTags.POSTBOXES);
        boolean isWrench = stack.isIn(AllItemTags.TOOLS_WRENCH);

        if (!isWrench) {
            if (activePackageTarget == null)
                return;
            if (!stack.isOf(AllItems.PACKAGE_FROGPORT) && !isPostbox)
                return;
        }

        HitResult objectMouseOver = mc.crosshairTarget;
        if (!(objectMouseOver instanceof BlockHitResult blockRayTraceResult))
            return;

        if (isWrench) {
            if (blockRayTraceResult.getType() == Type.MISS)
                return;
            BlockPos pos = blockRayTraceResult.getBlockPos();
            if (!(mc.world.getBlockEntity(pos) instanceof PackagePortBlockEntity ppbe))
                return;
            if (ppbe.target == null)
                return;
            Vec3d source = Vec3d.ofBottomCenter(pos);
            Vec3d target = ppbe.target.getExactTargetLocation(ppbe, mc.world, pos);
            if (target == Vec3d.ZERO)
                return;
            Color color = new Color(0x9ede73);
            animateConnection(mc, source, target, color);
            Outliner.getInstance().chaseAABB("ChainPointSelected", new Box(target, target)).colored(color).lineWidth(1 / 5f).disableLineNormals();
            return;
        }

        Vec3d target = exactPositionOfTarget;
        if (blockRayTraceResult.getType() == Type.MISS) {
            Outliner.getInstance().chaseAABB("ChainPointSelected", new Box(target, target)).colored(0x9ede73).lineWidth(1 / 5f).disableLineNormals();
            return;
        }

        BlockPos pos = blockRayTraceResult.getBlockPos();
        if (!mc.world.getBlockState(pos).isReplaceable())
            pos = pos.offset(blockRayTraceResult.getSide());

        String validateDiff = validateDiff(target, pos);
        boolean valid = validateDiff == null;
        Color color = new Color(valid ? 0x9ede73 : 0xff7171);
        Vec3d source = Vec3d.ofBottomCenter(pos);

        CreateLang.translate(validateDiff != null ? validateDiff : "package_port.valid").color(color.getRGB()).sendStatus(player);

        Outliner.getInstance().chaseAABB("ChainPointSelected", new Box(target, target)).colored(color).lineWidth(1 / 5f).disableLineNormals();

        if (!mc.world.getBlockState(pos).isReplaceable())
            return;

        Outliner.getInstance().chaseAABB("TargetedFrogPos", new Box(pos).shrink(0, 1, 0).contract(0.125, 0, 0.125)).colored(color).lineWidth(1 / 16f)
            .disableLineNormals();

        animateConnection(mc, source, target, color);

    }

    public static void animateConnection(MinecraftClient mc, Vec3d source, Vec3d target, Color color) {
        DustParticleEffect data = new DustParticleEffect(color.getRGB(), 1);
        ClientWorld world = mc.world;
        double totalFlyingTicks = 10;
        int segments = (((int) totalFlyingTicks) / 3) + 1;
        double tickOffset = totalFlyingTicks / segments;

        for (int i = 0; i < segments; i++) {
            double ticks = ((AnimationTickHolder.getRenderTime() / 3) % tickOffset) + i * tickOffset;
            Vec3d vec = source.lerp(target, ticks / totalFlyingTicks);
            world.addParticleClient(data, vec.x, vec.y, vec.z, 0, 0, 0);
        }

    }

    public static String validateDiff(Vec3d target, BlockPos placedPos) {
        Vec3d source = Vec3d.ofBottomCenter(placedPos);
        Vec3d diff = target.subtract(source);
        if (diff.y < 0 && !isPostbox)
            return "package_port.cannot_reach_down";
        if (diff.length() > AllConfigs.server().logistics.packagePortRange.get())
            return "package_port.too_far";
        return null;
    }

}
