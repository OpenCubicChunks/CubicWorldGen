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

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

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
                    Biomes.MUTATED_EXTREME_HILLS, Biomes.MUTATED_EXTREME_HILLS_WITH_TREES }, // monster egg
            { Biomes.MESA, Biomes.MESA_CLEAR_ROCK, Biomes.MESA_ROCK, Biomes.MUTATED_MESA,
                    Biomes.MUTATED_MESA_CLEAR_ROCK, Biomes.MUTATED_MESA_ROCK },// mesa gold
    };

    private static boolean isVersionUpToDate(JsonObject root) {
        return root.has("version") && root.get("version").getAsInt() >= 3;
    }

    public static boolean isUpToDate(String json) {
        return isUpToDate(stringToJson(json));
    }

    public static boolean isUpToDate(JsonObject root) {
        boolean rootIsUpToDate = isVersionUpToDate(root);
        if (!rootIsUpToDate)
            return false;
        if (root.has("cubeAreas") && root.get("cubeAreas").isJsonArray()) {
            JsonArray array = root.get("cubeAreas").getAsJsonArray();
            for (JsonElement entry : array) {
                JsonArray mapEntry = entry.getAsJsonArray();
                JsonObject value = mapEntry.get(1).getAsJsonObject();
                if (!isUpToDate(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String fixGeneratorOptions(String json, @Nullable JsonObject parent) {
        Gson gson = CustomGeneratorSettings.gson();
        JsonObject oldRoot = CustomGeneratorSettingsFixer.stringToJson(json);
        JsonObject newRoot = stringToJson("{}");
        newRoot.add("version", new JsonPrimitive(3));
        newRoot.add("waterLevel", getWaterLevel(oldRoot, parent));
        newRoot.add("caves", getCaves(oldRoot, parent));
        newRoot.add("strongholds", getStrongholds(oldRoot, parent));
        newRoot.add("alternateStrongholdsPositions", getAlternateStrongholdsPositions(oldRoot, parent));
        newRoot.add("villages", getVillages(oldRoot, parent));
        newRoot.add("mineshafts", getMineshafts(oldRoot, parent));
        newRoot.add("temples", getTemples(oldRoot, parent));
        newRoot.add("oceanMonuments", getOceanMonuments(oldRoot, parent));
        newRoot.add("woodlandMansions", getWoodlandMansions(oldRoot, parent));
        newRoot.add("ravines", getRavines(oldRoot, parent));
        newRoot.add("dungeons", getDungeons(oldRoot, parent));
        newRoot.add("dungeonCount", getDungeonCount(oldRoot, parent));
        newRoot.add("waterLakes", getWaterLakes(oldRoot, parent));
        newRoot.add("waterLakeRarity", getWaterLakeRarity(oldRoot, parent));
        newRoot.add("lavaLakes", getLavaLakes(oldRoot, parent));
        newRoot.add("lavaLakeRarity", getLavaLakeRarity(oldRoot, parent));
        newRoot.add("aboveSeaLavaLakeRarity", getAboveSeaLavaLakeRarity(oldRoot, parent));
        newRoot.add("lavaOceans", getLavaOceans(oldRoot, parent));
        newRoot.add("biome", getBiome(oldRoot, parent));
        newRoot.add("biomeSize", getBiomeSize(oldRoot, parent));
        newRoot.add("riverSize", getRiverSize(oldRoot, parent));
        newRoot.add("standardOres", getStandardOres(oldRoot, parent));
        newRoot.add("periodicGaussianOres", getPeriodicGaussianOres(oldRoot, parent));
        newRoot.add("expectedBaseHeight", getExpectedBaseHeight(oldRoot, parent));
        newRoot.add("expectedHeightVariation", getExpectedHeightVariation(oldRoot, parent));
        newRoot.add("actualHeight", getActualHeight(oldRoot, parent));
        newRoot.add("heightVariationFactor", getHeightVariationFactor(oldRoot, parent));
        newRoot.add("specialHeightVariationFactorBelowAverageY",
                getSpecialHeightVariationFactorBelowAverageY(oldRoot, parent));
        newRoot.add("heightVariationOffset", getHeightVariationOffset(oldRoot, parent));
        newRoot.add("heightFactor", getHeightFactor(oldRoot, parent));
        newRoot.add("heightOffset", getHeightOffset(oldRoot, parent));
        newRoot.add("depthNoiseFactor", getDepthNoiseFactor(oldRoot, parent));
        newRoot.add("depthNoiseOffset", getDepthNoiseOffset(oldRoot, parent));
        newRoot.add("depthNoiseFrequencyX", getDepthNoiseFrequencyX(oldRoot, parent));
        newRoot.add("depthNoiseFrequencyZ", getDepthNoiseFrequencyZ(oldRoot, parent));
        newRoot.add("depthNoiseOctaves", getDepthNoiseOctaves(oldRoot, parent));
        newRoot.add("selectorNoiseFactor", getSelectorNoiseFactor(oldRoot, parent));
        newRoot.add("selectorNoiseOffset", getSelectorNoiseOffset(oldRoot, parent));
        newRoot.add("selectorNoiseFrequencyX", getSelectorNoiseFrequencyX(oldRoot, parent));
        newRoot.add("selectorNoiseFrequencyY", getSelectorNoiseFrequencyY(oldRoot, parent));
        newRoot.add("selectorNoiseFrequencyZ", getSelectorNoiseFrequencyZ(oldRoot, parent));
        newRoot.add("selectorNoiseOctaves", getSelectorNoiseOctaves(oldRoot, parent));
        newRoot.add("lowNoiseFactor", getLowNoiseFactor(oldRoot, parent));
        newRoot.add("lowNoiseOffset", getLowNoiseOffset(oldRoot, parent));
        newRoot.add("lowNoiseFrequencyX", getLowNoiseFrequencyX(oldRoot, parent));
        newRoot.add("lowNoiseFrequencyY", getLowNoiseFrequencyY(oldRoot, parent));
        newRoot.add("lowNoiseFrequencyZ", getLowNoiseFrequencyZ(oldRoot, parent));
        newRoot.add("lowNoiseOctaves", getLowNoiseOctaves(oldRoot, parent));
        newRoot.add("highNoiseFactor", getHighNoiseFactor(oldRoot, parent));
        newRoot.add("highNoiseOffset", getHighNoiseOffset(oldRoot, parent));
        newRoot.add("highNoiseFrequencyX", getHighNoiseFrequencyX(oldRoot, parent));
        newRoot.add("highNoiseFrequencyY", getHighNoiseFrequencyY(oldRoot, parent));
        newRoot.add("highNoiseFrequencyZ", getHighNoiseFrequencyZ(oldRoot, parent));
        newRoot.add("highNoiseOctaves", getHighNoiseOctaves(oldRoot, parent));
        newRoot.add("replacerConfig", getReplacerConfig(oldRoot, parent));
        newRoot.add("cubeAreas", getCubeAreas(oldRoot));

        String newGeneratorOptions = gson.toJson(newRoot).replaceAll("cubicchunks:", MODID + ":");
        return newGeneratorOptions;
    }

    private static JsonElement getReplacerConfig(JsonObject json, @Nullable JsonObject parent) {
        JsonReader reader = new JsonReader(new StringReader(
                "{\"defaults\":{\"cubicgen:biome_fill_noise_octaves\":4.0,\"cubicgen:ocean_block\":{\"Properties\":{\"level\":\"0\"},\"Name\":\"minecraft:water\"},\"cubicgen:height_scale\":64.0,\"cubicgen:biome_fill_noise_freq\":0.0078125,\"cubicgen:water_level\":63.0,\"cubicgen:biome_fill_depth_factor\":2.3333333333333335,\"cubicgen:terrain_fill_block\":{\"Properties\":{\"variant\":\"stone\"},\"Name\":\"minecraft:stone\"},\"cubicgen:mesa_depth\":16.0,\"cubicgen:biome_fill_depth_offset\":3.0,\"cubicgen:horizontal_gradient_depth_decrease_weight\":1.0,\"cubicgen:height_offset\":64.0},\"overrides\":{}}"));
        JsonObject biomeBlockReplacerConfigDefaultJson = new JsonParser().parse(reader).getAsJsonObject();
        return getOrDefault(json, parent, "replacerConfig", biomeBlockReplacerConfigDefaultJson);
    }

    private static JsonElement getCubeAreas(JsonObject json) {
        JsonArray cubeAreas = new JsonArray();
        if (json.has("cubeAreas") && json.get("cubeAreas").isJsonArray()) {
            JsonArray array = json.get("cubeAreas").getAsJsonArray();
            for (JsonElement entry : array) {
                JsonArray mapEntry = entry.getAsJsonArray();
                JsonElement key = mapEntry.get(0);
                JsonObject value = stringToJson(fixGeneratorOptions(
                        CustomGeneratorSettings.gson().toJson(mapEntry.get(1).getAsJsonObject()), json));
                JsonArray newEntry = new JsonArray();
                newEntry.add(key);
                newEntry.add(value);
                cubeAreas.add(newEntry);
            }
        }
        return cubeAreas;
    }

    private static JsonElement getHighNoiseOctaves(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "highNoiseOctaves", new JsonPrimitive(16));
    }

    private static JsonElement getHighNoiseFrequencyZ(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "highNoiseFrequencyZ", new JsonPrimitive(0.005221649));
    }

    private static JsonElement getHighNoiseFrequencyY(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "highNoiseFrequencyY", new JsonPrimitive(0.0026108245));
    }

    private static JsonElement getHighNoiseFrequencyX(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "highNoiseFrequencyX", new JsonPrimitive(0.005221649));
    }

    private static JsonElement getHighNoiseOffset(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "highNoiseOffset", new JsonPrimitive(0));
    }

    private static JsonElement getHighNoiseFactor(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "highNoiseFactor", new JsonPrimitive(1));
    }

    private static JsonElement getLowNoiseFrequencyZ(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lowNoiseFrequencyZ", new JsonPrimitive(0.005221649));
    }

    private static JsonElement getLowNoiseFrequencyY(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lowNoiseFrequencyY", new JsonPrimitive(0.0026108245));
    }

    private static JsonElement getLowNoiseFrequencyX(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lowNoiseFrequencyX", new JsonPrimitive(0.005221649));
    }

    private static JsonElement getLowNoiseOffset(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lowNoiseOffset", new JsonPrimitive(0));
    }

    private static JsonElement getLowNoiseFactor(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lowNoiseFactor", new JsonPrimitive(1));
    }

    private static JsonElement getLowNoiseOctaves(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lowNoiseOctaves", new JsonPrimitive(16));
    }

    private static JsonElement getSelectorNoiseOctaves(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "selectorNoiseOctaves", new JsonPrimitive(8));
    }

    private static JsonElement getSelectorNoiseFrequencyZ(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "selectorNoiseFrequencyZ", new JsonPrimitive(0.016709277));
    }

    private static JsonElement getSelectorNoiseFrequencyX(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "selectorNoiseFrequencyX", new JsonPrimitive(0.016709277));
    }

    private static JsonElement getSelectorNoiseFrequencyY(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "selectorNoiseFrequencyY", new JsonPrimitive(0.008354639));
    }

    private static JsonElement getSelectorNoiseOffset(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "selectorNoiseOffset", new JsonPrimitive(0.5));
    }

    private static JsonElement getSelectorNoiseFactor(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "selectorNoiseFactor", new JsonPrimitive(12.75));
    }

    private static JsonElement getDepthNoiseOctaves(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "depthNoiseOctaves", new JsonPrimitive(16));
    }

    private static JsonElement getDepthNoiseFrequencyZ(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "depthNoiseFrequencyZ", new JsonPrimitive(0.0015258789));
    }

    private static JsonElement getDepthNoiseFrequencyX(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "depthNoiseFrequencyX", new JsonPrimitive(0.0015258789));
    }

    private static JsonElement getDepthNoiseOffset(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "depthNoiseOffset", new JsonPrimitive(0));
    }

    private static JsonElement getDepthNoiseFactor(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "depthNoiseFactor", new JsonPrimitive(1.024));
    }

    private static JsonElement getHeightOffset(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "heightOffset", new JsonPrimitive(64));
    }

    private static JsonElement getHeightFactor(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "heightFactor", new JsonPrimitive(64));
    }

    private static JsonElement getHeightVariationOffset(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "heightVariationOffset", new JsonPrimitive(0));
    }

    private static JsonElement getSpecialHeightVariationFactorBelowAverageY(JsonObject json,
            @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "specialHeightVariationFactorBelowAverageY", new JsonPrimitive(0.25f));
    }

    private static JsonElement getHeightVariationFactor(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "heightVariationFactor", new JsonPrimitive(64));
    }

    private static JsonElement getActualHeight(JsonObject json, @Nullable JsonObject parent) {
        if (json.has("heightOffset") && json.has("heightVariationOffset") && json.has("heightFactor")) {
            float heightVariationOffset = json.get("heightVariationOffset").getAsFloat();
            float offset = json.get("heightOffset").getAsFloat();
            float factor = json.get("heightFactor").getAsFloat();
            return getOrDefault(json, parent, "actualHeight", new JsonPrimitive((offset + heightVariationOffset
                    + Math.max(factor * 2 + heightVariationOffset, factor + heightVariationOffset * 2))));
        }
        return getOrDefault(json, parent, "actualHeight", new JsonPrimitive(64));
    }

    private static JsonElement getExpectedBaseHeight(JsonObject json, @Nullable JsonObject parent) {
        if (json.has("expectedBaseHeight"))
            return json.get("expectedBaseHeight");
        return getOrDefault(json, parent, "heightOffset", new JsonPrimitive(64));
    }

    private static JsonElement getExpectedHeightVariation(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "expectedHeightVariation",
                getOrDefault(json, parent, "heightFactor", new JsonPrimitive(64)));
    }

    private static JsonElement getPeriodicGaussianOres(JsonObject oldRoot, JsonObject parent) {
        if (oldRoot.has("periodicGaussianOres"))
            return oldRoot.get("periodicGaussianOres");
        if (parent != null && parent.has("periodicGaussianOres"))
            return parent.get("periodicGaussianOres");
        Gson gson = CustomGeneratorSettings.gson();
        JsonArray periodicGaussianOres = new JsonArray();
        JsonObject obj = convertGaussianPeriodicOre(gson, oldRoot, "lapisLazuli", Blocks.LAPIS_ORE.getDefaultState(),
                null);
        if (obj != null) {
            periodicGaussianOres.add(obj);
        }
        return periodicGaussianOres;
    }

    private static JsonElement getStandardOres(JsonObject oldRoot, JsonObject parent) {
        if (oldRoot.has("standardOres"))
            return oldRoot.get("standardOres");
        if (parent != null && parent.has("standardOres"))
            return parent.get("standardOres");
        Gson gson = CustomGeneratorSettings.gson();
        JsonArray standardOres = new JsonArray();
        for (int i = 0; i < standard.length; i++) {
            JsonObject obj = convertStandardOre(gson, oldRoot, standard[i], standardBlockstates[i], standardBiomes[i]);
            if (obj != null) {
                standardOres.add(obj);
            }
        }
        return standardOres;
    }

    private static JsonElement getRiverSize(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "riverSize", new JsonPrimitive(4));
    }

    private static JsonElement getBiomeSize(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "biomeSize", new JsonPrimitive(4));
    }

    private static JsonElement getBiome(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "biome", new JsonPrimitive(-1));
    }

    private static JsonElement getLavaOceans(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lavaOceans", new JsonPrimitive(false));
    }

    private static JsonElement getAboveSeaLavaLakeRarity(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "aboveSeaLavaLakeRarity", new JsonPrimitive(13));
    }

    private static JsonElement getLavaLakeRarity(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lavaLakeRarity", new JsonPrimitive(8));
    }

    private static JsonElement getLavaLakes(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "lavaLakes", new JsonPrimitive(true));
    }

    private static JsonElement getWaterLakeRarity(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "waterLakeRarity", new JsonPrimitive(4));
    }

    private static JsonElement getWaterLakes(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "waterLakes", new JsonPrimitive(true));
    }

    private static JsonElement getDungeonCount(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "dungeonCount", new JsonPrimitive(7));
    }

    private static JsonElement getDungeons(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "dungeons", new JsonPrimitive(true));
    }

    private static JsonElement getRavines(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "ravines", new JsonPrimitive(true));
    }

    private static JsonElement getWoodlandMansions(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "woodlandMansions", new JsonPrimitive(true));
    }

    private static JsonElement getOceanMonuments(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "oceanMonuments", new JsonPrimitive(true));
    }

    private static JsonElement getTemples(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "temples", new JsonPrimitive(true));
    }

    private static JsonElement getMineshafts(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "mineshafts", new JsonPrimitive(true));
    }

    private static JsonElement getVillages(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "villages", new JsonPrimitive(true));
    }

    private static JsonElement getAlternateStrongholdsPositions(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "alternateStrongholdsPositions", new JsonPrimitive(false));
    }

    private static JsonElement getStrongholds(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "strongholds", new JsonPrimitive(true));
    }

    private static JsonElement getCaves(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "caves", new JsonPrimitive(true));
    }

    private static JsonElement getWaterLevel(JsonObject json, @Nullable JsonObject parent) {
        return getOrDefault(json, parent, "waterLevel", new JsonPrimitive(63));
    }

    private static JsonElement getOrDefault(JsonObject source, @Nullable JsonObject parent, String name,
            JsonElement jsonElement) {
        if (source.has(name))
            return source.get(name);
        if (parent != null && parent.has(name))
            return parent.get(name);
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

    public static JsonObject stringToJson(String jsonString) {
        if (jsonString.isEmpty()) {
            // avoid JsonNull
            jsonString = "{}";
        }
        JsonReader reader = new JsonReader(new StringReader(jsonString));
        return new JsonParser().parse(reader).getAsJsonObject();
    }
}
