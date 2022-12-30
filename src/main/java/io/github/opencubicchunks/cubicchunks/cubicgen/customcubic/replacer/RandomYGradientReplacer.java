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
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.UserFunction;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RandomYGradientReplacer extends IBiomeBlockReplacer {

    private final IBlockState blockToPlace;
    private final UserFunction probabilityFunction;
    private final int hashInit;

    public RandomYGradientReplacer(int minY, int maxY, IBlockState blockToPlace, UserFunction probabilityFunction, int seed) {
        super(minY, maxY);
        this.blockToPlace = blockToPlace;
        this.probabilityFunction = probabilityFunction;
        this.hashInit = seed;
    }

    public static IBiomeBlockReplacer create(long seed, CustomGeneratorSettings.RandomYGradientReplacerConfig config) {
        IBlockState blockState = BlockStateDesc.stateFromNullable(config.blockToPlace);
        if (blockState == null) {
            return new NoopReplacer();
        }
        int seedInit = RngHash.murmurHashInt((int) seed, (int) (seed >>> 32));
        seedInit = RngHash.murmurHashInt(seedInit, config.seed);
        return new RandomYGradientReplacer(config.minY, config.maxY, blockState, config.probabilityFunction, seedInit);
    }

    @Override public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
        double p = probabilityFunction.getValue(y);
        if (p >= 1) {
            return blockToPlace;
        }
        if (p <= 0) {
            return previousBlock;
        }
        int hash = RngHash.murmurHashCoords(hashInit, x, y, z);
        float rnd = RngHash.murmurHashFinalize(hash) * (1f / 0xFFFFFFFFL);
        if (rnd <= p) {
            return blockToPlace;
        }
        return previousBlock;
    }
}
