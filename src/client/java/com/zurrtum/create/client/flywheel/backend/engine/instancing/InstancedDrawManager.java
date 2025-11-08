package com.zurrtum.create.client.flywheel.backend.engine.instancing;

import com.zurrtum.create.client.flywheel.api.backend.Engine;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.compile.ContextShader;
import com.zurrtum.create.client.flywheel.backend.compile.InstancingPrograms;
import com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler;
import com.zurrtum.create.client.flywheel.backend.engine.*;
import com.zurrtum.create.client.flywheel.backend.engine.embed.EnvironmentStorage;
import com.zurrtum.create.client.flywheel.backend.engine.indirect.OitFramebuffer;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.backend.gl.TextureBuffer;
import com.zurrtum.create.client.flywheel.backend.gl.array.GlVertexArray;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelBaker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstancedDrawManager extends DrawManager<InstancedInstancer<?>> {
    private static final Comparator<InstancedDraw> DRAW_COMPARATOR = Comparator.comparingInt(InstancedDraw::bias)
        .thenComparingInt(InstancedDraw::indexOfMeshInModel).thenComparing(InstancedDraw::material, MaterialRenderState.COMPARATOR);

    private final List<InstancedDraw> allDraws = new ArrayList<>();
    private boolean needSort = false;

    private final List<InstancedDraw> draws = new ArrayList<>();
    private final List<InstancedDraw> oitDraws = new ArrayList<>();

    private final InstancingPrograms programs;
    /**
     * A map of vertex types to their mesh pools.
     */
    private final MeshPool meshPool;
    private final GlVertexArray vao;
    private final TextureBuffer instanceTexture;
    private final InstancedLight light;

    private final OitFramebuffer oitFramebuffer;

    public InstancedDrawManager(InstancingPrograms programs) {
        programs.acquire();
        this.programs = programs;

        meshPool = new MeshPool();
        vao = GlVertexArray.create();
        instanceTexture = new TextureBuffer();
        light = new InstancedLight();

        meshPool.bind(vao);

        oitFramebuffer = new OitFramebuffer(programs.oitPrograms());

    }

    @Override
    public void render(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
        super.render(lightStorage, environmentStorage);

        this.instancers.values().removeIf(instancer -> {
            if (instancer.instanceCount() == 0) {
                instancer.delete();
                return true;
            } else {
                instancer.updateBuffer();
                return false;
            }
        });

        // Remove the draw calls for any instancers we deleted.
        needSort |= allDraws.removeIf(InstancedDraw::deleted);

        if (needSort) {
            allDraws.sort(DRAW_COMPARATOR);

            draws.clear();
            oitDraws.clear();

            for (var draw : allDraws) {
                if (draw.material().transparency() == Transparency.ORDER_INDEPENDENT) {
                    oitDraws.add(draw);
                } else {
                    draws.add(draw);
                }
            }

            needSort = false;
        }

        meshPool.flush();

        light.flush(lightStorage);

        if (allDraws.isEmpty()) {
            return;
        }

        Uniforms.bindAll();
        vao.bindForDraw();
        TextureBinder.bindLightAndOverlay();
        light.bind();

        MaterialRenderState.setupFrameBuffer();
        submitDraws();

        if (!oitDraws.isEmpty()) {
            oitFramebuffer.prepare();

            oitFramebuffer.depthRange();

            submitOitDraws(PipelineCompiler.OitMode.DEPTH_RANGE);

            oitFramebuffer.renderTransmittance();

            submitOitDraws(PipelineCompiler.OitMode.GENERATE_COEFFICIENTS);

            oitFramebuffer.renderDepthFromTransmittance();

            // Need to bind this again because we just drew a full screen quad for OIT.
            vao.bindForDraw();

            oitFramebuffer.accumulate();

            submitOitDraws(PipelineCompiler.OitMode.EVALUATE);

            oitFramebuffer.composite();
        }

        MaterialRenderState.reset();
        TextureBinder.resetLightAndOverlay();
    }

    private void submitDraws() {
        for (var drawCall : draws) {
            var material = drawCall.material();
            var groupKey = drawCall.groupKey;
            var environment = groupKey.environment();

            var program = programs.get(groupKey.instanceType(), environment.contextShader(), material, PipelineCompiler.OitMode.OFF);
            program.bind();

            environment.setupDraw(program);

            uploadMaterialUniform(program, material);

            program.setUInt("_flw_vertexOffset", drawCall.mesh().baseVertex());

            MaterialRenderState.setup(material);

            Samplers.INSTANCE_BUFFER.makeActive();

            drawCall.render(instanceTexture);
        }
    }

    private void submitOitDraws(PipelineCompiler.OitMode mode) {
        for (var drawCall : oitDraws) {
            var material = drawCall.material();
            var groupKey = drawCall.groupKey;
            var environment = groupKey.environment();

            var program = programs.get(groupKey.instanceType(), environment.contextShader(), material, mode);
            program.bind();

            environment.setupDraw(program);

            uploadMaterialUniform(program, material);

            program.setUInt("_flw_vertexOffset", drawCall.mesh().baseVertex());

            MaterialRenderState.setupOit(material);

            Samplers.INSTANCE_BUFFER.makeActive();

            drawCall.render(instanceTexture);
        }
    }

    @Override
    public void delete() {
        instancers.values().forEach(InstancedInstancer::delete);

        allDraws.forEach(InstancedDraw::delete);
        allDraws.clear();
        draws.clear();
        oitDraws.clear();

        meshPool.delete();
        instanceTexture.delete();
        programs.release();
        vao.delete();

        light.delete();

        oitFramebuffer.delete();

        super.delete();
    }

    @Override
    protected <I extends Instance> InstancedInstancer<I> create(InstancerKey<I> key) {
        return new InstancedInstancer<>(key, new AbstractInstancer.Recreate<>(key, this));
    }

    @Override
    protected <I extends Instance> void initialize(InstancerKey<I> key, InstancedInstancer<?> instancer) {
        instancer.init();

        var meshes = key.model().meshes();
        for (int i = 0; i < meshes.size(); i++) {
            var entry = meshes.get(i);
            var mesh = meshPool.alloc(entry.mesh());

            GroupKey<?> groupKey = new GroupKey<>(key.type(), key.environment());
            InstancedDraw instancedDraw = new InstancedDraw(instancer, mesh, groupKey, entry.material(), key.bias(), i);

            allDraws.add(instancedDraw);
            needSort = true;
            instancer.addDrawCall(instancedDraw);
        }
    }

    @Override
    public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
        // Sort draw calls into buckets, so we don't have to do as many shader binds.
        var byType = doCrumblingSort(
            crumblingBlocks, handle -> {
                // AbstractInstancer directly implement HandleState, so this check is valid.
                if (handle instanceof InstancedInstancer<?> instancer) {
                    return instancer;
                }
                // This rejects instances that were created by a different engine,
                // and also instances that are hidden or deleted.
                return null;
            }
        );

        if (byType.isEmpty()) {
            return;
        }

        var crumblingMaterial = SimpleMaterial.builder();

        Uniforms.bindAll();
        vao.bindForDraw();
        TextureBinder.bindLightAndOverlay();

        MaterialRenderState.setupFrameBuffer();

        for (var groupEntry : byType.entrySet()) {
            var byProgress = groupEntry.getValue();

            GroupKey<?> shader = groupEntry.getKey();

            for (var progressEntry : byProgress.int2ObjectEntrySet()) {
                TextureBinder.bindCrumbling(ModelBaker.BLOCK_DESTRUCTION_STAGE_TEXTURES.get(progressEntry.getIntKey()));

                for (var instanceHandlePair : progressEntry.getValue()) {
                    InstancedInstancer<?> instancer = instanceHandlePair.getFirst();
                    var index = instanceHandlePair.getSecond().index;

                    for (InstancedDraw draw : instancer.draws()) {
                        CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());
                        var program = programs.get(shader.instanceType(), ContextShader.CRUMBLING, crumblingMaterial, PipelineCompiler.OitMode.OFF);
                        program.bind();
                        program.setInt("_flw_baseInstance", index);
                        uploadMaterialUniform(program, crumblingMaterial);

                        MaterialRenderState.setup(crumblingMaterial);

                        Samplers.INSTANCE_BUFFER.makeActive();

                        draw.renderOne(instanceTexture);
                    }
                }
            }
        }

        MaterialRenderState.reset();
        TextureBinder.resetLightAndOverlay();
    }

    @Override
    public void triggerFallback() {
        InstancingPrograms.kill();
        MinecraftClient.getInstance().worldRenderer.reload();
    }

    public static void uploadMaterialUniform(GlProgram program, Material material) {
        int packedFogAndCutout = MaterialEncoder.packUberShader(material);
        int packedMaterialProperties = MaterialEncoder.packProperties(material);
        program.setUVec2("_flw_packedMaterial", packedFogAndCutout, packedMaterialProperties);
    }
}
