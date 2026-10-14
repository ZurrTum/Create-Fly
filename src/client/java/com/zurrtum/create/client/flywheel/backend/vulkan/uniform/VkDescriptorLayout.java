package com.zurrtum.create.client.flywheel.backend.vulkan.uniform;

import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;
import com.zurrtum.create.client.flywheel.backend.vulkan.shader.VkBindings;
import com.zurrtum.create.client.flywheel.backend.vulkan.shader.VkProgram;

import net.vulkanmod.vulkan.shader.PipelineConfig;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.UBO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;

/**
 * Builds the VulkanMod descriptors that back Flywheel's uniform layer, for
 * {@link VkProgram.Builder}.
 *
 * <p><b>The pattern is Beryl's.</b> {@code net.beryl.render.shader.BerylPipelineConfigs}
 * declares a {@code PipelineConfig} with {@code addUB} and {@code addImageDescriptor}
 * and hands it to {@code Pipeline.Builder#applyConfig}; {@code net.beryl.render.util.SUtil}
 * creates the pipeline. Flywheel cannot go through {@code PipelineConfig} - its JSON shape
 * cannot express the {@code _Flw} block sizes or the {@code Samplers} texture-slot mapping
 * - but the destination objects are the same ones {@code applyConfig} would have built, so
 * this class constructs them directly and {@link VkProgram.Builder#addUBO} /
 * {@link #addImageDescriptor} accept them verbatim.
 *
 * <h2>Why the sizes come from {@link VkUniformLayout}</h2>
 * VulkanMod's own member layout ({@code Uniform.Info}) is close to std140 but not
 * quite: alignments are expressed in four-byte words, and a {@code vec3} is recorded as
 * three words with <em>no trailing padding</em>, so the member after it would land four
 * bytes early. It also has no concept of a struct member, which {@code _FlwFrameUniforms}
 * needs for {@code FrustumPlanes} and {@code _FlwCullData}. Handing it a member list
 * would therefore produce offsets that disagree with the SPIR-V glslang compiled.
 * Instead each {@link UBO} is created with a {@code null} member list and a
 * hand-supplied total size, which is VulkanMod's "dumb block" mode:
 * {@code AlignedStruct}'s constructor returns early on a null list, {@code UBO#update}
 * then has no fields to write, and {@link VkUniformBuffer} owns the bytes. The size
 * still matters - VulkanMod copies it straight into
 * {@code VkDescriptorBufferInfo::range} - so it is the std140 size from
 * {@link VkUniformLayout}.
 *
 * <h2>Contiguity</h2>
 * {@code Pipeline#createDescriptorSetLayout} allocates
 * {@code buffers.size() + imageDescriptors.size()} entries and writes into slot
 * {@code binding} directly, so the bindings handed here must cover {@code 0 .. N-1} with
 * no gaps. {@link VkBindings} already arranges that - blocks {@code 0..5}, samplers
 * {@code 6..12} - and {@link #apply} asserts it rather than trusting it.
 *
 * <p><b>Not thread safe</b>: {@link #apply} mutates a builder that is consumed on the
 * render thread.
 */
public final class VkDescriptorLayout {

    private VkDescriptorLayout() {
    }

    // ------------------------------------------------------------------
    // Uniform buffers
    // ------------------------------------------------------------------

    /**
     * One {@link UBO} per block, in binding order, sized per std140.
     *
     * <p>The stage mask is {@code VK_SHADER_STAGE_ALL_GRAPHICS}, obtained through
     * {@code PipelineConfig#getStageFromString} rather than a hard-coded constant so it
     * cannot drift from what VulkanMod considers "all".
     */
    public static List<UBO> uniformBuffers() {
        var ubos = new ArrayList<UBO>(VkUniformLayout.ALL.size());

        for (VkUniformLayout.Block block : VkUniformLayout.ALL) {
            ubos.add(new UBO(
                block.name(),
                block.binding(),
                PipelineConfig.getStageFromString("all"),
                block.size(),
                null
            ));
        }

        return ubos;
    }

