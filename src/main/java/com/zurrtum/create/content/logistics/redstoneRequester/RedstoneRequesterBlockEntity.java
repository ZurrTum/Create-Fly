package com.zurrtum.create.content.logistics.redstoneRequester;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.packet.s2c.RedstoneRequesterEffectPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class RedstoneRequesterBlockEntity extends StockCheckingBlockEntity implements MenuProvider {

    public AbstractComputerBehaviour computerBehaviour;
    public boolean allowPartialRequests;
    public PackageOrderWithCrafts encodedRequest = PackageOrderWithCrafts.empty();
    public String encodedTargetAdress = "";

    public boolean lastRequestSucceeded;

    protected boolean redstonePowered;

    public RedstoneRequesterBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.REDSTONE_REQUESTER, pos, state);
        allowPartialRequests = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    protected void onRedstonePowerChanged() {
        boolean hasNeighborSignal = world.isReceivingRedstonePower(pos);
        if (redstonePowered == hasNeighborSignal)
            return;

        lastRequestSucceeded = false;
        if (hasNeighborSignal)
            triggerRequest();

        redstonePowered = hasNeighborSignal;
        notifyUpdate();
    }

    public void triggerRequest() {
        if (encodedRequest.isEmpty())
            return;

        boolean anySucceeded = false;

        InventorySummary summaryOfOrder = new InventorySummary();
        encodedRequest.stacks().forEach(summaryOfOrder::add);

        InventorySummary summary = getAccurateSummary();
        for (BigItemStack entry : summaryOfOrder.getStacks()) {
            if (summary.getCountOf(entry.stack) >= entry.count) {
                anySucceeded = true;
                continue;
            }
            if (!allowPartialRequests && world instanceof ServerWorld serverLevel) {
                serverLevel.getServer().getPlayerManager().sendToAround(
                    null,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    32,
                    serverLevel.getRegistryKey(),
                    new RedstoneRequesterEffectPacket(pos, false)
                );
                return;
            }
        }

        broadcastPackageRequest(RequestType.REDSTONE, encodedRequest, null, encodedTargetAdress);
        if (world instanceof ServerWorld serverLevel)
            serverLevel.getServer().getPlayerManager().sendToAround(
                null,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                32,
                serverLevel.getRegistryKey(),
                new RedstoneRequesterEffectPacket(pos, anySucceeded)
            );
        lastRequestSucceeded = true;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        redstonePowered = view.getBoolean("Powered", false);
        lastRequestSucceeded = view.getBoolean("Success", false);
        allowPartialRequests = view.getBoolean("AllowPartial", false);
        encodedRequest = view.read("EncodedRequest", PackageOrderWithCrafts.CODEC).orElse(PackageOrderWithCrafts.empty());
        encodedTargetAdress = view.getString("EncodedAddress", "");
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        view.putBoolean("AllowPartial", allowPartialRequests);
        view.putString("EncodedAddress", encodedTargetAdress);
        view.put("EncodedRequest", PackageOrderWithCrafts.CODEC, encodedRequest);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Powered", redstonePowered);
        view.putBoolean("Success", lastRequestSucceeded);
        view.putBoolean("AllowPartial", allowPartialRequests);
        view.putString("EncodedAddress", encodedTargetAdress);
        view.put("EncodedRequest", PackageOrderWithCrafts.CODEC, encodedRequest);
    }

    public ActionResult use(PlayerEntity player) {
        if (player == null || player.isInSneakingPose())
            return ActionResult.PASS;
        if (FakePlayerHandler.has(player))
            return ActionResult.PASS;
        if (world.isClient)
            return ActionResult.SUCCESS;
        if (!behaviour.mayInteractMessage(player))
            return ActionResult.SUCCESS;

        openHandledScreen((ServerPlayerEntity) player);
        return ActionResult.SUCCESS;
    }

    @Override
    public Text getDisplayName() {
        return Text.empty();
    }

    @Override
    public RedstoneRequesterMenu createMenu(int pContainerId, PlayerInventory pPlayerInventory, PlayerEntity pPlayer, RegistryByteBuf extraData) {
        extraData.writeBlockPos(pos);
        return new RedstoneRequesterMenu(pContainerId, pPlayerInventory, this);
    }

    public void playEffect(boolean success) {
        Vec3d vec3 = Vec3d.ofCenter(pos);
        if (success) {
            AllSoundEvents.CONFIRM.playAt(world, pos, 0.5f, 1.5f, false);
            AllSoundEvents.STOCK_LINK.playAt(world, pos, 1.0f, 1.0f, false);
            world.addParticleClient(AllParticleTypes.WIFI, vec3.x, vec3.y, vec3.z, 1, 1, 1);
        } else {
            AllSoundEvents.DENY.playAt(world, pos, 0.5f, 1, false);
            world.addParticleClient(ParticleTypes.ENCHANTED_HIT, vec3.x, vec3.y + 1, vec3.z, 0, 0, 0);
        }
    }

}