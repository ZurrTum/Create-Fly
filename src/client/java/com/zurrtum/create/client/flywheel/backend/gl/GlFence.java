package com.zurrtum.create.client.flywheel.backend.gl;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL32.*;

public class GlFence {
    private final long fence;

    public GlFence() {
        fence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    public boolean isSignaled() {
        int result;
        try (var memoryStack = MemoryStack.stackPush()) {
            long checkPtr = memoryStack.ncalloc(Integer.BYTES, 0, Integer.BYTES);
            nglGetSynciv(fence, GL_SYNC_STATUS, 1, MemoryUtil.NULL, checkPtr);

            result = MemoryUtil.memGetInt(checkPtr);
        }
        return result == GL_SIGNALED;
    }

    public void delete() {
        glDeleteSync(fence);
    }
}
