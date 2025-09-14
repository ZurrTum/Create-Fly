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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.stream.Stream;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void interactBlock(
        ServerPlayerEntity player,
        World world,
        ItemStack stack,
        Hand hand,
        BlockHitResult hit,
        CallbackInfoReturnable<ActionResult> cir,
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

    @Inject(method = "processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;II)V", at = @At("HEAD"), cancellable = true)
    private void leftClick(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        ItemStack stack = player.getMainHandStack();
        if (ZapperInteractionHandler.leftClickingBlocksWithTheZapperSelectsTheBlock(player, stack) || ClipboardValueSettingsHandler.leftClickToPaste(world,
            player,
            stack,
            direction,
            pos
        )) {
            ci.cancel();
        } else if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            CardboardSwordItem.cardboardSwordsMakeNoiseOnClick(player, pos);
        }
    }

    @WrapOperation(method = "interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldCancelInteraction()Z"))
    private boolean shouldCancelInteraction(
        ServerPlayerEntity instance,
        Operation<Boolean> original,
        @Local(argsOnly = true) World world,
        @Local(argsOnly = true) ItemStack stack,
        @Local(argsOnly = true) Hand hand,
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

    @WrapOperation(method = "tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;postMine(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void postMine(ItemStack stack, World world, BlockState state, BlockPos pos, PlayerEntity miner, Operation<Void> original) {
        original.call(stack, world, state, pos, miner);
        if (!world.isClient && state.getHardness(world, pos) != 0) {
            ExtendoGripItem.postMine(miner, stack);
        }
    }

    @WrapOperation(method = "interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", ordinal = 1))
    private ActionResult interactBlock(ItemStack stack, ItemUsageContext context, Operation<ActionResult> original) {
        ActionResult result = original.call(stack, context);
        if (result.isAccepted() && stack.getItem() instanceof BlockItem) {
            ExtendoGripItem.postPlace(context.getPlayer());
        }
        return result;
    }

    @WrapOperation(method = "tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private boolean removeBlock(
        ServerWorld world,
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