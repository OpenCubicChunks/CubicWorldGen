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
package io.github.opencubicchunks.cubicchunks.cubicgen.flat;

import io.github.opencubicchunks.cubicchunks.api.worldgen.CubeGeneratorsRegistry;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.BasicCubeGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.FlatGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.FlatLayer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map.Entry;
import java.util.NavigableMap;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A cube generator that generates a flat surface of grass, dirt and stone.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlatTerrainProcessor extends BasicCubeGenerator {

    private final FlatGeneratorSettings conf;

    public FlatTerrainProcessor(World world) {
        super(world);
        String json = world.getWorldInfo().getGeneratorOptions();
        conf = FlatGeneratorSettings.fromJson(json);
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        CubePrimer primer = new CubePrimer();
        int floorY = Coords.cubeToMinBlock(cubeY);
        int topY = Coords.cubeToMaxBlock(cubeY);
        int floorKeyI = floorY;
        int topKeyI = topY;
        Integer floorKey = conf.layers.floorKey(floorY);
        if (floorKey != null)
            floorKeyI = floorKey;
        Integer ceilingKey = conf.layers.ceilingKey(topY);
        if (ceilingKey != null)
            topKeyI = ceilingKey;
        NavigableMap<Integer, FlatLayer> cubeLayerSubMap = conf.layers.subMap(floorKeyI, true, topKeyI, true);
        for (Entry<Integer, FlatLayer> entry : cubeLayerSubMap.entrySet()) {
            FlatLayer layer = entry.getValue();
            int fromY = layer.fromY - floorY;
            int toY = layer.toY - floorY;
            IBlockState iBlockState = layer.blockState.getOrDefault(Blocks.STONE.getDefaultState());
            int maxY = Math.min(toY, ICube.SIZE);
            for (int y = Math.max(fromY, 0); y < maxY; y++)
                for (int x = 0; x < ICube.SIZE; x++) {
                    for (int z = 0; z < ICube.SIZE; z++) {
                        primer.setBlockState(x, y, z, iBlockState);
                    }
                }
        }
        return primer;
    }

    @Override
    public void populate(ICube cube) {
        /**
         * If event is not canceled we will use cube populators from registry.
         **/
        if (!MinecraftForge.EVENT_BUS.post(new CubePopulatorEvent(world, cube))) {
            CubeGeneratorsRegistry.generateWorld(cube.getWorld(),
                    Coords.coordsSeedRandom(cube.getWorld().getSeed(), cube.getX(), cube.getY(), cube.getZ()),
                    cube.getCoords(), world.getBiome(cube.getCoords().getCenterBlockPos()));
        }
    }

    @Override
    public Box getFullPopulationRequirements(ICube cube) {
        return NO_REQUIREMENT;
    }

    @Override
    public Box getPopulationPregenerationRequirements(ICube cube) {
        return NO_REQUIREMENT;
    }

    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        // eyes of ender are the new F3 for finding the origin :P
        return name.equals("Stronghold") ? new BlockPos(0, 0, 0) : null;
    }
}
