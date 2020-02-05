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

import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.cubicgen.CWGEventFactory;
import io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.accessor.IBiomeSnow;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SnowBiomeDecorator implements ICubicPopulator {

    @Override public void generate(World world, Random random, CubePos pos, Biome biome) {
        IBiomeSnow snow = (IBiomeSnow) biome;

        ICubicWorld cworld = (ICubicWorld) world;

        if (snow.isSuperIcy() && CWGEventFactory.decorate(world, random, pos, DecorateBiomeEvent.Decorate.EventType.ICE)) {
            for (int i = 0; i < 3; ++i) {
                int xOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;
                int zOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;
                BlockPos blockPos = cworld.getSurfaceForCube(pos, xOffset, zOffset, 0, ICubicWorld.SurfaceType.SOLID);
                if (blockPos != null) {
                    snow.getIceSpike().generate(world, random, blockPos);
                }
            }

            for (int l = 0; l < 2; ++l) {
                int xOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;
                int zOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;
                BlockPos blockPos = cworld.getSurfaceForCube(pos, xOffset, zOffset, 0, ICubicWorld.SurfaceType.SOLID);
                if (blockPos != null) {
                    snow.getIcePatch().generate(world, random, blockPos);
                }
            }
        }
    }
}
