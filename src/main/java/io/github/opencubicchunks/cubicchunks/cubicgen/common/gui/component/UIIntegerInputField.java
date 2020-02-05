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

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;

public class UIIntegerInputField extends UITextField {

    private static final String MIN = "-infinity";
    private static final String MAX = "infinity";
    private static final FontOptions WHITE_FONT = FontOptions.builder().color(0xFFFFFF).build();
    private static final FontOptions RED_FONT = FontOptions.builder().color(0xFF0000).build();
    // TODO: add this to API
    private static final int MIN_BLOCK_Y = -(1 << 30);
    private static final int MAX_BLOCK_Y = (1 << 30) - 1;

    private int lastValidValue;

    public UIIntegerInputField(MalisisGui gui, int valueIn) {
        super(gui, false);
        lastValidValue = valueIn;
        if (valueIn <= MIN_BLOCK_Y)
            this.setText(MIN);
        else if (valueIn >= MAX_BLOCK_Y)
            this.setText(MAX);
        else
            this.setText(String.valueOf(valueIn));
        this.setFontOptions(WHITE_FONT);
        this.setOptions(0x6D6D6D, 0xFFFFFF, 0x000000);
    }

    public int getValue() {
        if (this.getText().equalsIgnoreCase(MIN)) {
            return MIN_BLOCK_Y;
        } else if (this.getText().equalsIgnoreCase(MAX)) {
            return MAX_BLOCK_Y;
        } else {
            try {
                return Integer.parseInt(this.getText());
            } catch (NumberFormatException e) {
                return lastValidValue;
            }
        }
    }

    private boolean isValidValue() {
        if (this.getText().equalsIgnoreCase(MIN) || this.getText().equalsIgnoreCase(MAX))
            return true;
        try {
            Integer.parseInt(this.getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean onKeyTyped(char keyChar, int keyCode) {
        boolean success = super.onKeyTyped(keyChar, keyCode);
        lastValidValue = getValue();
        if (isValidValue())
            this.setFontOptions(WHITE_FONT);
        else
            this.setFontOptions(RED_FONT);
        return success;
    }
}
