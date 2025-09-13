package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public class SignDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableText> text, DisplayLinkContext context) {
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
            String content = text.get(iFinal).asTruncatedString(sign.getMaxTextWidth());
            signText = signText.map(st -> st.withMessage(iFinal + line, Text.literal(content)));
            changed = true;
        }

        if (changed) {
            signText.forEachWithContext(sign::setText);
            context.level().updateListeners(context.getTargetPos(), sign.getCachedState(), sign.getCachedState(), 2);
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