    // ------------------------------------------------------------------
    // Samplers
    // ------------------------------------------------------------------

    /**
     * The seven instancing samplers, bindings {@code 6..12}, mapped to the VulkanMod
     * texture slots Flywheel's {@code Samplers} enum already reserves.
     *
     * <p>The OIT samplers ({@code _flw_depthRange}, {@code _flw_coefficients},
     * {@code _flw_blueNoise}, {@code _flw_accumulate}) are deliberately excluded: v1
     * never defines {@code _FLW_OIT}, so those declarations are preprocessed away and a
     * descriptor for them would only widen the set layout. They remain in
     * {@link VkBindings} so a future OIT pass can add them without touching the GLSL.
     *
     * <p>{@code _flw_crumblingTex} is included even though only
     * {@code ContextShader.CRUMBLING} pipelines declare it. Vulkan permits a descriptor
     * the shader does not read, whereas the reverse is an error, and pinning the set
     * layout across every pipeline keeps one layout for all of them.
     */
    public static List<ImageDescriptor> imageDescriptors() {
        Map<String, GlTextureUnit> units = VkBindings.instancingSamplerUnits();
        var descriptors = new ArrayList<ImageDescriptor>(units.size());

        for (Map.Entry<String, GlTextureUnit> entry : units.entrySet()) {
            String samplerName = entry.getKey();
            int binding = VkBindings.samplerBinding(samplerName);
            if (binding < 0) {
                continue;
            }

            descriptors.add(new ImageDescriptor(
                binding,
                "sampler2D",
                samplerName,
                VkBindings.imageIdx(entry.getValue()),
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
            ));
        }

        return descriptors;
    }

    // ------------------------------------------------------------------
    // Applying
    // ------------------------------------------------------------------

    /**
     * Registers the full descriptor set on {@code builder}.
     *
     * <p>Mutually exclusive with {@link VkProgram.Builder#withDefaultInstancingLayout},
     * which registers the same bindings itself: adding both would give every UBO and
     * sampler two descriptors at one binding, and
     * {@code Pipeline#createDescriptorSetLayout} would write only one of them.
     *
     * @throws IllegalStateException if the bindings are not the contiguous
     *                               {@code 0 .. N-1} range VulkanMod requires
     */
    public static void apply(VkProgram.Builder builder) {
        List<UBO> ubos = uniformBuffers();
        List<ImageDescriptor> images = imageDescriptors();

        verifyContiguous(ubos, images);

        for (UBO ubo : ubos) {
            builder.addUBO(ubo);
        }
        for (ImageDescriptor image : images) {
            builder.addImageDescriptor(image);
        }
    }

    /**
     * Fails loudly if the binding numbers do not tile {@code 0 .. N-1}.
     *
     * <p>This is the one structural assumption that cannot be checked by the Vulkan
     * driver at pipeline creation in a way that points back at the cause: an out-of-range
     * binding index writes into a heap-allocated {@code VkDescriptorSetLayoutBinding}
     * array and fails somewhere unrelated. Checking it here turns that into a message
     * naming the block.
     */
    private static void verifyContiguous(List<UBO> ubos, List<ImageDescriptor> images) {
        int expected = 0;

        for (UBO ubo : ubos) {
            if (ubo.getBinding() != expected) {
                throw new IllegalStateException(
                    "Uniform block '" + ubo.name + "' has binding " + ubo.getBinding()
                        + " but " + expected + " was expected. VulkanMod indexes its descriptor set layout "
                        + "array by binding number, so the bindings must be contiguous from 0."
                );
            }
            expected++;
        }

        for (ImageDescriptor image : images) {
            if (image.getBinding() != expected) {
                throw new IllegalStateException(
                    "Sampler '" + image.name + "' has binding " + image.getBinding()
                        + " but " + expected + " was expected. VulkanMod indexes its descriptor set layout "
                        + "array by binding number, so the bindings must be contiguous from 0."
                );
            }
            expected++;
        }
    }
}