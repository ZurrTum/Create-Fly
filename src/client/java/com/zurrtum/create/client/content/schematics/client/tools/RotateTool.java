package com.zurrtum.create.client.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.LineOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RotateTool extends PlacementToolBase {

    private final LineOutline line = new LineOutline();

    @Override
    public boolean handleMouseWheel(double delta) {
        schematicHandler.getTransformation().rotate90(delta > 0);
        schematicHandler.markDirty();
        return true;
    }

    @Override
    public void renderOnSchematic(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer) {
        AABB bounds = schematicHandler.getBounds();
        double lengthY = bounds.getYsize();
        double height = lengthY + Math.max(20, lengthY);
        Vec3 center = bounds.getCenter().add(schematicHandler.getTransformation().getRotationOffset(false));
        Vec3 start = center.subtract(0, height / 2, 0);
        Vec3 end = center.add(0, height / 2, 0);

        line.getParams().disableCull().disableLineNormals().colored(0xdddddd).lineWidth(1 / 16f);
        line.set(start, end).render(mc, ms, buffer, Vec3.ZERO, AnimationTickHolder.getPartialTicks());

        super.renderOnSchematic(mc, ms, buffer);
    }

}
