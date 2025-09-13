package com.zurrtum.create.client.flywheel.api.backend;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE})
public @interface BackendImplemented {
}
