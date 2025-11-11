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
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@Deprecated // this is temporary for incremental migration only
public final class WrappedVanillaComponent<T extends Gui> extends UIComponent<WrappedVanillaComponent<T>> {

    private final T vanillaComponent;
    private final IntSupplier getX;
    private final IntSupplier getY;
    private final IntConsumer setX;
    private final IntConsumer setY;
    private final IntSupplier getWidth;
    private final IntSupplier getHeight;
    private final IntConsumer setWidth;
    private final IntConsumer setHeight;
    private final BooleanSupplier isEnabled;
    private final Consumer<Boolean> setEnabled;
    private final BooleanSupplier isVisible;
    private final GuiRender drawBackground;
    private final GuiRender drawForeground;
    private final MouseHandler mousePressed;
    private final MouseHandler mouseReleased;
    private final KeyHandler keyHandler;

    public static <T extends GuiButton> WrappedVanillaComponent<T> of(MalisisGui gui, T btn) {
        return new WrappedVanillaComponent<>(
                gui, btn,
                () -> btn.x, () -> btn.y, x -> btn.x = x, y -> btn.y = y,
                btn::getButtonWidth, () -> btn.height, btn::setWidth, height -> btn.height = height,
                () -> btn.enabled, val -> btn.enabled = val, () -> btn.visible,
                btn::drawButton,
                (mc, mouseX, mouseY, partialTick) -> btn.drawButtonForegroundLayer(mouseX, mouseY),
                btn::mousePressed,
                (mc, mouseX, mouseY) -> btn.mouseReleased(mouseX, mouseY),
                KeyHandler.NULL
        );
    }

    public static <T extends GuiLabel> WrappedVanillaComponent<T> of(MalisisGui gui, T lbl) {
        return new WrappedVanillaComponent<>(
                gui, lbl,
                () -> lbl.x, () -> lbl.y, x -> lbl.x = x, y -> lbl.y = y,
                ((IGuiLabel) lbl)::getWidth, ((IGuiLabel) lbl)::getHeight, ((IGuiLabel) lbl)::setWidth, ((IGuiLabel) lbl)::setHeight,
                () -> lbl.visible, val -> lbl.visible = val, () -> lbl.visible,
                (mc, mouseX, mouseY, partialTick) -> lbl.drawLabel(mc, mouseX, mouseY),
                GuiRender.NULL, MouseHandler.NULL, MouseHandler.NULL, KeyHandler.NULL
        );
    }

    public static <T extends GuiTextField> WrappedVanillaComponent<T> of(MalisisGui gui, T fld) {
        return new WrappedVanillaComponent<>(
                gui, fld,
                () -> fld.x, () -> fld.y, x -> fld.x = x, y -> fld.y = y,
                () -> fld.width, () -> fld.height, width -> {
                    fld.width = width;
                    // changing width affects line wrapping?
                    fld.setSelectionPos(fld.getSelectionEnd());
                }, height -> fld.height = height,
                fld::getVisible, fld::setVisible, fld::getVisible,
                (mc, mouseX, mouseY, partialTick) -> {
                    // vanilla renders outline 1 px out of bounds...
                    if (fld.getEnableBackgroundDrawing()) {
                        fld.x += 1;
                        fld.y += 1;
                        fld.width -= 2;
                        fld.height -= 2;
                    }
                    fld.drawTextBox();
                    if (fld.getEnableBackgroundDrawing()) {
                        fld.x -= 1;
                        fld.y -= 1;
                        fld.width += 2;
                        fld.height += 2;
                    }
                },
                GuiRender.NULL,
                (mc, mouseX, mouseY) -> fld.mouseClicked(mouseX, mouseY, 0),
                MouseHandler.NULL, fld::textboxKeyTyped
        );
    }

    public static <T extends Gui & ICwgGuiComponent> WrappedVanillaComponent<T> of(MalisisGui gui, T comp) {
        return new WrappedVanillaComponent<>(
                gui, comp,
                comp::getX, comp::getY, comp::setX, comp::setY,
                comp::getWidth, comp::getHeight, comp::setWidth, comp::setHeight,
                comp::isEnabled, comp::setEnabled, comp::isVisible,
                (mc, mouseX, mouseY, partialTick) -> {
                    comp.preDraw(mc, mouseX, mouseY, partialTick);
                    comp.drawBackground(mc, mouseX, mouseY, partialTick);
                },
                comp::drawForeground, comp::onMousePressed, comp::onMouseReleased, comp::onKeyTyped
        );
    }

