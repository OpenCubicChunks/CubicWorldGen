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

import net.minecraft.client.Minecraft;

public interface ICwgGuiComponent {
    int getX();
    int getY();
    int getWidth();
    int getHeight();

    void setX(int x);
    void setY(int y);
    void setWidth(int width);
    void setHeight(int height);

    boolean isEnabled();
    boolean isVisible();
    void setEnabled(boolean enabled);
    void setVisible(boolean visible);

    default void preDraw(Minecraft mc, int mouseX, int mouseY, float partialTick) {}
    void drawBackground(Minecraft mc, int mouseX, int mouseY, float partialTick);
    default void drawForeground(Minecraft mc, int mouseX, int mouseY, float partialTick) {}

    default boolean onMousePressed(Minecraft mc, int mouseX, int mouseY) {
        return false;
    }

    default boolean onMouseReleased(Minecraft mc, int mouseX, int mouseY) {
        return false;
    }

    default boolean onKeyTyped(char keyChar, int keyCode) {
        return false;
    }
}
