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

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class IBiomeBlockReplacer {

    protected final int minY;
    protected final int maxY;

    public IBiomeBlockReplacer(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    // TODO: some way for other mods to hook into the system to allow adding new replacer types
    public static IBiomeBlockReplacer create(long seed, CustomGeneratorSettings.ReplacerConfig config) {
        if (config.getClass() == CustomGeneratorSettings.DensityRangeReplacerConfig.class) {
            return DensityRangeReplacer.create((CustomGeneratorSettings.DensityRangeReplacerConfig) config);
        }
        if (config.getClass() == CustomGeneratorSettings.RandomYGradientReplacerConfig.class) {
            return RandomYGradientReplacer.create(seed, (CustomGeneratorSettings.RandomYGradientReplacerConfig) config);
        }
        if (config.getClass() == CustomGeneratorSettings.MainSurfaceReplacerConfig.class) {
            return MainSurfaceReplacer.create(seed, (CustomGeneratorSettings.MainSurfaceReplacerConfig) config);
        }
        if (config.getClass() == CustomGeneratorSettings.NoiseBasedSurfaceDecorationConfig.class) {
            return NoiseBasedSurfaceDecoration.create(seed, (CustomGeneratorSettings.NoiseBasedSurfaceDecorationConfig) config);
        }
        if (config.getClass() == CustomGeneratorSettings.MesaSurfaceReplacerConfig.class) {
            return MesaSurfaceReplacer.create(seed, (CustomGeneratorSettings.MesaSurfaceReplacerConfig) config);
        }
        if (config.getClass() == CustomGeneratorSettings.DepthBasedSurfaceReplacerConfig.class) {
            return DepthBasedSurfaceReplacer.create(seed, (CustomGeneratorSettings.DepthBasedSurfaceReplacerConfig) config);
        }
        throw new UnsupportedOperationException("Unknown replacer type: " + config.getClass());
    }

    protected boolean rangeChecksAlwaysFail() {
        return minY > maxY;
    }

    /**
     * Replaces the given block with another block based on given location, density gradient and density value. Biome
     * block replacers can be chained (output if one replacer used as input to another replacer)
     * <p>
     * The common interpretation of density value: If it's greater than 0, there is block at that position. Density is
     * scaled in such way that it approximately represents how many blocks below the surface this position is.
     * <p>
     * Gradient values approximate how the value will change after going 1 block in x/y/z direction.
     *
     * @param previousBlock the block that was there before using this replacer
     * @param biome         the biome at current location
     * @param x             the block X coordinate
     * @param y             the block Y coordinate
     * @param z             the block Z coordinate
     * @param dx            the X component of density gradient
     * @param dy            the Y component of density gradient
     * @param dz            the Z component of density gradient
     * @param density       the density value
     */
    public final IBlockState getReplacedBlock(IBlockState previousBlock, Biome biome,
            int x, int y, int z, double dx, double dy, double dz, double density) {
        if (y >= minY && y <= maxY) {
            return getReplacedBlockImpl(previousBlock, biome, x, y, z, dx, dy, dz, density);
        }
        return previousBlock;
    }

    protected abstract IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome,
            int x, int y, int z, double dx, double dy, double dz, double density);
}
