package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.google.common.cache.Cache;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import com.zurrtum.create.infrastructure.packet.c2s.ChainConveyorConnectionPacket;
import com.zurrtum.create.infrastructure.packet.c2s.ChainPackageInteractionPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;

public class ChainConveyorInteractionHandler {

    public static WorldAttached<Cache<BlockPos, List<ChainConveyorShape>>> loadedChains = new WorldAttached<>($ -> new TickBasedCache<>(60, true));

    public static BlockPos selectedLift;
    public static float selectedChainPosition;
    public static BlockPos selectedConnection;
    public static Vec3d selectedBakedPosition;
    public static ChainConveyorShape selectedShape;

    public static void clientTick(MinecraftClient mc) {
        if (!isActive(mc)) {
            selectedLift = null;
            return;
        }

        ClientPlayerEntity player = mc.player;
        boolean isWrench = player.isHolding(i -> i.isIn(AllItemTags.TOOLS_WRENCH));
        boolean dismantling = isWrench && player.isSneaking();
        double range = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 1;

        Vec3d from = player.getEyePos();
        Vec3d to = RaycastHelper.getTraceTarget(player, range, from);
        HitResult hitResult = mc.crosshairTarget;

        double bestDiff = Float.MAX_VALUE;
        if (hitResult != null)
            bestDiff = hitResult.getPos().squaredDistanceTo(from);

        BlockPos bestLift = null;
        ChainConveyorShape bestShape = null;
        selectedConnection = null;

        for (Map.Entry<BlockPos, List<ChainConveyorShape>> entry : loadedChains.get(mc.world).asMap().entrySet()) {
            BlockPos liftPos = entry.getKey();
            for (ChainConveyorShape chainConveyorShape : entry.getValue()) {
                if (chainConveyorShape instanceof ChainConveyorShape.ChainConveyorBB && dismantling)
                    continue;
                Vec3d liftVec = Vec3d.of(liftPos);
                Vec3d intersect = chainConveyorShape.intersect(from.subtract(liftVec), to.subtract(liftVec));
                if (intersect == null)
                    continue;

                double distanceToSqr = intersect.add(liftVec).squaredDistanceTo(from);
                if (distanceToSqr > bestDiff)
                    continue;
                bestDiff = distanceToSqr;
                bestLift = liftPos;
                bestShape = chainConveyorShape;
                selectedChainPosition = chainConveyorShape.getChainPosition(intersect);
                if (chainConveyorShape instanceof ChainConveyorShape.ChainConveyorOBB obb)
                    selectedConnection = obb.connection;
            }
        }

        selectedLift = bestLift;
        if (bestLift == null)
            return;

        selectedShape = bestShape;
        selectedBakedPosition = bestShape.getVec(bestLift, selectedChainPosition);

        if (!isWrench) {
            Outliner.getInstance().chaseAABB("ChainPointSelection", new Box(selectedBakedPosition, selectedBakedPosition)).colored(Color.WHITE)
                .lineWidth(1 / 6f).disableLineNormals();
        }
    }

    private static boolean isActive(MinecraftClient mc) {
        ItemStack mainHandItem = mc.player.getMainHandStack();
        return mc.player.isHolding(i -> i.isIn(AllItemTags.CHAIN_RIDEABLE)) || mainHandItem.isOf(AllItems.PACKAGE_FROGPORT) || PackageItem.isPackage(
            mainHandItem);
    }

    public static boolean onUse(MinecraftClient mc) {
        if (selectedLift == null)
            return false;

        ClientPlayerEntity player = mc.player;
        ItemStack mainHandItem = player.getMainHandStack();

        if (player.isHolding(i -> i.isIn(AllItemTags.CHAIN_RIDEABLE))) {
            ItemStack usedItem = mainHandItem.isIn(AllItemTags.CHAIN_RIDEABLE) ? mainHandItem : player.getOffHandStack();
            if (!player.isSneaking()) {
                ChainConveyorRidingHandler.embark(mc, selectedLift, selectedChainPosition, selectedConnection);
                return true;
            }

            player.networkHandler.sendPacket(new ChainConveyorConnectionPacket(selectedLift, selectedLift.add(selectedConnection), usedItem, false));
            return true;
        }

        if (mainHandItem.isOf(AllItems.PACKAGE_FROGPORT)) {
            PackagePortTargetSelectionHandler.exactPositionOfTarget = selectedBakedPosition;
            PackagePortTargetSelectionHandler.activePackageTarget = new PackagePortTarget.ChainConveyorFrogportTarget(
                selectedLift,
                selectedChainPosition,
                selectedConnection,
                false
            );
            return true;
        }

        if (PackageItem.isPackage(mainHandItem)) {
            player.networkHandler.sendPacket(new ChainPackageInteractionPacket(
                selectedLift,
                selectedConnection == null ? BlockPos.ORIGIN : selectedConnection,
                selectedChainPosition,
                false
            ));
            return true;
        }

        return true;
    }

    public static void drawCustomBlockSelection(MatrixStack ms, VertexConsumerProvider buffer, Vec3d camera) {
        if (selectedLift == null || selectedShape == null)
            return;

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getLines());
        ms.push();
        ms.translate(selectedLift.getX() - camera.x, selectedLift.getY() - camera.y, selectedLift.getZ() - camera.z);
        selectedShape.drawOutline(selectedLift, ms, vb);
        ms.pop();
    }

    public static boolean hideVanillaBlockSelection() {
        return selectedLift != null && selectedShape != null;
    }

}
