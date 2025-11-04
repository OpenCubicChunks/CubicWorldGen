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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;
import java.util.function.Function;

// TODO: fix vanilla rendering for large widths
public class CwgGuiSlider extends GuiButton {

    private final String textFormat;
    private final Function<Double, String> valueToString;
    private final Converter<Double, Double> positionToValue;

    private double sliderPosition;
    private boolean mousePressed;
    private Consumer<CwgGuiSlider> updateHandler = x -> {};

    public CwgGuiSlider(String textFormat, Function<Double, String> valueToString, Converter<Double, Double> positionToValue, double defaultValue) {
        super(0, 0, 0, "");
        this.textFormat = textFormat;
        this.valueToString = valueToString;
        this.positionToValue = positionToValue;
        this.sliderPosition = positionToValue.reverse().convert(defaultValue);
    }

    public void onUpdate(Consumer<CwgGuiSlider> updateHandler) {
        this.updateHandler = updateHandler;
    }

    public double getSliderPosition() {
        return sliderPosition;
    }

    public double getSliderValue() {
        return positionToValue.convert(getSliderPosition());
    }

    public void setSliderValue(double sliderValue) {
        this.sliderPosition = positionToValue.reverse().convert(sliderValue);
    }

    @Override protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    // this is actually a draw() method

    @Override protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }
        if (mousePressed && enabled) {
            double position = (mouseX - (this.x + 4.0)) / (this.width - 8.0);
            this.sliderPosition = MathHelper.clamp(position, 0, 1);
            updateHandler.accept(this);
        }

        this.displayString = I18n.format(textFormat, valueToString.apply(getSliderValue()));

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(this.x + (int)(this.sliderPosition * (this.width - 8)), this.y, 0, 66, 4, 20);
        this.drawTexturedModalRect(this.x + (int)(this.sliderPosition * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
    }

    @Override public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!super.mousePressed(mc, mouseX, mouseY)) {
            return false;
        }
        this.mousePressed = true;

        double position = (mouseX - (this.x + 4.0)) / (this.width - 8.0);
        this.sliderPosition = MathHelper.clamp(position, 0, 1);
        updateHandler.accept(this);

        return true;
    }

    @Override public void mouseReleased(int mouseX, int mouseY) {
        this.mousePressed = false;
    }
}