    private WrappedVanillaComponent(MalisisGui malisisGui, T vanillaComponent,
            IntSupplier getX, IntSupplier getY, IntConsumer setX, IntConsumer setY,
            IntSupplier getWidth, IntSupplier getHeight, IntConsumer setWidth, IntConsumer setHeight,
            BooleanSupplier isEnabled, Consumer<Boolean> setEnabled,
            BooleanSupplier isVisible,
            GuiRender drawBackground, GuiRender drawForeground,
            MouseHandler mousePressed, MouseHandler mouseReleased, KeyHandler keyHandler) {
        super(malisisGui);
        this.vanillaComponent = vanillaComponent;
        this.getX = getX;
        this.getY = getY;
        this.setX = setX;
        this.setY = setY;
        this.getWidth = getWidth;
        this.getHeight = getHeight;
        this.setWidth = setWidth;
        this.setHeight = setHeight;
        this.isEnabled = isEnabled;
        this.setEnabled = setEnabled;
        this.isVisible = isVisible;
        this.drawBackground = drawBackground;
        this.drawForeground = drawForeground;
        this.mousePressed = mousePressed;
        this.mouseReleased = mouseReleased;
        this.keyHandler = keyHandler;

        setSize(getWidth.getAsInt(), getHeight.getAsInt());
        setPosition(getX.getAsInt(), getY.getAsInt());
        // render foreground from fake tooltip for proper z-ordering
        this.setTooltip(new UITooltip(malisisGui, "") {
            @Override public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            }

            @Override public void drawForeground(GuiRenderer guiRenderer, int mouseX, int mouseY, float partialTick) {
                guiRenderer.draw();
                drawForeground.draw(Minecraft.getMinecraft(), mouseX, mouseY, partialTick);
                guiRenderer.next();
                resyncGlState(guiRenderer);
            }
        });
    }

    public T get() {
        return vanillaComponent;
    }

    public WrappedVanillaComponent<T> setEnabled(boolean enabled) {
        return super.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override public WrappedVanillaComponent<T> setPosition(int x, int y, int anchor) {
        super.setPosition(x, y, anchor);
        setX.accept(getX());
        setY.accept(getY());
        return self();
    }

    @Override public WrappedVanillaComponent<T> setSize(int width, int height) {
        super.setSize(width, height);
        setWidth.accept(getWidth());
        setHeight.accept(getHeight());
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
        setX.accept((int) (screenX() - offsetX));
        setY.accept((int) (screenY() - offsetY));
        setWidth.accept(getWidth());
        setHeight.accept(getHeight());

        guiRenderer.draw();
        drawBackground.draw(Minecraft.getMinecraft(), mouseX, mouseY, partialTick);
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
        if (button != MouseButton.LEFT) {
            return super.onButtonPress(x, y, button);
        }
        if (!(isEnabled.getAsBoolean() && isVisible.getAsBoolean())) {
            return super.onButtonPress(x, y, button);
        }
        mousePressed.handle(Minecraft.getMinecraft(), x, y);
        return true;
    }

    @Override public boolean onButtonRelease(int x, int y, MouseButton button) {
        if (button != MouseButton.LEFT) {
            return super.onButtonPress(x, y, button);
        }
        if (!(isEnabled.getAsBoolean() && isVisible.getAsBoolean())) {
            return super.onButtonPress(x, y, button);
        }
        mouseReleased.handle(Minecraft.getMinecraft(), x, y);
        return true;
    }

    @Override public boolean onKeyTyped(char keyChar, int keyCode) {
        if (!(isEnabled.getAsBoolean() && isVisible.getAsBoolean())) {
            return super.onKeyTyped(keyChar, keyCode);
        }
        return keyHandler.handle(keyChar, keyCode);
    }

    private interface GuiRender {
        GuiRender NULL = (mc, mouseX, mouseY, partialTick) -> {};

        void draw(Minecraft mc, int mouseX, int mouseY, float partialTick);
    }

    private interface MouseHandler {
        MouseHandler NULL = (mc, mouseX, mouseY) -> {};
        void handle(Minecraft mc, int mouseX, int mouseY);
    }

    private interface KeyHandler {
        KeyHandler NULL = (chr, code) -> false;
        boolean handle(char keyChar, int keyCode);
    }
}
