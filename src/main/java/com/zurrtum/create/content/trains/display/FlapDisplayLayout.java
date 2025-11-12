package com.zurrtum.create.content.trains.display;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FlapDisplayLayout {
    List<FlapDisplaySection> sections;
    String layoutKey;

    public FlapDisplayLayout(int maxCharCount) {
        loadDefault(maxCharCount);
    }

    public void loadDefault(int maxCharCount) {
        configure("Default", Arrays.asList(new FlapDisplaySection(maxCharCount * FlapDisplaySection.MONOSPACE, "alphabet", false, false)));
    }

    public boolean isLayout(String key) {
        return layoutKey.equals(key);
    }

    public void configure(String layoutKey, List<FlapDisplaySection> sections) {
        this.layoutKey = layoutKey;
        this.sections = sections;
    }

    public void write(ValueOutput view) {
        view.putString("Key", layoutKey);
        ValueOutput.ValueOutputList list = view.childrenList("Sections");
        sections.forEach(section -> section.write(list.addChild()));
    }

    public void read(ValueInput view) {
        String prevKey = layoutKey;
        layoutKey = view.getStringOr("Key", "");

        if (!prevKey.equals(layoutKey)) {
            sections = new ArrayList<>();
            view.childrenListOrEmpty("Sections").forEach(section -> sections.add(FlapDisplaySection.load(section)));
            return;
        }

        MutableInt index = new MutableInt(0);
        view.childrenListOrEmpty("Sections").forEach(section -> sections.get(index.getAndIncrement()).update(section));
    }

    public List<FlapDisplaySection> getSections() {
        return sections;
    }

}
