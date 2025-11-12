package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

public class SignDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        BlockEntity be = context.getTargetBlockEntity();
        if (!(be instanceof SignBlockEntity sign))
            return;

        boolean changed = false;
        Couple<SignText> signText = Couple.createWithContext(sign::getText);
        DisplayHolder holder = (DisplayHolder) sign;
        for (int i = 0; i < text.size() && i + line < 4; i++) {
            if (i == 0)
                reserve(i + line, holder, context);
            if (i > 0 && isReserved(i + line, holder, context))
                break;

            final int iFinal = i;
            String content = text.get(iFinal).getString(sign.getMaxTextLineWidth());
            signText = signText.map(st -> st.setMessage(iFinal + line, Component.literal(content)));
            changed = true;
        }

        if (changed) {
            signText.forEachWithContext(sign::setText);
            context.level().sendBlockUpdated(context.getTargetPos(), sign.getBlockState(), sign.getBlockState(), 2);
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(4, 15, this);
    }

    @Override
    public boolean requiresComponentSanitization() {
        return true;
    }

}
