package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.blaze3d.platform.Window;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record SceneRenderState(
    int id, PonderScene scene, int width, int height, double slide, boolean userViewMode, LerpedFloat finishingFlash, float partialTicks,
    Matrix3x2f pose, int x1, int y1, ScreenRectangle bounds
) implements PictureInPictureRenderState {
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
            window.getGuiScaledWidth(),
            window.getGuiScaledHeight(),
            new ScreenRectangle(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight()).transformMaxBounds(pose)
        );
    }

    @Override
    public int x0() {
        return 0;
    }

    @Override
    public int y0() {
        return 0;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}