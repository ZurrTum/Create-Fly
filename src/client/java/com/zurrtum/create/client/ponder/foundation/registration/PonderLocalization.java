package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.ponder.api.registration.LangRegistryAccess;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class PonderLocalization implements LangRegistryAccess {
    public static final String LANG_PREFIX = "ponder.";
    public static final String UI_PREFIX = "ui.";

    public final Map<ResourceLocation, String> shared = new HashMap<>();
    public final Map<ResourceLocation, Couple<String>> tag = new HashMap<>();
    public final Map<ResourceLocation, Map<String, String>> specific = new HashMap<>();

    //

    public void clearAll() {
        shared.clear();
        tag.clear();
        specific.clear();
    }

    public void clearShared() {
        shared.clear();
    }

    public void registerShared(ResourceLocation key, String enUS) {
        shared.put(key, enUS);
    }

    public void registerTag(ResourceLocation key, String title, String description) {
        tag.put(key, Couple.create(title, description));
    }

    public void registerSpecific(ResourceLocation sceneId, String key, String enUS) {
        specific.computeIfAbsent(sceneId, $ -> new HashMap<>()).put(key, enUS);
    }

    //

    protected static String langKeyForShared(ResourceLocation k) {
        return k.getNamespace() + "." + LANG_PREFIX + "shared." + k.getPath();
    }

    protected static String langKeyForTag(ResourceLocation k) {
        return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath();
    }

    protected static String langKeyForTagDescription(ResourceLocation k) {
        return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath() + ".description";
    }

    protected static String langKeyForSpecific(ResourceLocation sceneId, String k) {
        return sceneId.getNamespace() + "." + LANG_PREFIX + sceneId.getPath() + "." + k;
    }

    @Override
    public String getShared(ResourceLocation key) {
        if (PonderIndex.editingModeActive())
            return shared.containsKey(key) ? shared.get(key) : ("unregistered shared entry: " + key);
        return I18n.get(langKeyForShared(key));
    }

    @Override
    public String getShared(ResourceLocation key, Object... params) {
        if (PonderIndex.editingModeActive())
            return shared.containsKey(key) ? String.format(shared.get(key), params) : ("unregistered shared entry: " + key);
        return I18n.get(langKeyForShared(key), params);
    }

    @Override
    public String getTagName(ResourceLocation key) {
        if (PonderIndex.editingModeActive())
            return tag.containsKey(key) ? tag.get(key).getFirst() : ("unregistered tag entry: " + key);
        return I18n.get(langKeyForTag(key));
    }

    @Override
    public String getTagDescription(ResourceLocation key) {
        if (PonderIndex.editingModeActive())
            return tag.containsKey(key) ? tag.get(key).getSecond() : ("unregistered tag entry: " + key);
        return I18n.get(langKeyForTagDescription(key));
    }

    @Override
    public String getSpecific(ResourceLocation sceneId, String k) {
        if (PonderIndex.editingModeActive())
            try {
                return specific.get(sceneId).get(k);
            } catch (Exception e) {
                return "MISSING_SPECIFIC";
            }
        return I18n.get(langKeyForSpecific(sceneId, k));
    }

    @Override
    public String getSpecific(ResourceLocation sceneId, String k, Object... params) {
        if (PonderIndex.editingModeActive())
            try {
                return String.format(specific.get(sceneId).get(k), params);
            } catch (Exception e) {
                return "MISSING_SPECIFIC";
            }
        return I18n.get(langKeyForSpecific(sceneId, k), params);
    }
}
