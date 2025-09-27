package com.zurrtum.create.client.ponder.foundation;

import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

public class PonderChapter implements ScreenElement {

    private final Identifier id;
    private final Identifier icon;

    private PonderChapter(Identifier id) {
        this.id = id;
        icon = Identifier.of(id.getNamespace(), "textures/ponder/chapter/" + id.getPath() + ".png");
    }

    public Identifier getId() {
        return id;
    }

    public String getTitle() {
        return "";
    }

    @Override
    public void render(DrawContext graphics, int x, int y) {
        Matrix3x2fStack ms = graphics.getMatrices();
        ms.pushMatrix();
        ms.scale(0.25f, 0.25f);
        //x and y offset, blit z offset, tex x and y, tex width and height, entire tex sheet width and height
        graphics.drawTexture(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, 0, 64, 64, 64, 64);
        ms.popMatrix();
    }

    @Deprecated
    public static PonderChapter of(Identifier id) {
		/*PonderChapter chapter = PonderRegistry.CHAPTERS.getChapter(id);
		if (chapter == null) {
			 chapter = PonderRegistry.CHAPTERS.addChapter(new PonderChapter(id));
		}

		return chapter;*/
        return null;
    }
}