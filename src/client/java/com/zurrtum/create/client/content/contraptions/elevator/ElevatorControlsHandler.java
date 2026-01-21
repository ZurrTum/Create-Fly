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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

import java.lang.ref.WeakReference;
import java.util.Collection;

public class ElevatorControlsHandler {

    private static final ControlsSlot slot = new ElevatorControlsSlot();

    private static class ElevatorControlsSlot extends ControlsSlot {

        @Override
        public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
            return localHit.distanceTo(getLocalOffset(state)) < scale * .85;
        }

    }

    public static boolean onScroll(Minecraft mc, double delta) {
        ClientLevel world = mc.level;
        if (world == null)
            return false;
        LocalPlayer player = mc.player;

        Couple<Vec3> rayInputs = ContraptionHandlerClient.getRayInputs(mc, player);
        Vec3 origin = rayInputs.getFirst();
        Vec3 target = rayInputs.getSecond();
        AABB aabb = new AABB(origin, target).inflate(16);

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
            if (!info.state().is(AllBlocks.CONTRAPTION_CONTROLS))
                continue;

            if (!slot.testHit(world, pos, info.state(), rayTraceResult.getLocation().subtract(Vec3.atLowerCornerOf(pos))))
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
                pitch = Mth.lerp(pitch, 1f, 1.5f);
                AllSoundEvents.SCROLL_VALUE.play(
                    world,
                    player,
                    BlockPos.containing(contraptionEntity.toGlobalVector(rayTraceResult.getLocation(), 1)),
                    1,
                    pitch
                );
            }

            return true;
        }

        return false;
    }

}
