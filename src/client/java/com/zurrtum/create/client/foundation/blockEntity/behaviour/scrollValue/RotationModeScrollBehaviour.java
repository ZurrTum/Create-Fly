package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.IControlContraption.RotationMode;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

public class RotationModeScrollBehaviour extends ScrollOptionBehaviour<RotationMode> {
    public RotationModeScrollBehaviour(SmartBlockEntity be) {
        super(RotationModeIcon.class, RotationModeIcon::from, CreateLang.translateDirect("contraptions.movement_mode"), be, getMovementModeSlot());
    }

    private enum RotationModeIcon implements INamedIconOptions {
        ROTATE_PLACE(AllIcons.I_ROTATE_PLACE),
        ROTATE_PLACE_RETURNED(AllIcons.I_ROTATE_PLACE_RETURNED),
        ROTATE_NEVER_PLACE(AllIcons.I_ROTATE_NEVER_PLACE);

        private final String translationKey;
        private final AllIcons icon;

        RotationModeIcon(AllIcons icon) {
            this.icon = icon;
            translationKey = "create.contraptions.movement_mode." + Lang.asId(name());
        }

        public static RotationModeIcon from(RotationMode mode) {
            return switch (mode) {
                case ROTATE_PLACE -> ROTATE_PLACE;
                case ROTATE_PLACE_RETURNED -> ROTATE_PLACE_RETURNED;
                case ROTATE_NEVER_PLACE -> ROTATE_NEVER_PLACE;
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
