package com.zurrtum.create.client.content.equipment.clipboard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.zurrtum.create.content.equipment.clipboard.ClipboardCloneable;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.LOGGER;

public class ClipboardValueSettingsClientHandler {
    public static boolean drawCustomBlockSelection(Minecraft mc, BlockPos pos, MultiBufferSource vertexConsumerProvider, Vec3 camPos, PoseStack ms) {
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator())
            return false;
        if (!player.getMainHandItem().is(AllItems.CLIPBOARD))
            return false;
        ClientLevel world = mc.level;
        if (!world.getWorldBorder().isWithinBounds(pos))
            return false;
        BlockState blockstate = world.getBlockState(pos);

        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
            return false;
        if (!(smartBE instanceof ClipboardBlockEntity) && !(smartBE instanceof ClipboardCloneable)) {
            if (mc.hitResult instanceof BlockHitResult target) {
                RegistryAccess registryManager = world.registryAccess();
                Direction side = target.getDirection();
                if (Stream.of(ServerScrollValueBehaviour.TYPE, ServerFilteringBehaviour.TYPE, ServerLinkBehaviour.TYPE)
                    .noneMatch(type -> smartBE.getBehaviour(type) instanceof ClipboardCloneable cc && cc.canWrite(registryManager, side))) {
                    return false;
                }
            }
        }

        VoxelShape shape = blockstate.getShape(world, pos);
        if (shape.isEmpty())
            return false;

        VertexConsumer vb = vertexConsumerProvider.getBuffer(RenderType.lines());

        ms.pushPose();
        ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
        TrackBlockOutline.renderShape(shape, ms, vb, true);
        ms.popPose();
        return true;
    }

    public static void clientTick(Minecraft mc) {
        if (!(mc.hitResult instanceof BlockHitResult target))
            return;
        LocalPlayer player = mc.player;
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(AllItems.CLIPBOARD))
            return;
        BlockPos pos = target.getBlockPos();
        ClientLevel level = mc.level;
        if (!(level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
            return;

        if (smartBE instanceof ClipboardBlockEntity) {
            List<MutableComponent> tip = new ArrayList<>();
            tip.add(CreateLang.translateDirect("clipboard.actions"));
            tip.add(CreateLang.translateDirect("clipboard.copy_other_clipboard", Component.keybind("key.use")));
            Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
            return;
        }


        Direction side = target.getDirection();
        boolean canCopy = Stream.of(ServerScrollValueBehaviour.TYPE, ServerFilteringBehaviour.TYPE, ServerLinkBehaviour.TYPE)
            .anyMatch(type -> smartBE.getBehaviour(type) instanceof ClipboardCloneable cc && cc.canWrite(
                level.registryAccess(),
                side
            )) || smartBE instanceof ClipboardCloneable ccbe && ccbe.canWrite(level.registryAccess(), side);

        boolean canPaste;
        ClipboardContent content = stack.get(AllDataComponents.CLIPBOARD_CONTENT);
        if (content == null) {
            return;
        }
        CompoundTag tagElement = content.copiedValues().orElse(null);
        if (tagElement == null) {
            canPaste = false;
        } else {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(smartBE.problemPath(), LOGGER)) {
                ValueInput view = TagValueInput.create(logging, level.registryAccess(), tagElement);
                canPaste = (Stream.of(ServerScrollValueBehaviour.TYPE, ServerFilteringBehaviour.TYPE, ServerLinkBehaviour.TYPE)
                    .anyMatch(type -> smartBE.getBehaviour(type) instanceof ClipboardCloneable cc && view.child(cc.getClipboardKey())
                        .map(v -> cc.readFromClipboard(v, player, side, true))
                        .orElse(false)) || smartBE instanceof ClipboardCloneable ccbe && view.child(ccbe.getClipboardKey())
                    .map(v -> ccbe.readFromClipboard(v, player, side, true)).orElse(false));
            }
        }

        if (!canCopy && !canPaste)
            return;

        List<MutableComponent> tip = new ArrayList<>();
        tip.add(CreateLang.translateDirect("clipboard.actions"));
        if (canCopy)
            tip.add(CreateLang.translateDirect("clipboard.to_copy", Component.keybind("key.use")));
        if (canPaste)
            tip.add(CreateLang.translateDirect("clipboard.to_paste", Component.keybind("key.attack")));

        Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
    }
}
