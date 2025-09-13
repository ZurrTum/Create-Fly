package com.zurrtum.create.content.trains.entity;

import com.google.common.collect.Streams;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TrainStatus {

    Train train;

    public boolean navigation;
    public boolean track;
    public boolean conductor;

    List<StatusMessage> queued = new ArrayList<>();

    public TrainStatus(Train train) {
        this.train = train;
    }

    public void failedNavigation() {
        if (navigation)
            return;
        displayInformation("no_path", false);
        navigation = true;
    }

    public void failedNavigationNoTarget(String filter) {
        if (navigation)
            return;
        displayInformation("no_match", false, filter);
        navigation = true;
    }

    public void failedPackageNoTarget(String address) {
        if (navigation)
            return;
        displayInformation("no_package_target", false, address);
        navigation = true;
    }

    public void successfulNavigation() {
        if (!navigation)
            return;
        displayInformation("navigation_success", true);
        navigation = false;
    }

    public void foundConductor() {
        if (!conductor)
            return;
        displayInformation("found_driver", true);
        conductor = false;
    }

    public void missingConductor() {
        if (conductor)
            return;
        displayInformation("missing_driver", false);
        conductor = true;
    }

    public void missingCorrectConductor() {
        if (conductor)
            return;
        displayInformation("opposite_driver", false);
        conductor = true;
    }

    public void manualControls() {
        displayInformation("paused_for_manual", true);
    }

    public void failedMigration() {
        if (track)
            return;
        displayInformation("track_missing", false);
        track = true;
    }

    public void highStress() {
        if (track)
            return;
        displayInformation("coupling_stress", false);
        track = true;
    }

    public void doublePortal() {
        if (track)
            return;
        displayInformation("double_portal", false);
        track = true;
    }

    public void endOfTrack() {
        if (track)
            return;
        displayInformation("end_of_track", false);
        track = true;
    }

    public void crash() {
        Text component = Text.literal(" - ").formatted(Formatting.GRAY)
            .append(Text.translatable("create.train.status.collision").withColor(0xFFD3B4));
        List<RegistryKey<World>> presentDimensions = train.getPresentDimensions();
        Stream<Text> locationComponents = presentDimensions.stream().map(key -> {
            return Text.literal(" - ").formatted(Formatting.GRAY).append(Text.translatable(
                "create.train.status.collision.where",
                key.getValue().toString(),
                train.getPositionInDimension(key).get().toShortString()
            ).withColor(0xFFD3B4));
        });
        addMessage(new StatusMessage(Streams.concat(Stream.of(component), locationComponents).toArray(Text[]::new)));

    }

    public void successfulMigration() {
        if (!track)
            return;
        displayInformation("back_on_track", true);
        track = false;
    }

    public void trackOK() {
        track = false;
    }

    public void tick(World level) {
        if (queued.isEmpty())
            return;
        LivingEntity owner = train.getOwner(level);
        if (owner == null)
            return;
        if (owner instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("create.train.status", train.name).formatted(Formatting.GOLD), false);
            queued.forEach(message -> message.displayToPlayer(player));
        }
        queued.clear();
    }

    public void displayInformation(String key, boolean itsAGoodThing, Object... args) {
        MutableText component = Text.literal(" - ").formatted(Formatting.GRAY)
            .append(Text.translatable("create.train.status." + key, args).withColor(itsAGoodThing ? 0xD5ECC2 : 0xFFD3B4));
        addMessage(new StatusMessage(component));
    }

    public void addMessage(StatusMessage message) {
        queued.add(message);

        if (queued.size() > 3)
            queued.remove(0);
    }

    public void newSchedule() {
        navigation = false;
        conductor = false;
    }

    public record StatusMessage(Text... messages) {
        public void displayToPlayer(PlayerEntity player) {
            Arrays.stream(messages).forEach(messages -> player.sendMessage(messages, false));
        }

    }
}
