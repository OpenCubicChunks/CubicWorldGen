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
