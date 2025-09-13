package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.contraptions.DirectionalExtenderScrollOptionSlot;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter.ScrollOptionSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction.Axis;

import java.util.function.Function;

public abstract class ScrollOptionBehaviour<T extends Enum<T>> extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollOptionBehaviour<T>> {
    private final INamedIconOptions[] icons;
    private final Function<T, INamedIconOptions> iconGetter;

    public <E extends Enum<E> & INamedIconOptions> ScrollOptionBehaviour(
        Class<E> enum_,
        Function<T, INamedIconOptions> getter,
        Text label,
        SmartBlockEntity be,
        ValueBoxTransform slot
    ) {
        super(label, be, slot);
        icons = enum_.getEnumConstants();
        iconGetter = getter;
    }

    public INamedIconOptions getIconForSelected() {
        return iconGetter.apply(behaviour.get());
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
            label,
            behaviour.getMax(),
            1,
            ImmutableList.of(Text.literal("Select")),
            new ScrollOptionSettingsFormatter(icons)
        );
    }

    public static ValueBoxTransform getMovementModeSlot() {
        return new DirectionalExtenderScrollOptionSlot((state, d) -> {
            Axis axis = d.getAxis();
            Axis bearingAxis = state.get(BearingBlock.FACING).getAxis();
            return bearingAxis != axis;
        });
    }
}
