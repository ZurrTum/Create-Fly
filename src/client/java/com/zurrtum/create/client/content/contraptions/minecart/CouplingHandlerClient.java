package com.zurrtum.create.client.content.contraptions.minecart;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.packet.c2s.CouplingCreationPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class CouplingHandlerClient {

    static AbstractMinecartEntity selectedCart;
    static Random r = Random.create();

    public static void tick(MinecraftClient mc) {
        if (selectedCart == null)
            return;
        spawnSelectionParticles(selectedCart.getBoundingBox(), false);
        ClientPlayerEntity player = mc.player;
        ItemStack heldItemMainhand = player.getMainHandStack();
        ItemStack heldItemOffhand = player.getOffHandStack();
        if (heldItemMainhand.isOf(AllItems.MINECART_COUPLING) || heldItemOffhand.isOf(AllItems.MINECART_COUPLING))
            return;
        selectedCart = null;
    }

    public static void onCartClicked(ClientPlayerEntity player, AbstractMinecartEntity entity) {
        if (MinecraftClient.getInstance().player != player)
            return;
        if (selectedCart == null || selectedCart == entity) {
            selectedCart = entity;
            spawnSelectionParticles(selectedCart.getBoundingBox(), true);
            return;
        }
        spawnSelectionParticles(entity.getBoundingBox(), true);
        player.networkHandler.sendPacket(new CouplingCreationPacket(selectedCart, entity));
        selectedCart = null;
    }

    private static void spawnSelectionParticles(Box box, boolean highlight) {
        ClientWorld world = MinecraftClient.getInstance().world;
        Vec3d center = box.getCenter();
        int amount = highlight ? 100 : 2;
        ParticleEffect particleData = highlight ? ParticleTypes.END_ROD : new DustParticleEffect(0xFFFFFF, 1);
        for (int i = 0; i < amount; i++) {
            Vec3d v = VecHelper.offsetRandomly(Vec3d.ZERO, r, 1);
            double yOffset = v.y;
            v = v.multiply(1, 0, 1).normalize().add(0, yOffset / 8f, 0).add(center);
            world.addParticleClient(particleData, v.x, v.y, v.z, 0, 0, 0);
        }
    }

}
