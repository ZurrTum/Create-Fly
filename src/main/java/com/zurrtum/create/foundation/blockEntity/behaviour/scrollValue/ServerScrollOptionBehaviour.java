package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

public class ServerScrollOptionBehaviour<E extends Enum<E>> extends ServerScrollValueBehaviour {
    public E[] options;

    public ServerScrollOptionBehaviour(Class<E> enum_, SmartBlockEntity be) {
        super(be);
        options = enum_.getEnumConstants();
        between(0, options.length - 1);
    }

    public E get() {
        return options[value];
    }

    @Override
    public String getClipboardKey() {
        return options[0].getClass().getSimpleName();
    }
}
