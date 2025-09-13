package com.zurrtum.create.client.model;

import net.minecraft.client.render.model.json.ModelElement;

public interface NormalsModelElement {
    static NormalsType getNormalsType(ModelElement element) {
        return ((NormalsModelElement) (Object) element).create$getNormalsType();
    }

    static void markNormals(ModelElement element) {
        ((NormalsModelElement) (Object) element).create$markNormals();
    }

    static void markFacingNormals(ModelElement element) {
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
