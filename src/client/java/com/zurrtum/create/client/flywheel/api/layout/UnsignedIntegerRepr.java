package com.zurrtum.create.client.flywheel.api.layout;

public enum UnsignedIntegerRepr implements ValueRepr {
    UNSIGNED_BYTE(1), UNSIGNED_SHORT(2), UNSIGNED_INT(4);

    private final int byteSize;

    private UnsignedIntegerRepr(int byteSize) {
        this.byteSize = byteSize;
    }

    public int byteSize() {
        return this.byteSize;
    }
}
