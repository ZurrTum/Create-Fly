package com.zurrtum.create.client.catnip.placement;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.client.catnip.ghostblock.GhostBlocks;
import com.zurrtum.create.client.catnip.gui.render.ArrowRenderState;
import com.zurrtum.create.client.catnip.gui.render.TextureArrowRenderState;
import com.zurrtum.create.client.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.ponder.config.CClient;
import com.zurrtum.create.client.ponder.enums.PonderConfig;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.catnip.math.VecHelper.getCenterOf;

public class PlacementClient {

    static final LerpedFloat angle = LerpedFloat.angular().chase(0, 0.25f, LerpedFloat.Chaser.EXP);
    @Nullable
    static BlockPos target = null;
    @Nullable
    static BlockPos lastTarget = null;
    static int animationTick = 0;

    public static void tick(MinecraftClient mc) {
        setTarget(null);
        checkHelpers(mc);

        if (target == null) {
            if (animationTick > 0)
                animationTick = Math.max(animationTick - 2, 0);

            return;
        }

        if (animationTick < 10)
            animationTick++;

    }

    private static void checkHelpers(MinecraftClient mc) {
        ClientWorld world = mc.world;

        if (world == null)
            return;

        if (!(mc.crosshairTarget instanceof BlockHitResult ray))
            return;

        if (mc.player == null)
            return;

        if (mc.player.isSneaking())// for now, disable all helpers when sneaking TODO add helpers that respect
            // sneaking but still show position
            return;

        for (Hand hand : Hand.values()) {

            ItemStack heldItem = mc.player.getStackInHand(hand);

            List<IPlacementHelper> filteredForHeldItem = new ArrayList<>();
            for (IPlacementHelper helper : PlacementHelpers.getHelpersView()) {
                if (helper.matchesItem(heldItem))
                    filteredForHeldItem.add(helper);
            }

            if (filteredForHeldItem.isEmpty())
                continue;

            BlockPos pos = ray.getBlockPos();
            BlockState state = world.getBlockState(pos);

            List<IPlacementHelper> filteredForState = new ArrayList<>();
            for (IPlacementHelper helper : filteredForHeldItem) {
                if (helper.matchesState(state))
                    filteredForState.add(helper);
            }

            if (filteredForState.isEmpty())
                continue;

            boolean atLeastOneMatch = false;
            for (IPlacementHelper h : filteredForState) {
                PlacementOffset offset = h.getOffset(mc.player, world, state, pos, ray, heldItem);

                if (offset.isSuccessful()) {
                    renderAt(h, offset);
                    setTarget(offset.getBlockPos());
                    atLeastOneMatch = true;
                    break;
                }

            }

            // at least one helper activated, no need to check the offhand if we are still
            // in the mainhand
            if (atLeastOneMatch)
                return;

        }
    }

    static void setTarget(@Nullable BlockPos target) {
        PlacementClient.target = target;

        if (target == null)
            return;

        if (lastTarget == null) {
            lastTarget = target;
            return;
        }

        if (!lastTarget.equals(target))
            lastTarget = target;
    }

    public static void onRenderCrosshairOverlay(MinecraftClient mc, DrawContext graphics, float partialTicks) {
        PlayerEntity player = mc.player;

        if (player != null && animationTick > 0) {
            float screenY = graphics.getScaledWindowHeight() / 2f;
            float screenX = graphics.getScaledWindowWidth() / 2f;
            float progress = getCurrentAlpha();

            drawDirectionIndicator(graphics, partialTicks, screenX, screenY, progress);
        }
    }

    public static float getCurrentAlpha() {
        return Math.min(animationTick / 10f/* + event.getPartialTicks() */, 1f);
    }

