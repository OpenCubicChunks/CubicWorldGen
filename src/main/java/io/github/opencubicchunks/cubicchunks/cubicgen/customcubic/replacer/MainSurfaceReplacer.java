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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.replacer;

import static java.lang.Math.abs;

import io.github.opencubicchunks.cubicchunks.cubicgen.RngHash;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MainSurfaceReplacer extends IBiomeBlockReplacer {
    protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
    protected static final IBlockState RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();
    protected static final IBlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();

    private final IBuilder depthNoise;
    private final double maxPossibleDepth;
    private final IBlockState overrideTop;
    private final IBlockState overrideFiller;
    private final double horizontalGradientDepthDecreaseWeight;
    private final double oceanHeight;

    protected MainSurfaceReplacer(int minY, int maxY, IBuilder depthNoise,
            double horizontalGradientDepthDecreaseWeight, double oceanHeight, double surfaceDepthLimit,
            @Nullable IBlockState overrideTop, @Nullable IBlockState overrideFiller) {
        super(minY, maxY);
        this.depthNoise = depthNoise;
        this.horizontalGradientDepthDecreaseWeight = horizontalGradientDepthDecreaseWeight;
        this.oceanHeight = oceanHeight;
        this.maxPossibleDepth = surfaceDepthLimit;
        this.overrideTop = overrideTop;
        this.overrideFiller = overrideFiller;
    }

    public static IBiomeBlockReplacer create(long worldSeed, CustomGeneratorSettings.MainSurfaceReplacerConfig config) {
        IBuilder builder = createNoiseBuilder(worldSeed, config.surfaceDepthNoiseSeed, config.surfaceDepthNoiseType,
                config.surfaceDepthNoiseFrequencyX, config.surfaceDepthNoiseFrequencyY, config.surfaceDepthNoiseFrequencyZ,
                config.surfaceDepthNoiseOctaves, config.surfaceDepthNoiseFactor, config.surfaceDepthNoiseOffset);

        return new MainSurfaceReplacer(config.minY, config.maxY, builder, config.horizontalGradientDepthDecreaseWeight,
                config.oceanLevel, config.maxSurfaceDepth,
                BlockStateDesc.stateFromNullable(config.overrideTop),
                BlockStateDesc.stateFromNullable(config.overrideFiller));
    }

    @Nonnull public static IBuilder createNoiseBuilder(long worldSeed, int surfaceDepthNoiseSeed,
            CustomGeneratorSettings.NoiseType surfaceDepthNoiseType,
            double surfaceDepthNoiseFrequencyX, double surfaceDepthNoiseFrequencyY, double surfaceDepthNoiseFrequencyZ,
            int surfaceDepthNoiseOctaves, double surfaceDepthNoiseFactor, double surfaceDepthNoiseOffset) {
        IBuilder builder;
        if (surfaceDepthNoiseType == CustomGeneratorSettings.NoiseType.PERLIN_FLOW_NOISE) {
            builder = NoiseSource.perlin()
                    .seed(RngHash.combineSeedsForNoise(worldSeed, surfaceDepthNoiseSeed))
                    .frequency(surfaceDepthNoiseFrequencyX, surfaceDepthNoiseFrequencyY, surfaceDepthNoiseFrequencyZ)
                    .octaves(surfaceDepthNoiseOctaves).create();
        } else if (surfaceDepthNoiseType == CustomGeneratorSettings.NoiseType.SIMPLEX_SPONGE_NOISE) {
            builder = NoiseSource.simplex()
                    .seed(RngHash.combineSeedsForNoise(worldSeed, surfaceDepthNoiseSeed))
                    .frequency(surfaceDepthNoiseFrequencyX, surfaceDepthNoiseFrequencyY, surfaceDepthNoiseFrequencyZ)
                    .octaves(surfaceDepthNoiseOctaves).create();;
        } else {
            throw new IllegalArgumentException("Unknown noise type " + surfaceDepthNoiseType);
        }
        builder = builder.mul(surfaceDepthNoiseFactor).add(surfaceDepthNoiseOffset);
        if (surfaceDepthNoiseFrequencyY == 0) {
            builder = builder.cached2d(256, v -> v.getX() + v.getZ() * 16);
        }
        return builder;
    }

    /**
     * Replaces a few top non-air blocks with biome surface and filler blocks
     */
    @Override
    public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
        // skip everything below if there is no chance it will actually do something
        if (previousBlock.getBlock() == Blocks.AIR) {
            return previousBlock;
        }
        if (density > maxPossibleDepth * abs(dy) || density < 0) {
            return previousBlock;
        }

        double depth = depthNoise.get(x, 0, z);
        double densityAdjusted = density / abs(dy);
        IBlockState top = getRawTop(biome, depth);
        IBlockState filler = getRawFiller(biome, depth);

        if (density + dy <= 0) { // if air above
            if (y < oceanHeight - 7 - depth) { // if we are deep into the ocean
                return GRAVEL;
            }
            if (y < oceanHeight - 1) { // if just below the ocean level
                return fillerForDepth(filler, previousBlock, depth);
            }
            return topForDepth(top, depth);
        } else {
            double xzSize = Math.sqrt(dx * dx + dz * dz);
            double dyAdjusted = dy;
            if (dyAdjusted < 0 && densityAdjusted < depth + 1 - horizontalGradientDepthDecreaseWeight * xzSize / dy) {
                return biome.fillerBlock;
            }

            if (biome.fillerBlock.getBlock() == Blocks.SAND && depth > 1 && y > oceanHeight - depth) {
                return biome.fillerBlock.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
            }
        }
        return previousBlock;
    }

    public IBlockState getRawTop(Biome biome, double depth) {
        return overrideTop == null ? biome.topBlock : overrideTop;
    }

    public IBlockState getRawFiller(Biome biome, double depth) {
        return overrideFiller == null ? biome.fillerBlock : overrideFiller;
    }

    private IBlockState fillerForDepth(IBlockState filler, IBlockState prev, double depth) {
        return depth > 0 ? filler : prev;
    }

    private IBlockState topForDepth(IBlockState top, double depth) {
        return depth > 0 ? top : Blocks.AIR.getDefaultState();
    }
}
