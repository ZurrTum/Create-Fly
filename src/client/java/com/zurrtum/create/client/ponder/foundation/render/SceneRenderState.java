package com.zurrtum.create.client.ponder.foundation.render;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record SceneRenderState(
    int id, PonderScene scene, int width, int height, double slide, boolean userViewMode, LerpedFloat finishingFlash, float partialTicks,
    Matrix3x2f pose, int x2, int y2, ScreenRect bounds
) implements SpecialGuiElementRenderState {
    public SceneRenderState(
        int id,
        PonderScene scene,
        int width,
        int height,
        double slide,
        boolean userViewMode,
        LerpedFloat finishingFlash,
        float partialTicks,
        Window window,
        Matrix3x2f pose
    ) {
        this(
            id,
            scene,
            width,
            height,
            slide,
            userViewMode,
            finishingFlash,
            partialTicks,
            pose,
            window.getScaledWidth(),
            window.getScaledHeight(),
            new ScreenRect(0, 0, window.getScaledWidth(), window.getScaledHeight()).transformEachVertex(pose)
        );
    }

    @Override
    public int x1() {
        return 0;
    }

    @Override
    public int y1() {
        return 0;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}