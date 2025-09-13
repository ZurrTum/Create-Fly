package com.zurrtum.create.infrastructure.debugInfo;

import com.google.common.collect.ImmutableMap;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.Create;
import com.zurrtum.create.infrastructure.debugInfo.element.DebugInfoSection;
import com.zurrtum.create.infrastructure.debugInfo.element.InfoElement;
import com.zurrtum.create.infrastructure.debugInfo.element.InfoEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allows for providing easily accessible debugging information.
 * This info can be retrieved with the "/create debuginfo" command.
 * This command copies all information to the clipboard, formatted for a GitHub issue.
 * Addons are welcome to add their own sections. Registration must occur synchronously.
 */
public class DebugInformation {
    private static final List<DebugInfoSection> client = new ArrayList<>();
    private static final List<DebugInfoSection> server = new ArrayList<>();

    private static final ImmutableMap<String, String> mcSystemInfo = Util.make(() -> {
        SystemDetails systemReport = new SystemDetails();
        return ImmutableMap.copyOf(systemReport.sections);
    });

    public static void registerClientInfo(DebugInfoSection section) {
        client.add(section);
    }

    public static void registerServerInfo(DebugInfoSection section) {
        server.add(section);
    }

    public static void registerBothInfo(DebugInfoSection section) {
        registerClientInfo(section);
        registerServerInfo(section);
    }

    public static List<DebugInfoSection> getClientInfo() {
        return client;
    }

    public static List<DebugInfoSection> getServerInfo() {
        return server;
    }

    static {
        DebugInfoSection.builder(Create.NAME).put("Mod Version", Create.VERSION).put("Ponder Version", getVersionOfMod("ponder"))
            .put("NeoForge Version", getVersionOfMod("neoforge")).put("Minecraft Version", SharedConstants.getGameVersion().name())
            .buildTo(DebugInformation::registerBothInfo);

        AllClientHandle.INSTANCE.buildDebugInfo();

        DebugInfoSection.builder("System Information").put("Operating System", SystemDetails.OPERATING_SYSTEM)
            .put("Java Version", SystemDetails.JAVA_VERSION).put("JVM Flags", getMcSystemInfo("JVM Flags"))
            .put("Memory", () -> getMcSystemInfo("Memory")).put("Total Memory", getTotalRam()).put("CPU", getCpuInfo()).putAll(listAllGraphicsCards())
            .buildTo(DebugInformation::registerBothInfo);

        DebugInfoSection.builder("Other Mods").putAll(listAllOtherMods()).buildTo(DebugInformation::registerBothInfo);
    }

    public static String getVersionOfMod(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(mod -> mod.getMetadata().getVersion().toString()).orElse("None");
    }

    public static Collection<InfoElement> listAllOtherMods() {
        List<InfoElement> mods = new ArrayList<>();
        FabricLoader.getInstance().getAllMods().forEach(mod -> {
            ModMetadata meta = mod.getMetadata();
            String id = meta.getId();
            if (!id.equals(Create.MOD_ID) && !id.equals("fabric-api") && !id.equals("minecraft") && !id.equals("flywheel")) {
                String name = meta.getName();
                String version = meta.getVersion().toString();
                mods.add(new InfoEntry(name, version));
            }
        });
        return mods;
    }

    public static Collection<InfoElement> listAllGraphicsCards() {
        List<InfoElement> cards = new ArrayList<>();
        for (int i = 0; i < 10; i++) { // there won't be more than 10, right? right??
            String name = getMcSystemInfo("Graphics card #" + i + " name");
            String vendor = getMcSystemInfo("Graphics card #" + i + " vendor");
            String vram = getMcSystemInfo("Graphics card #" + i + " VRAM (MB)");
            if (name == null || vendor == null || vram == null)
                break;
            String key = "Graphics card #" + i;
            String value = String.format("%s (%s); %s MB of VRAM", name, vendor, vram);
            cards.add(new InfoEntry(key, value));
        }
        return cards.isEmpty() ? List.of(new InfoEntry("Graphics cards", "none")) : cards;
    }

    public static String getTotalRam() {
        Runtime runtime = Runtime.getRuntime();
        long availableMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - availableMemory;
        return String.format("%s bytes (%s MiB) / %s bytes (%s MiB)", usedMemory, usedMemory / 1048576L, totalMemory, totalMemory / 1048576L);
    }

    public static String getCpuInfo() {
        String name = tryTrim(getMcSystemInfo("Processor Name"));
        String freq = getMcSystemInfo("Frequency (GHz)");
        String sockets = getMcSystemInfo("Number of physical packages");
        String cores = getMcSystemInfo("Number of physical CPUs");
        String threads = getMcSystemInfo("Number of logical CPUs");
        return String.format("%s @ %s GHz; %s cores / %s threads on %s socket(s)", name, freq, cores, threads, sockets);
    }

    /**
     * Get a system attribute provided by Minecraft.
     * They can be found in the constructor of {@link SystemDetails}.
     */
    @Nullable
    public static String getMcSystemInfo(String key) {
        return mcSystemInfo.get(key);
    }

    public static String getIndent(int depth) {
        return Stream.generate(() -> "\t").limit(depth).collect(Collectors.joining());
    }

    @Nullable
    public static String tryTrim(@Nullable String s) {
        return s == null ? null : s.trim();
    }
}
