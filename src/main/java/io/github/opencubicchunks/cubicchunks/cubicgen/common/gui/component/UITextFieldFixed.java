package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UITextField;

// UITextField with fixed scrolling and setOffsetY
public class UITextFieldFixed extends UITextField {

    public UITextFieldFixed(MalisisGui gui, String text, boolean multiLine) {
        super(gui, text, multiLine);
    }

    public UITextFieldFixed(MalisisGui gui, String text) {
        super(gui, text);
    }

    public UITextFieldFixed(MalisisGui gui, boolean multiLine) {
        super(gui, multiLine);
    }

    @Override
    public void setOffsetY(float offsetY, int delta) {
        lineOffset = Math.round(offsetY * (lines.size() - getVisibleLines()));
        lineOffset = Math.max(0, Math.min(lines.size(), lineOffset));
    }
}
