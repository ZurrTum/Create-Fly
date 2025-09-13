package com.zurrtum.create.client.content.schematics.client.tools;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ToolType {

    DEPLOY(new DeployTool(), AllIcons.I_TOOL_DEPLOY),
    MOVE(new MoveTool(), AllIcons.I_TOOL_MOVE_XZ),
    MOVE_Y(new MoveVerticalTool(), AllIcons.I_TOOL_MOVE_Y),
    ROTATE(new RotateTool(), AllIcons.I_TOOL_ROTATE),
    FLIP(new FlipTool(), AllIcons.I_TOOL_MIRROR),
    PRINT(new PlaceTool(), AllIcons.I_CONFIRM);

    private final ISchematicTool tool;
    private final AllIcons icon;

    ToolType(ISchematicTool tool, AllIcons icon) {
        this.tool = tool;
        this.icon = icon;
    }

    public ISchematicTool getTool() {
        return tool;
    }

    public MutableText getDisplayName() {
        return CreateLang.translateDirect("schematic.tool." + Lang.asId(name()));
    }

    public AllIcons getIcon() {
        return icon;
    }

    public static List<ToolType> getTools(boolean creative) {
        List<ToolType> tools = new ArrayList<>();
        Collections.addAll(tools, MOVE, MOVE_Y, DEPLOY, ROTATE, FLIP);
        if (creative)
            tools.add(PRINT);
        return tools;
    }

    public List<Text> getDescription() {
        return CreateLang.translatedOptions("schematic.tool." + Lang.asId(name()) + ".description", "0", "1", "2", "3");
    }

}
