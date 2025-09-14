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
import com.zurrtum.create.foundation.block.SoundControlBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.stream.Stream;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private GameMode gameMode;

    @Inject(method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"), cancellable = true)
    private void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (gameMode == GameMode.SPECTATOR) {
            return;
        }
        Stream.<ClientRightClickPreHandle>of(
                SuperGlueItem::glueItemAlwaysPlacesWhenUsed,
                ArmInteractionPointHandler::rightClickingBlocksSelectsThem,
                ChainConveyorConnectionHandler::onItemUsedOnBlock,
                ValueSettingsInputHandler::onBlockActivated,
                LinkHandler::onBlockActivated,
                EjectorTargetHandler::rightClickingBlocksSelectsThem
            ).map(handler -> handler.onRightClickBlock(client.world, player, hand, hitResult)).filter(Objects::nonNull).findFirst()
            .ifPresentOrElse(cir::setReturnValue, () -> TrackPlacementClient.sendExtenderPacket(player, hand));
    }

    @Inject(method = "interactBlockInternal(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void interactBlockInternal(
        ClientPlayerEntity player,
        Hand hand,
        BlockHitResult hit,
        CallbackInfoReturnable<ActionResult> cir,
        @Local BlockPos pos,
        @Local ItemStack stack
    ) {
        Stream.<ClientRightClickHandle>of(
                WrenchEventHandler::useOwnWrenchLogicForCreateBlocks,
                ClipboardValueSettingsHandler::rightClickToCopy,
                ManualApplicationHelper::manualApplicationRecipesApplyInWorld,
                EdgeInteractionHandler::onBlockActivated,
                LinkedControllerItem::onItemUseFirst,
                CogwheelBlockItem::onItemUseFirst
            ).map(handler -> handler.onRightClickBlock(client.world, player, stack, hand, hit, pos)).filter(Objects::nonNull).findFirst()
            .ifPresent(cir::setReturnValue);
    }

    @Inject(method = "attackBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onBlockBreaking(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;F)V"), cancellable = true)
    private void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (EjectorTargetHandler.leftClickingBlocksDeselectsThem(
            client.player,
            pos
        ) || ArmInteractionPointHandler.leftClickingBlocksDeselectsThem(pos)) {
            cir.setReturnValue(true);
            return;
        }
        CardboardSwordItem.cardboardSwordsMakeNoiseOnClick(client.player, pos);
    }

    @Inject(method = "updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onBlockBreaking(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;F)V"), cancellable = true)
    private void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (EjectorTargetHandler.leftClickingBlocksDeselectsThem(
            client.player,
            pos
        ) || ArmInteractionPointHandler.leftClickingBlocksDeselectsThem(pos)) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(method = "method_41936(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/network/packet/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onLeftClick1(
        ClientPlayerInteractionManager instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(client.world, client.player, client.player.getMainHandStack(), direction, pos)) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "method_41930(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/network/packet/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onLeftClick2(
        ClientPlayerInteractionManager instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(client.world, client.player, client.player.getMainHandStack(), direction, pos)) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "method_41935(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/network/packet/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onLeftClick3(
        ClientPlayerInteractionManager instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(client.world, client.player, client.player.getMainHandStack(), direction, pos)) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "method_41932(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/network/packet/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onLeftClick4(
        ClientPlayerInteractionManager instance,
        BlockPos pos,
        Operation<Boolean> original,
        @Local(argsOnly = true) Direction direction
    ) {
        if (ClipboardValueSettingsHandler.leftClickToPaste(client.world, client.player, client.player.getMainHandStack(), direction, pos)) {
            return false;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "interactBlockInternal(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"))
    private boolean shouldCancelInteraction(
        ClientPlayerEntity player,
        Operation<Boolean> original,
        @Local(argsOnly = true) Hand hand,
        @Local BlockPos pos,
        @Local ItemStack stack
    ) {
        if (original.call(player)) {
            BlockState state = client.world.getBlockState(pos);
            return !(HandCrankBlock.onBlockActivated(hand, state, stack) || AnalogLeverBlock.onBlockActivated(
                hand,
                state,
                stack
            ) || ExtendoGripItem.shouldInteraction(player, hand, stack));
        }
        return FunnelItem.funnelItemAlwaysPlacesWhenUsed(stack) || ClickToLinkBlockItem.linkableItemAlwaysPlacesWhenUsed(client.world, pos, stack);
    }

    @WrapOperation(method = "interactEntityAtLocation(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult interactEntityAtLocation(Entity entity, PlayerEntity player, Vec3d hitPos, Hand hand, Operation<ActionResult> original) {
        ActionResult result = ScheduleItemEntityInteraction.interactWithConductor(entity, player, hand);
        if (result != null) {
            return result;
        }
        result = StockTickerInteractionHandler.interactWithLogisticsManager(entity, player, hand);
        if (result != null) {
            return result;
        }
        return original.call(entity, player, hitPos, hand);
    }

    @WrapOperation(method = "breakBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean breakBlock(
        World world,
        BlockPos pos,
        BlockState newState,
        int flags,
        Operation<Boolean> original,
        @Local BlockState state,
        @Local Block block
    ) {
        if (block instanceof BreakControlBlock controlBlock && !controlBlock.onDestroyedByPlayer(state, world, pos, client.player)) {
            return false;
        }
        return original.call(world, pos, newState, flags);
    }

    @WrapOperation(method = "updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"))
    private BlockSoundGroup getHitSound(BlockState state, Operation<BlockSoundGroup> original, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(client.world, pos);
        }
        return original.call(state);
    }
}