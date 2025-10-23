package com.zurrtum.create.client.content.logistics;

import com.google.common.cache.Cache;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddressEditBoxHelper {

    private static final Cache<BlockPos, WeakReference<ClipboardBlockEntity>> NEARBY_CLIPBOARDS = new TickBasedCache<>(20, false);

    public static void advertiseClipboard(ClipboardBlockEntity blockEntity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null)
            return;
        BlockPos blockPos = blockEntity.getPos();
        if (player.squaredDistanceTo(Vec3d.ofCenter(blockPos)) > 32 * 32)
            return;
        NEARBY_CLIPBOARDS.put(blockPos, new WeakReference<>(blockEntity));
    }

    public static DestinationSuggestions createSuggestions(Screen screen, TextFieldWidget pInput, boolean anchorToBottom, String localAddress) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        List<IntAttached<String>> options = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();

        DestinationSuggestions destinationSuggestions = new DestinationSuggestions(
            mc,
            screen,
            pInput,
            mc.textRenderer,
            options,
            anchorToBottom,
            -72 + pInput.getY() + (anchorToBottom ? 0 : pInput.getHeight())
        );

        if (player == null)
            return destinationSuggestions;

        if (localAddress != null) {
            options.add(IntAttached.with(-1, localAddress));
            alreadyAdded.add(localAddress);
        }

        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            appendAddresses(options, alreadyAdded, inventory.getStack(i));
        }

        for (WeakReference<ClipboardBlockEntity> wr : NEARBY_CLIPBOARDS.asMap().values()) {
            ClipboardBlockEntity cbe = wr.get();
            if (cbe != null)
                appendAddresses(options, alreadyAdded, cbe.getComponents());
        }

        return destinationSuggestions;
    }

    private static void appendAddresses(List<IntAttached<String>> options, Set<String> alreadyAdded, ItemStack item) {
        if (item == null || !item.isOf(AllItems.CLIPBOARD))
            return;

        appendAddresses(options, alreadyAdded, item.getComponents());
    }

    private static void appendAddresses(List<IntAttached<String>> options, Set<String> alreadyAdded, ComponentMap components) {
        List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(components);
        pages.forEach(page -> page.forEach(entry -> {
            String string = entry.text.getString();
            if (entry.checked)
                return;
            if (!string.startsWith("#") || string.length() == 1)
                return;
            String address = string.substring(1);
            if (address.isBlank())
                return;
            String trim = address.trim();
            if (!alreadyAdded.add(trim))
                return;
            options.add(IntAttached.withZero(trim));
        }));
    }

}
