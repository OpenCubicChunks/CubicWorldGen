/*
 *  This file is part of Cubic World Generation, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015-2020 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
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
