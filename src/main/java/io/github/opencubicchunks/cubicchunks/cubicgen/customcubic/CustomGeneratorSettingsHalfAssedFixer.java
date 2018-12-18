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

public class CustomGeneratorSettingsHalfAssedFixer {
    
    public static String fixGeneratorOptions(String generatorOptions) {
        generatorOptions = fixGeneratorOptionsVersion0(generatorOptions);
        generatorOptions = fixGeneratorOptionsVersion1(generatorOptions);
        generatorOptions = fixGeneratorOptionsVersion3(generatorOptions);
        return CustomGeneratorSettings.fromJson(generatorOptions).toJson(false);
    }
    
    public static String fixGeneratorOptionsVersion0(String generatorOptions) {
        if (generatorOptions.isEmpty()) {
            generatorOptions = new CustomGeneratorSettings().toJson(false);
        }
        Gson gson = CustomGeneratorSettings.gson(false);

        JsonReader reader = new JsonReader(new StringReader(generatorOptions));
        JsonObject root = new JsonParser().parse(reader).getAsJsonObject();

        // some old saves are broken, especially 1.11.2 ones from the
        // 1.12.2->1.11.2 backport, build 847
        // this preserves the existing ores
        JsonArray standardOres = root.has("standardOres") ? root.getAsJsonArray("standardOres") : new JsonArray();
        JsonArray periodicGaussianOres = root.has("periodicGaussianOres") ? root.getAsJsonArray("periodicGaussianOres")
                : new JsonArray();

        // kind of ugly but I don'twant to make a special class just so store
        // these 3 objects...
        String[] standard = { "dirt", "gravel", "granite", "diorite", "andesite", "coalOre", "ironOre", "goldOre",
                "redstoneOre", "diamondOre", "hillsEmeraldOre", "hillsSilverfishStone", "mesaAddedGoldOre" };
        IBlockState[] standardBlockstates = { Blocks.DIRT.getDefaultState(), Blocks.GRAVEL.getDefaultState(),
                Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE),
                Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE),
                Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),
                Blocks.COAL_ORE.getDefaultState(), Blocks.IRON_ORE.getDefaultState(), Blocks.GOLD_ORE.getDefaultState(),
                Blocks.REDSTONE_ORE.getDefaultState(), Blocks.DIAMOND_ORE.getDefaultState(),
                Blocks.EMERALD_ORE.getDefaultState(), Blocks.MONSTER_EGG.getDefaultState().withProperty(
                        BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE),
                Blocks.GOLD_ORE.getDefaultState() };
        Biome[][] standardBiomes = { null, // dirt
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
        for (int i = 0; i < standard.length; i++) {
            JsonObject obj = convertStandardOre(gson, root, standard[i], standardBlockstates[i], standardBiomes[i]);
            if (obj != null) {
                standardOres.add(obj);
            }

        }
        JsonObject lapis = convertGaussianPeriodicOre(gson, root, "lapisLazuli", Blocks.LAPIS_ORE.getDefaultState(),
                null);
        if (lapis != null) {
            periodicGaussianOres.add(lapis);
        }
        root.add("standardOres", standardOres);
        root.add("periodicGaussianOres", periodicGaussianOres);
        return gson.toJson(root);
    }

    private static JsonObject convertStandardOre(Gson gson, JsonObject root, String ore, IBlockState state, Biome[] biomes) {
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

    public static String fixGeneratorOptionsVersion1(String generatorOptions) {
        if (generatorOptions.isEmpty()) {
            generatorOptions = new CustomGeneratorSettings().toJson(false);
        }
        Gson gson = CustomGeneratorSettings.gson(false);

        JsonReader reader = new JsonReader(new StringReader(generatorOptions));
        JsonObject root = new JsonParser().parse(reader).getAsJsonObject();

        float heightVariationOffset = root.get("heightVariationOffset").getAsFloat();
        float offset = root.get("heightOffset").getAsFloat();
        float factor = root.get("heightFactor").getAsFloat();
        if (!root.has("expectedBaseHeight")) {
            root.add("expectedBaseHeight", root.get("heightOffset"));
        }
        if (!root.has("expectedHeightVariation")) {
            root.add("expectedHeightVariation", root.get("heightFactor"));
        }
        if (!root.has("actualHeight")) {
            root.add("actualHeight", new JsonPrimitive((offset + heightVariationOffset
                    + Math.max(factor * 2 + heightVariationOffset, factor + heightVariationOffset * 2))));
        }
        if (!root.has("cubeAreas")) {
            root.add("cubeAreas", new JsonObject());
        }
        if (!root.has("replacerConfig")) {
            JsonObject replacerConf = new JsonObject();
            {
                JsonObject defaults = new JsonObject();
                {
                    defaults.add(MODID + ":horizontal_gradient_depth_decrease_weight", new JsonPrimitive(1.0f));
                    defaults.add(MODID + ":height_offset", new JsonPrimitive(offset));
                    JsonObject terrainfill = new JsonObject();
                    {
                        JsonObject properties = new JsonObject();
                        properties.add("variant", new JsonPrimitive("stone"));
                        terrainfill.add("Properties", properties);
                        terrainfill.add("Name", new JsonPrimitive("minecraft:stone"));
                    }
                    JsonObject oceanblock = new JsonObject();

                    {
                        JsonObject properties = new JsonObject();
                        properties.add("level", new JsonPrimitive("0"));
                        oceanblock.add("Properties", properties);
                        oceanblock.add("Name", new JsonPrimitive("minecraft:water"));
                    }
                    defaults.add(MODID + ":biome_fill_depth_offset", new JsonPrimitive(3.0f));
                    defaults.add(MODID + ":biome_fill_noise_octaves", new JsonPrimitive(4.0f));
                    defaults.add(MODID + ":height_scale", new JsonPrimitive(factor));
                    defaults.add(MODID + ":biome_fill_depth_factor", new JsonPrimitive(2.3333333333333335f));
                    defaults.add(MODID + ":mesa_depth", new JsonPrimitive(16.0f));
                    defaults.add(MODID + ":water_level", root.get("waterLevel"));
                    defaults.add(MODID + ":biome_fill_noise_freq", new JsonPrimitive(0.0078125f));
                }
                replacerConf.add("defaults", defaults);
                replacerConf.add("overrides", new JsonObject());
            }

            root.add("replacerConfig", replacerConf);
        }
        return gson.toJson(root);
    }

    public static String fixGeneratorOptionsVersion3(String generatorOptions) {
        if (generatorOptions.isEmpty()) {
            generatorOptions = new CustomGeneratorSettings().toJson(true);
        }
        // this is far simpler that walking through the json and figurring out
        // all the places where it occurs
        // instead, just do string search and replace. The string shouldn't
        // occur in any other context
        return generatorOptions.replaceAll(MODID + ":", MODID + ":");
    }
}
