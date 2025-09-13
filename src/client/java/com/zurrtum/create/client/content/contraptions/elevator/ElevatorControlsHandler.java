package com.zurrtum.create.client.content.contraptions.elevator;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.content.contraptions.actors.contraptionControls.ControlsSlot;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement.ElevatorFloorSelection;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.tuple.MutablePair;

import java.lang.ref.WeakReference;
import java.util.Collection;

public class ElevatorControlsHandler {

    private static final ControlsSlot slot = new ElevatorControlsSlot();

    private static class ElevatorControlsSlot extends ControlsSlot {

        @Override
        public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
            Vec3d offset = getLocalOffset(level, pos, state);
            if (offset == null)
                return false;
            return localHit.distanceTo(offset) < scale * .85;
        }

    }

    public static boolean onScroll(MinecraftClient mc, double delta) {
        ClientWorld world = mc.world;
        if (world == null)
            return false;
        ClientPlayerEntity player = mc.player;

        Couple<Vec3d> rayInputs = ContraptionHandlerClient.getRayInputs(mc, player);
        Vec3d origin = rayInputs.getFirst();
        Vec3d target = rayInputs.getSecond();
        Box aabb = new Box(origin, target).expand(16);

        Collection<WeakReference<AbstractContraptionEntity>> contraptions = ContraptionHandlerClient.loadedContraptions.get(world).values();

        for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
            AbstractContraptionEntity contraptionEntity = ref.get();
            if (contraptionEntity == null)
                continue;

            Contraption contraption = contraptionEntity.getContraption();
            if (!(contraption instanceof ElevatorContraption ec))
                continue;

            if (!contraptionEntity.getBoundingBox().intersects(aabb))
                continue;

            BlockHitResult rayTraceResult = ContraptionHandlerClient.rayTraceContraption(origin, target, contraptionEntity);
            if (rayTraceResult == null)
                continue;

            BlockPos pos = rayTraceResult.getBlockPos();
            StructureBlockInfo info = contraption.getBlocks().get(pos);

            if (info == null)
                continue;
            if (!info.state().isOf(AllBlocks.CONTRAPTION_CONTROLS))
                continue;

            if (!slot.testHit(world, pos, info.state(), rayTraceResult.getPos().subtract(Vec3d.of(pos))))
                continue;

            MovementContext ctx = null;
            for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
                if (info.equals(pair.left)) {
                    ctx = pair.right;
                    break;
                }
            }
            if (ctx == null) {
                continue;
            }

            if (!(ctx.temporaryData instanceof ElevatorFloorSelection))
                ctx.temporaryData = new ElevatorFloorSelection();

            ElevatorFloorSelection efs = (ElevatorFloorSelection) ctx.temporaryData;
            int prev = efs.currentIndex;
            // Round away from 0. delta may be ~0.9, which is implicitly floor'd during a pure cast.
            efs.currentIndex += (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));
            ContraptionControlsMovement.tickFloorSelection(efs, ec);

            if (prev != efs.currentIndex && !ec.namesList.isEmpty()) {
                float pitch = (efs.currentIndex) / (float) (ec.namesList.size());
                pitch = MathHelper.lerp(pitch, 1f, 1.5f);
                AllSoundEvents.SCROLL_VALUE.play(
                    world,
                    player,
                    BlockPos.ofFloored(contraptionEntity.toGlobalVector(rayTraceResult.getPos(), 1)),
                    1,
                    pitch
                );
            }

            return true;
        }

        return false;
    }

}
