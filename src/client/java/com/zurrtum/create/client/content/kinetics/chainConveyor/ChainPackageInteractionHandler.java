package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.infrastructure.packet.c2s.ChainPackageInteractionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ChainPackageInteractionHandler {
    public static boolean onUse(Minecraft mc) {
        LocalPlayer player = mc.player;
        ItemStack stack = player.getMainHandItem();
        if (stack.is(AllItemTags.TOOLS_WRENCH) || stack.is(Items.IRON_CHAIN) || stack.is(AllItems.PACKAGE_FROGPORT)) {
            return false;
        }
        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        for (Map.Entry<Integer, @Nullable ChainConveyorPackagePhysicsData> entry : ChainConveyorClientBehaviour.physicsDataCache.get(mc.level).asMap()
            .entrySet()) {
            ChainConveyorPackagePhysicsData data = entry.getValue();
            if (data == null || data.targetPos == null || data.beReference == null)
                continue;
            AABB bounds = new AABB(data.targetPos, data.targetPos).move(0, -.25, 0).expandTowards(0, 0.5, 0).inflate(0.45);

            Vec3 from = player.getEyePosition();
            Vec3 to = RaycastHelper.getTraceTarget(player, range, from);

            if (bounds.clip(from, to).isEmpty())
                continue;

            ChainConveyorBlockEntity ccbe = data.beReference.get();
            if (ccbe == null || ccbe.isRemoved())
                continue;

            int i = entry.getKey();
            for (ChainConveyorPackage pckg : ccbe.getLoopingPackages()) {
                if (pckg.netId == i) {
                    player.connection.send(new ChainPackageInteractionPacket(ccbe.getBlockPos(), BlockPos.ZERO, pckg.chainPosition, true));
                    return true;
                }
            }

            for (BlockPos connection : ccbe.connections) {
                List<ChainConveyorPackage> list = ccbe.getTravellingPackages().get(connection);
                if (list == null)
                    continue;
                for (ChainConveyorPackage pckg : list) {
                    if (pckg.netId == i) {
                        player.connection.send(new ChainPackageInteractionPacket(ccbe.getBlockPos(), connection, pckg.chainPosition, true));
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
