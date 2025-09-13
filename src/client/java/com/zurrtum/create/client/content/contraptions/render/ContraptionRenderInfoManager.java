package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.contraptions.Contraption;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ContraptionRenderInfoManager {
    static final WorldAttached<ContraptionRenderInfoManager> MANAGERS = new WorldAttached<>(ContraptionRenderInfoManager::new);

    private final World level;
    private final Int2ObjectMap<ContraptionRenderInfo> renderInfos = new Int2ObjectOpenHashMap<>();
    private int removalTimer;

    private ContraptionRenderInfoManager(WorldAccess level) {
        this.level = (World) level;
    }

    public static void tickFor(MinecraftClient mc) {
        if (mc.isPaused())
            return;

        MANAGERS.get(mc.world).tick();
    }

    public static void resetAll() {
        MANAGERS.empty(ContraptionRenderInfoManager::delete);
    }

    public static void onReloadLevelRenderer() {
        resetAll();
    }

    ContraptionRenderInfo getRenderInfo(Contraption contraption) {
        int entityId = contraption.entity.getId();
        ContraptionRenderInfo renderInfo = renderInfos.get(entityId);

        if (renderInfo == null) {
            renderInfo = new ContraptionRenderInfo(level, contraption);
            renderInfos.put(entityId, renderInfo);
        }

        return renderInfo;
    }

    boolean invalidate(Contraption contraption) {
        int entityId = contraption.entity.getId();
        ContraptionRenderInfo renderInfo = renderInfos.remove(entityId);

        if (renderInfo != null) {
            renderInfo.invalidate();
            return true;
        }

        return false;
    }

    private void tick() {
        if (removalTimer >= 20) {
            renderInfos.values().removeIf(ContraptionRenderInfo::isDead);
            removalTimer = 0;
        }
        removalTimer++;
    }

    private void delete() {
        for (ContraptionRenderInfo renderer : renderInfos.values()) {
            renderer.invalidate();
        }
        renderInfos.clear();
    }
}
