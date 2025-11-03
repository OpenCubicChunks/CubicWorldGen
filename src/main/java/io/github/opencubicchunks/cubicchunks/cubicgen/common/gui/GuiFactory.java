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
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.CwgGuiSlider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.converter.Converters;

import java.util.function.BiPredicate;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import javax.annotation.Nonnull;

public class GuiFactory {

    private GuiFactory() {
        throw new Error();
    }

    public static CwgGuiSlider makeSlider(double min, double max, double defVal, String formatString, Function<Double, String> toString) {

        CwgGuiSlider[] wrappedSlider = new CwgGuiSlider[1];
        BiPredicate<Double, Double> isInRoundRadius = getIsInRoundRadiusPredicate(wrappedSlider);

        double defMult = defVal == 0 ? 1 : defVal;

        Converter<Double, Double> conv = Converters.builder()
                .linearScale(min, max).rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128)
                .withRoundingRadiusPredicate(isInRoundRadius)
                .build();

        CwgGuiSlider slider = new CwgGuiSlider(formatString, toString, conv, defVal);
        wrappedSlider[0] = slider;
        return slider;
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

}
