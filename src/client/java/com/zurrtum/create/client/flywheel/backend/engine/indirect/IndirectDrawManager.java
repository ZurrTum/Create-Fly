package com.zurrtum.create.client.flywheel.backend.engine.indirect;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.backend.compile.IndirectPrograms;
import com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler.OitMode;
import com.zurrtum.create.client.flywheel.backend.engine.*;
import com.zurrtum.create.client.flywheel.backend.engine.embed.EnvironmentStorage;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.backend.gl.array.GlVertexArray;
import com.zurrtum.create.client.flywheel.backend.gl.buffer.GlBuffer;
import com.zurrtum.create.client.flywheel.backend.gl.buffer.GlBufferType;
import com.zurrtum.create.client.flywheel.backend.gl.buffer.GlBufferUsage;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.oit.OitStage;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL40.glDrawElementsIndirect;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class IndirectDrawManager extends DrawManager<IndirectInstancer<?>> {
    private final IndirectPrograms programs;
    private final StagingBuffer stagingBuffer;
    private final MeshPool meshPool;
    private final GlVertexArray vertexArray;
    private final Map<InstanceType<?>, IndirectCullingGroup<?>> cullingGroups = new HashMap<>();
    private final GlBuffer crumblingDrawBuffer = new GlBuffer(GlBufferUsage.STREAM_DRAW);
    private final LightBuffers lightBuffers;
    private final MatrixBuffer matrixBuffer;

    private final DepthPyramid depthPyramid;

    private final OitFramebuffer oitFramebuffer;
    private boolean useOit;

    public IndirectDrawManager(IndirectPrograms programs) {
        this.programs = programs;
        programs.acquire();

        // WARN: We should avoid eagerly grabbing GlPrograms here as catching compile
        // errors and falling back during construction is a bit more complicated.
        stagingBuffer = new StagingBuffer(this.programs);
        meshPool = new MeshPool();
        vertexArray = GlVertexArray.create();
        meshPool.bind(vertexArray);
        lightBuffers = new LightBuffers();
        matrixBuffer = new MatrixBuffer();

        depthPyramid = new DepthPyramid(programs);

        oitFramebuffer = new OitFramebuffer(programs.oitPrograms());
        crumblingDrawBuffer.upload(MemoryUtil.NULL, IndirectBuffers.DRAW_COMMAND_STRIDE);
    }

    @Override
    protected <I extends Instance> IndirectInstancer<?> create(InstancerKey<I> key) {
        return new IndirectInstancer<>(key, new AbstractInstancer.Recreate<>(key, this));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <I extends Instance> void initialize(
        InstancerKey<I> key,
        IndirectInstancer<?> instancer,
        GpuTextureManager textureManager
    ) {
        var group = (IndirectCullingGroup<I>) cullingGroups.computeIfAbsent(
            key.type(),
            t -> new IndirectCullingGroup<>(t, programs, textureManager)
        );
        group.add((IndirectInstancer<I>) instancer, key, meshPool);
    }

    @Override
    public void render(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
        // Flush instance counts, page mappings, and prune empty groups.
        cullingGroups.values().removeIf(IndirectCullingGroup::flushInstancers);

        // Instancers may have been emptied in the above call, now remove them here.
        instancers.values().removeIf(instancer -> instancer.instanceCount() == 0);

        meshPool.flush();

        stagingBuffer.reclaim();

        // Genuinely nothing to do, we can just early out.
        // Still process the mesh pool and reclaim fenced staging regions though.
        if (cullingGroups.isEmpty()) {
            return;
        }

        lightBuffers.flush(stagingBuffer, lightStorage);

        matrixBuffer.flush(stagingBuffer, environmentStorage);

        for (var group : cullingGroups.values()) {
            group.upload(stagingBuffer);
        }

        stagingBuffer.flush();

        depthPyramid.generate();

        // We could probably save some driver calls here when there are
        // actually zero instances, but that feels like a very rare case

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        matrixBuffer.bind();

        depthPyramid.bindForCull();

        for (var group : cullingGroups.values()) {
            group.dispatchCull();
        }

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        programs.getApplyProgram().bind();

        for (var group : cullingGroups.values()) {
            group.dispatchApply();
        }

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        TextureBinder.bindLightAndOverlay();

        vertexArray.bindForDraw();
        lightBuffers.bind();
        matrixBuffer.bind();
        Uniforms.bindAll();

        for (var group : cullingGroups.values()) {
            group.submitSolid();
        }

        // Let's avoid invoking the oit chain if we don't have anything to do
        for (var group : cullingGroups.values()) {
            if (group.hasOitDraws()) {
                useOit = true;
                return;
            }
        }
        useOit = false;
    }

    @Override
    public void renderClassicTransparency() {
        if (useOit) {
            oitFramebuffer.prepare();

            oitFramebuffer.depthRange();

            for (var group : cullingGroups.values()) {
                group.submitTransparent(OitMode.DEPTH_RANGE);
            }

            oitFramebuffer.renderTransmittance();

            for (var group : cullingGroups.values()) {
                group.submitTransparent(OitMode.GENERATE_COEFFICIENTS);
            }

            oitFramebuffer.renderDepthFromTransmittance();

            // Need to bind this again because we just drew a full screen quad for OIT.
            vertexArray.bindForDraw();

            oitFramebuffer.accumulate();

            for (var group : cullingGroups.values()) {
                group.submitTransparent(OitMode.EVALUATE);
            }

            oitFramebuffer.composite();
        }
    }

    @Override
    public void renderOit(
        OitStage stage,
        GpuTextureView depthBoundsCulledTargetView,
        GpuTextureView[] transmittanceTargetViews,
        CrumblingBlockManager crumblingBlockManager
    ) {
        if (useOit) {
            TextureBinder.bindLightAndOverlay();
            vertexArray.bindForDraw();
            lightBuffers.bind();
            matrixBuffer.bind();
            Uniforms.bindAll();
            switch (stage) {
                case DEPTH_BOUNDS -> {
                    for (var group : cullingGroups.values()) {
                        OitFramebuffer.vanillaDepthRange();
                        group.submitTransparent(OitMode.VANILLA_DEPTH_BOUNDS);
                    }
                    renderOitCrumbling(crumblingBlockManager, OitMode.VANILLA_DEPTH_BOUNDS);
                }
                case TRANSMITTANCE -> {
                    OitFramebuffer.vanillaTransmittance(depthBoundsCulledTargetView);
                    for (var group : cullingGroups.values()) {
                        group.submitTransparent(OitMode.VANILLA_TRANSMITTANCE);
                    }
                    renderOitCrumbling(crumblingBlockManager, OitMode.VANILLA_TRANSMITTANCE);
                }
                case ACCUMULATE -> {
                    OitFramebuffer.vanillaAccumulate(depthBoundsCulledTargetView, transmittanceTargetViews);
                    for (var group : cullingGroups.values()) {
                        group.submitTransparent(OitMode.VANILLA_ACCUMULATE);
                    }
                    renderOitCrumbling(crumblingBlockManager, OitMode.VANILLA_ACCUMULATE);
                }
            }
        } else if (stage == OitStage.ACCUMULATE && crumblingBlockManager.hasOitCrumbling()) {
            crumblingBlockManager.drainOit((shader, map) -> map.clearOwned());
        }
    }

    @Override
    public void delete() {
        super.delete();

        cullingGroups.values().forEach(IndirectCullingGroup::delete);
        cullingGroups.clear();

        stagingBuffer.delete();

        meshPool.delete();

        crumblingDrawBuffer.delete();

        programs.release();

        depthPyramid.delete();

        lightBuffers.delete();

        matrixBuffer.delete();

        oitFramebuffer.delete();
    }

    @Override
    public @Nullable IndirectInstancer<?> cast(InstanceHandleImpl.State<?> handle) {
        return IndirectInstancer.fromState(handle);
    }

    @Override
    public void renderCrumbling(CrumblingBlockManager crumblingBlockManager, RenderContext context) {
        if (crumblingBlockManager.hasCrumbling()) {
            TextureBinder.bindLightAndOverlay();

            vertexArray.bindForDraw();
            Uniforms.bindAll();

            var crumblingMaterial = SimpleMaterial.builder();

            // Scratch memory for writing draw commands.
            var block = MemoryBlock.malloc(IndirectBuffers.DRAW_COMMAND_STRIDE);

            // Set up the crumbling program buffers. Nothing changes here between draws.
            int handle = crumblingDrawBuffer.handle();
            GlBufferType.DRAW_INDIRECT_BUFFER.bind(handle);
            glBindBufferRange(
                GL_SHADER_STORAGE_BUFFER,
                BufferBindings.DRAW,
                handle,
                0,
                IndirectBuffers.DRAW_COMMAND_STRIDE
            );
            if (context.useClassicTransparency()) {
                crumblingBlockManager.drain((shader, map) -> {
                    IndirectCullingGroup<?> cullingGroup = cullingGroups.get(shader.instanceType());
                    if (cullingGroup == null) {
                        map.clear();
                        return;
                    }
                    map.<IndirectDraw>drain((index, draw) -> renderCrumbling(
                        crumblingDrawBuffer,
                        block,
                        crumblingMaterial,
                        cullingGroup,
                        index,
                        draw
                    ));
                });
            } else {
                crumblingBlockManager.drain((shader, map) -> {
                    IndirectCullingGroup<?> cullingGroup = cullingGroups.get(shader.instanceType());
                    if (cullingGroup == null) {
                        map.clearOwned();
                        return;
                    }
                    map.<IndirectDraw>drainOwned((index, draw) -> renderCrumbling(
                        crumblingDrawBuffer,
                        block,
                        crumblingMaterial,
                        cullingGroup,
                        index,
                        draw
                    ));
                });
            }
            block.free();
        }
    }

    private void renderOitCrumbling(CrumblingBlockManager crumblingBlockManager, OitMode mode) {
        if (crumblingBlockManager.hasOitCrumbling()) {
            TextureBinder.bindLightAndOverlay();

            vertexArray.bindForDraw();
            Uniforms.bindAll();

            var crumblingMaterial = SimpleMaterial.builder();

            // Scratch memory for writing draw commands.
            var block = MemoryBlock.malloc(IndirectBuffers.DRAW_COMMAND_STRIDE);

            // Set up the crumbling program buffers. Nothing changes here between draws.
            int handle = crumblingDrawBuffer.handle();
            GlBufferType.DRAW_INDIRECT_BUFFER.bind(handle);
            glBindBufferRange(
                GL_SHADER_STORAGE_BUFFER,
                BufferBindings.DRAW,
                handle,
                0,
                IndirectBuffers.DRAW_COMMAND_STRIDE
            );
            if (mode != OitMode.VANILLA_ACCUMULATE) {
                crumblingBlockManager.forEachOit((shader, map) -> {
                    IndirectCullingGroup<?> cullingGroup = cullingGroups.get(shader.instanceType());
                    if (cullingGroup == null) {
                        return;
                    }
                    map.<IndirectDraw>forEach((index, draw) -> renderOitCrumbling(
                        crumblingDrawBuffer,
                        block,
                        mode,
                        crumblingMaterial,
                        cullingGroup,
                        index,
                        draw
                    ));
                });
            } else {
                crumblingBlockManager.drainOit((shader, map) -> {
                    IndirectCullingGroup<?> cullingGroup = cullingGroups.get(shader.instanceType());
                    if (cullingGroup == null) {
                        map.clearOwned();
                        return;
                    }
                    map.<IndirectDraw>drainOwned((index, draw) -> renderOitCrumbling(
                        crumblingDrawBuffer,
                        block,
                        mode,
                        crumblingMaterial,
                        cullingGroup,
                        index,
                        draw
                    ));
                });
            }
            block.free();
        }
    }

    private static void renderCrumbling(
        GlBuffer crumblingDrawBuffer,
        MemoryBlock block,
        SimpleMaterial.Builder crumblingMaterial,
        IndirectCullingGroup<?> cullingGroup,
        int index,
        IndirectDraw draw
    ) {
        // Transform the material to be suited for crumbling.
        CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());
        cullingGroup.bindForCrumbling(crumblingMaterial, OitMode.OFF);
        MaterialRenderState.setup(crumblingMaterial);
        // Upload the draw command.
        draw.writeWithOverrides(block.ptr(), index, crumblingMaterial);
        crumblingDrawBuffer.uploadSpan(0, block);
        // Submit! Everything is already bound by here.
        glDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0);
    }

    private static void renderOitCrumbling(
        GlBuffer crumblingDrawBuffer,
        MemoryBlock block,
        OitMode mode,
        SimpleMaterial.Builder crumblingMaterial,
        IndirectCullingGroup<?> cullingGroup,
        int index,
        IndirectDraw draw
    ) {
        // Transform the material to be suited for crumbling.
        CommonCrumbling.applyOitCrumblingProperties(crumblingMaterial, draw.material());
        cullingGroup.bindForCrumbling(crumblingMaterial, mode);
        MaterialRenderState.setupOit(crumblingMaterial);
        // Upload the draw command.
        draw.writeWithOverrides(block.ptr(), index, crumblingMaterial);
        crumblingDrawBuffer.uploadSpan(0, block);
        // Submit! Everything is already bound by here.
        glDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0);
    }

    @Override
    public void triggerFallback() {
        IndirectPrograms.kill();
        Minecraft.getInstance().levelExtractor.allChanged();
    }

    @Override
    public MeshPool meshPool() {
        return meshPool;
    }
}
