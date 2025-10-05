package com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox.ItemValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FilteringRenderer {

    public static void tick(MinecraftClient mc) {
        HitResult target = mc.crosshairTarget;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientWorld world = mc.world;
        BlockPos pos = result.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (mc.player.isSneaking())
            return;
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity sbe))
            return;

        ItemStack mainhandItem = mc.player.getStackInHand(Hand.MAIN_HAND);

        List<FilteringBehaviour<?>> behaviours;
        if (sbe instanceof FactoryPanelBlockEntity fpbe) {
            behaviours = FactoryPanelBehaviour.allBehaviours(fpbe);
        } else {
            FilteringBehaviour<?> behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
            if (behaviour instanceof SidedFilteringBehaviour sidedBehaviour) {
                behaviour = sidedBehaviour.get(result.getSide());
            }
            if (behaviour == null) {
                return;
            }
            behaviours = List.of(behaviour);
        }

        for (FilteringBehaviour<?> behaviour : behaviours) {
            if (!behaviour.isActive())
                continue;
            if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
                ((Sided) behaviour.slotPositioning).fromSide(result.getSide());
            if (!behaviour.slotPositioning.shouldRender(world, pos, state))
                continue;
            if (!behaviour.mayInteract(mc.player))
                continue;

            ItemStack filter = behaviour.getFilter();
            boolean isFilterSlotted = filter.getItem() instanceof FilterItem;
            boolean showCount = behaviour.isCountVisible();
            Text label = behaviour.getLabel();
            boolean hit = behaviour.slotPositioning.testHit(world, pos, state, target.getPos().subtract(Vec3d.of(pos)));

            Box emptyBB = new Box(Vec3d.ZERO, Vec3d.ZERO);
            Box bb = isFilterSlotted ? emptyBB.expand(.45f, .31f, .2f) : emptyBB.expand(.25f);

            ValueBox box = new ItemValueBox(label, bb, pos, filter, behaviour.getCountLabelForValueBox());
            box.passive(!hit || behaviour.bypassesInput(mainhandItem));

            Outliner.getInstance().showOutline(Pair.of("filter" + behaviour.netId(), pos), box.transform(behaviour.slotPositioning))
                .lineWidth(1 / 64f).withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null).highlightFace(result.getSide());

            if (!hit)
                continue;

            List<MutableText> tip = new ArrayList<>();
            tip.add(label.copy());
            tip.add(behaviour.getTip());
            if (showCount)
                tip.add(behaviour.getAmountTip());

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

        World level = be.getWorld();
        BlockPos blockPos = be.getPos();

        List<FilteringBehaviour<?>> behaviours;
        if (be instanceof FactoryPanelBlockEntity fpbe) {
            behaviours = FactoryPanelBehaviour.allBehaviours(fpbe);
        } else {
            FilteringBehaviour<?> behaviour = be.getBehaviour(FilteringBehaviour.TYPE);
            if (behaviour == null) {
                return;
            }
            behaviours = List.of(behaviour);
        }

        for (FilteringBehaviour<?> behaviour : behaviours) {
            if (!be.isVirtual()) {
                Entity cameraEntity = MinecraftClient.getInstance().cameraEntity;
                if (cameraEntity != null && level == cameraEntity.getEntityWorld()) {
                    float max = behaviour.getRenderDistance();
                    if (cameraEntity.getPos().squaredDistanceTo(VecHelper.getCenterOf(blockPos)) > (max * max)) {
                        continue;
                    }
                }
            }

            if (behaviour.behaviour == null || !behaviour.isActive())
                continue;
            if (behaviour.getFilter().isEmpty() && !(behaviour instanceof SidedFilteringBehaviour))
                continue;

            ValueBoxTransform slotPositioning = behaviour.slotPositioning;
            BlockState blockState = be.getCachedState();

            if (slotPositioning instanceof Sided sided) {
                Direction side = sided.getSide();
                for (Direction d : Iterate.directions) {
                    ItemStack filter = behaviour.getFilter(d);
                    if (filter.isEmpty())
                        continue;

                    sided.fromSide(d);
                    if (!slotPositioning.shouldRender(level, blockPos, blockState))
                        continue;

                    ms.push();
                    slotPositioning.transform(level, blockPos, blockState, ms);
                    if (blockState.isOf(AllBlocks.CONTRAPTION_CONTROLS))
                        ValueBoxRenderer.renderFlatItemIntoValueBox(filter, ms, buffer, light, overlay);
                    else
                        ValueBoxRenderer.renderItemIntoValueBox(filter, ms, buffer, light, overlay);
                    ms.pop();
                }
                sided.fromSide(side);
            } else if (slotPositioning.shouldRender(level, blockPos, blockState)) {
                ms.push();
                slotPositioning.transform(level, blockPos, blockState, ms);
                ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter(), ms, buffer, light, overlay);
                ms.pop();
            }
        }
    }

}
