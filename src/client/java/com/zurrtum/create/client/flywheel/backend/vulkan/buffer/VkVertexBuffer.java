package com.zurrtum.create.client.flywheel.backend.vulkan.buffer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.InternalVertex;
import com.zurrtum.create.client.flywheel.lib.memory.FlwMemoryTracker;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import org.jetbrains.annotations.Nullable;

/**
 * The Vulkan replacement for {@code GlVertexArray} + the {@code GlBuffer}s it bound: a vertex
 * {@link GpuBuffer} and an index {@link GpuBuffer}, plus the {@link VertexFormat} that tells
 * VulkanMod's {@code GraphicsPipeline} how to read the vertex bytes.
 *
 * <p><b>What a VAO actually did, and what replaces it.</b> {@code MeshPool.bind(vao)} and
 * {@code IndexPool.bind(vao)} did four things: set the element buffer, bind the VBO at binding 0
 * with offset 0 and stride {@code InternalVertex.STRIDE}, and point six attributes at their
 * offsets inside that buffer. Vulkan has no VAO; those four things are split between the pipeline
 * (offsets, formats, stride, all baked at pipeline creation from the {@code VertexFormat}) and the
 * render pass (which buffer is currently bound to slot 0 / as the index buffer). So this class
 * owns the two buffers and the {@code VertexFormat}, and {@link #bind(RenderPass)} does the
 * per-draw half.
 *
 * <p><b>The byte layout is unchanged from the GL path.</b> {@link #FORMAT} is built to match
 * {@code InternalVertex.STRIDE} ({@link #stride()} bytes) exactly:
 * <pre>
 *   offset  size  attribute        VertexFormatElement       VkFormat VulkanMod picks
 *        0    12  _flw_aPos        POSITION  FLOAT  x3      R32G32B32_SFLOAT  (106)
 *       12     4  _flw_aColor      COLOR     UBYTE  x4      R8G8B8A8_UNORM    (37)
 *       16     8  _flw_aTexCoord   UV0       FLOAT  x2      R32G32_SFLOAT     (103)
 *       24     4  _flw_aOverlay    UV1       SHORT  x2      R16G16_SINT       (82)
 *       28     4  _flw_aLight      (custom)  USHORT x2      R16G16_UINT       (81)
 *       32     3  _flw_aNormal     NORMAL    BYTE   x3      R8G8B8A8_SNORM    (38)
 *       35     1  padding
 *       36        stride
 * </pre>
 * The VkFormat column is not cosmetic: {@code GraphicsPipeline.getAttributeDescriptions} derives
 * each attribute's format from {@code (Usage, Type)} alone, and picks up its offset from
 * {@code VertexFormat.getOffset(i)}. Three consequences worth knowing:
 * <ul>
 *   <li><b>Location == element index.</b> VulkanMod sets
 *       {@code VkVertexInputAttributeDescription.location} to the element's position in the list,
 *       which is why {@code VkBindings} puts {@code _flw_aPos.._flw_aNormal} at locations 0..5.
 *       Do not reorder {@link #FORMAT}.</li>
 *   <li><b>No alignment is applied.</b> {@code VertexFormat.Builder.add} packs tightly and only
 *       {@code padding(int)} inserts gaps, so {@link #FORMAT} derives its one trailing pad byte
 *       from {@link #stride()} rather than hard-coding 36, and asserts the resulting offsets.</li>
 *   <li><b>{@code Usage.GENERIC} only accepts count == 1.</b> Anything else throws
 *       {@code RuntimeException("Unknown type: %s")} at pipeline creation. {@code overlay} and
 *       {@code light} are 2-component, so they are declared as {@code Usage.UV} elements -
 *       {@code SHORT -> R16G16_SINT}, {@code USHORT -> R16G16_UINT} - which is also why
 *       {@code vertex_input.vert} must declare them as {@code ivec2} / {@code uvec2} rather than
 *       {@code vec2}.</li>
 * </ul>
 *
 * <p><b>No {@code net.vulkanmod} in this file.</b> Everything here is Minecraft's public
 * {@code GpuBuffer} / {@code CommandEncoder} / {@code RenderPass} API; VulkanMod only shows up in
 * {@link VkInstanceTexture}.
 *
 * <p><b>Upload caveat.</b> {@code CommandEncoder.writeToBuffer} throws
 * {@code IllegalStateException("Close the existing render pass before performing additional
 * commands")} if called while a render pass is open. Upload before opening the pass, exactly like
 * the GL backend uploads its VBOs in {@code MeshPool.flush()}.
 */
public class VkVertexBuffer {

    private static final String VERTEX_LABEL = "flywheel/vertex";
    private static final String INDEX_LABEL = "flywheel/index";

