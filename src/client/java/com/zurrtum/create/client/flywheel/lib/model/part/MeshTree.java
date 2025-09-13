package com.zurrtum.create.client.flywheel.lib.model.part;

import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibLink;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.model.SimpleQuadMesh;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import com.zurrtum.create.client.flywheel.lib.vertex.PosTexNormalVertexView;
import com.zurrtum.create.client.flywheel.lib.vertex.VertexView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.NoSuchElementException;

public final class MeshTree {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
    private static final MatrixStack.Entry IDENTITY_POSE = new MatrixStack().peek();
    private static final RendererReloadCache<EntityModelLayer, MeshTree> CACHE = new RendererReloadCache<>(MeshTree::convert);

    @Nullable
    private final Mesh mesh;
    private final ModelTransform initialPose;
    private final MeshTree[] children;
    private final String[] childNames;

    private MeshTree(@Nullable Mesh mesh, ModelTransform initialPose, MeshTree[] children, String[] childNames) {
        this.mesh = mesh;
        this.initialPose = initialPose;
        this.children = children;
        this.childNames = childNames;
    }

    public static MeshTree of(EntityModelLayer layer) {
        return CACHE.get(layer);
    }

    private static MeshTree convert(EntityModelLayer layer) {
        LoadedEntityModels entityModels = MinecraftClient.getInstance().getLoadedEntityModels();
        ModelPart modelPart = entityModels.getModelPart(layer);

        return convert(modelPart, THREAD_LOCAL_OBJECTS.get());
    }

    private static MeshTree convert(ModelPart modelPart, ThreadLocalObjects objects) {
        var modelPartChildren = FlwLibLink.INSTANCE.getModelPartChildren(modelPart);

        String[] childNames = modelPartChildren.keySet().toArray(String[]::new);
        Arrays.sort(childNames);

        MeshTree[] children = new MeshTree[childNames.length];
        for (int i = 0; i < childNames.length; i++) {
            children[i] = convert(modelPartChildren.get(childNames[i]), objects);
        }

        return new MeshTree(compile(modelPart, objects), modelPart.getDefaultTransform(), children, childNames);
    }

    @Nullable
    private static Mesh compile(ModelPart modelPart, ThreadLocalObjects objects) {
        if (modelPart.isEmpty()) {
            return null;
        }

        VertexWriter vertexWriter = objects.vertexWriter;
        FlwLibLink.INSTANCE.compileModelPart(
            modelPart,
            IDENTITY_POSE,
            vertexWriter,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            OverlayTexture.DEFAULT_UV,
            0xFFFFFFFF
        );
        MemoryBlock data = vertexWriter.copyDataAndReset();

        VertexView vertexView = new PosTexNormalVertexView();
        vertexView.load(data);
        return new SimpleQuadMesh(vertexView, "source=MeshTree");
    }

    @Nullable
    public Mesh mesh() {
        return mesh;
    }

    public ModelTransform initialPose() {
        return initialPose;
    }

    public int childCount() {
        return children.length;
    }

    public MeshTree child(int index) {
        return children[index];
    }

    public String childName(int index) {
        return childNames[index];
    }

    public int childIndex(String name) {
        return Arrays.binarySearch(childNames, name);
    }

    public boolean hasChild(String name) {
        return childIndex(name) >= 0;
    }

    @Nullable
    public MeshTree child(String name) {
        int index = childIndex(name);

        if (index < 0) {
            return null;
        }

        return child(index);
    }

    public MeshTree childOrThrow(String name) {
        MeshTree child = child(name);

        if (child == null) {
            throw new NoSuchElementException("Can't find part " + name);
        }

        return child;
    }

    private static class ThreadLocalObjects {
        public final VertexWriter vertexWriter = new VertexWriter();
    }
}