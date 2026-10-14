package com.zurrtum.create.client.flywheel.backend.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.backend.FlwBackend;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Probes whether the running game is on a GPU backend this Vulkan Flywheel port can drive.
 *
 * <p>Two independent signals, in priority order:
 * <ol>
 *   <li>Ask Minecraft itself, via {@link RenderSystem#getDevice()}. This is backend-agnostic:
 *       it says "Vulkan" no matter which mod is providing it, and it keeps working if Mojang
 *       ever ships an official Vulkan backend. This is the signal we actually trust.</li>
 *   <li>Fall back to "is the vulkanmod present". Weaker - it only proves the mod is loaded,
 *       not that Vulkan is actually the active backend. Used only as a diagnostic so we can
 *       tell "VulkanMod absent" apart from "VulkanMod present but renderer fell back to OpenGL".</li>
 * </ol>
 */
public final class VulkanSupport {

    /** Set from gradle.properties at build time so we can flip behaviour without a rebuild dance. */
    private static boolean forceEnabled = false;

    private static Boolean cachedVulkanModLoaded;
    private static String cachedBackendName;

    private VulkanSupport() {
    }

    /**
     * The {@code supported()} predicate handed to the Flywheel backend registry.
     *
     * <p>Logs on every call. That matters: {@code BackendManagerImpl.chooseBackend()} walks the
     * priority chain asking each candidate {@code isSupported()}, and discards the answers
     * silently. Without this log there is no evidence the backend was ever consulted, and a
     * missing backend is indistinguishable from one that was skipped by mistake.
     *
     * <p>Returns {@code false} for now - Phase 3 only. {@link VulkanDrawManager} is still a
     * skeleton that logs instead of drawing.
     */
    public static boolean probe() {
        String backend = backendName();
        boolean modLoaded = vulkanModLoaded();
        boolean isVulkan = backend != null && backend.toLowerCase().contains("vulkan");

        boolean verdict = isVulkan || forceEnabled;

        FlwBackend.LOGGER.info(
            "[FW/VK] supported() consulted: backend='{}' vendor='{}' renderer='{}' "
                + "vulkanmodLoaded={} isVulkan={} forceEnabled={} -> usable={} (Phase 3 hardcoded false)",
            backend, vendor(), renderer(), modLoaded, isVulkan, forceEnabled, verdict
        );

        // Phase 3: hard false. Once VulkanDrawManager can submit draws this becomes
        // `return verdict;` and Flywheel will select this backend whenever the game is on
        // a Vulkan device.
        return false;
    }

    private static String backendName() {
        if (cachedBackendName != null) {
            return cachedBackendName;
        }
        try {
            var device = RenderSystem.getDevice();
            cachedBackendName = device == null ? "<device-not-initialised>" : device.getBackendName();
        } catch (Throwable t) {
            FlwBackend.LOGGER.warn("[FW/VK] could not query RenderSystem.getDevice()", t);
            cachedBackendName = "<query-failed: " + t.getClass().getSimpleName() + ">";
        }
        return cachedBackendName;
    }

    private static String vendor() {
        try {
            var device = RenderSystem.getDevice();
            return device == null ? "?" : device.getVendor();
        } catch (Throwable t) {
            return "?";
        }
    }

    private static String renderer() {
        try {
            var device = RenderSystem.getDevice();
            return device == null ? "?" : device.getRenderer();
        } catch (Throwable t) {
            return "?";
        }
    }

    private static boolean vulkanModLoaded() {
        if (cachedVulkanModLoaded == null) {
            try {
                cachedVulkanModLoaded = FabricLoader.getInstance().isModLoaded("vulkanmod");
            } catch (Throwable t) {
                cachedVulkanModLoaded = false;
            }
        }
        return cachedVulkanModLoaded;
    }
}