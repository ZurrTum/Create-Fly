package com.zurrtum.create.client.content.equipment.symmetryWand;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.CrossPlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.PlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.TriplePlaneMirror;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SymmetryHandlerClient {
    private static int tickCounter = 0;

    public static void onRenderWorld(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider buffer, Vec3d cameraPos) {
        ClientPlayerEntity player = mc.player;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0, size = PlayerInventory.getHotbarSize(); i < size; i++) {
            ItemStack stackInSlot = inventory.getStack(i);
            if (!stackInSlot.isOf(AllItems.WAND_OF_SYMMETRY))
                continue;
            if (!SymmetryWandItem.isEnabled(stackInSlot))
                continue;
            SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
            if (mirror instanceof EmptyMirror)
                continue;

            BlockPos pos = BlockPos.ofFloored(mirror.getPosition());

            double speed = 1 / 16d;
            float yShift = MathHelper.sin((float) (AnimationTickHolder.getRenderTime() * speed)) / 5f;

            ms.push();
            ms.translate(pos.getX() - cameraPos.getX(), pos.getY() - cameraPos.getY(), pos.getZ() - cameraPos.getZ());
            ms.translate(0, yShift + .2f, 0);
            applyModelTransform(mirror, ms);
            GeometryBakedModel model = getModel(mirror).get();
            VertexConsumer builder = buffer.getBuffer(RenderLayer.getSolid());

            mc.getBlockRenderManager().getModelRenderer()
                .render(mc.world, List.of(model), Blocks.AIR.getDefaultState(), pos, ms, builder, true, OverlayTexture.DEFAULT_UV);

            ms.pop();
        }
    }

    @Nullable
    public static PartialModel getModel(SymmetryMirror mirror) {
        return switch (mirror) {
            case PlaneMirror planeMirror -> AllPartialModels.SYMMETRY_PLANE;
            case CrossPlaneMirror crossPlaneMirror -> AllPartialModels.SYMMETRY_CROSSPLANE;
            case TriplePlaneMirror triplePlaneMirror -> AllPartialModels.SYMMETRY_TRIPLEPLANE;
            default -> throw new IllegalArgumentException("Unknown mirror type: " + mirror.getClass().getName());
        };
    }

    public static void applyModelTransform(SymmetryMirror mirror, MatrixStack ms) {
        if (mirror instanceof PlaneMirror) {
            if (mirror.orientation != PlaneMirror.Align.XY) {
                TransformStack.of(ms).center().rotateYDegrees(90).uncenter();
            }
        } else if (mirror instanceof CrossPlaneMirror) {
            if (mirror.orientation != CrossPlaneMirror.Align.Y) {
                TransformStack.of(ms).center().rotateYDegrees(45).uncenter();
            }
        }
    }

    public static void onClientTick(MinecraftClient mc) {
        ClientWorld world = mc.world;
        if (world == null)
            return;
        if (mc.isPaused())
            return;

        ClientPlayerEntity player = mc.player;
        tickCounter++;

        if (tickCounter % 10 == 0) {
            PlayerInventory inventory = player.getInventory();
            for (int i = 0, size = PlayerInventory.getHotbarSize(); i < size; i++) {
                ItemStack stackInSlot = inventory.getStack(i);
                if (stackInSlot.isOf(AllItems.WAND_OF_SYMMETRY) && SymmetryWandItem.isEnabled(stackInSlot)) {
                    SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
                    if (mirror instanceof EmptyMirror)
                        continue;

                    Random random = mc.world.random;
                    double offsetX = (random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.3;

                    Vec3d pos = mirror.getPosition().add(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
                    Vec3d speed = new Vec3d(0, random.nextDouble() * 1 / 8f, 0);
                    world.addParticleClient(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
                }
            }
        }
    }

    public static void drawEffect(MinecraftClient client, BlockPos from, BlockPos to) {
        Random random = client.world.random;
        double density = 0.8f;
        Vec3d start = Vec3d.of(from).add(0.5, 0.5, 0.5);
        Vec3d end = Vec3d.of(to).add(0.5, 0.5, 0.5);
        Vec3d diff = end.subtract(start);

        Vec3d step = diff.normalize().multiply(density);
        int steps = (int) (diff.length() / step.length());

        ClientWorld world = client.world;
        for (int i = 3; i < steps - 1; i++) {
            Vec3d pos = start.add(step.multiply(i));
            Vec3d speed = new Vec3d(0, random.nextDouble() * -40f, 0);

            world.addParticleClient(new DustParticleEffect(0x010101, 1), pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
        }

        Vec3d speed = new Vec3d(0, random.nextDouble() * 1 / 32f, 0);
        Vec3d pos = start.add(step.multiply(2));
        world.addParticleClient(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);

        speed = new Vec3d(0, random.nextDouble() * 1 / 32f, 0);
        pos = start.add(step.multiply(steps));
        world.addParticleClient(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
    }
}
