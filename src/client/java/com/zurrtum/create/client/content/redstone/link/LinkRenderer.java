package com.zurrtum.create.client.content.redstone.link;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LinkRenderer {
    public static void tick(Minecraft mc) {
        HitResult target = mc.hitResult;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientLevel world = mc.level;
        BlockPos pos = result.getBlockPos();

        LinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
        if (behaviour == null)
            return;

        Component freq1 = CreateLang.translateDirect("logistics.firstFrequency");
        Component freq2 = CreateLang.translateDirect("logistics.secondFrequency");

        for (boolean first : Iterate.trueAndFalse) {
            AABB bb = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25f);
            Component label = first ? freq1 : freq2;
            boolean hit = behaviour.testHit(first, target.getLocation());
            ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;

            ValueBox box = new ValueBox(label, bb, pos).passive(!hit);
            boolean empty = behaviour.getNetworkKey().get(first).getStack().isEmpty();

            if (!empty)
                box.wideOutline();

            Outliner.getInstance().showOutline(Pair.of(first, pos), box.transform(transform)).highlightFace(result.getDirection());

            if (!hit)
                continue;

            List<MutableComponent> tip = new ArrayList<>();
            tip.add(label.copy());
            tip.add(CreateLang.translateDirect(empty ? "logistics.filter.click_to_set" : "logistics.filter.click_to_replace"));
            Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
        }
    }

    @Nullable
    public static LinkRenderState getLinkRenderState(SmartBlockEntity be, ItemModelResolver itemModelManager, double distance) {
        LinkBehaviour behaviour = be.getBehaviour(LinkBehaviour.TYPE);
        if (behaviour == null || behaviour.behaviour == null) {
            return null;
        }
        float max = behaviour.getRenderDistance();
        if (max * max < distance) {
            return null;
        }
        return LinkRenderState.create(
            behaviour.firstSlot,
            behaviour.secondSlot,
            itemModelManager,
            behaviour.getFirstStack(),
            behaviour.getLastStack(),
            be.getLevel()
        );
    }

    public record LinkRenderState(
        ValueBoxTransform firstSlot, ItemStackRenderState firstState, float firstOffset, ValueBoxTransform secondSlot,
        ItemStackRenderState secondState, float secondOffset
    ) {
        public static LinkRenderState create(
            ValueBoxTransform firstSlot,
            ValueBoxTransform secondSlot,
            ItemModelResolver itemModelManager,
            ItemStack firstStack,
            ItemStack secondStack,
            Level world
        ) {
            ItemStackRenderState firstState = new ItemStackRenderState(), secondState = new ItemStackRenderState();
            firstState.displayContext = secondState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(firstState, firstStack, firstState.displayContext, world, null, 0);
            itemModelManager.appendItemLayers(secondState, secondStack, secondState.displayContext, world, null, 0);
            return new LinkRenderState(
                firstSlot,
                firstState,
                ValueBoxRenderer.customZOffset(firstStack.getItem()),
                secondSlot,
                secondState,
                ValueBoxRenderer.customZOffset(secondStack.getItem())
            );
        }

        public void render(BlockState blockState, SubmitNodeCollector queue, PoseStack ms, int light) {
            ms.pushPose();
            firstSlot.transform(blockState, ms);
            ValueBoxRenderer.renderItemIntoValueBox(firstState, queue, ms, light, firstOffset);
            ms.popPose();
            ms.pushPose();
            secondSlot.transform(blockState, ms);
            ValueBoxRenderer.renderItemIntoValueBox(secondState, queue, ms, light, secondOffset);
            ms.popPose();
        }
    }
}
