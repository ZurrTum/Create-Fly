package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox.IconValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox.TextValueBox;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ScrollValueRenderer {

    public static void tick(Minecraft mc) {
        HitResult target = mc.hitResult;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientLevel world = mc.level;
        if (world == null) {
            return;
        }
        BlockPos pos = result.getBlockPos();
        Direction face = result.getDirection();

        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity sbe)) {
            return;
        }

        ScrollValueBehaviour<?, ?> behaviour = sbe.getBehaviour(ScrollValueBehaviour.TYPE);
        if (behaviour == null) {
            return;
        }

        if (!behaviour.isActive()) {
            Outliner.getInstance().remove(behaviour);
            return;
        }

        ItemStack mainhandItem = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean clipboard = behaviour.bypassesInput(mainhandItem);
        if (behaviour.needsWrench && !mainhandItem.is(AllItems.WRENCH) && !clipboard)
            return;
        boolean highlight = behaviour.testHit(target.getLocation()) && !clipboard;

        if (AllKeys.hasControlDown()) {
            List<? extends SmartBlockEntity> bulks = behaviour.getBulk();
            if (bulks != null) {
                for (SmartBlockEntity smartBlockEntity : bulks) {
                    if (smartBlockEntity.getBehaviour(ScrollValueBehaviour.TYPE) instanceof ScrollValueBehaviour<?, ?> other) {
                        addBox(smartBlockEntity.getBlockPos(), face, other, highlight);
                    }
                }
            } else {
                addBox(pos, face, behaviour, highlight);
            }
        } else {
            addBox(pos, face, behaviour, highlight);
        }

        if (!highlight)
            return;

        List<MutableComponent> tip = new ArrayList<>();
        tip.add(behaviour.label.copy());
        tip.add(CreateLang.translateDirect("gui.value_settings.hold_to_edit"));
        Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
    }

    protected static void addBox(BlockPos pos, Direction face, ScrollValueBehaviour<?, ?> behaviour, boolean highlight) {
        AABB bb = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.5f).contract(0, 0, -.5f).move(0, 0, -.125f);
        Component label = behaviour.label;
        ValueBox box;

        if (behaviour instanceof ScrollOptionBehaviour<?> optionBehaviour) {
            box = new IconValueBox(label, optionBehaviour.getIconForSelected(), bb, pos);
        } else {
            box = new TextValueBox(label, bb, pos, Component.literal(behaviour.formatValue()));
        }

        box.passive(!highlight).wideOutline();

        Outliner.getInstance().showOutline(behaviour, box.transform(behaviour.slotPositioning)).highlightFace(face);
    }

}
