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
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

@Deprecated // this is temporary for incremental migration only
public final class WrappedVanillaButton<T extends GuiButton> extends UIComponent<WrappedVanillaButton<T>> {

    private final T vanillaButton;

    public WrappedVanillaButton(MalisisGui gui, T vanillaButton) {
        super(gui);
        this.vanillaButton = vanillaButton;
        setSize(vanillaButton.getButtonWidth(), vanillaButton.height);
        setPosition(vanillaButton.x, vanillaButton.y);
        // render foreground from fake tooltip for proper z-ordering
        this.setTooltip(new UITooltip(gui, "") {
            @Override public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            }

            @Override public void drawForeground(GuiRenderer guiRenderer, int mouseX, int mouseY, float partialTick) {
                guiRenderer.draw();
                vanillaButton.drawButtonForegroundLayer(mouseX, mouseY);
                guiRenderer.next();
                resyncGlState(guiRenderer);
            }
        });
    }

    public T get() {
        return vanillaButton;
    }

    public WrappedVanillaButton<T> setEnabled(boolean enabled) {
        return super.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override public WrappedVanillaButton<T> setPosition(int x, int y, int anchor) {
        super.setPosition(x, y, anchor);
        vanillaButton.x = getX();
        vanillaButton.y = getY();
        return self();
    }

    @Override public WrappedVanillaButton<T> setSize(int width, int height) {
        super.setSize(width, height);
        vanillaButton.setWidth(getWidth());
        vanillaButton.height = getHeight();
        return self();
    }

    @Override public void drawBackground(GuiRenderer guiRenderer, int mouseX, int mouseY, float partialTick) {
        float offsetX = ((UIContainer<?>) getParent()).getOffsetX();
        float offsetY = ((UIContainer<?>) getParent()).getOffsetY();
        // MalisisCore is dumb and assumes that if you call those methods we have a scrollbar and computes a NaN which breaks cast to int
        if (Float.isNaN(offsetX)) {
            offsetX = 0;
        }
        if (Float.isNaN(offsetY)) {
            offsetY = 0;
        }
        vanillaButton.x = (int) (screenX() - offsetX);
        vanillaButton.y = (int) (screenY() - offsetY);
        vanillaButton.setWidth(getWidth());
        vanillaButton.height = getHeight();

        guiRenderer.draw();
        vanillaButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTick);
        resyncGlState(guiRenderer);
    }

    private static void resyncGlState(GuiRenderer guiRenderer) {
        // vanilla components can change it from under malisiscore, and malisiscore doesn't actually update if it thinks nothing changed
        Minecraft.getMinecraft().getTextureManager().bindTexture(guiRenderer.getDefaultTexture().getResourceLocation());
        guiRenderer.bindDefaultTexture();
        // in vanilla it's enabled by default, in malisiscore it's disabled by default
        GlStateManager.disableRescaleNormal();
        // MalisisCore has standard item lighting disabled by default
        RenderHelper.disableStandardItemLighting();
        // ... but colorMaterial is enabled
        GlStateManager.enableColorMaterial();
        // resync blending state, by default disabled in vanilla, enabled in malisis
        GlStateManager.enableBlend();
        guiRenderer.enableBlending();
        // Malisis always uses GL11.GL_SMOOTH shade model, vanilla uses flat by default
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
    }

    @Override public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
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
