package com.zurrtum.create.content.contraptions.actors.plough;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.contraptions.actors.AttachedActorBlock;
import net.minecraft.block.HorizontalFacingBlock;
import org.jetbrains.annotations.NotNull;

public class PloughBlock extends AttachedActorBlock {

    public static final MapCodec<PloughBlock> CODEC = createCodec(PloughBlock::new);

    public PloughBlock(Settings p_i48377_1_) {
        super(p_i48377_1_);
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
