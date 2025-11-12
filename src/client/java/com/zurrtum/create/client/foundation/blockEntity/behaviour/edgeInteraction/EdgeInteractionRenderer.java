package com.zurrtum.create.client.foundation.blockEntity.behaviour.edgeInteraction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.crafter.CrafterHelper;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EdgeInteractionRenderer {

    public static void tick(Minecraft mc) {
        HitResult target = mc.hitResult;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientLevel world = mc.level;
        BlockPos pos = result.getBlockPos();
        Player player = mc.player;
        ItemStack heldItem = player.getMainHandItem();

        if (player.isShiftKeyDown())
            return;
        EdgeInteractionBehaviour behaviour = BlockEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
        if (behaviour == null)
            return;
        if (!behaviour.requiredItem.test(heldItem.getItem()))
            return;

        Direction face = result.getDirection();
        List<Direction> connectiveSides = EdgeInteractionHandler.getConnectiveSides(world, pos, face, behaviour);
        if (connectiveSides.isEmpty())
            return;

        Direction closestEdge = connectiveSides.getFirst();
        double bestDistance = Double.MAX_VALUE;
        Vec3 center = VecHelper.getCenterOf(pos);
        for (Direction direction : connectiveSides) {
            double distance = Vec3.atLowerCornerOf(direction.getUnitVec3i()).subtract(target.getLocation().subtract(center)).length();
            if (distance > bestDistance)
                continue;
            bestDistance = distance;
            closestEdge = direction;
        }

        AABB bb = EdgeInteractionHandler.getBB(pos, closestEdge);
        boolean hit = bb.contains(target.getLocation());
        Vec3 offset = Vec3.atLowerCornerOf(closestEdge.getUnitVec3i()).scale(.5).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(.469))
            .add(VecHelper.CENTER_OF_ORIGIN);

        ValueBox box = new ValueBox(CommonComponents.EMPTY, bb, pos).passive(!hit).transform(new EdgeValueBoxTransform(offset)).wideOutline();
        Outliner.getInstance().showOutline("edge", box).highlightFace(face);

        if (!hit)
            return;

        List<MutableComponent> tip = new ArrayList<>();
        tip.add(CreateLang.translateDirect("logistics.crafter.connected"));
        tip.add(CreateLang.translateDirect(CrafterHelper.areCraftersConnected(
            world,
            pos,
            pos.relative(closestEdge)
        ) ? "logistics.crafter.click_to_separate" : "logistics.crafter.click_to_merge"));
        Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
    }

    static class EdgeValueBoxTransform extends ValueBoxTransform.Sided {

        private final Vec3 add;

        public EdgeValueBoxTransform(Vec3 add) {
            this.add = add;
        }

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return add;
        }

        @Override
        public void rotate(BlockState state, PoseStack ms) {
            super.rotate(state, ms);
        }

    }

}
