package com.zurrtum.create.client.flywheel.backend.engine.embed;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualEmbedding;
import com.zurrtum.create.client.flywheel.backend.compile.ContextShader;
import com.zurrtum.create.client.flywheel.backend.engine.EngineImpl;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.lib.util.ExtraMemoryOps;
import net.minecraft.core.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public class EmbeddedEnvironment implements VisualEmbedding, Environment {
    private final EngineImpl engine;
    private final Vec3i renderOrigin;
    @Nullable
    private final EmbeddedEnvironment parent;
    private final InstancerProvider instancerProvider;

    private final Matrix4f pose = new Matrix4f();
    private final Matrix3f normal = new Matrix3f();
    private final Matrix4f poseComposed = new Matrix4f();
    private final Matrix3f normalComposed = new Matrix3f();

    public int matrixIndex = 0;

    private boolean deleted = false;

    public EmbeddedEnvironment(EngineImpl engine, Vec3i renderOrigin, @Nullable EmbeddedEnvironment parent) {
        this.engine = engine;
        this.renderOrigin = renderOrigin;
        this.parent = parent;

        instancerProvider = new InstancerProvider() {
            @Override
            public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, int bias) {
                // Kinda cursed usage of anonymous classes here, but it does the job.
                return engine.instancer(EmbeddedEnvironment.this, type, model, bias);
            }
        };
    }

    public EmbeddedEnvironment(EngineImpl engine, Vec3i renderOrigin) {
        this(engine, renderOrigin, null);
    }

    @Override
    public void transforms(Matrix4fc pose, Matrix3fc normal) {
        this.pose.set(pose);
        this.normal.set(normal);
    }

    @Override
    public InstancerProvider instancerProvider() {
        return instancerProvider;
    }

    @Override
    public Vec3i renderOrigin() {
        return renderOrigin;
    }

    @Override
    public VisualEmbedding createEmbedding(Vec3i renderOrigin) {
        var out = new EmbeddedEnvironment(engine, renderOrigin, this);
        engine.environmentStorage().track(out);
        return out;
    }

    @Override
    public ContextShader contextShader() {
        return ContextShader.EMBEDDED;
    }

    @Override
    public void setupDraw(GlProgram program) {
        program.setMat4(EmbeddingUniforms.MODEL_MATRIX, poseComposed);
        program.setMat3(EmbeddingUniforms.NORMAL_MATRIX, normalComposed);
    }

    @Override
    public int matrixIndex() {
        return matrixIndex;
    }

    public void flush(long ptr) {
        poseComposed.identity();
        normalComposed.identity();

        composeMatrices(poseComposed, normalComposed);

        ExtraMemoryOps.putMatrix4f(ptr, poseComposed);
        ExtraMemoryOps.putMatrix3fPadded(ptr + 16 * Float.BYTES, normalComposed);
    }

    private void composeMatrices(Matrix4f pose, Matrix3f normal) {
        if (parent != null) {
            parent.composeMatrices(pose, normal);
            pose.mul(this.pose);
            normal.mul(this.normal);
        } else {
            pose.set(this.pose);
            normal.set(this.normal);
        }
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Called by visuals
     */
    @Override
    public void delete() {
        deleted = true;
    }
}
