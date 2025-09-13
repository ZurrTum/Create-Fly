package com.zurrtum.create.client.content.schematics.client.tools;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.LineOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RotateTool extends PlacementToolBase {

    private final LineOutline line = new LineOutline();

    @Override
    public boolean handleMouseWheel(double delta) {
        schematicHandler.getTransformation().rotate90(delta > 0);
        schematicHandler.markDirty();
        return true;
    }

    @Override
    public void renderOnSchematic(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer) {
        Box bounds = schematicHandler.getBounds();
        double lengthY = bounds.getLengthY();
        double height = lengthY + Math.max(20, lengthY);
        Vec3d center = bounds.getCenter().add(schematicHandler.getTransformation().getRotationOffset(false));
        Vec3d start = center.subtract(0, height / 2, 0);
        Vec3d end = center.add(0, height / 2, 0);

        line.getParams().disableCull().disableLineNormals().colored(0xdddddd).lineWidth(1 / 16f);
        line.set(start, end).render(mc, ms, buffer, Vec3d.ZERO, AnimationTickHolder.getPartialTicks());

        super.renderOnSchematic(mc, ms, buffer);
    }

}
