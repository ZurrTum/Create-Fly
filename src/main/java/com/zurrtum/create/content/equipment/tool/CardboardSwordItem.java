package com.zurrtum.create.content.equipment.tool;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.item.CustomAttackSoundItem;
import com.zurrtum.create.foundation.item.DamageControlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.zurrtum.create.Create.MOD_ID;

public class CardboardSwordItem extends Item implements DamageControlItem, CustomAttackSoundItem {
    public static final AttributeModifier KNOCKBACK_MODIFIER = new AttributeModifier(
        Identifier.fromNamespaceAndPath(
            MOD_ID,
            "cardboard_sword_knockback_attribute_modifier"
    ), 2, AttributeModifier.Operation.ADD_VALUE
    );

    public CardboardSwordItem(Properties settings) {
        super(settings);
    }

    public static void cardboardSwordsMakeNoiseOnClick(Player player, BlockPos pos) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(AllItems.CARDBOARD_SWORD)) {
            return;
        }
        Level world = player.level();
        if (world.isClientSide()) {
            AllSoundEvents.CARDBOARD_SWORD.playAt(world, pos, 0.5f, 1.85f, false);
        } else {
            AllSoundEvents.CARDBOARD_SWORD.play(world, player, pos, 0.5f, 1.85f);
        }
    }

    @Override
    public boolean damage(Entity entity) {
        return !(entity instanceof LivingEntity) || entity.is(EntityTypeTags.ARTHROPOD);
    }

    @Override
    public void playSound(
        Level world,
        Player player,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundSource category,
        float volume,
        float pitch
    ) {
        if (!player.isSilent()) {
            AllSoundEvents.CARDBOARD_SWORD.play(world, player, x + 0.5, y + 0.5, z + 0.5, 0.75f, 1.85f);
        }
    }
}
