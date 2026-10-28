package com.zurrtum.create.client.content.equipment.symmetryWand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.CrossPlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.PlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.TriplePlaneMirror;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SymmetryHandlerClient {
    private static int tickCounter;

    public static void onRenderWorld(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 cameraPos) {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        Inventory inventory = player.getInventory();
        for (int i = 0, size = Inventory.getSelectionSize(); i < size; i++) {
            ItemStack stackInSlot = inventory.getItem(i);
            if (!stackInSlot.is(AllItems.WAND_OF_SYMMETRY)) {
                continue;
            }
            if (!SymmetryWandItem.isEnabled(stackInSlot)) {
                continue;
            }
            SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
            PartialModel model = getModel(mirror);
            if (model == null) {
                continue;
            }

            BlockPos pos = BlockPos.containing(mirror.getPosition());

            double speed = 1 / 16.0d;
            float yShift = Mth.sin((float) (AnimationTickHolder.getRenderTime() * speed)) / 5.0f;

            ms.pushPose();
            Pose pose = ms.last();
            pose.translate(
                (float) (pos.getX() - cameraPos.x()),
                (float) (pos.getY() - cameraPos.y() + yShift + 0.2f),
                (float) (pos.getZ() - cameraPos.z())
            );
            applyModelTransform(mirror, ms);
            int light = SmartBlockEntityRenderer.getLightCoords(level, pos);
            CachedBuffers.partial(model, Blocks.AIR.defaultBlockState()).light(light).submit(ms, queue);
            ms.popPose();
        }
    }

    @Nullable
    public static PartialModel getModel(SymmetryMirror mirror) {
        if (mirror instanceof PlaneMirror) {
            return AllPartialModels.SYMMETRY_PLANE;
        }
        if (mirror instanceof CrossPlaneMirror) {
            return AllPartialModels.SYMMETRY_CROSSPLANE;
        }
        if (mirror instanceof TriplePlaneMirror) {
            return AllPartialModels.SYMMETRY_TRIPLEPLANE;
        }
        return null;
    }

    public static void applyModelTransform(SymmetryMirror mirror, PoseStack ms) {
        if (mirror instanceof PlaneMirror) {
            if (mirror.orientation != PlaneMirror.Align.XY) {
                ms.rotateYCentered(Radians.DEG_90);
            }
        } else if (mirror instanceof CrossPlaneMirror) {
            if (mirror.orientation != CrossPlaneMirror.Align.Y) {
                ms.rotateYCentered(Radians.DEG_45);
            }
        }
    }

    public static void onClientTick(Minecraft mc) {
        ClientLevel world = mc.level;
        if (world == null) {
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        LocalPlayer player = mc.player;
        tickCounter++;

        if (tickCounter % 10 == 0) {
            Inventory inventory = player.getInventory();
            for (int i = 0, size = Inventory.getSelectionSize(); i < size; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (stackInSlot.is(AllItems.WAND_OF_SYMMETRY) && SymmetryWandItem.isEnabled(stackInSlot)) {
                    SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
                    if (mirror instanceof EmptyMirror) {
                        continue;
                    }

                    RandomSource random = mc.level.getRandom();
                    double offsetX = (random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.3;

                    Vec3 pos = mirror.getPosition().add(0.5 + offsetX, 1 / 4.0d, 0.5 + offsetZ);
                    Vec3 speed = new Vec3(0, random.nextDouble() * 1 / 8.0f, 0);
                    world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
                }
            }
        }
    }

    public static void drawEffect(Minecraft client, BlockPos from, BlockPos to) {
        RandomSource random = client.level.getRandom();
        double density = 0.8f;
        Vec3 start = Vec3.atLowerCornerOf(from).add(0.5, 0.5, 0.5);
        Vec3 end = Vec3.atLowerCornerOf(to).add(0.5, 0.5, 0.5);
        Vec3 diff = end.subtract(start);

        Vec3 step = diff.normalize().scale(density);
        int steps = (int) (diff.length() / step.length());

        ClientLevel world = client.level;
        for (int i = 3; i < steps - 1; i++) {
            Vec3 pos = start.add(step.scale(i));
            Vec3 speed = new Vec3(0, random.nextDouble() * -40.0f, 0);

            world.addParticle(new DustParticleOptions(0x010101, 1), pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
        }

        Vec3 speed = new Vec3(0, random.nextDouble() * 1 / 32.0f, 0);
        Vec3 pos = start.add(step.scale(2));
        world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);

        speed = new Vec3(0, random.nextDouble() * 1 / 32.0f, 0);
        pos = start.add(step.scale(steps));
        world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
    }
}
