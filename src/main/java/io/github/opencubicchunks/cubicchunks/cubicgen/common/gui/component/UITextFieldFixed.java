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
