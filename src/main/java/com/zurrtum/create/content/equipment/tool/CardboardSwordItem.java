package com.zurrtum.create.content.equipment.tool;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.item.CustomAttackSoundItem;
import com.zurrtum.create.foundation.item.DamageControlItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.zurrtum.create.Create.MOD_ID;

public class CardboardSwordItem extends Item implements DamageControlItem, CustomAttackSoundItem {
    public static final EntityAttributeModifier KNOCKBACK_MODIFIER = new EntityAttributeModifier(
        Identifier.of(
            MOD_ID,
            "cardboard_sword_knockback_attribute_modifier"
    ), 2, EntityAttributeModifier.Operation.ADD_VALUE
    );

    public CardboardSwordItem(Settings settings) {
        super(settings);
    }

    public static void cardboardSwordsMakeNoiseOnClick(PlayerEntity player, BlockPos pos) {
        ItemStack stack = player.getMainHandStack();
        if (!stack.isOf(AllItems.CARDBOARD_SWORD)) {
            return;
        }
        World world = player.getWorld();
        if (world.isClient) {
            AllSoundEvents.CARDBOARD_SWORD.playAt(world, pos, 0.5f, 1.85f, false);
        } else {
            AllSoundEvents.CARDBOARD_SWORD.play(world, player, pos, 0.5f, 1.85f);
        }
    }

    @Override
    public boolean damage(Entity entity) {
        return !(entity instanceof LivingEntity) || entity.getType().isIn(EntityTypeTags.ARTHROPOD);
    }

    @Override
    public void playSound(
        World world,
        PlayerEntity player,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundCategory category,
        float volume,
        float pitch
    ) {
        if (!player.isSilent()) {
            AllSoundEvents.CARDBOARD_SWORD.play(world, player, x + 0.5, y + 0.5, z + 0.5, 0.75f, 1.85f);
        }
    }
}
