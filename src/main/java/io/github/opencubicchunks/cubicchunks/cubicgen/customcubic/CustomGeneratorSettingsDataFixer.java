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
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import io.github.opencubicchunks.cubicchunks.cubicgen.ConversionUtils;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorSettings;

public class CustomGeneratorSettingsDataFixer {
    
    private static final String[] standard = {
            "dirt",
            "gravel",
            "granite",
            "diorite",
            "andesite",
            "coalOre",
            "ironOre",
            "goldOre",
            "redstoneOre",
            "diamondOre",
            "hillsEmeraldOre",
            "hillsSilverfishStone",
            "mesaAddedGoldOre"
    };
    
    private static final IBlockState[] standardBlockstates = {
            Blocks.DIRT.getDefaultState(),
            Blocks.GRAVEL.getDefaultState(),
            Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE),
            Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE),
            Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),
            Blocks.COAL_ORE.getDefaultState(),
            Blocks.IRON_ORE.getDefaultState(),
            Blocks.GOLD_ORE.getDefaultState(),
            Blocks.REDSTONE_ORE.getDefaultState(),
            Blocks.DIAMOND_ORE.getDefaultState(),
            Blocks.EMERALD_ORE.getDefaultState(),
            Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE),
            Blocks.GOLD_ORE.getDefaultState()
    };
    private static final Biome[][] standardBiomes = {
            null, // dirt
            null, // gravel
            null, // granite
            null, // diorite
            null, // andesite
            null, // coal
            null, // iron
            null, // gold
            null, // redstone
            null, // diamond
            {Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.MUTATED_EXTREME_HILLS,
                    Biomes.MUTATED_EXTREME_HILLS_WITH_TREES},//emerald
            {Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.MUTATED_EXTREME_HILLS,
                    Biomes.MUTATED_EXTREME_HILLS_WITH_TREES},//monster egg
            {Biomes.MESA, Biomes.MESA_CLEAR_ROCK, Biomes.MESA_ROCK, Biomes.MUTATED_MESA, Biomes.MUTATED_MESA_CLEAR_ROCK,
                    Biomes.MUTATED_MESA_ROCK},//mesa gold
    };

    @SuppressWarnings("unused")
    private static CustomGeneratorSettings fromVanilla(ChunkGeneratorSettings settings) {
        CustomGeneratorSettings obj = CustomGeneratorSettings.defaults();

        obj.lowNoiseFactor = 512.0f / settings.lowerLimitScale;
        obj.highNoiseFactor = 512.0f / settings.upperLimitScale;

        obj.depthNoiseFrequencyX = ConversionUtils.frequencyFromVanilla(settings.depthNoiseScaleX, 16);
        obj.depthNoiseFrequencyZ = ConversionUtils.frequencyFromVanilla(settings.depthNoiseScaleZ, 16);
        // settings.depthNoiseScaleExponent is ignored by vanilla

        obj.selectorNoiseFrequencyX = ConversionUtils.frequencyFromVanilla(settings.coordinateScale / settings.mainNoiseScaleX, 8);
        obj.selectorNoiseFrequencyY = ConversionUtils.frequencyFromVanilla(settings.heightScale / settings.mainNoiseScaleY, 8);
        obj.selectorNoiseFrequencyZ = ConversionUtils.frequencyFromVanilla(settings.coordinateScale / settings.mainNoiseScaleZ, 8);

        obj.lowNoiseFrequencyX = ConversionUtils.frequencyFromVanilla(settings.coordinateScale, 16);
        obj.lowNoiseFrequencyY = ConversionUtils.frequencyFromVanilla(settings.heightScale, 16);
        obj.lowNoiseFrequencyZ = ConversionUtils.frequencyFromVanilla(settings.coordinateScale, 16);

        obj.highNoiseFrequencyX = ConversionUtils.frequencyFromVanilla(settings.coordinateScale, 16);
        obj.highNoiseFrequencyY = ConversionUtils.frequencyFromVanilla(settings.heightScale, 16);
        obj.highNoiseFrequencyZ = ConversionUtils.frequencyFromVanilla(settings.coordinateScale, 16);

        return obj;
    }

    public static String fixGeneratorOptions(String generatorOptionsToFix) {
        String newGeneratorOptions = CustomGeneratorSettings.defaults().toJson(true);
        Gson gson = CustomGeneratorSettings.gson(false);
        
        JsonReader readerToFix = new JsonReader(new StringReader(generatorOptionsToFix));
        JsonObject rootToFix = new JsonParser().parse(readerToFix).getAsJsonObject();

        JsonReader newReader = new JsonReader(new StringReader(newGeneratorOptions));
        JsonObject newRoot = new JsonParser().parse(newReader).getAsJsonObject();

        for(Entry<String, JsonElement> entry:rootToFix.entrySet()) {
            if(newRoot.has(entry.getKey())) {
                newRoot.add(entry.getKey(), entry.getValue());
            }
        }
        
        if (!rootToFix.has("standardOres")) {
            JsonArray standardOres = new JsonArray();
            for (int i = 0; i < standard.length; i++) {
                JsonObject obj = convertStandardOre(gson, rootToFix, standard[i], standardBlockstates[i],
                        standardBiomes[i]);
                if (obj != null) {
                    standardOres.add(obj);
                }
            }
            newRoot.add("standardOres", standardOres);
        }
        if (!rootToFix.has("periodicGaussianOres")) {
            JsonArray periodicGaussianOres = new JsonArray();
            JsonObject obj = convertGaussianPeriodicOre(gson, rootToFix, "lapisLazuli", Blocks.LAPIS_ORE.getDefaultState(),
                    null);
            if (obj != null) {
                periodicGaussianOres.add(obj);
            }
            newRoot.add("periodicGaussianOres", periodicGaussianOres);
        }
        
        if (!rootToFix.has("expectedBaseHeight") && rootToFix.has("heightOffset")) {
            newRoot.add("expectedBaseHeight", rootToFix.get("heightOffset"));
        }
        
        if (!rootToFix.has("expectedHeightVariation") && rootToFix.has("heightFactor")) {
            newRoot.add("expectedHeightVariation", rootToFix.get("heightFactor"));
        }
        
        if (!rootToFix.has("actualHeight") 
                && rootToFix.has("heightOffset") 
                && rootToFix.has("heightVariationOffset")
                && rootToFix.has("heightFactor")) {
            float heightVariationOffset = rootToFix.get("heightVariationOffset").getAsFloat();
            float offset = rootToFix.get("heightOffset").getAsFloat();
            float factor = rootToFix.get("heightFactor").getAsFloat();
            newRoot.add("actualHeight", new JsonPrimitive((offset + heightVariationOffset
                    + Math.max(factor * 2 + heightVariationOffset, factor + heightVariationOffset * 2))));
        }
        
        newGeneratorOptions = gson.toJson(newRoot).replaceAll("cubicchunks:", MODID + ":");
        return newGeneratorOptions;
    }
    
    private static JsonObject convertStandardOre(Gson gson, JsonObject root, String ore, IBlockState state, Biome[] biomes) {
        if (!root.has(ore + "SpawnTries")) {
            // some old saves are broken, especially 1.11.2 ones from the 1.12.2->1.11.2 backport, build 847
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

    private static JsonObject convertGaussianPeriodicOre(Gson gson, JsonObject root, String ore, IBlockState state, Biome[] biomes) {
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
