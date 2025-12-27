package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.kinetics.steamEngine.SteamEngineValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.network.chat.Component;

public class RotationDirectionScrollBehaviour extends ScrollOptionBehaviour<RotationDirection> {
    public RotationDirectionScrollBehaviour(SmartBlockEntity be, Component title, ValueBoxTransform slot) {
        super(RotationDirectionIcon.class, RotationDirectionIcon::from, title, be, slot);
    }

    public static RotationDirectionScrollBehaviour windmill(WindmillBearingBlockEntity be) {
        return new RotationDirectionScrollBehaviour(
            be,
            CreateLang.translateDirect("contraptions.windmill.rotation_direction"),
            getMovementModeSlot()
        );
    }

    public static BlockEntityBehaviour<SmartBlockEntity> engine(SteamEngineBlockEntity be) {
        return new RotationDirectionScrollBehaviour(
            be,
            CreateLang.translateDirect("contraptions.windmill.rotation_direction"),
            new SteamEngineValueBox()
        ).onlyActiveWhen(() -> {
            PoweredShaftBlockEntity shaft = be.getShaft();
            return shaft == null || !shaft.hasSource();
        });
    }

    private enum RotationDirectionIcon implements INamedIconOptions {

        CLOCKWISE(AllIcons.I_REFRESH),
        COUNTER_CLOCKWISE(AllIcons.I_ROTATE_CCW);

        private final String translationKey;
        private final AllIcons icon;

        RotationDirectionIcon(AllIcons icon) {
            this.icon = icon;
            translationKey = "create.generic." + Lang.asId(name());
        }

        public static RotationDirectionIcon from(RotationDirection direction) {
            return switch (direction) {
                case CLOCKWISE -> CLOCKWISE;
                case COUNTER_CLOCKWISE -> COUNTER_CLOCKWISE;
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
