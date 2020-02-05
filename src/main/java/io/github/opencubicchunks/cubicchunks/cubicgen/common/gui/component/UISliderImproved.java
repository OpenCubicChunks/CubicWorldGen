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

import com.google.common.base.Converter;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.function.Function;

public class UISliderImproved<T> extends UISlider<T> {

    private final Function<T, String> translation;

    public UISliderImproved(MalisisGui gui, int width, Converter<Float, T> converter, Function<T, String> translation) {
        super(gui, width, converter, "<this text should never be used>");
        this.translation = translation;
    }

    public UISliderImproved(MalisisGui gui, int width, Converter<Float, T> converter, String name) {
        this(gui, width, converter, value -> String.format(name, value));
    }

    @Override
    public boolean onScrollWheel(int x, int y, int delta) {
        if (parent != null && !(this instanceof IControlComponent)) {
            return parent.onScrollWheel(x, y, delta);
        }
        return false;
    }

    // improved accuracy
    @Override
    public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int realMouseX = Mouse.getEventX();
        float scaledMouseX = realMouseX / (float) sr.getScaleFactor();
        float relativeMouseX = scaledMouseX - screenX();

        int l = getWidth() - SLIDER_WIDTH;
        float pos = MathHelper.clamp(relativeMouseX - SLIDER_WIDTH / 2, 0, l);
        slideTo( pos / l);
        return true;
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {

        zIndex = 0;
        float factor = getHeight() / 20F;
        int ox = (int) (offset * (getWidth() - SLIDER_WIDTH * factor));
        sliderShape.resetState();
        sliderShape.setSize((int) (8 * factor), getHeight());
        sliderShape.setPosition(ox, 0);

        rp.iconProvider.set(sliderIcon);
        renderer.drawShape(sliderShape, rp);

        renderer.next();
        //zIndex = 1;

        String str = translation.apply(getValue());
        if (str == null) {
            str = ChatFormatting.ITALIC + "Format error";
        }
        int x = (int) ((getWidth() - font.getStringWidth(str, fontOptions)) / 2);
        int y = (int) Math.ceil((getHeight() - font.getStringHeight(fontOptions)) / 2);

        renderer.drawText(font, str, x, y, 0, isHovered() ? hoveredFontOptions : fontOptions);

        if (!this.isEnabled()) {
            renderer.next();
            GlStateManager.disableTexture2D();

            rp.setColor(0);
            rp.setAlpha(128);
            shape.resetState();
            shape.setSize(getWidth(), getHeight());
            renderer.drawShape(shape, rp);

            renderer.next();
            GlStateManager.enableTexture2D();
        }


    }
}
