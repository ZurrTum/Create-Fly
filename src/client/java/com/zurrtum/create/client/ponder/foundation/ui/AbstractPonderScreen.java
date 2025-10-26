package com.zurrtum.create.client.ponder.foundation.ui;

import com.zurrtum.create.client.catnip.gui.NavigatableSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.text.Text;

import static com.zurrtum.create.client.ponder.foundation.registration.PonderLocalization.UI_PREFIX;

public abstract class AbstractPonderScreen extends NavigatableSimiScreen {

    public static final String INDEX_TITLE = UI_PREFIX + "index_title";
    public static final String WELCOME = UI_PREFIX + "welcome";
    public static final String CATEGORIES = UI_PREFIX + "categories";
    public static final String DESCRIPTION = UI_PREFIX + "index_description";

    public static final String PONDERING = UI_PREFIX + "pondering";
    public static final String PONDERING_TAG = UI_PREFIX + "pondering_tag";
    public static final String IDENTIFY_MODE = UI_PREFIX + "identify_mode";
    public static final String IN_CHAPTER = UI_PREFIX + "in_chapter";
    public static final String IDENTIFY = UI_PREFIX + "identify";
    public static final String PREVIOUS = UI_PREFIX + "previous";
    public static final String CLOSE = UI_PREFIX + "close";
    public static final String NEXT = UI_PREFIX + "next";
    public static final String NEXT_UP = UI_PREFIX + "next_up";
    public static final String REPLAY = UI_PREFIX + "replay";
    public static final String SLOW_TEXT = UI_PREFIX + "slow_text";
    public static final String THINK_BACK = UI_PREFIX + "think_back";
    public static final String EXIT = UI_PREFIX + "exit";

    public static final String ASSOCIATED = UI_PREFIX + "associated";

    @Override
    protected void init() {
        super.init();

        if (backTrack != null) {
            backTrack.withCustomTheme(PonderButton.COLOR_IDLE, PonderButton.COLOR_HOVER, PonderButton.COLOR_CLICK, PonderButton.COLOR_DISABLED);
        }

    }

    @Override
    protected Text backTrackingComponent() {
        if (ScreenOpener.getBackStepScreen() instanceof NavigatableSimiScreen) {
            return Ponder.lang().translate(AbstractPonderScreen.THINK_BACK).component();
        }

        return Ponder.lang().translate(AbstractPonderScreen.EXIT).component();
    }
}