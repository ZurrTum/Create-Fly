package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.impl.FlwImpl;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Holds all references to the Sodium API, accessed via reflection to avoid a compile-time
 * Sodium dependency. This class is only loaded when Sodium is present
 * ({@link SodiumCompat#ACTIVE} is true), so the mod can start without Sodium on the
 * classpath without triggering a {@link NoClassDefFoundError}.
 */
final class SodiumCompatInternals {
    private static final Class<?> PREDICATE_CLASS;
    private static final Object HANDLER;
    private static final Method ADD_PREDICATE;
    private static final Method REMOVE_PREDICATE;

    static {
        Class<?> predicateClass = null;
        Object handler = null;
        Method addPredicate = null;
        Method removePredicate = null;
        try {
            predicateClass = Class.forName("net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderPredicate");
            Class<?> handlerClass = Class.forName("net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderHandler");
            handler = handlerClass.getMethod("instance").invoke(null);
            addPredicate = handlerClass.getMethod("addRenderPredicate", BlockEntityType.class, predicateClass);
            removePredicate = handlerClass.getMethod("removeRenderPredicate", BlockEntityType.class, predicateClass);
        } catch (ReflectiveOperationException e) {
            // Sodium API structure has changed; Sodium integration will be unavailable
            FlwImpl.LOGGER.debug("Could not initialize Sodium block entity render integration", e);
        }
        PREDICATE_CLASS = predicateClass;
        HANDLER = handler;
        ADD_PREDICATE = addPredicate;
        REMOVE_PREDICATE = removePredicate;
    }

    private SodiumCompatInternals() {
    }

    static <T extends BlockEntity> Object addPredicate(BlockEntityType<T> type) {
        if (HANDLER == null || ADD_PREDICATE == null || PREDICATE_CLASS == null) {
            return null;
        }
        try {
            Object predicate = Proxy.newProxyInstance(
                    SodiumCompatInternals.class.getClassLoader(),
                    new Class<?>[]{PREDICATE_CLASS},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "toString" -> "FlywheelSodiumPredicate@" + Integer.toHexString(System.identityHashCode(proxy));
                        default -> {
                            // The predicate method receives (getter, pos, blockEntity); find the BlockEntity arg
                            if (args != null) {
                                for (Object arg : args) {
                                    if (arg instanceof BlockEntity be) {
                                        yield !VisualizationHelper.tryAddBlockEntity(be);
                                    }
                                }
                            }
                            yield true; // allow vanilla rendering as a safe default
                        }
                    }
            );
            ADD_PREDICATE.invoke(HANDLER, type, predicate);
            return predicate;
        } catch (ReflectiveOperationException e) {
            FlwImpl.LOGGER.debug("Could not register Sodium block entity render predicate for {}", type, e);
            return null;
        }
    }

    static <T extends BlockEntity> void removePredicate(BlockEntityType<T> type, Object predicate) {
        if (HANDLER == null || REMOVE_PREDICATE == null || predicate == null) {
            return;
        }
        try {
            REMOVE_PREDICATE.invoke(HANDLER, type, predicate);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
