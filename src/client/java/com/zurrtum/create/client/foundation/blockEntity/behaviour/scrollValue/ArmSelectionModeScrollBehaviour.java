package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.SelectionModeValueBox;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity.SelectionMode;

public class ArmSelectionModeScrollBehaviour extends ScrollOptionBehaviour<SelectionMode> {
    public ArmSelectionModeScrollBehaviour(ArmBlockEntity be) {
        super(
            SelectionModeIcon.class,
            SelectionModeIcon::from,
            CreateLang.translateDirect("logistics.when_multiple_outputs_available"),
            be,
            new SelectionModeValueBox()
        );
    }

    public enum SelectionModeIcon implements INamedIconOptions {
        ROUND_ROBIN(AllIcons.I_ARM_ROUND_ROBIN),
        FORCED_ROUND_ROBIN(AllIcons.I_ARM_FORCED_ROUND_ROBIN),
        PREFER_FIRST(AllIcons.I_ARM_PREFER_FIRST);

        private final String translationKey;
        private final AllIcons icon;

        SelectionModeIcon(AllIcons icon) {
            this.icon = icon;
            this.translationKey = "create.mechanical_arm.selection_mode." + Lang.asId(name());
        }

        public static SelectionModeIcon from(SelectionMode mode) {
            return switch (mode) {
                case ROUND_ROBIN -> ROUND_ROBIN;
                case FORCED_ROUND_ROBIN -> FORCED_ROUND_ROBIN;
                case PREFER_FIRST -> PREFER_FIRST;
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
