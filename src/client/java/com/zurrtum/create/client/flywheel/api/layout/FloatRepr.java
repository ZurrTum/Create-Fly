package com.zurrtum.create.client.flywheel.api.layout;

public enum FloatRepr implements ValueRepr {
    BYTE(1), NORMALIZED_BYTE(1), UNSIGNED_BYTE(1), NORMALIZED_UNSIGNED_BYTE(1), SHORT(2), NORMALIZED_SHORT(2), UNSIGNED_SHORT(2), NORMALIZED_UNSIGNED_SHORT(
        2), INT(4), NORMALIZED_INT(4), UNSIGNED_INT(4), NORMALIZED_UNSIGNED_INT(4), FLOAT(4);

    private final int byteSize;

    private FloatRepr(int byteSize) {
        this.byteSize = byteSize;
    }

    public int byteSize() {
        return this.byteSize;
    }
}
