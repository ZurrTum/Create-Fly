package com.zurrtum.create.client.ponder.foundation.ui;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.NavigatableSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.element.BoxElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiItemRenderBuilder;
import com.zurrtum.create.client.catnip.gui.widget.BoxWidget;
import com.zurrtum.create.client.catnip.lang.ClientFontHelper;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry.SceneOrderingEntry;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry.SceneOrderingType;
import com.zurrtum.create.client.ponder.enums.PonderConfig;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import com.zurrtum.create.client.ponder.foundation.*;
import com.zurrtum.create.client.ponder.foundation.PonderScene.SceneTransform;
import com.zurrtum.create.client.ponder.foundation.content.DebugScenes;
import com.zurrtum.create.client.ponder.foundation.element.TextWindowElement;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PonderUI extends AbstractPonderScreen {

    public static int ponderTicks;
    public static float ponderPartialTicksPaused;

    public static final Color BACKGROUND_TRANSPARENT = new Color(0xdd_000000, true);
    public static final Color BACKGROUND_FLAT = new Color(0xff_000000, true);
    public static final Color BACKGROUND_IMPORTANT = new Color(0xdd_0e0e20, true);

    public static final Couple<Color> COLOR_IDLE = Couple.create(new Color(0x40_ffeedd, true), new Color(0x20_ffeedd, true)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_HOVER = Couple.create(new Color(0x70_ffffff, true), new Color(0x30_ffffff, true))
        .map(Color::setImmutable);
    public static final Couple<Color> COLOR_HIGHLIGHT = Couple.create(new Color(0xf0_ffeedd, true), new Color(0x60_ffeedd, true))
        .map(Color::setImmutable);
    public static final Couple<Color> MISSING_VANILLA_ENTRY = Couple.create(new Color(0x50_5000ff, true), new Color(0x50_28007f, true))
        .map(Color::setImmutable);
    public static final Couple<Color> MISSING_MODDED_ENTRY = Couple.create(new Color(0x70_984500, true), new Color(0x70_692400, true))
        .map(Color::setImmutable);

    private final List<PonderScene> scenes;
    private final List<PonderTag> tags;
    private List<PonderButton> tagButtons = new ArrayList<>();
    private List<LerpedFloat> tagFades = new ArrayList<>();
    private final LerpedFloat fadeIn;
    ItemStack stack;
    GuiItemRenderBuilder itemRender;
    @Nullable PonderChapter chapter = null;

    private boolean userViewMode;
    private boolean identifyMode;
    private ItemStack hoveredTooltipItem = ItemStack.EMPTY;
    @Nullable
    private BlockPos hoveredBlockPos;

    private final Clipboard clipboardHelper;
    @Nullable
    private BlockPos copiedBlockPos;

    private final LerpedFloat finishingFlash;
    private final LerpedFloat nextUp;
    private int finishingFlashWarmup = 0;
    private int nextUpWarmup = 0;

    private final LerpedFloat lazyIndex;
    private int index = 0;
    @Nullable
    private PonderTag referredToByTag;

    private PonderButton left, right, scan, chap, userMode, close, replay, slowMode;
    private int skipCooling = 0;

    private int extendedTickLength = 0;
    private int extendedTickTimer = 0;

    public static PonderUI of(Identifier id) {
        return new PonderUI(PonderIndex.getSceneAccess().compile(id));
    }

    public static PonderUI of(ItemStack item) {
        return new PonderUI(PonderIndex.getSceneAccess().compile(RegisteredObjectsHelper.getKeyOrThrow(item.getItem())));
    }

    public static PonderUI of(ItemStack item, PonderTag tag) {
        PonderUI ponderUI = new PonderUI(PonderIndex.getSceneAccess().compile(RegisteredObjectsHelper.getKeyOrThrow(item.getItem())));
        ponderUI.referredToByTag = tag;
        return ponderUI;
    }

    protected PonderUI(List<PonderScene> scenes) {
        Identifier location = scenes.get(0).getLocation();
        stack = new ItemStack(RegisteredObjectsHelper.getItemOrBlock(location));
        itemRender = GuiGameElement.of(stack).scale(2).at(-35, 1);

        tags = new ArrayList<>(PonderIndex.getTagAccess().getTags(location));

        Ponder.LOGGER.debug("Ponder Scenes before ordering: {}", Arrays.toString(scenes.stream().map(PonderScene::getId).toArray()));

        List<PonderScene> orderedScenes;
        try {
            orderedScenes = orderScenes(scenes);
            Ponder.LOGGER.debug("Ponder Scenes after ordering: {}", Arrays.toString(orderedScenes.stream().map(PonderScene::getId).toArray()));
        } catch (Exception e) {
            Ponder.LOGGER.warn("Unable to sort PonderScenes, using unordered List", e);
            orderedScenes = scenes;
        }
        this.scenes = orderedScenes;

        if (this.scenes.isEmpty()) {
            List<StoryBoardEntry> list = Collections.singletonList(new PonderStoryBoardEntry(
                DebugScenes::empty,
                Ponder.MOD_ID,
                "debug/scene_1",
                Identifier.of("stick")
            ));
            this.scenes.addAll(PonderIndex.getSceneAccess().compile(list));
        }
        lazyIndex = LerpedFloat.linear().startWithValue(index);
        fadeIn = LerpedFloat.linear().startWithValue(0).chase(1, .1f, Chaser.EXP);
        clipboardHelper = new Clipboard();
        finishingFlash = LerpedFloat.linear().startWithValue(0).chase(0, .1f, Chaser.EXP);
        nextUp = LerpedFloat.linear().startWithValue(0).chase(0, .4f, Chaser.EXP);
    }

    @SuppressWarnings("UnstableApiUsage")
    private List<PonderScene> orderScenes(List<PonderScene> scenes) {
        Map<Boolean, List<PonderScene>> partitioned = scenes.stream()
            .collect(Collectors.partitioningBy(scene -> scene.getOrderingEntries().isEmpty()));

        List<PonderScene> scenesWithOrdering = partitioned.get(false);
        List<PonderScene> scenesWithoutOrdering = partitioned.get(true);

        if (scenesWithOrdering.isEmpty())
            return scenes;

        List<PonderScene> sceneList = new ArrayList<>(scenes);
        Collections.reverse(sceneList);

        Map<Identifier, PonderScene> sceneLookup = scenes.stream().collect(Collectors.toMap(PonderScene::getId, scene -> scene));

        MutableGraph<PonderScene> graph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
        sceneList.forEach(graph::addNode);

        IntStream.range(1, scenesWithoutOrdering.size()).forEach(i -> graph.putEdge(scenesWithoutOrdering.get(i - 1), scenesWithoutOrdering.get(i)));

        scenesWithOrdering.forEach(scene -> {
            List<SceneOrderingEntry> relevantOrderings = scene.getOrderingEntries().stream()
                .filter(entry -> scenes.stream().anyMatch(sc -> sc.getId().equals(entry.sceneId()))).toList();

            if (relevantOrderings.isEmpty())
                return;

            relevantOrderings.forEach(entry -> {
                PonderScene otherScene = sceneLookup.get(entry.sceneId());
                if (entry.type() == SceneOrderingType.BEFORE) {
                    graph.putEdge(scene, otherScene);
                } else if (entry.type() == SceneOrderingType.AFTER) {
                    graph.putEdge(otherScene, scene);
                }
            });
        });

        return topologicalSort(graph);

		/*sceneList.sort((scene1, scene2) -> {
			boolean hasOrderings1 = !scene1.getOrderingEntries().isEmpty();
			boolean hasOrderings2 = !scene2.getOrderingEntries().isEmpty();

			if (!hasOrderings1 && !hasOrderings2)
				return 0;

			Map<SceneOrderingType, Long> relevantOrderings1 = scene1.getOrderingEntries()
					.stream()
					.filter(entry -> entry.sceneId().equals(scene2.getId()))
					.collect(Collectors.groupingBy(SceneOrderingEntry::type, Collectors.counting()));

			Map<SceneOrderingType, Long> relevantOrderings2 = scene2.getOrderingEntries()
					.stream()
					.filter(entry -> entry.sceneId().equals(scene1.getId()))
					.collect(Collectors.groupingBy(SceneOrderingEntry::type, Collectors.counting()));

			// both scenes don't want to be ordered compared to each other
			if (relevantOrderings1.isEmpty() && relevantOrderings2.isEmpty())
				return 0;

			// only scene2 wants to be ordered either before or after scene1
			if (relevantOrderings1.isEmpty())
				return relevantOrderings2.containsKey(SceneOrderingType.AFTER) ? -1 : 1;

			// only scene1 wants to be ordered either before or after scene2
			if (relevantOrderings2.isEmpty())
				return relevantOrderings1.containsKey(SceneOrderingType.AFTER) ? 1 : -1;

			// both scenes want scene1 to be ordered after scene2
			if (relevantOrderings1.containsKey(SceneOrderingType.AFTER) && relevantOrderings2.containsKey(SceneOrderingType.BEFORE))
				return 1;

			// both scenes want scene1 to be ordered before scene2
			if (relevantOrderings1.containsKey(SceneOrderingType.BEFORE) && relevantOrderings2.containsKey(SceneOrderingType.AFTER))
				return -1;

			// everything else is contradictory so we ignore it
			return 0;
		});

		return sceneList;*/
    }

    private static List<PonderScene> topologicalSort(MutableGraph<PonderScene> graph) {
        List<PonderScene> result = new ArrayList<>();
        Set<PonderScene> visited = new HashSet<>();
        Set<PonderScene> currentlyVisiting = new HashSet<>();

        for (PonderScene node : graph.nodes()) {
            if (!visited.contains(node)) {
                if (!dfs(node, graph, visited, currentlyVisiting, result)) {
                    throw new IllegalArgumentException("Graph has a cycle!");
                }
            }
        }

        Collections.reverse(result);
        return result;
    }

    private static boolean dfs(
        PonderScene node,
        MutableGraph<PonderScene> graph,
        Set<PonderScene> visited,
        Set<PonderScene> currentlyVisiting,
        List<PonderScene> result
    ) {
        if (currentlyVisiting.contains(node)) {
            return false; // Detected a cycle
        }

        if (!visited.contains(node)) {
            currentlyVisiting.add(node);
            for (PonderScene neighbor : graph.successors(node)) {
                if (!dfs(neighbor, graph, visited, currentlyVisiting, result)) {
                    return false; // Detected a cycle
                }
            }
            currentlyVisiting.remove(node);
            visited.add(node);
            result.add(node);
        }

        return true;
    }

    @Override
    protected void init() {
        super.init();

        tagButtons = new ArrayList<>();
        tagFades = new ArrayList<>();

        tags.forEach(t -> {
            int i = tagButtons.size();
            int x = 31;
            int y = 81 + i * 30;

            PonderButton b2 = new PonderButton(x, y).showing(t).withCallback((mX, mY) -> {
                centerScalingOn(mX, mY);
                ScreenOpener.transitionTo(new PonderTagScreen(t));
            });

            addDrawableChild(b2);
            tagButtons.add(b2);

            LerpedFloat chase = LerpedFloat.linear().startWithValue(0).chase(0, .05f, Chaser.exp(.1));
            tagFades.add(chase);

        });

        /*
         * if (chapter != null) { widgets.add(chap = new PonderButton(width - 31 - 24,
         * 31, () -> { }).showing(chapter)); }
         */

        GameOptions bindings = client.options;
        int spacing = 8;
        int bX = (width - 20) / 2 - (70 + 2 * spacing);
        int bY = height - 20 - 31;

        {
            int pX = (width / 2) - 110;
            int pY = bY + 20 + 4;
            int pW = width - 2 * pX;
            addDrawableChild(new PonderProgressBar(this, pX, pY, pW, 1));
        }

        addDrawableChild(scan = new PonderButton(bX, bY).withShortcut(bindings.dropKey).showing(PonderGuiTextures.ICON_PONDER_IDENTIFY)
            .enableFade(0, 5).withCallback(() -> {
                identifyMode = !identifyMode;
                if (!identifyMode)
                    scenes.get(index).deselect();
                else
                    ponderPartialTicksPaused = AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());
            }));
        scan.atZLevel(600);

        addDrawableChild(slowMode = new PonderButton(width - 20 - 31, bY).showing(PonderGuiTextures.ICON_PONDER_SLOW_MODE).enableFade(0, 5)
            .withCallback(() -> setComfyReadingEnabled(!isComfyReadingEnabled())));

        if (PonderIndex.editingModeActive()) {
            addDrawableChild(userMode = new PonderButton(width - 50 - 31, bY).showing(PonderGuiTextures.ICON_PONDER_USER_MODE).enableFade(0, 5)
                .withCallback(() -> userViewMode = !userViewMode));
        }

        bX += 50 + spacing;
        addDrawableChild(left = new PonderButton(bX, bY).withShortcut(bindings.leftKey).showing(PonderGuiTextures.ICON_PONDER_LEFT).enableFade(0, 5)
            .withCallback(() -> this.scroll(false)));

        bX += 20 + spacing;
        addDrawableChild(close = new PonderButton(bX, bY).withShortcut(bindings.inventoryKey).showing(PonderGuiTextures.ICON_PONDER_CLOSE)
            .enableFade(0, 5).withCallback(this::close));

        bX += 20 + spacing;
        addDrawableChild(right = new PonderButton(bX, bY).withShortcut(bindings.rightKey).showing(PonderGuiTextures.ICON_PONDER_RIGHT)
            .enableFade(0, 5).withCallback(() -> this.scroll(true)));

        bX += 50 + spacing;
        addDrawableChild(replay = new PonderButton(bX, bY).withShortcut(bindings.backKey).showing(PonderGuiTextures.ICON_PONDER_REPLAY)
            .enableFade(0, 5).withCallback(this::replay));
    }

    @Override
    protected void initBackTrackIcon(BoxWidget backTrack) {
        backTrack.showingElement(GuiGameElement.of(stack).scale(1.5f).at(-4, -4));
    }

    @Override
    public void tick() {
        super.tick();

        if (skipCooling > 0)
            skipCooling--;

        if (referredToByTag != null) {
            for (int i = 0; i < scenes.size(); i++) {
                PonderScene ponderScene = scenes.get(i);
                if (!ponderScene.getTags().contains(referredToByTag))
                    continue;
                if (i == index)
                    break;
                scenes.get(index).fadeOut();
                index = i;
                scenes.get(index).begin();
                lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
                identifyMode = false;
                break;
            }
            referredToByTag = null;
        }

        lazyIndex.tickChaser();
        fadeIn.tickChaser();
        finishingFlash.tickChaser();
        nextUp.tickChaser();
        PonderScene activeScene = scenes.get(index);

        extendedTickLength = 0;
        if (isComfyReadingEnabled())
            activeScene.forEachVisible(TextWindowElement.class, twe -> extendedTickLength = 2);

        if (extendedTickTimer == 0) {
            if (!identifyMode) {
                ponderTicks++;
                if (skipCooling == 0)
                    activeScene.tick();
            }

            if (!identifyMode) {
                float lazyIndexValue = lazyIndex.getValue();
                if (Math.abs(lazyIndexValue - index) > 1 / 512f)
                    scenes.get(lazyIndexValue < index ? index - 1 : index + 1).tick();
            }
            extendedTickTimer = extendedTickLength;
        } else
            extendedTickTimer--;

        if (activeScene.getCurrentTime() == activeScene.getTotalTime() - 1) {
            finishingFlashWarmup = 30;
            nextUpWarmup = 50;
        }

        if (finishingFlashWarmup > 0) {
            finishingFlashWarmup--;
            if (finishingFlashWarmup == 0) {
                finishingFlash.setValue(1);
                finishingFlash.setValue(1);
            }
        }

        if (nextUpWarmup > 0) {
            nextUpWarmup--;
            if (nextUpWarmup == 0)
                nextUp.updateChaseTarget(1);
        }

        updateIdentifiedItem(activeScene);
    }

    public PonderScene getActiveScene() {
        return scenes.get(index);
    }

    public void seekToTime(int time) {
        if (getActiveScene().getCurrentTime() > time)
            replay();

        getActiveScene().seekToTime(time);
        if (time != 0)
            coolDownAfterSkip();
    }

    public void updateIdentifiedItem(PonderScene activeScene) {
        hoveredTooltipItem = ItemStack.EMPTY;
        hoveredBlockPos = null;
        if (!identifyMode)
            return;

        Window w = client.getWindow();
        double mouseX = client.mouse.getX() * w.getScaledWidth() / w.getWidth();
        double mouseY = client.mouse.getY() * w.getScaledHeight() / w.getHeight();
        SceneTransform t = activeScene.getTransform();
        Vec3d vec1 = t.screenToScene(mouseX, mouseY, 1000, 0);
        Vec3d vec2 = t.screenToScene(mouseX, mouseY, -100, 0);
        Pair<ItemStack, BlockPos> pair = activeScene.rayTraceScene(vec1, vec2);
        hoveredTooltipItem = pair.getFirst();
        hoveredBlockPos = pair.getSecond();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scroll(scrollY > 0))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    protected void replay() {
        identifyMode = false;
        PonderScene scene = scenes.get(index);

        if (hasShiftDown()) {
            PonderIndex.reload();
            this.scenes.clear();
            this.scenes.addAll(PonderIndex.getSceneAccess().compile(scene.getLocation()));


			/*PonderScene finalScene = scene;
			List<PonderStoryBoardEntry> list = PonderIndex.getSceneAccess().getRegisteredEntries().stream().filter(
					entry -> entry.getKey() == finalScene.getLocation()).map(Map.Entry::getValue).toList();
			PonderStoryBoardEntry sb = list.get(index);
			StructureTemplate activeTemplate = PonderSceneRegistry.loadSchematic(sb.getSchematicLocation());
			PonderLevel world = new PonderLevel(BlockPos.ZERO, Minecraft.getInstance().level);
			activeTemplate.placeInWorld(world, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings(),
										RandomSource.create(), Block.UPDATE_CLIENTS);
			world.createBackup();
			scene = PonderSceneRegistry.compileScene(scene.localization, sb, world);
			scene.begin();
			this.scenes.set(index, scene);*/
        }

        scene.begin();
    }

    protected boolean scroll(boolean forward) {
        int prevIndex = index;
        index = forward ? index + 1 : index - 1;
        index = MathHelper.clamp(index, 0, scenes.size() - 1);
        if (prevIndex != index) {// && Math.abs(index - lazyIndex.getValue()) < 1.5f) {
            scenes.get(prevIndex).fadeOut();
            scenes.get(index).begin();
            lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
            identifyMode = false;
            return true;
        } else
            index = prevIndex;
        return false;
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWindow(graphics, mouseX, mouseY, partialTicks);
        partialTicks = getPartialTicks();
        renderVisibleScenes(graphics, mouseX, mouseY, skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
        renderWidgets(graphics, mouseX, mouseY, identifyMode ? ponderPartialTicksPaused : partialTicks);
    }

    protected void renderVisibleScenes(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        Window window = graphics.client.getWindow();
        float uiTicks = lazyIndex.getValue(AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter()));// TODO - Checkover
        renderScene(graphics, 0, window, index, partialTicks, uiTicks);
        float lazyIndexValue = lazyIndex.getValue(partialTicks);
        if (Math.abs(lazyIndexValue - index) > 1 / 512f)
            renderScene(graphics, 1, window, lazyIndexValue < index ? index - 1 : index + 1, partialTicks, uiTicks);
    }

    protected void renderScene(DrawContext graphics, int id, Window window, int i, float partialTicks, float uiTicks) {
        double diff = i - uiTicks;
        double slide = MathHelper.lerp(diff * diff, 200, 600) * diff;
        PonderScene scene = scenes.get(i);
        graphics.state.addSpecialElement(new SceneRenderState(
            id,
            scene,
            width,
            height,
            slide,
            userViewMode,
            finishingFlash,
            partialTicks,
            window,
            new Matrix3x2f(graphics.getMatrices())
        ));
    }

    protected void renderWidgets(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        float fade = fadeIn.getValue(partialTicks);
        float lazyIndexValue = lazyIndex.getValue(partialTicks);
        float indexDiff = lazyIndexValue - index;
        PonderScene activeScene = scenes.get(index);
        PonderScene nextScene = scenes.size() > index + 1 ? scenes.get(index + 1) : null;

        boolean noWidgetsHovered = true;
        for (Element child : children())
            noWidgetsHovered &= !child.isMouseOver(mouseX, mouseY);

        int tooltipColor = UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB();
        renderSceneInformation(graphics, fade, indexDiff, activeScene, tooltipColor);

        Matrix3x2fStack ms = graphics.getMatrices();

        MinecraftClient mc = MinecraftClient.getInstance();
        if (identifyMode) {
            if (noWidgetsHovered && mouseY < height - 80) {
                if (hoveredTooltipItem.isEmpty()) {

                    MutableText text = Ponder.lang().translate(
                        AbstractPonderScreen.IDENTIFY_MODE,
                        ((MutableText) client.options.dropKey.getBoundKeyLocalizedText()).formatted(Formatting.WHITE)
                    ).style(Formatting.GRAY).component();

                    List<Text> tooltipLines = textRenderer.getTextHandler().wrapLines(text, width / 3, Style.EMPTY).stream()
                        .map(t -> (Text) Text.literal(t.getString())).toList();
                    graphics.drawTooltip(textRenderer, tooltipLines, mouseX, mouseY);
                } else
                    graphics.drawItemTooltip(textRenderer, hoveredTooltipItem, mouseX, mouseY);
                if (hoveredBlockPos != null && PonderIndex.editingModeActive() && !userViewMode) {
                    boolean copied = hoveredBlockPos.equals(copiedBlockPos);
                    MutableText coords = Text.literal(hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ())
                        .formatted(copied ? Formatting.GREEN : Formatting.GOLD);
                    graphics.drawTooltip(textRenderer, coords, 0, -15);
                }
            }
            scan.flash();
        } else {
            scan.dim();
        }

        if (PonderIndex.editingModeActive()) {
            if (userViewMode)
                userMode.flash();
            else
                userMode.dim();
        }

        if (isComfyReadingEnabled())
            slowMode.flash();
        else
            slowMode.dim();

        renderSceneOverlay(graphics, partialTicks, lazyIndexValue, Math.abs(indexDiff));

        renderNextUp(graphics, partialTicks, nextScene);

        // Widgets
        getRenderables().forEach(w -> {
            if (w instanceof PonderButton button) {
                button.fade().startWithValue(fade);
            }
        });

        if (index == 0 || index == 1 && lazyIndexValue < index)
            left.fade().startWithValue(lazyIndexValue);
        if (index == scenes.size() - 1 || index == scenes.size() - 2 && lazyIndexValue > index)
            right.fade().startWithValue(scenes.size() - lazyIndexValue - 1);

        if (activeScene.isFinished())
            right.flash();
        else {
            right.dim();
            nextUp.updateChaseTarget(0);
        }

        // Arrows behind the main widgets
        Color c1 = COLOR_NAV_ARROW.getFirst().setAlpha(0x40);
        Color c2 = COLOR_NAV_ARROW.getFirst().setAlpha(0x20);
        Color c3 = COLOR_NAV_ARROW.getFirst().setAlpha(0x10);
        UIRenderHelper.breadcrumbArrow(graphics, width / 2 - 20, height - 51, 20, 20, 5, c1, c2);
        UIRenderHelper.breadcrumbArrow(graphics, width / 2 + 20, height - 51, -20, 20, -5, c1, c2);
        UIRenderHelper.breadcrumbArrow(graphics, width / 2 - 90, height - 51, 70, 20, 5, c1, c3);
        UIRenderHelper.breadcrumbArrow(graphics, width / 2 + 90, height - 51, -70, 20, -5, c1, c3);

        // Tags
        List<PonderTag> sceneTags = activeScene.getTags();
        boolean highlightAll = sceneTags.stream().anyMatch(tag -> tag.getId() == PonderTag.Highlight.ALL);
        double s = mc.getWindow().getScaleFactor();
        IntStream.range(0, tagButtons.size()).forEach(i -> {
            ms.pushMatrix();
            PonderTag tag = this.tags.get(i);
            LerpedFloat chase = tagFades.get(i);
            PonderButton button = tagButtons.get(i);
            if (button.isMouseOver(mouseX, mouseY)) {
                chase.updateChaseTarget(1);
            } else
                chase.updateChaseTarget(0);

            chase.tickChaser();

            if (highlightAll || sceneTags.contains(tag))
                button.flash();
            else
                button.dim();

            int x = button.getX() + button.getWidth() + 4;
            int y = button.getY() - 2;
            ms.translate(x, y + 5 * (1 - fade));

            float fadedWidth = 200 * chase.getValue(partialTicks);
            UIRenderHelper.streak(graphics, 0, 0, 12, 26, (int) fadedWidth);

            //            graphics.enableScissor(x, 0, (int) fadedWidth, height);

            graphics.enableScissor(0, 8, (int) fadedWidth, 8 + height);
            String tagName = tag.getTitle();
            graphics.drawText(textRenderer, tagName, 3, 8, UIRenderHelper.COLOR_TEXT_ACCENT.getFirst().getRGB(), false);

            graphics.disableScissor();

            ms.popMatrix();
        });

        renderHoverTooltips(graphics, tooltipColor);
    }

    private void renderHoverTooltips(DrawContext graphics, int tooltipColor) {
        int tooltipY = height - 16;
        if (scan.isSelected())
            graphics.drawCenteredTextWithShadow(
                textRenderer,
                Ponder.lang().translate(AbstractPonderScreen.IDENTIFY).component(),
                scan.getX() + 10,
                tooltipY,
                tooltipColor
            );
        if (index != 0 && left.isSelected())
            graphics.drawCenteredTextWithShadow(
                textRenderer,
                Ponder.lang().translate(AbstractPonderScreen.PREVIOUS).component(),
                left.getX() + 10,
                tooltipY,
                tooltipColor
            );
        if (close.isSelected())
            graphics.drawCenteredTextWithShadow(
                textRenderer,
                Ponder.lang().translate(AbstractPonderScreen.CLOSE).component(),
                close.getX() + 10,
                tooltipY,
                tooltipColor
            );
        if (index != scenes.size() - 1 && right.isSelected())
            graphics.drawCenteredTextWithShadow(
                textRenderer,
                Ponder.lang().translate(AbstractPonderScreen.NEXT).component(),
                right.getX() + 10,
                tooltipY,
                tooltipColor
            );
        if (replay.isSelected())
            graphics.drawCenteredTextWithShadow(
                textRenderer,
                Ponder.lang().translate(AbstractPonderScreen.REPLAY).component(),
                replay.getX() + 10,
                tooltipY,
                tooltipColor
            );
        if (slowMode.isSelected())
            graphics.drawCenteredTextWithShadow(
                textRenderer,
                Ponder.lang().translate(AbstractPonderScreen.SLOW_TEXT).component(),
                slowMode.getX() + 5,
                tooltipY,
                tooltipColor
            );
        if (PonderIndex.editingModeActive() && userMode.isSelected())
            graphics.drawCenteredTextWithShadow(textRenderer, "Editor View", userMode.getX() + 10, tooltipY, tooltipColor);
    }

    private void renderNextUp(DrawContext graphics, float partialTicks, @Nullable PonderScene nextScene) {
        if (!getActiveScene().isFinished())
            return;

        if (nextScene == null || !nextScene.isNextUpEnabled())
            return;

        if (!(nextUp.getValue() > 1 / 16f))
            return;

        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(right.getX() + 10, right.getY() - 6 + nextUp.getValue(partialTicks) * 5);
        MutableText nextUpComponent = Ponder.lang().translate(AbstractPonderScreen.NEXT_UP).component();
        int boxWidth = (Math.max(textRenderer.getWidth(nextScene.getTitle()), textRenderer.getWidth(nextUpComponent)) + 5);
        renderSpeechBox(graphics, 0, 0, boxWidth, 20, right.isSelected(), Pointing.DOWN, false);
        poseStack.translate(0, -29);
        graphics.drawCenteredTextWithShadow(textRenderer, nextUpComponent, 0, 0, UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB());
        graphics.drawCenteredTextWithShadow(textRenderer, nextScene.getTitle(), 0, 10, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
        poseStack.popMatrix();
    }

    private void renderSceneOverlay(DrawContext graphics, float partialTicks, float lazyIndexValue, float indexDiff) {
        // Scene overlay
        float scenePT = skipCooling > 0 ? 0 : partialTicks;
        renderOverlay(graphics, index, scenePT);
        if (indexDiff > 1 / 512f)
            renderOverlay(graphics, lazyIndexValue < index ? index - 1 : index + 1, scenePT);
    }

    private void renderSceneInformation(DrawContext graphics, float fade, float indexDiff, PonderScene activeScene, int tooltipColor) {
        float absoluteIndexDiff = Math.abs(indexDiff);
        // info includes icon, scene title and the "Pondering about... " text

        int otherIndex = index;
        if (scenes.size() != 1 && absoluteIndexDiff >= 0.01) {
            float indexOffset = Math.signum(indexDiff);
            otherIndex = index + (int) indexOffset;
            if (otherIndex < 0 || otherIndex >= scenes.size()) {
                return; // should never be reached
            }
        }

        String title = activeScene.getTitle();
        String otherTitle = scenes.get(otherIndex).getTitle();

        int maxTitleWidth = 180;

        int titleWidth = textRenderer.getWidth(title);
        if (titleWidth > maxTitleWidth)
            titleWidth = maxTitleWidth;

        int otherTitleWidth = textRenderer.getWidth(otherTitle);
        if (otherTitleWidth > maxTitleWidth)
            otherTitleWidth = maxTitleWidth;

        int wrappedTitleHeight = textRenderer.getWrappedLinesHeight(title, maxTitleWidth);
        int otherWrappedTitleHeight = textRenderer.getWrappedLinesHeight(otherTitle, maxTitleWidth);

        // height is ideal for single line titles
        int streakHeight = 35 - 9 + MathHelper.lerp(absoluteIndexDiff, wrappedTitleHeight, otherWrappedTitleHeight);
        int streakWidth = 70 + MathHelper.lerp(absoluteIndexDiff, titleWidth, otherTitleWidth);

        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        // translate to top left of the background streak
        poseStack.translate(55, 19);

        // background streak
        UIRenderHelper.streak(graphics, 0, 0, streakHeight / 2, streakHeight, (int) (streakWidth * fade));
        UIRenderHelper.streak(graphics, 180, 0, streakHeight / 2, streakHeight, (int) (30 * fade));

        // icon
        new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT).gradientBorder(COLOR_IDLE).at(-34, 2, 100).withBounds(30, 30).render(graphics);

        itemRender.render(graphics);

        // pondering about text
        poseStack.translate(4, 6);
        graphics.drawText(textRenderer, Ponder.lang().translate(AbstractPonderScreen.PONDERING).component(), 0, 0, tooltipColor, false);

        // scene title
        poseStack.translate(0, 14);

        // short version for single scene views
        if (scenes.size() == 1 || absoluteIndexDiff < 0.01) {
            ClientFontHelper.drawSplitString(
                graphics,
                textRenderer,
                title,
                0,
                0,
                maxTitleWidth,
                UIRenderHelper.COLOR_TEXT.getFirst().scaleAlphaForText(fade).getRGB()
            );

            poseStack.popMatrix();
            return;
        }

        graphics.state.addSpecialElement(new TitleTextRenderState(new Matrix3x2f(poseStack), 0, 0, indexDiff, title, otherTitle));
        poseStack.popMatrix();
    }

    private void renderOverlay(DrawContext graphics, int i, float partialTicks) {
        if (identifyMode)
            return;
        Matrix3x2fStack matrices = graphics.getMatrices();
        matrices.pushMatrix();
        PonderScene story = scenes.get(i);
        story.renderOverlay(this, graphics, skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
        matrices.popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (identifyMode && hoveredBlockPos != null && PonderIndex.editingModeActive()) {
            long handle = client.getWindow().getHandle();
            if (copiedBlockPos != null && click.button() == 1) {
                clipboardHelper.setClipboard(
                    handle,
                    "util.select().fromTo(" + copiedBlockPos.getX() + ", " + copiedBlockPos.getY() + ", " + copiedBlockPos.getZ() + ", " + hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")"
                );
                copiedBlockPos = hoveredBlockPos;
                return true;
            }

            if (hasShiftDown())
                clipboardHelper.setClipboard(
                    handle,
                    "util.select().position(" + hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")"
                );
            else
                clipboardHelper.setClipboard(
                    handle,
                    "util.grid().at(" + hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")"
                );
            copiedBlockPos = hoveredBlockPos;
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    protected String getBreadcrumbTitle() {
        if (chapter != null)
            return chapter.getTitle();

        return stack.getItem().getName().getString();
    }

    public TextRenderer getFontRenderer() {
        return textRenderer;
    }

    protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
        boolean hovered = !(mouseX < x || mouseX > x + w);
        hovered &= !(mouseY < y || mouseY > y + h);
        return hovered;
    }

    @SuppressWarnings("DefaultNotLastCaseInSwitch")
    public static void renderSpeechBox(
        DrawContext graphics,
        int x,
        int y,
        int w,
        int h,
        boolean highlighted,
        Pointing pointing,
        boolean returnWithLocalTransform
    ) {
        Matrix3x2fStack poseStack = graphics.getMatrices();
        if (!returnWithLocalTransform) {
            poseStack.pushMatrix();
        }

        int boxX = x;
        int boxY = y;
        int divotX = x;
        int divotY = y;
        int divotRotation;
        int divotSize = 8;
        int distance = 1;
        int divotRadius = divotSize / 2;
        Couple<Color> borderColors = highlighted ? PonderButton.COLOR_HOVER : COLOR_IDLE;
        Color c;

        switch (pointing) {
            default:
            case DOWN:
                divotRotation = 0;
                boxX -= w / 2;
                boxY -= h + divotSize + 1 + distance;
                divotX -= divotRadius;
                divotY -= divotSize + distance;
                c = borderColors.getSecond();
                break;
            case LEFT:
                divotRotation = 90;
                boxX += divotSize + 1 + distance;
                boxY -= h / 2;
                divotX += distance;
                divotY -= divotRadius;
                c = Color.mixColors(borderColors, 0.5f);
                break;
            case RIGHT:
                divotRotation = 270;
                boxX -= w + divotSize + 1 + distance;
                boxY -= h / 2;
                divotX -= divotSize + distance;
                divotY -= divotRadius;
                c = Color.mixColors(borderColors, 0.5f);
                break;
            case UP:
                divotRotation = 180;
                boxX -= w / 2;
                boxY += divotSize + 1 + distance;
                divotX -= divotRadius;
                divotY += distance;
                c = borderColors.getFirst();
                break;
        }

        new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT).gradientBorder(borderColors).at(boxX, boxY, 100).withBounds(w, h).render(graphics);

        poseStack.pushMatrix();
        poseStack.translate(divotX + divotRadius, divotY + divotRadius);
        poseStack.rotate(divotRotation * (float) (Math.PI / 180.0));
        poseStack.translate(-divotRadius, -divotRadius);
        PonderGuiTextures.SPEECH_TOOLTIP_BACKGROUND.render(graphics, 0, 0);
        PonderGuiTextures.SPEECH_TOOLTIP_COLOR.render(graphics, 0, 0, c);
        poseStack.popMatrix();

        if (returnWithLocalTransform) {
            poseStack.translate(boxX, boxY);
            return;
        }

        poseStack.popMatrix();

    }

    public ItemStack getHoveredTooltipItem() {
        return hoveredTooltipItem;
    }

    public ItemStack getSubject() {
        return stack;
    }

    @Override
    public boolean isEquivalentTo(NavigatableSimiScreen other) {
        if (other instanceof PonderUI otherUI)
            return !otherUI.stack.isEmpty() && stack.isOf(otherUI.stack.getItem());
        return super.isEquivalentTo(other);
    }

    @Override
    public void shareContextWith(NavigatableSimiScreen other) {
        if (other instanceof PonderUI ponderUI) {
            ponderUI.referredToByTag = referredToByTag;
        }
    }

    public static float getPartialTicks() {
        MinecraftClient mc = MinecraftClient.getInstance();
        float renderPartialTicks = AnimationTickHolder.getPartialTicksUI(mc.getRenderTickCounter());

        if (mc.currentScreen instanceof PonderUI ui) {
            if (ui.identifyMode)
                return ponderPartialTicksPaused;

            return (renderPartialTicks + (ui.extendedTickLength - ui.extendedTickTimer)) / (ui.extendedTickLength + 1);
        }

        return renderPartialTicks;
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    public void coolDownAfterSkip() {
        skipCooling = 15;
    }

    @Override
    public void removed() {
        super.removed();
        hoveredTooltipItem = ItemStack.EMPTY;
        itemRender.clear();
        for (PonderTag tag : tags) {
            tag.clear();
        }
    }

    public boolean isComfyReadingEnabled() {
        return PonderConfig.client().comfyReading.get();
    }

    public void setComfyReadingEnabled(boolean slowTextMode) {
        PonderConfig.client().comfyReading.set(slowTextMode);
    }

}