package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import com.zurrtum.create.content.equipment.tool.CardboardSwordItem;
import com.zurrtum.create.content.equipment.wrench.WrenchEventHandler;
import com.zurrtum.create.content.equipment.zapper.ZapperInteractionHandler;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationHelper;
import com.zurrtum.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import com.zurrtum.create.content.logistics.funnel.FunnelItem;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.displayLink.ClickToLinkBlockItem;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerItem;
import com.zurrtum.create.foundation.block.BreakControlBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import com.zurrtum.create.infrastructure.click.ServerRightClickHandle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void interactBlock(
        ServerPlayer player,
        Level world,
        ItemStack stack,
        InteractionHand hand,
        BlockHitResult hit,
        CallbackInfoReturnable<InteractionResult> cir,
        @Local BlockPos pos
    ) {
        Stream.<ServerRightClickHandle>of(
                WrenchEventHandler::useOwnWrenchLogicForCreateBlocks,
                ClipboardValueSettingsHandler::rightClickToCopy,
                ManualApplicationHelper::manualApplicationRecipesApplyInWorld,
                EdgeInteractionHandler::onBlockActivated,
                LinkedControllerItem::onItemUseFirst,
                CogwheelBlockItem::onItemUseFirst
            ).map(handler -> handler.onRightClickBlock(world, player, stack, hand, hit, pos)).filter(Objects::nonNull).findFirst()
            .ifPresent(cir::setReturnValue);
    }

    @Inject(method = "handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;II)V", at = @At("HEAD"), cancellable = true)
    private void leftClick(
        BlockPos pos,
        ServerboundPlayerActionPacket.Action action,
        Direction direction,
        int worldHeight,
        int sequence,
        CallbackInfo ci
    ) {
        ItemStack stack = player.getMainHandItem();
        if (ZapperInteractionHandler.leftClickingBlocksWithTheZapperSelectsTheBlock(player, stack) || ClipboardValueSettingsHandler.leftClickToPaste(level,
            player,
            stack,
            direction,
            pos
        )) {
            ci.cancel();
        } else if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            CardboardSwordItem.cardboardSwordsMakeNoiseOnClick(player, pos);
        }
    }

    @WrapOperation(method = "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
    private boolean shouldCancelInteraction(
        ServerPlayer instance,
        Operation<Boolean> original,
        @Local(argsOnly = true) Level world,
        @Local(argsOnly = true) ItemStack stack,
        @Local(argsOnly = true) InteractionHand hand,
        @Local BlockPos pos,
        @Local BlockState state
    ) {
        if (original.call(instance)) {
            return !(HandCrankBlock.onBlockActivated(hand, state, stack) || AnalogLeverBlock.onBlockActivated(
                hand,
                state,
                stack
            ) || ExtendoGripItem.shouldInteraction(player, hand, stack));
        }
        return FunnelItem.funnelItemAlwaysPlacesWhenUsed(stack) || ClickToLinkBlockItem.linkableItemAlwaysPlacesWhenUsed(world, pos, stack);
    }

    @WrapOperation(method = "destroyBlock(Lnet/minecraft/core/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"))
    private void postMine(ItemStack stack, Level world, BlockState state, BlockPos pos, Player miner, Operation<Void> original) {
        original.call(stack, world, state, pos, miner);
        if (!world.isClientSide() && state.getDestroySpeed(world, pos) != 0) {
            ExtendoGripItem.postMine(miner, stack);
        }
    }

    @WrapOperation(method = "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", ordinal = 1))
    private InteractionResult interactBlock(ItemStack stack, UseOnContext context, Operation<InteractionResult> original) {
        InteractionResult result = original.call(stack, context);
        if (result.consumesAction() && stack.getItem() instanceof BlockItem) {
            ExtendoGripItem.postPlace(context.getPlayer());
        }
        return result;
    }

    @WrapOperation(method = "destroyBlock(Lnet/minecraft/core/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean removeBlock(
        ServerLevel world,
        BlockPos pos,
        boolean move,
        Operation<Boolean> original,
        @Local(ordinal = 0) BlockState state,
        @Local Block block
    ) {
        if (block instanceof BreakControlBlock controlBlock && !controlBlock.onDestroyedByPlayer(state, world, pos, player)) {
            return false;
        }
        boolean remove = original.call(world, pos, move);
        if (remove) {
            SymmetryHandler.onBlockDestroyed(player, pos, state);
        }
        return remove;
    }
}