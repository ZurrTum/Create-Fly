package com.zurrtum.create.content.decoration.encasing;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public class CasingBlock extends Block implements IWrenchable {

    public CasingBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        return ActionResult.FAIL;
    }

}
