package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class LecternDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableText> text, DisplayLinkContext context) {
        BlockEntity be = context.getTargetBlockEntity();
        if (!(be instanceof LecternBlockEntity lectern))
            return;
        ItemStack book = lectern.getBook();
        if (book.isEmpty())
            return;

        if (book.isOf(Items.WRITABLE_BOOK))
            lectern.setBook(book = signBook(book));
        if (!book.isOf(Items.WRITTEN_BOOK))
            return;

        WrittenBookContentComponent writtenBookContent = book.getOrDefault(
            DataComponentTypes.WRITTEN_BOOK_CONTENT,
            WrittenBookContentComponent.DEFAULT
        );
        List<RawFilteredPair<Text>> pages = new ArrayList<>(writtenBookContent.pages());

        boolean changed = false;
        DisplayHolder holder = (DisplayHolder) lectern;
        for (int i = 0; i - line < text.size() && i < 50; i++) {
            if (pages.size() <= i)
                pages.add(RawFilteredPair.of(i < line ? Text.empty() : text.get(i - line)));

            else if (i >= line) {
                if (i - line == 0)
                    reserve(i, holder, context);
                if (i - line > 0 && isReserved(i - line, holder, context))
                    break;

                pages.set(i, RawFilteredPair.of(text.get(i - line)));
            }
            changed = true;
        }

        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContent.withPages(pages));
        lectern.setBook(book);

        if (changed)
            context.level().updateListeners(context.getTargetPos(), lectern.getCachedState(), lectern.getCachedState(), 2);
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(50, 256, this);
    }

    public Text getLineOptionText(int line) {
        return Text.translatable("create.display_target.page", line + 1);
    }

    private ItemStack signBook(ItemStack book) {
        ItemStack written = new ItemStack(Items.WRITTEN_BOOK);
        WritableBookContentComponent bookContents = book.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);

        List<RawFilteredPair<Text>> list = bookContents.pages().stream().map(filterable -> filterable.<Text>map(Text::literal)).toList();
        WrittenBookContentComponent writtenContent = new WrittenBookContentComponent(
            RawFilteredPair.of("Printed Book"),
            "Data Gatherer",
            0,
            list,
            true
        );
        written.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenContent);

        return written;
    }

    @Override
    public boolean requiresComponentSanitization() {
        return true;
    }

}
