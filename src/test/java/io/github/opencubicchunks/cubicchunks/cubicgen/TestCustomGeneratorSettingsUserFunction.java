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
package io.github.opencubicchunks.cubicchunks.cubicgen;

import static org.junit.Assert.assertEquals;

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestCustomGeneratorSettingsUserFunction {

    @Test public void testNoValues() {
        Map<Float, Float> funcMap = new HashMap<>();
        CustomGeneratorSettings.UserFunction func = new CustomGeneratorSettings.UserFunction(funcMap);

        for (int y = -100; y < 100; y++) {
            assertEquals(0.0f, func.getValue(y), 0);
        }
    }

    @Test public void testSingleValue() {
        Map<Float, Float> funcMap = new HashMap<>();
        funcMap.put(1.23456789f, 9.87654321f);
        CustomGeneratorSettings.UserFunction func = new CustomGeneratorSettings.UserFunction(funcMap);

        for (int y = -100; y < 100; y++) {
            assertEquals(9.87654321f, func.getValue(y), 0);
        }
    }

    @Test public void test2Values() {
        Map<Float, Float> funcMap = new HashMap<>();
        funcMap.put(0.0f, 0.0f);
        funcMap.put(1.0f, 1.0f); // identity function
        CustomGeneratorSettings.UserFunction func = new CustomGeneratorSettings.UserFunction(funcMap);

        for (int y = -100; y < 100; y++) {
            float yTest = y * 0.1f;
            assertEquals(yTest, func.getValue(yTest), 0.00001);
        }
    }

    @Test public void test3Values() {
        Map<Float, Float> funcMap = new HashMap<>();
        funcMap.put(-1.0f, 1.0f);
        funcMap.put(0.0f, 0.0f);
        funcMap.put(1.0f, 1.0f); // abs function
        CustomGeneratorSettings.UserFunction func = new CustomGeneratorSettings.UserFunction(funcMap);

        for (int y = -10; y < 100; y++) {
            float yTest = y * 0.1f;
            assertEquals(Math.abs(yTest), func.getValue(yTest), 0.00001);
        }
    }
}
