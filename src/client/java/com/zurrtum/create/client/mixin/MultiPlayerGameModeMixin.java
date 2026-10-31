package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import com.zurrtum.create.client.content.logistics.depot.EjectorTargetHandler;
import com.zurrtum.create.client.content.redstone.link.LinkHandler;
import com.zurrtum.create.client.content.trains.track.TrackPlacementClient;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import com.zurrtum.create.client.infrastructure.click.ClientRightClickHandle;
import com.zurrtum.create.client.infrastructure.click.ClientRightClickPreHandle;
import com.zurrtum.create.content.contraptions.glue.SuperGlueItem;
import com.zurrtum.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.zurrtum.create.content.equipment.tool.CardboardSwordItem;
import com.zurrtum.create.content.equipment.wrench.WrenchEventHandler;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationHelper;
import com.zurrtum.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import com.zurrtum.create.content.logistics.funnel.FunnelItem;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.displayLink.ClickToLinkBlockItem;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerItem;
import com.zurrtum.create.content.trains.schedule.ScheduleItemEntityInteraction;
import com.zurrtum.create.foundation.block.BreakControlBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.stream.Stream;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private GameType localPlayerMode;

    @Inject(method = "useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"), cancellable = true)
    private void interactBlock(
        LocalPlayer player,
        InteractionHand hand,
        BlockHitResult blockHit,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (localPlayerMode == GameType.SPECTATOR) {
            return;
        }
        Stream.<ClientRightClickPreHandle>of(
                SuperGlueItem::glueItemAlwaysPlacesWhenUsed,
                ArmInteractionPointHandler::rightClickingBlocksSelectsThem,
                ChainConveyorConnectionHandler::onItemUsedOnBlock,
                ValueSettingsInputHandler::onBlockActivated,
                LinkHandler::onBlockActivated,
                EjectorTargetHandler::rightClickingBlocksSelectsThem
            ).map(handler -> handler.onRightClickBlock(minecraft.level, player, hand, blockHit)).filter(Objects::nonNull)
            .findFirst()
            .ifPresentOrElse(cir::setReturnValue, () -> TrackPlacementClient.sendExtenderPacket(player, hand));
    }

    @Inject(method = "performUseItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void interactBlockInternal(
        LocalPlayer player,
        InteractionHand hand,
        BlockHitResult blockHit,
        CallbackInfoReturnable<InteractionResult> cir,
        @Local BlockPos pos,
        @Local ItemStack itemStack
    ) {
        Stream.<ClientRightClickHandle>of(
                WrenchEventHandler::useOwnWrenchLogicForCreateBlocks,
                ClipboardValueSettingsHandler::rightClickToCopy,
                ManualApplicationHelper::manualApplicationRecipesApplyInWorld,
                EdgeInteractionHandler::onBlockActivated,
                LinkedControllerItem::onItemUseFirst,
                CogwheelBlockItem::onItemUseFirst
            ).map(handler -> handler.onRightClickBlock(minecraft.level, player, itemStack, hand, blockHit, pos))
            .filter(Objects::nonNull).findFirst().ifPresent(cir::setReturnValue);
    }

    @Inject(method = "startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onDestroyBlock(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;F)V"), cancellable = true)
    private void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (EjectorTargetHandler.leftClickingBlocksDeselectsThem(
            minecraft.player,
            pos
        ) || ArmInteractionPointHandler.leftClickingBlocksDeselectsThem(pos)) {
            cir.setReturnValue(true);
            return;
        }
        CardboardSwordItem.cardboardSwordsMakeNoiseOnClick(minecraft.player, pos);
    }

    @Inject(method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onDestroyBlock(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;F)V"), cancellable = true)
    private void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (EjectorTargetHandler.leftClickingBlocksDeselectsThem(
            minecraft.player,
            pos
        ) || ArmInteractionPointHandler.leftClickingBlocksDeselectsThem(pos)) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(method = "lambda$startDestroyBlock$0(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;I)Lnet/minecraft/network/protocol/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onLeftClick1(
        MultiPlayerGameMode instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(
            minecraft.level,
            minecraft.player,
            minecraft.player.getMainHandItem(),
            direction,
            pos
        )) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "lambda$startDestroyBlock$1(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;I)Lnet/minecraft/network/protocol/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onLeftClick2(
        MultiPlayerGameMode instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(
            minecraft.level,
            minecraft.player,
            minecraft.player.getMainHandItem(),
            direction,
            pos
        )) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "lambda$continueDestroyBlock$0(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;I)Lnet/minecraft/network/protocol/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onLeftClick3(
        MultiPlayerGameMode instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(
            minecraft.level,
            minecraft.player,
            minecraft.player.getMainHandItem(),
            direction,
            pos
        )) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "lambda$continueDestroyBlock$1(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;I)Lnet/minecraft/network/protocol/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onLeftClick4(
        MultiPlayerGameMode instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(
            minecraft.level,
            minecraft.player,
            minecraft.player.getMainHandItem(),
            direction,
            pos
        )) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "performUseItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"))
    private boolean shouldCancelInteraction(
        LocalPlayer player,
        Operation<Boolean> original,
        @Local(argsOnly = true) InteractionHand hand,
        @Local BlockPos pos,
        @Local ItemStack itemStack
    ) {
        if (original.call(player)) {
            BlockState state = minecraft.level.getBlockState(pos);
            return !(HandCrankBlock.onBlockActivated(hand, state, itemStack) || AnalogLeverBlock.onBlockActivated(
                hand,
                state,
                itemStack
            ) || ExtendoGripItem.shouldInteraction(player, hand, itemStack));
        }
        return FunnelItem.funnelItemAlwaysPlacesWhenUsed(itemStack) || ClickToLinkBlockItem.linkableItemAlwaysPlacesWhenUsed(minecraft.level,
            pos,
            itemStack
        );
    }

    @WrapOperation(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult interactEntityAtLocation(
        Player player,
        Entity entity,
        InteractionHand hand,
        Vec3 location,
        Operation<InteractionResult> original
    ) {
        InteractionResult result = ScheduleItemEntityInteraction.interactWithConductor(entity, player, hand);
        if (result != null) {
            return result;
        }
        result = StockTickerInteractionHandler.interactWithLogisticsManager(entity, player, hand);
        if (result != null) {
            return result;
        }
        return original.call(player, entity, hand, location);
    }

    @WrapOperation(method = "destroyBlock(Lnet/minecraft/core/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean breakBlock(
        Level world,
        BlockPos pos,
        BlockState blockState,
        int updateFlags,
        Operation<Boolean> original,
        @Local BlockState oldState,
        @Local Block oldBlock
    ) {
        if (oldBlock instanceof BreakControlBlock controlBlock && !controlBlock.onDestroyedByPlayer(
            oldState,
            world,
            pos,
            minecraft.player
        )) {
            return false;
        }
        return original.call(world, pos, blockState, updateFlags);
    }
}