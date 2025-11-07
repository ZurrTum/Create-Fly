package com.zurrtum.create.client.flywheel.api.backend;

import java.lang.annotation.*;

/**
 * <p>Indicates that the annotated API class, interface or method must not be extended, implemented or overridden,
 * <strong>except by backend implementations</strong>.</p>
 *
 * <p>API class, interface or method may not be marked {@code final} because it is extended by classes of registered backends
 * but it is not supposed to be extended outside of backend implementations. Instances of classes and interfaces marked with this annotation
 * may be cast to an internal implementing class within the active backend, leading to {@code ClassCastException}
 * if a different implementation is provided by a client.</p>
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE})
public @interface BackendImplemented {
}
