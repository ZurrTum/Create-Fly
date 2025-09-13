package com.zurrtum.create.content.contraptions.actors.harvester;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.contraptions.actors.AttachedActorBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public class HarvesterBlock extends AttachedActorBlock implements IBE<HarvesterBlockEntity> {

    public static final MapCodec<HarvesterBlock> CODEC = createCodec(HarvesterBlock::new);

    public HarvesterBlock(Settings p_i48377_1_) {
        super(p_i48377_1_);
    }

    @Override
    public Class<HarvesterBlockEntity> getBlockEntityClass() {
        return HarvesterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HarvesterBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.HARVESTER;
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
