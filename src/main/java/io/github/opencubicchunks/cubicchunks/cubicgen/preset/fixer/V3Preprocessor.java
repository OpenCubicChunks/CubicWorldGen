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

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;

/**
 * Converts old v3 mappings json file, into a jankson JsonObject.
 * It can't be loaded directly because:
 *  * GSON is not strictly a subset of what jankson supports
 *  * jankson doesn't (yet) support dealing with duplicate keys,
 *  which were used as a workaround for lack of comment support in gson
 *
 * This pre-processor, will automatically try to convert known commonly used comment
 * formats to proper comments, and put all redundant key duplicates into comments.
 *
 * Because neither JsonObject implementation supports duplicate keys, and the
 * transformation can't be done in place while reading, the preprocessor has a custom
 * minimalistic version of JsonObject (ParsedJson) and a json loader.
 *
 * V3 preprocessor will attempt to ONLY process json input that is v3 or earlier (unspecified).
 * This is done to avoid the potential need to support (potentially mutually exclusive) both gson
 * and jankson quirks when loading the same file.
 *
 * Loading V3 layers in V4+ preset is currently going to be explicitly unsupported, because if the
 * special inheritance mechanisms in V3, that are intentionally removed from V4+ as the reason for it
 * no longer applies (64k character limit for the whole preset, coming from DataInput/OutputStream limit)
 */
public class V3Preprocessor {

    public JsonObject load(String json) throws PresetLoadError {
        Jankson jankson = blue.endless.jankson.Jankson
                .builder()
                .build();
        try {
            return jankson.load(json);
        } catch (SyntaxError err) {
            String msg = "Unable to load preset json due to syntax errors:\n" +
                    err.getMessage() + "\n" +
                    err.getLineMessage();
            throw new PresetLoadError(msg, err);
        }
    }
}
