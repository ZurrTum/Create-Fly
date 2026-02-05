package com.zurrtum.create.client.content.schematics.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.content.schematics.client.tools.ToolType;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.schematics.SchematicInstances;
import com.zurrtum.create.content.schematics.SchematicItem;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.packet.c2s.SchematicPlacePacket;
import com.zurrtum.create.infrastructure.packet.c2s.SchematicSyncPacket;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SchematicHandler {

    private @Nullable String displayedSchematic;
    private SchematicTransformation transformation;
    private AABB bounds;
    private boolean deployed;
    private boolean active;
    private ToolType currentTool;

    private static final int SYNC_DELAY = 10;
    private int syncCooldown;
    private int activeHotbarSlot;
    private @Nullable ItemStack activeSchematicItem;
    private AABBOutline outline;

    private final SchematicRenderer[] renderers = new SchematicRenderer[3];
    private final SchematicHotbarSlotOverlay overlay;
    private ToolSelectionScreen selectionScreen;

    public SchematicHandler() {
        overlay = new SchematicHotbarSlotOverlay();
        currentTool = ToolType.DEPLOY;
        selectionScreen = new ToolSelectionScreen(Minecraft.getInstance(), ImmutableList.of(ToolType.DEPLOY), this::equip);
        transformation = new SchematicTransformation();
    }

    public void tick(Minecraft mc) {
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            if (active) {
                active = false;
                syncCooldown = 0;
                activeHotbarSlot = 0;
                activeSchematicItem = null;
            }
            return;
        }

        if (activeSchematicItem != null && transformation != null)
            transformation.tick();

        LocalPlayer player = mc.player;
        ItemStack stack = findBlueprintInHand(player);
        if (stack == null) {
            active = false;
            syncCooldown = 0;
            if (activeSchematicItem != null && itemLost(player)) {
                activeHotbarSlot = 0;
                activeSchematicItem = null;
            }
            return;
        }

        if (!active || !stack.get(AllDataComponents.SCHEMATIC_FILE).equals(displayedSchematic)) {
            init(mc, stack);
        }
        if (!active)
            return;

        if (syncCooldown > 0)
            syncCooldown--;
        if (syncCooldown == 1)
            sync(player);

        selectionScreen.update();
        currentTool.getTool().updateSelection(mc);
    }

    private void init(Minecraft mc, ItemStack stack) {
        LocalPlayer player = mc.player;
        loadSettings(stack);
        displayedSchematic = stack.get(AllDataComponents.SCHEMATIC_FILE);
        active = true;
        if (deployed) {
            setupRenderer(mc);
            ToolType toolBefore = currentTool;
            selectionScreen = new ToolSelectionScreen(mc, ToolType.getTools(player.isCreative()), this::equip);
            if (toolBefore != null) {
                selectionScreen.setSelectedElement(toolBefore);
                equip(toolBefore);
            }
        } else
            selectionScreen = new ToolSelectionScreen(mc, ImmutableList.of(ToolType.DEPLOY), this::equip);
    }

    private void setupRenderer(Minecraft mc) {
        Level clientWorld = mc.level;
        StructureTemplate schematic = SchematicItem.loadSchematic(clientWorld, activeSchematicItem);
        Vec3i size = schematic.getSize();
        if (size.equals(Vec3i.ZERO))
            return;

        SchematicLevel w = new SchematicLevel(clientWorld);
        SchematicLevel wMirroredFB = new SchematicLevel(clientWorld);
        SchematicLevel wMirroredLR = new SchematicLevel(clientWorld);
        StructurePlaceSettings placementSettings = new StructurePlaceSettings();
        StructureTransform transform;
        BlockPos pos;

        pos = BlockPos.ZERO;

        try {
            schematic.placeInWorld(w, pos, pos, placementSettings, w.getRandom(), Block.UPDATE_CLIENTS);
            for (BlockEntity blockEntity : w.getBlockEntities()) {
                blockEntity.setLevel(w);
                if (blockEntity instanceof SmartBlockEntity smartBlockEntity) {
                    smartBlockEntity.tick();
                }
            }
            fixControllerBlockEntities(w);
        } catch (Exception e) {
            mc.player.displayClientMessage(CreateLang.translate("schematic.error").component(), false);
            Create.LOGGER.error("Failed to load Schematic for Previewing", e);
            return;
        }

        placementSettings.setMirror(Mirror.FRONT_BACK);
        pos = BlockPos.ZERO.east(size.getX() - 1);
        schematic.placeInWorld(wMirroredFB, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.UPDATE_CLIENTS);
        transform = new StructureTransform(placementSettings.getRotationPivot(), Axis.Y, Rotation.NONE, placementSettings.getMirror());
        for (BlockEntity be : wMirroredFB.getRenderedBlockEntities()) {
            transform.apply(be);
            if (be instanceof SmartBlockEntity smartBlockEntity) {
                smartBlockEntity.tick();
            }
        }
        fixControllerBlockEntities(wMirroredFB);

        placementSettings.setMirror(Mirror.LEFT_RIGHT);
        pos = BlockPos.ZERO.south(size.getZ() - 1);
        schematic.placeInWorld(wMirroredLR, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.UPDATE_CLIENTS);
        transform = new StructureTransform(placementSettings.getRotationPivot(), Axis.Y, Rotation.NONE, placementSettings.getMirror());
        for (BlockEntity be : wMirroredLR.getRenderedBlockEntities()) {
            transform.apply(be);
            if (be instanceof SmartBlockEntity smartBlockEntity) {
                smartBlockEntity.tick();
            }
        }
        fixControllerBlockEntities(wMirroredLR);

        renderers[0] = new SchematicRenderer(w);
        renderers[1] = new SchematicRenderer(wMirroredFB);
        renderers[2] = new SchematicRenderer(wMirroredLR);
    }

    private void fixControllerBlockEntities(SchematicLevel level) {
        for (BlockEntity blockEntity : level.getBlockEntities()) {
            if (!(blockEntity instanceof IMultiBlockEntityContainer multiBlockEntity))
                continue;
            BlockPos lastKnown = multiBlockEntity.getLastKnownPos();
            BlockPos current = blockEntity.getBlockPos();
            if (lastKnown == null || current == null)
                continue;
            if (multiBlockEntity.isController())
                continue;
            if (!lastKnown.equals(current)) {
                BlockPos newControllerPos = multiBlockEntity.getController().offset(current.subtract(lastKnown));
                if (multiBlockEntity instanceof SmartBlockEntity sbe)
                    sbe.markVirtual();
                multiBlockEntity.setController(newControllerPos);
            }
        }
    }

    public void render(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
        if (!active) {
            return;
        }
        boolean present = activeSchematicItem != null;
        if (!present) {
            return;
        }

        ms.pushPose();
        currentTool.getTool().renderTool(mc, ms, buffer, camera);
        ms.popPose();

        ms.pushPose();
        transformation.applyTransformations(ms, camera);

        if (deployed) {
            float pt = AnimationTickHolder.getPartialTicks();
            boolean lr = transformation.getScaleLR().getValue(pt) < 0;
            boolean fb = transformation.getScaleFB().getValue(pt) < 0;
            if (lr && !fb && renderers[2] != null) {
                renderers[2].render(mc, ms, buffer, transformation, camera);
            } else if (fb && !lr && renderers[1] != null) {
                renderers[1].render(mc, ms, buffer, transformation, camera);
            } else if (renderers[0] != null) {
                renderers[0].render(mc, ms, buffer, transformation, camera);
            }
        }

        currentTool.getTool().renderOnSchematic(mc, ms, buffer);

        ms.popPose();
    }

    public void updateRenderers() {
        for (SchematicRenderer renderer : renderers) {
            if (renderer != null) {
                renderer.update();
            }
        }
    }

    public void render(Minecraft mc, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!active)
            return;
        float tickProgress = deltaTracker.getGameTimeDeltaPartialTick(false);
        if (activeSchematicItem != null)
            overlay.renderOn(mc, guiGraphics, activeHotbarSlot, tickProgress);
        currentTool.getTool().renderOverlay(mc.gui, guiGraphics, tickProgress, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        selectionScreen.renderPassive(guiGraphics, tickProgress);
    }

    public boolean onMouseInput(Minecraft mc, int button) {
        if (!active)
            return false;
        if (button != 1)
            return false;
        if (mc.player.isShiftKeyDown())
            return false;
        if (mc.hitResult instanceof BlockHitResult blockRayTraceResult) {
            BlockState clickedBlock = mc.level.getBlockState(blockRayTraceResult.getBlockPos());
            if (clickedBlock.is(AllBlocks.SCHEMATICANNON))
                return false;
            if (clickedBlock.is(AllBlocks.DEPLOYER))
                return false;
        }
        return currentTool.getTool().handleRightClick(mc);
    }

    public boolean onKeyInput(KeyEvent input, boolean pressed) {
        if (!active || !AllKeys.TOOL_MENU.matches(input))
            return false;

        if (pressed && !selectionScreen.focused)
            selectionScreen.focused = true;
        if (!pressed && selectionScreen.focused) {
            selectionScreen.focused = false;
            selectionScreen.onClose();
        }
        return true;
    }

    public boolean mouseScrolled(double delta) {
        if (!active)
            return false;

        if (selectionScreen.focused) {
            selectionScreen.cycle((int) Math.signum(delta));
            return true;
        }
        if (AllKeys.hasControlDown())
            return currentTool.getTool().handleMouseWheel(delta);
        return false;
    }

    @Nullable
    private ItemStack findBlueprintInHand(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(AllItems.SCHEMATIC))
            return null;
        if (!stack.has(AllDataComponents.SCHEMATIC_FILE))
            return null;

        activeSchematicItem = stack;
        activeHotbarSlot = player.getInventory().getSelectedSlot();
        return stack;
    }

    private boolean itemLost(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0, size = Inventory.getSelectionSize(); i < size; i++) {
            if (inventory.getItem(i).is(activeSchematicItem.getItem()))
                continue;
            if (!ItemStack.matches(inventory.getItem(i), activeSchematicItem))
                continue;
            return false;
        }
        return true;
    }

    public void markDirty() {
        syncCooldown = SYNC_DELAY;
    }

    public void sync(LocalPlayer player) {
        if (activeSchematicItem == null)
            return;
        player.connection.send(new SchematicSyncPacket(activeHotbarSlot, transformation.toSettings(), transformation.getAnchor(), deployed));
    }

    public void equip(ToolType tool) {
        this.currentTool = tool;
        currentTool.getTool().init();
    }

    public void loadSettings(ItemStack blueprint) {
        BlockPos anchor = BlockPos.ZERO;
        StructurePlaceSettings settings = SchematicItem.getSettings(blueprint);
        transformation = new SchematicTransformation();

        deployed = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false);
        anchor = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
        Vec3i size = blueprint.get(AllDataComponents.SCHEMATIC_BOUNDS);
        if (size == null) {
            return;
        }

        bounds = new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ());
        outline = new AABBOutline(bounds);
        outline.getParams().colored(0x6886c5).lineWidth(1 / 16f);
        transformation.init(anchor, settings, bounds);
    }

    public void deploy(Minecraft mc) {
        if (!deployed) {
            List<ToolType> tools = ToolType.getTools(mc.player.isCreative());
            selectionScreen = new ToolSelectionScreen(mc, tools, this::equip);
        }
        deployed = true;
        setupRenderer(mc);
    }

    public String getCurrentSchematicName() {
        return displayedSchematic != null ? displayedSchematic : "-";
    }

    public void printInstantly(Minecraft mc) {
        mc.player.connection.send(new SchematicPlacePacket(activeSchematicItem.copy()));
        activeSchematicItem.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
        SchematicInstances.clearHash(activeSchematicItem);
        active = false;
        markDirty();
    }

    public boolean isActive() {
        return active;
    }

    public AABB getBounds() {
        return bounds;
    }

    public SchematicTransformation getTransformation() {
        return transformation;
    }

    public boolean isDeployed() {
        return deployed;
    }

    @Nullable
    public ItemStack getActiveSchematicItem() {
        return activeSchematicItem;
    }

    public AABBOutline getOutline() {
        return outline;
    }

}
