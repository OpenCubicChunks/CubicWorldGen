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
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.JsonTransformer.CombinedContext;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class V3LegacyFix {

    private final double[] spawnSizes = {33, 33, 33, 33, 33, 17, 9, 9, 8, 8, 3, 7, 20, 7};
    private final double[] spawnTries = {10, 8, 10, 10, 10, 20, 20, 2, 8, 1, 11, 7, 2, 1};
    private final double[] spawnProbabilities = {
            1f / (256f / 16),
            1f / (256f / 16),
            256f / 80f / (256f / 16),
            256f / 80f / (256f / 16),
            256f / 80f / (256f / 16),
            256f / 128f / (256f / 6),
            256f / 64f / (256f / 16),
            256f / 32f / (256f / 16),
            256f / 16f / (256f / 16),
            256f / 16f / (256f / 16),
            0.5f * 256f / 28f / (256f / 16),
            256f / 64f / (256f / 16),
            256f / 32f / (256f / 16),
            0.933307775f
    };
    private final double[] minHeights = {
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            -0.5f,
            Double.NEGATIVE_INFINITY
    };

    private final double[] maxHeights = {
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            (80f - 64f) / 64f,
            (80f - 64f) / 64f,
            (80f - 64f) / 64f,
            1,
            0,
            -0.5f,
            -0.75f,
            -0.75f,
            0,
            -0.5f,
            0.25,
            -0.5f
    };

    private final String[] standard = {"dirt", "gravel", "granite", "diorite", "andesite", "coalOre", "ironOre",
            "goldOre", "redstoneOre", "diamondOre", "hillsEmeraldOre", "hillsSilverfishStone", "mesaAddedGoldOre", "lapisLazuli"};

    private final String[] legacyOreFieldNames = new String[(standard.length - 1) * 5];
    private final String[] legacyOreFieldNamesPeriodic = new String[8];

    {
        int i = 0;
        for (int i1 = 0; i1 < standard.length - 1; i1++) {
            String ore = standard[i1];
            legacyOreFieldNames[i++] = ore + "SpawnSize";
            legacyOreFieldNames[i++] = ore + "SpawnTries";
            legacyOreFieldNames[i++] = ore + "SpawnProbability";
            legacyOreFieldNames[i++] = ore + "SpawnMinHeight";
            legacyOreFieldNames[i++] = ore + "SpawnMaxHeight";
        }
        String ore = standard[standard.length - 1];
        i = 0;
        legacyOreFieldNamesPeriodic[i++] = ore + "SpawnSize";
        legacyOreFieldNamesPeriodic[i++] = ore + "SpawnTries";
        legacyOreFieldNamesPeriodic[i++] = ore + "SpawnProbability";
        legacyOreFieldNamesPeriodic[i++] = ore + "SpawnMinHeight";
        legacyOreFieldNamesPeriodic[i++] = ore + "SpawnMaxHeight";
        legacyOreFieldNamesPeriodic[i++] = ore + "HeightMean";
        legacyOreFieldNamesPeriodic[i++] = ore + "HeightStdDeviation";
        legacyOreFieldNamesPeriodic[i] = ore + "HeightSpacing";

    }

    private final JsonObject[] standardBlockstates = new JsonObject[14];

    {
        JsonObject[] blockstates = standardBlockstates;

        JsonObject dirt = new JsonObject();
        JsonObject dirtProps = new JsonObject();
        dirtProps.put("variant", new JsonPrimitive("dirt"), null);
        dirtProps.put("snowy", new JsonPrimitive("false"), null);
        dirt.put("Name", new JsonPrimitive("minecraft:dirt"), null);
        dirt.put("Properties", dirtProps, null);
        blockstates[0] = dirt;

        JsonObject gravel = new JsonObject();
        gravel.put("Name", new JsonPrimitive("minecraft:gravel"));
        blockstates[1] = gravel;

        JsonObject granite = new JsonObject();
        JsonObject graniteProps = new JsonObject();
        graniteProps.put("variant", new JsonPrimitive("granite"));
        granite.put("Name", new JsonPrimitive("minecraft:stone"), null);
        granite.put("Properties", graniteProps, null);
        blockstates[2] = granite;

        JsonObject diorite = new JsonObject();
        JsonObject dioriteProps = new JsonObject();
        dioriteProps.put("variant", new JsonPrimitive("diorite"));
        diorite.put("Name", new JsonPrimitive("minecraft:stone"), null);
        diorite.put("Properties", dioriteProps, null);
        blockstates[3] = diorite;

        JsonObject andesite = new JsonObject();
        JsonObject andesiteProps = new JsonObject();
        andesiteProps.put("variant", new JsonPrimitive("andesite"));
        andesite.put("Name", new JsonPrimitive("minecraft:stone"), null);
        andesite.put("Properties", andesiteProps, null);
        blockstates[4] = andesite;

        JsonObject coalOre = new JsonObject();
        coalOre.put("Name", new JsonPrimitive("minecraft:coal_ore"));
        blockstates[5] = coalOre;

        JsonObject ironOre = new JsonObject();
        ironOre.put("Name", new JsonPrimitive("minecraft:iron_ore"));
        blockstates[6] = ironOre;

        JsonObject goldOre = new JsonObject();
        goldOre.put("Name", new JsonPrimitive("minecraft:gold_ore"));
        blockstates[7] = goldOre;

        JsonObject redstoneOre = new JsonObject();
        redstoneOre.put("Name", new JsonPrimitive("minecraft:redstone_ore"));
        blockstates[8] = redstoneOre;

        JsonObject diamondOre = new JsonObject();
        diamondOre.put("Name", new JsonPrimitive("minecraft:diamond_ore"));
        blockstates[9] = diamondOre;

        JsonObject emeraldOre = new JsonObject();
        emeraldOre.put("Name", new JsonPrimitive("minecraft:emerald_ore"));
        blockstates[10] = emeraldOre;

        JsonObject monsterEgg = new JsonObject();
        JsonObject monsterEggProps = new JsonObject();
        monsterEggProps.put("variant", new JsonPrimitive("stone"));
        monsterEgg.put("Name", new JsonPrimitive("minecraft:monster_egg"), null);
        monsterEgg.put("Properties", monsterEggProps, null);
        blockstates[11] = monsterEgg;

        blockstates[12] = blockstates[7].clone();

        JsonObject lapisOre = new JsonObject();
        lapisOre.put("Name", new JsonPrimitive("minecraft:lapis_ore"));
        blockstates[13] = lapisOre;
    }

    private final JsonElement[] standardBiomes = {
            JsonNull.INSTANCE, // dirt
            JsonNull.INSTANCE, // gravel
            JsonNull.INSTANCE, // granite
            JsonNull.INSTANCE, // diorite
            JsonNull.INSTANCE, // andesite
            JsonNull.INSTANCE, // coal
            JsonNull.INSTANCE, // iron
            JsonNull.INSTANCE, // gold
            JsonNull.INSTANCE, // redstone
            JsonNull.INSTANCE, // diamond
            strArray("minecraft:extreme_hills", "minecraft:smaller_extreme_hills", "minecraft:extreme_hills_with_trees",
                    "minecraft:mutated_extreme_hills", "minecraft:mutated_extreme_hills_with_trees"), // emerald
            strArray("minecraft:extreme_hills", "minecraft:smaller_extreme_hills", "minecraft:extreme_hills_with_trees",
                    "minecraft:mutated_extreme_hills", "minecraft:mutated_extreme_hills_with_trees"), // monster egg
            strArray("minecraft:mesa", "minecraft:mesa_clear_rock", "minecraft:mesa_rock", "minecraft:mutated_mesa",
                    "minecraft:mutated_mesa_clear_rock", "minecraft:mutated_mesa_rock"), // mesa gold
            JsonNull.INSTANCE
    };

    private static JsonElement strArray(String... arr) {
        JsonArray json = new JsonArray();
        for (String s : arr) {
            json.add(new JsonPrimitive(s));
        }
        return json;
    }

    private void defaultHandler(JsonTransformer.Builder<JsonObject> builder, String name, Object defaultPrimitive) {
        builder.transform(name, (oldRoot, newRoot, context) -> copyDirect(name, oldRoot, context, newRoot, defaultPrimitive));
    }

    private void defaultHandlerCombinedCtx(JsonTransformer.Builder<CombinedContext<JsonObject>> builder, String name, Object defaultPrimitive) {
        builder.transform(name, (oldRoot, newRoot, context) -> copyDirect(name, oldRoot, context.context(), newRoot, defaultPrimitive));
    }

    private final JsonTransformer<JsonObject> transformer;

    {
        JsonTransformer.Builder<JsonObject> builder = JsonTransformer.builder("LegacyV0-3 -> V3");

        builder.transform("version", (oldRoot, newRoot, context) -> newRoot.put("version", new JsonPrimitive(3)));
        builder.transform("v3fix", (oldRoot, newRoot, context) -> newRoot.put("v3fix", JsonPrimitive.TRUE), "version");
        defaultHandler(builder, "waterLevel", 63);
        defaultHandler(builder, "caves", true);
        defaultHandler(builder, "strongholds", true);
        defaultHandler(builder, "alternateStrongholdsPositions", false);
        defaultHandler(builder, "villages", true);
        defaultHandler(builder, "mineshafts", true);
        defaultHandler(builder, "temples", true);
        defaultHandler(builder, "oceanMonuments", true);
        defaultHandler(builder, "woodlandMansions", true);
        defaultHandler(builder, "ravines", true);
        defaultHandler(builder, "dungeons", true);
        defaultHandler(builder, "dungeonCount", 7);
        defaultHandler(builder, "waterLakes", true);
        defaultHandler(builder, "waterLakeRarity", 4);
        defaultHandler(builder, "lavaLakes", true);
        defaultHandler(builder, "lavaLakeRarity", 8);
        defaultHandler(builder, "aboveSeaLavaLakeRarity", 13);
        defaultHandler(builder, "lavaOceans", false);
        defaultHandler(builder, "biome", -1);
        defaultHandler(builder, "biomeSize", 4);
        defaultHandler(builder, "riverSize", 4);
        defaultHandler(builder, "heightVariationFactor", 64);
        defaultHandler(builder, "specialHeightVariationFactorBelowAverageY", 0.25);
        defaultHandler(builder, "heightVariationOffset", 0);
        defaultHandler(builder, "heightFactor", 64);
        defaultHandler(builder, "heightOffset", 64);
        defaultHandler(builder, "depthNoiseFactor", 1.024);
        defaultHandler(builder, "depthNoiseOffset", 0);
        defaultHandler(builder, "depthNoiseFrequencyX", 0.0015258789);
        defaultHandler(builder, "depthNoiseFrequencyZ", 0.0015258789);
        defaultHandler(builder, "depthNoiseOctaves", 16);
        defaultHandler(builder, "selectorNoiseFactor", 12.75);
        defaultHandler(builder, "selectorNoiseOffset", 0.5);
        defaultHandler(builder, "selectorNoiseFrequencyX", 0.016709277);
        defaultHandler(builder, "selectorNoiseFrequencyY", 0.008354639);
        defaultHandler(builder, "selectorNoiseFrequencyZ", 0.016709277);
        defaultHandler(builder, "selectorNoiseOctaves", 8);
        defaultHandler(builder, "lowNoiseFactor", 1);
        defaultHandler(builder, "lowNoiseOffset", 0);
        defaultHandler(builder, "lowNoiseFrequencyX", 0.005221649);
        defaultHandler(builder, "lowNoiseFrequencyY", 0.0026108245);
        defaultHandler(builder, "lowNoiseFrequencyZ", 0.005221649);
        defaultHandler(builder, "lowNoiseOctaves", 16);
        defaultHandler(builder, "highNoiseFactor", 1);
        defaultHandler(builder, "highNoiseOffset", 0);
        defaultHandler(builder, "highNoiseFrequencyX", 0.005221649);
        defaultHandler(builder, "highNoiseFrequencyY", 0.0026108245);
        defaultHandler(builder, "highNoiseFrequencyZ", 0.005221649);
        defaultHandler(builder, "highNoiseOctaves", 16);

        String[] oldStdNames = new String[legacyOreFieldNames.length + 1];
        System.arraycopy(legacyOreFieldNames, 0, oldStdNames, 0, legacyOreFieldNames.length);
        oldStdNames[oldStdNames.length - 1] = "standardOres";

        JsonTransformer.Builder<CombinedContext<JsonObject>> stdBuilder =
                JsonTransformer.builder("LegacyV0-3 -> V3 (ores)");
        defaultHandlerCombinedCtx(stdBuilder, "blockstate", null);
        defaultHandlerCombinedCtx(stdBuilder, "spawnSize", 6);
        defaultHandlerCombinedCtx(stdBuilder, "spawnTries", 6);
        defaultHandlerCombinedCtx(stdBuilder, "spawnProbability", 1.0);
        defaultHandlerCombinedCtx(stdBuilder, "minHeight", Double.NEGATIVE_INFINITY);
        defaultHandlerCombinedCtx(stdBuilder, "maxHeight", Double.POSITIVE_INFINITY);
        defaultHandlerCombinedCtx(stdBuilder, "biomes", null);
        defaultHandlerCombinedCtx(stdBuilder, "genInBlockstates", null);
        JsonTransformer<CombinedContext<JsonObject>> stdOreTransformer = stdBuilder.build();
        builder.objectArrayTransform("standardOres", this::copyStandardOres, stdOreTransformer, oldStdNames);

        String[] oldPeriodicNames = new String[legacyOreFieldNamesPeriodic.length + 1];
        System.arraycopy(legacyOreFieldNamesPeriodic, 0, oldPeriodicNames, 0, legacyOreFieldNamesPeriodic.length);
        oldPeriodicNames[oldPeriodicNames.length - 1] = "periodicGaussianOres";

        JsonTransformer.Builder<CombinedContext<JsonObject>> periodicBuilder =
                JsonTransformer.builder("LegacyV0-3 -> V3 (periodic gaussian ores)");
        defaultHandlerCombinedCtx(periodicBuilder, "blockstate", null);
        defaultHandlerCombinedCtx(periodicBuilder, "spawnSize", 6);
        defaultHandlerCombinedCtx(periodicBuilder, "spawnTries", 6);
        defaultHandlerCombinedCtx(periodicBuilder, "spawnProbability", 1.0);
        defaultHandlerCombinedCtx(periodicBuilder, "heightMean", 0.0);
        defaultHandlerCombinedCtx(periodicBuilder, "heightStdDeviation", 1.0);
        defaultHandlerCombinedCtx(periodicBuilder, "heightSpacing", 10.0);
        defaultHandlerCombinedCtx(periodicBuilder, "minHeight", Double.NEGATIVE_INFINITY);
        defaultHandlerCombinedCtx(periodicBuilder, "maxHeight", Double.POSITIVE_INFINITY);
        defaultHandlerCombinedCtx(periodicBuilder, "biomes", null);
        defaultHandlerCombinedCtx(periodicBuilder, "genInBlockstates", null);
        JsonTransformer<CombinedContext<JsonObject>> periodicOreTransformer = periodicBuilder.build();
        builder.objectArrayTransform("periodicGaussianOres", this::copyPeriodicGaussianOres, periodicOreTransformer, oldPeriodicNames);

        builder.transform("expectedBaseHeight", (oldRoot, newRoot, context) ->
                copyDirect("expectedBaseHeight", oldRoot, context, newRoot, computeDefaultExpectedBaseHeight(oldRoot, context)));
        builder.transform("expectedHeightVariation", (oldRoot, newRoot, context) ->
                copyDirect("expectedHeightVariation", oldRoot, context, newRoot, computeDefaultExpectedHeightVariation(oldRoot, context)));
        builder.transform("actualHeight", (oldRoot, newRoot, context) ->
                copyDirect("actualHeight", oldRoot, context, newRoot, computeDefaultActualHeight(oldRoot, context)));

        final BiFunction<String, Object, JsonTransformer.EntryTransform<CombinedContext<CombinedContext<JsonObject>>>> replacerTransform =
                (name, defaultValue) -> (oldRoot, newRoot, context) -> {
                    JsonElement defaultEntry = defaultValue instanceof JsonElement ? (JsonElement) defaultValue : new JsonPrimitive(defaultValue);
                    String tryOldName = "cubicchunks:" + name;
                    String newName = "cubicgen:" + name;
                    JsonElement element = oldRoot.get(tryOldName);
                    String comment = oldRoot.getComment(tryOldName);
                    if (element == null) {
                        element = oldRoot.get(newName);
                        comment = oldRoot.getComment(newName);
                    }
                    if (element == null) {
                        element = defaultEntry;
                    }
                    newRoot.put(newName, element, comment);
                };

        final JsonObject stoneBlock = new JsonObject();
        {
            JsonObject stoneProps = new JsonObject();
            stoneProps.put("variant", new JsonPrimitive("stone"));
            stoneBlock.put("Name", new JsonPrimitive("minecraft:stone"));
            stoneBlock.put("Properties", stoneProps);
        }
        builder.objectTransform("replacerConfig",
                (oldRoot, newRoot, context) ->
                        newRoot.put("replacerConfig", getOrDefault(oldRoot, context, "replacerConfig", new JsonObject()),
                                oldRoot.getComment("replacerConfig")),
                JsonTransformer.<CombinedContext<JsonObject>>builder("LegacyV0-3 -> V3 (replacer config)")
                        .objectTransform("defaults",
                                (oldRoot, newRoot, context) -> newRoot.put("defaults", oldRoot.get("defaults"), oldRoot.getComment("defaults")),
                                JsonTransformer.<CombinedContext<CombinedContext<JsonObject>>>builder("LegacyV0-3 -> V3 (replacer config defaults)")
                                        .transform("cubicgen:biome_fill_noise_octaves",
                                                replacerTransform.apply("biome_fill_noise_octaves", 4.0),
                                                "cubicchunks:biome_fill_noise_octaves", "cubicgen:biome_fill_noise_octaves")
                                        .transform("cubicgen:ocean_block",
                                                this::copyOceanBlock, "cubicchunks:ocean_block", "cubicgen:ocean_block")
                                        .transform("cubicgen:height_scale",
                                                this::copyHeightScale,
                                                "cubicchunks:height_scale",
                                                "cubicgen:height_scale")
                                        .transform("cubicgen:biome_fill_noise_freq",
                                                replacerTransform.apply("biome_fill_noise_freq", 0.0078125),
                                                "cubicchunks:biome_fill_noise_freq",
                                                "cubicgen:biome_fill_noise_freq")
                                        .transform("cubicgen:water_level",
                                                this::copyWaterLevel, "cubicchunks:water_level", "cubicgen:water_level")
                                        .transform("cubicgen:biome_fill_depth_factor",
                                                replacerTransform.apply("biome_fill_depth_factor", 2.3333333333333335),
                                                "cubicchunks:biome_fill_depth_factor",
                                                "cubicgen:biome_fill_depth_factor")
                                        .transform("cubicgen:terrain_fill_block",
                                                replacerTransform.apply("terrain_fill_block", stoneBlock),
                                                "cubicchunks:terrain_fill_block",
                                                "cubicgen:terrain_fill_block")
                                        .transform("cubicgen:mesa_depth",
                                                replacerTransform.apply("mesa_depth", 16),
                                                "cubicchunks:mesa_depth",
                                                "cubicgen:mesa_depth")
                                        .transform("cubicgen:horizontal_gradient_depth_decrease_weight",
                                                replacerTransform.apply("horizontal_gradient_depth_decrease_weight", 1.0),
                                                "cubicchunks:horizontal_gradient_depth_decrease_weight",
                                                "cubicgen:horizontal_gradient_depth_decrease_weight")
                                        .transform("cubicgen:biome_fill_depth_offset",
                                                replacerTransform.apply("biome_fill_depth_offset", 3.0),
                                                "cubicchunks:biome_fill_depth_offset",
                                                "cubicgen:biome_fill_depth_offset")
                                        .transform("cubicgen:height_offset",
                                                this::copyHeightOffset,
                                                "cubicchunks:height_offset",
                                                "cubicgen:height_offset")
                                        .build())
                        .objectTransform("overrides",
                                (oldRoot, newRoot, context) -> newRoot.put("overrides", oldRoot.get("overrides"), oldRoot.getComment("overrides")),
                                JsonTransformer.<CombinedContext<CombinedContext<JsonObject>>>builder("LegacyV0-3 -> V3 (replacer config overrides)")
                                        .defaultTransform((name, element, comment, newRoot, ctx) -> {
                                            String s = name.substring(name.indexOf(":") + 1);
                                            newRoot.put("cubicgen:" + s, element, comment);
                                        })
                                        .build())
                        .build());

        builder.transform("cubeAreas", (oldRoot, newRoot, context) ->
                newRoot.put("cubeAreas", oldRoot.getOrDefault("cubeAreas", new JsonArray()), oldRoot.getComment("cubeAreas")));
        transformer = builder.build();
    }

    public JsonObject fixGeneratorOptions(JsonObject json, @Nullable JsonObject parent, String lastCwgVersion) {
        // biome size and river size have been implemented since CWG 39.
        // presets from before that may have it changed and this has a very significant effect on the world
        boolean hasWorkingBiomeSizes = true;
        // regex matching for something like this: 1.12.2-0.0.X.0 for example
        if (lastCwgVersion != null && lastCwgVersion.matches("1\\.\\d+\\.\\d+-\\d+\\.\\d+\\.\\d+\\.\\d+(-.*)?")) {
            String[] parts = lastCwgVersion.split("-");
            String mcVersion = parts[0];
            String[] split = parts[1].split("\\.");
            if (split[0].equals("0") && split[1].equals("0")) {
                int v = Integer.parseInt(split[2]);
                if ((mcVersion.equals("1.12.2") && v < 39)
                        || (mcVersion.equals("1.11.2") && v < 49)
                        || (mcVersion.equals("1.10.2") && v < 60)) {
                    hasWorkingBiomeSizes = false;
                }
            }
        } else {
            // if there is no replacer config, then it DEFINITELY didn't work
            // even versions with compressed presets always keys replacer config key
            // and to my knowledge, noone ever understood the compressed presets well enough
            // to manually remove replacer config entries
            if (!json.containsKey("replacerConfig")) {
                hasWorkingBiomeSizes = false;
            } else {
                // CWG 12 switched from using "cubicchunks:" to "cubicgen:" in replacer config IDs
                // this was before compressed configs, so if there are eny entries that still have "cubicchunks:"
                // then river and biome size definitely didn't work
                JsonObject replacerConfigDefaults = (JsonObject) json.getOrDefault("replacerConfig", new JsonObject());
                replacerConfigDefaults = (JsonObject) replacerConfigDefaults.getOrDefault("defaults", new JsonObject());
                if (replacerConfigDefaults.keySet().stream().anyMatch(n -> n.startsWith("cubicchunks:"))) {
                    hasWorkingBiomeSizes = false;
                }
            }
        }
        JsonObject transformed = transformer.transform(json, parent);
        if (!hasWorkingBiomeSizes) {
            transformed.put("biomeSize", new JsonPrimitive(4));
            transformed.put("riverSize", new JsonPrimitive(4));
        }

        JsonElement cubeAreasElem = transformed.get("cubeAreas");
        if (!(cubeAreasElem instanceof JsonArray)) {
            // in old versions, empty cubeAreas was sometimes serialized to "{}" (empty object)
            // instead of "[]" (empty array)
            transformed.put("cubeAreas", new JsonArray(), transformed.getComment("cubeAreas") +
                    "\nOld unknown value: " + cubeAreasElem.toJson());
            return transformed;
        }
        JsonArray cubeAreas = (JsonArray) cubeAreasElem;

        assert cubeAreas != null;
        for (JsonElement cubeArea : cubeAreas) {
            JsonArray entry = (JsonArray) cubeArea;
            JsonObject presetLayer = (JsonObject) entry.get(1);
            String layerComment = entry.getComment(1);
            JsonObject fixedLayer = fixGeneratorOptions(presetLayer, transformed, lastCwgVersion);

            entry.remove(presetLayer);
            entry.add(fixedLayer, layerComment);
        }

        return transformed;
    }

    private void copyHeightOffset(JsonObject oldRoot, JsonObject newRoot, CombinedContext<CombinedContext<JsonObject>> ctx) {
        if (oldRoot.containsKey("cubicchunks:height_offset")) {
            newRoot.put("cubicgen:height_offset", oldRoot.get("cubicchunks:height_offset"), oldRoot.getComment("cubicchunks:height_offset"));
            return;
        }
        if (oldRoot.containsKey("cubicgen:height_offset")) {
            newRoot.put("cubicgen:height_offset", oldRoot.get("cubicgen:height_offset"), oldRoot.getComment("cubicgen:height_offset"));
            return;
        }
        JsonObject globalRootParent = ctx.parent().parent();
        JsonObject globalRoot = ctx.parent().context();
        JsonElement newEntry = computeDefaultExpectedBaseHeight(globalRoot, globalRootParent);
        newRoot.put("cubicgen:height_offset", newEntry);
    }

    private void copyWaterLevel(JsonObject oldRoot, JsonObject newRoot, CombinedContext<CombinedContext<JsonObject>> ctx) {
        if (oldRoot.containsKey("cubicchunks:water_level")) {
            newRoot.put("cubicgen:water_level", oldRoot.get("cubicchunks:water_level"), oldRoot.getComment("cubicchunks:water_level"));
            return;
        }
        if (oldRoot.containsKey("cubicgen:water_level")) {
            newRoot.put("cubicgen:water_level", oldRoot.get("cubicgen:water_level"), oldRoot.getComment("cubicgen:water_level"));
            return;
        }
        JsonElement newEntry = getOrDefault(oldRoot, ctx.parent().parent(), "waterLevel", new JsonPrimitive(63));
        newRoot.put("cubicgen:water_level", newEntry);
    }

    private void copyHeightScale(JsonObject oldRoot, JsonObject newRoot, CombinedContext<CombinedContext<JsonObject>> ctx) {
        if (oldRoot.containsKey("cubicchunks:height_scale")) {
            newRoot.put("cubicgen:height_scale", oldRoot.get("cubicchunks:height_scale"), oldRoot.getComment("cubicchunks:height_scale"));
            return;
        }
        if (oldRoot.containsKey("cubicgen:height_scale")) {
            newRoot.put("cubicgen:height_scale", oldRoot.get("cubicgen:height_scale"), oldRoot.getComment("cubicgen:height_scale"));
            return;
        }
        JsonObject globalRootParent = ctx.parent().parent();
        JsonObject globalRoot = ctx.parent().context();
        JsonElement newEntry = computeDefaultExpectedHeightVariation(globalRoot, globalRootParent);
        newRoot.put("cubicgen:height_scale", newEntry);
    }

    private void copyOceanBlock(JsonObject oldRoot, JsonObject newRoot, CombinedContext<CombinedContext<JsonObject>> context) {
        if (oldRoot.containsKey("cubicchunks:ocean_block")) {
            newRoot.put("cubicgen:ocean_block", oldRoot.get("cubicchunks:ocean_block"), oldRoot.getComment("cubicchunks:ocean_block"));
            return;
        }
        if (oldRoot.containsKey("cubicgen:ocean_block")) {
            newRoot.put("cubicgen:ocean_block", oldRoot.get("cubicgen:ocean_block"), oldRoot.getComment("cubicgen:ocean_block"));
            return;
        }
        JsonElement lavaOceans = getOrDefault(oldRoot, context.parent().parent(), "lavaOceans", JsonPrimitive.FALSE);
        String liquidName = JsonPrimitive.TRUE.equals(lavaOceans) ? "minecraft:lava" : "minecraft:water";
        JsonObject liquid = new JsonObject();
        JsonObject liquidProp = new JsonObject();
        liquidProp.put("level", new JsonPrimitive(0.0));
        liquid.put("Properties", liquidProp);
        liquid.put("Name", new JsonPrimitive(liquidName));
        newRoot.put("cubicgen:ocean_block", liquid);
    }

    private void copyDirect(String name, JsonObject src, @Nullable JsonObject parent, JsonObject newRoot, Object defaultValue) {
        copyDirect(name, src, parent, newRoot, defaultValue == null ? JsonNull.INSTANCE : new JsonPrimitive(defaultValue));
    }

    private void copyDirect(String name, JsonObject src, @Nullable JsonObject parent, JsonObject newRoot,
            JsonElement defaultValue) {
        if (src.containsKey(name)) {
            JsonElement ret = src.get(name);
            assert ret != null;
            newRoot.put(name, ret.clone(), src.getComment(name));
            return;
        }
        if (parent == null) {
            throw new UnsupportedPresetException("Cannot update legacy compressed preset layer without V3 or older parent preset. This is caused by"
                    + " trying to use V3 or older compressed preset as a layer in V4 or newer preset.");
        }
        if (parent.containsKey(name)) {
            JsonElement ret = parent.get(name);
            assert ret != null;
            newRoot.put(name, ret.clone(), parent.getComment(name));
            return;
        }
        newRoot.put(name, defaultValue);
    }

    private JsonElement computeDefaultActualHeight(JsonObject json, @Nullable JsonObject parent) throws UnsupportedPresetException {
        if ((json.containsKey("heightOffset") || (parent != null && parent.containsKey("heightOffset"))) &&
                (json.containsKey("heightVariationOffset") || (parent != null && parent.containsKey("heightVariationOffset"))) &&
                (json.containsKey("heightFactor") || (parent != null && parent.containsKey("heightFactor")))) {
            float heightVariationOffset = ((JsonPrimitive) getOrDefault(json, parent, "heightVariationOffset", null)).asFloat(64);
            float offset = ((JsonPrimitive) getOrDefault(json, parent, "heightOffset", null)).asFloat(64);
            float factor = ((JsonPrimitive) getOrDefault(json, parent, "heightFactor", null)).asFloat(64);
            return new JsonPrimitive(
                    (offset + heightVariationOffset + Math.max(factor * 2 + heightVariationOffset, factor + heightVariationOffset * 2)));
        }
        return new JsonPrimitive(256);
    }

    private JsonElement computeDefaultExpectedBaseHeight(JsonObject json, JsonObject parent) throws UnsupportedPresetException {
        return getOrDefault(json, parent, "heightOffset", new JsonPrimitive(64));
    }

    private JsonElement computeDefaultExpectedHeightVariation(JsonObject json, JsonObject parent) throws UnsupportedPresetException {
        return getOrDefault(json, parent, "heightFactor", new JsonPrimitive(64));
    }

    private void copyPeriodicGaussianOres(JsonObject oldRoot, JsonObject newRoot, JsonObject parent) {
        if (oldRoot.containsKey("periodicGaussianOres")) {
            JsonElement ret = oldRoot.get("periodicGaussianOres");
            newRoot.put("periodicGaussianOres", ret, oldRoot.getComment("periodicGaussianOres"));
        } else {
            JsonArray periodicGaussianOres = new JsonArray();
            boolean converted = convertLapis(oldRoot, periodicGaussianOres, false);
            if (converted) {
                newRoot.put("periodicGaussianOres", periodicGaussianOres);
            } else {
                JsonElement ret = parent.get("periodicGaussianOres");
                if (ret == null) {
                    convertLapis(oldRoot, periodicGaussianOres, true);
                    newRoot.put("periodicGaussianOres", periodicGaussianOres);
                } else {
                    newRoot.put("periodicGaussianOres", ret, parent.getComment("periodicGaussianOres"));
                }
            }
        }
    }

    private void copyStandardOres(JsonObject oldRoot, JsonObject newRoot, JsonObject parent) {
        if (oldRoot.containsKey("standardOres")) {
            JsonElement ret = oldRoot.get("standardOres");
            assert ret != null;
            newRoot.put("standardOres", ret, oldRoot.getComment("standardOres"));
        } else {
            boolean foundAny = false;
            JsonArray standardOres = new JsonArray();
            for (int i = 0; i < 13; i++) {
                foundAny |= copyStandardOre(oldRoot, standardOres, i, false);
            }
            if (!foundAny) {
                JsonElement ret = parent.get("standardOres");
                if (ret == null) {
                    JsonArray newOres = new JsonArray();
                    for (int i = 0; i < 13; i++) {
                        copyStandardOre(oldRoot, newOres, i, true);
                    }
                    ret = newOres;
                }
                newRoot.put("standardOres", ret, parent.getComment("standardOres"));
            } else {
                newRoot.put("standardOres", standardOres, oldRoot.getComment("standardOres"));
            }
        }
    }

    private JsonElement getOrDefault(JsonObject source, @Nullable JsonObject parent, String name, JsonElement jsonElement)
            throws UnsupportedPresetException {
        if (source.containsKey(name)) {
            return source.get(name);
        }
        if (parent == null) {
            throw new UnsupportedPresetException("Cannot update legacy compressed preset layer without V3 or older parent preset. This is caused by"
                    + " trying to use V3 or older compressed preset as a layer in V4 or newer preset.");
        }
        if (parent.containsKey(name)) {
            return parent.get(name);
        }
        return jsonElement;
    }

    private boolean copyStandardOre(JsonObject root, JsonArray newObj, int idx, boolean create) {
        String ore = standard[idx];
        if (!root.containsKey(ore + "SpawnTries") && !create) {
            // some old saves are broken, especially 1.11.2 ones from the
            // 1.12.2->1.11.2 backport, build 847
            // this avoids adding a lot of air ores
            return false;
        }

        JsonElement oldSpawnSize = root.getOrDefault(ore + "SpawnSize", new JsonPrimitive(spawnSizes[idx]));
        String commentOldSpawnSize = root.getComment(ore + "SpawnSize");
        JsonElement oldSpawnTries = root.getOrDefault(ore + "SpawnTries", new JsonPrimitive(spawnTries[idx]));
        String commentOldSpawnTries = root.getComment(ore + "SpawnTries");
        JsonElement oldSpawnProbability = root.getOrDefault(ore + "SpawnProbability", new JsonPrimitive(spawnProbabilities[idx]));
        String commentOldSpawnProbability = root.getComment(ore + "SpawnProbability");
        JsonElement oldMinHeight = root.getOrDefault(ore + "SpawnMinHeight", new JsonPrimitive(minHeights[idx]));
        String commentOldMinHeight = root.getComment(ore + "SpawnMinHeight");
        JsonElement oldMaxHeight = root.getOrDefault(ore + "SpawnMaxHeight", new JsonPrimitive(maxHeights[idx]));
        String commentOldMaxHeight = root.getComment(ore + "SpawnMaxHeight");

        JsonObject obj = new JsonObject();
        obj.put("blockstate", standardBlockstates[idx]);
        obj.put("biomes", standardBiomes[idx]);

        if (oldSpawnSize != null) {
            obj.put("spawnSize", oldSpawnSize, commentOldSpawnSize);
        } else {
            // emerald doesn't have size defined in the old format
            obj.put("spawnSize", new JsonPrimitive(3), commentOldSpawnSize);
        }
        obj.put("spawnTries", oldSpawnTries, commentOldSpawnTries);
        obj.put("spawnProbability", oldSpawnProbability, commentOldSpawnProbability);
        obj.put("minHeight", oldMinHeight, commentOldMinHeight);
        obj.put("maxHeight", oldMaxHeight, commentOldMaxHeight);
        newObj.add(obj);
        return true;
    }

    private boolean convertLapis(JsonObject oldRoot, JsonArray newObj, boolean force) {
        boolean success = copyStandardOre(oldRoot, newObj, 13, false);
        if (!success && !force) {
            return false;
        }
        if (!success) {
            copyStandardOre(oldRoot, newObj, 13, true);
        }
        JsonObject lapis = (JsonObject) newObj.get(newObj.size() - 1);
        String commentOldMean = oldRoot.getComment("lapisLazuliHeightMean");
        JsonElement oldMean = oldRoot.getOrDefault("lapisLazuliHeightMean", new JsonPrimitive(-0.75f));
        String commentOldStdDev = oldRoot.getComment("lapisLazuliHeightStdDeviation");
        JsonElement oldStdDev = oldRoot.getOrDefault("lapisLazuliHeightStdDeviation", new JsonPrimitive(0.11231704455f));
        String commentOldSpacing = oldRoot.getComment("lapisLazuliHeightSpacing");
        JsonElement oldSpacing = oldRoot.getOrDefault("lapisLazuliHeightSpacing", new JsonPrimitive(3.0f));

        lapis.put("heightMean", oldMean, commentOldMean);
        lapis.put("heightStdDeviation", oldStdDev, commentOldStdDev);
        lapis.put("heightSpacing", oldSpacing, commentOldSpacing);
        return true;
    }
}
