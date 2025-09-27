package com.zurrtum.create.client.ponder.api.scene;

import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.InputElementBuilder;
import com.zurrtum.create.client.ponder.api.element.TextElementBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface OverlayInstructions {
    TextElementBuilder showText(int duration);

    TextElementBuilder showOutlineWithText(Selection selection, int duration);

    InputElementBuilder showControls(Vec3d sceneSpace, Pointing direction, int duration);

    void chaseBoundingBoxOutline(PonderPalette color, Object slot, Box boundingBox, int duration);

    void showCenteredScrollInput(BlockPos pos, Direction side, int duration);

    void showScrollInput(Vec3d location, Direction side, int duration);

    void showRepeaterScrollInput(BlockPos pos, int duration);

    void showFilterSlotInput(Vec3d location, int duration);

    void showFilterSlotInput(Vec3d location, Direction side, int duration);

    void showLine(PonderPalette color, Vec3d start, Vec3d end, int duration);

    void showBigLine(PonderPalette color, Vec3d start, Vec3d end, int duration);

    void showOutline(PonderPalette color, Object slot, Selection selection, int duration);
}