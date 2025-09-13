package com.zurrtum.create.client.catnip.gui;

import com.zurrtum.create.client.catnip.render.BindableTexture;

public interface TextureSheetSegment extends BindableTexture {

    int getStartX();

    int getStartY();

    int getWidth();

    int getHeight();

}
