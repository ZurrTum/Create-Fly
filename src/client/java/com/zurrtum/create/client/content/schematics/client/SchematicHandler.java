package com.zurrtum.create.client.content.schematics.client;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.List;

public class SchematicHandler {

    private String displayedSchematic;
    private SchematicTransformation transformation;
    private Box bounds;
    private boolean deployed;
    private boolean active;
    private ToolType currentTool;

    private static final int SYNC_DELAY = 10;
    private int syncCooldown;
    private int activeHotbarSlot;
    private ItemStack activeSchematicItem;
    private AABBOutline outline;

    private final SchematicRenderer[] renderers = new SchematicRenderer[3];
    private final SchematicHotbarSlotOverlay overlay;
    private ToolSelectionScreen selectionScreen;

    public SchematicHandler() {
        overlay = new SchematicHotbarSlotOverlay();
        currentTool = ToolType.DEPLOY;
        selectionScreen = new ToolSelectionScreen(MinecraftClient.getInstance(), ImmutableList.of(ToolType.DEPLOY), this::equip);
        transformation = new SchematicTransformation();
    }

    public void tick(MinecraftClient mc) {
        if (mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
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

        ClientPlayerEntity player = mc.player;
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

    private void init(MinecraftClient mc, ItemStack stack) {
        ClientPlayerEntity player = mc.player;
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

    private void setupRenderer(MinecraftClient mc) {
        World clientWorld = mc.world;
        StructureTemplate schematic = SchematicItem.loadSchematic(clientWorld, activeSchematicItem);
        Vec3i size = schematic.getSize();
        if (size.equals(Vec3i.ZERO))
            return;

        SchematicLevel w = new SchematicLevel(clientWorld);
        SchematicLevel wMirroredFB = new SchematicLevel(clientWorld);
        SchematicLevel wMirroredLR = new SchematicLevel(clientWorld);
        StructurePlacementData placementSettings = new StructurePlacementData();
        StructureTransform transform;
        BlockPos pos;

        pos = BlockPos.ORIGIN;

        try {
            schematic.place(w, pos, pos, placementSettings, w.getRandom(), Block.NOTIFY_LISTENERS);
            for (BlockEntity blockEntity : w.getBlockEntities())
                blockEntity.setWorld(w);
            fixControllerBlockEntities(w);
        } catch (Exception e) {
            mc.player.sendMessage(CreateLang.translate("schematic.error").component(), false);
            Create.LOGGER.error("Failed to load Schematic for Previewing", e);
            return;
        }

        placementSettings.setMirror(BlockMirror.FRONT_BACK);
        pos = BlockPos.ORIGIN.east(size.getX() - 1);
        schematic.place(wMirroredFB, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.NOTIFY_LISTENERS);
        transform = new StructureTransform(placementSettings.getPosition(), Axis.Y, BlockRotation.NONE, placementSettings.getMirror());
        for (BlockEntity be : wMirroredFB.getRenderedBlockEntities())
            transform.apply(be);
        fixControllerBlockEntities(wMirroredFB);

        placementSettings.setMirror(BlockMirror.LEFT_RIGHT);
        pos = BlockPos.ORIGIN.south(size.getZ() - 1);
        schematic.place(wMirroredLR, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.NOTIFY_LISTENERS);
        transform = new StructureTransform(placementSettings.getPosition(), Axis.Y, BlockRotation.NONE, placementSettings.getMirror());
        for (BlockEntity be : wMirroredLR.getRenderedBlockEntities())
            transform.apply(be);
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
            BlockPos current = blockEntity.getPos();
            if (lastKnown == null || current == null)
                continue;
            if (multiBlockEntity.isController())
                continue;
            if (!lastKnown.equals(current)) {
                BlockPos newControllerPos = multiBlockEntity.getController().add(current.subtract(lastKnown));
                if (multiBlockEntity instanceof SmartBlockEntity sbe)
                    sbe.markVirtual();
                multiBlockEntity.setController(newControllerPos);
            }
        }
    }

    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera) {
        if (!active) {
            return;
        }
        boolean present = activeSchematicItem != null;
        if (!present) {
            return;
        }

        ms.push();
        currentTool.getTool().renderTool(mc, ms, buffer, camera);
        ms.pop();

        ms.push();
        transformation.applyTransformations(ms, camera);

        if (deployed) {
            float pt = AnimationTickHolder.getPartialTicks();
            boolean lr = transformation.getScaleLR().getValue(pt) < 0;
            boolean fb = transformation.getScaleFB().getValue(pt) < 0;
            if (lr && !fb && renderers[2] != null) {
                renderers[2].render(mc, ms, buffer);
            } else if (fb && !lr && renderers[1] != null) {
                renderers[1].render(mc, ms, buffer);
            } else if (renderers[0] != null) {
                renderers[0].render(mc, ms, buffer);
            }
        }

        currentTool.getTool().renderOnSchematic(mc, ms, buffer);

        ms.pop();
    }

    public void updateRenderers() {
        for (SchematicRenderer renderer : renderers) {
            if (renderer != null) {
                renderer.update();
            }
        }
    }

    public void render(MinecraftClient mc, DrawContext guiGraphics, RenderTickCounter deltaTracker) {
        if (!active)
            return;
        float tickProgress = deltaTracker.getTickProgress(false);
        if (activeSchematicItem != null)
            overlay.renderOn(mc, guiGraphics, activeHotbarSlot, tickProgress);
        currentTool.getTool()
            .renderOverlay(mc.inGameHud, guiGraphics, tickProgress, guiGraphics.getScaledWindowWidth(), guiGraphics.getScaledWindowHeight());
        selectionScreen.renderPassive(guiGraphics, tickProgress);
    }

    public boolean onMouseInput(MinecraftClient mc, int button) {
        if (!active)
            return false;
        if (button != 1)
            return false;
        if (mc.player.isSneaking())
            return false;
        if (mc.crosshairTarget instanceof BlockHitResult blockRayTraceResult) {
            BlockState clickedBlock = mc.world.getBlockState(blockRayTraceResult.getBlockPos());
            if (clickedBlock.isOf(AllBlocks.SCHEMATICANNON))
                return false;
            if (clickedBlock.isOf(AllBlocks.DEPLOYER))
                return false;
        }
        return currentTool.getTool().handleRightClick(mc);
    }

    public boolean onKeyInput(InputUtil.Key key, boolean pressed) {
        if (!active || key != AllKeys.TOOL_MENU.boundKey)
            return false;

        if (pressed && !selectionScreen.focused)
            selectionScreen.focused = true;
        if (!pressed && selectionScreen.focused) {
            selectionScreen.focused = false;
            selectionScreen.close();
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
        if (Screen.hasControlDown())
            return currentTool.getTool().handleMouseWheel(delta);
        return false;
    }

    private ItemStack findBlueprintInHand(PlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (!stack.isOf(AllItems.SCHEMATIC))
            return null;
        if (!stack.contains(AllDataComponents.SCHEMATIC_FILE))
            return null;

        activeSchematicItem = stack;
        activeHotbarSlot = player.getInventory().getSelectedSlot();
        return stack;
    }

    private boolean itemLost(PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0, size = PlayerInventory.getHotbarSize(); i < size; i++) {
            if (inventory.getStack(i).isOf(activeSchematicItem.getItem()))
                continue;
            if (!ItemStack.areEqual(inventory.getStack(i), activeSchematicItem))
                continue;
            return false;
        }
        return true;
    }

    public void markDirty() {
        syncCooldown = SYNC_DELAY;
    }

    public void sync(ClientPlayerEntity player) {
        if (activeSchematicItem == null)
            return;
        player.networkHandler.sendPacket(new SchematicSyncPacket(
            activeHotbarSlot,
            transformation.toSettings(),
            transformation.getAnchor(),
            deployed
        ));
    }

    public void equip(ToolType tool) {
        this.currentTool = tool;
        currentTool.getTool().init();
    }

    public void loadSettings(ItemStack blueprint) {
        BlockPos anchor = BlockPos.ORIGIN;
        StructurePlacementData settings = SchematicItem.getSettings(blueprint);
        transformation = new SchematicTransformation();

        deployed = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false);
        anchor = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ORIGIN);
        Vec3i size = blueprint.get(AllDataComponents.SCHEMATIC_BOUNDS);
        if (size == null) {
            return;
        }

        bounds = new Box(0, 0, 0, size.getX(), size.getY(), size.getZ());
        outline = new AABBOutline(bounds);
        outline.getParams().colored(0x6886c5).lineWidth(1 / 16f);
        transformation.init(anchor, settings, bounds);
    }

    public void deploy(MinecraftClient mc) {
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

    public void printInstantly(MinecraftClient mc) {
        mc.player.networkHandler.sendPacket(new SchematicPlacePacket(activeSchematicItem.copy()));
        activeSchematicItem.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
        SchematicInstances.clearHash(activeSchematicItem);
        active = false;
        markDirty();
    }

    public boolean isActive() {
        return active;
    }

    public Box getBounds() {
        return bounds;
    }

    public SchematicTransformation getTransformation() {
        return transformation;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public ItemStack getActiveSchematicItem() {
        return activeSchematicItem;
    }

    public AABBOutline getOutline() {
        return outline;
    }

}
