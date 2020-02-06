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

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

import java.util.function.Function;

public class V5Fix implements IJsonFix {


    private final JsonTransformer<Function<JsonObject, JsonObject>> transformer = JsonTransformer.<Function<JsonObject, JsonObject>>builder("V4 -> V5")
            .drop("v3fix")
            .valueTransform("version", (e, ctx) -> new JsonPrimitive(5))
            .passthroughFor(
                    "waterLevel",
                    "caves",
                    "strongholds", "alternateStrongholdsPositions",
                    "villages", "mineshafts", "temples", "oceanMonuments", "woodlandMansions", "ravines",
                    "dungeons", "dungeonCount",
                    "lakes",
                    "lavaOceans",
                    "biome", "biomeSize", "riverSize",
                    "heightVariationFactor", "specialHeightVariationFactorBelowAverageY", "heightVariationOffset", "heightFactor", "heightOffset",
                    "depthNoiseFactor", "depthNoiseOffset", "depthNoiseFrequencyX", "depthNoiseFrequencyZ", "depthNoiseOctaves",
                    "selectorNoiseFactor", "selectorNoiseOffset",
                    "selectorNoiseFrequencyX", "selectorNoiseFrequencyY", "selectorNoiseFrequencyZ", "selectorNoiseOctaves",
                    "lowNoiseFactor", "lowNoiseOffset", "lowNoiseFrequencyX", "lowNoiseFrequencyY", "lowNoiseFrequencyZ", "lowNoiseOctaves",
                    "highNoiseFactor", "highNoiseOffset", "highNoiseFrequencyX", "highNoiseFrequencyY", "highNoiseFrequencyZ", "highNoiseOctaves",
                    "expectedBaseHeight", "expectedHeightVariation", "actualHeight",
                    "replacerConfig"
            )
            .objectArrayTransform("standardOres", JsonTransformer.passthroughEntry("standardOres"),
                    JsonTransformer.<JsonTransformer.CombinedContext<Function<JsonObject, JsonObject>>>builder("V4 -> V5 (standard ores)")
                            .passthroughFor("blockstate", "biomes", "spawnSize", "spawnTries", "spawnProbability", "minHeight", "maxHeight")
                            .setPrimitive("generateWhen", ctx -> null)
                            .transform("placeBlockWhen", this::transformGenInBlockstates, "genInBlockstates")
                            .build())
            .objectArrayTransform("periodicGaussianOres", JsonTransformer.passthroughEntry("periodicGaussianOres"),
                    JsonTransformer.<JsonTransformer.CombinedContext<Function<JsonObject, JsonObject>>>builder("V4 -> V5 (standard ores)")
                            .passthroughFor("blockstate", "biomes", "spawnSize", "spawnTries", "spawnProbability", "minHeight", "maxHeight",
                                    "heightMean", "heightStdDeviation", "heightSpacing")
                            .setPrimitive("generateWhen", ctx -> null)
                            .transform("placeBlockWhen", this::transformGenInBlockstates, "genInBlockstates")
                            .build())
            .valueTransform("cubeAreas", this::convertCubeAreas)
            .build();

    private void transformGenInBlockstates(JsonObject oldRoot, JsonObject newRoot, JsonTransformer.CombinedContext<Function<JsonObject, JsonObject>> ctx) {
        JsonElement oldValue = oldRoot.get("genInBlockstates");
        if (oldValue instanceof JsonNull) {
            newRoot.put("placeBlockWhen", JsonNull.INSTANCE, oldRoot.getComment("genInBlockstates"));
            return;
        }
        JsonObject placeBlockWhen = new JsonObject();
        placeBlockWhen.put("x", new JsonPrimitive(0.0));
        placeBlockWhen.put("y", new JsonPrimitive(0.0));
        placeBlockWhen.put("z", new JsonPrimitive(0.0));
        placeBlockWhen.put("blocks", oldValue);
        newRoot.put("placeBlockWhen", placeBlockWhen, oldRoot.getComment("genInBlockstates"));
    }

    @Override
    public JsonObject fix(Function<JsonObject, JsonObject> fixerFunction, JsonObject oldRoot) {
        return transformer.transform(oldRoot, fixerFunction);
    }

    private JsonElement convertCubeAreas(JsonElement oldCubeAreasElement, Function<JsonObject, JsonObject> fixer) {
        JsonArray newCubeAreas = new JsonArray();
        JsonArray oldCubeAreas = (JsonArray) oldCubeAreasElement;
        for (int i = 0; i < oldCubeAreas.size(); i++) {
            JsonArray oldLayer = (JsonArray) oldCubeAreas.get(i);
            JsonArray newLayer = new JsonArray();

            newLayer.add(oldLayer.get(0), oldLayer.getComment(0));

            JsonObject fixedLayer = fixer.apply((JsonObject) oldLayer.get(1));
            newLayer.add(fixedLayer, oldLayer.getComment(1));

            newCubeAreas.add(newLayer, oldCubeAreas.getComment(i));
        }
        return newCubeAreas;
    }

}
