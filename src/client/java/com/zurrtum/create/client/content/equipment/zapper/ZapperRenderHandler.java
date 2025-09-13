package com.zurrtum.create.client.content.equipment.zapper;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.equipment.zapper.ZapperItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ZapperRenderHandler extends ShootableGadgetRenderHandler {

    public List<LaserBeam> cachedBeams;

    @Override
    protected boolean appliesTo(ItemStack stack) {
        return stack.getItem() instanceof ZapperItem;
    }

    @Override
    public void tick() {
        super.tick();

        if (cachedBeams == null)
            cachedBeams = new LinkedList<>();

        cachedBeams.removeIf(b -> b.itensity < .1f);
        if (cachedBeams.isEmpty())
            return;

        cachedBeams.forEach(beam -> {
            Outliner.getInstance().endChasingLine(beam, beam.start, beam.end, 1 - beam.itensity, false).disableLineNormals().colored(0xffffff)
                .lineWidth(beam.itensity * 1 / 8f);
        });

        cachedBeams.forEach(b -> b.itensity *= .6f);
    }

    @Override
    protected void transformTool(MatrixStack ms, float flip, float equipProgress, float recoil, float pt) {
        ms.translate(flip * -0.1f, 0.1f, -0.4f);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * 5.0F));
    }

    @Override
    protected void transformHand(MatrixStack ms, float flip, float equipProgress, float recoil, float pt) {
    }

    @Override
    public void playSound(Hand hand, Vec3d position) {
        float pitch = hand == Hand.MAIN_HAND ? 0.1f : 0.9f;
        MinecraftClient mc = MinecraftClient.getInstance();
        AllSoundEvents.WORLDSHAPER_PLACE.play(mc.world, mc.player, position, 0.1f, pitch);
    }

    public void addBeam(MinecraftClient mc, LaserBeam beam) {
        ClientWorld world = mc.world;
        Random random = world.random;
        double x = beam.end.x;
        double y = beam.end.y;
        double z = beam.end.z;
        Supplier<Double> randomSpeed = () -> (random.nextDouble() - .5d) * .2f;
        Supplier<Double> randomOffset = () -> (random.nextDouble() - .5d) * .2f;
        for (int i = 0; i < 10; i++) {
            world.addParticleClient(ParticleTypes.END_ROD, x, y, z, randomSpeed.get(), randomSpeed.get(), randomSpeed.get());
            world.addParticleClient(ParticleTypes.FIREWORK, x + randomOffset.get(), y + randomOffset.get(), z + randomOffset.get(), 0, 0, 0);
        }

        cachedBeams.add(beam);
    }

    public static class LaserBeam {
        float itensity;
        Vec3d start;
        Vec3d end;

        public LaserBeam(Vec3d start, Vec3d end) {
            this.start = start;
            this.end = end;
            itensity = 1;
        }
    }

}