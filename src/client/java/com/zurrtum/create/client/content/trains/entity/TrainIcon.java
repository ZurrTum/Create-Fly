package com.zurrtum.create.client.content.trains.entity;

import com.zurrtum.create.content.trains.entity.TrainIconType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public record TrainIcon(TrainIconType type, Identifier sheet, int x, int y) {
    public static final Identifier ASSEMBLE = Identifier.of(MOD_ID, "textures/gui/assemble.png");
    public static final int ENGINE = -1;
    public static final int FLIPPED_ENGINE = -2;

    public int render(int lengthOrEngine, DrawContext graphics, int x, int y) {
        int offset = getIconOffset(lengthOrEngine);
        int width = getIconWidth(lengthOrEngine);
        graphics.drawTexture(RenderPipelines.GUI_TEXTURED, sheet, x, y, this.x + offset, this.y, width, 10, 256, 256);
        return width;
    }

    public int getIconWidth(int lengthOrEngine) {
        if (lengthOrEngine == FLIPPED_ENGINE)
            return 19;
        if (lengthOrEngine == ENGINE)
            return 19;
        if (lengthOrEngine < 3)
            return 7;
        if (lengthOrEngine < 9)
            return 13;
        return 19;
    }

    public int getIconOffset(int lengthOrEngine) {
        if (lengthOrEngine == FLIPPED_ENGINE)
            return 0;
        if (lengthOrEngine == ENGINE)
            return 62;
        if (lengthOrEngine < 3)
            return 34;
        if (lengthOrEngine < 9)
            return 20;
        return 42;
    }

}
