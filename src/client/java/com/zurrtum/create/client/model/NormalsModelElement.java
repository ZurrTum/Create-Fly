package com.zurrtum.create.client.model;

import net.minecraft.client.render.model.json.ModelElement;

public interface NormalsModelElement {
    static boolean calcNormals(ModelElement element) {
        return ((NormalsModelElement) (Object) element).create$calcNormals();
    }

    static void markNormals(ModelElement element) {
        ((NormalsModelElement) (Object) element).create$markNormals();
    }

    boolean create$calcNormals();

    void create$markNormals();
}
