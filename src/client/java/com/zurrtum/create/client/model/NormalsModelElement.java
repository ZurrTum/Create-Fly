package com.zurrtum.create.client.model;

import net.minecraft.client.renderer.block.model.BlockElement;

public interface NormalsModelElement {
    static boolean calcNormals(BlockElement element) {
        return ((NormalsModelElement) (Object) element).create$calcNormals();
    }

    static void markNormals(BlockElement element) {
        ((NormalsModelElement) (Object) element).create$markNormals();
    }

    boolean create$calcNormals();

    void create$markNormals();
}
