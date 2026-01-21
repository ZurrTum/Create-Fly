package com.zurrtum.create;

import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.Create.MOD_ID;
import static net.minecraft.sounds.SoundSource.*;

public class AllSoundEvents {
    public static final SoundEntry SCHEMATICANNON_LAUNCH_BLOCK = register("schematicannon_launch_block", BLOCKS, .1f, 1.1f);
    public static final SoundEntry SCHEMATICANNON_FINISH = register("schematicannon_finish", BLOCKS, 1, .7f);
    public static final SoundEntry DEPOT_SLIDE = register("depot_slide", BLOCKS, .125f, 1.5f);
    public static final SoundEntry DEPOT_PLOP = register("depot_plop", BLOCKS, .25f, 1.25f);
    public static final SoundEntry FUNNEL_FLAP = register("funnel_flap", BLOCKS, .125f, 1.5f, .0425f, .75f);
    public static final SoundEntry PACKAGER = register("packager", BLOCKS, 0.5f, 0.75f);
    public static final SoundEntry SLIME_ADDED = register("slime_added", BLOCKS);
    public static final SoundEntry MECHANICAL_PRESS_ACTIVATION = register("mechanical_press_activation", BLOCKS, .125f, 1f, .5f, 1f);
    public static final SoundEntry MECHANICAL_PRESS_ACTIVATION_ON_BELT = register("mechanical_press_activation_belt", BLOCKS, .75f, 1f, .15f, .75f);
    public static final SoundEntry MIXING = register("mixing", BLOCKS, .125f, .5f, .125f, .5f);
    public static final SoundEntry SPOUTING = register("spout", BLOCKS);
    public static final SoundEntry CRANKING = register("cranking", BLOCKS, .075f, .5f, .025f, .5f);
    public static final SoundEntry WORLDSHAPER_PLACE = register("worldshaper_place", PLAYERS);
    public static final SoundEntry SCROLL_VALUE = register("scroll_value", PLAYERS, .124f, 1f);
    public static final SoundEntry CONFIRM = register("confirm", PLAYERS, 0.5f, 0.8f);
    public static final SoundEntry CONFIRM_2 = register("confirm_2", PLAYERS);
    public static final SoundEntry DENY = register("deny", PLAYERS, 1f, 0.5f);
    public static final SoundEntry COGS = register("cogs", BLOCKS);
    public static final SoundEntry FWOOMP = register("fwoomp", PLAYERS);
    public static final SoundEntry CARDBOARD_SWORD = register("cardboard_bonk", PLAYERS);
    public static final SoundEntry FROGPORT_OPEN = register("frogport_open", BLOCKS, 1f, 2f);
    public static final SoundEntry FROGPORT_CLOSE = register("frogport_close", BLOCKS);
    public static final SoundEntry FROGPORT_CATCH = register("frogport_catch", BLOCKS);
    public static final SoundEntry STOCK_LINK = register("stock_link", BLOCKS);
    public static final SoundEntry FROGPORT_DEPOSIT = register("frogport_deposit", BLOCKS);
    public static final SoundEntry POTATO_HIT = register("potato_hit", PLAYERS, .75f, .75f, .75f, 1.25f);
    public static final SoundEntry CONTRAPTION_ASSEMBLE = register("contraption_assemble", BLOCKS, .5f, .5f, .045f, .74f);
    public static final SoundEntry CONTRAPTION_DISASSEMBLE = register("contraption_disassemble", BLOCKS, .35f, .75f);
    public static final SoundEntry WRENCH_ROTATE = register("wrench_rotate", BLOCKS, .25f, 1.25f);
    public static final SoundEntry WRENCH_REMOVE = register("wrench_remove", BLOCKS, .25f, .75f, .25f, .75f);
    public static final SoundEntry PACKAGE_POP = register("package_pop", BLOCKS, .75f, 1f, .25f, 1.15f);
    public static final SoundEntry CRAFTER_CLICK = register("crafter_click", BLOCKS, .25f, 1, .125f, 1);
    public static final SoundEntry CRAFTER_CRAFT = register("crafter_craft", BLOCKS, .125f, .75f);
    public static final SoundEntry SANDING_SHORT = register("sanding_short", BLOCKS);
    public static final SoundEntry SANDING_LONG = register("sanding_long", BLOCKS);
    public static final SoundEntry CONTROLLER_CLICK = register("controller_click", BLOCKS, .35f, 1f);
    public static final SoundEntry CONTROLLER_PUT = register("controller_put", BLOCKS);
    public static final SoundEntry CONTROLLER_TAKE = register("controller_take", BLOCKS);
    public static final SoundEntry SAW_ACTIVATE_WOOD = register("saw_activate_wood", BLOCKS, .75f, 1.5f);
    public static final SoundEntry SAW_ACTIVATE_STONE = register("saw_activate_stone", BLOCKS, .125f, 1.25f);
    public static final SoundEntry BLAZE_MUNCH = register("blaze_munch", BLOCKS, .5f, 1f);
    public static final SoundEntry ITEM_HATCH = register("item_hatch", BLOCKS, .25f, 1.4f, .75f, 1.15f);
    public static final SoundEntry CRUSHING_1 = register("crushing_1", BLOCKS);
    public static final SoundEntry CRUSHING_2 = register("crushing_2", BLOCKS);
    public static final SoundEntry CRUSHING_3 = register("crushing_3", BLOCKS);
    public static final SoundEntry PECULIAR_BELL_USE = register("peculiar_bell_use", BLOCKS);
    public static final SoundEntry DESK_BELL_USE = register("desk_bell", BLOCKS);
    public static final SoundEntry WHISTLE_HIGH = register("whistle_high", RECORDS);
    public static final SoundEntry WHISTLE_MEDIUM = register("whistle", RECORDS);
    public static final SoundEntry WHISTLE_LOW = register("whistle_low", RECORDS);
    public static final SoundEntry STEAM = register("steam", NEUTRAL);
    public static final SoundEntry TRAIN = register("train", NEUTRAL);
    public static final SoundEntry TRAIN2 = register("train2", NEUTRAL);
    public static final SoundEntry TRAIN3 = register("train3", NEUTRAL);
    public static final SoundEntry WHISTLE_TRAIN = register("whistle_train", RECORDS);
    public static final SoundEntry WHISTLE_TRAIN_LOW = register("whistle_train_low", RECORDS);
    public static final SoundEntry WHISTLE_TRAIN_MANUAL = register("whistle_train_manual", NEUTRAL);
    public static final SoundEntry WHISTLE_TRAIN_MANUAL_LOW = register("whistle_train_manual_low", NEUTRAL);
    public static final SoundEntry WHISTLE_TRAIN_MANUAL_END = register("whistle_train_manual_end", NEUTRAL);
    public static final SoundEntry WHISTLE_TRAIN_MANUAL_LOW_END = register("whistle_train_manual_low_end", NEUTRAL);
    public static final SoundEntry WHISTLE_CHIFF = register("chiff", RECORDS);
    public static final SoundEntry HAUNTED_BELL_CONVERT = register("haunted_bell_convert", BLOCKS);
    public static final SoundEntry HAUNTED_BELL_USE = register("haunted_bell_use", BLOCKS);
    public static final SoundEntry STOCK_TICKER_REQUEST = register("stock_ticker_request", BLOCKS);
    public static final SoundEntry STOCK_TICKER_TRADE = register("stock_ticker_trade", BLOCKS);
    public static final SoundEntry CLIPBOARD_CHECKMARK = register("clipboard_check", BLOCKS);
    public static final SoundEntry CLIPBOARD_ERASE = register("clipboard_erase", BLOCKS);

