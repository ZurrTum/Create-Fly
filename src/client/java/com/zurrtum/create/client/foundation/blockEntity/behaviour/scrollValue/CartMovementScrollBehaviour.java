package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.contraptions.mounted.CartAssemblerValueBoxTransform;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockEntity.CartMovementMode;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

public class CartMovementScrollBehaviour extends ScrollOptionBehaviour<CartMovementMode> {
    public CartMovementScrollBehaviour(SmartBlockEntity be) {
        super(
            CartMovementModeIcon.class,
            CartMovementModeIcon::from,
            CreateLang.translateDirect("contraptions.cart_movement_mode"),
            be,
            new CartAssemblerValueBoxTransform()
        );
    }

    public enum CartMovementModeIcon implements INamedIconOptions {

        ROTATE(AllIcons.I_CART_ROTATE),
        ROTATE_PAUSED(AllIcons.I_CART_ROTATE_PAUSED),
        ROTATION_LOCKED(AllIcons.I_CART_ROTATE_LOCKED);

        private final String translationKey;
        private final AllIcons icon;

        CartMovementModeIcon(AllIcons icon) {
            this.icon = icon;
            translationKey = "create.contraptions.cart_movement_mode." + Lang.asId(name());
        }

        public static CartMovementModeIcon from(CartMovementMode mode) {
            return switch (mode) {
                case ROTATE -> ROTATE;
                case ROTATE_PAUSED -> ROTATE_PAUSED;
                case ROTATION_LOCKED -> ROTATION_LOCKED;
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
