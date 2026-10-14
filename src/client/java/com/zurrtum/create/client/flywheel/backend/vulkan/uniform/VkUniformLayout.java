package com.zurrtum.create.client.flywheel.backend.vulkan.uniform;

import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.vulkan.shader.VkBindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The authoritative std140 layout of Flywheel's six Vulkan uniform blocks.
 *
 * <p><b>Why this exists.</b> OpenGL resolves a uniform to a byte offset inside a
 * named buffer object at {@code glUniform*} time, so {@code GlProgram} never has to
 * know where a member lives. Vulkan inverts that: the whole block is one descriptor
 * (see {@code DescriptorSets.updateDescriptorSet}, which writes
 * {@code VkDescriptorBufferInfo::range = ubo.getSize()}), and each member's address is
 * {@code dynamicOffset + Offset(member)} as baked into the SPIR-V by glslang. Somebody
 * has to know both the per-member offsets and the total block size. That knowledge lives
 * here, in one table, rather than being spread across five Java writers and six blocks.
 *
 * <p><b>The offsets are computed, not transcribed.</b> {@link Layouter} walks the member
 * list in declaration order applying the std140 rules, so a reordering of the GLSL is a
 * reordering of the {@code Spec} arrays below and never a silent mismatch:
 * <ul>
 *   <li>scalars align 4, {@code vec2}/{@code ivec2}/{@code uvec2} align 8;</li>
 *   <li>{@code vec3}/{@code vec4}/{@code ivec4}/matrices align 16;</li>
 *   <li>a {@code vec3} occupies 12 bytes but the <em>next</em> member starts at the next
 *       multiple of 16;</li>
 *   <li>{@code mat3} is three 16-byte columns (48 bytes), {@code mat4} is 64;</li>
 *   <li>a block's size is its last member's end rounded up to 16.</li>
 * </ul>
 *
 * <p><b>Cross-check.</b> {@link #verify()} compares the computed bindings and sizes
 * against {@link VkBindings}, which is the file Agent B keeps as the descriptor map, and
 * logs a warning listing every disagreement. It runs once, from the static initialiser.
 * See the report: {@link VkBindings}' hand-written size constants are stale for four of
 * the six blocks, and this class is what the pipelines are built from.
 *
 * @see VkUniformBuffer for the runtime side
 * @see VkDescriptorLayout for turning this into VulkanMod descriptors
 */
public final class VkUniformLayout {

    /**
     * The GLSL types Flywheel actually declares, with their std140 alignment and size.
     *
     * <p>{@code OPAQUE} covers the two structs inside {@code _FlwFrameUniforms}
     * ({@code FrustumPlanes}, {@code _FlwCullData}). Nothing routes them through
     * {@link VkUniformAccess}; they are written straight into the staging buffer by
     * {@code FrameUniforms}, so they are modelled as "a blob of N bytes at 16-byte
     * alignment" purely so the members that follow land at the right offsets.
     */
    public enum Type {
        OPAQUE(16, 0),
        FLOAT(4, 4),
        INT(4, 4),
        UINT(4, 4),
        VEC2(8, 8),
        VEC3(16, 12),
        VEC4(16, 16),
        IVEC2(8, 8),
        UVEC2(8, 8),
        IVEC4(16, 16),
        MAT3(16, 48),
        MAT4(16, 64);

        /** std140 base alignment, in bytes. */
        public final int align;
        /** Bytes actually occupied; the next member starts at {@code alignUp(offset + size, align)}. */
        public final int size;

        Type(int align, int size) {
            this.align = align;
            this.size = size;
        }

        /**
         * Whether this is a 4-byte scalar, i.e. whether {@code float} and {@code int}
         * setters may be used interchangeably on it. They share a bit pattern in memory,
         * so this is a convenience rather than a correctness requirement.
         */
        public boolean isScalar() {
            return align == 4;
        }
    }

    /**
     * One uniform block member: which block, its GLSL name, its std140 type and its byte
     * offset from the start of the block.
     */
    public record Member(String block, String name, Type type, int offset) {
    }

    /**
     * One uniform block: name, descriptor binding, total std140 size and its members.
     */
    public static final class Block {

        private final String name;
        private final int binding;
        private final int size;
        private final List<Member> members;
        private final Map<String, Member> byName;

        private Block(String name, int binding, int size, List<Member> members) {
            this.name = name;
            this.binding = binding;
            this.size = size;
            this.members = Collections.unmodifiableList(members);

            var index = new LinkedHashMap<String, Member>();
            for (Member member : members) {
                index.put(member.name(), member);
            }
            this.byName = Collections.unmodifiableMap(index);
        }

        public String name() {
            return name;
        }

        public int binding() {
            return binding;
        }

        /** Total std140 size, a multiple of 16. This becomes the descriptor range. */
        public int size() {
            return size;
        }

        public List<Member> members() {
            return members;
        }

        public Member member(String uniformName) {
            return byName.get(uniformName);
        }

        @Override
        public String toString() {
            return name + "@" + binding + " (" + size + " bytes, " + members.size() + " members)";
        }
    }

    // ------------------------------------------------------------------
    // The table
    // ------------------------------------------------------------------

    /**
     * {@code _FlwFrameUniforms}, binding 0. Source:
     * {@code resources/assets/flywheel/flywheel/internal/uniforms/frame.glsl}.
     *
     * <p>{@code flw_frustumPlanes} and {@code _flw_cullData} are opaque structs sized
     * 96 and 32 respectively; see {@link Type#OPAQUE}.
     */
    public static final Block FRAME = build(VkBindings.FRAME_BLOCK, 0,
        Spec.blob("flw_frustumPlanes", 96),
        Spec.blob("_flw_cullData", 32),
        Spec.of("flw_view", Type.MAT4),
        Spec.of("flw_viewInverse", Type.MAT4),
        Spec.of("flw_viewPrev", Type.MAT4),
        Spec.of("flw_projection", Type.MAT4),
        Spec.of("flw_projectionInverse", Type.MAT4),
        Spec.of("flw_projectionPrev", Type.MAT4),
        Spec.of("flw_viewProjection", Type.MAT4),
        Spec.of("flw_viewProjectionInverse", Type.MAT4),
        Spec.of("flw_viewProjectionPrev", Type.MAT4),
        Spec.of("_flw_renderOrigin", Type.IVEC4),
        Spec.of("_flw_cameraPos", Type.VEC4),
        Spec.of("_flw_cameraPosPrev", Type.VEC4),
        Spec.of("_flw_cameraLook", Type.VEC4),
        Spec.of("_flw_cameraLookPrev", Type.VEC4),
        Spec.of("flw_cameraRot", Type.VEC2),
        Spec.of("flw_cameraRotPrev", Type.VEC2),
        Spec.of("flw_viewportSize", Type.VEC2),
        Spec.of("flw_aspectRatio", Type.FLOAT),
        Spec.of("flw_defaultLineWidth", Type.FLOAT),
        Spec.of("flw_viewDistance", Type.FLOAT),
        Spec.of("flw_ticks", Type.UINT),
        Spec.of("flw_partialTick", Type.FLOAT),
        Spec.of("flw_renderTicks", Type.FLOAT),
        Spec.of("flw_renderSeconds", Type.FLOAT),
        Spec.of("flw_systemSeconds", Type.FLOAT),
        Spec.of("flw_systemMillis", Type.UINT),
        Spec.of("flw_cameraInFluid", Type.UINT),
        Spec.of("flw_cameraInBlock", Type.UINT),
        Spec.of("_flw_debugMode", Type.UINT),
        Spec.of("_flw_oitNoise", Type.FLOAT)
    );

    /** {@code _FlwFogUniforms}, binding 1. {@code internal/uniforms/fog.glsl}. */
    public static final Block FOG = build(VkBindings.FOG_BLOCK, 1,
        Spec.of("flw_fogColor", Type.VEC4),
        Spec.of("flw_fogEnvironmentalStart", Type.FLOAT),
        Spec.of("flw_fogEnvironmentalEnd", Type.FLOAT),
        Spec.of("flw_fogRenderDistanceStart", Type.FLOAT),
        Spec.of("flw_fogRenderDistanceEnd", Type.FLOAT)
    );

    /** {@code _FlwOptionsUniforms}, binding 2. {@code internal/uniforms/options.glsl}. */
    public static final Block OPTIONS = build(VkBindings.OPTIONS_BLOCK, 2,
        Spec.of("flw_brightnessOption", Type.FLOAT),
        Spec.of("flw_fovOption", Type.UINT),
        Spec.of("flw_distortionOption", Type.FLOAT),
        Spec.of("flw_glintSpeedOption", Type.FLOAT),
        Spec.of("flw_glintStrengthOption", Type.FLOAT),
        Spec.of("flw_biomeBlendOption", Type.UINT),
        Spec.of("flw_smoothLightingOption", Type.UINT),
        Spec.of("flw_viewBobbingOption", Type.UINT),
        Spec.of("flw_highContrastOption", Type.UINT),
        Spec.of("flw_textBackgroundOpacityOption", Type.FLOAT),
        Spec.of("flw_textBackgroundForChatOnlyOption", Type.UINT),
        Spec.of("flw_darknessPulsingOption", Type.FLOAT),
        Spec.of("flw_damageTiltOption", Type.FLOAT),
        Spec.of("flw_hideLightningFlashesOption", Type.UINT)
    );

    /** {@code _FlwPlayerUniforms}, binding 3. {@code internal/uniforms/player.glsl}. */
    public static final Block PLAYER = build(VkBindings.PLAYER_BLOCK, 3,
        Spec.of("_flw_eyePos", Type.VEC4),
        Spec.of("flw_teamColor", Type.VEC4),
        Spec.of("flw_eyeBrightness", Type.VEC2),
        Spec.of("flw_heldLight", Type.FLOAT),
        Spec.of("flw_playerEyeInFluid", Type.UINT),
        Spec.of("flw_playerEyeInBlock", Type.UINT),
        Spec.of("flw_playerCrouching", Type.UINT),
        Spec.of("flw_playerSleeping", Type.UINT),
        Spec.of("flw_playerSwimming", Type.UINT),
        Spec.of("flw_playerFallFlying", Type.UINT),
        Spec.of("flw_shiftKeyDown", Type.UINT),
        Spec.of("flw_gameMode", Type.UINT)
    );

    /** {@code _FlwLevelUniforms}, binding 4. {@code internal/uniforms/level.glsl}. */
    public static final Block LEVEL = build(VkBindings.LEVEL_BLOCK, 4,
        Spec.of("flw_skyColor", Type.VEC4),
        Spec.of("flw_cloudColor", Type.VEC4),
        Spec.of("_flw_light0Direction", Type.VEC4),
        Spec.of("_flw_light1Direction", Type.VEC4),
        Spec.of("flw_levelDay", Type.UINT),
        Spec.of("flw_timeOfDay", Type.FLOAT),
        Spec.of("flw_levelHasSkyLight", Type.UINT),
        Spec.of("flw_sunAngle", Type.FLOAT),
        Spec.of("flw_moonBrightness", Type.FLOAT),
        Spec.of("flw_moonPhase", Type.UINT),
        Spec.of("flw_isRaining", Type.UINT),
        Spec.of("flw_rainLevel", Type.FLOAT),
        Spec.of("flw_isThundering", Type.UINT),
        Spec.of("flw_thunderLevel", Type.FLOAT),
        Spec.of("flw_skyDarken", Type.FLOAT),
        Spec.of("flw_constantAmbientLight", Type.UINT),
        Spec.of("flw_dimension", Type.UINT)
    );

    /**
     * {@code _FlwDrawUniforms}, binding 5. Not a resource file: Agent B's
     * {@code VkShaderSource.wrapLooseUniforms} synthesises it around the loose
     * non-opaque uniforms of {@code instancing/main.vert} and
     * {@code instancing/main.frag}.
     */
    public static final Block DRAW = build(VkBindings.DRAW_BLOCK, 5,
        Spec.of("_flw_packedMaterial", Type.UVEC2),
        Spec.of("_flw_baseInstance", Type.INT),
        Spec.of("_flw_baseVertex", Type.UINT),
        Spec.of("_flw_modelMatrixUniform", Type.MAT4),
        Spec.of("_flw_normalMatrixUniform", Type.MAT3)
    );

    /** Every block, in binding order. */
    public static final List<Block> ALL = List.of(FRAME, FOG, OPTIONS, PLAYER, LEVEL, DRAW);

    /**
     * Names that are {@code #define}s in GLSL rather than real members, mapped onto the
     * member they expand to. Flywheel's own draw code never uses these - it writes
     * {@code _flw_*} directly - but shader authors do, so accepting them keeps
     * {@link VkUniformBuffer#has(String)} honest.
     */
    private static final Map<String, String> ALIASES;

    private static final Map<String, Block> BY_NAME;
    private static final Map<String, Member> MEMBERS_BY_NAME;

    static {
        var blocks = new LinkedHashMap<String, Block>();
        var members = new LinkedHashMap<String, Member>();

        for (Block block : ALL) {
            blocks.put(block.name(), block);
            for (Member member : block.members()) {
                Member previous = members.put(member.name(), member);
                if (previous != null) {
                    throw new IllegalStateException(
                        "Uniform '" + member.name() + "' is declared in both " + previous.block() + " and "
                            + member.block() + "; the name -> block mapping would be ambiguous."
                    );
                }
            }
        }

        BY_NAME = Collections.unmodifiableMap(blocks);
        MEMBERS_BY_NAME = Collections.unmodifiableMap(members);

        var aliases = new LinkedHashMap<String, String>();
        aliases.put("flw_renderOrigin", "_flw_renderOrigin");
        aliases.put("flw_cameraPos", "_flw_cameraPos");
        aliases.put("flw_cameraPosPrev", "_flw_cameraPosPrev");
        aliases.put("flw_cameraLook", "_flw_cameraLook");
        aliases.put("flw_cameraLookPrev", "_flw_cameraLookPrev");
        aliases.put("flw_eyePos", "_flw_eyePos");
        aliases.put("flw_light0Direction", "_flw_light0Direction");
        aliases.put("flw_light1Direction", "_flw_light1Direction");
        ALIASES = Collections.unmodifiableMap(aliases);

        verify();
    }

    private VkUniformLayout() {
    }

    // ------------------------------------------------------------------
    // Lookup
    // ------------------------------------------------------------------

    /**
     * The member a GLSL uniform name resolves to, following {@code #define} aliases,
     * or {@code null} if the name is not a uniform of any of the six blocks.
     */
    public static Member member(String uniformName) {
        Member direct = MEMBERS_BY_NAME.get(uniformName);
        if (direct != null) {
            return direct;
        }
        String target = ALIASES.get(uniformName);
        return target == null ? null : MEMBERS_BY_NAME.get(target);
    }

    public static Block block(String blockName) {
        return BY_NAME.get(blockName);
    }

    /** Total std140 size of a block in bytes, or {@code -1} if the name is unknown. */
    public static int blockSize(String blockName) {
        Block block = BY_NAME.get(blockName);
        return block == null ? -1 : block.size();
    }

    /** Byte offset of a uniform inside its block, or {@code -1} if it resolves to nothing. */
    public static int offsetOf(String uniformName) {
        Member member = member(uniformName);
        return member == null ? -1 : member.offset();
    }

    /** Total bytes across all six blocks, before the per-block alignment padding. */
    public static int totalSize() {
        int total = 0;
        for (Block block : ALL) {
            total += block.size();
        }
        return total;
    }

    /**
     * A multi-line dump of the whole table, for the log and for eyeballing against the
     * GLSL when a block is edited.
     */
    public static String describe() {
        var sb = new StringBuilder();
        for (Block block : ALL) {
            sb.append(block.name()).append(" @binding ").append(block.binding())
                .append(", size ").append(block.size()).append('\n');
            for (Member member : block.members()) {
                sb.append(String.format("    %-30s %-7s +%4d%n",
                    member.name(), member.type().name().toLowerCase(java.util.Locale.ROOT), member.offset()));
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Construction and self-check
    // ------------------------------------------------------------------

    private record Spec(String name, Type type, int blobSize) {
        static Spec of(String name, Type type) {
            return new Spec(name, type, 0);
        }

        static Spec blob(String name, int size) {
            return new Spec(name, Type.OPAQUE, size);
        }
    }

    /**
     * Walks the member list applying std140 and produces the offsets, so the numbers in
     * the table above are derived rather than typed.
     */
    private static final class Layouter {

        private final String blockName;
        private final List<Member> members = new ArrayList<>();
        private int offset;

        Layouter(String blockName) {
            this.blockName = blockName;
        }

        void add(Spec spec) {
            if (spec.type() == Type.OPAQUE) {
                offset = alignUp(offset, 16);
                members.add(new Member(blockName, spec.name(), Type.OPAQUE, offset));
                offset += spec.blobSize();
            } else {
                Type type = spec.type();
                offset = alignUp(offset, type.align);
                members.add(new Member(blockName, spec.name(), type, offset));
                offset = alignUp(offset + type.size, type.align);
            }
        }
    }

    private static Block build(String blockName, int binding, Spec... specs) {
        var layouter = new Layouter(blockName);
        for (Spec spec : specs) {
            layouter.add(spec);
        }
        return new Block(blockName, binding, alignUp(layouter.offset, 16), layouter.members);
    }

    private static int alignUp(int value, int align) {
        return (value + align - 1) / align * align;
    }

    /**
     * Compares this table against {@link VkBindings} and logs every disagreement.
     *
     * <p>Agent B owns {@code VkBindings} and describes it as the single source of truth
     * for bindings; this class owns the sizes. Rather than silently picking a winner, the
     * mismatch is reported once at class-load so the next build fails loudly in the log
     * instead of producing a descriptor whose range is too small.
     */
    private static void verify() {
        var problems = new ArrayList<String>();

        for (Block block : ALL) {
            int declaredBinding = VkBindings.blockBinding(block.name());
            if (declaredBinding != block.binding()) {
                problems.add(block.name() + ": binding " + block.binding()
                    + " here vs " + declaredBinding + " in VkBindings");
            }

            int declaredSize = VkBindings.blockSize(block.name());
            if (declaredSize != block.size()) {
                problems.add(block.name() + ": size " + block.size()
                    + " here (std140) vs " + declaredSize + " in VkBindings");
            }
        }

        if (problems.isEmpty()) {
            return;
        }

        FlwBackend.LOGGER.warn(
            "[FW/VK] VkUniformLayout disagrees with VkBindings on {} of {} block descriptors. "
                + "The descriptors VkDescriptorLayout hands to VulkanMod carry this class's values, "
                + "so they win; VkBindings should be updated to match. Problems: {}",
            problems.size(), ALL.size(), problems
        );
    }
}