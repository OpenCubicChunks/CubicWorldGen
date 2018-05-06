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

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.blockToCube;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PopulatorUtils {

    public static void genOreUniform(World world, CustomGeneratorSettings cfg, Random random, CubePos pos,
            int count, double probability, WorldGenerator generator, double minY, double maxY) {
        int minBlockY = Math.round((float) (minY * cfg.heightFactor + cfg.heightOffset));
        int maxBlockY = Math.round((float) (maxY * cfg.heightFactor + cfg.heightOffset));
        if (pos.getMinBlockY() > maxBlockY || pos.getMaxBlockY() < minBlockY) {
            return;
        }
        for (int i = 0; i < count; ++i) {
            if (random.nextDouble() > probability) {
                continue;
            }
            int yOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;
            int blockY = pos.getMinBlockY() + yOffset;
            if (blockY > maxBlockY || blockY < minBlockY) {
                continue;
            }
            int xOffset = random.nextInt(ICube.SIZE);
            int zOffset = random.nextInt(ICube.SIZE);
            generator.generate((World) world, random, new BlockPos(pos.getMinBlockX() + xOffset, blockY, pos.getMinBlockZ() + zOffset));
        }
    }

    public static void genOreBellCurve(World world, CustomGeneratorSettings cfg, Random random, CubePos pos, int count,
                                      double probability, WorldGenerator generator, double mean, double stdDevFactor, double spacing, double minY, double maxY) {

        int factor = (cfg.getMaxHeight() - cfg.getMinHeight()) / 2;
        int minBlockY = Math.round((float) (minY * factor + cfg.heightOffset));
        int maxBlockY = Math.round((float) (maxY * factor+ cfg.heightOffset));
        //temporary fix for slider becoming 0 at minimum position
        if(spacing == 0.0){
            spacing = 0.5;
        }
        int iSpacing = Math.round((float) (spacing * factor));
        int iMean = Math.round((float) (mean * factor + cfg.heightOffset));
		double scaledStdDev = stdDevFactor * factor;
        for (int i = 0; i < count; ++i) {
            int yOffset = random.nextInt(ICube.SIZE) + ICube.SIZE / 2;
            int blockY = pos.getMinBlockY() + yOffset;
            //skip all potential spawns outside the spawn range
            if((blockY > maxBlockY) || (blockY < minBlockY)){
                continue;
            }
            double modifier = MathUtil.bellCurveProbabilityCyclic(blockY, iMean, scaledStdDev, iSpacing);
            //Modify base probability with the curve
            if (random.nextDouble() > (probability * modifier)) {
                continue;
            }
            int xOffset = random.nextInt(ICube.SIZE);
            int zOffset = random.nextInt(ICube.SIZE);
            generator.generate((World) world, random, new BlockPos(pos.getMinBlockX() + xOffset, blockY, pos.getMinBlockZ() + zOffset));
        }
    }
}
