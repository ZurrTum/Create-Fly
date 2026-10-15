package com.zurrtum.create.client.flywheel.backend.engine;

/**
 * A no-op scope for code that only needs to run between "start of our draws" and "after our
 * draws".
 *
 * <p>The OpenGL backend needs this to bracket its work with {@code GlStateTracker.getRestoreState()},
 * which snapshots and restores bound VAOs, buffers and the active texture unit. That class reads
 * {@code GlStateManager} fields and calls into it directly, so merely <em>constructing</em> a
 * restore-state touches OpenGL.
 *
 * <p>On a non-OpenGL backend that is fatal rather than merely wasteful: {@code GlStateManager}'s
 * function pointer table is null, so the access throws, and because the surrounding
 * {@code EngineImpl#render} catches it and reports a fallback, the draw manager is never reached
 * again - the symptom is a world that renders normally but with no instanced geometry at all,
 * and no error explaining why.
 *
 * <p>There is no equivalent state to restore here: Vulkan keeps pipeline and descriptor bindings
 * on the command buffer, and {@code VRenderSystem} tracks them per-pipeline itself.
 */
public final class NoOpRenderScope implements AutoCloseable {

    public static final NoOpRenderScope INSTANCE = new NoOpRenderScope();

    private NoOpRenderScope() {
    }

    @Override
    public void close() {
        // Nothing to restore.
    }
}