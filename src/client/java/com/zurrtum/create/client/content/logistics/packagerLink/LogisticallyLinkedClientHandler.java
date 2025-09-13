package com.zurrtum.create.client.content.logistics.packagerLink;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LogisticallyLinkedClientHandler {

    private static UUID previouslyHeldFrequency;

    public static void tick(MinecraftClient mc) {
        previouslyHeldFrequency = null;

        ClientPlayerEntity player = mc.player;
        if (player == null)
            return;
        ItemStack mainHandItem = player.getMainHandStack();
        if (!(mainHandItem.getItem() instanceof LogisticallyLinkedBlockItem) || !LogisticallyLinkedBlockItem.isTuned(mainHandItem))
            return;

        NbtCompound tag = mainHandItem.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        Optional<UUID> uuid = tag.get("Freq", Uuids.INT_STREAM_CODEC);
        if (uuid.isEmpty())
            return;

        previouslyHeldFrequency = uuid.get();

        for (LogisticallyLinkedBehaviour behaviour : LogisticallyLinkedBehaviour.getAllPresent(previouslyHeldFrequency, false, true)) {
            SmartBlockEntity be = behaviour.blockEntity;
            VoxelShape shape = be.getCachedState().getOutlineShape(player.getWorld(), be.getPos());
            if (shape.isEmpty())
                continue;
            if (!player.getBlockPos().isWithinDistance(be.getPos(), 64))
                continue;
            List<Box> list = shape.getBoundingBoxes();
            for (int i = 0, size = list.size(); i < size; i++) {
                Box aabb = list.get(i);
                Outliner.getInstance().showAABB(Pair.of(behaviour, i), aabb.expand(-1 / 128f).offset(be.getPos()), 2).lineWidth(1 / 32f)
                    .disableLineNormals().colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
            }

        }
    }

    public static void tickPanel(FactoryPanelBehaviour fpb) {
        if (previouslyHeldFrequency == null)
            return;
        if (!previouslyHeldFrequency.equals(fpb.getNetwork()))
            return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null)
            return;
        if (!player.getBlockPos().isWithinDistance(fpb.getPos(), 64))
            return;

        Outliner.getInstance()
            .showAABB(fpb, FactoryPanelConnectionHandler.getBB(fpb.blockEntity.getCachedState(), fpb.getPanelPosition()).expand(-1.5 / 128f))
            .lineWidth(1 / 32f).disableLineNormals().colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
    }

}
