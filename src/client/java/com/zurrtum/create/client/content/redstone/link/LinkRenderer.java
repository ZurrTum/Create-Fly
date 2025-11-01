package com.zurrtum.create.client.content.redstone.link;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LinkRenderer {
    public static void tick(MinecraftClient mc) {
        HitResult target = mc.crosshairTarget;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientWorld world = mc.world;
        BlockPos pos = result.getBlockPos();

        LinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
        if (behaviour == null)
            return;

        Text freq1 = CreateLang.translateDirect("logistics.firstFrequency");
        Text freq2 = CreateLang.translateDirect("logistics.secondFrequency");

        for (boolean first : Iterate.trueAndFalse) {
            Box bb = new Box(Vec3d.ZERO, Vec3d.ZERO).expand(.25f);
            Text label = first ? freq1 : freq2;
            boolean hit = behaviour.testHit(first, target.getPos());
            ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;

            ValueBox box = new ValueBox(label, bb, pos).passive(!hit);
            boolean empty = behaviour.getNetworkKey().get(first).getStack().isEmpty();

            if (!empty)
                box.wideOutline();

            Outliner.getInstance().showOutline(Pair.of(first, pos), box.transform(transform)).highlightFace(result.getSide());

            if (!hit)
                continue;

            List<MutableText> tip = new ArrayList<>();
            tip.add(label.copy());
            tip.add(CreateLang.translateDirect(empty ? "logistics.filter.click_to_set" : "logistics.filter.click_to_replace"));
            Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
        }
    }

    @Nullable
    public static LinkRenderState getLinkRenderState(SmartBlockEntity be, ItemModelManager itemModelManager, double distance) {
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
            be.getWorld()
        );
    }

    public record LinkRenderState(
        ValueBoxTransform firstSlot, ItemRenderState firstState, float firstOffset, ValueBoxTransform secondSlot, ItemRenderState secondState,
        float secondOffset
    ) {
        public static LinkRenderState create(
            ValueBoxTransform firstSlot,
            ValueBoxTransform secondSlot,
            ItemModelManager itemModelManager,
            ItemStack firstStack,
            ItemStack secondStack,
            World world
        ) {
            ItemRenderState firstState = new ItemRenderState(), secondState = new ItemRenderState();
            firstState.displayContext = secondState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(firstState, firstStack, firstState.displayContext, world, null, 0);
            itemModelManager.update(secondState, secondStack, secondState.displayContext, world, null, 0);
            return new LinkRenderState(
                firstSlot,
                firstState,
                ValueBoxRenderer.customZOffset(firstStack.getItem()),
                secondSlot,
                secondState,
                ValueBoxRenderer.customZOffset(secondStack.getItem())
            );
        }

        public void render(BlockState blockState, OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
            ms.push();
            firstSlot.transform(blockState, ms);
            ValueBoxRenderer.renderItemIntoValueBox(firstState, queue, ms, light, firstOffset);
            ms.pop();
            ms.push();
            secondSlot.transform(blockState, ms);
            ValueBoxRenderer.renderItemIntoValueBox(secondState, queue, ms, light, secondOffset);
            ms.pop();
        }
    }
}
