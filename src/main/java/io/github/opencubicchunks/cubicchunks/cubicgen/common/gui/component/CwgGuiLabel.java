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

import io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.accessor.IGuiLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CwgGuiLabel extends GuiLabel {

    public static CwgGuiLabel create(String formatString,Type type, int x, int y, int width, int height, int color) {
        CwgGuiLabel label = new CwgGuiLabel(x, y, width, height, color);
        if (type == Type.CENTERED) {
            label.setCentered();
        }
        label.setTranslationKey(formatString);
        return label;
    }

    public static CwgGuiLabel createUnlocalized(String text, Type type, int x, int y, int width, int height, int color) {
        CwgGuiLabel label = new CwgGuiLabel(x, y, width, height, color);
        if (type == Type.CENTERED) {
            label.setCentered();
        }
        label.setUnlocalizedText(text);
        return label;
    }

    public static CwgGuiLabel create(Type type, int x, int y, int width, int height, int color) {
        CwgGuiLabel label = new CwgGuiLabel(x, y, width, height, color);
        if (type == Type.CENTERED) {
            label.setCentered();
        }
        return label;
    }

    private CwgGuiLabel(int x, int y, int width, int height, int color) {
        super(Minecraft.getMinecraft().fontRenderer, 0, x, y, width, height, color);
    }

    @Override public void addLine(String line) {
        super.addLine(line);
        this.height += 10;
    }

    public void setTranslationKey(String... text) {
        List<String> labels = ((IGuiLabel) this).getLabels();
        labels.clear();
        for (String translationKey : text) {
            labels.addAll(Arrays.asList(I18n.format(translationKey).split("\n")));
        }
        this.height = labels.size() * 10;
    }

    public void setUnlocalizedText(String text) {
        setLines(text.split("\n"));
    }

    public void setLines(String... text) {
        List<String> labels = ((IGuiLabel) this).getLabels();
        labels.clear();
        Collections.addAll(labels, text);
        this.height = text.length * 10;
    }

    public enum Type {
        LEFT_ALIGN, CENTERED
    }
}
