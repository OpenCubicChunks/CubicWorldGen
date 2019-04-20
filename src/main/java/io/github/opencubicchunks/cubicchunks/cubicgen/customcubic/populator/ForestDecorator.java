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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator;

import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.CWGEventFactory;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeForest;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ForestDecorator implements ICubicPopulator {

    @Override public void generate(World world, Random random, CubePos pos, Biome biome) {

        if (((BiomeForest) biome).type == BiomeForest.Type.ROOFED) {
            this.addMushrooms(world, random, pos, biome);
        }

        if (CWGEventFactory.decorate(world, random, pos, DecorateBiomeEvent.Decorate.EventType.FLOWERS)) {
            int plantAmount = random.nextInt(5) - 3;

            if (((BiomeForest) biome).type == BiomeForest.Type.FLOWER) {
                plantAmount += 2;
            }

            this.addDoublePlants(world, random, pos, biome, plantAmount);
        }
    }


    public void addMushrooms(World world, Random random, CubePos pos, Biome biome) {
        final int gridSize = 4;
        for (int xGrid = 0; xGrid < ICube.SIZE / gridSize; ++xGrid) {
            for (int zGrid = 0; zGrid < ICube.SIZE / gridSize; ++zGrid) {
                int xOffset = xGrid * gridSize + 1 + ICube.SIZE / 2 + random.nextInt(gridSize / 2 + 1);
                int zOffset = zGrid * gridSize + 1 + ICube.SIZE / 2 + random.nextInt(gridSize / 2 + 1);
                BlockPos blockpos = ((ICubicWorld) world).getSurfaceForCube(pos, xOffset, zOffset, 0, ICubicWorld.SurfaceType.OPAQUE);
                if (blockpos == null) {
                    continue;
                }
                if (random.nextInt(20) == 0 && CWGEventFactory.decorate(world, random, pos, DecorateBiomeEvent.Decorate.EventType.BIG_SHROOM)) {
                    new WorldGenBigMushroom().generate(world, random, blockpos);
                } else if (CWGEventFactory.decorate(world, random, pos, DecorateBiomeEvent.Decorate.EventType.TREE)) {
                    WorldGenAbstractTree generator = biome.getRandomTreeFeature(random);
                    generator.setDecorationDefaults();

                    if (generator.generate(world, random, blockpos)) {
                        generator.generateSaplings(world, random, blockpos);
                    }
                }
            }
        }
    }

    public void addDoublePlants(World world, Random random, CubePos pos, Biome biome, int amount) {
        for (int i = 0; i < amount; ++i) {
            int type = random.nextInt(3);

            if (type == 0) {
                biome.DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SYRINGA);
            } else if (type == 1) {
                biome.DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.ROSE);
            } else if (type == 2) {
                biome.DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.PAEONIA);
            }

            for (int j = 0; j < 5; ++j) {
                // see flower generator in DefaultDecorator
                if (random.nextInt(7) != 0) {
                    continue;
                }
                int xOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;
                int zOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;

                BlockPos blockPos = ((ICubicWorld) world).getSurfaceForCube(pos, xOffset, zOffset, 0, ICubicWorld.SurfaceType.OPAQUE);

                if (blockPos != null && biome.DOUBLE_PLANT_GENERATOR.generate(world, random, blockPos)) {
                    break;
                }
            }
        }
    }

}
