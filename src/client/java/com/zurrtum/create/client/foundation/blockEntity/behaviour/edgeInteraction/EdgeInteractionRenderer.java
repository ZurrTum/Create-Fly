package com.zurrtum.create.client.foundation.blockEntity.behaviour.edgeInteraction;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.crafter.CrafterHelper;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.List;

public class EdgeInteractionRenderer {

    public static void tick(MinecraftClient mc) {
        HitResult target = mc.crosshairTarget;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientWorld world = mc.world;
        BlockPos pos = result.getBlockPos();
        PlayerEntity player = mc.player;
        ItemStack heldItem = player.getMainHandStack();

        if (player.isSneaking())
            return;
        EdgeInteractionBehaviour behaviour = BlockEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
        if (behaviour == null)
            return;
        if (!behaviour.requiredItem.test(heldItem.getItem()))
            return;

        Direction face = result.getSide();
        List<Direction> connectiveSides = EdgeInteractionHandler.getConnectiveSides(world, pos, face, behaviour);
        if (connectiveSides.isEmpty())
            return;

        Direction closestEdge = connectiveSides.getFirst();
        double bestDistance = Double.MAX_VALUE;
        Vec3d center = VecHelper.getCenterOf(pos);
        for (Direction direction : connectiveSides) {
            double distance = Vec3d.of(direction.getVector()).subtract(target.getPos().subtract(center)).length();
            if (distance > bestDistance)
                continue;
            bestDistance = distance;
            closestEdge = direction;
        }

        Box bb = EdgeInteractionHandler.getBB(pos, closestEdge);
        boolean hit = bb.contains(target.getPos());
        Vec3d offset = Vec3d.of(closestEdge.getVector()).multiply(.5).add(Vec3d.of(face.getVector()).multiply(.469)).add(VecHelper.CENTER_OF_ORIGIN);

        ValueBox box = new ValueBox(ScreenTexts.EMPTY, bb, pos).passive(!hit).transform(new EdgeValueBoxTransform(offset)).wideOutline();
        Outliner.getInstance().showOutline("edge", box).highlightFace(face);

        if (!hit)
            return;

        List<MutableText> tip = new ArrayList<>();
        tip.add(CreateLang.translateDirect("logistics.crafter.connected"));
        tip.add(CreateLang.translateDirect(CrafterHelper.areCraftersConnected(
            world,
            pos,
            pos.offset(closestEdge)
        ) ? "logistics.crafter.click_to_separate" : "logistics.crafter.click_to_merge"));
        Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
    }

    static class EdgeValueBoxTransform extends ValueBoxTransform.Sided {

        private final Vec3d add;

        public EdgeValueBoxTransform(Vec3d add) {
            this.add = add;
        }

        @Override
        protected Vec3d getSouthLocation() {
            return Vec3d.ZERO;
        }

        @Override
        public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
            return add;
        }

        @Override
        public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
            super.rotate(level, pos, state, ms);
        }

    }

}
