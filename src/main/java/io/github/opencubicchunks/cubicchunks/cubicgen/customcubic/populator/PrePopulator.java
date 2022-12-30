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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.DecorateCubeBiomeEvent;
import io.github.opencubicchunks.cubicchunks.cubicgen.CWGEventFactory;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BiomeDesc;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.cubeToMinBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrePopulator implements ICubicPopulator {

    private final CustomGeneratorSettings cfg;

    public PrePopulator(CustomGeneratorSettings cfg) {
        this.cfg = cfg;
    }

    @Override public void generate(World world, Random random, CubePos pos, Biome biome) {

        for (CustomGeneratorSettings.LakeConfig lake : cfg.lakes) {
            if (lake.block.getBlock() == null) {
                continue;
            }
            if (!lake.biomeSelect.isAllowed(lake.biomes, new BiomeDesc(biome))) {
                continue;
            }
            BlockPos populationPos = pos.randomPopulationPos(random);
            BlockPos surface = ((ICubicWorld) world).getSurfaceForCube(pos,
                    populationPos.getX() - cubeToMinBlock(pos.getX()),
                    populationPos.getZ() - cubeToMinBlock(pos.getZ()),
                    0, (p, s) -> !s.getBlock().isAir(s, world, p));
            if (surface != null) {
                double prob = lake.surfaceProbability.getValue(surface.getY());
                if (random.nextFloat() < prob && (lake.generateWhen == null || lake.generateWhen.canGenerate(random, world, surface))) {
                    new WorldGenLakes(lake.block.getBlock()).generate(world, random, surface);
                }
            } else  {
                double prob = lake.mainProbability.getValue(populationPos.getY());
                if (random.nextFloat() < prob && (lake.generateWhen == null || lake.generateWhen.canGenerate(random, world, populationPos))) {
                    new WorldGenLakes(lake.block.getBlock()).generate(world, random, populationPos);
                }
            }
        }

        if (cfg.dungeons && CWGEventFactory.populate(world, random, pos, false, PopulateChunkEvent.Populate.EventType.DUNGEON)) {
            for (int i = 0; i < cfg.dungeonCount; ++i) {
                (new WorldGenDungeons()).generate(world, random, pos.randomPopulationPos(random));
            }
        }
        MinecraftForge.EVENT_BUS.post(new DecorateCubeBiomeEvent.Pre(world, random, pos));
    }
}
