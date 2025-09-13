package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox.IconValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox.TextValueBox;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
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

import java.util.ArrayList;
import java.util.List;

public class ScrollValueRenderer {

    public static void tick(MinecraftClient mc) {
        HitResult target = mc.crosshairTarget;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientWorld world = mc.world;
        if (world == null) {
            return;
        }
        BlockPos pos = result.getBlockPos();
        Direction face = result.getSide();

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

        ItemStack mainhandItem = mc.player.getStackInHand(Hand.MAIN_HAND);
        boolean clipboard = behaviour.bypassesInput(mainhandItem);
        if (behaviour.needsWrench && !mainhandItem.isOf(AllItems.WRENCH) && !clipboard)
            return;
        boolean highlight = behaviour.testHit(target.getPos()) && !clipboard;

        if (Screen.hasControlDown()) {
            List<? extends SmartBlockEntity> bulks = behaviour.getBulk();
            if (bulks != null) {
                for (SmartBlockEntity smartBlockEntity : bulks) {
                    if (smartBlockEntity.getBehaviour(ScrollValueBehaviour.TYPE) instanceof ScrollValueBehaviour<?, ?> other) {
                        addBox(smartBlockEntity.getPos(), face, other, highlight);
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

        List<MutableText> tip = new ArrayList<>();
        tip.add(behaviour.label.copy());
        tip.add(CreateLang.translateDirect("gui.value_settings.hold_to_edit"));
        Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
    }

    protected static void addBox(BlockPos pos, Direction face, ScrollValueBehaviour<?, ?> behaviour, boolean highlight) {
        Box bb = new Box(Vec3d.ZERO, Vec3d.ZERO).expand(.5f).shrink(0, 0, -.5f).offset(0, 0, -.125f);
        Text label = behaviour.label;
        ValueBox box;

        if (behaviour instanceof ScrollOptionBehaviour<?> optionBehaviour) {
            box = new IconValueBox(label, optionBehaviour.getIconForSelected(), bb, pos);
        } else {
            box = new TextValueBox(label, bb, pos, Text.literal(behaviour.formatValue()));
        }

        box.passive(!highlight).wideOutline();

        Outliner.getInstance().showOutline(behaviour, box.transform(behaviour.slotPositioning)).highlightFace(face);
    }

}
