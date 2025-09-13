package com.zurrtum.create.client.content.contraptions.glue;

import com.google.common.base.Objects;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.content.contraptions.chassis.AbstractChassisBlock;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.content.contraptions.glue.SuperGlueItem;
import com.zurrtum.create.content.contraptions.glue.SuperGlueSelectionHelper;
import com.zurrtum.create.infrastructure.packet.c2s.SuperGlueRemovalPacket;
import com.zurrtum.create.infrastructure.packet.c2s.SuperGlueSelectionPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SuperGlueSelectionHandler {

    private static final int PASSIVE = 0x4D9162;
    private static final int HIGHLIGHT = 0x68c586;
    private static final int FAIL = 0xc5b548;

    private Object clusterOutlineSlot = new Object();
    private Object bbOutlineSlot = new Object();
    private int clusterCooldown;

    private BlockPos firstPos;
    private BlockPos hoveredPos;
    private Set<BlockPos> currentCluster;
    private int glueRequired;

    private SuperGlueEntity selected;
    private BlockPos soundSourceForRemoval;

    public void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        BlockPos hovered = null;
        ItemStack stack = player.getMainHandStack();

        if (!isGlue(stack)) {
            if (firstPos != null)
                discard(player);
            return;
        }

        if (clusterCooldown > 0) {
            if (clusterCooldown == 25)
                player.sendMessage(ScreenTexts.EMPTY, true);
            Outliner.getInstance().keep(clusterOutlineSlot);
            clusterCooldown--;
        }

        Box scanArea = player.getBoundingBox().expand(32, 16, 32);

        List<SuperGlueEntity> glueNearby = mc.world.getNonSpectatingEntities(SuperGlueEntity.class, scanArea);

        selected = null;
        if (firstPos == null) {
            double range = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 1;
            Vec3d traceOrigin = RaycastHelper.getTraceOrigin(player);
            Vec3d traceTarget = RaycastHelper.getTraceTarget(player, range, traceOrigin);

            double bestDistance = Double.MAX_VALUE;
            for (SuperGlueEntity glueEntity : glueNearby) {
                Optional<Vec3d> clip = glueEntity.getBoundingBox().raycast(traceOrigin, traceTarget);
                if (clip.isEmpty())
                    continue;
                Vec3d vec3 = clip.get();
                double distanceToSqr = vec3.squaredDistanceTo(traceOrigin);
                if (distanceToSqr > bestDistance)
                    continue;
                selected = glueEntity;
                soundSourceForRemoval = BlockPos.ofFloored(vec3);
                bestDistance = distanceToSqr;
            }

            for (SuperGlueEntity glueEntity : glueNearby) {
                boolean h = clusterCooldown == 0 && glueEntity == selected;
                AllSpecialTextures faceTex = h ? AllSpecialTextures.GLUE : null;
                Outliner.getInstance().showAABB(glueEntity, glueEntity.getBoundingBox()).colored(h ? HIGHLIGHT : PASSIVE)
                    .withFaceTextures(faceTex, faceTex).disableLineNormals().lineWidth(h ? 1 / 16f : 1 / 64f);
            }
        }

        HitResult hitResult = mc.crosshairTarget;
        if (hitResult != null && hitResult.getType() == Type.BLOCK)
            hovered = ((BlockHitResult) hitResult).getBlockPos();

        if (hovered == null) {
            hoveredPos = null;
            return;
        }

        if (firstPos != null && !firstPos.isWithinDistance(hovered, 24)) {
            CreateLang.translate("super_glue.too_far").color(FAIL).sendStatus(player);
            return;
        }

        boolean cancel = player.isSneaking();
        if (cancel && firstPos == null)
            return;

        Box currentSelectionBox = getCurrentSelectionBox();

        boolean unchanged = Objects.equal(hovered, hoveredPos);

        if (unchanged) {
            if (currentCluster != null) {
                boolean canReach = currentCluster.contains(hovered);
                boolean canAfford = SuperGlueSelectionHelper.collectGlueFromInventory(player, glueRequired, true);
                int color = HIGHLIGHT;
                String key = "super_glue.click_to_confirm";

                if (!canReach) {
                    color = FAIL;
                    key = "super_glue.cannot_reach";
                } else if (!canAfford) {
                    color = FAIL;
                    key = "super_glue.not_enough";
                } else if (cancel) {
                    color = FAIL;
                    key = "super_glue.click_to_discard";
                }

                CreateLang.translate(key).color(color).sendStatus(player);

                if (currentSelectionBox != null)
                    Outliner.getInstance().showAABB(bbOutlineSlot, currentSelectionBox).colored(canReach && canAfford && !cancel ? HIGHLIGHT : FAIL)
                        .withFaceTextures(AllSpecialTextures.GLUE, AllSpecialTextures.GLUE).disableLineNormals().lineWidth(1 / 16f);

                Outliner.getInstance().showCluster(clusterOutlineSlot, currentCluster).colored(0x4D9162).disableLineNormals().lineWidth(1 / 64f);
            }

            return;
        }

        hoveredPos = hovered;

        Set<BlockPos> cluster = SuperGlueSelectionHelper.searchGlueGroup(mc.world, firstPos, hoveredPos, true);
        currentCluster = cluster;
        glueRequired = 1;
    }

    private boolean isGlue(ItemStack stack) {
        return stack.getItem() instanceof SuperGlueItem;
    }

    private Box getCurrentSelectionBox() {
        return firstPos == null || hoveredPos == null ? null : new Box(Vec3d.of(firstPos), Vec3d.of(hoveredPos)).stretch(1, 1, 1);
    }

    public boolean onMouseInput(MinecraftClient mc, boolean attack) {
        ClientPlayerEntity player = mc.player;
        ClientWorld level = mc.world;

        if (!isGlue(player.getMainHandStack()))
            return false;
        if (!player.canModifyBlocks())
            return false;

        if (attack) {
            if (selected == null)
                return false;
            player.networkHandler.sendPacket(new SuperGlueRemovalPacket(selected.getId(), soundSourceForRemoval));
            selected = null;
            clusterCooldown = 0;
            return true;
        }

        if (player.isSneaking()) {
            if (firstPos != null) {
                discard(player);
                return true;
            }
            return false;
        }

        if (hoveredPos == null)
            return false;

        Direction face = null;
        if (mc.crosshairTarget instanceof BlockHitResult bhr) {
            face = bhr.getSide();
            BlockState blockState = level.getBlockState(hoveredPos);
            if (blockState.getBlock() instanceof AbstractChassisBlock cb)
                if (cb.getGlueableSide(blockState, bhr.getSide()) != null)
                    return false;
        }

        if (firstPos != null && currentCluster != null) {
            boolean canReach = currentCluster.contains(hoveredPos);
            boolean canAfford = SuperGlueSelectionHelper.collectGlueFromInventory(player, glueRequired, true);

            if (!canReach || !canAfford)
                return true;

            confirm(player);
            return true;
        }

        firstPos = hoveredPos;
        if (face != null)
            spawnParticles(level, firstPos, face, true);
        CreateLang.translate("super_glue.first_pos").sendStatus(player);
        AllSoundEvents.SLIME_ADDED.playAt(level, firstPos, 0.5F, 0.85F, false);
        level.playSound(player, firstPos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.75f, 1);
        return true;
    }

    public void discard(ClientPlayerEntity player) {
        currentCluster = null;
        firstPos = null;
        CreateLang.translate("super_glue.abort").sendStatus(player);
        clusterCooldown = 0;
    }

    public void confirm(ClientPlayerEntity player) {
        player.networkHandler.sendPacket(new SuperGlueSelectionPacket(firstPos, hoveredPos));
        AllSoundEvents.SLIME_ADDED.playAt(player.clientWorld, hoveredPos, 0.5F, 0.95F, false);
        player.clientWorld.playSound(player, hoveredPos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.75f, 1);

        if (currentCluster != null)
            Outliner.getInstance().showCluster(clusterOutlineSlot, currentCluster).colored(0xB5F2C6)
                .withFaceTextures(AllSpecialTextures.GLUE, AllSpecialTextures.HIGHLIGHT_CHECKERED).disableLineNormals().lineWidth(1 / 24f);

        discard(player);
        CreateLang.translate("super_glue.success").sendStatus(player);
        clusterCooldown = 40;
    }

    public static void spawnParticles(World world, BlockPos pos, Direction direction, boolean fullBlock) {
        Vec3d vec = Vec3d.of(direction.getVector());
        Vec3d plane = VecHelper.axisAlingedPlaneOf(vec);
        Vec3d facePos = VecHelper.getCenterOf(pos).add(vec.multiply(.5f));

        float distance = fullBlock ? 1f : .25f + .25f * (world.random.nextFloat() - .5f);
        plane = plane.multiply(distance);
        ItemStack stack = new ItemStack(Items.SLIME_BALL);

        for (int i = fullBlock ? 40 : 15; i > 0; i--) {
            Vec3d offset = VecHelper.rotate(plane, 360 * world.random.nextFloat(), direction.getAxis());
            Vec3d motion = offset.normalize().multiply(1 / 16f);
            if (fullBlock)
                offset = new Vec3d(MathHelper.clamp(offset.x, -.5, .5), MathHelper.clamp(offset.y, -.5, .5), MathHelper.clamp(offset.z, -.5, .5));
            Vec3d particlePos = facePos.add(offset);
            world.addParticleClient(
                new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                particlePos.x,
                particlePos.y,
                particlePos.z,
                motion.x,
                motion.y,
                motion.z
            );
        }

    }
}
