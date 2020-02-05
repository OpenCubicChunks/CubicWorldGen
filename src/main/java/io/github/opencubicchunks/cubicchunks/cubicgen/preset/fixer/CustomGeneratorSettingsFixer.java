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
package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class CustomGeneratorSettingsFixer {

    public static final int LATEST = 4;
    public static final CustomGeneratorSettingsFixer INSTANCE = new CustomGeneratorSettingsFixer();

    private final V3Preprocessor legacyPreprocessor = new V3Preprocessor();
    private final V3LegacyFix legacyFixer = new V3LegacyFix();
    private final List<IJsonFix> fixers = Arrays.asList(
            null, //0
            null, //1
            null, //2, versions 0-2 aren't specified in json, and are very inconsistent, handled by v3
            null, //3, special handling due to quirks
            new V4Fix()
    );

    private CustomGeneratorSettingsFixer() {
    }

    public JsonObject fixJson(String json) {
        // when creating a world without configuring preset, the string is empty. Use defaults
        if (json.isEmpty()) {
            return CustomGeneratorSettings.defaults().toJsonObject();
        }
        JsonObject obj = legacyPreprocessor.load(json);
        if (isUpToDate(obj)) {
            return obj;
        }
        return fixJsonWithLegacy(obj, null);
    }

    // Note: lastCwgVersion is only given by MixinSaveHandler, as this is the first time conversion happens
    // then the preset is saved back, already converted
    // so there is no need for any other code to give valid cwg version data
    // this is only used to determine whether river and biome size options should be considered valid
    public String fixJsonString(String json, @Nullable String lastCwgVersion) {
        // when creating a world without configuring preset, the string is empty
        if (json.isEmpty()) {
            return CustomGeneratorSettings.defaults().toJsonObject().toJson(CustomGenSettingsSerialization.OUT_GRAMMAR);
        }
        JsonObject obj = legacyPreprocessor.load(json);
        if (isUpToDate(obj)) {
            return obj.toJson(CustomGenSettingsSerialization.OUT_GRAMMAR);
        }
        JsonObject newObj = fixJsonWithLegacy(obj, lastCwgVersion);
        return newObj.toJson(CustomGenSettingsSerialization.OUT_GRAMMAR);
    }

    public CustomGeneratorSettings fixPreset(String json) throws DeserializationException {
        if (json.isEmpty()) {
            return CustomGeneratorSettings.defaults();
        }
        return CustomGenSettingsSerialization.jankson().fromJsonCarefully(fixJson(json), CustomGeneratorSettings.class);
    }

    JsonObject fixJsonNew(JsonObject toFix) throws UnsupportedPresetException {
        return fixJsonWithLegacy(toFix, null);
    }

    private boolean isUpToDate(JsonObject json) {
        return isConvertedTo(json, LATEST);
    }

    private boolean isConvertedTo(JsonObject json, int version) {
        if (version <= 3) {
            throw new IllegalArgumentException("Version: " + version);
        }
        if (!json.containsKey("version")) {
            return false;
        }
        return ((JsonPrimitive) json.get("version")).asInt(0) == version;
    }

    private JsonObject fixJsonWithLegacy(JsonObject toFix, @Nullable String lastCwgVersion) throws UnsupportedPresetException {
        return fixJsonWithLegacy(toFix, new JsonObject(), lastCwgVersion);
    }

    private JsonObject fixJsonWithLegacy(JsonObject toFix, @Nullable JsonObject parent, @Nullable String lastCwgVersion) throws UnsupportedPresetException {
        int v = getVersion(toFix);
        boolean v3fix = toFix.getOrDefault("v3fix", JsonPrimitive.FALSE).equals(JsonPrimitive.TRUE);
        if (v <= 3 && !v3fix && parent == null) {
            throw new UnsupportedPresetException("V3 and older layers are not supported in V4+ presets");
        }
        if (v <= 3 && !v3fix) { // reprocess existing V3, some V3 presets aren't proper output of V3 fixer
            toFix = legacyFixer.fixGeneratorOptions(toFix, parent, lastCwgVersion);
        }
        for (int i = v + 1; i < fixers.size(); i++) {
            IJsonFix fixer = fixers.get(i);
            if (fixer != null) {
                toFix = fixer.fix(this::fixJsonNew, toFix);
            }
        }
        return toFix;
    }

    private int getVersion(JsonObject obj) {
        JsonElement e = obj.get("version");
        if (e == null) {
            return 0;
        }
        return ((JsonPrimitive) e).asInt(3);
    }
}
