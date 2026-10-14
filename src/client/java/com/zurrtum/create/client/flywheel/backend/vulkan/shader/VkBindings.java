package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The fixed descriptor-binding and location map for Flywheel's Vulkan pipelines.
 *
 * <p><b>Why this file is necessary.</b> In OpenGL a program binds resources by
 * <em>name</em>, after linking: {@code glUniformBlockBinding(name, index)},
 * {@code glUniform1i(samplerLoc, textureUnit)}, and
 * {@code glBindAttribLocation(program, index, name)}. In Vulkan all of those are
 * baked into the compiled SPIR-V as {@code layout(binding = N)} and
 * {@code layout(location = N)}, and descriptor bindings must agree with the
 * {@code VkDescriptorSetLayoutBinding} array the pipeline was created with.
 *
 * <p><b>Contiguity requirement in VulkanMod.</b>
 * {@code Pipeline.createDescriptorSetLayout()} allocates a
 * {@code VkDescriptorSetLayoutBinding.Buffer} of length
 * {@code buffers.size() + imageDescriptors.size()} and indexes into it using
 * {@code bindings.get(descriptor.getBinding())} directly. Bindings registered on
 * a pipeline must therefore form a contiguous range {@code 0 .. N - 1} with no
 * gaps:
 * <ul>
 *   <li>Bindings {@code 0..4}: the five global std140 uniform blocks, matching
 *       {@code Uniforms.FRAME_INDEX..LEVEL_INDEX}.</li>
 *   <li>Binding {@code 5}: {@link #DRAW_BLOCK} ({@code _FlwDrawUniforms}), which
 *       packs the loose per-draw uniforms ({@code _flw_packedMaterial},
 *       {@code _flw_baseInstance}, {@code _flw_baseVertex},
 *       {@code _flw_modelMatrixUniform}, {@code _flw_normalMatrixUniform}) that
 *       Vulkan GLSL forbids outside a block.</li>
 *   <li>Bindings {@code 6..12}: the seven instancing samplers (including
 *       {@code _flw_instances}, {@code _flw_lightLut}, and
 *       {@code _flw_lightSections}, rewritten from {@code usamplerBuffer} to
 *       {@code sampler2D} under Option C).</li>
 * </ul>
 */
public final class VkBindings {

    /**
     * Width (in texels) of the 2D textures that replace OpenGL's 1D
     * {@code usamplerBuffer}s under Option C. Each texel is 4 bytes
     * ({@code VK_FORMAT_R8G8B8A8_UNORM}, 4096 bytes per row).
     */
    public static final int BUFFER_TEXTURE_WIDTH = 1024;
    public static final int BUFFER_TEXTURE_SHIFT = 10;
    public static final int BUFFER_TEXTURE_MASK = BUFFER_TEXTURE_WIDTH - 1;

    public static final String FRAME_BLOCK = "_FlwFrameUniforms";
    public static final String FOG_BLOCK = "_FlwFogUniforms";
    public static final String OPTIONS_BLOCK = "_FlwOptionsUniforms";
    public static final String PLAYER_BLOCK = "_FlwPlayerUniforms";
    public static final String LEVEL_BLOCK = "_FlwLevelUniforms";
    public static final String DRAW_BLOCK = "_FlwDrawUniforms";

    /**
     * Rounds a block's occupied byte count up to the 16-byte base alignment std140 requires of
     * the block as a whole. Individual members are already aligned by the layout rules, so this
     * only ever moves the total.
     */
    private static int std140BlockSize(int usedBytes) {
        return (usedBytes + 15) & ~15;
    }

    // The expressions below are per-block *used* byte counts taken from the declarations in
    // internal/uniforms/*.glsl, then std140-rounded.
    //
    // These used to be wrong. FRAME read 852 because the hand arithmetic dropped _FlwCullData
    // (32 B), dropped the third vec2 (flw_viewportSize, 8 B), and counted 19 trailing scalars
    // where frame.glsl declares 13; OPTIONS/PLAYER/LEVEL were never rounded to the block
    // alignment at all. It is not cosmetic -- VkProgram passes blockSize() straight into
    // VkDescriptorBufferInfo::range, so a short value lets the shader read past the block.
    // VkUniformLayout.verify() re-derives these from the parsed GLSL and warns on mismatch.
    public static final int FRAME_BLOCK_SIZE = std140BlockSize(96 + 32 + 64 * 9 + 16 * 5 + 8 * 3 + 4 * 13); // 864
    public static final int FOG_BLOCK_SIZE = std140BlockSize(16 + 4 * 4); // 32
    public static final int OPTIONS_BLOCK_SIZE = std140BlockSize(4 * 14); // 56 -> 64
    public static final int PLAYER_BLOCK_SIZE = std140BlockSize(16 * 2 + 8 + 4 * 9); // 76 -> 80
    public static final int LEVEL_BLOCK_SIZE = std140BlockSize(16 * 4 + 4 * 13); // 116 -> 128
    public static final int DRAW_BLOCK_SIZE = std140BlockSize(8 + 4 * 2 + 64 + 48); // 128

    /** First binding handed out to a sampler. Uniform blocks occupy 0-5. */
    public static final int FIRST_SAMPLER_BINDING = 6;

    private static final Map<String, Integer> BLOCK_BINDINGS;
    private static final Map<String, Integer> BLOCK_SIZES;
    private static final Map<String, Integer> SAMPLER_BINDINGS;
    private static final Map<String, GlTextureUnit> INSTANCING_SAMPLER_UNITS;
    private static final Map<String, Integer> ALL_BINDINGS;

    private static final Map<String, Integer> VERTEX_INPUT_LOCATIONS;
    private static final Map<String, Integer> VARYING_LOCATIONS;
    private static final Map<String, Integer> FRAGMENT_OUTPUT_LOCATIONS;

    static {
        var blocks = new LinkedHashMap<String, Integer>();
        blocks.put(FRAME_BLOCK, 0);
        blocks.put(FOG_BLOCK, 1);
        blocks.put(OPTIONS_BLOCK, 2);
        blocks.put(PLAYER_BLOCK, 3);
        blocks.put(LEVEL_BLOCK, 4);
        blocks.put(DRAW_BLOCK, 5);
        BLOCK_BINDINGS = Collections.unmodifiableMap(blocks);

        var sizes = new LinkedHashMap<String, Integer>();
        sizes.put(FRAME_BLOCK, FRAME_BLOCK_SIZE);
        sizes.put(FOG_BLOCK, FOG_BLOCK_SIZE);
        sizes.put(OPTIONS_BLOCK, OPTIONS_BLOCK_SIZE);
        sizes.put(PLAYER_BLOCK, PLAYER_BLOCK_SIZE);
        sizes.put(LEVEL_BLOCK, LEVEL_BLOCK_SIZE);
        sizes.put(DRAW_BLOCK, DRAW_BLOCK_SIZE);
        BLOCK_SIZES = Collections.unmodifiableMap(sizes);

        var samplers = new LinkedHashMap<String, Integer>();
        int next = FIRST_SAMPLER_BINDING;

        // Non-OIT instancing samplers: bindings 6..12 (contiguous after blocks 0..5).
        samplers.put("flw_diffuseTex", next++);
        samplers.put("flw_overlayTex", next++);
        samplers.put("flw_lightTex", next++);
        samplers.put("_flw_crumblingTex", next++);
        samplers.put("_flw_instances", next++);
        samplers.put("_flw_lightLut", next++);
        samplers.put("_flw_lightSections", next++);

        // OIT-only. Registered in the lookup map so OIT shaders can be preprocessed,
        // though v1 always compiles with OitMode.OFF.
        samplers.put("_flw_depthRange", next++);
        samplers.put("_flw_coefficients", next++);
        samplers.put("_flw_blueNoise", next++);
        samplers.put("_flw_accumulate", next);
        SAMPLER_BINDINGS = Collections.unmodifiableMap(samplers);

        var units = new LinkedHashMap<String, GlTextureUnit>();
        units.put("flw_diffuseTex", Samplers.DIFFUSE);
        units.put("flw_overlayTex", Samplers.OVERLAY);
        units.put("flw_lightTex", Samplers.LIGHT);
        units.put("_flw_crumblingTex", Samplers.CRUMBLING);
        units.put("_flw_instances", Samplers.INSTANCE_BUFFER);
        units.put("_flw_lightLut", Samplers.LIGHT_LUT);
        units.put("_flw_lightSections", Samplers.LIGHT_SECTIONS);
        INSTANCING_SAMPLER_UNITS = Collections.unmodifiableMap(units);

        var all = new LinkedHashMap<String, Integer>();
        all.putAll(blocks);
        all.putAll(samplers);
        ALL_BINDINGS = Collections.unmodifiableMap(all);

        var vertInputs = new LinkedHashMap<String, Integer>();
        vertInputs.put("_flw_aPos", 0);
        vertInputs.put("_flw_aColor", 1);
        vertInputs.put("_flw_aTexCoord", 2);
        vertInputs.put("_flw_aOverlay", 3);
        vertInputs.put("_flw_aLight", 4);
        vertInputs.put("_flw_aNormal", 5);
        VERTEX_INPUT_LOCATIONS = Collections.unmodifiableMap(vertInputs);

        var varyings = new LinkedHashMap<String, Integer>();
        varyings.put("flw_vertexPos", 0);
        varyings.put("flw_vertexColor", 1);
        varyings.put("flw_vertexTexCoord", 2);
        varyings.put("flw_vertexOverlay", 3);
        varyings.put("flw_vertexLight", 4);
        varyings.put("flw_vertexNormal", 5);
        varyings.put("flw_sphericalDistance", 6);
        varyings.put("flw_cylindricalDistance", 7);
        varyings.put("_flw_crumblingTexCoord", 8);
        varyings.put("_flw_ids", 9);
        varyings.put("_flw_packedMaterial", 10);
        VARYING_LOCATIONS = Collections.unmodifiableMap(varyings);

        var fragOutputs = new LinkedHashMap<String, Integer>();
        fragOutputs.put("_flw_outputColor", 0);
        fragOutputs.put("frag", 0);
        fragOutputs.put("_flw_depthRange_out", 0);
        fragOutputs.put("_flw_accumulate", 0);
        fragOutputs.put("_flw_coeffs0", 0);
        fragOutputs.put("_flw_coeffs1", 1);
        fragOutputs.put("_flw_coeffs2", 2);
        fragOutputs.put("_flw_coeffs3", 3);
        FRAGMENT_OUTPUT_LOCATIONS = Collections.unmodifiableMap(fragOutputs);
    }

    private VkBindings() {
    }

    /**
     * Uniform block name to descriptor binding, 0-5.
     */
    public static Map<String, Integer> blockBindings() {
        return BLOCK_BINDINGS;
    }

    /**
     * Uniform block name to std140 byte size.
     */
    public static Map<String, Integer> blockSizes() {
        return BLOCK_SIZES;
    }

    /**
     * Sampler name to descriptor binding, 6 and up.
     */
    public static Map<String, Integer> samplerBindings() {
        return SAMPLER_BINDINGS;
    }

    /**
     * The seven samplers bound by instancing pipelines (bindings 6..12), mapped to
     * their corresponding {@link GlTextureUnit}.
     */
    public static Map<String, GlTextureUnit> instancingSamplerUnits() {
        return INSTANCING_SAMPLER_UNITS;
    }

    /**
     * Combined map of all uniform blocks and samplers to their descriptor binding,
     * suitable for {@link VkShaderSource#applyBindings(Map)}.
     */
    public static Map<String, Integer> allBindings() {
        return ALL_BINDINGS;
    }

    public static Map<String, Integer> vertexInputLocations() {
        return VERTEX_INPUT_LOCATIONS;
    }

    public static Map<String, Integer> varyingLocations() {
        return VARYING_LOCATIONS;
    }

    public static Map<String, Integer> fragmentOutputLocations() {
        return FRAGMENT_OUTPUT_LOCATIONS;
    }

    /**
     * Descriptor binding for a uniform block, or {@code -1} if Flywheel does not
     * own it.
     */
    public static int blockBinding(String name) {
        return BLOCK_BINDINGS.getOrDefault(name, -1);
    }

    /**
     * Byte size of a uniform block, or {@code -1} if unknown.
     */
    public static int blockSize(String name) {
        return BLOCK_SIZES.getOrDefault(name, -1);
    }

    /**
     * Descriptor binding for a sampler, or {@code -1} if Flywheel does not own it.
     */
    public static int samplerBinding(String name) {
        return SAMPLER_BINDINGS.getOrDefault(name, -1);
    }

    /**
     * Maps a Flywheel texture unit to the Vulkan texture-slot index
     * {@code VTextureSelector} uses.
     *
     * @throws IllegalArgumentException if the unit exceeds the slot count
     */
    public static int imageIdx(GlTextureUnit unit) {
        // VTextureSelector.SIZE is 12; hard-coded here to avoid a compile-time
        // dependency on VulkanMod from this constants holder.
        int size = 12;

        if (unit.number < 0 || unit.number >= size) {
            throw new IllegalArgumentException(
                "Texture unit " + unit + " maps to Vulkan slot " + unit.number + ", outside [0, " + (size - 1) + "]"
            );
        }

        return unit.number;
    }
}