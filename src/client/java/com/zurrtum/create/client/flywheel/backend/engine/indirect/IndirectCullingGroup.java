package com.zurrtum.create.client.flywheel.backend.engine.indirect;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.backend.compile.ContextShader;
import com.zurrtum.create.client.flywheel.backend.compile.IndirectPrograms;
import com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler;
import com.zurrtum.create.client.flywheel.backend.engine.InstancerKey;
import com.zurrtum.create.client.flywheel.backend.engine.MaterialRenderState;
import com.zurrtum.create.client.flywheel.backend.engine.MeshPool;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.backend.gl.GlCompat;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.lib.math.MoreMath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class IndirectCullingGroup<I extends Instance> {
    private static final Comparator<IndirectDraw> DRAW_COMPARATOR = Comparator.comparing(IndirectDraw::isEmbedded)
        .thenComparingInt(IndirectDraw::bias).thenComparingInt(IndirectDraw::indexOfMeshInModel)
        .thenComparing(IndirectDraw::material, MaterialRenderState.COMPARATOR);

    private final InstanceType<I> instanceType;
    private final long instanceStride;
    private final IndirectBuffers buffers;
    private final List<IndirectInstancer<I>> instancers = new ArrayList<>();
    private final List<IndirectDraw> indirectDraws = new ArrayList<>();
    private final List<MultiDraw> multiDraws = new ArrayList<>();
    private final List<MultiDraw> oitDraws = new ArrayList<>();

    private final IndirectPrograms programs;
    private final GlProgram cullProgram;

    private boolean needsDrawBarrier;
    private boolean needsDrawSort;
    private int instanceCountThisFrame;

    IndirectCullingGroup(InstanceType<I> instanceType, IndirectPrograms programs) {
        this.instanceType = instanceType;
        instanceStride = MoreMath.align4(instanceType.layout().byteSize());
        buffers = new IndirectBuffers(instanceStride);

        this.programs = programs;
        cullProgram = programs.getCullingProgram(instanceType);
    }

    public boolean flushInstancers() {
        instanceCountThisFrame = 0;
        int modelIndex = 0;
        for (var iterator = instancers.iterator(); iterator.hasNext(); ) {
            var instancer = iterator.next();
            var instanceCount = instancer.instanceCount();

            if (instanceCount == 0) {
                iterator.remove();
                instancer.delete();
                continue;
            }

            instancer.update(modelIndex, instanceCountThisFrame);
            instanceCountThisFrame += instanceCount;

            modelIndex++;
        }

        if (indirectDraws.removeIf(IndirectDraw::deleted)) {
            needsDrawSort = true;
        }

        var out = indirectDraws.isEmpty();

        if (out) {
            delete();
        }

        return out;
    }

    public void upload(StagingBuffer stagingBuffer) {
        buffers.updateCounts(instanceCountThisFrame, instancers.size(), indirectDraws.size());

        // Upload only instances that have changed.
        uploadInstances(stagingBuffer);

        buffers.objectStorage.uploadDescriptors(stagingBuffer);

        // We need to upload the models every frame to reset the instance count.
        uploadModels(stagingBuffer);

        if (needsDrawSort) {
            sortDraws();
            needsDrawSort = false;
        }

        uploadDraws(stagingBuffer);

        needsDrawBarrier = true;
    }

    public void dispatchCull() {
        Uniforms.bindAll();
        cullProgram.bind();

        buffers.bindForCull();
        glDispatchCompute(buffers.objectStorage.capacity(), 1, 1);
    }

    public void dispatchApply() {
        buffers.bindForApply();
        glDispatchCompute(GlCompat.getComputeGroupCount(indirectDraws.size()), 1, 1);
    }

    public boolean hasOitDraws() {
        return !oitDraws.isEmpty();
    }

    private void sortDraws() {
        multiDraws.clear();
        oitDraws.clear();
        // sort by visual type, then material
        indirectDraws.sort(DRAW_COMPARATOR);

        for (int start = 0, i = 0; i < indirectDraws.size(); i++) {
            var draw1 = indirectDraws.get(i);

            // if the next draw call has a different VisualType or Material, start a new MultiDraw
            if (i == indirectDraws.size() - 1 || incompatibleDraws(draw1, indirectDraws.get(i + 1))) {
                var dst = draw1.material().transparency() == Transparency.ORDER_INDEPENDENT ? oitDraws : multiDraws;
                dst.add(new MultiDraw(draw1.material(), draw1.isEmbedded(), start, i + 1));
                start = i + 1;
            }
        }
    }

    private boolean incompatibleDraws(IndirectDraw draw1, IndirectDraw draw2) {
        if (draw1.isEmbedded() != draw2.isEmbedded()) {
            return true;
        }
        return !MaterialRenderState.materialEquals(draw1.material(), draw2.material());
    }

    public void add(IndirectInstancer<I> instancer, InstancerKey<I> key, MeshPool meshPool) {
        instancer.mapping = buffers.objectStorage.createMapping();
        instancer.update(instancers.size(), -1);

        instancers.add(instancer);

        List<Model.ConfiguredMesh> meshes = key.model().meshes();
        for (int i = 0; i < meshes.size(); i++) {
            var entry = meshes.get(i);

            MeshPool.PooledMesh mesh = meshPool.alloc(entry.mesh());
            var draw = new IndirectDraw(instancer, entry.material(), mesh, key.bias(), i);
            indirectDraws.add(draw);
            instancer.addDraw(draw);
        }

        needsDrawSort = true;
    }

    public void submitSolid() {
        if (multiDraws.isEmpty()) {
            return;
        }

        buffers.bindForDraw();

        drawBarrier();

        GlProgram lastProgram = null;

        for (var multiDraw : multiDraws) {
            var drawProgram = programs.getIndirectProgram(
                instanceType,
                multiDraw.embedded ? ContextShader.EMBEDDED : ContextShader.DEFAULT,
                multiDraw.material,
                PipelineCompiler.OitMode.OFF
            );
            if (drawProgram != lastProgram) {
                lastProgram = drawProgram;

                // Don't need to do this unless the program changes.
                drawProgram.bind();
            }

            MaterialRenderState.setup(multiDraw.material);

            multiDraw.submit(drawProgram);
        }
    }

    public void submitTransparent(PipelineCompiler.OitMode oit) {
        if (oitDraws.isEmpty()) {
            return;
        }

        buffers.bindForDraw();

        drawBarrier();

        GlProgram lastProgram = null;

        for (var multiDraw : oitDraws) {
            var drawProgram = programs.getIndirectProgram(
                instanceType,
                multiDraw.embedded ? ContextShader.EMBEDDED : ContextShader.DEFAULT,
                multiDraw.material,
                oit
            );
            if (drawProgram != lastProgram) {
                lastProgram = drawProgram;

                // Don't need to do this unless the program changes.
                drawProgram.bind();

                drawProgram.setFloat("_flw_blueNoiseFactor", 0.07f);
            }

            MaterialRenderState.setupOit(multiDraw.material);

            multiDraw.submit(drawProgram);
        }
    }

    public void bindForCrumbling(Material material) {
        var program = programs.getIndirectProgram(instanceType, ContextShader.CRUMBLING, material, PipelineCompiler.OitMode.OFF);

        program.bind();

        buffers.bindForCrumbling();

        drawBarrier();

        program.setUInt("_flw_baseDraw", 0);
    }

    private void drawBarrier() {
        if (needsDrawBarrier) {
            // In theory all command buffer writes will be protected by
            // the shader storage barrier bit, but better safe than sorry.
            glMemoryBarrier(GL_COMMAND_BARRIER_BIT);
            needsDrawBarrier = false;
        }
    }

    private void uploadInstances(StagingBuffer stagingBuffer) {
        for (var instancer : instancers) {
            instancer.uploadInstances(stagingBuffer, buffers.objectStorage.objectBuffer.handle());
        }
    }

    private void uploadModels(StagingBuffer stagingBuffer) {
        var totalSize = instancers.size() * IndirectBuffers.MODEL_STRIDE;
        var handle = buffers.model.handle();

        stagingBuffer.enqueueCopy(totalSize, handle, 0, this::writeModels);
    }

    private void uploadDraws(StagingBuffer stagingBuffer) {
        var totalSize = indirectDraws.size() * IndirectBuffers.DRAW_COMMAND_STRIDE;
        var handle = buffers.draw.handle();

        stagingBuffer.enqueueCopy(totalSize, handle, 0, this::writeCommands);
    }

    private void writeModels(long writePtr) {
        for (var model : instancers) {
            model.writeModel(writePtr);
            writePtr += IndirectBuffers.MODEL_STRIDE;
        }
    }

    private void writeCommands(long writePtr) {
        for (var draw : indirectDraws) {
            draw.write(writePtr);
            writePtr += IndirectBuffers.DRAW_COMMAND_STRIDE;
        }
    }

    public void delete() {
        buffers.delete();
    }

    private record MultiDraw(Material material, boolean embedded, int start, int end) {
        private void submit(GlProgram drawProgram) {
            GlCompat.safeMultiDrawElementsIndirect(
                drawProgram,
                GL_TRIANGLES,
                GL_UNSIGNED_INT,
                this.start,
                this.end,
                IndirectBuffers.DRAW_COMMAND_STRIDE
            );
        }
    }
}
