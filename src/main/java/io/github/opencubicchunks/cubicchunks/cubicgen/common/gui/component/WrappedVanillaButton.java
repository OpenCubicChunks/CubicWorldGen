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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

@Deprecated // this is temporary for incremental migration only
public class WrappedVanillaButton<T extends WrappedVanillaButton<T>> extends UIComponent<T> {

    private final GuiButton vanillaButton;

    public WrappedVanillaButton(MalisisGui gui, GuiButton vanillaButton) {
        super(gui);
        this.vanillaButton = vanillaButton;
        setSize(vanillaButton.getButtonWidth(), vanillaButton.height);
    }

    public T setEnabled(boolean enabled) {
        return super.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override public T setPosition(int x, int y, int anchor) {
        super.setPosition(x, y, anchor);
        vanillaButton.x = getX();
        vanillaButton.y = getY();
        return self();
    }

    @Override public T setSize(int width, int height) {
        super.setSize(width, height);
        vanillaButton.setWidth(getWidth());
        vanillaButton.height = getHeight();
        return self();
    }

    @Override public void drawBackground(GuiRenderer guiRenderer, int mouseX, int mouseY, float partialTick) {
        vanillaButton.x = (int) (screenX() - ((UIContainer<?>) getParent()).getOffsetX());
        vanillaButton.y = (int) (screenY() - ((UIContainer<?>) getParent()).getOffsetY());
        vanillaButton.setWidth(getWidth());
        vanillaButton.height = getHeight();

        guiRenderer.draw();
        vanillaButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTick);
        // vanilla components can change it from under malisiscore, and malisiscore doesn't actually update if it thinks nothing changed
        Minecraft.getMinecraft().getTextureManager().bindTexture(guiRenderer.getDefaultTexture().getResourceLocation());
        guiRenderer.bindDefaultTexture();
    }

    @Override public void drawForeground(GuiRenderer guiRenderer, int mouseX, int mouseY, float partialTick) {
        guiRenderer.draw();
        vanillaButton.drawButtonForegroundLayer(mouseX, mouseY);
        guiRenderer.next();
    }

    @Override public boolean onButtonPress(int x, int y, MouseButton button) {
        if (!super.onButtonPress(x, y, button) && button == MouseButton.LEFT) {
            vanillaButton.mousePressed(Minecraft.getMinecraft(), x, y);
            return vanillaButton.enabled && vanillaButton.visible;
        }
        return false;
    }

    @Override public boolean onButtonRelease(int x, int y, MouseButton button) {
        if (!super.onButtonRelease(x, y, button) && button == MouseButton.LEFT) {
            vanillaButton.mouseReleased(x, y);
            return vanillaButton.enabled && vanillaButton.visible;
        }
        return false;
    }
}
