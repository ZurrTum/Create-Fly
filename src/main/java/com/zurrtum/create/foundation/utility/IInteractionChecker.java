package com.zurrtum.create.foundation.utility;

import net.minecraft.entity.player.PlayerEntity;

public interface IInteractionChecker {
    boolean canPlayerUse(PlayerEntity player);
}
