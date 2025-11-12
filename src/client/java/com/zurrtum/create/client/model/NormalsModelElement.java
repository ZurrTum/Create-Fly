package com.zurrtum.create.client.model;

import net.minecraft.client.renderer.block.model.BlockElement;

public interface NormalsModelElement {
    static NormalsType getNormalsType(BlockElement element) {
        return ((NormalsModelElement) (Object) element).create$getNormalsType();
    }

    static void markNormals(BlockElement element) {
        ((NormalsModelElement) (Object) element).create$markNormals();
    }

    static void markFacingNormals(BlockElement element) {
        ((NormalsModelElement) (Object) element).create$markFacingNormals();
    }

    NormalsType create$getNormalsType();

    void create$markNormals();

    void create$markFacingNormals();

    enum NormalsType {
        FACING,
        CALC
    }
}
