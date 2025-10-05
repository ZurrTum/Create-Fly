package com.zurrtum.create.content.materials;

import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ExperienceNuggetItem extends Item {

    public ExperienceNuggetItem(Settings pProperties) {
        super(pProperties);
    }

    @Override
    public boolean hasGlint(ItemStack pStack) {
        return true;
    }

    @Override
    public ActionResult use(World pLevel, PlayerEntity pPlayer, Hand pUsedHand) {
        ItemStack itemInHand = pPlayer.getStackInHand(pUsedHand);
        if (pLevel.isClient()) {
            pLevel.playSound(pPlayer, pPlayer.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, .5f, 1);
            return ActionResult.CONSUME.withNewHandStack(itemInHand);
        }

        int amountUsed = pPlayer.isSneaking() ? 1 : itemInHand.getCount();
        int total = MathHelper.ceil(3f * amountUsed);
        int maxOrbs = amountUsed == 1 ? 1 : 5;
        int valuePer = Math.max(1, 1 + total / maxOrbs);

        for (int i = 0; i < maxOrbs; i++) {
            int value = Math.min(valuePer, total - i * valuePer);
            if (value == 0)
                continue;

            Vec3d offset = VecHelper.offsetRandomly(Vec3d.ZERO, pLevel.random, 1).normalize();
            Vec3d look = pPlayer.getRotationVector();
            Vec3d motion = look.multiply(0.2).add(0, 0.2, 0).add(offset.multiply(.1));
            Vec3d cross = look.crossProduct(VecHelper.rotate(new Vec3d(-.75f, 0, 0), -pPlayer.getYaw(), Axis.Y));

            Vec3d global = pPlayer.getEyePos().add(look.multiply(.5f)).add(cross);
            ExperienceOrbEntity xp = new ExperienceOrbEntity(pLevel, global.x, global.y, global.z, value);
            xp.setVelocity(motion);
            pLevel.spawnEntity(xp);
        }

        itemInHand.decrement(amountUsed);
        if (!itemInHand.isEmpty())
            return ActionResult.SUCCESS.withNewHandStack(itemInHand);

        pPlayer.setStackInHand(pUsedHand, ItemStack.EMPTY);
        return ActionResult.CONSUME.withNewHandStack(itemInHand);
    }

}
