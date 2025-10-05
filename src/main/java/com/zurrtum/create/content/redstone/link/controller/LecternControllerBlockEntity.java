package com.zurrtum.create.content.redstone.link.controller;

import com.zurrtum.create.*;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class LecternControllerBlockEntity extends SmartBlockEntity {
    private ContainerComponent controllerData = ContainerComponent.DEFAULT;
    public UUID user;
    public UUID prevUser;    // used only on client
    private boolean deactivatedThisTick;    // used only on server

    public LecternControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.LECTERN_CONTROLLER, pos, state);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState state) {
        super.onBlockReplaced(pos, state);
        dropController(state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.put("ControllerData", ContainerComponent.CODEC, controllerData);
        if (user != null)
            view.put("User", Uuids.INT_STREAM_CODEC, user);
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        view.put("ControllerData", ContainerComponent.CODEC, controllerData);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);

        controllerData = view.read("ControllerData", ContainerComponent.CODEC).orElse(ContainerComponent.DEFAULT);
        user = view.read("User", Uuids.INT_STREAM_CODEC).orElse(null);
    }

    public ItemStack getController() {
        return createLinkedController();
    }

    public boolean hasUser() {
        return user != null;
    }

    public boolean isUsedBy(PlayerEntity player) {
        return hasUser() && user.equals(player.getUuid());
    }

    public void tryStartUsing(PlayerEntity player) {
        if (!deactivatedThisTick && !hasUser() && !playerIsUsingLectern(player) && playerInRange(player, world, pos))
            startUsing(player);
    }

    public void tryStopUsing(PlayerEntity player) {
        if (isUsedBy(player))
            stopUsing(player);
    }

    private void startUsing(PlayerEntity player) {
        user = player.getUuid();
        AllSynchedDatas.IS_USING_LECTERN_CONTROLLER.set(player, true);
        sendData();
    }

    private void stopUsing(PlayerEntity player) {
        user = null;
        if (player != null)
            AllSynchedDatas.IS_USING_LECTERN_CONTROLLER.set(player, false);
        deactivatedThisTick = true;
        sendData();
    }

    public static boolean playerIsUsingLectern(PlayerEntity player) {
        return AllSynchedDatas.IS_USING_LECTERN_CONTROLLER.get(player);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient()) {
            AllClientHandle.INSTANCE.tryToggleActive(this);
            prevUser = user;
        }

        if (!world.isClient()) {
            deactivatedThisTick = false;

            if (!(world instanceof ServerWorld))
                return;
            if (user == null)
                return;

            Entity entity = world.getEntity(user);
            if (!(entity instanceof PlayerEntity player)) {
                stopUsing(null);
                return;
            }

            if (!playerInRange(player, world, pos) || !playerIsUsingLectern(player))
                stopUsing(player);
        }
    }

    public void setController(ItemStack newController) {
        if (newController != null) {
            controllerData = newController.getOrDefault(AllDataComponents.LINKED_CONTROLLER_ITEMS, ContainerComponent.DEFAULT);
            AllSoundEvents.CONTROLLER_PUT.playOnServer(world, pos);
        }
    }

    public void swapControllers(ItemStack stack, PlayerEntity player, Hand hand, BlockState state) {
        ItemStack newController = stack.copy();
        stack.setCount(0);
        if (player.getStackInHand(hand).isEmpty()) {
            player.setStackInHand(hand, createLinkedController());
        } else {
            dropController(state);
        }
        setController(newController);
    }

    public void dropController(BlockState state) {
        Entity entity = world.getEntity(user);
        if (entity instanceof PlayerEntity player)
            stopUsing(player);

        Direction dir = state.get(LecternControllerBlock.FACING);
        double x = pos.getX() + 0.5 + 0.25 * dir.getOffsetX();
        double y = pos.getY() + 1;
        double z = pos.getZ() + 0.5 + 0.25 * dir.getOffsetZ();
        ItemEntity itementity = new ItemEntity(world, x, y, z, createLinkedController());
        itementity.setToDefaultPickupDelay();
        world.spawnEntity(itementity);
        controllerData = ContainerComponent.DEFAULT;
    }

    public static boolean playerInRange(PlayerEntity player, World world, BlockPos pos) {
        //double modifier = world.isRemote ? 0 : 1.0;
        double reach = 0.4 * player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);// + modifier;
        return player.squaredDistanceTo(Vec3d.ofCenter(pos)) < reach * reach;
    }

    private ItemStack createLinkedController() {
        ItemStack stack = AllItems.LINKED_CONTROLLER.getDefaultStack();
        stack.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, controllerData);
        return stack;
    }
}
