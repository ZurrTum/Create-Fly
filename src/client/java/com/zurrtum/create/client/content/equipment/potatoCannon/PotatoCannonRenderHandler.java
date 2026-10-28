package com.zurrtum.create.client.content.equipment.potatoCannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.equipment.zapper.ShootableGadgetRenderHandler;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.Vec3;

public class PotatoCannonRenderHandler extends ShootableGadgetRenderHandler {

    private float nextPitch;

    @Override
    public void playSound(InteractionHand hand, Vec3 position) {
        PotatoProjectileEntity.playLaunchSound(Minecraft.getInstance().level, position, nextPitch);
    }

    @Override
    protected boolean appliesTo(ItemStack stack) {
        return stack.getItem() instanceof PotatoCannonItem;
    }

    public void beforeShoot(float nextPitch, Vec3 location, Vec3 motion, ItemStack stack) {
        this.nextPitch = nextPitch;
        if (stack.isEmpty()) {
            return;
        }
        ClientLevel world = Minecraft.getInstance().level;
        for (int i = 0; i < 2; i++) {
            Vec3 m = VecHelper.offsetRandomly(motion.scale(0.1f), world.getRandom(), 0.025f);
            world.addParticle(
                new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)),
                location.x,
                location.y,
                location.z,
                m.x,
                m.y,
                m.z
            );

            Vec3 m2 = VecHelper.offsetRandomly(motion.scale(2.0f), world.getRandom(), 0.5f);
            world.addParticle(new AirParticleData(1, 1 / 4.0f), location.x, location.y, location.z, m2.x, m2.y, m2.z);
        }
    }

    @Override
    protected void transformTool(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {
        Pose pose = ms.last();
        pose.translate(flip * -0.1f, 0, 0.14f);
        pose.scale(0.75f, 0.75f, 0.75f);
        pose.rotate(Axis.XP, recoil * Radians.DEG_80);
    }

    @Override
    protected void transformHand(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {
        Pose pose = ms.last();
        pose.translate(flip * -0.09f, -0.275f, -0.25f);
        pose.rotate(Axis.ZP, flip * Radians.DEG_350);
    }

}
