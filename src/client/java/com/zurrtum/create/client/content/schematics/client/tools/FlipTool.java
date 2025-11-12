package com.zurrtum.create.client.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FlipTool extends PlacementToolBase {

    private final AABBOutline outline = new AABBOutline(new AABB(BlockPos.ZERO));

    @Override
    public void init() {
        super.init();
        renderSelectedFace = false;
    }

    @Override
    public boolean handleRightClick(Minecraft mc) {
        mirror();
        return true;
    }

    @Override
    public boolean handleMouseWheel(double delta) {
        mirror();
        return true;
    }

    @Override
    public void updateSelection(Minecraft mc) {
        super.updateSelection(mc);
    }

    private void mirror() {
        if (schematicSelected && selectedFace.getAxis().isHorizontal()) {
            schematicHandler.getTransformation().flip(selectedFace.getAxis());
            schematicHandler.markDirty();
        }
    }

    @Override
    public void renderOnSchematic(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer) {
        if (!schematicSelected || !selectedFace.getAxis().isHorizontal()) {
            super.renderOnSchematic(mc, ms, buffer);
            return;
        }

        Direction facing = selectedFace.getClockWise();
        AABB bounds = schematicHandler.getBounds();

        Vec3 directionVec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, facing.getAxis()).getUnitVec3i());
        Vec3 boundsSize = new Vec3(bounds.getXsize(), bounds.getYsize(), bounds.getZsize());
        Vec3 vec = boundsSize.multiply(directionVec);
        bounds = bounds.contract(vec.x, vec.y, vec.z).inflate(1 - directionVec.x, 1 - directionVec.y, 1 - directionVec.z);
        bounds = bounds.move(directionVec.scale(.5f).multiply(boundsSize));

        outline.setBounds(bounds);
        AllSpecialTextures tex = AllSpecialTextures.CHECKERED;
        outline.getParams().lineWidth(1 / 16f).disableLineNormals().colored(0xdddddd).withFaceTextures(tex, tex);
        outline.render(mc, ms, buffer, Vec3.ZERO, AnimationTickHolder.getPartialTicks());

        super.renderOnSchematic(mc, ms, buffer);
    }

}
