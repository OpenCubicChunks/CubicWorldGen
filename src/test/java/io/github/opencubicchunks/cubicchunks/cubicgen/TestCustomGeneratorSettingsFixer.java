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

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.V3LegacyFix;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.V3Preprocessor;
import io.github.opencubicchunks.cubicchunks.cubicgen.testutil.MinecraftEnvironment;
import mcp.MethodsReturnNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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

    private void runTest(String exampleURL, String resultURL) throws IOException, SyntaxError {
        final V3Preprocessor legacyPreprocessor = new V3Preprocessor();
        final V3LegacyFix legacyFixer = new V3LegacyFix();

        String testCaseString = getTestCaseString(exampleURL);
        JsonObject preprocessed = legacyPreprocessor.load(testCaseString);
        JsonObject fixed = removeV3FixAndFixComments(legacyFixer.fixGeneratorOptions(preprocessed, new JsonObject()));
        JsonObject expected = removeV3FixAndFixComments(CustomGenSettingsSerialization.jankson().load(getTestCaseString(resultURL)));
        assertEquals(expected, fixed);
    }

    private JsonObject removeV3FixAndFixComments(JsonObject in) {
        JsonObject sorted = new JsonObject();
        ArrayList<Map.Entry<String, JsonElement>> keys = new ArrayList<>(in.entrySet());
        for (Map.Entry<String, JsonElement> e : keys) {
            String key = e.getKey();
            if (key.equals("v3fix")) {
                continue;
            }
            String comment = fixComment(in.getComment(key));
            JsonElement jsonElement = e.getValue();
            if (jsonElement instanceof JsonObject) {
                jsonElement = removeV3FixAndFixComments((JsonObject) jsonElement);
            } else if (jsonElement instanceof JsonArray) {
                jsonElement = removeV3FixAndFixComments((JsonArray) jsonElement);
            } else if (jsonElement instanceof JsonPrimitive) {
                if (((JsonPrimitive) jsonElement).getValue() instanceof Number) {
                    jsonElement = new JsonPrimitive(((Number) ((JsonPrimitive) jsonElement).getValue()).doubleValue());
                }
            }
            sorted.put(key, jsonElement, comment);
        }
        return sorted;
    }

    private JsonElement removeV3FixAndFixComments(JsonArray in) {
        JsonArray sorted = new JsonArray();
        for (int i = 0; i < in.size(); i++) {
            JsonElement jsonElement = in.get(i);
            String comment = fixComment(in.getComment(i));
            if (jsonElement instanceof JsonObject) {
                jsonElement = removeV3FixAndFixComments((JsonObject) jsonElement);
            } else if (jsonElement instanceof JsonArray) {
                jsonElement = removeV3FixAndFixComments((JsonArray) jsonElement);
            } else if (jsonElement instanceof JsonPrimitive) {
                if (((JsonPrimitive) jsonElement).getValue() instanceof Number) {
                    jsonElement = new JsonPrimitive(((Number) ((JsonPrimitive) jsonElement).getValue()).doubleValue());
                }
            }
            sorted.add(jsonElement, comment);
        }
        return sorted;
    }

    @Nullable
    private String fixComment(@Nullable String comment) {
        if (comment == null) return null;
        // jankson trims comments when deserializing, but fixer doesn't
        // this is expected difference so ignore it
        String[] split = comment.split("\n");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return String.join("\n", split);
    }

    @Test
    public void testCustomGeneratorSettingsFixerCC655() throws IOException, SyntaxError {
        runTest(testCase1, testCase1ExpectedResult);
    }

    @Test
    public void testCustomGeneratorSettingsFixerCC854MultiLayer() throws IOException, SyntaxError {
        runTest(testCase2, testCase2ExpectedResult);
    }
}
