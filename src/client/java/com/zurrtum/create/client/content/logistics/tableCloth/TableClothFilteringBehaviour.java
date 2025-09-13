package com.zurrtum.create.client.content.logistics.tableCloth;

import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.tableCloth.ServerTableClothFilteringBehaviour;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public class TableClothFilteringBehaviour extends FilteringBehaviour<ServerTableClothFilteringBehaviour> {
    public TableClothFilteringBehaviour(TableClothBlockEntity be) {
        super(be, new TableClothFilterSlot(be));
    }

    @Override
    public float getRenderDistance() {
        return 32;
    }

    @Override
    public MutableText getLabel() {
        return CreateLang.translateDirect("table_cloth.price_per_order");
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
            getLabel(),
            100,
            10,
            CreateLang.translatedOptions("table_cloth", "amount"),
            new ValueSettingsFormatter(this::formatValue)
        );
    }

    @Override
    public MutableText formatValue(ValueSettings value) {
        return Text.literal(String.valueOf(Math.max(1, value.value())));
    }

    @Override
    public MutableText getCountLabelForValueBox() {
        return Text.literal(isCountVisible() ? String.valueOf(behaviour.count) : "");
    }

    public boolean targetsPriceTag(PlayerEntity player, BlockHitResult ray) {
        return behaviour != null && behaviour.mayInteract(player) && getSlotPositioning().testHit(
            blockEntity.getWorld(),
            blockEntity.getPos(),
            blockEntity.getCachedState(),
            ray.getPos().subtract(Vec3d.of(blockEntity.getPos()))
        );
    }
}
