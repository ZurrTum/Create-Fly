package com.zurrtum.create.client.catnip.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.render.BreadcrumbArrowRenderState;
import com.zurrtum.create.client.catnip.gui.render.GradientRectRenderState;
import com.zurrtum.create.client.catnip.gui.render.RadialSectorRenderState;
import com.zurrtum.create.client.catnip.gui.render.TexturedQuadRenderState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class UIRenderHelper {
    public static final Couple<Color> COLOR_TEXT = Couple.create(new Color(0xff_eeeeee), new Color(0xff_a3a3a3)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_DARKER = Couple.create(new Color(0xff_a3a3a3), new Color(0xff_808080)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_ACCENT = Couple.create(new Color(0xff_ddeeff), new Color(0xff_a0b0c0)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_STRONG_ACCENT = Couple.create(new Color(0xff_8ab6d6), new Color(0xff_6e92ab))
        .map(Color::setImmutable);

    public static final Color COLOR_STREAK = new Color(0x101010, false).setImmutable();

    /**
     * @param angle   angle in degrees, 0 means fading to the right
     * @param x       x-position of the starting edge middle point
     * @param y       y-position of the starting edge middle point
     * @param breadth total width of the streak
     * @param length  total length of the streak
     */
    public static void streak(GuiGraphics graphics, float angle, int x, int y, int breadth, int length) {
        streak(graphics, angle, x, y, breadth, length, COLOR_STREAK);
    }

    public static void streak(GuiGraphics graphics, float angle, int x, int y, int breadth, int length, Color c) {
        Color color = c.copy().setImmutable();
        Color c1 = color.scaleAlpha(0.625f);
        Color c2 = color.scaleAlpha(0.5f);
        Color c3 = color.scaleAlpha(0.0625f);
        Color c4 = color.scaleAlpha(0f);

        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.rotate((float) ((angle - 90) * (Math.PI / 180.0)));

        streak(graphics, breadth / 2, length, c1, c2, c3, c4);

        poseStack.popMatrix();
    }

    private static void streak(GuiGraphics graphics, int width, int height, Color c1, Color c2, Color c3, Color c4) {
        if (NavigatableSimiScreen.isCurrentlyRenderingPreviousScreen())
            return;

        double split1 = .5;
        double split2 = .75;
        graphics.fillGradient(-width, 0, width, (int) (split1 * height), c1.getRGB(), c2.getRGB());
        graphics.fillGradient(-width, (int) (split1 * height), width, (int) (split2 * height), c2.getRGB(), c3.getRGB());
        graphics.fillGradient(-width, (int) (split2 * height), width, height, c3.getRGB(), c4.getRGB());
    }

    /**
     * @see #angledGradient(GuiGraphics, float, int, int, float, float, Color, Color)
     */
    public static void angledGradient(GuiGraphics graphics, float angle, int x, int y, float breadth, float length, Couple<Color> c) {
        angledGradient(graphics, angle, x, y, breadth, length, c.getFirst(), c.getSecond());
    }

    /**
     * x and y specify the middle point of the starting edge
     *
     * @param angle      the angle of the gradient in degrees; 0° means from left to right
     * @param startColor the color at the starting edge
     * @param endColor   the color at the ending edge
     * @param breadth    the total width of the gradient
     */
    public static void angledGradient(
        GuiGraphics graphics,
        float angle,
        int x,
        int y,
        float breadth,
        float length,
        Color startColor,
        Color endColor
    ) {
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.rotate((float) ((angle - 90) * (Math.PI / 180.0)));

        float w = breadth / 2;
        //graphics.fillGradient(-w, 0, w, length, startColor.getRGB(), endColor.getRGB());
        drawGradientRect(graphics, -w, 0f, w, length, startColor, endColor);

        poseStack.popMatrix();
    }

    public static void drawGradientRect(GuiGraphics graphics, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        graphics.guiRenderState.submitGuiElement(new GradientRectRenderState(
            new Matrix3x2f(graphics.pose()),
            left,
            top,
            right,
            bottom,
            startColor,
            endColor
        ));
    }

    public static void breadcrumbArrow(GuiGraphics graphics, int x, int y, int width, int height, int indent, Couple<Color> colors) {
        breadcrumbArrow(graphics, x, y, width, height, indent, colors.getFirst(), colors.getSecond());
    }

    // draws a wide chevron-style breadcrumb arrow pointing left
    public static void breadcrumbArrow(GuiGraphics graphics, int x, int y, int width, int height, int indent, Color startColor, Color endColor) {
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x - indent, y);

        breadcrumbArrow(graphics, width, height, indent, startColor, endColor);

        poseStack.popMatrix();
    }

    private static void breadcrumbArrow(GuiGraphics graphics, int width, int height, int indent, Color c1, Color c2) {

        /*
         * 0,0       x1,y0 ********************* x2,y0 ***** x3,y0
         *       ****                                     ****
         *   ****                                     ****
         * x0,y1     x1,y1                       x2,y1
         *   ****                                     ****
         *       ****                                     ****
         *           x1,y2 ********************* x2,y2 ***** x3,y2
         *
         */

        float x0 = 0;
        float x1 = indent;
        float x2 = width;
        float x3 = indent + width;

        float y0 = 0;
        float y1 = height / 2f;
        float y2 = height;

        indent = Math.abs(indent);
        width = Math.abs(width);
        Color fc1 = Color.mixColors(c1, c2, 0);
        Color fc2 = Color.mixColors(c1, c2, (indent) / (width + 2f * indent));
        Color fc3 = Color.mixColors(c1, c2, (indent + width) / (width + 2f * indent));
        Color fc4 = Color.mixColors(c1, c2, 1);

        graphics.guiRenderState.submitGuiElement(new BreadcrumbArrowRenderState(
            new Matrix3x2f(graphics.pose()),
            x0,
            x1,
            x2,
            x3,
            y0,
            y1,
            y2,
            fc1,
            fc2,
            fc3,
            fc4,
            indent + width,
            height
        ));
    }

    /**
     * centered on 0, 0
     *
     * @param arcAngle length of the sector arc
     */
    public static void drawRadialSector(
        GuiGraphics graphics,
        float innerRadius,
        float outerRadius,
        float startAngle,
        float arcAngle,
        Color innerColor,
        Color outerColor
    ) {
        List<Vec2> innerPoints = getPointsForCircleArc(innerRadius, startAngle, arcAngle);
        List<Vec2> outerPoints = getPointsForCircleArc(outerRadius, startAngle, arcAngle);
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Vec2 point : innerPoints) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        for (Vec2 point : outerPoints) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }

        graphics.guiRenderState.submitGuiElement(new RadialSectorRenderState(
            new Matrix3x2f(graphics.pose()),
            minX,
            maxX,
            minY,
            maxY,
            innerPoints,
            outerPoints,
            innerColor,
            outerColor
        ));
    }

    private static List<Vec2> getPointsForCircleArc(float radius, float startAngle, float arcAngle) {
        int segmentCount = Math.abs(arcAngle) <= 90 ? 16 : 32;
        List<Vec2> points = new ArrayList<>(segmentCount);


        float theta = (Mth.DEG_TO_RAD * arcAngle) / (float) (segmentCount - 1);
        float t = Mth.DEG_TO_RAD * startAngle;

        for (int i = 0; i < segmentCount; i++) {
            points.add(new Vec2((float) (radius * Math.cos(t)), (float) (radius * Math.sin(t))));

            t += theta;
        }

        return points;
    }


    //just like AbstractGui#drawTexture, but with a color at every vertex
    public static void drawColoredTexture(
        GuiGraphics graphics,
        TextureSetup texture,
        Color c,
        int x,
        int y,
        int tex_left,
        int tex_top,
        int width,
        int height
    ) {
        drawColoredTexture(graphics, texture, c, x, y, (float) tex_left, (float) tex_top, width, height, 256, 256);
    }

    public static void drawColoredTexture(
        GuiGraphics graphics,
        TextureSetup texture,
        Color c,
        int x,
        int y,
        float tex_left,
        float tex_top,
        int width,
        int height,
        int sheet_width,
        int sheet_height
    ) {
        drawColoredTexture(graphics, texture, c, x, x + width, y, y + height, width, height, tex_left, tex_top, sheet_width, sheet_height);
    }

    public static void drawStretched(GuiGraphics graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
        drawTexturedQuad(
            graphics,
            tex.bind(),
            Color.WHITE,
            left,
            left + w,
            top,
            top + h,
            tex.getStartX() / 256f,
            (tex.getStartX() + tex.getWidth()) / 256f,
            tex.getStartY() / 256f,
            (tex.getStartY() + tex.getHeight()) / 256f
        );
    }

    public static void drawCropped(GuiGraphics graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
        drawTexturedQuad(
            graphics,
            tex.bind(),
            Color.WHITE,
            left,
            left + w,
            top,
            top + h,
            tex.getStartX() / 256f,
            (tex.getStartX() + w) / 256f,
            tex.getStartY() / 256f,
            (tex.getStartY() + h) / 256f
        );
    }

    private static void drawColoredTexture(
        GuiGraphics graphics,
        TextureSetup texture,
        Color c,
        int left,
        int right,
        int top,
        int bot,
        int tex_width,
        int tex_height,
        float tex_left,
        float tex_top,
        int sheet_width,
        int sheet_height
    ) {
        drawTexturedQuad(
            graphics,
            texture,
            c,
            left,
            right,
            top,
            bot,
            (tex_left + 0.0F) / (float) sheet_width,
            (tex_left + (float) tex_width) / (float) sheet_width,
            (tex_top + 0.0F) / (float) sheet_height,
            (tex_top + (float) tex_height) / (float) sheet_height
        );
    }

    private static void drawTexturedQuad(
        GuiGraphics graphics,
        TextureSetup texture,
        Color c,
        int left,
        int right,
        int top,
        int bot,
        float u1,
        float u2,
        float v1,
        float v2
    ) {
        graphics.guiRenderState.submitGuiElement(new TexturedQuadRenderState(
            new Matrix3x2f(graphics.pose()),
            texture,
            left,
            right,
            top,
            bot,
            c,
            u1,
            u2,
            v1,
            v2,
            graphics.scissorStack.peek()
        ));
    }

    public static void flipForGuiRender(PoseStack poseStack) {
        poseStack.mulPose(new Matrix4f().scaling(1, -1, 1));
    }
}
