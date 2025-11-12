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
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class CwgGuiComponent extends Gui implements ICwgGuiComponent {

    private int x, y, width, height;
    private boolean enabled = true, visible = true, hovered;
    private final List<String> tooltipLines = new ArrayList<>();
    private Consumer<? super CwgGuiComponent> onClick;

    public CwgGuiComponent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setOnClick(Consumer<? super CwgGuiComponent> action) {
        this.onClick = action;
    }

    public void addTooltipLines(String... lines) {
        this.tooltipLines.addAll(Arrays.asList(lines));
    }

    public void addTooltipLine(String line) {
        this.tooltipLines.add(line);
    }

    public void setTooltip(String... lines) {
        this.tooltipLines.clear();
        this.tooltipLines.addAll(Arrays.asList(lines));
    }

    public void clearTooltip() {
        this.tooltipLines.clear();
    }

    @Override public int getX() {
        return x;
    }

    @Override public int getY() {
        return y;
    }

    @Override public int getWidth() {
        return width;
    }

    @Override public int getHeight() {
        return height;
    }

    @Override public void setX(int x) {
        this.x = x;
    }

    @Override public void setY(int y) {
        this.y = y;
    }

    @Override public void setWidth(int width) {
        this.width = width;
    }

    @Override public void setHeight(int height) {
        this.height = height;
    }

    @Override public boolean isEnabled() {
        return enabled;
    }

    @Override public boolean isVisible() {
        return visible;
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override public void preDraw(Minecraft mc, int mouseX, int mouseY, float partialTick) {
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }


    @Override public void drawForeground(Minecraft mc, int mouseX, int mouseY, float partialTick) {
        if (hovered && !tooltipLines.isEmpty()) {
            Minecraft.getMinecraft().currentScreen.drawHoveringText(tooltipLines, mouseX, mouseY);
        }
    }

    @Override
    public boolean onMousePressed(Minecraft mc, int x, int y, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        if (onClick == null) {
            return false;
        }
        onClick.accept(this);
        return true;
    }
}
