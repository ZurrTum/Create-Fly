package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.logistics.tunnel.BrassTunnelModeSlot;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity.SelectionMode;

public class TunnelSelectionModeScrollBehaviour extends ScrollOptionBehaviour<SelectionMode> {
    public TunnelSelectionModeScrollBehaviour(BrassTunnelBlockEntity be) {
        super(
            SelectionModeIcon.class,
            SelectionModeIcon::from,
            CreateLang.translateDirect("logistics.when_multiple_outputs_available"),
            be,
            new BrassTunnelModeSlot()
        );
        onlyActiveWhen(be::hasDistributionBehaviour);
    }

    public enum SelectionModeIcon implements INamedIconOptions {
        SPLIT(AllIcons.I_TUNNEL_SPLIT),
        FORCED_SPLIT(AllIcons.I_TUNNEL_FORCED_SPLIT),
        ROUND_ROBIN(AllIcons.I_TUNNEL_ROUND_ROBIN),
        FORCED_ROUND_ROBIN(AllIcons.I_TUNNEL_FORCED_ROUND_ROBIN),
        PREFER_NEAREST(AllIcons.I_TUNNEL_PREFER_NEAREST),
        RANDOMIZE(AllIcons.I_TUNNEL_RANDOMIZE),
        SYNCHRONIZE(AllIcons.I_TUNNEL_SYNCHRONIZE);

        private final String translationKey;
        private final AllIcons icon;

        SelectionModeIcon(AllIcons icon) {
            this.icon = icon;
            this.translationKey = "create.tunnel.selection_mode." + Lang.asId(name());
        }

        public static SelectionModeIcon from(SelectionMode mode) {
            return switch (mode) {
                case SPLIT -> SPLIT;
                case FORCED_SPLIT -> FORCED_SPLIT;
                case ROUND_ROBIN -> ROUND_ROBIN;
                case FORCED_ROUND_ROBIN -> FORCED_ROUND_ROBIN;
                case PREFER_NEAREST -> PREFER_NEAREST;
                case RANDOMIZE -> RANDOMIZE;
                case SYNCHRONIZE -> SYNCHRONIZE;
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
