package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.client.ponder.api.registration.MultiTagBuilder;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.util.Identifier;

public class GenericMultiTagBuilder<T> implements MultiTagBuilder {

    private PonderTagRegistrationHelper<T> helper;

    public class Tag implements MultiTagBuilder.Tag<T> {

        Iterable<Identifier> tags;

        public Tag(PonderTagRegistrationHelper<T> helper, Iterable<Identifier> tags) {
            GenericMultiTagBuilder.this.helper = helper;
            this.tags = tags;
        }

        @Override
        public Tag add(T component) {
            tags.forEach(tag -> helper.addTagToComponent(component, tag));
            return this;
        }
    }

    public class Component implements MultiTagBuilder.Component {

        Iterable<T> components;

        public Component(PonderTagRegistrationHelper<T> helper, Iterable<T> components) {
            GenericMultiTagBuilder.this.helper = helper;
            this.components = components;
        }

        @Override
        public Component add(Identifier tag) {
            components.forEach(component -> helper.addTagToComponent(component, tag));
            return this;
        }
    }

}