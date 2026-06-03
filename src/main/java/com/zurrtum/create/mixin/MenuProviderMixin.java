package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MenuProvider.class)
public interface MenuProviderMixin {
    @Overwrite(remap = false)
    @SuppressWarnings("DataFlowIssue")
    @Nullable
    static MenuBase<?> createMenu(
        MenuProvider provider,
        int containerCounter,
        Inventory inventory,
        ServerPlayer player,
        RegistryFriendlyByteBuf buf
    ) {
        return PacketContext.supplyWithContext(
            (PacketContextProvider) player,
            () -> provider.createMenu(containerCounter, inventory, player, buf)
        );
    }
}
