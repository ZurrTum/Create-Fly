package com.zurrtum.create.client.content.contraptions.minecart;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.packet.c2s.CouplingCreationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CouplingHandlerClient {

    static @Nullable AbstractMinecart selectedCart;
    static RandomSource r = RandomSource.create();

    public static void tick(Minecraft mc) {
        if (selectedCart == null)
            return;
        spawnSelectionParticles(selectedCart.getBoundingBox(), false);
        LocalPlayer player = mc.player;
        ItemStack heldItemMainhand = player.getMainHandItem();
        ItemStack heldItemOffhand = player.getOffhandItem();
        if (heldItemMainhand.is(AllItems.MINECART_COUPLING) || heldItemOffhand.is(AllItems.MINECART_COUPLING))
            return;
        selectedCart = null;
    }

    public static void onCartClicked(LocalPlayer player, AbstractMinecart entity) {
        if (Minecraft.getInstance().player != player)
            return;
        if (selectedCart == null || selectedCart == entity) {
            selectedCart = entity;
            spawnSelectionParticles(selectedCart.getBoundingBox(), true);
            return;
        }
        spawnSelectionParticles(entity.getBoundingBox(), true);
        player.connection.send(new CouplingCreationPacket(selectedCart, entity));
        selectedCart = null;
    }

    private static void spawnSelectionParticles(AABB box, boolean highlight) {
        ClientLevel world = Minecraft.getInstance().level;
        Vec3 center = box.getCenter();
        int amount = highlight ? 100 : 2;
        ParticleOptions particleData = highlight ? ParticleTypes.END_ROD : new DustParticleOptions(0xFFFFFF, 1);
        for (int i = 0; i < amount; i++) {
            Vec3 v = VecHelper.offsetRandomly(Vec3.ZERO, r, 1);
            double yOffset = v.y;
            v = v.multiply(1, 0, 1).normalize().add(0, yOffset / 8f, 0).add(center);
            world.addParticle(particleData, v.x, v.y, v.z, 0, 0, 0);
        }
    }

}
