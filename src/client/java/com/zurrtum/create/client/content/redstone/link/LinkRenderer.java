package com.zurrtum.create.client.content.redstone.link;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class LinkRenderer {

    public static void tick(MinecraftClient mc) {
        HitResult target = mc.crosshairTarget;
        if (target == null || !(target instanceof BlockHitResult result))
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

    public static void renderOnBlockEntity(
        SmartBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {

        if (be == null || be.isRemoved())
            return;

        Entity cameraEntity = MinecraftClient.getInstance().cameraEntity;
        float max = AllConfigs.client().filterItemRenderDistance.getF();
        if (!be.isVirtual() && cameraEntity != null && cameraEntity.getPos().squaredDistanceTo(VecHelper.getCenterOf(be.getPos())) > (max * max))
            return;

        LinkBehaviour behaviour = be.getBehaviour(LinkBehaviour.TYPE);
        if (behaviour == null)
            return;

        for (boolean first : Iterate.trueAndFalse) {
            ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;
            ItemStack stack = first ? behaviour.getFirstStack() : behaviour.getLastStack();

            ms.push();
            transform.transform(be.getWorld(), be.getPos(), be.getCachedState(), ms);
            ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
            ms.pop();
        }

    }

}
