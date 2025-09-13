package com.zurrtum.create.content.trains.display;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public void write(WriteView view) {
        view.putString("Key", layoutKey);
        WriteView.ListView list = view.getList("Sections");
        sections.forEach(section -> section.write(list.add()));
    }

    public void read(ReadView view) {
        String prevKey = layoutKey;
        layoutKey = view.getString("Key", "");

        if (!prevKey.equals(layoutKey)) {
            sections = new ArrayList<>();
            view.getListReadView("Sections").forEach(section -> sections.add(FlapDisplaySection.load(section)));
            return;
        }

        MutableInt index = new MutableInt(0);
        view.getListReadView("Sections").forEach(section -> sections.get(index.getAndIncrement()).update(section));
    }

    public List<FlapDisplaySection> getSections() {
        return sections;
    }

}
