package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RailState.class)
public class RailStateMixin {
    @Shadow
    @Final
    private BaseRailBlock block;

    @Definition(id = "shape", local = @Local(type = RailShape.class))
    @Definition(id = "NORTH_SOUTH", field = "Lnet/minecraft/world/level/block/state/properties/RailShape;NORTH_SOUTH:Lnet/minecraft/world/level/block/state/properties/RailShape;")
    @Expression("shape == NORTH_SOUTH")
    @ModifyExpressionValue(method = "connectTo(Lnet/minecraft/world/level/block/RailState;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean modifyCheck1(boolean original) {
        return original && !(block instanceof CartAssemblerBlock);
    }

    @Definition(id = "shape", local = @Local(type = RailShape.class))
    @Definition(id = "EAST_WEST", field = "Lnet/minecraft/world/level/block/state/properties/RailShape;EAST_WEST:Lnet/minecraft/world/level/block/state/properties/RailShape;")
    @Expression("shape == EAST_WEST")
    @ModifyExpressionValue(method = "connectTo(Lnet/minecraft/world/level/block/RailState;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean modifyCheck2(boolean original) {
        return original && !(block instanceof CartAssemblerBlock);
    }
}
