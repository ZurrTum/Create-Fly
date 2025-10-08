package com.zurrtum.create.client.content.schematics.client;

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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.AxisDirection;
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

    public boolean mouseScrolled(MinecraftClient mc, double delta) {
        if (!isActive(mc))
            return false;
        if (!AllKeys.hasControlDown())
            return false;
        if (secondPos == null)
            range = (int) MathHelper.clamp(range + delta, 1, 100);
        if (selectedFace == null)
            return true;

        Box bb = new Box(Vec3d.of(firstPos), Vec3d.of(secondPos));
        Vec3i vec = selectedFace.getVector();
        Vec3d projectedView = mc.gameRenderer.getCamera().getPos();
        if (bb.contains(projectedView))
            delta *= -1;

        // Round away from zero to avoid an implicit floor
        int intDelta = (int) (delta > 0 ? Math.ceil(delta) : Math.floor(delta));

        int x = vec.getX() * intDelta;
        int y = vec.getY() * intDelta;
        int z = vec.getZ() * intDelta;

        AxisDirection axisDirection = selectedFace.getDirection();
        if (axisDirection == AxisDirection.NEGATIVE)
            bb = bb.offset(-x, -y, -z);

        double maxX = Math.max(bb.maxX - x * axisDirection.offset(), bb.minX);
        double maxY = Math.max(bb.maxY - y * axisDirection.offset(), bb.minY);
        double maxZ = Math.max(bb.maxZ - z * axisDirection.offset(), bb.minZ);
        bb = new Box(bb.minX, bb.minY, bb.minZ, maxX, maxY, maxZ);

        firstPos = BlockPos.ofFloored(bb.minX, bb.minY, bb.minZ);
        secondPos = BlockPos.ofFloored(bb.maxX, bb.maxY, bb.maxZ);
        ClientPlayerEntity player = mc.player;
        CreateLang.translate("schematicAndQuill.dimensions", (int) bb.getLengthX() + 1, (int) bb.getLengthY() + 1, (int) bb.getLengthZ() + 1)
            .sendStatus(player);

        return true;
    }

    public boolean onMouseInput(MinecraftClient mc, int button) {
        if (button != 1)
            return false;
        if (!isActive(mc))
            return false;

        ClientPlayerEntity player = mc.player;

        if (player.isSneaking()) {
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

    public void discard(MinecraftClient mc) {
        firstPos = null;
        secondPos = null;
        CreateLang.translate("schematicAndQuill.abort").sendStatus(mc.player);
    }

    public void tick(MinecraftClient mc) {
        if (!isActive(mc))
            return;

        ClientPlayerEntity player = mc.player;
        if (InputUtil.isKeyPressed(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            float pt = AnimationTickHolder.getPartialTicks();
            Vec3d targetVec = player.getCameraPosVec(pt).add(player.getRotationVector().multiply(range));
            selectedPos = BlockPos.ofFloored(targetVec);

        } else {
            BlockHitResult trace = RaycastHelper.rayTraceRange(player.getEntityWorld(), player, 75);
            if (trace != null && trace.getType() == Type.BLOCK) {

                BlockPos hit = trace.getBlockPos();
                boolean replaceable = player.getEntityWorld().getBlockState(hit)
                    .canReplace(new ItemPlacementContext(new ItemUsageContext(player, Hand.MAIN_HAND, trace)));
                if (trace.getSide().getAxis().isVertical() && !replaceable)
                    hit = hit.offset(trace.getSide());
                selectedPos = hit;
            } else
                selectedPos = null;
        }

        selectedFace = null;
        if (secondPos != null) {
            Box bb = new Box(Vec3d.of(firstPos), Vec3d.of(secondPos)).stretch(1, 1, 1).expand(.45f);
            Vec3d projectedView = mc.gameRenderer.getCamera().getPos();
            boolean inside = bb.contains(projectedView);
            PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 70, pos -> inside ^ bb.contains(VecHelper.getCenterOf(pos)));
            selectedFace = result.missed() ? null : inside ? result.getFacing().getOpposite() : result.getFacing();
        }

        Box currentSelectionBox = getCurrentSelectionBox();
        if (currentSelectionBox != null)
            outliner().chaseAABB(outlineSlot, currentSelectionBox).colored(0x6886c5)
                .withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED).lineWidth(1 / 16f)
                .highlightFace(selectedFace);
    }

    private Box getCurrentSelectionBox() {
        if (secondPos == null) {
            if (firstPos == null)
                return selectedPos == null ? null : new Box(selectedPos);
            return selectedPos == null ? new Box(firstPos) : new Box(Vec3d.of(firstPos), Vec3d.of(selectedPos)).stretch(1, 1, 1);
        }
        return new Box(Vec3d.of(firstPos), Vec3d.of(secondPos)).stretch(1, 1, 1);
    }

    private boolean isActive(MinecraftClient mc) {
        return mc != null && mc.world != null && mc.currentScreen == null && mc.player.getMainHandStack().isOf(AllItems.SCHEMATIC_AND_QUILL);
    }

    public void saveSchematic(MinecraftClient mc, String string, boolean convertImmediately) {
        SchematicExportResult result = SchematicExport.saveSchematic(CreatePaths.SCHEMATICS_DIR, string, false, mc.world, firstPos, secondPos);
        ClientPlayerEntity player = mc.player;
        if (result == null) {
            CreateLang.translate("schematicAndQuill.failed").style(Formatting.RED).sendStatus(player);
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
            player.networkHandler.sendPacket(new InstantSchematicPacket(result.fileName(), result.origin(), result.bounds()));
        } catch (IOException e) {
            Create.LOGGER.error("Error instantly uploading Schematic file: " + file, e);
        }
    }

    private Outliner outliner() {
        return Outliner.getInstance();
    }

}
