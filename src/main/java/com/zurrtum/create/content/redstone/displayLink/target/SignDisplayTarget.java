package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;

import java.util.List;

public class SignDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        int size = text.size();
        if (size == 0) {
            return;
        }
        BlockEntity be = context.getTargetBlockEntity();
        if (!(be instanceof SignBlockEntity sign)) {
            return;
        }

        boolean changed = false;
        SignText.Mutable frontText = sign.getText(SignTextSlot.FRONT).asMutable();
        SignText.Mutable backText = sign.getText(SignTextSlot.BACK).asMutable();
        DisplayHolder holder = (DisplayHolder) sign;
        for (int i = 0; i < size && i + line < 4; i++) {
            if (i == 0) {
                reserve(line, holder, context);
            }
            if (i > 0 && isReserved(i + line, holder, context)) {
                break;
            }

            String content = text.get(i).getString(sign.getMaxTextLineWidth());
            int index = i + line;
            Component contentText = Component.literal(content);
            frontText.setLine(index, contentText);
            backText.setLine(index, contentText);
            changed = true;
        }

        if (changed) {
            sign.setText(frontText.asImmutable(), SignTextSlot.FRONT);
            sign.setText(backText.asImmutable(), SignTextSlot.BACK);
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
