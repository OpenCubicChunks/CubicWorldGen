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
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.cubicgen.testutil.MinecraftEnvironment;
import net.minecraft.world.World;
import org.junit.Before;
import org.junit.Test;

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.FlatGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.FlatTerrainProcessor;
import io.github.opencubicchunks.cubicchunks.cubicgen.flat.Layer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.storage.WorldInfo;
import org.mockito.Mockito;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TestCustomGeneratorSettingsFixer {
    String cc655ExamplePreset = "{\n" + 
            "   \"waterLevel\":63,\n" + 
            "   \"caves\":true,\n" + 
            "   \"strongholds\":true,\n" + 
            "   \"villages\":true,\n" + 
            "   \"mineshafts\":true,\n" + 
            "   \"temples\":true,\n" + 
            "   \"oceanMonuments\":true,\n" + 
            "   \"woodlandMansions\":true,\n" + 
            "   \"ravines\":true,\n" + 
            "   \"dungeons\":true,\n" + 
            "   \"dungeonCount\":7,\n" + 
            "   \"waterLakes\":true,\n" + 
            "   \"waterLakeRarity\":4,\n" + 
            "   \"lavaLakes\":true,\n" + 
            "   \"lavaLakeRarity\":8,\n" + 
            "   \"aboveSeaLavaLakeRarity\":13,\n" + 
            "   \"lavaOceans\":false,\n" + 
            "   \"biome\":-1,\n" + 
            "   \"biomeSize\":4,\n" + 
            "   \"riverSize\":4,\n" + 
            "   \"dirtSpawnSize\":33,\n" + 
            "   \"dirtSpawnTries\":10,\n" + 
            "   \"dirtSpawnProbability\":0.0625,\n" + 
            "   \"dirtSpawnMinHeight\":-Infinity,\n" + 
            "   \"dirtSpawnMaxHeight\":Infinity,\n" + 
            "   \"gravelSpawnSize\":33,\n" + 
            "   \"gravelSpawnTries\":8,\n" + 
            "   \"gravelSpawnProbability\":0.0625,\n" + 
            "   \"gravelSpawnMinHeight\":-Infinity,\n" + 
            "   \"gravelSpawnMaxHeight\":Infinity,\n" + 
            "   \"graniteSpawnSize\":33,\n" + 
            "   \"graniteSpawnTries\":10,\n" + 
            "   \"graniteSpawnProbability\":0.2,\n" + 
            "   \"graniteSpawnMinHeight\":-Infinity,\n" + 
            "   \"graniteSpawnMaxHeight\":0.25,\n" + 
            "   \"dioriteSpawnSize\":33,\n" + 
            "   \"dioriteSpawnTries\":10,\n" + 
            "   \"dioriteSpawnProbability\":0.2,\n" + 
            "   \"dioriteSpawnMinHeight\":-Infinity,\n" + 
            "   \"dioriteSpawnMaxHeight\":0.25,\n" + 
            "   \"andesiteSpawnSize\":33,\n" + 
            "   \"andesiteSpawnTries\":10,\n" + 
            "   \"andesiteSpawnProbability\":0.2,\n" + 
            "   \"andesiteSpawnMinHeight\":-Infinity,\n" + 
            "   \"andesiteSpawnMaxHeight\":0.25,\n" + 
            "   \"coalOreSpawnSize\":17,\n" + 
            "   \"coalOreSpawnTries\":20,\n" + 
            "   \"coalOreSpawnProbability\":0.125,\n" + 
            "   \"coalOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"coalOreSpawnMaxHeight\":1.0,\n" + 
            "   \"ironOreSpawnSize\":9,\n" + 
            "   \"ironOreSpawnTries\":20,\n" + 
            "   \"ironOreSpawnProbability\":0.25,\n" + 
            "   \"ironOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"ironOreSpawnMaxHeight\":0.0,\n" + 
            "   \"goldOreSpawnSize\":9,\n" + 
            "   \"goldOreSpawnTries\":2,\n" + 
            "   \"goldOreSpawnProbability\":0.5,\n" + 
            "   \"goldOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"goldOreSpawnMaxHeight\":-0.5,\n" + 
            "   \"redstoneOreSpawnSize\":8,\n" + 
            "   \"redstoneOreSpawnTries\":8,\n" + 
            "   \"redstoneOreSpawnProbability\":1.0,\n" + 
            "   \"redstoneOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"redstoneOreSpawnMaxHeight\":-0.75,\n" + 
            "   \"diamondOreSpawnSize\":8,\n" + 
            "   \"diamondOreSpawnTries\":1,\n" + 
            "   \"diamondOreSpawnProbability\":1.0,\n" + 
            "   \"diamondOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"diamondOreSpawnMaxHeight\":-0.75,\n" + 
            "   \"lapisLazuliSpawnSize\":7,\n" + 
            "   \"lapisLazuliSpawnTries\":1,\n" + 
            "   \"lapisLazuliSpawnProbability\":0.5,\n" + 
            "   \"lapisLazuliHeightMean\":0.25,\n" + 
            "   \"lapisLazuliHeightStdDeviation\":0.125,\n" + 
            "   \"hillsEmeraldOreSpawnTries\":11,\n" + 
            "   \"hillsEmeraldOreSpawnProbability\":0.2857143,\n" + 
            "   \"hillsEmeraldOreSpawnMinHeight\":-Infinity,\n" + 
            "   \"hillsEmeraldOreSpawnMaxHeight\":-0.5,\n" + 
            "   \"hillsSilverfishStoneSpawnSize\":7,\n" + 
            "   \"hillsSilverfishStoneSpawnTries\":7,\n" + 
            "   \"hillsSilverfishStoneSpawnProbability\":0.25,\n" + 
            "   \"hillsSilverfishStoneSpawnMinHeight\":-Infinity,\n" + 
            "   \"hillsSilverfishStoneSpawnMaxHeight\":0.0,\n" + 
            "   \"mesaAddedGoldOreSpawnSize\":20,\n" + 
            "   \"mesaAddedGoldOreSpawnTries\":2,\n" + 
            "   \"mesaAddedGoldOreSpawnProbability\":0.5,\n" + 
            "   \"mesaAddedGoldOreSpawnMinHeight\":-0.5,\n" + 
            "   \"mesaAddedGoldOreSpawnMaxHeight\":0.25,\n" + 
            "   \"heightVariationFactor\":64.0,\n" + 
            "   \"specialHeightVariationFactorBelowAverageY\":0.25,\n" + 
            "   \"heightVariationOffset\":0.0,\n" + 
            "   \"heightFactor\":64.0,\n" + 
            "   \"heightOffset\":64.0,\n" + 
            "   \"depthNoiseFactor\":1.024,\n" + 
            "   \"depthNoiseOffset\":0.0,\n" + 
            "   \"depthNoiseFrequencyX\":0.0015258789,\n" + 
            "   \"depthNoiseFrequencyZ\":0.0015258789,\n" + 
            "   \"depthNoiseOctaves\":16,\n" + 
            "   \"selectorNoiseFactor\":12.75,\n" + 
            "   \"selectorNoiseOffset\":0.5,\n" + 
            "   \"selectorNoiseFrequencyX\":0.016709277,\n" + 
            "   \"selectorNoiseFrequencyY\":0.008354639,\n" + 
            "   \"selectorNoiseFrequencyZ\":0.016709277,\n" + 
            "   \"selectorNoiseOctaves\":8,\n" + 
            "   \"lowNoiseFactor\":1.0,\n" + 
            "   \"lowNoiseOffset\":0.0,\n" + 
            "   \"lowNoiseFrequencyX\":0.005221649,\n" + 
            "   \"lowNoiseFrequencyY\":0.0026108245,\n" + 
            "   \"lowNoiseFrequencyZ\":0.005221649,\n" + 
            "   \"lowNoiseOctaves\":16,\n" + 
            "   \"highNoiseFactor\":1.0,\n" + 
            "   \"highNoiseOffset\":0.0,\n" + 
            "   \"highNoiseFrequencyX\":0.005221649,\n" + 
            "   \"highNoiseFrequencyY\":0.0026108245,\n" + 
            "   \"highNoiseFrequencyZ\":0.005221649,\n" + 
            "   \"highNoiseOctaves\":16\n" + 
            "}";

    @Before
    public void setUp() {
        MinecraftEnvironment.init();
    }

    @Test
    public void testCustomGeneratorSettingsFixer() {
        CustomGeneratorSettings settings  = CustomGeneratorSettings.fromJson(cc655ExamplePreset);
        assertEquals(settings.standardOres.size(),10);
    }
}
