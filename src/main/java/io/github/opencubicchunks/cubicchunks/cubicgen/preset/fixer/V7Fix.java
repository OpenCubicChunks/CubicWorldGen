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

import java.util.Arrays;
import java.util.function.Function;

public class V7Fix implements IJsonFix {

    private final JsonTransformer<Function<JsonObject, JsonObject>> transformer = JsonTransformer.<Function<JsonObject, JsonObject>>builder("V6 -> V7")
            .valueTransform("version", (e, ctx) -> new JsonPrimitive(6))
            .passthroughFor(
                    "caves",
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
                    "lakes"
            )
            .transform("replacers", this::transformReplacerConfig, "replacerConfig")
            .valueTransform("cubeAreas", this::convertCubeAreas)
            .build();

    @Override public JsonObject fix(Function<JsonObject, JsonObject> fixerFunction, JsonObject oldRoot) {
        return transformer.transform(oldRoot, fixerFunction);
    }

    private void transformReplacerConfig(JsonObject oldRoot, JsonObject newRoot, Function<JsonObject, JsonObject> context) {
        JsonObject oldConfig = oldRoot.getObject("replacerConfig");

        JsonPrimitive heightScale = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:height_scale"));
        JsonPrimitive surfaceDepthLimit = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:surface_depth_limit"));
        JsonPrimitive waterLevel = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:water_level"));
        JsonPrimitive biomeFillDepthFactor = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:biome_fill_depth_factor"));
        JsonElement terrainFillBlock = getOldReplacerOption(oldConfig, "cubicgen:terrain_fill_block");
        JsonPrimitive mesaDepth = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:mesa_depth"));
        JsonPrimitive biomeFillDepthOffset = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:biome_fill_depth_offset"));
        JsonPrimitive biomeFillDepthOctaves = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:biome_fill_noise_octaves"));
        JsonElement oceanBlock = getOldReplacerOption(oldConfig, "cubicgen:ocean_block");
        JsonPrimitive biomeFillNoiseFreq = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:biome_fill_noise_freq"));
        JsonPrimitive horGradDepthDecWeight = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:horizontal_gradient_depth_decrease_weight"));
        JsonPrimitive heightOffset = ((JsonPrimitive) getOldReplacerOption(oldConfig, "cubicgen:height_offset"));

        String heightScaleComment = getOldReplacerComment(oldConfig, "cubicgen:height_scale");
        String surfaceDepthLimitComment = getOldReplacerComment(oldConfig, "cubicgen:surface_depth_limit");
        String waterLevelComment = getOldReplacerComment(oldConfig, "cubicgen:water_level");
        String biomeFillDepthFactorComment = getOldReplacerComment(oldConfig, "cubicgen:biome_fill_depth_factor");
        String terrainFillBlockComment = getOldReplacerComment(oldConfig, "cubicgen:terrain_fill_block");
        String mesaDepthComment = getOldReplacerComment(oldConfig, "cubicgen:mesa_depth");
        String biomeFillDepthOffsetComment = getOldReplacerComment(oldConfig, "cubicgen:biome_fill_depth_offset");
        String biomeFillDepthOctavesComment = getOldReplacerComment(oldConfig, "cubicgen:biome_fill_noise_octaves");
        String oceanBlockComment = getOldReplacerComment(oldConfig, "cubicgen:ocean_block");
        String biomeFillNoiseFreqComment = getOldReplacerComment(oldConfig, "cubicgen:biome_fill_noise_freq");
        String horGradDepthDecWeightComment = getOldReplacerComment(oldConfig, "cubicgen:horizontal_gradient_depth_decrease_weight");
        String heightOffsetComment = getOldReplacerComment(oldConfig, "cubicgen:height_offset");



        JsonObject stoneReplacer = new JsonObject();
        stoneReplacer.put("type", new JsonPrimitive("densityRange"));
        stoneReplacer.put("blockInRange", terrainFillBlock, terrainFillBlockComment);
        stoneReplacer.put("blockOutOfRange", JsonNull.INSTANCE);
        stoneReplacer.put("filterBlocks", new JsonArray());
        stoneReplacer.put("blockFilterType", new JsonPrimitive("EXCLUDE"));
        stoneReplacer.put("minDensity", new JsonPrimitive(0.0));
        stoneReplacer.put("maxDensity", new JsonPrimitive(Double.POSITIVE_INFINITY));
        stoneReplacer.put("minY", new JsonPrimitive(Integer.MIN_VALUE));
        stoneReplacer.put("maxY", new JsonPrimitive(Integer.MAX_VALUE));
        stoneReplacer.put("biomeFilter", JsonNull.INSTANCE);


        JsonObject mainSurfaceReplacer = new JsonObject();
        mainSurfaceReplacer.put("type", new JsonPrimitive("mainSurface"));
        mainSurfaceReplacer.put("surfaceDepthNoiseType", new JsonPrimitive("SIMPLEX_SPONGE_NOISE"));
        mainSurfaceReplacer.put("surfaceDepthNoiseSeed", new JsonPrimitive(0));
        mainSurfaceReplacer.put("surfaceDepthNoiseFrequencyX", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        mainSurfaceReplacer.put("surfaceDepthNoiseFrequencyY", new JsonPrimitive(0.0));
        mainSurfaceReplacer.put("surfaceDepthNoiseFrequencyZ", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        mainSurfaceReplacer.put("surfaceDepthNoiseOctaves", biomeFillDepthOctaves, biomeFillDepthOctavesComment);
        mainSurfaceReplacer.put("surfaceDepthNoiseFactor", biomeFillDepthFactor, biomeFillDepthFactorComment);
        mainSurfaceReplacer.put("surfaceDepthNoiseOffset", biomeFillDepthOffset, biomeFillDepthOffsetComment);
        mainSurfaceReplacer.put("maxSurfaceDepth", surfaceDepthLimit, surfaceDepthLimitComment);
        mainSurfaceReplacer.put("horizontalGradientDepthDecreaseWeight", horGradDepthDecWeight, horGradDepthDecWeightComment);
        mainSurfaceReplacer.put("oceanLevel", waterLevel, waterLevelComment);
        mainSurfaceReplacer.put("overrideTop", JsonNull.INSTANCE);
        mainSurfaceReplacer.put("overrideFiller", JsonNull.INSTANCE);
        mainSurfaceReplacer.put("minY", new JsonPrimitive(Integer.MIN_VALUE));
        mainSurfaceReplacer.put("maxY", new JsonPrimitive(Integer.MAX_VALUE));

        JsonObject mainSurfaceBiomeFilter = new JsonObject();
        {
            mainSurfaceBiomeFilter.put("type", new JsonPrimitive("allOf"));
            JsonArray filters = new JsonArray();
            {
                JsonObject excludeMesaAndSavanna = new JsonObject();
                excludeMesaAndSavanna.put("type", new JsonPrimitive("excludeClass"));
                JsonArray excludedClasses = new JsonArray();
                excludedClasses.addAll(Arrays.asList(
                        new JsonPrimitive("net.minecraft.world.biome.BiomeSavannaMutated"),
                        new JsonPrimitive("net.minecraft.world.biome.BiomeMesa")
                ));
                excludeMesaAndSavanna.put("classNames", excludedClasses);
                excludeMesaAndSavanna.put("matchType", new JsonPrimitive("RAW_EXACT"));
                filters.add(excludeMesaAndSavanna);

                JsonObject excludeTaiga = new JsonObject();
                excludeTaiga.put("type", new JsonPrimitive("exclude"));
                JsonArray excluded = new JsonArray();
                excluded.addAll(Arrays.asList(
                        new JsonPrimitive("minecraft:redwood_taiga"),
                        new JsonPrimitive("minecraft:redwood_taiga_hills"),
                        new JsonPrimitive("minecraft:mutated_redwood_taiga"),
                        new JsonPrimitive("minecraft:mutated_redwood_taiga_hills")
                ));
                excludeTaiga.put("biomes", excluded);
                filters.add(excludeTaiga);
            }
            mainSurfaceBiomeFilter.put("filters", filters);
        }
        mainSurfaceReplacer.put("biomeFilter", mainSurfaceBiomeFilter);


        JsonObject mutatedSavannaReplacer = new JsonObject();
        mutatedSavannaReplacer.put("type", new JsonPrimitive("depthBasedSurface"));
        JsonArray mutatedSavannaTopThresholds = new JsonArray();
        {
            JsonObject stone = new JsonObject();
            stone.put("y", new JsonPrimitive(3.5833333333333335));
            stone.put("b", new JsonPrimitive("stone"));
            mutatedSavannaTopThresholds.add(stone);


            JsonObject coarseDirt = new JsonObject();
            coarseDirt.put("y", new JsonPrimitive(2.8333333333333335));
            JsonObject block = new JsonObject();
            {
                block.put("Name", new JsonPrimitive("dirt"));
                JsonObject properties = new JsonObject();
                properties.put("variant", new JsonPrimitive("coarse_dirt"));
                block.put("Properties", properties);
            }
            coarseDirt.put("b", block);
            mutatedSavannaTopThresholds.add(coarseDirt);
        }
        mutatedSavannaReplacer.put("topThresholds", mutatedSavannaTopThresholds);
        JsonArray mutatedSavannaFillerThresholds = new JsonArray();
        {
            JsonObject stone = new JsonObject();
            stone.put("y", new JsonPrimitive(3.5833333333333335));
            stone.put("b", new JsonPrimitive("stone"));
            mutatedSavannaFillerThresholds.add(stone);
        }
        mutatedSavannaReplacer.put("fillerThresholds", mutatedSavannaFillerThresholds);
        mutatedSavannaReplacer.put("surfaceDepthNoiseType", new JsonPrimitive("SIMPLEX_SPONGE_NOISE"));
        mutatedSavannaReplacer.put("surfaceDepthNoiseSeed", new JsonPrimitive(0));
        mutatedSavannaReplacer.put("surfaceDepthNoiseFrequencyX", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        mutatedSavannaReplacer.put("surfaceDepthNoiseFrequencyY", new JsonPrimitive(0.0));
        mutatedSavannaReplacer.put("surfaceDepthNoiseFrequencyZ", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        mutatedSavannaReplacer.put("surfaceDepthNoiseOctaves", biomeFillDepthOctaves, biomeFillDepthOctavesComment);
        mutatedSavannaReplacer.put("surfaceDepthNoiseFactor", biomeFillDepthFactor, biomeFillDepthFactorComment);
        mutatedSavannaReplacer.put("surfaceDepthNoiseOffset", biomeFillDepthOffset, biomeFillDepthOffsetComment);
        mutatedSavannaReplacer.put("maxSurfaceDepth", surfaceDepthLimit, surfaceDepthLimitComment);
        mutatedSavannaReplacer.put("horizontalGradientDepthDecreaseWeight", horGradDepthDecWeight, horGradDepthDecWeightComment);
        mutatedSavannaReplacer.put("oceanLevel", new JsonPrimitive(63.0));
        mutatedSavannaReplacer.put("overrideTop", JsonNull.INSTANCE);
        mutatedSavannaReplacer.put("overrideFiller", JsonNull.INSTANCE);
        mutatedSavannaReplacer.put("minY", new JsonPrimitive(-2147483648));
        mutatedSavannaReplacer.put("maxY", new JsonPrimitive(2147483647));
        JsonObject mutatedSavannaBiomeFilter = new JsonObject();
        {
            mutatedSavannaBiomeFilter.put("type", new JsonPrimitive("allOf"));
            JsonArray filters = new JsonArray();
            {
                JsonObject includeSavanna = new JsonObject();
                includeSavanna.put("type", new JsonPrimitive("includeClass"));
                JsonArray excludedClasses = new JsonArray();
                excludedClasses.add(new JsonPrimitive("net.minecraft.world.biome.BiomeSavannaMutated"));
                includeSavanna.put("classNames", excludedClasses);
                includeSavanna.put("matchType", new JsonPrimitive("RAW_EXACT"));
                filters.add(includeSavanna);

            }
            mutatedSavannaBiomeFilter.put("filters", filters);
        }
        mutatedSavannaReplacer.put("biomeFilter", mutatedSavannaBiomeFilter);


        JsonObject taigaReplacer = new JsonObject();
        taigaReplacer.put("type", new JsonPrimitive("depthBasedSurface"));
        JsonArray taigaTopThresholds = new JsonArray();
        {
            JsonObject podzol = new JsonObject();
            podzol.put("y", new JsonPrimitive(2.6833333333333336));
            JsonObject podzolBlock = new JsonObject();
            {
                podzolBlock.put("Name", new JsonPrimitive("dirt"));
                JsonObject properties = new JsonObject();
                properties.put("variant", new JsonPrimitive("podzol"));
                podzolBlock.put("Properties", properties);
            }
            podzol.put("b", podzolBlock);
            taigaTopThresholds.add(podzol);


            JsonObject coarseDirt = new JsonObject();
            coarseDirt.put("y", new JsonPrimitive(3.5833333333333335));
            JsonObject block = new JsonObject();
            {
                block.put("Name", new JsonPrimitive("dirt"));
                JsonObject properties = new JsonObject();
                properties.put("variant", new JsonPrimitive("coarse_dirt"));
                block.put("Properties", properties);
            }
            coarseDirt.put("b", block);
            taigaTopThresholds.add(coarseDirt);
        }
        taigaReplacer.put("topThresholds", taigaTopThresholds);
        taigaReplacer.put("fillerThresholds", new JsonArray());
        taigaReplacer.put("surfaceDepthNoiseType", new JsonPrimitive("SIMPLEX_SPONGE_NOISE"));
        taigaReplacer.put("surfaceDepthNoiseSeed", new JsonPrimitive(0));
        taigaReplacer.put("surfaceDepthNoiseFrequencyX", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        taigaReplacer.put("surfaceDepthNoiseFrequencyY", new JsonPrimitive(0.0));
        taigaReplacer.put("surfaceDepthNoiseFrequencyZ", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        taigaReplacer.put("surfaceDepthNoiseOctaves", biomeFillDepthOctaves, biomeFillDepthOctavesComment);
        taigaReplacer.put("surfaceDepthNoiseFactor", biomeFillDepthFactor, biomeFillDepthFactorComment);
        taigaReplacer.put("surfaceDepthNoiseOffset", biomeFillDepthOffset, biomeFillDepthOffsetComment);
        taigaReplacer.put("maxSurfaceDepth", surfaceDepthLimit, surfaceDepthLimitComment);
        taigaReplacer.put("horizontalGradientDepthDecreaseWeight", horGradDepthDecWeight, horGradDepthDecWeightComment);
        taigaReplacer.put("oceanLevel", new JsonPrimitive(63.0));
        taigaReplacer.put("overrideTop", JsonNull.INSTANCE);
        taigaReplacer.put("overrideFiller", JsonNull.INSTANCE);
        taigaReplacer.put("minY", new JsonPrimitive(-2147483648));
        taigaReplacer.put("maxY", new JsonPrimitive(2147483647));
        JsonObject taigaBiomeFilter = new JsonObject();
        {
            taigaBiomeFilter.put("type", new JsonPrimitive("allOf"));
            JsonArray filters = new JsonArray();
            {
                JsonObject includeTaiga = new JsonObject();
                includeTaiga.put("type", new JsonPrimitive("include"));
                JsonArray excluded = new JsonArray();
                excluded.addAll(Arrays.asList(
                        new JsonPrimitive("minecraft:redwood_taiga"),
                        new JsonPrimitive("minecraft:redwood_taiga_hills"),
                        new JsonPrimitive("minecraft:mutated_redwood_taiga"),
                        new JsonPrimitive("minecraft:mutated_redwood_taiga_hills")
                ));
                includeTaiga.put("biomes", excluded);
                filters.add(includeTaiga);
            }
            taigaBiomeFilter.put("filters", filters);
        }
        taigaReplacer.put("biomeFilter", taigaBiomeFilter);


        JsonObject mesaReplacer = new JsonObject();
        mesaReplacer.put("type", new JsonPrimitive("mesaSurface"));
        mesaReplacer.put("mesaDepth", mesaDepth, mesaDepthComment);
        mesaReplacer.put("heightOffset", heightOffset, heightOffsetComment);
        mesaReplacer.put("heightScale", heightScale, heightScaleComment);
        mesaReplacer.put("waterHeight", waterLevel, waterLevelComment);
        mesaReplacer.put("surfaceDepthNoiseType", new JsonPrimitive("SIMPLEX_SPONGE_NOISE"));
        mesaReplacer.put("surfaceDepthNoiseSeed", new JsonPrimitive(0));
        mesaReplacer.put("surfaceDepthNoiseFrequencyX", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        mesaReplacer.put("surfaceDepthNoiseFrequencyY", new JsonPrimitive(0.0));
        mesaReplacer.put("surfaceDepthNoiseFrequencyZ", biomeFillNoiseFreq, biomeFillNoiseFreqComment);
        mesaReplacer.put("surfaceDepthNoiseOctaves", biomeFillDepthOctaves, biomeFillDepthOctavesComment);
        mesaReplacer.put("surfaceDepthNoiseFactor", biomeFillDepthFactor, biomeFillDepthFactorComment);
        mesaReplacer.put("surfaceDepthNoiseOffset", biomeFillDepthOffset, biomeFillDepthOffsetComment);
        mesaReplacer.put("clayBandsOverride", JsonNull.INSTANCE);
        mesaReplacer.put("clayBandsOffsetNoiseSource", new JsonPrimitive("MESA_CLAY_BANDS_OFFSET_NOISE"));
        mesaReplacer.put("clayBandsNoiseFrequencyX", new JsonPrimitive(0.001953125));
        mesaReplacer.put("clayBandsNoiseFrequencyY", new JsonPrimitive(0.0));
        mesaReplacer.put("clayBandsNoiseFrequencyZ", new JsonPrimitive(0.001953125));
        mesaReplacer.put("clayBandsNoiseFactor", new JsonPrimitive(1.0));
        mesaReplacer.put("clayBandsNoiseOffset", new JsonPrimitive(0.0));
        mesaReplacer.put("customClayBandsNoiseSeed", new JsonPrimitive(0));
        mesaReplacer.put("customClayBandsNoiseOctaves", new JsonPrimitive(0));
        mesaReplacer.put("minY", new JsonPrimitive(-2147483648));
        mesaReplacer.put("maxY", new JsonPrimitive(2147483647));
        JsonObject mesaBiomeFilter = new JsonObject();
        {
            mesaBiomeFilter.put("type", new JsonPrimitive("allOf"));
            JsonArray filters = new JsonArray();
            {
                JsonObject includeMesa = new JsonObject();
                includeMesa.put("type", new JsonPrimitive("includeClass"));
                JsonArray excludedClasses = new JsonArray();
                excludedClasses.add(new JsonPrimitive("net.minecraft.world.biome.BiomeMesa"));
                includeMesa.put("classNames", excludedClasses);
                includeMesa.put("matchType", new JsonPrimitive("RAW_EXACT"));
                filters.add(includeMesa);
            }
            mesaBiomeFilter.put("filters", filters);
        }
        mesaReplacer.put("biomeFilter", mesaBiomeFilter);


        JsonObject oceanReplacer = new JsonObject();
        oceanReplacer.put("type", new JsonPrimitive("densityRange"));
        oceanReplacer.put("blockInRange", oceanBlock, oceanBlockComment);
        oceanReplacer.put("blockOutOfRange", JsonNull.INSTANCE);
        JsonArray oceanFillerBlocks = new JsonArray();
        oceanFillerBlocks.add(new JsonPrimitive("air"));
        oceanReplacer.put("filterBlocks", oceanFillerBlocks);
        oceanReplacer.put("blockFilterType", new JsonPrimitive("INCLUDE"));
        oceanReplacer.put("minDensity", new JsonPrimitive(Double.NEGATIVE_INFINITY));
        oceanReplacer.put("maxDensity", new JsonPrimitive(Double.POSITIVE_INFINITY));
        oceanReplacer.put("minY", new JsonPrimitive(-2147483648));
        oceanReplacer.put("maxY", waterLevel, waterLevelComment);
        oceanReplacer.put("biomeFilter", JsonNull.INSTANCE);


        JsonObject swampReplacer = new JsonObject();
        swampReplacer.put("type", new JsonPrimitive("noiseBasedSurfaceDecoration"));
        swampReplacer.put("surfaceDensityThreshold", new JsonPrimitive(0.0));
        swampReplacer.put("groundBlock", new JsonPrimitive("water"));
        swampReplacer.put("featureBlock", new JsonPrimitive("waterlily"));
        swampReplacer.put("noiseSource", new JsonPrimitive("GRASS_COLOR_NOISE"));
        swampReplacer.put("noiseFreqX", new JsonPrimitive(0.25));
        swampReplacer.put("noiseFreqY", new JsonPrimitive(0.0));
        swampReplacer.put("noiseFreqZ", new JsonPrimitive(0.25));
        swampReplacer.put("noiseFactor", new JsonPrimitive(1.0));
        swampReplacer.put("noiseOffset", new JsonPrimitive(0.0));
        swampReplacer.put("customNoiseSeed", new JsonPrimitive(0));
        swampReplacer.put("customNoiseOctaves", new JsonPrimitive(0));
        swampReplacer.put("featureMinNoise", new JsonPrimitive(0.0));
        swampReplacer.put("featureMaxNoise", new JsonPrimitive(0.12));
        swampReplacer.put("groundMinNoise", new JsonPrimitive(0.0));
        swampReplacer.put("groundMaxNoise", new JsonPrimitive(Double.POSITIVE_INFINITY));
        swampReplacer.put("minY", new JsonPrimitive(63));
        swampReplacer.put("maxY", new JsonPrimitive(64));
        JsonObject swampBiomeFilter = new JsonObject();
        {
            swampBiomeFilter.put("type", new JsonPrimitive("allOf"));
            JsonArray filters = new JsonArray();
            {
                JsonObject includeSwamp = new JsonObject();
                includeSwamp.put("type", new JsonPrimitive("includeClass"));
                JsonArray excludedClasses = new JsonArray();
                excludedClasses.add(new JsonPrimitive("net.minecraft.world.biome.BiomeSwamp"));
                includeSwamp.put("classNames", excludedClasses);
                includeSwamp.put("matchType", new JsonPrimitive("RAW_WITH_SUBCLASSES"));
                filters.add(includeSwamp);
            }
            swampBiomeFilter.put("filters", filters);
        }
        swampReplacer.put("biomeFilter", swampBiomeFilter);


        JsonObject bedrockReplacer = new JsonObject();
        bedrockReplacer.put("type", new JsonPrimitive("randomYGradient"));
        bedrockReplacer.put("blockToPlace", new JsonPrimitive("bedrock"));
        JsonArray bedrockProbabilityFunction = new JsonArray();
        {
            JsonObject p1 = new JsonObject();
            p1.put("y", new JsonPrimitive(-1.073741824E9));
            p1.put("v", new JsonPrimitive(1.0));

            JsonObject p2 = new JsonObject();
            p2.put("y", new JsonPrimitive(-1.073741819E9));
            p2.put("v", new JsonPrimitive(0.0));

            bedrockProbabilityFunction.add(p1);
            bedrockProbabilityFunction.add(p2);
        }
        bedrockReplacer.put("probabilityFunction", bedrockProbabilityFunction);
        bedrockReplacer.put("seed", new JsonPrimitive(0));
        bedrockReplacer.put("minY", new JsonPrimitive(-1073741824));
        bedrockReplacer.put("maxY", new JsonPrimitive(-1073741819));
        bedrockReplacer.put("biomeFilter", JsonNull.INSTANCE);



        String replacersComment = oldConfig.getComment("defaults");
        if (replacersComment != null) {
            replacersComment = "defaults: " + replacersComment;
        }
        String overridesComment = oldConfig.getComment("overrides");
        if (overridesComment != null) {
            overridesComment = "overrides: " + overridesComment;
        }

        if (replacersComment == null) {
            replacersComment = overridesComment;
        } else if (overridesComment != null) {
            replacersComment += "\n" + overridesComment;
        }

        JsonArray replacers = new JsonArray();
        replacers.addAll(Arrays.asList(
                stoneReplacer,
                mainSurfaceReplacer,
                mutatedSavannaReplacer,
                taigaReplacer,
                mesaReplacer,
                oceanReplacer,
                swampReplacer,
                bedrockReplacer
        ));
        replacers.setComment(0, replacersComment);

        newRoot.put("replacers", replacers, oldRoot.getComment("replacerConfig"));
    }

    private JsonElement getOldReplacerOption(JsonObject oldRoot, String name) {
        JsonObject overrides = oldRoot.getObject("overrides");

        JsonElement value = overrides.get(name);
        if (value == null) {
            JsonObject defaults = oldRoot.getObject("defaults");
            value = defaults.get(name);
        }
        return value;
    }

    private String getOldReplacerComment(JsonObject oldRoot, String name) {
        JsonObject overrides = oldRoot.getObject("overrides");
        JsonObject defaults = oldRoot.getObject("defaults");

        String value = overrides.getComment(name);
        String defaultComment = defaults.getComment(name);

        if (value == null) {
            value = defaultComment;
        } else if (defaultComment != null) {
            value += "\n" + defaultComment;
        }
        return value;
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
