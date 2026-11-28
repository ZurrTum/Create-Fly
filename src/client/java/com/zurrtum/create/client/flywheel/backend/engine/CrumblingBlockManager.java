package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import com.zurrtum.create.client.flywheel.impl.visualization.storage.BlockEntityStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.RenderShape;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CrumblingBlockManager {
    private final BlockEntityStorage blockEntities;
    private final Map<GroupKey, CrumblingMap> map = new HashMap<>();
    private final Map<GroupKey, CrumblingMap> oitMap = new HashMap<>();
    private final AbstractTexture[] textures = new AbstractTexture[ModelBakery.DESTROY_STAGE_COUNT];
    private final AbstractTexture[] oitTextures = new AbstractTexture[ModelBakery.DESTROY_STAGE_COUNT];
    private final Deque<GroupKey> keys = new ArrayDeque<>();
    private final Deque<CrumblingMap> values = new ArrayDeque<>();
    private final Deque<CrumblingMap.Node> nodes = new ArrayDeque<>();
    private final Deque<Deque<AbstractDraw>> draws = new ArrayDeque<>();
    private GroupKey key = new GroupKey();
    private Deque<AbstractDraw> solidDraws = new ArrayDeque<>();
    private Deque<AbstractDraw> oitDraws = new ArrayDeque<>();
    private final Function<GroupKey, CrumblingMap> compute = createComputeFunc(textures);
    private final Function<GroupKey, CrumblingMap> computeOit = createComputeFunc(oitTextures);
    private final Consumer<@Nullable Instance> consumer = this::consume;
    private final Consumer<@Nullable Instance> oitConsumer = this::consumeOit;
    private @UnknownNullability DrawManager<? extends AbstractInstancer<?>> drawManager;
    private int crumblingKey, crumblingFlag;

    public CrumblingBlockManager(BlockEntityStorage blockEntities) {
        this.blockEntities = blockEntities;
    }

    public void prepare(DrawManager<? extends AbstractInstancer<?>> drawManager, RenderContext context) {
        List<BlockBreakingRenderState> blockBreakingRenderStates = context.levelRenderState().blockBreakingRenderStates;
        if (blockBreakingRenderStates.isEmpty()) {
            return;
        }
        this.drawManager = drawManager;
        if (context.useClassicTransparency()) {
            collectCrumblingInstances(blockBreakingRenderStates, consumer);
            if (hasCrumbling()) {
                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                prepareTextures(textureManager, map, ModelBakery.BREAKING_LOCATIONS, textures);
            }
        } else {
            collectCrumblingInstances(blockBreakingRenderStates, oitConsumer);
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            if (hasCrumbling()) {
                prepareTextures(textureManager, map, ModelBakery.BREAKING_LOCATIONS, textures);
            }
            if (hasOitCrumbling()) {
                prepareTextures(textureManager, oitMap, ModelBakery.BREAKING_LOCATIONS_OIT, oitTextures);
            }
        }
        this.drawManager = null;
    }

    private void collectCrumblingInstances(
        List<BlockBreakingRenderState> blockBreakingRenderStates,
        Consumer<@Nullable Instance> consumer
    ) {
        for (BlockBreakingRenderState state : blockBreakingRenderStates) {
            int key = state.progress();
            if (key < 0 || key >= ModelBakery.DESTROY_STAGE_COUNT) {
                continue;
            }
            if (state.blockState().getRenderShape() != RenderShape.MODEL) {
                continue;
            }
            BlockEntityVisual<?> visual = blockEntities.visualAtPos(state.blockPos().asLong());
            if (visual == null) {
                continue;
            }
            crumblingKey = key;
            crumblingFlag = 1 << key;
            visual.collectCrumblingInstances(consumer);
        }
    }

    private void prepareTextures(
        TextureManager textureManager,
        Map<GroupKey, CrumblingMap> map,
        List<Identifier> ids,
        AbstractTexture[] textures
    ) {
        Iterator<CrumblingMap> iterator = map.values().iterator();
        CrumblingMap current = iterator.next();
        int flag = current.flag();
        while (iterator.hasNext()) {
            current = iterator.next();
            flag |= current.flag();
        }
        assert flag != 0;
        do {
            int i = Integer.numberOfTrailingZeros(flag);
            textures[i] = textureManager.getTexture(ids.get(i));
            flag &= flag - 1;
        } while (flag != 0);
    }

    private Function<GroupKey, CrumblingMap> createComputeFunc(AbstractTexture[] textures) {
        return key -> {
            key.readonly();
            CrumblingMap cacheValue = values.pollFirst();
            return cacheValue != null ? cacheValue.update(textures) : new CrumblingMap(nodes, draws, textures);
        };
    }

    private void consume(@Nullable Instance instance) {
        if (instance == null || !(instance.handle() instanceof InstanceHandleImpl<?> impl)) {
            return;
        }
        AbstractInstancer<?> instancer = drawManager.cast(impl.state);
        if (instancer == null) {
            return;
        }
        List<? extends AbstractDraw> draws = instancer.draws();
        if (draws.isEmpty()) {
            return;
        }
        GroupKey key = this.key.update(instancer.type, instancer.environment);
        map.computeIfAbsent(key, compute).add(crumblingKey, crumblingFlag, draws, impl.index);
        if (key.isReadonly()) {
            GroupKey cacheKey = keys.pollFirst();
            this.key = cacheKey != null ? cacheKey : new GroupKey();
        }
    }

    private void consumeOit(@Nullable Instance instance) {
        if (instance == null || !(instance.handle() instanceof InstanceHandleImpl<?> impl)) {
            return;
        }
        AbstractInstancer<?> instancer = drawManager.cast(impl.state);
        if (instancer == null) {
            return;
        }
        List<? extends AbstractDraw> draws = instancer.draws();
        if (draws.isEmpty()) {
            return;
        }
        for (AbstractDraw draw : draws) {
            (draw.material().transparency() != Transparency.ORDER_INDEPENDENT ? solidDraws : oitDraws).addLast(draw);
        }
        GroupKey key = this.key.update(instancer.type, instancer.environment);
        int index = impl.index;
        if (!solidDraws.isEmpty()) {
            map.computeIfAbsent(key, compute).add(crumblingKey, crumblingFlag, solidDraws, index);
            Deque<AbstractDraw> cacheDraws = this.draws.pollFirst();
            solidDraws = cacheDraws != null ? cacheDraws : new ArrayDeque<>();
        }
        if (!oitDraws.isEmpty()) {
            oitMap.computeIfAbsent(key, computeOit).add(crumblingKey, crumblingFlag, oitDraws, index);
            Deque<AbstractDraw> cacheDraws = this.draws.pollFirst();
            oitDraws = cacheDraws != null ? cacheDraws : new ArrayDeque<>();
        }
        if (key.isReadonly()) {
            GroupKey cacheKey = keys.pollFirst();
            this.key = cacheKey != null ? cacheKey : new GroupKey();
        }
    }

    public void drain(BiConsumer<GroupKey, CrumblingMap> consumer) {
        drain(map, consumer);
    }

    public void drainOit(BiConsumer<GroupKey, CrumblingMap> consumer) {
        drain(oitMap, consumer);
    }

    private void drain(Map<GroupKey, CrumblingMap> map, BiConsumer<GroupKey, CrumblingMap> consumer) {
        Iterator<Map.Entry<GroupKey, CrumblingMap>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<GroupKey, CrumblingMap> entry = iterator.next();
            GroupKey key = entry.getKey();
            CrumblingMap value = entry.getValue();
            consumer.accept(key, value);
            iterator.remove();
            if (key.isReadonly()) {
                keys.addLast(key.mutable());
            }
            values.addLast(value);
        }
    }

    public void forEachOit(BiConsumer<GroupKey, CrumblingMap> consumer) {
        oitMap.forEach(consumer);
    }

    public boolean hasCrumbling() {
        return !map.isEmpty();
    }

    public boolean hasOitCrumbling() {
        return !oitMap.isEmpty();
    }
}
