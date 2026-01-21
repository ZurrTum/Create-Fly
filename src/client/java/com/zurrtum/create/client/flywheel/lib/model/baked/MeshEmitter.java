package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

class MeshEmitter {
    private static final int INITIAL_CAPACITY = 1;

    private final MeshEmitterManager<?> manager;
    private final ByteBufferBuilderStack byteBufferBuilderStack;
    private final ChunkSectionLayer renderType;

    private Material @UnknownNullability [] materials = new Material[INITIAL_CAPACITY];
    private BufferBuilder @UnknownNullability [] bufferBuilders = new BufferBuilder[INITIAL_CAPACITY];

    // The number of valid elements in the above parallel arrays.
    private int numBufferBuildersPopulated = 0;

    private int currentIndex = 0;

    MeshEmitter(MeshEmitterManager<?> manager, ByteBufferBuilderStack byteBufferBuilderStack, ChunkSectionLayer renderType) {
        this.manager = manager;
        this.byteBufferBuilderStack = byteBufferBuilderStack;
        this.renderType = renderType;
    }

    public void prepareForBlock() {
        // Quad render order within blocks must be preserved for correctness, however between blocks we should try to
        // reduce the number of generated meshes as much as possible. Here we reset the head index without flushing
        // any buffers, so that the next block can start over scanning through the parallel arrays looking for a
        // matching Material/BufferBuilder pair.
        currentIndex = 0;
    }

    public void end(ImmutableList.Builder<Model.ConfiguredMesh> out) {
        for (int index = 0; index < numBufferBuildersPopulated; index++) {
            var renderedBuffer = bufferBuilders[index].build();

            if (renderedBuffer != null) {
                Material material = materials[index];
                Mesh mesh = MeshHelper.blockVerticesToMesh(renderedBuffer, "source=ModelBuilder" + ",material=" + material);
                out.add(new Model.ConfiguredMesh(material, mesh));
                renderedBuffer.close();
            }
        }

        // Not strictly necessary to clear the arrays, but best not to hold on to references for too long here.
        Arrays.fill(bufferBuilders, 0, numBufferBuildersPopulated, null);
        Arrays.fill(materials, 0, numBufferBuildersPopulated, null);

        currentIndex = 0;
        numBufferBuildersPopulated = 0;
    }

    @Nullable
    public BufferBuilder getBuffer(boolean shade, boolean ao) {
        Material material = manager.getMaterial(renderType, shade, ao);
        return material != null ? getBuffer(material) : null;
    }

    public BufferBuilder getBuffer(Material material) {
        // First, scan through and try to find a matching Material.
        while (currentIndex < numBufferBuildersPopulated) {
            if (material.equals(materials[currentIndex])) {
                // Return the matching BufferBuilder, but do not increment as we
                // may still be able to use the same BufferBuilder in the next quad.
                return bufferBuilders[currentIndex];
            }
            ++currentIndex;
        }

        // Nothing matched so we need to grab a new BufferBuilder.
        // Make sure we have room to represent it in the arrays.
        if (currentIndex >= materials.length) {
            // Only technically need to grow one at a time here, but doubling is
            // fine and should reduce the number of reallocations.
            resize(materials.length * 2);
        }

        ByteBufferBuilder byteBufferBuilder = byteBufferBuilderStack.nextOrCreate();

        // Trust that the RenderType mode/format don't change out from underneath us.
        RenderPipeline pipeline = renderType.pipeline();
        BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());

        // currentIndex == numBufferBuildersPopulated here.
        materials[currentIndex] = material;
        bufferBuilders[currentIndex] = bufferBuilder;

        // Again, do not increment currentIndex so we can re-use the new
        // BufferBuilder for the next quad if it matches.
        ++numBufferBuildersPopulated;

        return bufferBuilder;
    }

    private void resize(int capacity) {
        BufferBuilder[] newBufferBuilders = new BufferBuilder[capacity];
        Material[] newMaterials = new Material[capacity];

        System.arraycopy(bufferBuilders, 0, newBufferBuilders, 0, numBufferBuildersPopulated);
        System.arraycopy(materials, 0, newMaterials, 0, numBufferBuildersPopulated);

        bufferBuilders = newBufferBuilders;
        materials = newMaterials;
    }
}