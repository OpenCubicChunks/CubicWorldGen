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

import io.github.opencubicchunks.cubicchunks.cubicgen.RngHash;
import io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.accessor.IBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.NoiseBasedSurfaceDecorationConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NoiseBasedSurfaceDecoration extends IBiomeBlockReplacer {

    private final IBlockState groundBlock;
    private final IBlockState featureBlock;
    private final double densityThreshold;
    private final IBuilder builder;
    private final double groundMin;
    private final double groundMax;
    private final double featureMin;
    private final double featureMax;

    public NoiseBasedSurfaceDecoration(@Nullable IBlockState groundBlock, @Nullable IBlockState featureBlock,
            double densityThreshold, int minY, int maxY,
            IBuilder builder, double groundMin, double groundMax, double featureMin, double featureMax) {
        super(minY, maxY);
        this.groundBlock = groundBlock;
        this.featureBlock = featureBlock;
        this.densityThreshold = densityThreshold;
        this.builder = builder;
        this.groundMin = groundMin;
        this.groundMax = groundMax;
        this.featureMin = featureMin;
        this.featureMax = featureMax;
    }

    @Override protected boolean rangeChecksAlwaysFail() {
        // if both groundBlock and featureBlock are null, nothing will ever be replaced
        return super.rangeChecksAlwaysFail() || (this.groundBlock == null && this.featureBlock == null);
    }

    @Override public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome,
            int x, int y, int z, double dx, double dy, double dz, double density) {
        // NOTE: duplicating noise check code because all the earlier checks are much faster

        // block right under the surface, can't be at maxY or there will be nowhere to fit the block above
        if (groundBlock != null && y < maxY && density > densityThreshold && density + dy <= densityThreshold) {
            double v = builder.get(x, y, z);
            if (v >= groundMin && v <= groundMax) {
                return groundBlock;
            }
            return previousBlock;
        }
        // block right above the surface, can't be minY or there will be nowhere to fit the block below
        if (featureBlock != null && y > minY && density <= densityThreshold && density - dy > densityThreshold) {
            double v = builder.get(x, y, z);
            if (v >= featureMin && v < featureMax) {
                return featureBlock;
            }
            return previousBlock;
        }
        return previousBlock;
    }

    public static NoiseBasedSurfaceDecoration create(long worldSeed, CustomGeneratorSettings.NoiseBasedSurfaceDecorationConfig config) {
        double densityThreshold = config.surfaceDensityThreshold;
        IBlockState groundBlock = BlockStateDesc.stateFromNullable(config.groundBlock);
        IBlockState featureBlock = BlockStateDesc.stateFromNullable(config.featureBlock);
        int minY = config.minY;
        int maxY = config.maxY;
        NoiseBasedSurfaceDecorationConfig.NoiseSource noiseSource = config.noiseSource;
        IBuilder builder;
        switch (noiseSource) {
            case FLOW_NOISE_PERLIN: {
                builder = NoiseSource.perlin()
                        .frequency(config.noiseFreqX, config.noiseFreqY, config.noiseFreqZ)
                        .seed(RngHash.combineSeedsForNoise(worldSeed, config.customNoiseSeed))
                        .octaves(config.customNoiseOctaves)
                        .create()
                        .mul(config.noiseFactor).add(config.noiseOffset);
                if (config.noiseFreqY == 0) {
                    builder = builder.cached2d(256, p -> p.getX() * 16 + p.getZ());
                }
                break;
            }
            case SPONGE_NOISE_SIMPLEX: {
                builder = NoiseSource.simplex()
                        .frequency(config.noiseFreqX, config.noiseFreqY, config.noiseFreqZ)
                        .seed(RngHash.combineSeedsForNoise(worldSeed, config.customNoiseSeed))
                        .octaves(config.customNoiseOctaves)
                        .create()
                        .mul(config.noiseFactor).add(config.noiseOffset);
                if (config.noiseFreqY == 0) {
                    builder = builder.cached2d(256, p -> p.getX() * 16 + p.getZ());
                }
                break;
            }
            case GRASS_COLOR_NOISE: {
                NoiseGeneratorPerlin grassColorNoise = IBiome.getGrassColorNoise();
                builder = (x, y, z) ->
                        grassColorNoise.getValue(x * config.noiseFreqX, z * config.noiseFreqZ) * config.noiseFactor + config.noiseOffset;
                builder = builder.cached2d(256, p -> p.getX() * 16 + p.getZ());
                break;
            }
            case TEMPERATURE_NOISE: {
                NoiseGeneratorPerlin temperatureNoise = IBiome.getTemperatureNoise();
                builder = (x, y, z) ->
                        temperatureNoise.getValue(x * config.noiseFreqX, z * config.noiseFreqZ) * config.noiseFactor + config.noiseOffset;
                builder = builder.cached2d(256, p -> p.getX() * 16 + p.getZ());
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown noise source " + noiseSource);
        }

        return new NoiseBasedSurfaceDecoration(groundBlock, featureBlock, densityThreshold, minY, maxY, builder,
                config.groundMinNoise, config.groundMaxNoise, config.featureMinNoise, config.featureMaxNoise);
    }

}
