package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.compat.Mods;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class AllComputerDisplaySource {
    public static final ComputerDisplaySource COMPUTER = registerDisplay();
    public static final List<Identifier> SUPPORT_BLOCK = List.of(
        Mods.COMPUTERCRAFT.identifier("wired_modem_full"),
        Mods.COMPUTERCRAFT.identifier("computer_normal"),
        Mods.COMPUTERCRAFT.identifier("computer_advanced"),
        Mods.COMPUTERCRAFT.identifier("computer_command")
    );

    private static ComputerDisplaySource registerDisplay() {
        return Registry.register(CreateRegistries.DISPLAY_SOURCE, Identifier.fromNamespaceAndPath(MOD_ID, "computer"), new ComputerDisplaySource());
    }

    public static void register() {
        RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK).register((rawId, id, block) -> {
            if (SUPPORT_BLOCK.contains(id)) {
                DisplaySource.BY_BLOCK.add(block, COMPUTER);
            }
        });
    }
}
