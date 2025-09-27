package com.zurrtum.create.client.catnip.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.render.BreadcrumbArrowRenderState;
import com.zurrtum.create.client.catnip.gui.render.GradientRectRenderState;
import com.zurrtum.create.client.catnip.gui.render.RadialSectorRenderState;
import com.zurrtum.create.client.catnip.gui.render.TexturedQuadRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DebugLabelManager;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.TextureAllocationException;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.geom.Point2D;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class UIRenderHelper {
    private static final RawProjectionMatrix PROJECTION = new RawProjectionMatrix("UIRenderHelper");
    public static final RenderPipeline BLIT_SCREEN = RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
        .withLocation(Identifier.of(MOD_ID, "pipeline/blit_screen")).withVertexShader("core/blit_screen").withFragmentShader("core/blit_screen")
        .withSampler("InSampler").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS).build();

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
        Window mainWindow = MinecraftClient.getInstance().getWindow();
        framebuffer = CustomRenderTarget.create(mainWindow);
    }

    public static void updateWindowSize(Window mainWindow) {
        if (framebuffer != null)
            framebuffer.resize(mainWindow.getFramebufferWidth(), mainWindow.getFramebufferHeight());
    }

    public static void drawFramebuffer(MatrixStack poseStack, float alpha) {
        if (framebuffer != null)
            framebuffer.renderWithAlpha(poseStack, alpha);
    }

    /**
     * Switch from src to dst, after copying the contents of src to dst.
     */
    public static void swapAndBlitColor(Framebuffer src, Framebuffer dst) {
        int srcId = getFrameBufferId(src);
        int dstId = getFrameBufferId(dst);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, srcId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dstId);
        GlStateManager._glBlitFrameBuffer(
            0,
            0,
            src.viewportWidth,
            src.viewportHeight,
            0,
            0,
            dst.viewportWidth,
            dst.viewportHeight,
            GL30.GL_COLOR_BUFFER_BIT,
            GL20.GL_LINEAR
        );

        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, dstId);
    }

    private static int getFrameBufferId(Framebuffer buffer) {
        GlTexture colorAttachment = (GlTexture) buffer.getColorAttachment();
        int id = buffer.useDepthAttachment ? ((GlTexture) buffer.getDepthAttachment()).getGlId() : 0;
        return colorAttachment.depthTexToFramebufferIdCache.get(id);
    }

    /**
     * @param angle   angle in degrees, 0 means fading to the right
     * @param x       x-position of the starting edge middle point
     * @param y       y-position of the starting edge middle point
     * @param breadth total width of the streak
     * @param length  total length of the streak
     */
    public static void streak(DrawContext graphics, float angle, int x, int y, int breadth, int length) {
        streak(graphics, angle, x, y, breadth, length, COLOR_STREAK);
    }

    public static void streak(DrawContext graphics, float angle, int x, int y, int breadth, int length, Color c) {
        Color color = c.copy().setImmutable();
        Color c1 = color.scaleAlpha(0.625f);
        Color c2 = color.scaleAlpha(0.5f);
        Color c3 = color.scaleAlpha(0.0625f);
        Color c4 = color.scaleAlpha(0f);

        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.rotate((float) ((angle - 90) * (Math.PI / 180.0)));

        streak(graphics, breadth / 2, length, c1, c2, c3, c4);

        poseStack.popMatrix();
    }

    private static void streak(DrawContext graphics, int width, int height, Color c1, Color c2, Color c3, Color c4) {
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
    public static void angledGradient(DrawContext graphics, float angle, int x, int y, float breadth, float length, Couple<Color> c) {
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
        DrawContext graphics,
        float angle,
        int x,
        int y,
        float breadth,
        float length,
        Color startColor,
        Color endColor
    ) {
        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.rotate((float) ((angle - 90) * (Math.PI / 180.0)));

        float w = breadth / 2;
        //graphics.fillGradient(-w, 0, w, length, startColor.getRGB(), endColor.getRGB());
        drawGradientRect(graphics, -w, 0f, w, length, startColor, endColor);

        poseStack.popMatrix();
    }

    public static void drawGradientRect(DrawContext graphics, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        graphics.state.addSimpleElement(new GradientRectRenderState(
            new Matrix3x2f(graphics.getMatrices()),
            left,
            top,
            right,
            bottom,
            startColor,
            endColor
        ));
    }

    public static void breadcrumbArrow(DrawContext graphics, int x, int y, int width, int height, int indent, Couple<Color> colors) {
        breadcrumbArrow(graphics, x, y, width, height, indent, colors.getFirst(), colors.getSecond());
    }

    // draws a wide chevron-style breadcrumb arrow pointing left
    public static void breadcrumbArrow(DrawContext graphics, int x, int y, int width, int height, int indent, Color startColor, Color endColor) {
        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(x - indent, y);

        breadcrumbArrow(graphics, width, height, indent, startColor, endColor);

        poseStack.popMatrix();
    }

    private static void breadcrumbArrow(DrawContext graphics, int width, int height, int indent, Color c1, Color c2) {

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

        graphics.state.addSimpleElement(new BreadcrumbArrowRenderState(
            new Matrix3x2f(graphics.getMatrices()),
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
        DrawContext graphics,
        float innerRadius,
        float outerRadius,
        float startAngle,
        float arcAngle,
        Color innerColor,
        Color outerColor
    ) {
        List<Point2D> innerPoints = getPointsForCircleArc(innerRadius, startAngle, arcAngle);
        List<Point2D> outerPoints = getPointsForCircleArc(outerRadius, startAngle, arcAngle);

        graphics.state.addSimpleElement(new RadialSectorRenderState(
            new Matrix3x2f(graphics.getMatrices()),
            (int) (innerRadius * 2),
            (int) (outerRadius * 2),
            innerPoints,
            outerPoints,
            innerColor,
            outerColor
        ));
    }

    private static List<Point2D> getPointsForCircleArc(float radius, float startAngle, float arcAngle) {
        int segmentCount = Math.abs(arcAngle) <= 90 ? 16 : 32;
        List<Point2D> points = new ArrayList<>(segmentCount);


        float theta = (MathHelper.RADIANS_PER_DEGREE * arcAngle) / (float) (segmentCount - 1);
        float t = MathHelper.RADIANS_PER_DEGREE * startAngle;

        for (int i = 0; i < segmentCount; i++) {
            points.add(new Point2D.Float((float) (radius * Math.cos(t)), (float) (radius * Math.sin(t))));

            t += theta;
        }

        return points;
    }


    //just like AbstractGui#drawTexture, but with a color at every vertex
    public static void drawColoredTexture(
        DrawContext graphics,
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
        DrawContext graphics,
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

    public static void drawStretched(DrawContext graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
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

    public static void drawCropped(DrawContext graphics, int left, int top, int w, int h, TextureSheetSegment tex) {
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
        DrawContext graphics,
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
        DrawContext graphics,
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
        graphics.state.addSimpleElement(new TexturedQuadRenderState(
            new Matrix3x2f(graphics.getMatrices()),
            texture,
            left,
            right,
            top,
            bot,
            c,
            u1,
            u2,
            v1,
            v2
        ));
    }

    public static void flipForGuiRender(MatrixStack poseStack) {
        poseStack.multiplyPositionMatrix(new Matrix4f().scaling(1, -1, 1));
    }

    public static class CustomRenderTarget extends Framebuffer {
        public CustomRenderTarget(@Nullable String name, boolean useDepth) {
            super(name, useDepth);
        }

        public static CustomRenderTarget create(Window mainWindow) {
            CustomRenderTarget framebuffer = new CustomRenderTarget("Custom", true);
            framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight());
            return framebuffer;
        }

        @Override
        public void initFbo(int width, int height) {
            GlBackend device = ((GlBackend) RenderSystem.getDevice());
            int i = device.getMaxTextureSize();
            if (width > 0 && width <= i && height > 0 && height <= i) {
                viewportWidth = width;
                viewportHeight = height;
                textureWidth = width;
                textureHeight = height;
                colorAttachment = device.createTexture(() -> name + " / Color", 15, TextureFormat.RGBA8, width, height, 1, 1);
                colorAttachmentView = device.createTextureView(colorAttachment);
                colorAttachment.setAddressMode(AddressMode.CLAMP_TO_EDGE);
                setFilter(FilterMode.NEAREST, true);
                if (useDepthAttachment) {
                    depthAttachment = createDepthTexture(() -> name + " / Depth", 15, TextureFormat.DEPTH32, width, height, 1, 1);
                    depthAttachmentView = device.createTextureView(depthAttachment);
                    depthAttachment.setTextureFilter(FilterMode.NEAREST, false);
                    depthAttachment.setAddressMode(AddressMode.CLAMP_TO_EDGE);
                    setupFramebuffer(((GlTexture) colorAttachment), ((GlTexture) depthAttachment).getGlId());
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
            DebugLabelManager debugLabelManager = ((GlBackend) RenderSystem.getDevice()).getDebugLabelManager();
            String label = debugLabelManager.isUsable() && supplier != null ? supplier.get() : null;
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
                    throw new TextureAllocationException("Could not allocate texture of " + width + "x" + height + " for " + label);
                } else if (m != 0) {
                    throw new IllegalStateException("OpenGL error " + m);
                } else {
                    GlTexture glTexture = new GlTexture(usage, label, textureFormat, width, height, depthOrLayers, mipLevels, glId);
                    debugLabelManager.labelGlTexture(glTexture);
                    return glTexture;
                }
            }
        }

        private static void setupFramebuffer(GlTexture colorAttachment, int depthAttachmentId) {
            int framebufferId = GlStateManager.glGenFramebuffers();
            int target = GlConst.GL_DRAW_FRAMEBUFFER;
            int fbo = GlStateManager.getFrameBuffer(target);
            GlStateManager._glBindFramebuffer(target, framebufferId);
            GlStateManager._glFramebufferTexture2D(target, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, colorAttachment.getGlId(), 0);
            GlStateManager._glFramebufferTexture2D(target, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_TEXTURE_2D, depthAttachmentId, 0);
            GlStateManager._glFramebufferTexture2D(target, GL30.GL_STENCIL_ATTACHMENT, GlConst.GL_TEXTURE_2D, depthAttachmentId, 0);
            GlStateManager._glBindFramebuffer(target, fbo);
            colorAttachment.depthTexToFramebufferIdCache.put(depthAttachmentId, framebufferId);
        }

        public void renderWithAlpha(MatrixStack poseStack, float alpha) {
            Window window = MinecraftClient.getInstance().getWindow();

            float guiScaledWidth = window.getScaledWidth();
            float guiScaledHeight = window.getScaledHeight();

            float vx = guiScaledWidth;
            float vy = guiScaledHeight;
            float tx = (float) viewportWidth / (float) textureWidth;
            float ty = (float) viewportHeight / (float) textureHeight;

            MinecraftClient minecraft = MinecraftClient.getInstance();
            Matrix4f matrix4f = poseStack.peek().getPositionMatrix();
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(PROJECTION.set(matrix4f), ProjectionType.ORTHOGRAPHIC);
            GpuBufferSlice dynamicTransformsBuffer = RenderSystem.getDynamicUniforms().write(
                new Matrix4f().setTranslation(0.0F, 0.0F, -2000.0F),
                new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                new Vector3f(),
                new Matrix4f(),
                0.0F
            );

            VertexFormat.DrawMode vertexFormatMode = BLIT_SCREEN.getVertexFormatMode();
            VertexFormat vertexFormat = BLIT_SCREEN.getVertexFormat();
            Tessellator tesselator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(vertexFormatMode, vertexFormat);
            bufferbuilder.vertex(0, vy, 0).texture(0, 0).color(1, 1, 1, alpha);
            bufferbuilder.vertex(vx, vy, 0).texture(tx, 0).color(1, 1, 1, alpha);
            bufferbuilder.vertex(vx, 0, 0).texture(tx, ty).color(1, 1, 1, alpha);
            bufferbuilder.vertex(0, 0, 0).texture(0, ty).color(1, 1, 1, alpha);
            BuiltBuffer buffer = bufferbuilder.end();

            Framebuffer framebuffer = minecraft.getFramebuffer();
            RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(vertexFormatMode);
            GpuBuffer gpuBuffer = vertexFormat.uploadImmediateVertexBuffer(buffer.getBuffer());
            int count = buffer.getDrawParameters().indexCount();
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Immediate draw for UIRenderHelper",
                framebuffer.getColorAttachmentView(),
                OptionalInt.empty(),
                framebuffer.getDepthAttachmentView(),
                OptionalDouble.empty()
            )) {
                renderPass.setPipeline(BLIT_SCREEN);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setVertexBuffer(0, shapeIndexBuffer.getIndexBuffer(count));
                renderPass.setIndexBuffer(gpuBuffer, shapeIndexBuffer.getIndexType());
                renderPass.bindSampler("InSampler", colorAttachmentView);
                renderPass.setUniform("DynamicTransforms", dynamicTransformsBuffer);
                renderPass.drawIndexed(0, 0, count, 1);
            }

            buffer.close();
            RenderSystem.restoreProjectionMatrix();
        }

    }

}
