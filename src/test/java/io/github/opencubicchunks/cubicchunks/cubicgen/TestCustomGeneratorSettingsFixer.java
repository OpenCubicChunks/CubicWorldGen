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

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.IJsonFix;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.V3LegacyFix;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.V3Preprocessor;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.V4Fix;
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
    private static final String test1v0 = "/assets/cubicworldgen/presets/test1_CC655_random_preset_V0.json5";
    private static final String test1v3 = "/assets/cubicworldgen/presets/test1_CC655_random_preset_V3.json5";
    private static final String test1v4 = "/assets/cubicworldgen/presets/test1_CC655_random_preset_V4.json5";
    private static final String test1latest = test1v4;

    // Multi-layer CubicChunks v0.0.854 example preset
    private static final String test2oldv3 = "/assets/cubicworldgen/presets/test2_CC854_MineCrakLayersExample_OldV3.json5";
    private static final String test2v3 = "/assets/cubicworldgen/presets/test2_CC854_MineCrakLayersExample_V3.json5";
    private static final String test2v4 = "/assets/cubicworldgen/presets/test2_CC854_MineCrakLayersExample_V4.json5";
    private static final String test2latest = test2v4;

    // Zekkens Honeycombed underground world - CC809
    private static final String test3oldv3 = "/assets/cubicworldgen/presets/test3_CC808_ZekkensHoneycombUnderground_OldV3.json5";
    private static final String test3v3 = "/assets/cubicworldgen/presets/test3_CC808_ZekkensHoneycombUnderground_V3.json5";
    private static final String test3v4 = "/assets/cubicworldgen/presets/test3_CC808_ZekkensHoneycombUnderground_V4.json5";
    private static final String test3latest = test3v4;

    // IslandsOre preset - CC80X
    private static final String test4oldv3 = "/assets/cubicworldgen/presets/test4_CC80X_IslandsOre_OldV3.json5";
    private static final String test4v3 = "/assets/cubicworldgen/presets/test4_CC80X_IslandsOre_V3.json5";
    private static final String test4v4 = "/assets/cubicworldgen/presets/test4_CC80X_IslandsOre_V4.json5";
    private static final String test4latest = test4v4;

    // Wozat's Realistic Mountain preset - CC77X
    private static final String test5oldv3 = "/assets/cubicworldgen/presets/test5_CC77X_WozatRealisticMountains_OldV3.json5";
    private static final String test5v3 = "/assets/cubicworldgen/presets/test5_CC77X_WozatRealisticMountains_V3.json5";
    private static final String test5v4 = "/assets/cubicworldgen/presets/test5_CC77X_WozatRealisticMountains_V4.json5";
    private static final String test5latest = test5v4;

    // First test server preset - CC6XX
    private static final String test6oldv3 = "/assets/cubicworldgen/presets/test6_CC6XX_FirstTestServer_OldV3.json5";
    private static final String test6v3 = "/assets/cubicworldgen/presets/test6_CC6XX_FirstTestServer_V3.json5";
    private static final String test6v4 = "/assets/cubicworldgen/presets/test6_CC6XX_FirstTestServer_V4.json5";
    private static final String test6latest = test6v4;

    // MineCrak layers example - CC843
    private static final String test7oldv3 = "/assets/cubicworldgen/presets/test7_CC843_MineCrakLayersExample_OldV3.json5";
    private static final String test7v3 = "/assets/cubicworldgen/presets/test7_CC843_MineCrakLayersExample_V3.json5";
    private static final String test7v4 = "/assets/cubicworldgen/presets/test7_CC843_MineCrakLayersExample_V4.json5";
    private static final String test7latest = test7v4;

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

    private void runAll(String start, String latest) throws IOException, SyntaxError {
        String testCaseString = getTestCaseString(start);
        String expectedString = getTestCaseString(latest);

        JsonObject fixed = removeExpectedDifferences(CustomGeneratorSettingsFixer.INSTANCE.fixJson(testCaseString));
        JsonObject expected = removeExpectedDifferences(CustomGenSettingsSerialization.jankson().load(expectedString));
        assertEquals(expected, fixed);

        // make sure that it also matches the second time (test that there is no internal state that changes results)
        fixed = removeExpectedDifferences(CustomGeneratorSettingsFixer.INSTANCE.fixJson(testCaseString));
        expected = removeExpectedDifferences(CustomGenSettingsSerialization.jankson().load(expectedString));
        assertEquals(expected, fixed);
    }

    private void runToV3(String examplePath, String resutPath) throws IOException, SyntaxError {
        final V3Preprocessor legacyPreprocessor = new V3Preprocessor();
        final V3LegacyFix legacyFixer = new V3LegacyFix();

        String testCaseString = getTestCaseString(examplePath);
        JsonObject preprocessed = legacyPreprocessor.load(testCaseString);
        JsonObject fixed = removeExpectedDifferences(legacyFixer.fixGeneratorOptions(preprocessed, new JsonObject(), null));
        JsonObject expected = removeExpectedDifferences(Jankson.builder().build().load(getTestCaseString(resutPath)));
        assertEquals(expected, fixed);

        // make sure that it also matches the second time (test that there is no internal state that changes results)
        preprocessed = legacyPreprocessor.load(testCaseString);
        fixed = removeExpectedDifferences(legacyFixer.fixGeneratorOptions(preprocessed, new JsonObject(), null));
        expected = removeExpectedDifferences(Jankson.builder().build().load(getTestCaseString(resutPath)));
        assertEquals(expected, fixed);
    }

    private void runToV4(String example, String result) throws IOException, SyntaxError {
        final V4Fix fixer = new V4Fix();

        String testCaseString = getTestCaseString(example);
        JsonObject fixed = removeExpectedDifferences(applyFixer(fixer, (Jankson.builder().build().load(testCaseString))));
        JsonObject expected = removeExpectedDifferences(CustomGenSettingsSerialization.jankson().load(getTestCaseString(result)));
        assertEquals(expected, fixed);

        fixed = removeExpectedDifferences(applyFixer(fixer, (Jankson.builder().build().load(testCaseString))));
        expected = removeExpectedDifferences(CustomGenSettingsSerialization.jankson().load(getTestCaseString(result)));
        assertEquals(expected, fixed);
    }

    private JsonObject applyFixer(IJsonFix fixer, JsonObject toFix) {
        return fixer.fix(obj -> applyFixer(fixer, obj), toFix);
    }

    private JsonObject removeExpectedDifferences(JsonObject in) {
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
                jsonElement = removeExpectedDifferences((JsonObject) jsonElement);
            } else if (jsonElement instanceof JsonArray) {
                jsonElement = removeExpectedDifferences((JsonArray) jsonElement);
            } else if (jsonElement instanceof JsonPrimitive) {
                if (((JsonPrimitive) jsonElement).getValue() instanceof Number) {
                    jsonElement = new JsonPrimitive(((Number) ((JsonPrimitive) jsonElement).getValue()).doubleValue());
                }
            }
            sorted.put(key, jsonElement, comment);
        }
        return sorted;
    }

    private JsonElement removeExpectedDifferences(JsonArray in) {
        JsonArray sorted = new JsonArray();
        for (int i = 0; i < in.size(); i++) {
            JsonElement jsonElement = in.get(i);
            String comment = fixComment(in.getComment(i));
            if (jsonElement instanceof JsonObject) {
                jsonElement = removeExpectedDifferences((JsonObject) jsonElement);
            } else if (jsonElement instanceof JsonArray) {
                jsonElement = removeExpectedDifferences((JsonArray) jsonElement);
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

    // NOTE: currently (intellij 2019.3) intellij doesn't seem to be able to deal with assertEquals
    // output with *that* long strings and doesn't offer a builtin diff tool for that.
    // so here is a bash command that shows a "good enough" diff from the raw output (stored in diffinput.txt):
    // diff --color=always -u <(awk -F '(<{|}>)' '{print $2}' diffinput.txt | sed 's/,/,\n/g') <(awk -F '(<{|}>)' '{print $4}' diffinput.txt | sed 's/,/,\n/g')

    // CC655 preset

    @Test
    public void testV3FixerCC655() throws IOException, SyntaxError {
        runToV3(test1v0, test1v3);
    }

    @Test
    public void testIdentityV3FixerCC655() throws IOException, SyntaxError {
        runToV3(test1v3, test1v3);
    }

    @Test
    public void testV4FixerCC655() throws IOException, SyntaxError {
        runToV4(test1v3, test1v4);
    }

    @Test
    public void testAllFixersCC655() throws IOException, SyntaxError {
        runAll(test1v0, test1latest);
    }

    // MineCrak layers example - CC854
    @Test
    public void testV3FixerCC854MultiLayer() throws IOException, SyntaxError {
        runToV3(test2oldv3, test2v3);
    }

    @Test
    public void testIdentityV3FixerCC854MultiLayer() throws IOException, SyntaxError {
        runToV3(test2v3, test2v3);
    }

    @Test
    public void testV4FixerCC854MultiLayer() throws IOException, SyntaxError {
        runToV4(test2v3, test2v4);
    }

    // Honeycombed Underground by Zekken - CC808

    @Test
    public void testV3FixerCC808ZekkensHoneycombUnderground() throws IOException, SyntaxError {
        runToV3(test3oldv3, test3v3);
    }

    @Test
    public void testIdentityV3FixerCC808ZekkensHoneycombUnderground() throws IOException, SyntaxError {
        runToV3(test3v3, test3v3);
    }

    @Test
    public void testV4FixerCC808ZekkensHoneycombUnderground() throws IOException, SyntaxError {
        runToV4(test3v3, test3v4);
    }

    // IslandsOre - CC80X

    @Test
    public void testV3FixerCC80XIslandsOre() throws IOException, SyntaxError {
        runToV3(test4oldv3, test4v3);
    }

    @Test
    public void testIdentityV3FixerCC80XIslandsOre() throws IOException, SyntaxError {
        runToV3(test4v3, test4v3);
    }

    @Test
    public void testV4FixerCC80XIslandsOre() throws IOException, SyntaxError {
        runToV3(test4v3, test4v4);
    }

    // Wozat's realistic mountain - CC77X

    @Test
    public void testV3FixerCC77XRealisticMountains() throws IOException, SyntaxError {
        runToV3(test5oldv3, test5v3);
    }

    @Test
    public void testIdentityV3FixerCC77XRealisticMountains() throws IOException, SyntaxError {
        runToV3(test5v3, test5v3);
    }

    @Test
    public void testV4FixerCC8XXRealisticMountains() throws IOException, SyntaxError {
        runToV4(test5v3, test5v4);
    }

    // First test server - CC6XX

    @Test
    public void testV3FixerCC6XXFirstTestServer() throws IOException, SyntaxError {
        runToV3(test6oldv3, test6v3);
    }

    @Test
    public void testIdentityV3FixerCC6XXFirstTestServer() throws IOException, SyntaxError {
        runToV3(test6v3, test6v3);
    }

    @Test
    public void testV4FixerCC6XXFirstTestServer() throws IOException, SyntaxError {
        runToV4(test6v3, test6v4);
    }

    // MineCrak layers example - CC843

    @Test
    public void testV3FixerCC843MineCrakLayersExample() throws IOException, SyntaxError {
        runToV3(test7oldv3, test7v3);
    }

    @Test
    public void testIdentityV3FixerCC843MineCrakLayersExample() throws IOException, SyntaxError {
        runToV3(test7v3, test7v3);
    }

    @Test
    public void testV4FixerCC843MineCrakLayersExample() throws IOException, SyntaxError {
        runToV4(test7v3, test7v4);
    }
}
