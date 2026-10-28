package com.zurrtum.create.client.content.trains.station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllTrainIcons;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiBlockStateRenderBuilder;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiPartialRenderBuilder;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.compat.computercraft.ComputerScreen;
import com.zurrtum.create.client.compat.computercraft.ComputerScreen.AdditionalRenderer;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.trains.entity.TrainIcon;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class AbstractStationScreen extends AbstractSimiScreen implements AdditionalRenderer {

    protected AllGuiTextures background;
    protected StationBlockEntity blockEntity;
    protected GlobalStation station;
    private GuiBlockStateRenderBuilder renderedItem;
    private GuiPartialRenderBuilder renderedFlag;

    protected WeakReference<@Nullable Train> displayedTrain;

    private IconButton confirmButton;

    public AbstractStationScreen(StationBlockEntity be, GlobalStation station) {
        super(be.getBlockState().getBlock().getName());
        blockEntity = be;
        this.station = station;
        displayedTrain = new WeakReference<>(null);
    }

    @Override
    protected void init() {
        renderedFlag = GuiGameElement.partial().scale(2.5F).transform(this::transform).padding(13);
        renderedItem = GuiGameElement.of(blockEntity.getBlockState().setValue(BlockStateProperties.WATERLOGGED, false))
            .rotate(-22, 63, 0).scale(2.5F).padding(17);
        AbstractComputerBehaviour computer = blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE);
        if (computer != null && computer.hasAttachedComputer()) {
            minecraft.gui.setScreen(new ComputerScreen(
                title,
                () -> Component.literal(station.name),
                this,
                this,
                computer::hasAttachedComputer
            ));
        }

        setWindowSize(background.getWidth(), background.getHeight());
        super.init();
        clearWidgets();

        int x = guiLeft;
        int y = guiTop;

        confirmButton = new IconButton(
            x + background.getWidth() - 33,
            y + background.getHeight() - 24,
            AllIcons.I_CONFIRM
        );
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);
        addAdditional(this, x, y, background);
    }

    @Override
    public void addAdditional(Screen screen, int x, int y, AllGuiTextures background) {
        screen.addRenderableWidget(new ElementWidget(
            x + background.getWidth() + 25,
            y + background.getHeight() - 62
        ).showingElement(renderedFlag));
        screen.addRenderableWidget(new ElementWidget(
            x + background.getWidth() + 3,
            y + background.getHeight() - 46
        ).showingElement(renderedItem));
    }

    @Override
    public void updateAdditional(float partialTicks) {
        if (blockEntity.resolveFlagAngle()) {
            renderedFlag.partial(getFlag(partialTicks)).tick(blockEntity.flag.settled() ? 1 : partialTicks);
        } else {
            renderedFlag.partial(null);
        }
    }

    @Override
    public void removed() {
        renderedFlag.clear();
        renderedItem.clear();
    }

    public int getTrainIconWidth(Train train) {
        TrainIcon icon = AllTrainIcons.byType(train.icon);
        List<Carriage> carriages = train.carriages;

        int w = icon.getIconWidth(TrainIcon.ENGINE);
        if (carriages.size() == 1) {
            return w;
        }

        for (int i = 1; i < carriages.size(); i++) {
            if (i == carriages.size() - 1 && train.doubleEnded) {
                w += icon.getIconWidth(TrainIcon.FLIPPED_ENGINE) + 1;
                break;
            }
            Carriage carriage = carriages.get(i);
            w += icon.getIconWidth(carriage.bogeySpacing) + 1;
        }

        return w;
    }

    @Override
    public void tick() {
        super.tick();
        AbstractComputerBehaviour computer = blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE);
        if (computer != null && computer.hasAttachedComputer()) {
            minecraft.gui.setScreen(new ComputerScreen(
                title,
                () -> Component.literal(station.name),
                this,
                this,
                computer::hasAttachedComputer
            ));
        }
    }

    @Override
    protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        updateAdditional(partialTicks);
    }

    private void transform(PoseStack ms, float partialTicks) {
        Pose pose = ms.last();
        pose.scale(1, -1, 1);
        float value = blockEntity.flag.getValue(partialTicks);
        float progress = (float) Math.pow(Math.min(value * 5, 1), 2);
        if (blockEntity.flag.getChaseTarget() > 0 && !blockEntity.flag.settled() && progress == 1) {
            float wiggleProgress = (value - 0.2f) / 0.8f;
            progress += Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8.0f / Math.max(1, 8.0f * wiggleProgress);
        }

        pose.rotate(Axis.XP, Radians.DEG_24);
        pose.rotate(Axis.YP, Radians.DEG_150);
        pose.translate(-0.12F, -0.81F, 0);
        pose.rotate(Axis.YP, Radians.DEG_90);
        pose.rotate(Axis.XP, progress * Radians.DEG_90 + Radians.DEG_270);
    }

    protected abstract PartialModel getFlag(float partialTicks);

    @Nullable
    protected Train getImminent() {
        return blockEntity.imminentTrain == null ? null : Create.RAILWAYS.trains.get(blockEntity.imminentTrain);
    }

    protected boolean trainPresent() {
        return blockEntity.trainPresent;
    }

}