    private static void drawDirectionIndicator(DrawContext graphics, float partialTicks, float centerX, float centerY, float progress) {
        float r = .8f;
        float g = .8f;
        float b = .8f;
        float a = progress * progress;

        Vec3d projTarget = VecHelper.projectToPlayerView(getCenterOf(lastTarget), partialTicks);

        Vec3d target = new Vec3d(projTarget.x, projTarget.y, 0);
        if (projTarget.z > 0)
            target = target.negate();

        Vec3d norm = target.normalize();
        Vec3d ref = new Vec3d(0, 1, 0);
        float targetAngle = AngleHelper.deg(-Math.acos(norm.dotProduct(ref)));

        if (norm.x < 0)
            targetAngle = 360 - targetAngle;

        if (animationTick < 10)
            angle.setValue(targetAngle);

        angle.chase(targetAngle, .25f, LerpedFloat.Chaser.EXP);
        angle.tickChaser();

        float snapSize = 22.5f;
        float snappedAngle = (snapSize * Math.round(angle.getValue(0f) / snapSize)) % 360f;

        float length = 10;

        CClient.PlacementIndicatorSetting mode = PonderConfig.client().placementIndicator.get();
        if (mode == CClient.PlacementIndicatorSetting.TRIANGLE)
            fadedArrow(graphics, centerX, centerY, r, g, b, a, length, snappedAngle);
        else if (mode == CClient.PlacementIndicatorSetting.TEXTURE)
            textured(graphics, centerX, centerY, a, snappedAngle);
    }

    private static void fadedArrow(
        DrawContext graphics,
        float centerX,
        float centerY,
        float r,
        float g,
        float b,
        float a,
        float length,
        float snappedAngle
    ) {
        Matrix3x2fStack ms = graphics.getMatrices();
        ms.pushMatrix();
        ms.translate(centerX, centerY);
        ms.rotate(angle.getValue(0) * (float) (Math.PI / 180.0));
        double scale = PonderConfig.client().indicatorScale.get();
        ms.scale((float) scale, (float) scale);
        int size = (int) ((10 + length) * scale);
        graphics.state.addSimpleElement(new ArrowRenderState(new Matrix3x2f(ms), size, r, g, b, a, length));
        ms.popMatrix();
    }

    public static void textured(DrawContext graphics, float centerX, float centerY, float alpha, float snappedAngle) {
        Matrix3x2fStack ms = graphics.getMatrices();
        ms.pushMatrix();
        ms.translate(centerX, centerY);
        float scale = PonderConfig.client().indicatorScale.get() * .75f;
        ms.scale(scale, scale);
        ms.scale(12, 12);

        float index = snappedAngle / 22.5f;
        float tex_size = 16f / 256f;

        float tx = 0;
        float ty = index * tex_size;
        float tw = 1f;
        float th = tex_size;
        int size = (int) (36 * scale);
        TextureSetup texture = PonderGuiTextures.PLACEMENT_INDICATOR_SHEET.bind();
        graphics.state.addSimpleElement(new TextureArrowRenderState(new Matrix3x2f(ms), size, alpha, texture, tx, ty, tw, th));
        ms.popMatrix();
    }


    public static void renderAt(Object slot, PlacementOffset offset) {
        displayGhost(slot, offset);
    }

    //RIP
    public static void renderArrow(Vec3d center, Vec3d target, Direction arrowPlane) {
        renderArrow(center, target, arrowPlane, 1D);
    }

    public static void renderArrow(Vec3d center, Vec3d target, Direction arrowPlane, double distanceFromCenter) {
        Vec3d direction = target.subtract(center).normalize();
        Vec3d facing = Vec3d.of(arrowPlane.getVector());
        Vec3d start = center.add(direction);
        Vec3d offset = direction.multiply(distanceFromCenter - 1);
        Vec3d offsetA = direction.crossProduct(facing).normalize().multiply(.25);
        Vec3d offsetB = facing.crossProduct(direction).normalize().multiply(.25);
        Vec3d endA = center.add(direction.multiply(.75)).add(offsetA);
        Vec3d endB = center.add(direction.multiply(.75)).add(offsetB);
        Outliner.getInstance().showLine("placementArrowA" + center + target, start.add(offset), endA.add(offset)).lineWidth(1 / 16f);
        Outliner.getInstance().showLine("placementArrowB" + center + target, start.add(offset), endB.add(offset)).lineWidth(1 / 16f);
    }

    public static void displayGhost(Object slot, PlacementOffset offset) {
        if (!offset.hasGhostState())
            return;

        GhostBlocks.getInstance().showGhostState(slot, offset.getTransform().apply(offset.getGhostState())).at(offset.getBlockPos()).breathingAlpha();
    }
}
