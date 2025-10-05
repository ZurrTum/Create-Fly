package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.factoryBoard.*;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.c2s.FactoryPanelConnectionPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class FactoryPanelConnectionHandler {

    static FactoryPanelPosition connectingFrom;
    static Box connectingFromBox;

    static boolean relocating;
    static FactoryPanelPosition validRelocationTarget;

    public static boolean panelClicked(WorldAccess level, PlayerEntity player, ServerFactoryPanelBehaviour panel) {
        if (connectingFrom == null)
            return false;

        ServerFactoryPanelBehaviour at = ServerFactoryPanelBehaviour.at(level, connectingFrom);
        if (panel.getPanelPosition().equals(connectingFrom) || at == null) {
            player.sendMessage(Text.empty(), true);
            connectingFrom = null;
            connectingFromBox = null;
            return true;
        }

        String checkForIssues = checkForIssues(at, panel);
        if (checkForIssues != null) {
            player.sendMessage(CreateLang.translate(checkForIssues).style(Formatting.RED).component(), true);
            connectingFrom = null;
            connectingFromBox = null;
            AllSoundEvents.DENY.playAt(player.getEntityWorld(), player.getBlockPos(), 1, 1, false);
            return true;
        }

        ItemStack filterFrom = panel.getFilter();
        ItemStack filterTo = at.getFilter();

        ((ClientPlayerEntity) player).networkHandler.sendPacket(new FactoryPanelConnectionPacket(panel.getPanelPosition(), connectingFrom, false));

        player.sendMessage(
            CreateLang.translate("factory_panel.panels_connected", filterFrom.getName().getString(), filterTo.getName().getString())
                .style(Formatting.GREEN).component(), true
        );

        connectingFrom = null;
        connectingFromBox = null;
        player.getEntityWorld()
            .playSoundAtBlockCenterClient(player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE, SoundCategory.BLOCKS, 0.5f, 0.5f, false);

        return true;
    }

    @Nullable
    private static String checkForIssues(ServerFactoryPanelBehaviour from, ServerFactoryPanelBehaviour to) {
        if (from == null)
            return "factory_panel.connection_aborted";
        if (from.targetedBy.containsKey(to.getPanelPosition()))
            return "factory_panel.already_connected";
        if (from.targetedBy.size() >= 9)
            return "factory_panel.cannot_add_more_inputs";

        BlockState state1 = to.blockEntity.getCachedState();
        BlockState state2 = from.blockEntity.getCachedState();
        BlockPos diff = to.getPos().subtract(from.getPos());

        if (state1.with(FactoryPanelBlock.WATERLOGGED, false).with(FactoryPanelBlock.POWERED, false) != state2.with(
            FactoryPanelBlock.WATERLOGGED,
            false
        ).with(FactoryPanelBlock.POWERED, false))
            return "factory_panel.same_orientation";

        if (FactoryPanelBlock.connectedDirection(state1).getAxis().choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
            return "factory_panel.same_surface";

        if (!diff.isWithinDistance(BlockPos.ORIGIN, 16))
            return "factory_panel.too_far_apart";

        if (to.panelBE().restocker)
            return "factory_panel.input_in_restock_mode";

        if (to.getFilter().isEmpty() || from.getFilter().isEmpty())
            return "factory_panel.no_item";

        return null;
    }

    @Nullable
    private static String checkForIssues(ServerFactoryPanelBehaviour from, FactoryPanelSupportBehaviour to) {
        if (from == null)
            return "factory_panel.connection_aborted";

        BlockState state1 = from.blockEntity.getCachedState();
        BlockState state2 = to.blockEntity.getCachedState();
        BlockPos diff = to.getPos().subtract(from.getPos());
        Direction connectedDirection = FactoryPanelBlock.connectedDirection(state1);

        if (connectedDirection != state2.get(WrenchableDirectionalBlock.FACING, connectedDirection))
            return "factory_panel.same_orientation";

        if (connectedDirection.getAxis().choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
            return "factory_panel.same_surface";

        if (!diff.isWithinDistance(BlockPos.ORIGIN, 16))
            return "factory_panel.too_far_apart";

        return null;
    }

    public static void clientTick(MinecraftClient mc) {
        if (connectingFrom == null || connectingFromBox == null)
            return;

        ClientWorld world = mc.world;
        ServerFactoryPanelBehaviour at = ServerFactoryPanelBehaviour.at(world, connectingFrom);

        if (!connectingFrom.pos().isWithinDistance(mc.player.getBlockPos(), 16) || at == null) {
            connectingFrom = null;
            connectingFromBox = null;
            mc.player.sendMessage(Text.empty(), true);
            return;
        }

        Outliner.getInstance().showAABB(connectingFrom, connectingFromBox).colored(AnimationTickHolder.getTicks() % 16 > 8 ? 0x38b764 : 0xa7f070)
            .lineWidth(1 / 16f);

        mc.player.sendMessage(
            CreateLang.translate(relocating ? "factory_panel.click_to_relocate" : "factory_panel.click_second_panel").component(),
            true
        );

        if (!relocating)
            return;

        validRelocationTarget = null;

        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS)
            return;

        Vec3d offsetPos = bhr.getPos().add(Vec3d.of(bhr.getSide().getVector()).multiply(1 / 32f));
        BlockPos pos = BlockPos.ofFloored(offsetPos);
        BlockState blockState = at.blockEntity.getCachedState();
        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(pos, blockState, offsetPos);
        BlockPos diff = pos.subtract(connectingFrom.pos());
        Direction facing = FactoryPanelBlock.connectedDirection(blockState);

        if (facing.getAxis().choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
            return;
        if (!AllBlocks.FACTORY_GAUGE.canPlaceAt(blockState, world, pos))
            return;
        if (world.getBlockState(pos.offset(facing.getOpposite())).isOf(AllBlocks.PACKAGER))
            return;

        validRelocationTarget = new FactoryPanelPosition(pos, slot);

        Outliner.getInstance().showAABB("target", getBB(blockState, validRelocationTarget)).colored(0xeeeeee).disableLineNormals().lineWidth(1 / 16f);
    }

    public static boolean onRightClick(MinecraftClient mc) {
        if (connectingFrom == null || connectingFromBox == null)
            return false;
        boolean missed = false;

        ClientPlayerEntity player = mc.player;
        if (relocating) {
            if (player.isSneaking())
                validRelocationTarget = null;
            if (validRelocationTarget != null)
                player.networkHandler.sendPacket(new FactoryPanelConnectionPacket(validRelocationTarget, connectingFrom, true));

            connectingFrom = null;
            connectingFromBox = null;

            if (validRelocationTarget == null)
                player.sendMessage(CreateLang.translate("factory_panel.relocation_aborted").component(), true);

            relocating = false;
            validRelocationTarget = null;
            return true;
        }

        if (mc.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() != Type.MISS) {
            ClientWorld world = mc.world;
            BlockEntity blockEntity = world.getBlockEntity(bhr.getBlockPos());
            FactoryPanelSupportBehaviour behaviour = BlockEntityBehaviour.get(world, bhr.getBlockPos(), FactoryPanelSupportBehaviour.TYPE);

            // Connecting redstone or display links
            if (behaviour != null) {
                ServerFactoryPanelBehaviour at = ServerFactoryPanelBehaviour.at(world, connectingFrom);
                String checkForIssues = checkForIssues(at, behaviour);
                if (checkForIssues != null) {
                    player.sendMessage(CreateLang.translate(checkForIssues).style(Formatting.RED).component(), true);
                    connectingFrom = null;
                    connectingFromBox = null;
                    AllSoundEvents.DENY.playAt(world, player.getBlockPos(), 1, 1, false);
                    return true;
                }

                FactoryPanelPosition bestPosition = null;
                double bestDistance = Double.POSITIVE_INFINITY;

                for (PanelSlot slot : PanelSlot.values()) {
                    FactoryPanelPosition panelPosition = new FactoryPanelPosition(blockEntity.getPos(), slot);
                    FactoryPanelConnection connection = new FactoryPanelConnection(panelPosition, 1);
                    Vec3d diff = connection.calculatePathDiff(world.getBlockState(connectingFrom.pos()), connectingFrom);
                    if (bestDistance < diff.lengthSquared())
                        continue;
                    bestDistance = diff.lengthSquared();
                    bestPosition = panelPosition;
                }

                player.networkHandler.sendPacket(new FactoryPanelConnectionPacket(bestPosition, connectingFrom, false));

                player.sendMessage(
                    CreateLang.translate("factory_panel.link_connected", blockEntity.getCachedState().getBlock().getName())
                        .style(Formatting.GREEN).component(), true
                );

                connectingFrom = null;
                connectingFromBox = null;
                player.getEntityWorld().playSoundAtBlockCenterClient(
                    player.getBlockPos(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE,
                    SoundCategory.BLOCKS,
                    0.5f,
                    0.5f,
                    false
                );
                return true;
            }

            if (!(blockEntity instanceof FactoryPanelBlockEntity))
                missed = true;
        }

        if (!player.isSneaking() && !missed)
            return false;
        connectingFrom = null;
        connectingFromBox = null;
        player.sendMessage(CreateLang.translate("factory_panel.connection_aborted").component(), true);
        return true;
    }

    public static void startRelocating(ServerFactoryPanelBehaviour behaviour) {
        startConnection(behaviour);
        relocating = true;
    }

    public static void startConnection(ServerFactoryPanelBehaviour behaviour) {
        relocating = false;
        connectingFrom = behaviour.getPanelPosition();
        connectingFromBox = getBB(behaviour.blockEntity.getCachedState(), connectingFrom);
    }

    public static Box getBB(BlockState blockState, FactoryPanelPosition factoryPanelPosition) {
        Vec3d location = FactoryPanelSlotPositioning.getCenterOfSlot(blockState, factoryPanelPosition.slot())
            .add(Vec3d.of(factoryPanelPosition.pos()));
        Vec3d plane = VecHelper.axisAlingedPlaneOf(FactoryPanelBlock.connectedDirection(blockState));
        return new Box(location, location).expand(plane.x * 3 / 16f, plane.y * 3 / 16f, plane.z * 3 / 16f);
    }

}
