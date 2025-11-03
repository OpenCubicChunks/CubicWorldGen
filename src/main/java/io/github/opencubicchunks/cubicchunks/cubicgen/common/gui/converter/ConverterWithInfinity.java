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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.converter;

import com.google.common.base.Converter;

public class ConverterWithInfinity extends Converter<Double, Double> {

    private final double negative;
    private final double positive;

    public ConverterWithInfinity(double negative, double positive) {
        this.negative = negative;
        this.positive = positive;
    }

    @Override protected Double doForward(Double v) {
        if (v <= negative) {
            return Double.NEGATIVE_INFINITY;
        }
        if (v >= positive) {
            return Double.POSITIVE_INFINITY;
        }
        return v;
    }

    @Override protected Double doBackward(Double v) {
        if (v == Double.NEGATIVE_INFINITY) {
            return negative;
        }
        if (v == Double.POSITIVE_INFINITY) {
            return positive;
        }
        return v;
    }
}
