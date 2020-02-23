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

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.function.BiPredicate;

import static io.github.opencubicchunks.cubicchunks.api.util.MathUtil.lerp;
import static net.minecraft.util.math.MathHelper.cos;
import static net.minecraft.util.math.MathHelper.sin;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomVeinGenerator extends WorldGenerator {

    private final IBlockState oreBlock;
    private final int stepCount;
    private final CustomGeneratorSettings.GenerationCondition blockPlaceCondition;

    public CustomVeinGenerator(IBlockState state, int blockCount) {
        this(state, blockCount, (r, w, p) -> {
            IBlockState s = w.getBlockState(p);
            return s.getBlock() == Blocks.STONE && s.getValue(BlockStone.VARIANT).isNatural();
        });
    }

    public CustomVeinGenerator(IBlockState state, int stepCount, CustomGeneratorSettings.GenerationCondition blockPlaceCondition) {
        this.oreBlock = state;
        this.stepCount = stepCount;
        this.blockPlaceCondition = blockPlaceCondition;
    }

    public boolean generate(World world, Random rand, BlockPos pos) {
        float angle = rand.nextFloat() * (float) Math.PI;
        double x1 = pos.getX() + sin(angle) * this.stepCount / 8.0F;
        double x2 = pos.getX() - sin(angle) * this.stepCount / 8.0F;
        double z1 = pos.getZ() + cos(angle) * this.stepCount / 8.0F;
        double z2 = pos.getZ() - cos(angle) * this.stepCount / 8.0F;
        double y1 = pos.getY() + rand.nextInt(3) - 2;
        double y2 = pos.getY() + rand.nextInt(3) - 2;

        for (int i = 0; i < this.stepCount; ++i) {
            float progress = i / (float) this.stepCount;
            double stepX = lerp(progress, x1, x2);
            double stepY = lerp(progress, y1, y2);
            double stepZ = lerp(progress, z1, z2);
            double sizeFactor = rand.nextDouble() * this.stepCount / 16.0D;

            double xzDiameter = (sin((float) Math.PI * progress) + 1.0F) * sizeFactor + 1.0D;
            double yDiameter = (sin((float) Math.PI * progress) + 1.0F) * sizeFactor + 1.0D;

            generateEllipsoid(rand, world, this.oreBlock, this.blockPlaceCondition,
                    stepX, stepY, stepZ, xzDiameter, yDiameter);
        }
        return true;
    }

    private static void generateEllipsoid(Random rand, World world, IBlockState blockState,
                                          CustomGeneratorSettings.GenerationCondition placeCondition,
                                          double centerX, double centerY, double centerZ,
                                          double xzDiameter, double yDiameter) {

        int minX = MathHelper.floor(centerX - xzDiameter / 2.0D);
        int minY = MathHelper.floor(centerY - yDiameter / 2.0D);
        int minZ = MathHelper.floor(centerZ - xzDiameter / 2.0D);
        int maxX = MathHelper.floor(centerX + xzDiameter / 2.0D);
        int maxY = MathHelper.floor(centerY + yDiameter / 2.0D);
        int maxZ = MathHelper.floor(centerZ + xzDiameter / 2.0D);

        for (int x = minX; x <= maxX; ++x) {
            double dxNorm = (x + 0.5D - centerX) / (xzDiameter / 2.0D);
            if (dxNorm * dxNorm > 1.0D) continue;

            for (int y = minY; y <= maxY; ++y) {
                double dyNorm = (y + 0.5D - centerY) / (yDiameter / 2.0D);
                if (dxNorm * dxNorm + dyNorm * dyNorm > 1.0D) continue;

                for (int z = minZ; z <= maxZ; ++z) {
                    double dzNorm = (z + 0.5D - centerZ) / (xzDiameter / 2.0D);
                    if (dxNorm * dxNorm + dyNorm * dyNorm + dzNorm * dzNorm > 1.0D) continue;

                    BlockPos position = new BlockPos(x, y, z);
                    if (placeCondition.canGenerate(rand, world, position)) {
                        world.setBlockState(position, blockState, 2);
                    }
                }
            }
        }
    }
}
