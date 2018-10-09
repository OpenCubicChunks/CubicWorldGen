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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.biome;

import static java.lang.Math.abs;

import com.google.common.collect.Sets;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.ConversionUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SurfaceDefaultReplacer implements IBiomeBlockReplacer {
    protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
    protected static final IBlockState RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();
    protected static final IBlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();

    private final IBuilder depthNoise;
    private final double horizontalGradientDepthDecreaseWeight;
    private final double oceanHeight;
    private final double maxDepth;

    public SurfaceDefaultReplacer(IBuilder depthNoise,
            double horizontalGradientDepthDecreaseWeight, double oceanHeight, double maxDepth) {
        this.depthNoise = depthNoise;
        this.horizontalGradientDepthDecreaseWeight = horizontalGradientDepthDecreaseWeight;
        this.oceanHeight = oceanHeight;
        this.maxDepth = maxDepth;
    }

    /**
     * Replaces a few top non-air blocks with biome surface and filler blocks
     */
    @Override
    public IBlockState getReplacedBlock(Biome biome, IBlockState previousBlock,
            int x, int y, int z, double dx, double dy, double dz, double density) {
        // skip everything below if there is no chance it will actually do something
        if (density < 0 || density > maxDepth * abs(dy)) {
            return previousBlock;
        }
        if (previousBlock.getBlock() == Blocks.AIR) {
            return previousBlock;
        }

        double depth = depthNoise.get(x, 0, z);

        if (density + dy <= 0) { // if air above
            double oceanHeight = this.oceanHeight;
            if (y < oceanHeight - 7 - depth) { // if we are deep into the ocean
                return GRAVEL;
            }
            if (y < oceanHeight - 1) { // if just below the ocean level
                return filler(biome, previousBlock, depth);
            }
            return top(biome, depth);
        } else {
            double dyInv = 1.0F / (float) dy;
            double densityAdjusted = density * abs(dyInv);
            double xzSize = dx * dx + dz * dz;
            if (dy < 0 && densityAdjusted < depth + 1 - horizontalGradientDepthDecreaseWeight * xzSize * dyInv) {
                return biome.fillerBlock;
            }

            if (depth > 1 && y > oceanHeight - depth && biome.fillerBlock.getBlock() == Blocks.SAND) {
                return biome.fillerBlock.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
            }
        }
        return previousBlock;
    }

    public IBuilder getDepthNoise() {
        return depthNoise;
    }

    private IBlockState filler(Biome biome, IBlockState prev, double depth) {
        return depth > 0 ? biome.fillerBlock : prev;
    }

    private IBlockState top(Biome biome, double depth) {
        return depth > 0 ? biome.topBlock : Blocks.AIR.getDefaultState();
    }

    public static IBiomeBlockReplacerProvider provider() {
        return new IBiomeBlockReplacerProvider() {
            private final ResourceLocation HORIZONTAL_GRADIENT_DEC = CustomCubicMod.location("horizontal_gradient_depth_decrease_weight");
            private final ResourceLocation OCEAN_LEVEL = CustomCubicMod.location("water_level");
            private final ResourceLocation DEPTH_NOISE_FACTOR = CustomCubicMod.location("biome_fill_depth_factor");
            private final ResourceLocation DEPTH_NOISE_OFFSET = CustomCubicMod.location("biome_fill_depth_offset");
            private final ResourceLocation DEPTH_NOISE_FREQUENCY = CustomCubicMod.location("biome_fill_noise_freq");
            private final ResourceLocation DEPTH_NOISE_OCTAVES = CustomCubicMod.location("biome_fill_noise_octaves");
            private final ResourceLocation DEPTH_CUTOFF = CustomCubicMod.location("filler_depth_cutoff");

            @Override
            public IBiomeBlockReplacer create(World world, BiomeBlockReplacerConfig conf) {
                double gradientDec = conf.getDouble(HORIZONTAL_GRADIENT_DEC);
                double oceanY = conf.getDouble(OCEAN_LEVEL);

                double factor = conf.getDouble(DEPTH_NOISE_FACTOR);
                double offset = conf.getDouble(DEPTH_NOISE_OFFSET);
                double freq = conf.getDouble(DEPTH_NOISE_FREQUENCY);
                int octaves = (int) conf.getDouble(DEPTH_NOISE_OCTAVES);
                double cutoff = (int) conf.getDouble(DEPTH_CUTOFF);

                IBuilder builder = NoiseSource.perlin()
                        .frequency(freq).octaves(octaves).create()
                        .mul(factor).add(offset)
                        .cached2d(256, v -> v.getX() + v.getZ() * 16);
                return new SurfaceDefaultReplacer(builder, gradientDec, oceanY, cutoff);
            }

            @Override public Set<ConfigOptionInfo> getPossibleConfigOptions() {
                return Sets.newHashSet(
                        new ConfigOptionInfo(HORIZONTAL_GRADIENT_DEC, 1.0),
                        new ConfigOptionInfo(OCEAN_LEVEL, 63.0),
                        // TODO: do it properly, currently this value is just temporary until I figure out the right one
                        // TODO: figure out what the above comment actually means
                        new ConfigOptionInfo(DEPTH_NOISE_FACTOR, ((1 << 3) - 1) / 3.0),
                        new ConfigOptionInfo(DEPTH_NOISE_OFFSET, 3.0),
                        new ConfigOptionInfo(DEPTH_NOISE_FREQUENCY, ConversionUtils.frequencyFromVanilla(0.0625f, 4)),
                        new ConfigOptionInfo(DEPTH_NOISE_OCTAVES, 4.0),
                        new ConfigOptionInfo(DEPTH_CUTOFF, 9.0)
                );
            }
        };
    }

    public static IBuilder makeDepthNoise() {
        return NoiseSource.perlin()
                .frequency(ConversionUtils.frequencyFromVanilla(0.0625f, 4)).octaves(4).create()
                .mul((1 << 3) - 1) // TODO: do it properly, currently this value is just temporary until I figure out the right one
                .mul(1.0 / 3.0).add(3)
                .cached2d(256, v -> v.getX() + v.getZ() * 16);
    }
}
