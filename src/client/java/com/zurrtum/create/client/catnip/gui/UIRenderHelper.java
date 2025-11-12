package com.zurrtum.create.client.catnip.gui;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.*;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.render.BreadcrumbArrowRenderState;
import com.zurrtum.create.client.catnip.gui.render.GradientRectRenderState;
import com.zurrtum.create.client.catnip.gui.render.RadialSectorRenderState;
import com.zurrtum.create.client.catnip.gui.render.TexturedQuadRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;

import static com.zurrtum.create.client.catnip.render.PonderRenderPipelines.BLIT_SCREEN;

public class UIRenderHelper {
    private static final PerspectiveProjectionMatrixBuffer PROJECTION = new PerspectiveProjectionMatrixBuffer("UIRenderHelper");

    public static final Couple<Color> COLOR_TEXT = Couple.create(new Color(0xff_eeeeee), new Color(0xff_a3a3a3)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_DARKER = Couple.create(new Color(0xff_a3a3a3), new Color(0xff_808080)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_ACCENT = Couple.create(new Color(0xff_ddeeff), new Color(0xff_a0b0c0)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_TEXT_STRONG_ACCENT = Couple.create(new Color(0xff_8ab6d6), new Color(0xff_6e92ab))
        .map(Color::setImmutable);

    public static final Color COLOR_STREAK = new Color(0x101010, false).setImmutable();

    /**
     * An FBO that has a stencil buffer for use wherever stencil are necessary. Forcing the main FBO to have a stencil
     * buffer will cause GL error spam when using fabulous graphics.
     */
    @Nullable
    public static CustomRenderTarget framebuffer;

    public static void init() {
        RenderSystem.assertOnRenderThread();
        Window mainWindow = Minecraft.getInstance().getWindow();
        framebuffer = CustomRenderTarget.create(mainWindow);
    }

    public static void updateWindowSize(Window mainWindow) {
        if (framebuffer != null)
            framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight());
    }

    public static void drawFramebuffer(PoseStack poseStack, float alpha) {
        if (framebuffer != null)
            framebuffer.renderWithAlpha(poseStack, alpha);
    }

    private static int getFrameBufferId(RenderTarget buffer) {
        GlTexture colorAttachment = (GlTexture) buffer.getColorTexture();
        int id = buffer.useDepth ? ((GlTexture) buffer.getDepthTexture()).glId() : 0;
        return colorAttachment.fboCache.get(id);
    }

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
     * @see #angledGradient(DrawContext, float, int, int, float, float, Color, Color)
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

    public static class CustomRenderTarget extends RenderTarget {
        public CustomRenderTarget(@Nullable String name, boolean useDepth) {
            super(name, useDepth);
        }

        public static CustomRenderTarget create(Window mainWindow) {
            CustomRenderTarget framebuffer = new CustomRenderTarget("Custom", true);
            framebuffer.resize(mainWindow.getScreenWidth(), mainWindow.getScreenHeight());
            return framebuffer;
        }

        @Override
        public void createBuffers(int width, int height) {
            GlDevice device = ((GlDevice) RenderSystem.getDevice());
            int i = device.getMaxTextureSize();
            if (width > 0 && width <= i && height > 0 && height <= i) {
                width = width;
                height = height;
                colorTexture = device.createTexture(() -> label + " / Color", 15, TextureFormat.RGBA8, width, height, 1, 1);
                colorTextureView = device.createTextureView(colorTexture);
                colorTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
                setFilterMode(FilterMode.NEAREST, true);
                if (useDepth) {
                    depthTexture = createDepthTexture(() -> label + " / Depth", 15, TextureFormat.DEPTH32, width, height, 1, 1);
                    depthTextureView = device.createTextureView(depthTexture);
                    depthTexture.setTextureFilter(FilterMode.NEAREST, false);
                    depthTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
                    setupFramebuffer(((GlTexture) colorTexture), ((GlTexture) depthTexture).glId());
                }
            } else {
                throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + i + ")");
            }
        }

        private static GpuTexture createDepthTexture(
            @Nullable Supplier<String> supplier,
            int usage,
            TextureFormat textureFormat,
            int width,
            int height,
            int depthOrLayers,
            int mipLevels
        ) {
            GlDebugLabel debugLabelManager = ((GlDevice) RenderSystem.getDevice()).debugLabels();
            String label = debugLabelManager.exists() && supplier != null ? supplier.get() : null;
            if (mipLevels < 1) {
                throw new IllegalArgumentException("mipLevels must be at least 1");
            } else {
                GlStateManager.clearGlErrors();
                int glId = GlStateManager._genTexture();
                if (label == null) {
                    label = String.valueOf(glId);
                }

                GlStateManager._bindTexture(glId);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipLevels - 1);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, mipLevels - 1);
                if (textureFormat.hasDepthAspect()) {
                    GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_COMPARE_MODE, 0);
                }

                for (int m = 0; m < mipLevels; m++) {
                    GlStateManager._texImage2D(
                        GlConst.GL_TEXTURE_2D,
                        m,
                        GL30.GL_DEPTH32F_STENCIL8,
                        width >> m,
                        height >> m,
                        0,
                        GL30.GL_DEPTH_STENCIL,
                        GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV,
                        null
                    );
                }

                int m = GlStateManager._getError();
                if (m == GlConst.GL_OUT_OF_MEMORY) {
                    throw new GpuOutOfMemoryException("Could not allocate texture of " + width + "x" + height + " for " + label);
                } else if (m != 0) {
                    throw new IllegalStateException("OpenGL error " + m);
                } else {
                    GlTexture glTexture = new GlTexture(usage, label, textureFormat, width, height, depthOrLayers, mipLevels, glId);
                    debugLabelManager.applyLabel(glTexture);
                    return glTexture;
                }
            }
        }

        private static void setupFramebuffer(GlTexture colorAttachment, int depthAttachmentId) {
            int framebufferId = GlStateManager.glGenFramebuffers();
            int target = GlConst.GL_DRAW_FRAMEBUFFER;
            int fbo = GlStateManager.getFrameBuffer(target);
            GlStateManager._glBindFramebuffer(target, framebufferId);
            GlStateManager._glFramebufferTexture2D(target, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, colorAttachment.glId(), 0);
            GlStateManager._glFramebufferTexture2D(target, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_TEXTURE_2D, depthAttachmentId, 0);
            GlStateManager._glFramebufferTexture2D(target, GL30.GL_STENCIL_ATTACHMENT, GlConst.GL_TEXTURE_2D, depthAttachmentId, 0);
            GlStateManager._glBindFramebuffer(target, fbo);
            colorAttachment.fboCache.put(depthAttachmentId, framebufferId);
        }

        public void renderWithAlpha(PoseStack poseStack, float alpha) {
            Window window = Minecraft.getInstance().getWindow();

            float guiScaledWidth = window.getGuiScaledWidth();
            float guiScaledHeight = window.getGuiScaledHeight();

            float vx = guiScaledWidth;
            float vy = guiScaledHeight;
            float tx = (float) width;
            float ty = (float) height;

            Minecraft minecraft = Minecraft.getInstance();
            Matrix4f matrix4f = poseStack.last().pose();
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(PROJECTION.getBuffer(matrix4f), ProjectionType.ORTHOGRAPHIC);
            GpuBufferSlice dynamicTransformsBuffer = RenderSystem.getDynamicUniforms()
                .writeTransform(
                    new Matrix4f().setTranslation(0.0F, 0.0F, -2000.0F),
                    new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                    new Vector3f(),
                    new Matrix4f(),
                    0.0F
                );

            VertexFormat.Mode vertexFormatMode = BLIT_SCREEN.getVertexFormatMode();
            VertexFormat vertexFormat = BLIT_SCREEN.getVertexFormat();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(vertexFormatMode, vertexFormat);
            bufferbuilder.addVertex(0, vy, 0).setUv(0, 0).setColor(1, 1, 1, alpha);
            bufferbuilder.addVertex(vx, vy, 0).setUv(tx, 0).setColor(1, 1, 1, alpha);
            bufferbuilder.addVertex(vx, 0, 0).setUv(tx, ty).setColor(1, 1, 1, alpha);
            bufferbuilder.addVertex(0, 0, 0).setUv(0, ty).setColor(1, 1, 1, alpha);
            MeshData buffer = bufferbuilder.buildOrThrow();

            RenderTarget framebuffer = minecraft.getMainRenderTarget();
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(vertexFormatMode);
            GpuBuffer gpuBuffer = vertexFormat.uploadImmediateVertexBuffer(buffer.vertexBuffer());
            int count = buffer.drawState().indexCount();
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Immediate draw for UIRenderHelper",
                framebuffer.getColorTextureView(),
                OptionalInt.empty(),
                framebuffer.getDepthTextureView(),
                OptionalDouble.empty()
            )) {
                renderPass.setPipeline(BLIT_SCREEN);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setVertexBuffer(0, shapeIndexBuffer.getBuffer(count));
                renderPass.setIndexBuffer(gpuBuffer, shapeIndexBuffer.type());
                renderPass.bindSampler("InSampler", colorTextureView);
                renderPass.setUniform("DynamicTransforms", dynamicTransformsBuffer);
                renderPass.drawIndexed(0, 0, count, 1);
            }

            buffer.close();
            RenderSystem.restoreProjectionMatrix();
        }

    }

}
