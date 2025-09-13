package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.contraptions.actors.roller.RollerValueBox;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity.RollingMode;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

public class RollingModeScrollBehaviour extends ScrollOptionBehaviour<RollingMode> {
    public RollingModeScrollBehaviour(
        SmartBlockEntity be
    ) {
        super(RollingModeIcon.class, RollingModeIcon::from, CreateLang.translateDirect("contraptions.roller_mode"), be, new RollerValueBox(-3));
    }

    public enum RollingModeIcon implements INamedIconOptions {
        TUNNEL_PAVE(AllIcons.I_ROLLER_PAVE),
        STRAIGHT_FILL(AllIcons.I_ROLLER_FILL),
        WIDE_FILL(AllIcons.I_ROLLER_WIDE_FILL);

        private final String translationKey;
        private final AllIcons icon;

        RollingModeIcon(AllIcons icon) {
            this.icon = icon;
            translationKey = "create.contraptions.roller_mode." + Lang.asId(name());
        }

        public static RollingModeIcon from(RollingMode mode) {
            return switch (mode) {
                case TUNNEL_PAVE -> TUNNEL_PAVE;
                case STRAIGHT_FILL -> STRAIGHT_FILL;
                case WIDE_FILL -> WIDE_FILL;
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
