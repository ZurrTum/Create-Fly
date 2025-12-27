package com.zurrtum.create.content.redstone.link.controller;

import com.zurrtum.create.*;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class LecternControllerBlockEntity extends SmartBlockEntity {
    private ItemContainerContents controllerData = ItemContainerContents.EMPTY;
    public UUID user;
    public UUID prevUser;    // used only on client
    private boolean deactivatedThisTick;    // used only on server

    public LecternControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.LECTERN_CONTROLLER, pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        dropController(state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.store("ControllerData", ItemContainerContents.CODEC, controllerData);
        if (user != null)
            view.store("User", UUIDUtil.CODEC, user);
    }

    @Override
    public void writeSafe(ValueOutput view) {
        super.writeSafe(view);
        view.store("ControllerData", ItemContainerContents.CODEC, controllerData);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);

        controllerData = view.read("ControllerData", ItemContainerContents.CODEC).orElse(ItemContainerContents.EMPTY);
        user = view.read("User", UUIDUtil.CODEC).orElse(null);
    }

    public ItemStack getController() {
        return createLinkedController();
    }

    public boolean hasUser() {
        return user != null;
    }

    public boolean isUsedBy(Player player) {
        return hasUser() && user.equals(player.getUUID());
    }

    public void tryStartUsing(Player player) {
        if (!deactivatedThisTick && !hasUser() && !playerIsUsingLectern(player) && playerInRange(player, level, worldPosition))
            startUsing(player);
    }

    public void tryStopUsing(Player player) {
        if (isUsedBy(player))
            stopUsing(player);
    }

    private void startUsing(Player player) {
        user = player.getUUID();
        AllSynchedDatas.IS_USING_LECTERN_CONTROLLER.set(player, true);
        sendData();
    }

    private void stopUsing(Player player) {
        user = null;
        if (player != null)
            AllSynchedDatas.IS_USING_LECTERN_CONTROLLER.set(player, false);
        deactivatedThisTick = true;
        sendData();
    }

    public static boolean playerIsUsingLectern(Player player) {
        return AllSynchedDatas.IS_USING_LECTERN_CONTROLLER.get(player);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            AllClientHandle.INSTANCE.tryToggleActive(this);
            prevUser = user;
        }

        if (!level.isClientSide()) {
            deactivatedThisTick = false;

            if (!(level instanceof ServerLevel))
                return;
            if (user == null)
                return;

            Entity entity = level.getEntity(user);
            if (!(entity instanceof Player player)) {
                stopUsing(null);
                return;
            }

            if (!playerInRange(player, level, worldPosition) || !playerIsUsingLectern(player))
                stopUsing(player);
        }
    }

    public void setController(ItemStack newController) {
        if (newController != null) {
            controllerData = newController.getOrDefault(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemContainerContents.EMPTY);
            AllSoundEvents.CONTROLLER_PUT.playOnServer(level, worldPosition);
        }
    }

    public void swapControllers(ItemStack stack, Player player, InteractionHand hand, BlockState state) {
        ItemStack newController = stack.copy();
        stack.setCount(0);
        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, createLinkedController());
        } else {
            dropController(state);
        }
        setController(newController);
    }

    public void dropController(BlockState state) {
        Entity entity = level.getEntity(user);
        if (entity instanceof Player player)
            stopUsing(player);

        Direction dir = state.getValue(LecternControllerBlock.FACING);
        double x = worldPosition.getX() + 0.5 + 0.25 * dir.getStepX();
        double y = worldPosition.getY() + 1;
        double z = worldPosition.getZ() + 0.5 + 0.25 * dir.getStepZ();
        ItemEntity itementity = new ItemEntity(level, x, y, z, createLinkedController());
        itementity.setDefaultPickUpDelay();
        level.addFreshEntity(itementity);
        controllerData = ItemContainerContents.EMPTY;
    }

    public static boolean playerInRange(Player player, Level world, BlockPos pos) {
        //double modifier = world.isRemote ? 0 : 1.0;
        double reach = 0.4 * player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);// + modifier;
        return player.distanceToSqr(Vec3.atCenterOf(pos)) < reach * reach;
    }

    private ItemStack createLinkedController() {
        ItemStack stack = AllItems.LINKED_CONTROLLER.getDefaultInstance();
        stack.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, controllerData);
        return stack;
    }
}
