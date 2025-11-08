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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class McGuiRender {
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

    private static final int BTN_TEX_MIN_X = 0;
    private static final int BTN_TEX_MIN_Y = 46;
    private static final int BTN_TEX_WIDTH = 200;
    private static final int BTN_TEX_HEIGHT = 20;
    private static final int WIDGETS_TEX_SIZE = 256;

    public static void prepareWidgetRender(Minecraft mc) {
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void drawWidgetStringCentered(Minecraft mc, GuiButton btn, int colorOverride, boolean enabled, boolean hovered) {
        int color = 0xe0e0e0;
        if (colorOverride != 0) {
            color = colorOverride;
        } else if (!enabled) {
            color = 0xa0a0a0;
        } else if (hovered) {
            color = 0xffffa0;
        }
        btn.drawCenteredString(mc.fontRenderer, btn.displayString, btn.x + btn.width / 2, btn.y + (btn.height - 8) / 2, color);
    }

    public static void drawWidgetString(Minecraft mc, GuiButton btn, int colorOverride, boolean enabled, boolean hovered) {
        int color = 0xe0e0e0;
        if (colorOverride != 0) {
            color = colorOverride;
        } else if (!enabled) {
            color = 0xa0a0a0;
        } else if (hovered) {
            color = 0xffffa0;
        }
        btn.drawString(mc.fontRenderer, btn.displayString, btn.x, btn.y + (btn.height - 8) / 2, color);
    }

    public static void drawSliderBg(int x, int y, int width, int height) {
        drawButtonBg(ButtonMode.DISABLED, x, y, width, height);
    }

    public static void drawButtonBg(ButtonMode mode, int x, int y, int width, int height) {
        int yOffset = mode.ordinal() * BTN_TEX_HEIGHT;
        int texWidth = width / 2;
        if (texWidth > 180) {
            texWidth = 180;
        }
        Gui.drawScaledCustomSizeModalRect(
                x, y,
                BTN_TEX_MIN_X, BTN_TEX_MIN_Y + yOffset,
                texWidth, BTN_TEX_HEIGHT,
                width / 2, height,
                WIDGETS_TEX_SIZE, WIDGETS_TEX_SIZE
        );
        Gui.drawScaledCustomSizeModalRect(
                x + width / 2, y,
                BTN_TEX_MIN_X + BTN_TEX_WIDTH - texWidth, BTN_TEX_MIN_Y + yOffset,
                texWidth, BTN_TEX_HEIGHT,
                width - width / 2, height,
                WIDGETS_TEX_SIZE, WIDGETS_TEX_SIZE
        );
    }

    public enum ButtonMode {
        DISABLED, ENABLED, HOVERED
    }
}