    /**
     * {@code GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST} (0x20 | 0x08).
     *
     * <p>{@code COPY_DST} is required because we push data with
     * {@code CommandEncoder.writeToBuffer} (a staging {@code vkCmdCopyBuffer}) rather than mapping
     * the buffer. VulkanMod maps these to {@code VK_BUFFER_USAGE_VERTEX_BUFFER_BIT} and
     * {@code VK_BUFFER_USAGE_TRANSFER_DST_BIT}.
     */
    public static final int VERTEX_USAGE = GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST;

    /** {@code GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST} (0x40 | 0x08). */
    public static final int INDEX_USAGE = GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST;

    /** {@code IndexPool} fills 32-bit indices, and the GL path drew with {@code GL_UNSIGNED_INT}. */
    public static final VertexFormat.IndexType INDEX_TYPE = VertexFormat.IndexType.INT;

    /** Bytes between consecutive vertices; identical to the GL path. */
    public static final int STRIDE = InternalVertex.STRIDE;

    private static final VertexFormatElement LIGHT = lightElement();

    /**
     * The {@code VertexFormat} every instancing pipeline must be built with. Element order fixes
     * the vertex attribute locations, so it must stay in sync with
     * {@code VkBindings#vertexInputLocations()}.
     */
    public static final VertexFormat FORMAT = buildFormat();

    @Nullable
    private GpuBuffer vertexBuffer;
    @Nullable
    private GpuBuffer indexBuffer;

    private long vertexBytes;
    private long indexBytes;

    private boolean deleted;

    /**
     * {@code light} is {@code UNSIGNED_SHORT x 2}, which Minecraft has no built-in element for, so
     * register one. The id is scanned for rather than hard-coded because MC owns ids 0..6 and other
     * mods may have claimed more.
     */
    private static synchronized VertexFormatElement lightElement() {
        for (int id = 0; id < 32; id++) {
            if (VertexFormatElement.byId(id) == null) {
                return VertexFormatElement.register(
                    id, 2, VertexFormatElement.Type.USHORT, VertexFormatElement.Usage.UV, 2);
            }
        }
        throw new IllegalStateException("No free VertexFormatElement id for flywheel light");
    }

    private static VertexFormat buildFormat() {
        VertexFormatElement[] elements = {
            VertexFormatElement.POSITION,
            VertexFormatElement.COLOR,
            VertexFormatElement.UV0,
            VertexFormatElement.UV1,
            LIGHT,
            VertexFormatElement.NORMAL,
        };

        var builder = VertexFormat.builder();
        int used = 0;
        for (int i = 0; i < elements.length; i++) {
            builder.add(vertexInputName(i), elements[i]);
            used += elements[i].byteSize();
        }

        // InternalVertex.LAYOUT pads the last element up to the layout's max alignment (4), which
        // leaves one byte of slack after the 3-byte normal. MC's builder is strictly packed, so
        // that slack has to be asked for explicitly.
        int padding = STRIDE - used;
        if (padding < 0) {
            throw new IllegalStateException(
                "Flywheel vertex stride " + STRIDE + " is smaller than the packed element size " + used);
        }
        if (padding > 0) {
            builder.padding(padding);
        }

        return verify(builder.build(), elements, used + padding);
    }

    /** Fails loudly if the MC format drifted away from {@link InternalVertex#STRIDE}. */
    private static VertexFormat verify(
        VertexFormat format, VertexFormatElement[] elements, int expectedSize) {
        if (format.getVertexSize() != expectedSize) {
            throw new IllegalStateException(
                "Vertex size " + format.getVertexSize() + " != expected " + expectedSize);
        }

        int expected = 0;
        for (int i = 0; i < elements.length; i++) {
            // getOffset(element), not getOffsetsByElement()[i]: the array is keyed by
            // VertexFormatElement.id(), which is not the position in our list. Minecraft's own
            // ids are 0=POSITION, 1=COLOR, 2=UV0, 3=UV1, 4=an unused UV pair, so index 4 of that
            // array describes an element this format does not contain and reads back as -1.
            int offset = format.getOffset(elements[i]);
            if (offset != expected) {
                throw new IllegalStateException(
                    "Attribute " + i + " (" + vertexInputName(i) + ") is at offset " + offset
                        + ", Flywheel's vertex view puts it at " + expected);
            }
            expected += elements[i].byteSize();
        }

        if (format.getVertexSize() != STRIDE) {
            throw new IllegalStateException(
                "VertexFormat stride " + format.getVertexSize() + " != InternalVertex.STRIDE " + STRIDE);
        }

        FlwBackend.LOGGER.info(
            "[FW/VK] vertex format: stride={} offsets=[{}, {}, {}, {}, {}, {}]",
            format.getVertexSize(),
            format.getOffset(elements[0]), format.getOffset(elements[1]), format.getOffset(elements[2]),
            format.getOffset(elements[3]), format.getOffset(elements[4]), format.getOffset(elements[5]));

        return format;
    }

