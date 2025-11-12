package com.zurrtum.create.client.content.logistics.tableCloth;

import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.tableCloth.ServerTableClothFilteringBehaviour;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TableClothFilteringBehaviour extends FilteringBehaviour<ServerTableClothFilteringBehaviour> {
    public TableClothFilteringBehaviour(TableClothBlockEntity be) {
        super(be, new TableClothFilterSlot(be));
    }

    @Override
    public float getRenderDistance() {
        return 32;
    }

    @Override
    public MutableComponent getLabel() {
        return CreateLang.translateDirect("table_cloth.price_per_order");
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
            getLabel(),
            100,
            10,
            CreateLang.translatedOptions("table_cloth", "amount"),
            new ValueSettingsFormatter(this::formatValue)
        );
    }

    @Override
    public MutableComponent formatValue(ValueSettings value) {
        return Component.literal(String.valueOf(Math.max(1, value.value())));
    }

    @Override
    public MutableComponent getCountLabelForValueBox() {
        return Component.literal(isCountVisible() ? String.valueOf(behaviour.count) : "");
    }

    public boolean targetsPriceTag(Player player, BlockHitResult ray) {
        return behaviour != null && behaviour.mayInteract(player) && getSlotPositioning().testHit(
            blockEntity.getLevel(),
            blockEntity.getBlockPos(),
            blockEntity.getBlockState(),
            ray.getLocation().subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()))
        );
    }
}
