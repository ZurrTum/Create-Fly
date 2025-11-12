package com.zurrtum.create.client.content.schematics.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.client.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.zurrtum.create.content.schematics.SchematicExport;
import com.zurrtum.create.content.schematics.SchematicExport.SchematicExportResult;
import com.zurrtum.create.foundation.utility.CreatePaths;
import com.zurrtum.create.infrastructure.packet.c2s.InstantSchematicPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SchematicAndQuillHandler {

    private final Object outlineSlot = new Object();

    public BlockPos firstPos;
    public BlockPos secondPos;
    private BlockPos selectedPos;
    private Direction selectedFace;
    private int range = 10;

    public boolean mouseScrolled(Minecraft mc, double delta) {
        if (!isActive(mc))
            return false;
        if (!AllKeys.hasControlDown())
            return false;
        if (secondPos == null)
            range = (int) Mth.clamp(range + delta, 1, 100);
        if (selectedFace == null)
            return true;

        AABB bb = new AABB(Vec3.atLowerCornerOf(firstPos), Vec3.atLowerCornerOf(secondPos));
        Vec3i vec = selectedFace.getUnitVec3i();
        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
        if (bb.contains(projectedView))
            delta *= -1;

        // Round away from zero to avoid an implicit floor
        int intDelta = (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));

        int x = vec.getX() * intDelta;
        int y = vec.getY() * intDelta;
        int z = vec.getZ() * intDelta;

        AxisDirection axisDirection = selectedFace.getAxisDirection();
        if (axisDirection == AxisDirection.NEGATIVE)
            bb = bb.move(-x, -y, -z);

        double maxX = Math.max(bb.maxX - x * axisDirection.getStep(), bb.minX);
        double maxY = Math.max(bb.maxY - y * axisDirection.getStep(), bb.minY);
        double maxZ = Math.max(bb.maxZ - z * axisDirection.getStep(), bb.minZ);
        bb = new AABB(bb.minX, bb.minY, bb.minZ, maxX, maxY, maxZ);

        firstPos = BlockPos.containing(bb.minX, bb.minY, bb.minZ);
        secondPos = BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ);
        LocalPlayer player = mc.player;
        CreateLang.translate("schematicAndQuill.dimensions", (int) bb.getXsize() + 1, (int) bb.getYsize() + 1, (int) bb.getZsize() + 1)
            .sendStatus(player);

        return true;
    }

    public boolean onMouseInput(Minecraft mc, int button) {
        if (button != 1)
            return false;
        if (!isActive(mc))
            return false;

        LocalPlayer player = mc.player;

        if (player.isShiftKeyDown()) {
            discard(mc);
            return true;
        }

        if (secondPos != null) {
            ScreenOpener.open(new SchematicPromptScreen());
            return true;
        }

        if (selectedPos == null) {
            CreateLang.translate("schematicAndQuill.noTarget").sendStatus(player);
            return true;
        }

        if (firstPos != null) {
            secondPos = selectedPos;
            CreateLang.translate("schematicAndQuill.secondPos").sendStatus(player);
            return true;
        }

        firstPos = selectedPos;
        CreateLang.translate("schematicAndQuill.firstPos").sendStatus(player);
        return true;
    }

    public void discard(Minecraft mc) {
        firstPos = null;
        secondPos = null;
        CreateLang.translate("schematicAndQuill.abort").sendStatus(mc.player);
    }

    public void tick(Minecraft mc) {
        if (!isActive(mc))
            return;

        LocalPlayer player = mc.player;
        if (InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            float pt = AnimationTickHolder.getPartialTicks();
            Vec3 targetVec = player.getEyePosition(pt).add(player.getLookAngle().scale(range));
            selectedPos = BlockPos.containing(targetVec);

        } else {
            BlockHitResult trace = RaycastHelper.rayTraceRange(player.level(), player, 75);
            if (trace != null && trace.getType() == Type.BLOCK) {

                BlockPos hit = trace.getBlockPos();
                boolean replaceable = player.level().getBlockState(hit)
                    .canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, trace)));
                if (trace.getDirection().getAxis().isVertical() && !replaceable)
                    hit = hit.relative(trace.getDirection());
                selectedPos = hit;
            } else
                selectedPos = null;
        }

        selectedFace = null;
        if (secondPos != null) {
            AABB bb = new AABB(Vec3.atLowerCornerOf(firstPos), Vec3.atLowerCornerOf(secondPos)).expandTowards(1, 1, 1).inflate(.45f);
            Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
            boolean inside = bb.contains(projectedView);
            PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 70, pos -> inside ^ bb.contains(VecHelper.getCenterOf(pos)));
            selectedFace = result.missed() ? null : inside ? result.getFacing().getOpposite() : result.getFacing();
        }

        AABB currentSelectionBox = getCurrentSelectionBox();
        if (currentSelectionBox != null)
            outliner().chaseAABB(outlineSlot, currentSelectionBox).colored(0x6886c5)
                .withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED).lineWidth(1 / 16f)
                .highlightFace(selectedFace);
    }

    private AABB getCurrentSelectionBox() {
        if (secondPos == null) {
            if (firstPos == null)
                return selectedPos == null ? null : new AABB(selectedPos);
            return selectedPos == null ? new AABB(firstPos) : new AABB(
                Vec3.atLowerCornerOf(firstPos),
                Vec3.atLowerCornerOf(selectedPos)
            ).expandTowards(1, 1, 1);
        }
        return new AABB(Vec3.atLowerCornerOf(firstPos), Vec3.atLowerCornerOf(secondPos)).expandTowards(1, 1, 1);
    }

    private boolean isActive(Minecraft mc) {
        return mc != null && mc.level != null && mc.screen == null && mc.player.getMainHandItem().is(AllItems.SCHEMATIC_AND_QUILL);
    }

    public void saveSchematic(Minecraft mc, String string, boolean convertImmediately) {
        SchematicExportResult result = SchematicExport.saveSchematic(CreatePaths.SCHEMATICS_DIR, string, false, mc.level, firstPos, secondPos);
        LocalPlayer player = mc.player;
        if (result == null) {
            CreateLang.translate("schematicAndQuill.failed").style(ChatFormatting.RED).sendStatus(player);
            return;
        }
        Path file = result.file();
        CreateLang.translate("schematicAndQuill.saved", file.getFileName().toString()).sendStatus(player);
        firstPos = null;
        secondPos = null;
        if (!convertImmediately)
            return;
        try {
            if (!ClientSchematicLoader.validateSizeLimitation(mc, Files.size(file)))
                return;
            player.connection.send(new InstantSchematicPacket(result.fileName(), result.origin(), result.bounds()));
        } catch (IOException e) {
            Create.LOGGER.error("Error instantly uploading Schematic file: " + file, e);
        }
    }

    private Outliner outliner() {
        return Outliner.getInstance();
    }

}
