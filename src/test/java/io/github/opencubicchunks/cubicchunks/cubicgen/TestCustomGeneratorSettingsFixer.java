/*
 *  This file is part of Cubic World Generation, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import io.github.opencubicchunks.cubicchunks.cubicgen.testutil.MinecraftEnvironment;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TestCustomGeneratorSettingsFixer {
    // CubicChunks v0.0.655 example preset
    String testCase1 = "/assets/cubicworldgen/presets/test_case_1_original.json";
    String testCase1ExpectedResult = "/assets/cubicworldgen/presets/test_case_1_expected_result.json";
    // Multi-layer CubicChunks v0.0.854 example preset
    String testCase2 = "/assets/cubicworldgen/presets/test_case_2_original.json";
    String testCase2ExpectedResult = "/assets/cubicworldgen/presets/test_case_2_expected_result.json";

    @BeforeClass
    public static void setUp() {
        MinecraftEnvironment.init();
        CustomCubicMod.LOGGER = LogManager.getLogger("CustomCubicModTest");
        CubicBiome.init();
        CubicBiome.postInit();
    }

    private String getTestCaseString(String url) throws IOException {
        try (InputStream stream = TestCustomGeneratorSettingsFixer.class.getResourceAsStream(url)) {
            InputStreamReader isr = new InputStreamReader(stream);
            CharBuffer cb = CharBuffer.allocate(stream.available());
            isr.read(cb);
            cb.flip();
            return cb.toString();
        }
    }

    private void runTest(String exampleURL, String resultURL) throws IOException {
        CustomGeneratorSettingsFixer fixer = CustomGeneratorSettingsFixer.INSTANCE;
        //JsonObject fixed = fixer.fixJson(getTestCaseString(exampleURL));
       // JsonObject expected = fixer.fixJson(getTestCaseString(resultURL));
        //assertEquals(expected, fixed);
    }

    @Test
    public void testCustomGeneratorSettingsFixerCC655() throws IOException {
        runTest(testCase1, testCase1ExpectedResult);
    }

    @Test
    public void testCustomGeneratorSettingsFixerCC854MultiLayer() throws IOException {
        runTest(testCase2, testCase2ExpectedResult);
    }
}
