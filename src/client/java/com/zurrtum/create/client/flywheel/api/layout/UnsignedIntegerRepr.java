package com.zurrtum.create.client.flywheel.api.layout;

public enum UnsignedIntegerRepr implements ValueRepr {
    UNSIGNED_BYTE(Byte.BYTES),
    UNSIGNED_SHORT(Short.BYTES),
    UNSIGNED_INT(Integer.BYTES);

    private final int byteSize;

    UnsignedIntegerRepr(int byteSize) {
        this.byteSize = byteSize;
    }

    public int byteSize() {
        return this.byteSize;
    }
}
