package com.zurrtum.create.client.content.schematics.client.tools;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.content.schematics.client.SchematicTransformation;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class DeployTool extends PlacementToolBase {

    @Override
    public void init() {
        super.init();
        selectionRange = -1;
    }

    @Override
    public void updateSelection(MinecraftClient mc) {
        if (schematicHandler.isActive() && selectionRange == -1) {
            selectionRange = (int) (schematicHandler.getBounds().getCenter().length() / 2);
            selectionRange = MathHelper.clamp(selectionRange, 1, 100);
        }
        selectIgnoreBlocks = InputUtil.isKeyPressed(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);
        super.updateSelection(mc);
    }

    @Override
    public void renderTool(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera) {
        super.renderTool(mc, ms, buffer, camera);

        if (selectedPos == null)
            return;

        ms.push();
        float pt = AnimationTickHolder.getPartialTicks();
        double x = MathHelper.lerp(pt, lastChasingSelectedPos.x, chasingSelectedPos.x);
        double y = MathHelper.lerp(pt, lastChasingSelectedPos.y, chasingSelectedPos.y);
        double z = MathHelper.lerp(pt, lastChasingSelectedPos.z, chasingSelectedPos.z);

        SchematicTransformation transformation = schematicHandler.getTransformation();
        Box bounds = schematicHandler.getBounds();
        Vec3d center = bounds.getCenter();
        Vec3d rotationOffset = transformation.getRotationOffset(true);
        int centerX = (int) center.x;
        int centerZ = (int) center.z;
        double xOrigin = bounds.getLengthX() / 2f;
        double zOrigin = bounds.getLengthZ() / 2f;
        Vec3d origin = new Vec3d(xOrigin, 0, zOrigin);

        ms.translate(x - centerX - camera.x, y - camera.y, z - centerZ - camera.z);
        TransformStack.of(ms).translate(origin).translate(rotationOffset).rotateYDegrees(transformation.getCurrentRotation())
            .translateBack(rotationOffset).translateBack(origin);

        AABBOutline outline = schematicHandler.getOutline();
        outline.render(mc, ms, buffer, Vec3d.ZERO, pt);
        outline.getParams().clearTextures();
        ms.pop();
    }

    @Override
    public boolean handleMouseWheel(double delta) {
        if (!selectIgnoreBlocks)
            return super.handleMouseWheel(delta);
        selectionRange += delta;
        selectionRange = MathHelper.clamp(selectionRange, 1, 100);
        return true;
    }

    @Override
    public boolean handleRightClick(MinecraftClient mc) {
        if (selectedPos == null)
            return super.handleRightClick(mc);
        Vec3d center = schematicHandler.getBounds().getCenter();
        BlockPos target = selectedPos.add(-((int) center.x), 0, -((int) center.z));

        ItemStack item = schematicHandler.getActiveSchematicItem();
        if (item != null) {
            item.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
            item.set(AllDataComponents.SCHEMATIC_ANCHOR, target);
            schematicHandler.getTransformation().startAt(target);
        }

        schematicHandler.getTransformation().moveTo(target);
        schematicHandler.markDirty();
        schematicHandler.deploy(mc);
        return true;
    }

}
