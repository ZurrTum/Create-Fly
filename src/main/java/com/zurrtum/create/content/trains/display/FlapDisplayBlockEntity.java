package com.zurrtum.create.content.trains.display;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.utility.DynamicComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlapDisplayBlockEntity extends KineticBlockEntity implements DisplayHolder {

    public List<FlapDisplayLayout> lines;
    public boolean isController;
    public boolean isRunning;
    public int xSize, ySize;
    public DyeColor[] colour;
    public boolean[] glowingLines;
    public boolean[] manualLines;
    private NbtCompound displayLink;

    public FlapDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.FLAP_DISPLAY, pos, state);
        setLazyTickRate(10);
        isController = false;
        xSize = 1;
        ySize = 1;
        colour = new DyeColor[2];
        manualLines = new boolean[2];
        glowingLines = new boolean[2];
    }

    @Override
    public NbtCompound getDisplayLinkData() {
        return displayLink;
    }

    @Override
    public void setDisplayLinkData(NbtCompound data) {
        displayLink = data;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        updateControllerStatus();
    }

    public void updateControllerStatus() {
        if (world.isClient)
            return;

        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof FlapDisplayBlock))
            return;

        Direction leftDirection = blockState.get(FlapDisplayBlock.HORIZONTAL_FACING).rotateYClockwise();
        boolean shouldBeController = !blockState.get(FlapDisplayBlock.UP) && world.getBlockState(pos.offset(leftDirection)) != blockState;

        int newXSize = 1;
        int newYSize = 1;

        if (shouldBeController) {
            for (int xOffset = 1; xOffset < 32; xOffset++) {
                if (world.getBlockState(pos.offset(leftDirection.getOpposite(), xOffset)) != blockState)
                    break;
                newXSize++;
            }
            for (int yOffset = 0; yOffset < 32; yOffset++) {
                if (!world.getBlockState(pos.offset(Direction.DOWN, yOffset)).get(FlapDisplayBlock.DOWN, false))
                    break;
                newYSize++;
            }
        }

        if (isController == shouldBeController && newXSize == xSize && newYSize == ySize)
            return;

        isController = shouldBeController;
        xSize = newXSize;
        ySize = newYSize;
        colour = Arrays.copyOf(colour, ySize * 2);
        glowingLines = Arrays.copyOf(glowingLines, ySize * 2);
        manualLines = new boolean[ySize * 2];
        lines = null;
        sendData();
    }

    @Override
    public void tick() {
        super.tick();
        isRunning = super.isSpeedRequirementFulfilled();
        if ((!world.isClient || !isRunning) && !isVirtual())
            return;
        int activeFlaps = 0;
        boolean instant = Math.abs(getSpeed()) > 128;
        for (FlapDisplayLayout line : lines)
            for (FlapDisplaySection section : line.getSections())
                activeFlaps += section.tick(instant, world.random);
        if (activeFlaps == 0)
            return;

        float volume = MathHelper.clamp(activeFlaps / 20f, 0.25f, 1.5f);
        float bgVolume = MathHelper.clamp(activeFlaps / 40f, 0.25f, 1f);
        BlockPos middle = pos.offset(getDirection().rotateYClockwise(), xSize / 2).offset(Direction.DOWN, ySize / 2);
        AllSoundEvents.SCROLL_VALUE.playAt(world, middle, volume, 0.56f, false);
        world.playSoundClient(
            middle.getX(),
            middle.getY(),
            middle.getZ(),
            SoundEvents.BLOCK_CALCITE_HIT,
            SoundCategory.BLOCKS,
            .35f * bgVolume,
            1.95f,
            false
        );
    }

    @Override
    public boolean isNoisy() {
        return false;
    }

    @Override
    public boolean isSpeedRequirementFulfilled() {
        return isRunning;
    }

    public void applyTextManually(int lineIndex, Text componentText) {
        List<FlapDisplayLayout> lines = getLines();
        if (lineIndex >= lines.size())
            return;

        FlapDisplayLayout layout = lines.get(lineIndex);
        if (!layout.isLayout("Default"))
            layout.loadDefault(getMaxCharCount());
        List<FlapDisplaySection> sections = layout.getSections();

        FlapDisplaySection flapDisplaySection = sections.getFirst();
        if (componentText == null) {
            manualLines[lineIndex] = false;
            flapDisplaySection.setText(ScreenTexts.EMPTY);
            notifyUpdate();
            return;
        }

        manualLines[lineIndex] = true;
        Text text = isVirtual() ? componentText : DynamicComponent.parseCustomText(world, pos, componentText);
        flapDisplaySection.setText(text);
        if (isVirtual())
            flapDisplaySection.refresh(true);
        else
            notifyUpdate();
    }

    public void setColour(int lineIndex, DyeColor color) {
        colour[lineIndex] = color == DyeColor.WHITE ? null : color;
        notifyUpdate();
    }

    public void setGlowing(int lineIndex) {
        glowingLines[lineIndex] = true;
        notifyUpdate();
    }

    public List<FlapDisplayLayout> getLines() {
        if (lines == null)
            initDefaultSections();
        return lines;
    }

    public void initDefaultSections() {
        lines = new ArrayList<>();
        for (int i = 0; i < ySize * 2; i++)
            lines.add(new FlapDisplayLayout(getMaxCharCount()));
    }

    public int getMaxCharCount() {
        return getMaxCharCount(0);
    }

    public int getMaxCharCount(int gaps) {
        return (int) ((xSize * 16f - 2f - 4f * gaps) / 3.5f);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        writeDisplayLink(view);

        view.putBoolean("Controller", isController);
        view.putInt("XSize", xSize);
        view.putInt("YSize", ySize);

        for (int j = 0; j < manualLines.length; j++)
            if (manualLines[j])
                view.putBoolean("CustomLine" + j, true);

        for (int j = 0; j < glowingLines.length; j++)
            if (glowingLines[j])
                view.putBoolean("GlowingLine" + j, true);

        for (int j = 0; j < colour.length; j++)
            if (colour[j] != null)
                view.put("Dye" + j, DyeColor.CODEC, colour[j]);

        List<FlapDisplayLayout> lines = getLines();
        for (int i = 0; i < lines.size(); i++)
            lines.get(i).write(view.get("Display" + i));
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        readDisplayLink(view);
        boolean wasActive = isController;
        int prevX = xSize;
        int prevY = ySize;

        isController = view.getBoolean("Controller", false);
        xSize = view.getInt("XSize", 0);
        ySize = view.getInt("YSize", 0);

        manualLines = new boolean[ySize * 2];
        for (int i = 0; i < ySize * 2; i++)
            manualLines[i] = view.getBoolean("CustomLine" + i, false);

        glowingLines = new boolean[ySize * 2];
        for (int i = 0; i < ySize * 2; i++)
            glowingLines[i] = view.getBoolean("GlowingLine" + i, false);

        colour = new DyeColor[ySize * 2];
        for (int i = 0; i < ySize * 2; i++)
            colour[i] = view.read("Dye" + i, DyeColor.CODEC).orElse(null);

        if (clientPacket && wasActive != isController || prevX != xSize || prevY != ySize) {
            invalidateRenderBoundingBox();
            lines = null;
        }

        List<FlapDisplayLayout> lines = getLines();
        for (int i = 0; i < lines.size(); i++)
            lines.get(i).read(view.getReadView("Display" + i));
    }

    public int getLineIndexAt(double yCoord) {
        return (int) MathHelper.clamp(Math.floor(2 * (pos.getY() - yCoord + 1)), 0, ySize * 2);
    }

    public FlapDisplayBlockEntity getController() {
        if (isController)
            return this;

        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof FlapDisplayBlock))
            return null;

        BlockPos.Mutable pos = getPos().mutableCopy();
        Direction side = blockState.get(FlapDisplayBlock.HORIZONTAL_FACING).rotateYClockwise();

        for (int i = 0; i < 64; i++) {
            BlockState other = world.getBlockState(pos);

            if (other.get(FlapDisplayBlock.UP, false)) {
                pos.move(Direction.UP);
                continue;
            }

            if (!world.getBlockState(pos.offset(side)).get(FlapDisplayBlock.UP, true)) {
                pos.move(side);
                continue;
            }

            BlockEntity found = world.getBlockEntity(pos);
            if (found instanceof FlapDisplayBlockEntity flap && flap.isController)
                return flap;

            break;
        }

        return null;
    }

    @Override
    protected Box createRenderBoundingBox() {
        Box aabb = new Box(pos);
        if (!isController)
            return aabb;
        Vec3i normal = getDirection().rotateYClockwise().getVector();
        return aabb.stretch(normal.getX() * xSize, -ySize, normal.getZ() * xSize);
    }

    public Direction getDirection() {
        return getCachedState().get(FlapDisplayBlock.HORIZONTAL_FACING, Direction.SOUTH).getOpposite();
    }

    public boolean isLineGlowing(int line) {
        return glowingLines[line];
    }

}
