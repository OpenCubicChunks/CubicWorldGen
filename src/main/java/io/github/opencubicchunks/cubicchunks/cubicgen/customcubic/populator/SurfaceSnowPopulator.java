/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
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
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SurfaceSnowPopulator implements ICubicPopulator {

    @Override public void generate(World world, Random random, CubePos pos, Biome biome) {
        for (int dx = 0; dx < ICube.SIZE; ++dx) {
            for (int dz = 0; dz < ICube.SIZE; ++dz) {
                int xOffset = dx + ICube.SIZE / 2;
                int zOffset = dz + ICube.SIZE / 2;
                BlockPos aboveTop = ((ICubicWorld) world).getSurfaceForCube(pos, xOffset, zOffset, 0, ICubicWorld.SurfaceType
                        .BLOCKING_MOVEMENT);
                if (aboveTop == null) {
                    continue;
                }
                BlockPos topBlock = aboveTop.down();

                if (world.canBlockFreezeWater(topBlock)) {
                    world.setBlockState(topBlock, Blocks.ICE.getDefaultState(), 2);
                }

                if (world.canSnowAt(aboveTop, true)) {
                    world.setBlockState(aboveTop, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
    }
}
