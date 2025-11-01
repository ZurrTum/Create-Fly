package com.zurrtum.create.client.content.equipment.clipboard;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.LOGGER;

public class ClipboardValueSettingsClientHandler {
    public static boolean drawCustomBlockSelection(
        MinecraftClient mc,
        BlockPos pos,
        VertexConsumerProvider vertexConsumerProvider,
        Vec3d camPos,
        MatrixStack ms
    ) {
        ClientPlayerEntity player = mc.player;
        if (player == null || player.isSpectator())
            return false;
        if (!player.getMainHandStack().isOf(AllItems.CLIPBOARD))
            return false;
        ClientWorld world = mc.world;
        if (!world.getWorldBorder().contains(pos))
            return false;
        BlockState blockstate = world.getBlockState(pos);

        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
            return false;
        if (!(smartBE instanceof ClipboardBlockEntity) && !(smartBE instanceof ClipboardCloneable)) {
            if (mc.crosshairTarget instanceof BlockHitResult target) {
                DynamicRegistryManager registryManager = world.getRegistryManager();
                Direction side = target.getSide();
                if (Stream.of(ServerScrollValueBehaviour.TYPE, ServerFilteringBehaviour.TYPE, ServerLinkBehaviour.TYPE)
                    .noneMatch(type -> smartBE.getBehaviour(type) instanceof ClipboardCloneable cc && cc.canWrite(registryManager, side))) {
                    return false;
                }
            }
        }

        VoxelShape shape = blockstate.getOutlineShape(world, pos);
        if (shape.isEmpty())
            return false;

        VertexConsumer vb = vertexConsumerProvider.getBuffer(RenderLayer.getLines());

        ms.push();
        ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
        TrackBlockOutline.renderShape(shape, ms, vb, true);
        ms.pop();
        return true;
    }

    public static void clientTick(MinecraftClient mc) {
        if (!(mc.crosshairTarget instanceof BlockHitResult target))
            return;
        ClientPlayerEntity player = mc.player;
        ItemStack stack = player.getMainHandStack();
        if (!stack.isOf(AllItems.CLIPBOARD))
            return;
        BlockPos pos = target.getBlockPos();
        ClientWorld world = mc.world;
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
            return;

        if (smartBE instanceof ClipboardBlockEntity) {
            List<MutableText> tip = new ArrayList<>();
            tip.add(CreateLang.translateDirect("clipboard.actions"));
            tip.add(CreateLang.translateDirect("clipboard.copy_other_clipboard", Text.keybind("key.use")));
            Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
            return;
        }


        Direction side = target.getSide();
        boolean canCopy = Stream.of(ServerScrollValueBehaviour.TYPE, ServerFilteringBehaviour.TYPE, ServerLinkBehaviour.TYPE)
            .anyMatch(type -> smartBE.getBehaviour(type) instanceof ClipboardCloneable cc && cc.canWrite(
                world.getRegistryManager(),
                side
            )) || smartBE instanceof ClipboardCloneable ccbe && ccbe.canWrite(world.getRegistryManager(), side);

        boolean canPaste;
        NbtCompound tagElement = stack.get(AllDataComponents.CLIPBOARD_COPIED_VALUES);
        if (tagElement == null) {
            canPaste = false;
        } else {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(smartBE.getReporterContext(), LOGGER)) {
                ReadView view = NbtReadView.create(logging, world.getRegistryManager(), tagElement);
                canPaste = (Stream.of(ServerScrollValueBehaviour.TYPE, ServerFilteringBehaviour.TYPE, ServerLinkBehaviour.TYPE)
                    .anyMatch(type -> smartBE.getBehaviour(type) instanceof ClipboardCloneable cc && view.getOptionalReadView(cc.getClipboardKey())
                        .map(v -> cc.readFromClipboard(v, player, side, true))
                        .orElse(false)) || smartBE instanceof ClipboardCloneable ccbe && view.getOptionalReadView(ccbe.getClipboardKey())
                    .map(v -> ccbe.readFromClipboard(v, player, side, true)).orElse(false));
            }
        }

        if (!canCopy && !canPaste)
            return;

        List<MutableText> tip = new ArrayList<>();
        tip.add(CreateLang.translateDirect("clipboard.actions"));
        if (canCopy)
            tip.add(CreateLang.translateDirect("clipboard.to_copy", Text.keybind("key.use")));
        if (canPaste)
            tip.add(CreateLang.translateDirect("clipboard.to_paste", Text.keybind("key.attack")));

        Create.VALUE_SETTINGS_HANDLER.showHoverTip(mc, tip);
    }
}