    private static SoundEntry register(String name, SoundSource category, float... data) {
        if (data.length == 0) {
            data = new float[]{1f, 1f};
        }
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        int count = data.length / 2;
        CompiledSoundEvent[] compiledEvents = new CompiledSoundEvent[count];
        SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        compiledEvents[0] = new CompiledSoundEvent(event, data[0], data[1]);
        for (int i = 2, j = 1; j < count; i += 2, j++) {
            event = Registry.register(BuiltInRegistries.SOUND_EVENT, id.withSuffix("_compounded_" + j), SoundEvent.createVariableRangeEvent(id));
            compiledEvents[j] = new CompiledSoundEvent(event, data[i], data[i + 1]);
        }
        return new SoundEntry(category, compiledEvents);
    }

    public record SoundEntry(SoundSource category, CompiledSoundEvent[] sounds) {
        public SoundEvent getMainEvent() {
            return sounds[0].event();
        }

        public void play(Level world, @Nullable Player entity, double x, double y, double z, float volume, float pitch) {
            for (CompiledSoundEvent sound : sounds) {
                sound.play(world, entity, x, y, z, category, volume, pitch);
            }
        }

        public void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
            for (CompiledSoundEvent sound : sounds) {
                sound.playAt(world, x, y, z, category, volume, pitch, fade);
            }
        }

        public void playOnServer(Level world, Vec3i pos) {
            playOnServer(world, pos, 1, 1);
        }

        public void playOnServer(Level world, Vec3i pos, float volume, float pitch) {
            play(world, null, pos, volume, pitch);
        }

        public void play(Level world, @Nullable Player entity, Vec3i pos) {
            play(world, entity, pos, 1, 1);
        }

        public void playFrom(Entity entity) {
            playFrom(entity, 1, 1);
        }

        public void playFrom(Entity entity, float volume, float pitch) {
            if (!entity.isSilent())
                play(entity.level(), null, entity.blockPosition(), volume, pitch);
        }

        public void play(Level world, @Nullable Player entity, Vec3i pos, float volume, float pitch) {
            play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
        }

        public void play(Level world, Player entity, Vec3 pos, float volume, float pitch) {
            play(world, entity, pos.x(), pos.y(), pos.z(), volume, pitch);
        }

        public void playAt(Level world, Vec3i pos, float volume, float pitch, boolean fade) {
            playAt(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch, fade);
        }

        public void playAt(Level world, Vec3 pos, float volume, float pitch, boolean fade) {
            playAt(world, pos.x(), pos.y(), pos.z(), volume, pitch, fade);
        }
    }

    private record CompiledSoundEvent(SoundEvent event, float volume, float pitch) {
        public void play(Level world, @Nullable Player entity, double x, double y, double z, SoundSource category, float volume, float pitch) {
            world.playSound(entity, x, y, z, event(), category, volume() * volume, pitch() * pitch);
        }

        public void playAt(Level world, double x, double y, double z, SoundSource category, float volume, float pitch, boolean fade) {
            world.playLocalSound(x, y, z, event(), category, volume() * volume, pitch() * pitch, fade);
        }
    }

    public static void register() {
    }
}