    /** Must match {@code VkBindings#vertexInputLocations()} and {@code vertex_input.vert}. */
    private static String vertexInputName(int index) {
        return switch (index) {
            case 0 -> "_flw_aPos";
            case 1 -> "_flw_aColor";
            case 2 -> "_flw_aTexCoord";
            case 3 -> "_flw_aOverlay";
            case 4 -> "_flw_aLight";
            case 5 -> "_flw_aNormal";
            default -> throw new IllegalArgumentException("Bad vertex input index " + index);
        };
    }

    /** The {@code VertexFormat} to hand to the pipeline builder / {@code RenderPipeline}. */
    public static VertexFormat format() {
        return FORMAT;
    }

    public static int stride() {
        return STRIDE;
    }

    /**
     * Binds slot 0 and the index buffer for the currently open {@link RenderPass}.
     *
     * <p>Replaces {@code vao.bindForDraw()} plus the VAO's binding state. VulkanMod only accepts
     * slot 0, which is all Flywheel needs - instance data is addressed in the shader through the
     * {@code _flw_baseInstance} uniform rather than a second vertex buffer, because
     * {@code VkCommandEncoder.drawFromBuffers} hard-codes {@code firstInstance = 0}.
     */
    public void bind(RenderPass pass) {
        if (vertexBuffer != null) {
            pass.setVertexBuffer(0, vertexBuffer);
        }
        if (indexBuffer != null) {
            pass.setIndexBuffer(indexBuffer, INDEX_TYPE);
        }
    }

    /**
     * Replaces the vertex buffer contents with {@code block}.
     *
     * <p>{@code block} must hold {@code vertexCount * STRIDE} bytes at
     * {@code InternalVertex}'s layout - exactly what {@code MeshPool.uploadAll()} builds out of
     * {@code FullVertexView}. The buffer is recreated when the requested size grows, since a
     * {@code GpuBuffer} is immutable in size.
     *
     * <p>Must not be called while a render pass is open.
     */
    public void uploadVertices(MemoryBlock block) {
        long bytes = align4(block.size());
        if (bytes == 0) {
            return;
        }

        if (vertexBuffer == null || vertexBytes < bytes) {
            replaceVertexBuffer(bytes);
        }
        encoder().writeToBuffer(vertexBuffer.slice(0, block.size()), block.asBuffer());
    }

    /**
     * Replaces the index buffer contents with {@code block}, whose entries must be
     * {@link VertexFormat.IndexType#INT}.
     *
     * <p>Must not be called while a render pass is open.
     */
    public void uploadIndices(MemoryBlock block) {
        long bytes = align4(block.size());
        if (bytes == 0) {
            return;
        }

        if (indexBuffer == null || indexBytes < bytes) {
            replaceIndexBuffer(bytes);
        }
        encoder().writeToBuffer(indexBuffer.slice(0, block.size()), block.asBuffer());
    }

    @Nullable
    public GpuBuffer vertexBuffer() {
        return vertexBuffer;
    }

    @Nullable
    public GpuBuffer indexBuffer() {
        return indexBuffer;
    }

    public long vertexBytes() {
        return vertexBytes;
    }

    public long indexBytes() {
        return indexBytes;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        if (deleted) {
            return;
        }
        deleted = true;

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        if (indexBuffer != null) {
            indexBuffer.close();
            indexBuffer = null;
        }
        FlwMemoryTracker._freeGpuMemory(vertexBytes + indexBytes);
        vertexBytes = 0;
        indexBytes = 0;
    }

    private void replaceVertexBuffer(long bytes) {
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }
        FlwMemoryTracker._freeGpuMemory(vertexBytes);
        vertexBuffer = RenderSystem.getDevice().createBuffer(() -> VERTEX_LABEL, VERTEX_USAGE, bytes);
        vertexBytes = bytes;
        FlwMemoryTracker._allocGpuMemory(bytes);
        FlwBackend.LOGGER.debug("[FW/VK] reallocated {} to {} bytes", VERTEX_LABEL, bytes);
    }

    private void replaceIndexBuffer(long bytes) {
        if (indexBuffer != null) {
            indexBuffer.close();
        }
        FlwMemoryTracker._freeGpuMemory(indexBytes);
        indexBuffer = RenderSystem.getDevice().createBuffer(() -> INDEX_LABEL, INDEX_USAGE, bytes);
        indexBytes = bytes;
        FlwMemoryTracker._allocGpuMemory(bytes);
        FlwBackend.LOGGER.debug("[FW/VK] reallocated {} to {} bytes", INDEX_LABEL, bytes);
    }

    private static CommandEncoder encoder() {
        return RenderSystem.getDevice().createCommandEncoder();
    }

    private static long align4(long size) {
        return size + 3L & ~3L;
    }
}