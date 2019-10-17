package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component;

import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;

public class UIDynamicTextField extends UITextFieldFixed {

    public UIDynamicTextField(MalisisGui gui, String text, boolean multiLine) {
        super(gui, text, multiLine);
        registerEvents();
    }

    public UIDynamicTextField(MalisisGui gui, String text) {
        super(gui, text);
        registerEvents();
    }

    public UIDynamicTextField(MalisisGui gui, boolean multiLine) {
        super(gui, multiLine);
        registerEvents();
    }

    private void registerEvents() {
        this.register(new Object() {
            @Subscribe
            public void onContentUpdate(ContentUpdateEvent event) {
                MalisisFont font = UIDynamicTextField.this.font;
                FontOptions fo = UIDynamicTextField.this.fontOptions;
                String text = UIDynamicTextField.this.getText();
                int newWidth = Math.max(15, (int) (font.getStringWidth(text, fo) + 10));
                if (newWidth != UIDynamicTextField.this.getWidth()) {
                    UIDynamicTextField.this.setSize(newWidth, UIDynamicTextField.this.getHeight());
                }
            }
        });

    }
}
