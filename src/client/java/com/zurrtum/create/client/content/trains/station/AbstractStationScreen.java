package com.zurrtum.create.client.content.trains.station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllTrainIcons;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiPartialRenderBuilder;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.content.trains.entity.TrainIcon;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class AbstractStationScreen extends AbstractSimiScreen {

    protected AllGuiTextures background;
    protected StationBlockEntity blockEntity;
    protected GlobalStation station;
    private ElementWidget renderedItem;
    private ElementWidget renderedFlag;

    protected WeakReference<Train> displayedTrain;

    private IconButton confirmButton;

    public AbstractStationScreen(StationBlockEntity be, GlobalStation station) {
        super(be.getBlockState().getBlock().getName());
        this.blockEntity = be;
        this.station = station;
        displayedTrain = new WeakReference<>(null);
    }

    @Override
    protected void init() {
        //TODO
        //        if (blockEntity.computerBehaviour.hasAttachedComputer())
        //            minecraft.setScreen(new ComputerScreen(
        //                title,
        //                () -> Component.literal(station.name),
        //                this::renderAdditional,
        //                this,
        //                blockEntity.computerBehaviour::hasAttachedComputer
        //            ));

        setWindowSize(background.getWidth(), background.getHeight());
        super.init();
        clearWidgets();

        int x = guiLeft;
        int y = guiTop;

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        renderedFlag = new ElementWidget(x + background.getWidth() + 25, y + background.getHeight() - 62).showingElement(GuiGameElement.partial()
            .scale(2.5F).transform(this::transform).padding(13));
        addRenderableWidget(renderedFlag);

        renderedItem = new ElementWidget(
            x + background.getWidth() + 3,
            y + background.getHeight() - 46
        ).showingElement(GuiGameElement.of(blockEntity.getBlockState().setValue(BlockStateProperties.WATERLOGGED, false)).rotate(-22, 63, 0)
            .scale(2.5F).padding(17));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    public int getTrainIconWidth(Train train) {
        TrainIcon icon = AllTrainIcons.byType(train.icon);
        List<Carriage> carriages = train.carriages;

        int w = icon.getIconWidth(TrainIcon.ENGINE);
        if (carriages.size() == 1)
            return w;

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

    //TODO
    //    @Override
    //    public void tick() {
    //        super.tick();
    //        if (blockEntity.computerBehaviour.hasAttachedComputer())
    //            minecraft.setScreen(new ComputerScreen(
    //                title,
    //                () -> Component.literal(station.name),
    //                this::renderAdditional,
    //                this,
    //                blockEntity.computerBehaviour::hasAttachedComputer
    //            ));
    //    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        GuiPartialRenderBuilder flag = (GuiPartialRenderBuilder) renderedFlag.getRenderElement();
        if (blockEntity.resolveFlagAngle()) {
            flag.partial(getFlag(partialTicks)).tick(blockEntity.flag.settled() ? 1 : partialTicks);
        } else {
            flag.partial(null);
        }
    }

    private void transform(PoseStack ms, float partialTicks) {
        ms.scale(1, -1, 1);
        float value = blockEntity.flag.getValue(partialTicks);
        float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
        if (blockEntity.flag.getChaseTarget() > 0 && !blockEntity.flag.settled() && progress == 1) {
            float wiggleProgress = (value - .2f) / .8f;
            progress += (Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress);
        }

        TransformStack.of(ms).rotateXDegrees(24).rotateYDegrees(-210).translate(-0.12F, -0.81F, 0).rotateYDegrees(90)
            .rotateXDegrees(progress * 90 + 270);
    }

    protected abstract PartialModel getFlag(float partialTicks);

    protected Train getImminent() {
        return blockEntity.imminentTrain == null ? null : Create.RAILWAYS.trains.get(blockEntity.imminentTrain);
    }

    protected boolean trainPresent() {
        return blockEntity.trainPresent;
    }

}
