package com.zurrtum.create.client.content.schematics.client.tools;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.content.schematics.client.SchematicTransformation;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class DeployTool extends PlacementToolBase {

    @Override
    public void init() {
        super.init();
        selectionRange = -1;
    }

    @Override
    public void updateSelection(Minecraft mc) {
        if (schematicHandler.isActive() && selectionRange == -1) {
            selectionRange = (int) (schematicHandler.getBounds().getCenter().length() / 2);
            selectionRange = Mth.clamp(selectionRange, 1, 100);
        }
        selectIgnoreBlocks = InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);
        super.updateSelection(mc);
    }

    @Override
    public void renderTool(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
        super.renderTool(mc, ms, buffer, camera);

        if (selectedPos == null)
            return;

        ms.pushPose();
        float pt = AnimationTickHolder.getPartialTicks();
        double x = Mth.lerp(pt, lastChasingSelectedPos.x, chasingSelectedPos.x);
        double y = Mth.lerp(pt, lastChasingSelectedPos.y, chasingSelectedPos.y);
        double z = Mth.lerp(pt, lastChasingSelectedPos.z, chasingSelectedPos.z);

        SchematicTransformation transformation = schematicHandler.getTransformation();
        AABB bounds = schematicHandler.getBounds();
        Vec3 center = bounds.getCenter();
        Vec3 rotationOffset = transformation.getRotationOffset(true);
        int centerX = (int) center.x;
        int centerZ = (int) center.z;
        double xOrigin = bounds.getXsize() / 2f;
        double zOrigin = bounds.getZsize() / 2f;
        Vec3 origin = new Vec3(xOrigin, 0, zOrigin);

        ms.translate(x - centerX - camera.x, y - camera.y, z - centerZ - camera.z);
        TransformStack.of(ms).translate(origin).translate(rotationOffset).rotateYDegrees(transformation.getCurrentRotation())
            .translateBack(rotationOffset).translateBack(origin);

        AABBOutline outline = schematicHandler.getOutline();
        outline.render(mc, ms, buffer, Vec3.ZERO, pt);
        outline.getParams().clearTextures();
        ms.popPose();
    }

    @Override
    public boolean handleMouseWheel(double delta) {
        if (!selectIgnoreBlocks)
            return super.handleMouseWheel(delta);
        selectionRange += delta;
        selectionRange = Mth.clamp(selectionRange, 1, 100);
        return true;
    }

    @Override
    public boolean handleRightClick(Minecraft mc) {
        if (selectedPos == null)
            return super.handleRightClick(mc);
        Vec3 center = schematicHandler.getBounds().getCenter();
        BlockPos target = selectedPos.offset(-((int) center.x), 0, -((int) center.z));

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
