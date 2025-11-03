package com.zurrtum.create.client.content.schematics.client.tools;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.content.schematics.client.SchematicHandler;
import com.zurrtum.create.client.content.schematics.client.SchematicTransformation;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.client.foundation.utility.RaycastHelper.PredicateTraceResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public abstract class SchematicToolBase implements ISchematicTool {

    protected SchematicHandler schematicHandler;

    protected BlockPos selectedPos;
    protected Vec3d chasingSelectedPos;
    protected Vec3d lastChasingSelectedPos;

    protected boolean selectIgnoreBlocks;
    protected int selectionRange;
    protected boolean schematicSelected;
    protected boolean renderSelectedFace;
    protected Direction selectedFace;

    @Override
    public void init() {
        schematicHandler = Create.SCHEMATIC_HANDLER;
        selectedPos = null;
        selectedFace = null;
        schematicSelected = false;
        chasingSelectedPos = Vec3d.ZERO;
        lastChasingSelectedPos = Vec3d.ZERO;
    }

    @Override
    public void updateSelection(MinecraftClient mc) {
        updateTargetPos();

        if (selectedPos == null)
            return;
        lastChasingSelectedPos = chasingSelectedPos;
        Vec3d target = Vec3d.of(selectedPos);
        if (target.distanceTo(chasingSelectedPos) < 1 / 512f) {
            chasingSelectedPos = target;
            return;
        }

        chasingSelectedPos = chasingSelectedPos.add(target.subtract(chasingSelectedPos).multiply(1 / 2f));
    }

    public void updateTargetPos() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // Select Blueprint
        if (schematicHandler.isDeployed()) {
            SchematicTransformation transformation = schematicHandler.getTransformation();
            Box localBounds = schematicHandler.getBounds();

            Vec3d traceOrigin = player.getEyePos();
            Vec3d start = transformation.toLocalSpace(traceOrigin);
            Vec3d end = transformation.toLocalSpace(RaycastHelper.getTraceTarget(player, 70, traceOrigin));
            PredicateTraceResult result = RaycastHelper.rayTraceUntil(start, end, pos -> localBounds.contains(VecHelper.getCenterOf(pos)));

            schematicSelected = !result.missed();
            selectedFace = schematicSelected ? result.getFacing() : null;
        }

        boolean snap = this.selectedPos == null;

        // Select location at distance
        if (selectIgnoreBlocks) {
            float pt = AnimationTickHolder.getPartialTicks();
            selectedPos = BlockPos.ofFloored(player.getCameraPosVec(pt).add(player.getRotationVector().multiply(selectionRange)));
            if (snap)
                lastChasingSelectedPos = chasingSelectedPos = Vec3d.of(selectedPos);
            return;
        }

        // Select targeted Block
        selectedPos = null;
        BlockHitResult trace = RaycastHelper.rayTraceRange(player.getEntityWorld(), player, 75);
        if (trace == null || trace.getType() != Type.BLOCK)
            return;

        BlockPos hit = BlockPos.ofFloored(trace.getPos());
        boolean replaceable = player.getEntityWorld().getBlockState(hit).isReplaceable();
        if (trace.getSide().getAxis().isVertical() && !replaceable)
            hit = hit.offset(trace.getSide());
        selectedPos = hit;
        if (snap)
            lastChasingSelectedPos = chasingSelectedPos = Vec3d.of(selectedPos);
    }

    @Override
    public void renderTool(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera) {
    }

    @Override
    public void renderOverlay(InGameHud gui, DrawContext graphics, float partialTicks, int width, int height) {
    }

    @Override
    public void renderOnSchematic(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer) {
        if (!schematicHandler.isDeployed())
            return;

        ms.push();
        AABBOutline outline = schematicHandler.getOutline();
        if (renderSelectedFace) {
            outline.getParams().highlightFace(selectedFace).withFaceTextures(
                AllSpecialTextures.CHECKERED,
                AllKeys.hasControlDown() ? AllSpecialTextures.HIGHLIGHT_CHECKERED : AllSpecialTextures.CHECKERED
            );
        }
        outline.getParams().colored(0x6886c5).withFaceTexture(AllSpecialTextures.CHECKERED).lineWidth(1 / 16f);
        outline.render(MinecraftClient.getInstance(), ms, buffer, Vec3d.ZERO, AnimationTickHolder.getPartialTicks());
        outline.getParams().clearTextures();
        ms.pop();
    }

}
