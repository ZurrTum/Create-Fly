package com.zurrtum.create.content.redstone.displayLink.target;

import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldAccess;

import java.util.List;

public class DisplayBoardTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableText> text, DisplayLinkContext context) {
    }

    public void acceptFlapText(int line, List<List<MutableText>> text, DisplayLinkContext context) {
        FlapDisplayBlockEntity controller = getController(context);
        if (controller == null)
            return;
        if (!controller.isSpeedRequirementFulfilled())
            return;

        DisplaySource source = context.blockEntity().activeSource;
        List<FlapDisplayLayout> lines = controller.getLines();
        for (int i = 0; i + line < lines.size(); i++) {

            if (i == 0)
                reserve(i + line, controller, context);
            if (i > 0 && isReserved(i + line, controller, context))
                break;

            FlapDisplayLayout layout = lines.get(i + line);

            if (i >= text.size()) {
                if (source instanceof SingleLineDisplaySource)
                    break;
                controller.applyTextManually(i + line, null);
                continue;
            }

            source.loadFlapDisplayLayout(context, controller, layout, i);

            for (int sectionIndex = 0; sectionIndex < layout.getSections().size(); sectionIndex++) {
                List<MutableText> textLine = text.get(i);
                if (textLine.size() <= sectionIndex)
                    break;
                layout.getSections().get(sectionIndex).setText(textLine.get(sectionIndex));
            }
        }

        controller.sendData();
    }

    @Override
    public boolean isReserved(int line, DisplayHolder target, DisplayLinkContext context) {
        return super.isReserved(
            line,
            target,
            context
        ) || target instanceof FlapDisplayBlockEntity fdte && fdte.manualLines.length > line && fdte.manualLines[line];
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        FlapDisplayBlockEntity controller = getController(context);
        if (controller == null)
            return new DisplayTargetStats(1, 1, this);
        return new DisplayTargetStats(controller.ySize * 2, controller.getMaxCharCount(), this);
    }

    private FlapDisplayBlockEntity getController(DisplayLinkContext context) {
        BlockEntity teIn = context.getTargetBlockEntity();
        if (!(teIn instanceof FlapDisplayBlockEntity be))
            return null;
        return be.getController();
    }

    public Box getMultiblockBounds(WorldAccess level, BlockPos pos) {
        Box baseShape = super.getMultiblockBounds(level, pos);
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof FlapDisplayBlockEntity fdbe))
            return baseShape;

        FlapDisplayBlockEntity controller = fdbe.getController();
        if (controller == null)
            return baseShape;

        Vec3i normal = controller.getDirection().rotateYClockwise().getVector();
        return baseShape.offset(controller.getPos().subtract(pos))
            .stretch(normal.getX() * (controller.xSize - 1), 1 - controller.ySize, normal.getZ() * (controller.xSize - 1));
    }
}