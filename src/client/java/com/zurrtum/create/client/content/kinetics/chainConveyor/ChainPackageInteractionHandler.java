package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.infrastructure.packet.c2s.ChainPackageInteractionPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;

public class ChainPackageInteractionHandler {
    public static boolean onUse(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        ItemStack stack = player.getMainHandStack();
        if (stack.isIn(AllItemTags.TOOLS_WRENCH) || stack.isOf(Items.CHAIN) || stack.isOf(AllItems.PACKAGE_FROGPORT)) {
            return false;
        }
        double range = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 1;
        for (Map.Entry<Integer, ChainConveyorPackagePhysicsData> entry : ChainConveyorClientBehaviour.physicsDataCache.get(mc.world).asMap()
            .entrySet()) {
            ChainConveyorPackagePhysicsData data = entry.getValue();
            if (data == null || data.targetPos == null || data.beReference == null)
                continue;
            Box bounds = new Box(data.targetPos, data.targetPos).offset(0, -.25, 0).stretch(0, 0.5, 0).expand(0.45);

            Vec3d from = player.getEyePos();
            Vec3d to = RaycastHelper.getTraceTarget(player, range, from);

            if (bounds.raycast(from, to).isEmpty())
                continue;

            ChainConveyorBlockEntity ccbe = data.beReference.get();
            if (ccbe == null || ccbe.isRemoved())
                continue;

            int i = entry.getKey();
            for (ChainConveyorPackage pckg : ccbe.getLoopingPackages()) {
                if (pckg.netId == i) {
                    player.networkHandler.sendPacket(new ChainPackageInteractionPacket(ccbe.getPos(), BlockPos.ORIGIN, pckg.chainPosition, true));
                    return true;
                }
            }

            for (BlockPos connection : ccbe.connections) {
                List<ChainConveyorPackage> list = ccbe.getTravellingPackages().get(connection);
                if (list == null)
                    continue;
                for (ChainConveyorPackage pckg : list) {
                    if (pckg.netId == i) {
                        player.networkHandler.sendPacket(new ChainPackageInteractionPacket(ccbe.getPos(), connection, pckg.chainPosition, true));
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
