package com.zurrtum.create.client.foundation.item;

import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface TooltipModifier {
    SimpleRegistry<Item, TooltipModifier> REGISTRY = SimpleRegistry.create();

    TooltipModifier EMPTY = new TooltipModifier() {
        @Override
        public void modify(List<Component> tooltip, Player player) {
        }

        @Override
        public TooltipModifier andThen(TooltipModifier after) {
            return after;
        }
    };

    void modify(List<Component> tooltip, Player player);

    default TooltipModifier andThen(TooltipModifier after) {
        if (after == EMPTY) {
            return this;
        }
        return (tooltip, player) -> {
            modify(tooltip, player);
            after.modify(tooltip, player);
        };
    }

    static TooltipModifier mapNull(@Nullable TooltipModifier modifier) {
        if (modifier == null) {
            return EMPTY;
        }
        return modifier;
    }
}
