package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.client.catnip.outliner.Outline.OutlineParams;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.ponder.foundation.PonderScene;

import java.util.function.Function;

public class OutlinerElement extends AnimatedSceneElementBase {

    private final Function<Outliner, OutlineParams> outlinerCall;
    private int overrideColor;

    public OutlinerElement(Function<Outliner, OutlineParams> outlinerCall) {
        this.outlinerCall = outlinerCall;
        this.overrideColor = -1;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (fade.getValue() < 1 / 16f)
            return;
        if (fade.getValue(0) > fade.getValue(1))
            return;
        OutlineParams params = outlinerCall.apply(scene.getOutliner());
        if (overrideColor != -1)
            params.colored(overrideColor);
    }

    public void setColor(int overrideColor) {
        this.overrideColor = overrideColor;
    }

}