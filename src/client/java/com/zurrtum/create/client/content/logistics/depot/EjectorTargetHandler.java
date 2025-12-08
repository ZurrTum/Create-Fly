package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.depot.EntityLauncher;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorPlacementPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EjectorTargetHandler {

    static BlockPos currentSelection;
    static ItemStack currentItem;
    static long lastHoveredBlockPos = -1;
    static EntityLauncher launcher;

    public static InteractionResult rightClickingBlocksSelectsThem(Level world, LocalPlayer player, InteractionHand hand, BlockHitResult ray) {
        if (currentItem == null)
            return null;
        BlockPos pos = ray.getBlockPos();
        if (player.isSpectator() || !player.isShiftKeyDown())
            return null;

        String key = "weighted_ejector.target_set";
        player.displayClientMessage(CreateLang.translateDirect(key).withStyle(ChatFormatting.GOLD), true);
        currentSelection = pos;
        launcher = null;
        return InteractionResult.SUCCESS;
    }

    public static boolean leftClickingBlocksDeselectsThem(LocalPlayer player, BlockPos pos) {
        if (currentItem == null)
            return false;
        if (!player.isShiftKeyDown())
            return false;
        if (pos.equals(currentSelection)) {
            currentSelection = null;
            launcher = null;
            return true;
        }
        return false;
    }

    public static void flushSettings(BlockPos pos) {
        LocalPlayer player = Minecraft.getInstance().player;
        String key = "weighted_ejector.target_not_valid";
        ChatFormatting colour = ChatFormatting.WHITE;

        if (currentSelection == null)
            key = "weighted_ejector.no_target";

        Direction validTargetDirection = getValidTargetDirection(pos);
        if (validTargetDirection == null) {
            player.displayClientMessage(CreateLang.translateDirect(key).withStyle(colour), true);
            currentItem = null;
            currentSelection = null;
            return;
        }

        key = "weighted_ejector.targeting";
        colour = ChatFormatting.GREEN;

        player.displayClientMessage(
            CreateLang.translateDirect(key, currentSelection.getX(), currentSelection.getY(), currentSelection.getZ())
                .withStyle(colour), true
        );

        BlockPos diff = pos.subtract(currentSelection);
        int h = Math.abs(diff.getX() + diff.getZ());
        int v = -diff.getY();

        player.connection.send(new EjectorPlacementPacket(h, v, pos, validTargetDirection));
        currentSelection = null;
        currentItem = null;

    }

    public static Direction getValidTargetDirection(BlockPos pos) {
        if (currentSelection == null)
            return null;
        if (VecHelper.onSameAxis(pos, currentSelection, Axis.Y))
            return null;

        int xDiff = currentSelection.getX() - pos.getX();
        int zDiff = currentSelection.getZ() - pos.getZ();
        int max = AllConfigs.server().kinetics.maxEjectorDistance.get();

        if (Math.abs(xDiff) > max || Math.abs(zDiff) > max)
            return null;

        if (xDiff == 0)
            return Direction.get(zDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.Z);
        if (zDiff == 0)
            return Direction.get(xDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.X);

        return null;
    }

    public static void tick(Minecraft mc) {
        ItemStack heldItemMainhand = mc.player.getMainHandItem();
        if (!heldItemMainhand.is(AllItems.WEIGHTED_EJECTOR)) {
            currentItem = null;
        } else {
            if (heldItemMainhand != currentItem) {
                currentSelection = null;
                currentItem = heldItemMainhand;
            }
            drawOutline(mc.level, currentSelection);
        }

        boolean wrench = heldItemMainhand.is(AllItems.WRENCH);
        if (wrench) {
            checkForWrench(mc);
        }
        drawArc(mc, wrench);
    }

    protected static void drawArc(Minecraft mc, boolean wrench) {
        if (currentSelection == null)
            return;
        if (currentItem == null && !wrench)
            return;

        HitResult objectMouseOver = mc.hitResult;
        if (!(objectMouseOver instanceof BlockHitResult blockRayTraceResult))
            return;
        if (blockRayTraceResult.getType() == Type.MISS)
            return;

        BlockPos pos = blockRayTraceResult.getBlockPos();
        if (!wrench)
            pos = pos.relative(blockRayTraceResult.getDirection());

        int xDiff = currentSelection.getX() - pos.getX();
        int yDiff = currentSelection.getY() - pos.getY();
        int zDiff = currentSelection.getZ() - pos.getZ();
        int validX = Math.abs(zDiff) > Math.abs(xDiff) ? 0 : xDiff;
        int validZ = Math.abs(zDiff) < Math.abs(xDiff) ? 0 : zDiff;

        BlockPos validPos = currentSelection.offset(validX, yDiff, validZ);
        Direction d = getValidTargetDirection(validPos);
        if (d == null)
            return;
        if (launcher == null || lastHoveredBlockPos != pos.asLong()) {
            lastHoveredBlockPos = pos.asLong();
            launcher = new EntityLauncher(Math.abs(validX + validZ), yDiff);
        }

        double totalFlyingTicks = launcher.getTotalFlyingTicks() + 3;
        int segments = (((int) totalFlyingTicks) / 3) + 1;
        double tickOffset = totalFlyingTicks / segments;
        boolean valid = xDiff == validX && zDiff == validZ;
        int intColor = valid ? 0x9ede73 : 0xff7171;
        DustParticleOptions data = new DustParticleOptions(intColor, 1);
        ClientLevel world = mc.level;

        AABB bb = new AABB(0, 0, 0, 1, 0, 1).move(currentSelection.offset(-validX, -yDiff, -validZ));
        Outliner.getInstance().chaseAABB("valid", bb).colored(intColor).lineWidth(1 / 16f);

        for (int i = 0; i < segments; i++) {
            double ticks = ((AnimationTickHolder.getRenderTime() / 3) % tickOffset) + i * tickOffset;
            Vec3 vec = launcher.getGlobalPos(ticks, d, pos).add(xDiff - validX, 0, zDiff - validZ);
            world.addParticle(data, vec.x, vec.y, vec.z, 0, 0, 0);
        }
    }

    private static void checkForWrench(Minecraft mc) {
        HitResult objectMouseOver = mc.hitResult;
        if (!(objectMouseOver instanceof BlockHitResult result))
            return;
        BlockPos pos = result.getBlockPos();

        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof EjectorBlockEntity)) {
            lastHoveredBlockPos = -1;
            currentSelection = null;
            return;
        }

        if (lastHoveredBlockPos == -1 || lastHoveredBlockPos != pos.asLong()) {
            EjectorBlockEntity ejector = (EjectorBlockEntity) be;
            if (!ejector.getTargetPosition().equals(ejector.getBlockPos()))
                currentSelection = ejector.getTargetPosition();
            lastHoveredBlockPos = pos.asLong();
            launcher = null;
        }

        if (lastHoveredBlockPos != -1)
            drawOutline(mc.level, currentSelection);
    }

    public static void drawOutline(ClientLevel world, BlockPos pos) {
        if (pos == null)
            return;
        BlockState state = world.getBlockState(pos);
        VoxelShape shape = state.getShape(world, pos);
        AABB boundingBox = shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds();
        Outliner.getInstance().showAABB("target", boundingBox.move(pos)).colored(0xffcb74).lineWidth(1 / 16f);
    }

}
