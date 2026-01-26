package com.zurrtum.create.mixin;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import net.minecraft.component.ComponentType;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @SuppressWarnings("removal")
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At("TAIL"))
    private void create$migrateOldClipboardComponents(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        ClipboardContent content = ClipboardContent.EMPTY;

        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_PAGES, ClipboardContent::setPages);
        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_TYPE, ClipboardContent::setType);
        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_READ_ONLY, (c, v) -> c.setReadOnly(true));
        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_COPIED_VALUES, ClipboardContent::setCopiedValues);
        content = create$migrateComponent(
            content,
            components,
            AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE,
            ClipboardContent::setPreviouslyOpenedPage
        );

        if (content != ClipboardContent.EMPTY) {
            components.set(AllDataComponents.CLIPBOARD_CONTENT, content);
        }
    }

    @Unique
    private static <T> ClipboardContent create$migrateComponent(
        ClipboardContent content,
        MergedComponentMap components,
        ComponentType<T> componentType,
        BiFunction<ClipboardContent, T, ClipboardContent> function
    ) {
        T value = components.get(componentType);
        if (value != null) {
            components.remove(componentType);
            content = function.apply(content, value);
        }

        return content;
    }
}
