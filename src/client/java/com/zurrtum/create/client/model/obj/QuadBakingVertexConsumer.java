/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * Vertex consumer that outputs {@linkplain BakedQuad baked quads}.
 * <p>
 * This consumer accepts data in {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#BLOCK} and is not picky about
 * ordering or missing elements, but will not automatically populate missing data (color will be black, for example).
 * <p>
 * Built quads must be retrieved after building four vertices
 */
public class QuadBakingVertexConsumer implements VertexConsumer {
    public static final int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    public static final int POSITION = findOffset(VertexFormatElement.POSITION);
    public static final int COLOR = findOffset(VertexFormatElement.COLOR);
    public static final int UV0 = findOffset(VertexFormatElement.UV0);
    public static final int UV1 = findOffset(VertexFormatElement.UV1);
    public static final int UV2 = findOffset(VertexFormatElement.UV2);
    public static final int NORMAL = findOffset(VertexFormatElement.NORMAL);

    private static int findOffset(VertexFormatElement element) {
        if (DefaultVertexFormat.BLOCK.contains(element)) {
            // Divide by 4 because we want the int offset
            return DefaultVertexFormat.BLOCK.getOffset(element) / 4;
        }
        return -1;
    }

    private final Map<VertexFormatElement, Integer> ELEMENT_OFFSETS = Util.make(
        new IdentityHashMap<>(), map -> {
            for (var element : DefaultVertexFormat.BLOCK.getElements())
                map.put(element, DefaultVertexFormat.BLOCK.getOffset(element) / 4); // Int offset
        }
    );
    private static final int QUAD_DATA_SIZE = STRIDE * 4;

    private final int[] quadData = new int[QUAD_DATA_SIZE];
    private int vertexIndex = 0;
    private boolean building = false;

    private int tintIndex = -1;
    private Direction direction = Direction.DOWN;
    private TextureAtlasSprite sprite = UnitTextureAtlasSprite.INSTANCE;
    private boolean shade;
    private int lightEmission;
    private boolean hasAmbientOcclusion;

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if (building) {
            if (++vertexIndex > 4) {
                throw new IllegalStateException("Expected quad export after fourth vertex");
            }
        }
        building = true;

        int offset = vertexIndex * STRIDE + POSITION;
        quadData[offset] = Float.floatToRawIntBits(x);
        quadData[offset + 1] = Float.floatToRawIntBits(y);
        quadData[offset + 2] = Float.floatToRawIntBits(z);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        int offset = vertexIndex * STRIDE + NORMAL;
        quadData[offset] = ((int) (x * 127.0f) & 0xFF) | (((int) (y * 127.0f) & 0xFF) << 8) | (((int) (z * 127.0f) & 0xFF) << 16);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        int offset = vertexIndex * STRIDE + COLOR;
        quadData[offset] = ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((g & 0xFF) << 8) | (r & 0xFF);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        int offset = vertexIndex * STRIDE + UV0;
        quadData[offset] = Float.floatToRawIntBits(u);
        quadData[offset + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        if (UV1 >= 0) { // Vanilla doesn't support this, but it may be added by a 3rd party
            int offset = vertexIndex * STRIDE + UV1;
            quadData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        int offset = vertexIndex * STRIDE + UV2;
        quadData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
        return this;
    }

    //    @Override
    public VertexConsumer misc(VertexFormatElement element, int... rawData) {
        Integer baseOffset = ELEMENT_OFFSETS.get(element);
        if (baseOffset != null) {
            int offset = vertexIndex * STRIDE + baseOffset;
            System.arraycopy(rawData, 0, quadData, offset, rawData.length);
        }
        return this;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public BakedQuad bakeQuad() {
        if (!building || ++vertexIndex != 4) {
            throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + vertexIndex);
        }

        BakedQuad quad = new BakedQuad(quadData.clone(), tintIndex, direction, sprite, shade, lightEmission);
        NormalsBakedQuad.markNormals(quad);
        vertexIndex = 0;
        building = false;
        Arrays.fill(quadData, 0);
        return quad;
    }
}
