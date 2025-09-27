package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.ponder.api.registration.LangRegistryAccess;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PonderLocalization implements LangRegistryAccess {
    public static final String LANG_PREFIX = "ponder.";
    public static final String UI_PREFIX = "ui.";

    public final Map<Identifier, String> shared = new HashMap<>();
    public final Map<Identifier, Couple<String>> tag = new HashMap<>();
    public final Map<Identifier, Map<String, String>> specific = new HashMap<>();

    //

    public void clearAll() {
        shared.clear();
        tag.clear();
        specific.clear();
    }

    public void clearShared() {
        shared.clear();
    }

    public void registerShared(Identifier key, String enUS) {
        shared.put(key, enUS);
    }

    public void registerTag(Identifier key, String title, String description) {
        tag.put(key, Couple.create(title, description));
    }

    public void registerSpecific(Identifier sceneId, String key, String enUS) {
        specific.computeIfAbsent(sceneId, $ -> new HashMap<>()).put(key, enUS);
    }

    //

    protected static String langKeyForShared(Identifier k) {
        return k.getNamespace() + "." + LANG_PREFIX + "shared." + k.getPath();
    }

    protected static String langKeyForTag(Identifier k) {
        return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath();
    }

    protected static String langKeyForTagDescription(Identifier k) {
        return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath() + ".description";
    }

    protected static String langKeyForSpecific(Identifier sceneId, String k) {
        return sceneId.getNamespace() + "." + LANG_PREFIX + sceneId.getPath() + "." + k;
    }

    @Override
    public String getShared(Identifier key) {
        if (PonderIndex.editingModeActive())
            return shared.containsKey(key) ? shared.get(key) : ("unregistered shared entry: " + key);
        return I18n.translate(langKeyForShared(key));
    }

    @Override
    public String getTagName(Identifier key) {
        if (PonderIndex.editingModeActive())
            return tag.containsKey(key) ? tag.get(key).getFirst() : ("unregistered tag entry: " + key);
        return I18n.translate(langKeyForTag(key));
    }

    @Override
    public String getTagDescription(Identifier key) {
        if (PonderIndex.editingModeActive())
            return tag.containsKey(key) ? tag.get(key).getSecond() : ("unregistered tag entry: " + key);
        return I18n.translate(langKeyForTagDescription(key));
    }

    @Override
    public String getSpecific(Identifier sceneId, String k) {
        if (PonderIndex.editingModeActive())
            try {
                return specific.get(sceneId).get(k);
            } catch (Exception e) {
                return "MISSING_SPECIFIC";
            }
        return I18n.translate(langKeyForSpecific(sceneId, k));
    }
}
