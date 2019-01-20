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
package io.github.opencubicchunks.cubicchunks.cubicgen;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettingsFixer;
import io.github.opencubicchunks.cubicchunks.cubicgen.testutil.MinecraftEnvironment;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TestCustomGeneratorSettingsFixer {
    String cc655ExamplePreset = "{\n" + 
            "   \"waterLevel\":100,\n" + 
            "   \"caves\":false,\n" + 
            "   \"strongholds\":false,\n" + 
            "   \"villages\":false,\n" + 
            "   \"mineshafts\":false,\n" + 
            "   \"temples\":false,\n" + 
            "   \"oceanMonuments\":false,\n" + 
            "   \"woodlandMansions\":false,\n" + 
            "   \"ravines\":false,\n" + 
            "   \"dungeons\":false,\n" + 
            "   \"dungeonCount\":101,\n" + 
            "   \"waterLakes\":false,\n" + 
            "   \"waterLakeRarity\":102,\n" + 
            "   \"lavaLakes\":false,\n" + 
            "   \"lavaLakeRarity\":103,\n" + 
            "   \"aboveSeaLavaLakeRarity\":104,\n" + 
            "   \"lavaOceans\":true,\n" + 
            "   \"biome\":1,\n" + 
            "   \"biomeSize\":104,\n" + 
            "   \"riverSize\":105,\n" + 
            "   \"dirtSpawnSize\":134,\n" + 
            "   \"dirtSpawnTries\":135,\n" + 
            "   \"dirtSpawnProbability\":136.0,\n" + 
            "   \"dirtSpawnMinHeight\":-Infinity,\n" + 
            "   \"dirtSpawnMaxHeight\":Infinity,\n" + 
            "   \"gravelSpawnSize\":137,\n" + 
            "   \"gravelSpawnTries\":138,\n" + 
            "   \"gravelSpawnProbability\":139.0,\n" + 
            "   \"gravelSpawnMinHeight\":-Infinity,\n" + 
            "   \"gravelSpawnMaxHeight\":Infinity,\n" + 
            "   \"graniteSpawnSize\":140,\n" + 
            "   \"graniteSpawnTries\":141,\n" + 
            "   \"graniteSpawnProbability\":142.0,\n" + 
            "   \"graniteSpawnMinHeight\":-Infinity,\n" + 
            "   \"graniteSpawnMaxHeight\":143.0,\n" + 
            "   \"dioriteSpawnSize\":144,\n" + 
            "   \"dioriteSpawnTries\":145,\n" + 
            "   \"dioriteSpawnProbability\":146.0,\n" + 
            "   \"dioriteSpawnMinHeight\":-Infinity,\n" + 
            "   \"dioriteSpawnMaxHeight\":147.0,\n" + 
            "   \"andesiteSpawnSize\":148,\n" + 
            "   \"andesiteSpawnTries\":149,\n" + 
            "   \"andesiteSpawnProbability\":150.0,\n" + 
            "   \"andesiteSpawnMinHeight\":-Infinity,\n" + 
            "   \"andesiteSpawnMaxHeight\":151.0,\n" + 
            "   \"coalOreSpawnSize\":152,\n" + 
            "   \"coalOreSpawnTries\":153,\n" + 
            "   \"coalOreSpawnProbability\":154.0,\n" + 
            "   \"coalOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"coalOreSpawnMaxHeight\":155.0,\n" + 
            "   \"ironOreSpawnSize\":156,\n" + 
            "   \"ironOreSpawnTries\":157,\n" + 
            "   \"ironOreSpawnProbability\":158.0,\n" + 
            "   \"ironOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"ironOreSpawnMaxHeight\":159.0,\n" + 
            "   \"goldOreSpawnSize\":160,\n" + 
            "   \"goldOreSpawnTries\":161,\n" + 
            "   \"goldOreSpawnProbability\":162.0,\n" + 
            "   \"goldOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"goldOreSpawnMaxHeight\":-163.0,\n" + 
            "   \"redstoneOreSpawnSize\":164,\n" + 
            "   \"redstoneOreSpawnTries\":165,\n" + 
            "   \"redstoneOreSpawnProbability\":166.0,\n" + 
            "   \"redstoneOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"redstoneOreSpawnMaxHeight\":-167.0,\n" + 
            "   \"diamondOreSpawnSize\":168,\n" + 
            "   \"diamondOreSpawnTries\":169,\n" + 
            "   \"diamondOreSpawnProbability\":170.0,\n" + 
            "   \"diamondOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"diamondOreSpawnMaxHeight\":-178.0,\n" + 
            "   \"lapisLazuliSpawnSize\":179,\n" + 
            "   \"lapisLazuliSpawnTries\":180,\n" + 
            "   \"lapisLazuliSpawnProbability\":181.0,\n" + 
            "   \"lapisLazuliHeightMean\":182.0,\n" + 
            "   \"lapisLazuliHeightStdDeviation\":183.0,\n" + 
            "   \"hillsEmeraldOreSpawnTries\":184,\n" + 
            "   \"hillsEmeraldOreSpawnProbability\":185.0,\n" + 
            "   \"hillsEmeraldOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"hillsEmeraldOreSpawnMaxHeight\":-186.0,\n" + 
            "   \"hillsSilverfishStoneSpawnSize\":187,\n" + 
            "   \"hillsSilverfishStoneSpawnTries\":188,\n" + 
            "   \"hillsSilverfishStoneSpawnProbability\":189.0,\n" + 
            "   \"hillsSilverfishStoneSpawnMinHeight\":-Infinity,\n" + 
            "   \"hillsSilverfishStoneSpawnMaxHeight\":190.0,\n" + 
            "   \"mesaAddedGoldOreSpawnSize\":191,\n" + 
            "   \"mesaAddedGoldOreSpawnTries\":192,\n" + 
            "   \"mesaAddedGoldOreSpawnProbability\":193.0,\n" + 
            "   \"mesaAddedGoldOreSpawnMinHeight\":-194.0,\n" + 
            "   \"mesaAddedGoldOreSpawnMaxHeight\":196.0,\n" + 
            "   \"heightVariationFactor\":106.0,\n" + 
            "   \"specialHeightVariationFactorBelowAverageY\":107.0,\n" + 
            "   \"heightVariationOffset\":108.0,\n" + 
            "   \"heightFactor\":109.0,\n" + 
            "   \"heightOffset\":110.0,\n" + 
            "   \"depthNoiseFactor\":111.0,\n" + 
            "   \"depthNoiseOffset\":112.0,\n" + 
            "   \"depthNoiseFrequencyX\":113.0,\n" + 
            "   \"depthNoiseFrequencyZ\":114.0,\n" + 
            "   \"depthNoiseOctaves\":5,\n" + 
            "   \"selectorNoiseFactor\":116.0,\n" + 
            "   \"selectorNoiseOffset\":117.0,\n" + 
            "   \"selectorNoiseFrequencyX\":118.0,\n" + 
            "   \"selectorNoiseFrequencyY\":119.0,\n" + 
            "   \"selectorNoiseFrequencyZ\":120.0,\n" + 
            "   \"selectorNoiseOctaves\":1,\n" + 
            "   \"lowNoiseFactor\":122.0,\n" + 
            "   \"lowNoiseOffset\":123.0,\n" + 
            "   \"lowNoiseFrequencyX\":124.0,\n" + 
            "   \"lowNoiseFrequencyY\":125.0,\n" + 
            "   \"lowNoiseFrequencyZ\":126.0,\n" + 
            "   \"lowNoiseOctaves\":7,\n" + 
            "   \"highNoiseFactor\":128.0,\n" + 
            "   \"highNoiseOffset\":129.0,\n" + 
            "   \"highNoiseFrequencyX\":130.0,\n" + 
            "   \"highNoiseFrequencyY\":131.0,\n" + 
            "   \"highNoiseFrequencyZ\":132.0,\n" + 
            "   \"highNoiseOctaves\":3\n" + 
            "}";
    
    String expectedConversionResult = "{\"waterLevel\":100,"
            + "\"caves\":false,"
            + "\"strongholds\":false,"
            + "\"alternateStrongholdsPositions\":false,"
            + "\"villages\":false,"
            + "\"mineshafts\":false,"
            + "\"temples\":false,"
            + "\"oceanMonuments\":false,"
            + "\"woodlandMansions\":false,"
            + "\"ravines\":false,"
            + "\"dungeons\":false,"
            + "\"dungeonCount\":101,"
            + "\"waterLakes\":false,"
            + "\"waterLakeRarity\":102,"
            + "\"lavaLakes\":false,"
            + "\"lavaLakeRarity\":103,"
            + "\"aboveSeaLavaLakeRarity\":104,"
            + "\"lavaOceans\":true,"
            + "\"biome\":1,"
            + "\"biomeSize\":104,"
            + "\"riverSize\":105,"
            + "\"standardOres\":["
            + "{\"blockstate\":{\"Properties\":{\"variant\":\"dirt\",\"snowy\":\"false\"},\"Name\":\"minecraft:dirt\"},"
            + "\"spawnSize\":134,"
            + "\"spawnTries\":135,"
            + "\"spawnProbability\":136.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":Infinity},"
            + "{\"blockstate\":{\"Name\":\"minecraft:gravel\"},"
            + "\"spawnSize\":137,"
            + "\"spawnTries\":138,"
            + "\"spawnProbability\":139.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":Infinity},"
            + "{\"blockstate\":{\"Properties\":{\"variant\":\"granite\"},\"Name\":\"minecraft:stone\"},"
            + "\"spawnSize\":140,"
            + "\"spawnTries\":141,"
            + "\"spawnProbability\":142.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":143.0},"
            + "{\"blockstate\":{\"Properties\":{\"variant\":\"diorite\"},\"Name\":\"minecraft:stone\"},"
            + "\"spawnSize\":144,"
            + "\"spawnTries\":145,"
            + "\"spawnProbability\":146.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":147.0},"
            + "{\"blockstate\":{\"Properties\":{\"variant\":\"andesite\"},\"Name\":\"minecraft:stone\"},"
            + "\"spawnSize\":148,"
            + "\"spawnTries\":149,"
            + "\"spawnProbability\":150.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":151.0},"
            + "{\"blockstate\":{\"Name\":\"minecraft:coal_ore\"},"
            + "\"spawnSize\":152,"
            + "\"spawnTries\":153,"
            + "\"spawnProbability\":154.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":155.0},"
            + "{\"blockstate\":{\"Name\":\"minecraft:iron_ore\"},"
            + "\"spawnSize\":156,"
            + "\"spawnTries\":157,"
            + "\"spawnProbability\":158.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":159.0},"
            + "{\"blockstate\":{\"Name\":\"minecraft:gold_ore\"},"
            + "\"spawnSize\":160,"
            + "\"spawnTries\":161,"
            + "\"spawnProbability\":162.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":-163.0},"
            + "{\"blockstate\":{\"Name\":\"minecraft:redstone_ore\"},"
            + "\"spawnSize\":164,"
            + "\"spawnTries\":165,"
            + "\"spawnProbability\":166.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":-167.0},"
            + "{\"blockstate\":{\"Name\":\"minecraft:diamond_ore\"},"
            + "\"spawnSize\":168,"
            + "\"spawnTries\":169,"
            + "\"spawnProbability\":170.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":-178.0},"
            + "{\"blockstate\":{\"Name\":\"minecraft:emerald_ore\"},"
            + "\"biomes\":[\"minecraft:extreme_hills\",\"minecraft:smaller_extreme_hills\",\"minecraft:extreme_hills_with_trees\",\"minecraft:mutated_extreme_hills\",\"minecraft:mutated_extreme_hills_with_trees\"],"
            + "\"spawnSize\":3,"
            + "\"spawnTries\":184,"
            + "\"spawnProbability\":185.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":-186.0},"
            + "{\"blockstate\":{\"Properties\":{\"variant\":\"stone\"},\"Name\":\"minecraft:monster_egg\"},"
            + "\"biomes\":[\"minecraft:extreme_hills\",\"minecraft:smaller_extreme_hills\",\"minecraft:extreme_hills_with_trees\",\"minecraft:mutated_extreme_hills\",\"minecraft:mutated_extreme_hills_with_trees\"],"
            + "\"spawnSize\":187,"
            + "\"spawnTries\":188,"
            + "\"spawnProbability\":189.0,"
            + "\"minHeight\":-Infinity,"
            + "\"maxHeight\":190.0},"
            + "{\"blockstate\":{\"Name\":\"minecraft:gold_ore\"},"
            + "\"biomes\":[\"minecraft:mesa\",\"minecraft:mesa_clear_rock\",\"minecraft:mesa_rock\",\"minecraft:mutated_mesa\",\"minecraft:mutated_mesa_clear_rock\",\"minecraft:mutated_mesa_rock\"],"
            + "\"spawnSize\":191,"
            + "\"spawnTries\":192,"
            + "\"spawnProbability\":193.0,"
            + "\"minHeight\":-194.0,"
            + "\"maxHeight\":196.0}],"
            + "\"periodicGaussianOres\":["
            + "{\"blockstate\":{\"Name\":\"minecraft:lapis_ore\"},"
            + "\"spawnSize\":179,"
            + "\"spawnTries\":180,"
            + "\"spawnProbability\":181.0,"
            + "\"heightMean\":182.0,"
            + "\"heightStdDeviation\":183.0,"
            + "\"heightSpacing\":0.0,"
            + "\"minHeight\":0.0,"
            + "\"maxHeight\":0.0}],"
            + "\"expectedBaseHeight\":110.0,"
            + "\"expectedHeightVariation\":109.0,"
            + "\"actualHeight\":544.0,"
            + "\"heightVariationFactor\":106.0,"
            + "\"specialHeightVariationFactorBelowAverageY\":107.0,"
            + "\"heightVariationOffset\":108.0,"
            + "\"heightFactor\":109.0,"
            + "\"heightOffset\":110.0,"
            + "\"depthNoiseFactor\":111.0,"
            + "\"depthNoiseOffset\":112.0,"
            + "\"depthNoiseFrequencyX\":113.0,"
            + "\"depthNoiseFrequencyZ\":114.0,"
            + "\"depthNoiseOctaves\":5,"
            + "\"selectorNoiseFactor\":116.0,"
            + "\"selectorNoiseOffset\":117.0,"
            + "\"selectorNoiseFrequencyX\":118.0,"
            + "\"selectorNoiseFrequencyY\":119.0,"
            + "\"selectorNoiseFrequencyZ\":120.0,"
            + "\"selectorNoiseOctaves\":1,"
            + "\"lowNoiseFactor\":122.0,"
            + "\"lowNoiseOffset\":123.0,"
            + "\"lowNoiseFrequencyX\":124.0,"
            + "\"lowNoiseFrequencyY\":125.0,"
            + "\"lowNoiseFrequencyZ\":126.0,"
            + "\"lowNoiseOctaves\":7,"
            + "\"highNoiseFactor\":128.0,"
            + "\"highNoiseOffset\":129.0,"
            + "\"highNoiseFrequencyX\":130.0,"
            + "\"highNoiseFrequencyY\":131.0,"
            + "\"highNoiseFrequencyZ\":132.0,"
            + "\"highNoiseOctaves\":3,"
            + "\"cubeAreas\":[],"
            + "\"replacerConfig\":{"
            + "\"defaults\":{"
            + "\"cubicgen:biome_fill_noise_octaves\":4.0,"
            + "\"cubicgen:ocean_block\":{\"Properties\":{\"level\":\"0\"},\"Name\":\"minecraft:water\"},"
            + "\"cubicgen:height_scale\":64.0,"
            + "\"cubicgen:biome_fill_noise_freq\":0.0078125,"
            + "\"cubicgen:water_level\":63.0,"
            + "\"cubicgen:biome_fill_depth_factor\":2.3333333333333335,"
            + "\"cubicgen:terrain_fill_block\":{\"Properties\":{\"variant\":\"stone\"},\"Name\":\"minecraft:stone\"},"
            + "\"cubicgen:mesa_depth\":16.0,"
            + "\"cubicgen:biome_fill_depth_offset\":3.0,"
            + "\"cubicgen:horizontal_gradient_depth_decrease_weight\":1.0,"
            + "\"cubicgen:height_offset\":64.0},"
            + "\"overrides\":{}},"
            + "\"version\":3}";

    @Before
    public void setUp() {
        MinecraftEnvironment.init();
        CustomCubicMod.LOGGER = LogManager.getLogger("CustomCubicModTest");;
        CubicBiome.init();
        CubicBiome.postInit();
    }

    @Test
    public void testCustomGeneratorSettingsFixer() {
        CustomGeneratorSettings settings = CustomGeneratorSettings.fromJson(cc655ExamplePreset);
        JsonObject fixed = CustomGeneratorSettingsFixer.stringToJson(settings.toJson());
        JsonObject expected = CustomGeneratorSettingsFixer.stringToJson(expectedConversionResult);
        assertEquals(expected,fixed);
    }
}
