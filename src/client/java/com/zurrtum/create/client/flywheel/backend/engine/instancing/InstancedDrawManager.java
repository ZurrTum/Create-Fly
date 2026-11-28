package com.zurrtum.create.client.flywheel.backend.engine.instancing;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.compile.ContextShader;
import com.zurrtum.create.client.flywheel.backend.compile.InstancingPrograms;
import com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler.OitMode;
import com.zurrtum.create.client.flywheel.backend.engine.*;
import com.zurrtum.create.client.flywheel.backend.engine.embed.EnvironmentStorage;
import com.zurrtum.create.client.flywheel.backend.engine.indirect.OitFramebuffer;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.backend.gl.TextureBuffer;
import com.zurrtum.create.client.flywheel.backend.gl.array.GlVertexArray;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.oit.OitStage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstancedDrawManager extends DrawManager<InstancedInstancer<?>> {
    private static final Comparator<InstancedDraw> DRAW_COMPARATOR = Comparator.comparingInt(InstancedDraw::bias)
        .thenComparingInt(InstancedDraw::indexOfMeshInModel)
        .thenComparing(InstancedDraw::material, MaterialRenderState.COMPARATOR);

    private final List<InstancedDraw> allDraws = new ArrayList<>();
    private boolean needSort;

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
        instancers.values().removeIf(instancer -> {
            if (instancer.instanceCount() == 0) {
                instancer.delete();
                return true;
            }
            instancer.updateBuffer();
            return false;
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

        submitDraws();
    }

    @Override
    public void renderClassicTransparency() {
        if (!oitDraws.isEmpty()) {
            oitFramebuffer.prepare();

            oitFramebuffer.depthRange();

            submitOitDraws(OitMode.DEPTH_RANGE);

            oitFramebuffer.renderTransmittance();

            submitOitDraws(OitMode.GENERATE_COEFFICIENTS);

            oitFramebuffer.renderDepthFromTransmittance();

            // Need to bind this again because we just drew a full screen quad for OIT.
            vao.bindForDraw();

            oitFramebuffer.accumulate();

            submitOitDraws(OitMode.EVALUATE);

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
        if (oitDraws.isEmpty()) {
            if (stage == OitStage.ACCUMULATE && crumblingBlockManager.hasOitCrumbling()) {
                crumblingBlockManager.drainOit((shader, map) -> map.clearOwned());
            }
            return;
        }
        Uniforms.bindAll();
        vao.bindForDraw();
        TextureBinder.bindLightAndOverlay();
        light.bind();
        switch (stage) {
            case DEPTH_BOUNDS -> {
                OitFramebuffer.vanillaDepthRange();
                submitOitDraws(OitMode.VANILLA_DEPTH_BOUNDS);
                renderOitCrumbling(crumblingBlockManager, OitMode.VANILLA_DEPTH_BOUNDS);
            }
            case TRANSMITTANCE -> {
                OitFramebuffer.vanillaTransmittance(depthBoundsCulledTargetView);
                submitOitDraws(OitMode.VANILLA_TRANSMITTANCE);
                renderOitCrumbling(crumblingBlockManager, OitMode.VANILLA_TRANSMITTANCE);
            }
            case ACCUMULATE -> {
                OitFramebuffer.vanillaAccumulate(depthBoundsCulledTargetView, transmittanceTargetViews);
                submitOitDraws(OitMode.VANILLA_ACCUMULATE);
                renderOitCrumbling(crumblingBlockManager, OitMode.VANILLA_ACCUMULATE);
            }
        }
    }

    private void submitDraws() {
        for (var drawCall : draws) {
            var material = drawCall.material();
            var groupKey = drawCall.groupKey;
            var environment = groupKey.environment();

            var program = programs.get(groupKey.instanceType(), environment.contextShader(), material, OitMode.OFF);
            program.bind();

            environment.setupDraw(program);

            uploadMaterialUniform(program, material);

            program.setUInt("_flw_vertexOffset", drawCall.mesh().baseVertex());

            MaterialRenderState.setup(material);

            Samplers.INSTANCE_BUFFER.makeActive();

            drawCall.render(instanceTexture);
        }
    }

    private void submitOitDraws(OitMode mode) {
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
    protected <I extends Instance> void initialize(
        InstancerKey<I> key,
        InstancedInstancer<?> instancer,
        GpuTextureManager textureManager
    ) {
        instancer.init();

        var meshes = key.model().meshes();
        for (int i = 0; i < meshes.size(); i++) {
            var entry = meshes.get(i);
            var mesh = meshPool.alloc(entry.mesh());

            GroupKey groupKey = new GroupKey(key.type(), key.environment());
            InstancedDraw instancedDraw = new InstancedDraw(
                instancer,
                mesh,
                groupKey,
                textureManager,
                entry.material(),
                key.bias(),
                i
            );

            allDraws.add(instancedDraw);
            needSort = true;
            instancer.addDrawCall(instancedDraw);
        }
    }

    @Override
    public @Nullable InstancedInstancer<?> cast(InstanceHandleImpl.State<?> handle) {
        // AbstractInstancer directly implement HandleState, so this check is valid.
        if (handle instanceof InstancedInstancer<?> instancer) {
            return instancer;
        }
        // This rejects instances that were created by a different engine,
        // and also instances that are hidden or deleted.
        return null;
    }

    @Override
    public void renderCrumbling(CrumblingBlockManager crumblingBlockManager, RenderContext context) {
        if (crumblingBlockManager.hasCrumbling()) {
            var crumblingMaterial = SimpleMaterial.builder();
            Uniforms.bindAll();
            vao.bindForDraw();
            TextureBinder.bindLightAndOverlay();
            if (context.useClassicTransparency()) {
                crumblingBlockManager.drain((shader, map) -> {
                    InstanceType<?> instanceType = shader.instanceType();
                    map.<InstancedDraw>drain((index, draw) -> renderCrumbling(
                        programs,
                        instanceTexture,
                        crumblingMaterial,
                        instanceType,
                        index,
                        draw
                    ));
                });
            } else {
                crumblingBlockManager.drain((shader, map) -> {
                    InstanceType<?> instanceType = shader.instanceType();
                    map.<InstancedDraw>drainOwned((index, draw) -> renderCrumbling(
                        programs,
                        instanceTexture,
                        crumblingMaterial,
                        instanceType,
                        index,
                        draw
                    ));
                });
            }
        }
    }

    private void renderOitCrumbling(CrumblingBlockManager crumblingBlockManager, OitMode mode) {
        if (crumblingBlockManager.hasOitCrumbling()) {
            var crumblingMaterial = SimpleMaterial.builder();
            Uniforms.bindAll();
            vao.bindForDraw();
            TextureBinder.bindLightAndOverlay();
            if (mode != OitMode.VANILLA_ACCUMULATE) {
                crumblingBlockManager.forEachOit((shader, map) -> {
                    InstanceType<?> instanceType = shader.instanceType();
                    map.<InstancedDraw>forEach((index, draw) -> renderOitCrumbling(
                        programs,
                        instanceTexture,
                        mode,
                        crumblingMaterial,
                        instanceType,
                        index,
                        draw
                    ));
                });
            } else {
                crumblingBlockManager.drainOit((shader, map) -> {
                    InstanceType<?> instanceType = shader.instanceType();
                    map.<InstancedDraw>drainOwned((index, draw) -> renderOitCrumbling(
                        programs,
                        instanceTexture,
                        mode,
                        crumblingMaterial,
                        instanceType,
                        index,
                        draw
                    ));
                });
            }
        }
    }

    private static void renderCrumbling(
        InstancingPrograms programs,
        TextureBuffer instanceTexture,
        SimpleMaterial.Builder crumblingMaterial,
        InstanceType<?> instanceType,
        int index,
        InstancedDraw draw
    ) {
        CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());
        bindForCrumbling(programs, OitMode.OFF, crumblingMaterial, instanceType, index);
        MaterialRenderState.setup(crumblingMaterial);
        Samplers.INSTANCE_BUFFER.makeActive();
        draw.renderOne(instanceTexture);
    }

    private static void renderOitCrumbling(
        InstancingPrograms programs,
        TextureBuffer instanceTexture,
        OitMode mode,
        SimpleMaterial.Builder crumblingMaterial,
        InstanceType<?> instanceType,
        int index,
        InstancedDraw draw
    ) {
        CommonCrumbling.applyOitCrumblingProperties(crumblingMaterial, draw.material());
        bindForCrumbling(programs, mode, crumblingMaterial, instanceType, index);
        MaterialRenderState.setupOit(crumblingMaterial);
        Samplers.INSTANCE_BUFFER.makeActive();
        draw.renderOne(instanceTexture);
    }

    private static void bindForCrumbling(
        InstancingPrograms programs,
        OitMode mode,
        SimpleMaterial.Builder crumblingMaterial,
        InstanceType<?> instanceType,
        int index
    ) {
        var program = programs.get(instanceType, ContextShader.CRUMBLING, crumblingMaterial, mode);
        program.bind();
        program.setInt("_flw_baseInstance", index);
        uploadMaterialUniform(program, crumblingMaterial);
    }

    @Override
    public void triggerFallback() {
        InstancingPrograms.kill();
        Minecraft.getInstance().levelExtractor.allChanged();
    }

    @Override
    public MeshPool meshPool() {
        return meshPool;
    }

    public static void uploadMaterialUniform(GlProgram program, Material material) {
        int packedFogAndCutout = MaterialEncoder.packUberShader(material);
        int packedMaterialProperties = MaterialEncoder.packProperties(material);
        program.setUVec2("_flw_packedMaterial", packedFogAndCutout, packedMaterialProperties);
    }
}
