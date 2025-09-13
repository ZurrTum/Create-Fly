package com.zurrtum.create.client.content.trains.station;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TrainIconType;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.infrastructure.packet.c2s.StationEditPacket;
import com.zurrtum.create.infrastructure.packet.c2s.TrainEditPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

import java.lang.ref.WeakReference;
import java.util.List;

public class AssemblyScreen extends AbstractStationScreen {

    private IconButton quitAssembly;
    private IconButton toggleAssemblyButton;
    private List<Identifier> iconTypes;
    private ScrollInput iconTypeScroll;

    public AssemblyScreen(StationBlockEntity be, GlobalStation station) {
        super(be, station);
        background = AllGuiTextures.STATION_ASSEMBLING;
    }

    @Override
    protected void init() {
        super.init();
        int x = guiLeft;
        int y = guiTop;
        int by = y + background.getHeight() - 24;

        Drawable widget = drawables.getFirst();
        if (widget instanceof IconButton ib) {
            ib.setIcon(AllIcons.I_PRIORITY_VERY_LOW);
            ib.setToolTip(CreateLang.translateDirect("station.close"));
        }

        iconTypes = TrainIconType.ALL.keySet().stream().toList();
        iconTypeScroll = new ScrollInput(x + 4, y + 17, 162, 14).titled(CreateLang.translateDirect("station.icon_type"));
        iconTypeScroll.withRange(0, iconTypes.size());
        iconTypeScroll.withStepFunction(ctx -> -iconTypeScroll.standardStep().apply(ctx));
        iconTypeScroll.calling(s -> {
            Train train = displayedTrain.get();
            if (train != null)
                train.icon = TrainIconType.byId(iconTypes.get(s));
        });
        iconTypeScroll.active = iconTypeScroll.visible = false;
        addDrawableChild(iconTypeScroll);

        toggleAssemblyButton = new WideIconButton(x + 94, by, AllGuiTextures.I_ASSEMBLE_TRAIN);
        toggleAssemblyButton.active = false;
        toggleAssemblyButton.setToolTip(CreateLang.translateDirect("station.assemble_train"));
        toggleAssemblyButton.withCallback(() -> {
            client.player.networkHandler.sendPacket(StationEditPacket.tryAssemble(blockEntity.getPos()));
        });

        quitAssembly = new IconButton(x + 73, by, AllIcons.I_DISABLE);
        quitAssembly.active = true;
        quitAssembly.setToolTip(CreateLang.translateDirect("station.cancel"));
        quitAssembly.withCallback(() -> {
            client.player.networkHandler.sendPacket(StationEditPacket.configure(blockEntity.getPos(), false, station.name, null));
            client.setScreen(new StationScreen(blockEntity, station));
        });

        addDrawableChild(toggleAssemblyButton);
        addDrawableChild(quitAssembly);

        tickTrainDisplay();
    }

    @Override
    public void tick() {
        super.tick();
        tickTrainDisplay();
        Train train = displayedTrain.get();
        toggleAssemblyButton.active = blockEntity.bogeyCount > 0 || train != null;

        if (train != null) {
            client.player.networkHandler.sendPacket(StationEditPacket.configure(blockEntity.getPos(), false, station.name, null));
            client.setScreen(new StationScreen(blockEntity, station));
            for (Carriage carriage : train.carriages)
                carriage.updateConductors();
        }
    }

    private void tickTrainDisplay() {
        if (getImminent() == null) {
            displayedTrain = new WeakReference<>(null);
            quitAssembly.active = true;
            iconTypeScroll.active = iconTypeScroll.visible = false;
            toggleAssemblyButton.setToolTip(CreateLang.translateDirect("station.assemble_train"));
            toggleAssemblyButton.setIcon(AllGuiTextures.I_ASSEMBLE_TRAIN);
            toggleAssemblyButton.withCallback(() -> {
                client.player.networkHandler.sendPacket(StationEditPacket.tryAssemble(blockEntity.getPos()));
            });
        } else {
            client.player.networkHandler.sendPacket(StationEditPacket.configure(blockEntity.getPos(), false, station.name, null));
            client.setScreen(new StationScreen(blockEntity, station));
        }
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWindow(graphics, mouseX, mouseY, partialTicks);
        int x = guiLeft;
        int y = guiTop;

        MutableText header = CreateLang.translateDirect("station.assembly_title");
        graphics.drawText(textRenderer, header, x + background.getWidth() / 2 - textRenderer.getWidth(header) / 2, y + 4, 0xFF0E2233, false);

        AssemblyException lastAssemblyException = blockEntity.lastException;
        if (lastAssemblyException != null) {
            MutableText text = CreateLang.translateDirect("station.failed");
            graphics.drawText(textRenderer, text, x + 97 - textRenderer.getWidth(text) / 2, y + 47, 0xFF775B5B, false);
            int offset = 0;
            if (blockEntity.failedCarriageIndex != -1) {
                graphics.drawText(
                    textRenderer,
                    CreateLang.translateDirect("station.carriage_number", blockEntity.failedCarriageIndex),
                    x + 30,
                    y + 67,
                    0xFF7A7A7A,
                    false
                );
                offset += 10;
            }
            graphics.drawWrappedText(textRenderer, lastAssemblyException.component, x + 30, y + 67 + offset, 134, 0xFF775B5B, false);
            offset += textRenderer.wrapLines(lastAssemblyException.component, 134).size() * 9 + 5;
            graphics.drawWrappedText(textRenderer, CreateLang.translateDirect("station.retry"), x + 30, y + 67 + offset, 134, 0xFF7A7A7A, false);
            return;
        }

        int bogeyCount = blockEntity.bogeyCount;

        MutableText text = CreateLang.translateDirect(
            bogeyCount == 0 ? "station.no_bogeys" : bogeyCount == 1 ? "station.one_bogey" : "station.more_bogeys",
            bogeyCount
        );
        graphics.drawText(textRenderer, text, x + 97 - textRenderer.getWidth(text) / 2, y + 47, 0xFF7A7A7A, false);

        graphics.drawWrappedText(textRenderer, CreateLang.translateDirect("station.how_to"), x + 28, y + 62, 134, 0xFF7A7A7A, false);
        graphics.drawWrappedText(textRenderer, CreateLang.translateDirect("station.how_to_1"), x + 28, y + 94, 134, 0xFF7A7A7A, false);
        graphics.drawWrappedText(textRenderer, CreateLang.translateDirect("station.how_to_2"), x + 28, y + 117, 138, 0xFF7A7A7A, false);
    }

    @Override
    public void removed() {
        super.removed();
        Train train = displayedTrain.get();
        if (train != null) {
            Identifier iconId = iconTypes.get(iconTypeScroll.getState());
            train.icon = TrainIconType.byId(iconId);
            client.player.networkHandler.sendPacket(new TrainEditPacket(train.id, "", iconId, train.mapColorIndex));
        }
    }

    @Override
    protected PartialModel getFlag(float partialTicks) {
        return AllPartialModels.STATION_ASSEMBLE;
    }

}
