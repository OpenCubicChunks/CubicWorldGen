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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui;

import com.google.common.base.Converter;
import com.google.common.eventbus.Subscribe;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiButton;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiCheckBox;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiLabel;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiSeparator;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.WrappedVanillaComponent;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.converter.Converters;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import javax.annotation.Nonnull;

public class CwgGuiFactory {

    private CwgGuiFactory() {
        throw new Error();
    }

    public static <T extends GuiButton> WrappedVanillaComponent<T> wrap(MalisisGui gui, T vanillaComponent) {
        return WrappedVanillaComponent.of(gui, vanillaComponent);
    }

    public static <T extends GuiLabel> WrappedVanillaComponent<T> wrap(MalisisGui gui, T vanillaComponent) {
        return WrappedVanillaComponent.of(gui, vanillaComponent);
    }

    public static <T extends GuiTextField> WrappedVanillaComponent<T> wrap(MalisisGui gui, T vanillaComponent) {
        return WrappedVanillaComponent.of(gui, vanillaComponent);
    }

    public static CwgGuiSeparator separator() {
        return new CwgGuiSeparator(0, 0);
    }

    public static GuiTextField intTextField(int defaultValue) {
        GuiTextField field = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 0, 20);
        field.setValidator(str -> {
            if (str.isEmpty()) {
                return true;
            }
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        });
        field.setText(String.valueOf(defaultValue));
        return field;
    }

    public static GuiTextField doubleTextField(double defaultValue) {
        GuiTextField field = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 0, 20);
        field.setValidator(newStr -> {
            if (newStr.isEmpty()) {
                return true;
            }
            try {
                Double.parseDouble(newStr);
                return true;
            } catch (NumberFormatException e1) {
                return false;
            }
        });
        field.setText(String.valueOf(defaultValue));
        return field;
    }

    public static CwgGuiLabel label() {
        return CwgGuiLabel.create(0, 0, 10, 10, 0xFFFFFFFF);
    }

    public static CwgGuiLabel label(String formatString) {
        return label(formatString, 0xFFFFFFFF, 0, 0);
    }

    public static CwgGuiLabel label(String formatString, int color) {
        return label(str(formatString), color, 0, 0);
    }

    public static CwgGuiLabel label(String formatString, int x, int y) {
        return label(formatString, 0xFFFFFFFF, x, y);
    }

    public static CwgGuiLabel label(String formatString, int color, int x, int y) {
        return CwgGuiLabel.create(str(formatString), x, y, 10, 10, color);
    }

    public static CwgGuiLabel labelUnloc(String... lines) {
        CwgGuiLabel label = label();
        label.setLines(lines);
        return label;
    }

    public static CwgGuiLabel labelUnloc(String text) {
        return labelUnloc(text, 0xFFFFFFFF, 0, 0);
    }

    public static CwgGuiLabel labelUnloc(String text, int color) {
        return labelUnloc(str(text), color, 0, 0);
    }

    public static CwgGuiLabel labelUnloc(String text, int x, int y) {
        return labelUnloc(text, 0xFFFFFFFF, x, y);
    }

    public static CwgGuiLabel labelUnloc(String text, int color, int x, int y) {
        return CwgGuiLabel.createUnlocalized(text, x, y, 10, 10, color);
    }

    public static CwgGuiButton button(String formatString) {
        return CwgGuiButton.create(str(formatString), null);
    }

    public static CwgGuiButton button(String formatString, Consumer<CwgGuiButton> onClick) {
        return CwgGuiButton.create(str(formatString), onClick);
    }

    public static CwgGuiButton buttonUnloc(String formatString) {
        return CwgGuiButton.createUnlocalized(formatString, null);
    }

    public static CwgGuiButton buttonUnloc(String formatString, Consumer<CwgGuiButton> onClick) {
        return CwgGuiButton.createUnlocalized(formatString, onClick);
    }

    public static CwgGuiCheckBox checkBox(String formatString, boolean defaultValue) {
        return CwgGuiCheckBox.create(str(formatString), defaultValue);
    }

    public static CwgGuiCheckBox checkBoxUnloc(String formatString, boolean defaultValue) {
        return CwgGuiCheckBox.create(formatString, defaultValue);
    }

    public static CwgGuiSlider slider(double min, double max, double defVal, String formatString, Function<Double, Object[]> params) {

        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defVal, Converters.builder().linearScale(min, max));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), params, conv, defVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider slider(double min, double max, double defVal, String formatString) {
        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defVal, Converters.builder().linearScale(min, max));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), conv, defVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider intSlider(int min, int max, int defVal, String formatString) {
        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defVal, Converters.builder().linearScale(min, max));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), x -> new Object[]{(int) Math.round(x)}, conv, defVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider intSlider(int min, int max, int defVal, String formatString, Function<Integer, Object[]> toString) {

        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defVal, Converters.builder().linearScale(min, max));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), x -> toString.apply((int) Math.round(x)), conv, defVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider positiveExponentialSlider(double minPos, double maxPos, double defaultVal,
            String formatString, Function<Double, Object[]> params) {

        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defaultVal, Converters.builder()
                .exponential().withBaseValue(2).withPositiveExponentRange(minPos, maxPos)
                .withInfinity().positiveAt(Math.pow(2, maxPos)).negativeAt(Double.NaN));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), params, conv, defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider positiveExponentialSlider(double minPos, double maxPos, double defaultVal, String formatString) {

        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defaultVal, Converters.builder()
                .exponential().withBaseValue(2).withPositiveExponentRange(minPos, maxPos)
                .withInfinity().positiveAt(Math.pow(2, maxPos)).negativeAt(Double.NaN));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), conv, defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider exponentialSlider(double minNeg, double maxNeg, double minPos, double maxPos, double defaultVal,
            String formatString, Function<Double, Object[]> params) {

        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defaultVal, Converters.builder()
                .exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), params, conv, defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider exponentialSlider(double minNeg, double maxNeg, double minPos, double maxPos, double defaultVal, String formatString) {
        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defaultVal, Converters.builder()
                .exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos));

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), conv, defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider symmetricExponentialSlider(double min, double max, double defaultVal, String formatString, Function<Double, Object[]> params) {
        return exponentialSlider(min, max, min, max, defaultVal, formatString, params);
    }

    public static CwgGuiSlider symmetricExponentialSlider(double min, double max, double defaultVal, String formatString) {
        return exponentialSlider(min, max, min, max, defaultVal, formatString);
    }

    public static CwgGuiSlider invertedExponentialSlider(double minNeg, double maxNeg, double minPos, double maxPos, double defaultVal,
            String formatString, Function<Double, Object[]> params) {

        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defaultVal, Converters.builder()
                .reverse().pow(2)
                .exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos)
                .inverse());

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), params, conv, defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider invertedExponentialSlider(double minNeg, double maxNeg, double minPos, double maxPos, double defaultVal, String formatString) {
        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        Converter<Double, Double> conv = converter(wrappedSlider, defaultVal, Converters.builder()
                .reverse().pow(2)
                .exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos)
                .inverse());

        CwgGuiSlider slider = CwgGuiSlider.create(str(formatString), conv, defaultVal);
        wrappedSlider[0] = slider;
        return slider;
    }

    public static CwgGuiSlider invertedPositiveExponentialSlider(double min, double max, double defaultVal, String formatString) {
        return invertedExponentialSlider(Double.NaN, Double.NaN, min, max, defaultVal, formatString);
    }

    public static CwgGuiSlider invertedPositiveExponentialSlider(double min, double max, double defaultVal,
            String formatString, Function<Double, Object[]> params) {
        return invertedExponentialSlider(Double.NaN, Double.NaN, min, max, defaultVal, formatString, params);
    }

    // internal utils
    private static Converter<Double, Double> converter(CwgGuiSlider[] wrappedSlider, double defVal, Converters.Builder min) {
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);

        double defMult = defVal == 0 ? 1 : defVal;

        Converter<Double, Double> conv = min.rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128)
                .withRoundingRadiusPredicate(isInRoundRadius)
                .build();
        return conv;
    }

    @Nonnull private static BiPredicate<Double, Double> getIsInRoundRadiusPredicate(CwgGuiSlider[] slider) {
        return getIsInRoundRadiusPredicate(() -> slider[0] == null ? 1000 : slider[0].width);
    }

    @Nonnull private static BiPredicate<Double, Double> getIsInRoundRadiusPredicate(DoubleSupplier width) {
        return (previousSlide, foundSlide) -> {
            double w = width.getAsDouble();
            double rangeCenter = Math.round(previousSlide * w) / w;
            double minRange = rangeCenter - 0.5 / w;
            double maxRange = rangeCenter + 0.5 / w;

            return foundSlide >= minRange && foundSlide <= maxRange;
        };
    }


    private static String str(String name) {
        String unloc = CustomCubicMod.MODID + ".gui.cubicgen." + name;
        return unloc;
    }
}
