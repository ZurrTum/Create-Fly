package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.mounted.MountedContraption;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.UUID;

import static com.zurrtum.create.Create.LOGGER;

public class DeployerMovingInteraction extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(
        Player player,
        InteractionHand activeHand,
        BlockPos localPos,
        AbstractContraptionEntity contraptionEntity
    ) {
        MutablePair<StructureBlockInfo, MovementContext> actor = contraptionEntity.getContraption().getActorAt(localPos);
        if (actor == null || actor.right == null)
            return false;

        MovementContext ctx = actor.right;
        ItemStack heldStack = player.getItemInHand(activeHand);
        if (heldStack.is(AllItems.WRENCH)) {
            Mode mode = ctx.blockEntityData.read("Mode", Mode.CODEC).orElse(Mode.PUNCH);
            ctx.blockEntityData.store("Mode", Mode.CODEC, mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH);

        } else {
            if (ctx.world.isClientSide())
                return true; // we'll try again on the server side
            DeployerPlayer fake;

            if (!(ctx.temporaryData instanceof DeployerFakePlayer) && ctx.world instanceof ServerLevel) {
                UUID owner = ctx.blockEntityData.read("Owner", UUIDUtil.CODEC).orElse(null);
                String ownerName = ctx.blockEntityData.read("Owner", Codec.STRING).orElse(null);
                DeployerPlayer deployerFakePlayer = DeployerPlayer.create((ServerLevel) ctx.world, owner, ownerName);
                deployerFakePlayer.setOnMinecartContraption(ctx.contraption instanceof MountedContraption);
                try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(contraptionEntity.problemPath(), LOGGER)) {
                    CompoundTag inventory = ctx.blockEntityData.read("Inventory", CompoundTag.CODEC).orElseGet(CompoundTag::new);
                    ValueInput view = TagValueInput.create(logging, ctx.world.registryAccess(), inventory);
                    deployerFakePlayer.cast().getInventory().load(view.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
                }
                ctx.temporaryData = fake = deployerFakePlayer;
                ctx.blockEntityData.remove("Inventory");
            } else
                fake = (DeployerPlayer) ctx.temporaryData;

            if (fake == null)
                return false;

            ServerPlayer serverPlayer = fake.cast();
            ItemStack deployerItem = serverPlayer.getMainHandItem();
            player.setItemInHand(activeHand, deployerItem.copy());
            serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, heldStack.copy());
            if (!heldStack.isEmpty()) {
                RegistryOps<Tag> ops = player.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                ctx.blockEntityData.store("HeldItem", ItemStack.CODEC, ops, heldStack);
                ctx.data.store("HeldItem", ItemStack.CODEC, ops, heldStack);
            }
        }
        //		if (index >= 0)
        //			setContraptionActorData(contraptionEntity, index, info, ctx);
        return true;
    }
}
