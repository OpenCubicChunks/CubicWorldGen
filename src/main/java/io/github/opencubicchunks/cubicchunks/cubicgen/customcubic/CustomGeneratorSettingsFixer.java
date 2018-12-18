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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic;

import static io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod.MODID;

import java.io.StringReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import io.github.opencubicchunks.cubicchunks.cubicgen.ConversionUtils;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

public class CustomGeneratorSettingsFixer {

    public static final int VERSION = 3;

    private static final String[] standard = { "dirt", "gravel", "granite", "diorite", "andesite", "coalOre", "ironOre",
            "goldOre", "redstoneOre", "diamondOre", "hillsEmeraldOre", "hillsSilverfishStone", "mesaAddedGoldOre" };

    private static final IBlockState[] standardBlockstates = { Blocks.DIRT.getDefaultState(),
            Blocks.GRAVEL.getDefaultState(),
            Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE),
            Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE),
            Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),
            Blocks.COAL_ORE.getDefaultState(), Blocks.IRON_ORE.getDefaultState(), Blocks.GOLD_ORE.getDefaultState(),
            Blocks.REDSTONE_ORE.getDefaultState(), Blocks.DIAMOND_ORE.getDefaultState(),
            Blocks.EMERALD_ORE.getDefaultState(),
            Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE),
            Blocks.GOLD_ORE.getDefaultState() };
    private static final Biome[][] standardBiomes = { null, // dirt
            null, // gravel
            null, // granite
            null, // diorite
            null, // andesite
            null, // coal
            null, // iron
            null, // gold
            null, // redstone
            null, // diamond
            { Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES,
                    Biomes.MUTATED_EXTREME_HILLS, Biomes.MUTATED_EXTREME_HILLS_WITH_TREES }, // emerald
            { Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES,
                    Biomes.MUTATED_EXTREME_HILLS, Biomes.MUTATED_EXTREME_HILLS_WITH_TREES }, // monster
                                                                                             // egg
            { Biomes.MESA, Biomes.MESA_CLEAR_ROCK, Biomes.MESA_ROCK, Biomes.MUTATED_MESA,
                    Biomes.MUTATED_MESA_CLEAR_ROCK, Biomes.MUTATED_MESA_ROCK },// mesa
                                                                               // gold
    };

    public static String fixGeneratorOptions(String generatorOptionsToFix) {
        Gson gson = CustomGeneratorSettings.gson(false);

        JsonReader oldReader = new JsonReader(new StringReader(generatorOptionsToFix));
        JsonObject oldRoot = new JsonParser().parse(oldReader).getAsJsonObject();

        JsonReader newReader = new JsonReader(new StringReader("{}"));
        JsonObject newRoot = new JsonParser().parse(newReader).getAsJsonObject();

        newRoot.add("version", new JsonPrimitive(3));
        newRoot.add("waterLevel", getWaterLevel(oldRoot));
        newRoot.add("caves", getCaves(oldRoot));
        newRoot.add("strongholds", getStrongholds(oldRoot));
        newRoot.add("alternateStrongholdsPositions", getAlternateStrongholdsPositions(oldRoot));
        newRoot.add("villages", getVillages(oldRoot));
        newRoot.add("mineshafts", getMineshafts(oldRoot));
        newRoot.add("temples", getTemples(oldRoot));
        newRoot.add("oceanMonuments", getOceanMonuments(oldRoot));
        newRoot.add("woodlandMansions", getWoodlandMansions(oldRoot));
        newRoot.add("ravines", getRavines(oldRoot));
        newRoot.add("dungeons", getDungeons(oldRoot));
        newRoot.add("dungeonCount", getDungeonCount(oldRoot));
        newRoot.add("waterLakes", getWaterLakes(oldRoot));
        newRoot.add("waterLakeRarity", getWaterLakeRarity(oldRoot));
        newRoot.add("lavaLakes", getLavaLakes(oldRoot));
        newRoot.add("lavaLakeRarity", getLavaLakeRarity(oldRoot));
        newRoot.add("aboveSeaLavaLakeRarity", getAboveSeaLavaLakeRarity(oldRoot));
        newRoot.add("lavaOceans", getLavaOceans(oldRoot));
        newRoot.add("biome", getBiome(oldRoot));
        newRoot.add("biomeSize", getBiomeSize(oldRoot));
        newRoot.add("riverSize", getRiverSize(oldRoot));
        newRoot.add("standardOres", getStandardOres(oldRoot));
        newRoot.add("periodicGaussianOres", getPeriodicGaussianOres(oldRoot));
        newRoot.add("expectedBaseHeight", getExpectedBaseHeight(oldRoot));
        newRoot.add("expectedHeightVariation", getExpectedHeightVariation(oldRoot));
        newRoot.add("actualHeight", getActualHeight(oldRoot));
        newRoot.add("heightVariationFactor", getHeightVariationFactor(oldRoot));
        newRoot.add("specialHeightVariationFactorBelowAverageY", getSpecialHeightVariationFactorBelowAverageY(oldRoot));
        newRoot.add("heightVariationOffset", getHeightVariationOffset(oldRoot));
        newRoot.add("heightFactor", getHeightFactor(oldRoot));
        newRoot.add("heightOffset", getHeightOffset(oldRoot));
        newRoot.add("depthNoiseFactor", getDepthNoiseFactor(oldRoot));
        newRoot.add("depthNoiseOffset", getDepthNoiseOffset(oldRoot));
        newRoot.add("depthNoiseFrequencyX", getDepthNoiseFrequencyX(oldRoot));
        newRoot.add("depthNoiseFrequencyZ", getDepthNoiseFrequencyZ(oldRoot));
        newRoot.add("depthNoiseOctaves", getDepthNoiseOctaves(oldRoot));
        newRoot.add("selectorNoiseFactor", getSelectorNoiseFactor(oldRoot));
        newRoot.add("selectorNoiseOffset", getSelectorNoiseOffset(oldRoot));
        newRoot.add("selectorNoiseFrequencyX", getSelectorNoiseFrequencyX(oldRoot));
        newRoot.add("selectorNoiseFrequencyY", getSelectorNoiseFrequencyY(oldRoot));
        newRoot.add("selectorNoiseFrequencyZ", getSelectorNoiseFrequencyZ(oldRoot));
        newRoot.add("selectorNoiseOctaves", getSelectorNoiseOctaves(oldRoot));
        newRoot.add("lowNoiseFactor", getLowNoiseFactor(oldRoot));
        newRoot.add("lowNoiseOffset", getLowNoiseOffset(oldRoot));
        newRoot.add("lowNoiseFrequencyX", getLowNoiseFrequencyX(oldRoot));
        newRoot.add("lowNoiseFrequencyY", getLowNoiseFrequencyY(oldRoot));
        newRoot.add("lowNoiseFrequencyZ", getLowNoiseFrequencyZ(oldRoot));
        newRoot.add("lowNoiseOctaves", getLowNoiseOctaves(oldRoot));
        newRoot.add("highNoiseFactor", getHighNoiseFactor(oldRoot));
        newRoot.add("highNoiseOffset", getHighNoiseOffset(oldRoot));
        newRoot.add("highNoiseFrequencyX", getHighNoiseFrequencyX(oldRoot));
        newRoot.add("highNoiseFrequencyY", getHighNoiseFrequencyY(oldRoot));
        newRoot.add("highNoiseFrequencyZ", getHighNoiseFrequencyZ(oldRoot));
        newRoot.add("highNoiseOctaves", getHighNoiseOctaves(oldRoot));
        newRoot.add("cubeAreas", getCubeAreas(oldRoot));
        newRoot.add("replacerConfig", getReplacerConfig(oldRoot));

        String newGeneratorOptions = gson.toJson(newRoot).replaceAll("cubicchunks:", MODID + ":");
        return newGeneratorOptions;
    }

    private static JsonElement getReplacerConfig(JsonObject json) {
        Gson gson = CustomGeneratorSettings.gson(false);
        return getOrDefault(json, "replacerConfig", gson.toJsonTree(BiomeBlockReplacerConfig.defaults()));
    }

    private static JsonElement getCubeAreas(JsonObject json) {
        return getOrDefault(json, "cubeAreas", new JsonArray());
    }

    private static JsonElement getHighNoiseOctaves(JsonObject json) {
        return getOrDefault(json, "highNoiseOctaves", new JsonPrimitive(16));
    }

    private static JsonElement getHighNoiseFrequencyZ(JsonObject json) {
        return getOrDefault(json, "highNoiseFrequencyZ",
                new JsonPrimitive(ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ));
    }

    private static JsonElement getHighNoiseFrequencyY(JsonObject json) {
        return getOrDefault(json, "highNoiseFrequencyY",
                new JsonPrimitive(ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_Y));
    }

    private static JsonElement getHighNoiseFrequencyX(JsonObject json) {
        return getOrDefault(json, "highNoiseFrequencyX",
                new JsonPrimitive(ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ));
    }

    private static JsonElement getHighNoiseOffset(JsonObject json) {
        return getOrDefault(json, "highNoiseOffset", new JsonPrimitive(0));
    }

    private static JsonElement getHighNoiseFactor(JsonObject json) {
        return getOrDefault(json, "highNoiseFactor", new JsonPrimitive(1));
    }

    private static JsonElement getLowNoiseFrequencyZ(JsonObject json) {
        return getOrDefault(json, "lowNoiseFrequencyZ",
                new JsonPrimitive(ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ));
    }

    private static JsonElement getLowNoiseFrequencyY(JsonObject json) {
        return getOrDefault(json, "lowNoiseFrequencyY",
                new JsonPrimitive(ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_Y));
    }

    private static JsonElement getLowNoiseFrequencyX(JsonObject json) {
        return getOrDefault(json, "lowNoiseFrequencyX",
                new JsonPrimitive(ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ));
    }

    private static JsonElement getLowNoiseOffset(JsonObject json) {
        return getOrDefault(json, "lowNoiseOffset", new JsonPrimitive(0));
    }

    private static JsonElement getLowNoiseFactor(JsonObject json) {
        return getOrDefault(json, "lowNoiseFactor", new JsonPrimitive(1));
    }

    private static JsonElement getLowNoiseOctaves(JsonObject json) {
        return getOrDefault(json, "lowNoiseOctaves", new JsonPrimitive(16));
    }

    private static JsonElement getSelectorNoiseOctaves(JsonObject json) {
        return getOrDefault(json, "selectorNoiseOctaves", new JsonPrimitive(8));
    }

    private static JsonElement getSelectorNoiseFrequencyZ(JsonObject json) {
        return getOrDefault(json, "selectorNoiseFrequencyZ",
                new JsonPrimitive(ConversionUtils.VANILLA_SELECTOR_NOISE_FREQUENCY_XZ));
    }

    private static JsonElement getSelectorNoiseFrequencyX(JsonObject json) {
        return getOrDefault(json, "selectorNoiseFrequencyX",
                new JsonPrimitive(ConversionUtils.VANILLA_SELECTOR_NOISE_FREQUENCY_XZ));
    }

    private static JsonElement getSelectorNoiseFrequencyY(JsonObject json) {
        return getOrDefault(json, "selectorNoiseFrequencyY",
                new JsonPrimitive(ConversionUtils.VANILLA_SELECTOR_NOISE_FREQUENCY_Y));
    }

    private static JsonElement getSelectorNoiseOffset(JsonObject json) {
        return getOrDefault(json, "selectorNoiseOffset",
                new JsonPrimitive(ConversionUtils.VANILLA_SELECTOR_NOISE_OFFSET));
    }

    private static JsonElement getSelectorNoiseFactor(JsonObject json) {
        return getOrDefault(json, "selectorNoiseFactor",
                new JsonPrimitive(ConversionUtils.VANILLA_SELECTOR_NOISE_FACTOR));
    }

    private static JsonElement getDepthNoiseOctaves(JsonObject json) {
        return getOrDefault(json, "depthNoiseOctaves", new JsonPrimitive(16));
    }

    private static JsonElement getDepthNoiseFrequencyZ(JsonObject json) {
        return getOrDefault(json, "depthNoiseFrequencyZ",
                new JsonPrimitive(ConversionUtils.VANILLA_DEPTH_NOISE_FREQUENCY));
    }

    private static JsonElement getDepthNoiseFrequencyX(JsonObject json) {
        return getOrDefault(json, "depthNoiseFrequencyX",
                new JsonPrimitive(ConversionUtils.VANILLA_DEPTH_NOISE_FREQUENCY));
    }

    private static JsonElement getDepthNoiseOffset(JsonObject json) {
        return getOrDefault(json, "depthNoiseOffset", new JsonPrimitive(0));
    }

    private static JsonElement getDepthNoiseFactor(JsonObject json) {
        return getOrDefault(json, "depthNoiseFactor", new JsonPrimitive(ConversionUtils.VANILLA_DEPTH_NOISE_FACTOR));
    }

    private static JsonElement getHeightOffset(JsonObject json) {
        return getOrDefault(json, "heightOffset", new JsonPrimitive(64));
    }

    private static JsonElement getHeightFactor(JsonObject json) {
        return getOrDefault(json, "heightFactor", new JsonPrimitive(64));
    }

    private static JsonElement getHeightVariationOffset(JsonObject json) {
        return getOrDefault(json, "heightVariationOffset", new JsonPrimitive(0));
    }

    private static JsonElement getSpecialHeightVariationFactorBelowAverageY(JsonObject json) {
        return getOrDefault(json, "specialHeightVariationFactorBelowAverageY", new JsonPrimitive(0.25f));
    }

    private static JsonElement getHeightVariationFactor(JsonObject json) {
        return getOrDefault(json, "heightVariationFactor", new JsonPrimitive(64));
    }

    private static JsonElement getActualHeight(JsonObject json) {
        if (json.has("heightOffset") && json.has("heightVariationOffset") && json.has("heightFactor")) {
            float heightVariationOffset = json.get("heightVariationOffset").getAsFloat();
            float offset = json.get("heightOffset").getAsFloat();
            float factor = json.get("heightFactor").getAsFloat();
            return getOrDefault(json, "actualHeight", new JsonPrimitive((offset + heightVariationOffset
                    + Math.max(factor * 2 + heightVariationOffset, factor + heightVariationOffset * 2))));
        }
        return getOrDefault(json, "actualHeight", new JsonPrimitive(64));
    }

    private static JsonElement getExpectedBaseHeight(JsonObject json) {
        if (json.has("expectedBaseHeight"))
            return json.get("expectedBaseHeight");
        return getOrDefault(json, "heightOffset", new JsonPrimitive(64));
    }

    private static JsonElement getExpectedHeightVariation(JsonObject json) {
        return getOrDefault(json, "expectedHeightVariation", getOrDefault(json, "heightFactor", new JsonPrimitive(64)));
    }

    private static JsonElement getPeriodicGaussianOres(JsonObject oldRoot) {
        if (oldRoot.has("periodicGaussianOres"))
            return oldRoot.get("periodicGaussianOres");
        Gson gson = CustomGeneratorSettings.gson(false);
        JsonArray periodicGaussianOres = new JsonArray();
        JsonObject obj = convertGaussianPeriodicOre(gson, oldRoot, "lapisLazuli", Blocks.LAPIS_ORE.getDefaultState(),
                null);
        if (obj != null) {
            periodicGaussianOres.add(obj);
        }
        return periodicGaussianOres;
    }

    private static JsonElement getStandardOres(JsonObject oldRoot) {
        if (oldRoot.has("standardOres"))
            return oldRoot.get("standardOres");
        Gson gson = CustomGeneratorSettings.gson(false);
        JsonArray standardOres = new JsonArray();
        for (int i = 0; i < standard.length; i++) {
            JsonObject obj = convertStandardOre(gson, oldRoot, standard[i], standardBlockstates[i], standardBiomes[i]);
            if (obj != null) {
                standardOres.add(obj);
            }
        }
        return standardOres;
    }

    private static JsonElement getRiverSize(JsonObject json) {
        return getOrDefault(json, "riverSize", new JsonPrimitive(4));
    }

    private static JsonElement getBiomeSize(JsonObject json) {
        return getOrDefault(json, "biomeSize", new JsonPrimitive(4));
    }

    private static JsonElement getBiome(JsonObject json) {
        return getOrDefault(json, "biome", new JsonPrimitive(-1));
    }

    private static JsonElement getLavaOceans(JsonObject json) {
        return getOrDefault(json, "lavaOceans", new JsonPrimitive(false));
    }

    private static JsonElement getAboveSeaLavaLakeRarity(JsonObject json) {
        return getOrDefault(json, "aboveSeaLavaLakeRarity", new JsonPrimitive(13));
    }

    private static JsonElement getLavaLakeRarity(JsonObject json) {
        return getOrDefault(json, "lavaLakeRarity", new JsonPrimitive(8));
    }

    private static JsonElement getLavaLakes(JsonObject json) {
        return getOrDefault(json, "lavaLakes", new JsonPrimitive(true));
    }

    private static JsonElement getWaterLakeRarity(JsonObject json) {
        return getOrDefault(json, "waterLakeRarity", new JsonPrimitive(4));
    }

    private static JsonElement getWaterLakes(JsonObject json) {
        return getOrDefault(json, "waterLakes", new JsonPrimitive(true));
    }

    private static JsonElement getDungeonCount(JsonObject json) {
        return getOrDefault(json, "dungeonCount", new JsonPrimitive(7));
    }

    private static JsonElement getDungeons(JsonObject json) {
        return getOrDefault(json, "dungeons", new JsonPrimitive(true));
    }

    private static JsonElement getRavines(JsonObject json) {
        return getOrDefault(json, "ravines", new JsonPrimitive(true));
    }

    private static JsonElement getWoodlandMansions(JsonObject json) {
        return getOrDefault(json, "woodlandMansions", new JsonPrimitive(true));
    }

    private static JsonElement getOceanMonuments(JsonObject json) {
        return getOrDefault(json, "oceanMonuments", new JsonPrimitive(true));
    }

    private static JsonElement getTemples(JsonObject json) {
        return getOrDefault(json, "temples", new JsonPrimitive(true));
    }

    private static JsonElement getMineshafts(JsonObject json) {
        return getOrDefault(json, "mineshafts", new JsonPrimitive(true));
    }

    private static JsonElement getVillages(JsonObject json) {
        return getOrDefault(json, "villages", new JsonPrimitive(true));
    }

    private static JsonElement getAlternateStrongholdsPositions(JsonObject json) {
        return getOrDefault(json, "alternateStrongholdsPositions", new JsonPrimitive(false));
    }

    private static JsonElement getStrongholds(JsonObject json) {
        return getOrDefault(json, "strongholds", new JsonPrimitive(true));
    }

    private static JsonElement getCaves(JsonObject json) {
        return getOrDefault(json, "caves", new JsonPrimitive(true));
    }

    private static JsonElement getWaterLevel(JsonObject json) {
        return getOrDefault(json, "waterLevel", new JsonPrimitive(63));
    }

    private static JsonElement getOrDefault(JsonObject source, String name, JsonElement jsonElement) {
        if (source.has(name))
            return source.get(name);
        return jsonElement;
    }

    private static JsonObject convertStandardOre(Gson gson, JsonObject root, String ore, IBlockState state,
            Biome[] biomes) {
        if (!root.has(ore + "SpawnTries")) {
            // some old saves are broken, especially 1.11.2 ones from the
            // 1.12.2->1.11.2 backport, build 847
            // this avoids adding a lot of air ores
            return null;
        }

        JsonObject obj = new JsonObject();
        obj.add("blockstate", gson.toJsonTree(state));
        if (biomes != null) {
            obj.add("biomes", gson.toJsonTree(biomes));
        }
        if (root.has(ore + "SpawnSize")) {
            obj.add("spawnSize", root.remove(ore + "SpawnSize"));
        } else {
            // emerald doesn't have size defined in the old format
            obj.add("spawnSize", new JsonPrimitive(3));
        }
        obj.add("spawnTries", root.remove(ore + "SpawnTries"));
        obj.add("spawnProbability", root.remove(ore + "SpawnProbability"));
        obj.add("minHeight", root.remove(ore + "SpawnMinHeight"));
        obj.add("maxHeight", root.remove(ore + "SpawnMaxHeight"));
        return obj;
    }

    private static JsonObject convertGaussianPeriodicOre(Gson gson, JsonObject root, String ore, IBlockState state,
            Biome[] biomes) {
        JsonObject obj = convertStandardOre(gson, root, ore, state, biomes);
        if (obj == null) {
            return null;
        }
        obj.add("heightMean", root.remove(ore + "HeightMean"));
        obj.add("heightStdDeviation", root.remove(ore + "HeightStdDeviation"));
        obj.add("heightSpacing", root.remove(ore + "HeightSpacing"));
        return obj;
    }
}
