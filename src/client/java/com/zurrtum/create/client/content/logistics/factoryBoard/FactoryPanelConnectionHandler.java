package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.factoryBoard.*;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.infrastructure.packet.c2s.FactoryPanelConnectionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FactoryPanelConnectionHandler {

    static @Nullable FactoryPanelPosition connectingFrom;
    static @Nullable AABB connectingFromBox;

    static boolean relocating;
    static @Nullable FactoryPanelPosition validRelocationTarget;

    public static boolean panelClicked(LevelAccessor level, Player player, ServerFactoryPanelBehaviour panel) {
        if (connectingFrom == null)
            return false;

        ServerFactoryPanelBehaviour at = ServerFactoryPanelBehaviour.at(level, connectingFrom);
        if (panel.getPanelPosition().equals(connectingFrom) || at == null) {
            player.displayClientMessage(Component.empty(), true);
            connectingFrom = null;
            connectingFromBox = null;
            return true;
        }

        String checkForIssues = checkForIssues(at, panel);
        if (checkForIssues != null) {
            player.displayClientMessage(CreateLang.translate(checkForIssues).style(ChatFormatting.RED).component(), true);
            connectingFrom = null;
            connectingFromBox = null;
            AllSoundEvents.DENY.playAt(player.level(), player.blockPosition(), 1, 1, false);
            return true;
        }

        ItemStack filterFrom = panel.getFilter();
        ItemStack filterTo = at.getFilter();

        ((LocalPlayer) player).connection.send(new FactoryPanelConnectionPacket(panel.getPanelPosition(), connectingFrom, false));

        player.displayClientMessage(
            CreateLang.translate("factory_panel.panels_connected", filterFrom.getHoverName().getString(), filterTo.getHoverName().getString())
                .style(ChatFormatting.GREEN).component(), true
        );

        connectingFrom = null;
        connectingFromBox = null;
        player.level().playLocalSound(player.blockPosition(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 0.5f, 0.5f, false);

        return true;
    }

    @Nullable
    private static String checkForIssues(@Nullable ServerFactoryPanelBehaviour from, ServerFactoryPanelBehaviour to) {
        if (from == null)
            return "factory_panel.connection_aborted";
        if (from.targetedBy.containsKey(to.getPanelPosition()))
            return "factory_panel.already_connected";
        if (from.targetedBy.size() >= 9)
            return "factory_panel.cannot_add_more_inputs";

        BlockState state1 = to.blockEntity.getBlockState();
        BlockState state2 = from.blockEntity.getBlockState();
        BlockPos diff = to.getPos().subtract(from.getPos());

        if (state1.setValue(FactoryPanelBlock.WATERLOGGED, false)
            .setValue(FactoryPanelBlock.POWERED, false) != state2.setValue(FactoryPanelBlock.WATERLOGGED, false)
            .setValue(FactoryPanelBlock.POWERED, false))
            return "factory_panel.same_orientation";

        if (FactoryPanelBlock.connectedDirection(state1).getAxis().choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
            return "factory_panel.same_surface";

        if (!diff.closerThan(BlockPos.ZERO, 16))
            return "factory_panel.too_far_apart";

        if (to.panelBE().restocker)
            return "factory_panel.input_in_restock_mode";

        if (to.getFilter().isEmpty() || from.getFilter().isEmpty())
            return "factory_panel.no_item";

        return null;
    }

    @Nullable
    private static String checkForIssues(@Nullable ServerFactoryPanelBehaviour from, FactoryPanelSupportBehaviour to) {
        if (from == null)
            return "factory_panel.connection_aborted";

        BlockState state1 = from.blockEntity.getBlockState();
        BlockState state2 = to.blockEntity.getBlockState();
        BlockPos diff = to.getPos().subtract(from.getPos());
        Direction connectedDirection = FactoryPanelBlock.connectedDirection(state1);

        if (connectedDirection != state2.getValueOrElse(WrenchableDirectionalBlock.FACING, connectedDirection))
            return "factory_panel.same_orientation";

        if (connectedDirection.getAxis().choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
            return "factory_panel.same_surface";

        if (!diff.closerThan(BlockPos.ZERO, 16))
            return "factory_panel.too_far_apart";

        return null;
    }

    public static void clientTick(Minecraft mc) {
        if (connectingFrom == null || connectingFromBox == null)
            return;

        ClientLevel world = mc.level;
        ServerFactoryPanelBehaviour at = ServerFactoryPanelBehaviour.at(world, connectingFrom);

        if (!connectingFrom.pos().closerThan(mc.player.blockPosition(), 16) || at == null) {
            connectingFrom = null;
            connectingFromBox = null;
            mc.player.displayClientMessage(Component.empty(), true);
            return;
        }

        Outliner.getInstance().showAABB(connectingFrom, connectingFromBox).colored(AnimationTickHolder.getTicks() % 16 > 8 ? 0x38b764 : 0xa7f070)
            .lineWidth(1 / 16f);

        mc.player.displayClientMessage(
            CreateLang.translate(relocating ? "factory_panel.click_to_relocate" : "factory_panel.click_second_panel").component(), true);

        if (!relocating)
            return;

        validRelocationTarget = null;

        if (!(mc.hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS)
            return;

        Vec3 offsetPos = bhr.getLocation().add(Vec3.atLowerCornerOf(bhr.getDirection().getUnitVec3i()).scale(1 / 32f));
        BlockPos pos = BlockPos.containing(offsetPos);
        BlockState blockState = at.blockEntity.getBlockState();
        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(pos, blockState, offsetPos);
        BlockPos diff = pos.subtract(connectingFrom.pos());
        Direction facing = FactoryPanelBlock.connectedDirection(blockState);

        if (facing.getAxis().choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
            return;
        if (!AllBlocks.FACTORY_GAUGE.canSurvive(blockState, world, pos))
            return;
        if (world.getBlockState(pos.relative(facing.getOpposite())).is(AllBlocks.PACKAGER))
            return;

        validRelocationTarget = new FactoryPanelPosition(pos, slot);

        Outliner.getInstance().showAABB("target", getBB(blockState, validRelocationTarget)).colored(0xeeeeee).disableLineNormals().lineWidth(1 / 16f);
    }

    public static boolean onRightClick(Minecraft mc) {
        if (connectingFrom == null || connectingFromBox == null)
            return false;
        boolean missed = false;

        LocalPlayer player = mc.player;
        if (relocating) {
            if (player.isShiftKeyDown())
                validRelocationTarget = null;
            if (validRelocationTarget != null)
                player.connection.send(new FactoryPanelConnectionPacket(validRelocationTarget, connectingFrom, true));

            connectingFrom = null;
            connectingFromBox = null;

            if (validRelocationTarget == null)
                player.displayClientMessage(CreateLang.translate("factory_panel.relocation_aborted").component(), true);

            relocating = false;
            validRelocationTarget = null;
            return true;
        }

        if (mc.hitResult instanceof BlockHitResult bhr && bhr.getType() != Type.MISS) {
            ClientLevel world = mc.level;
            BlockEntity blockEntity = world.getBlockEntity(bhr.getBlockPos());
            FactoryPanelSupportBehaviour behaviour = BlockEntityBehaviour.get(world, bhr.getBlockPos(), FactoryPanelSupportBehaviour.TYPE);

            // Connecting redstone or display links
            if (behaviour != null) {
                ServerFactoryPanelBehaviour at = ServerFactoryPanelBehaviour.at(world, connectingFrom);
                String checkForIssues = checkForIssues(at, behaviour);
                if (checkForIssues != null) {
                    player.displayClientMessage(CreateLang.translate(checkForIssues).style(ChatFormatting.RED).component(), true);
                    connectingFrom = null;
                    connectingFromBox = null;
                    AllSoundEvents.DENY.playAt(world, player.blockPosition(), 1, 1, false);
                    return true;
                }

                FactoryPanelPosition bestPosition = null;
                double bestDistance = Double.POSITIVE_INFINITY;

                for (PanelSlot slot : PanelSlot.values()) {
                    FactoryPanelPosition panelPosition = new FactoryPanelPosition(blockEntity.getBlockPos(), slot);
                    FactoryPanelConnection connection = new FactoryPanelConnection(panelPosition, 1);
                    Vec3 diff = connection.calculatePathDiff(world.getBlockState(connectingFrom.pos()), connectingFrom);
                    if (bestDistance < diff.lengthSqr())
                        continue;
                    bestDistance = diff.lengthSqr();
                    bestPosition = panelPosition;
                }

                player.connection.send(new FactoryPanelConnectionPacket(bestPosition, connectingFrom, false));

                player.displayClientMessage(
                    CreateLang.translate("factory_panel.link_connected", blockEntity.getBlockState().getBlock().getName()).style(ChatFormatting.GREEN)
                        .component(), true
                );

                connectingFrom = null;
                connectingFromBox = null;
                player.level().playLocalSound(player.blockPosition(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 0.5f, 0.5f, false);
                return true;
            }

            if (!(blockEntity instanceof FactoryPanelBlockEntity))
                missed = true;
        }

        if (!player.isShiftKeyDown() && !missed)
            return false;
        connectingFrom = null;
        connectingFromBox = null;
        player.displayClientMessage(CreateLang.translate("factory_panel.connection_aborted").component(), true);
        return true;
    }

    public static void startRelocating(ServerFactoryPanelBehaviour behaviour) {
        startConnection(behaviour);
        relocating = true;
    }

    public static void startConnection(ServerFactoryPanelBehaviour behaviour) {
        relocating = false;
        connectingFrom = behaviour.getPanelPosition();
        connectingFromBox = getBB(behaviour.blockEntity.getBlockState(), connectingFrom);
    }

    public static AABB getBB(BlockState blockState, FactoryPanelPosition factoryPanelPosition) {
        Vec3 location = FactoryPanelSlotPositioning.getCenterOfSlot(blockState, factoryPanelPosition.slot())
            .add(Vec3.atLowerCornerOf(factoryPanelPosition.pos()));
        Vec3 plane = VecHelper.axisAlingedPlaneOf(FactoryPanelBlock.connectedDirection(blockState));
        return new AABB(location, location).inflate(plane.x * 3 / 16f, plane.y * 3 / 16f, plane.z * 3 / 16f);
    }

}
