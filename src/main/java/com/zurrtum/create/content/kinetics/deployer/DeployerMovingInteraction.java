package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.mounted.MountedContraption;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.UUID;

import static com.zurrtum.create.Create.LOGGER;

public class DeployerMovingInteraction extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        MutablePair<StructureBlockInfo, MovementContext> actor = contraptionEntity.getContraption().getActorAt(localPos);
        if (actor == null || actor.right == null)
            return false;

        MovementContext ctx = actor.right;
        ItemStack heldStack = player.getStackInHand(activeHand);
        if (heldStack.isOf(AllItems.WRENCH)) {
            Mode mode = ctx.blockEntityData.get("Mode", Mode.CODEC).orElse(Mode.PUNCH);
            ctx.blockEntityData.put("Mode", Mode.CODEC, mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH);

        } else {
            if (ctx.world.isClient)
                return true; // we'll try again on the server side
            DeployerPlayer fake;

            if (!(ctx.temporaryData instanceof DeployerFakePlayer) && ctx.world instanceof ServerWorld) {
                UUID owner = ctx.blockEntityData.get("Owner", Uuids.INT_STREAM_CODEC).orElse(null);
                String ownerName = ctx.blockEntityData.get("Owner", Codec.STRING).orElse(null);
                DeployerPlayer deployerFakePlayer = DeployerPlayer.create((ServerWorld) ctx.world, owner, ownerName);
                deployerFakePlayer.setOnMinecartContraption(ctx.contraption instanceof MountedContraption);
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(contraptionEntity.getErrorReporterContext(), LOGGER)) {
                    NbtCompound inventory = ctx.blockEntityData.get("Inventory", NbtCompound.CODEC).orElseGet(NbtCompound::new);
                    ReadView view = NbtReadView.create(logging, ctx.world.getRegistryManager(), inventory);
                    deployerFakePlayer.cast().getInventory().readData(view.getTypedListView("Inventory", StackWithSlot.CODEC));
                }
                ctx.temporaryData = fake = deployerFakePlayer;
                ctx.blockEntityData.remove("Inventory");
            } else
                fake = (DeployerPlayer) ctx.temporaryData;

            if (fake == null)
                return false;

            ServerPlayerEntity serverPlayer = fake.cast();
            ItemStack deployerItem = serverPlayer.getMainHandStack();
            player.setStackInHand(activeHand, deployerItem.copy());
            serverPlayer.setStackInHand(Hand.MAIN_HAND, heldStack.copy());
            if (!heldStack.isEmpty()) {
                RegistryOps<NbtElement> ops = player.getRegistryManager().getOps(NbtOps.INSTANCE);
                ctx.blockEntityData.put("HeldItem", ItemStack.CODEC, ops, heldStack);
                ctx.data.put("HeldItem", ItemStack.CODEC, ops, heldStack);
            }
        }
        //		if (index >= 0)
        //			setContraptionActorData(contraptionEntity, index, info, ctx);
        return true;
    }
}
