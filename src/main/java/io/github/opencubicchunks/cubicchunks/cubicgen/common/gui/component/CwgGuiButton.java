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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;

import javax.annotation.Nullable;

// TODO: fix vanilla rendering for large widths
public class CwgGuiButton extends GuiButton {

    private Consumer<CwgGuiButton> onClick;

    public CwgGuiButton(String formatString, @Nullable Consumer<CwgGuiButton> onClick) {
        super(0, 0, 0, I18n.format(formatString));
        this.onClick = onClick;
    }

    public void onClick(Consumer<CwgGuiButton> handler) {
        this.onClick = handler;
    }

    @Override public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.onClick.accept(this);
            return true;
        }
        return false;
    }


    @Override public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        McGuiRender.prepareWidgetRender(mc);
        McGuiRender.ButtonMode mode = McGuiRender.ButtonMode.values()[this.getHoverState(this.hovered)];
        McGuiRender.drawButtonBg(mode, this.x, this.y, this.width, this.height);

        this.mouseDragged(mc, mouseX, mouseY);

        McGuiRender.drawWidgetStringCentered(mc, this, packedFGColour, enabled, hovered);
    }
}
