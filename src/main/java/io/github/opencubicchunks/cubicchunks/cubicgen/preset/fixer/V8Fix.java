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
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;

import java.util.function.Function;

public class V8Fix implements IJsonFix {

    private final JsonTransformer<Function<JsonObject, JsonObject>> transformer = JsonTransformer.<Function<JsonObject, JsonObject>>builder("V7 -> "
                    + "V8")
            .valueTransform("version", (e, ctx) -> new JsonPrimitive(8))
            .passthroughFor(
                    "strongholds", "alternateStrongholdsPositions",
                    "villages", "mineshafts", "temples", "oceanMonuments", "woodlandMansions", "ravines",
                    "dungeons", "dungeonCount",
                    "standardOres",
                    "periodicGaussianOres",
                    "biome", "biomeSize", "riverSize",
                    "heightVariationFactor", "specialHeightVariationFactorBelowAverageY", "heightVariationOffset", "heightFactor", "heightOffset",
                    "depthNoiseFactor", "depthNoiseOffset", "depthNoiseFrequencyX", "depthNoiseFrequencyZ", "depthNoiseOctaves",
                    "selectorNoiseFactor", "selectorNoiseOffset",
                    "selectorNoiseFrequencyX", "selectorNoiseFrequencyY", "selectorNoiseFrequencyZ", "selectorNoiseOctaves",
                    "lowNoiseFactor", "lowNoiseOffset", "lowNoiseFrequencyX", "lowNoiseFrequencyY", "lowNoiseFrequencyZ", "lowNoiseOctaves",
                    "highNoiseFactor", "highNoiseOffset", "highNoiseFrequencyX", "highNoiseFrequencyY", "highNoiseFrequencyZ", "highNoiseOctaves",
                    "expectedBaseHeight", "expectedHeightVariation", "actualHeight",
                    "lakes",
                    "replacers"
            )
            .valueTransform("caves", this::transformCavesConfig)
            .valueTransform("cubeAreas", this::convertCubeAreas)
            .build();

    private JsonElement transformCavesConfig(JsonElement jsonElement, Function<JsonObject, JsonObject> jsonObjectJsonObjectFunction) {
        if(((JsonPrimitive)jsonElement).asBoolean(true)) {
            JsonObject caveConfig = new JsonObject();
            caveConfig.put("caveBlock", JsonObjectView.empty().put("Name", "minecraft:air").object());
            caveConfig.put("caveRarity", new JsonPrimitive(16 * 7 / (2 * 2 * 2)));
            caveConfig.put("maxInitNodes", new JsonPrimitive(14));
            caveConfig.put("largeNodeRarity", new JsonPrimitive(4));
            caveConfig.put("largeNodeMaxBranches", new JsonPrimitive(4));
            caveConfig.put("bigCaveRarity", new JsonPrimitive(10));
            caveConfig.put("caveSizeAdd", new JsonPrimitive(1.5));
            caveConfig.put("steepStepRarity", new JsonPrimitive(6));
            caveConfig.put("flattenFactor", new JsonPrimitive(0.699999988079071));
            caveConfig.put("steeperFlattenFactor", new JsonPrimitive(0.9200000166893005));
            caveConfig.put("directionChangeFactor", new JsonPrimitive(0.10000000149011612));
            caveConfig.put("prevHorizDirectionChangeWeight", new JsonPrimitive(0.75));
            caveConfig.put("prevVertDirectionChangeWeight", new JsonPrimitive(0.8999999761581421));
            caveConfig.put("maxAddDirectionChangeHoriz", new JsonPrimitive(4.0));
            caveConfig.put("maxAddDirectionChangeVert", new JsonPrimitive(2.0));
            caveConfig.put("carveStepRarity", new JsonPrimitive(4));
            caveConfig.put("caveFloorDepth", new JsonPrimitive(-0.7));

            JsonArray replaceable = new JsonArray();
            replaceable.add(new JsonPrimitive("minecraft:grass"));
            replaceable.add(new JsonPrimitive("minecraft:dirt"));
            replaceable.add(new JsonPrimitive("minecraft:stone"));
            caveConfig.put("isBlockReplaceable", replaceable);


            JsonArray caves = new JsonArray();
            caves.add(caveConfig);
            return caves;
        } else {
            return new JsonArray();
        }
    }

    @Override public JsonObject fix(Function<JsonObject, JsonObject> fixerFunction, JsonObject oldRoot) {
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
