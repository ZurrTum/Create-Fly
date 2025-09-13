package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.bearing.ClockworkBearingBlockEntity.ClockHands;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

public class ClockHandsScrollBehaviour extends ScrollOptionBehaviour<ClockHands> {
    public ClockHandsScrollBehaviour(SmartBlockEntity be) {
        super(
            ClockHandsIcon.class,
            ClockHandsIcon::from,
            CreateLang.translateDirect("contraptions.clockwork.clock_hands"),
            be,
            getMovementModeSlot()
        );
    }

    private enum ClockHandsIcon implements INamedIconOptions {
        HOUR_FIRST(AllIcons.I_HOUR_HAND_FIRST),
        MINUTE_FIRST(AllIcons.I_MINUTE_HAND_FIRST),
        HOUR_FIRST_24(AllIcons.I_HOUR_HAND_FIRST_24);

        private final String translationKey;
        private final AllIcons icon;

        ClockHandsIcon(AllIcons icon) {
            this.icon = icon;
            translationKey = "create.contraptions.clockwork." + Lang.asId(name());
        }

        public static ClockHandsIcon from(ClockHands hands) {
            return switch (hands) {
                case HOUR_FIRST -> HOUR_FIRST;
                case MINUTE_FIRST -> MINUTE_FIRST;
                case HOUR_FIRST_24 -> HOUR_FIRST_24;
            };
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

    }
}
