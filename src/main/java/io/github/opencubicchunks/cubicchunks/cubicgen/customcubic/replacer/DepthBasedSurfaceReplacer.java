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
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.DepthBasedSurfaceReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

// by default used for taiga and mutated savannabiome
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DepthBasedSurfaceReplacer extends MainSurfaceReplacer {

    private final double[] topThresholds;
    private final IBlockState[] topBlockstates;

    private final double[] fillerThresholds;
    private final IBlockState[] fillerBlockstates;

    protected DepthBasedSurfaceReplacer(int minY, int maxY, IBuilder depthNoise, double horizontalGradientDepthDecreaseWeight,
            double oceanHeight, double surfaceDepthLimit, @Nullable IBlockState overrideTop, @Nullable IBlockState overrideFiller,
            Set<DepthBasedSurfaceReplacerConfig.Entry> topThresholds, Set<DepthBasedSurfaceReplacerConfig.Entry> fillerThresholds) {
        super(minY, maxY, depthNoise, horizontalGradientDepthDecreaseWeight, oceanHeight, surfaceDepthLimit, overrideTop, overrideFiller);

        DepthBasedSurfaceReplacerConfig.Entry[] topEntries = topThresholds.toArray(new DepthBasedSurfaceReplacerConfig.Entry[0]);
        Arrays.sort(topEntries, Comparator.comparingDouble(e -> e.y));
        this.topThresholds = new double[topEntries.length];
        this.topBlockstates = new IBlockState[this.topThresholds.length];

        for (int i = 0; i < topEntries.length; i++) {
            this.topThresholds[i] = topEntries[i].y;
            this.topBlockstates[i] = topEntries[i].b.getBlockState();
        }

        DepthBasedSurfaceReplacerConfig.Entry[] fillerEntries = fillerThresholds.toArray(new DepthBasedSurfaceReplacerConfig.Entry[0]);
        Arrays.sort(fillerEntries, Comparator.comparingDouble(e -> e.y));
        this.fillerThresholds = new double[fillerEntries.length];
        this.fillerBlockstates = new IBlockState[this.fillerThresholds.length];

        for (int i = 0; i < fillerEntries.length; i++) {
            this.fillerThresholds[i] = fillerEntries[i].y;
            this.fillerBlockstates[i] = fillerEntries[i].b.getBlockState();
        }
    }

    public static IBiomeBlockReplacer create(long worldSeed, DepthBasedSurfaceReplacerConfig config) {

        IBuilder builder = MainSurfaceReplacer.createNoiseBuilder(worldSeed, config.surfaceDepthNoiseSeed, config.surfaceDepthNoiseType,
                config.surfaceDepthNoiseFrequencyX, config.surfaceDepthNoiseFrequencyY, config.surfaceDepthNoiseFrequencyZ,
                config.surfaceDepthNoiseOctaves, config.surfaceDepthNoiseFactor, config.surfaceDepthNoiseOffset);

        return new DepthBasedSurfaceReplacer(config.minY, config.maxY, builder, config.horizontalGradientDepthDecreaseWeight,
                config.oceanLevel, config.maxSurfaceDepth,
                BlockStateDesc.stateFromNullable(config.overrideTop),
                BlockStateDesc.stateFromNullable(config.overrideFiller),
                config.topThresholds, config.fillerThresholds);
    }

    @Override public IBlockState getRawTop(Biome biome, double depth) {
        for (int i = topThresholds.length - 1; i >= 0; i--) {
            if (depth > topThresholds[i]) {
                IBlockState state = topBlockstates[i];
                return state == null ? super.getRawTop(biome, depth) : state;
            }
        }
        return super.getRawTop(biome, depth);
    }

    @Override public IBlockState getRawFiller(Biome biome, double depth) {
        for (int i = fillerThresholds.length - 1; i >= 0; i--) {
            if (depth > fillerThresholds[i]) {
                IBlockState state = fillerBlockstates[i];
                return state == null ? super.getRawFiller(biome, depth) : state;
            }
        }
        return super.getRawFiller(biome, depth);
    }
}
