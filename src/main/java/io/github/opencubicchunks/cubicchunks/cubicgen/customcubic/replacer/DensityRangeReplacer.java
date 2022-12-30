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
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.FilterType;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * By default, used for ocean and stone.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DensityRangeReplacer extends IBiomeBlockReplacer {
    protected IBlockState blockInRange;

    protected DensityRangeReplacer(int minY, int maxY, @Nullable IBlockState blockInRange) {
        super(minY, maxY);
        this.blockInRange = blockInRange;
    }

    public static IBiomeBlockReplacer create(CustomGeneratorSettings.DensityRangeReplacerConfig config) {
        int minY = config.minY;
        int maxY = config.maxY;

        IBlockState blockInRange = BlockStateDesc.stateFromNullable(config.blockInRange);
        IBlockState blockOutOfRange = BlockStateDesc.stateFromNullable(config.blockOutOfRange);
        List<IBlockState> filterBlocks = config.filterBlocks.stream().map(BlockStateDesc::stateFromNullable)
                .filter(Objects::nonNull).collect(Collectors.toList());
        FilterType blockFilterType = config.blockFilterType;
        double minDensity = config.minDensity;
        double maxDensity = config.maxDensity;

        boolean noLower = minDensity == Double.NEGATIVE_INFINITY;
        boolean noUpper = maxDensity == Double.POSITIVE_INFINITY;
        boolean alwaysOutOfRange = minDensity > maxDensity || Double.isNaN(minDensity) || Double.isNaN(maxDensity);

        if ((blockInRange == null && blockOutOfRange == null) // always previous regardless of other conditions
                || (blockInRange == null && noUpper && noLower) // previous when in range and is always in range
                || (blockOutOfRange == null && alwaysOutOfRange) // previous when out of range and never in range
                || (filterBlocks.isEmpty() && blockFilterType.emptyAlwaysFails())) { // filter always fails
            return new NoopReplacer();
        }
        if (noUpper && noLower && filterBlocks.isEmpty() && blockFilterType.emptyAlwaysMatches()) {
            return new AlwaysReplace(minY, maxY, blockInRange);
        }
        if (alwaysOutOfRange && filterBlocks.isEmpty() && blockFilterType.emptyAlwaysMatches()) {
            return new AlwaysReplace(minY, maxY, blockOutOfRange);
        }
        if ((noUpper || noLower) && (filterBlocks.isEmpty() && blockFilterType.emptyAlwaysMatches())) {
            if (noUpper) {
                // condition is "density > lower"
                return new NoBlockThresholdFilter(minY, maxY, blockInRange, blockOutOfRange, minDensity);
            } else {
                // condition is "density < upper" -> so condition for out of range is density > upper
                // so flip in range and out of range
                return new NoBlockThresholdFilter(minY, maxY, blockOutOfRange, blockInRange, maxDensity);
            }
        }
        if ((filterBlocks.isEmpty() && blockFilterType.emptyAlwaysMatches())) {
            // no block filter
            return new NoBlockFilter(minY, maxY, blockInRange, blockOutOfRange, minDensity, maxDensity);
        }
        if (filterBlocks.size() == 1 && noUpper && noLower) {
            // single block filter, no density
            return new SingleBlockFilterNoDensity(minY, maxY, blockInRange, filterBlocks.get(0), blockFilterType);
        }
        if (filterBlocks.size() == 1 && alwaysOutOfRange) {
            // single block filter, no density
            return new SingleBlockFilterNoDensity(minY, maxY, blockOutOfRange, filterBlocks.get(0), blockFilterType);
        }
        if (filterBlocks.size() == 1) {
            // single block filter
            return new SingleBlockFilter(minY, maxY, blockInRange, blockOutOfRange, filterBlocks.get(0), blockFilterType, minDensity, maxDensity);
        }
        // general
        return new General(minY, maxY, blockInRange, blockOutOfRange, new HashSet<>(filterBlocks), blockFilterType, minDensity, maxDensity);
    }

    // specific implementations to avoid unnecessary work in simple cases
    // NOTE: these classes are public to allow them to be directly referenced in generated bytecode
    public static class AlwaysReplace extends DensityRangeReplacer {

        protected AlwaysReplace(int minY, int maxY, @Nullable IBlockState blockInRange) {
            super(minY, maxY, blockInRange);
        }

        @Override
        public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
            return blockInRange;
        }
    }

    public static class NoBlockThresholdFilter extends DensityRangeReplacer {

        private final IBlockState blockOutOfRange;
        private final double threshold;

        protected NoBlockThresholdFilter(int minY, int maxY, @Nullable IBlockState blockInRange, @Nullable IBlockState blockOutOfRange,
                double threshold) {
            super(minY, maxY, blockInRange);
            this.blockOutOfRange = blockOutOfRange;
            this.threshold = threshold;
        }

        @Override
        public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
            if (density > threshold) {
                return blockInRange == null ? previousBlock : blockInRange;
            }
            return blockOutOfRange == null ? previousBlock : blockInRange;
        }
    }
    public static class NoBlockFilter extends DensityRangeReplacer {

        private final IBlockState blockOutOfRange;
        private final double min, max;

        protected NoBlockFilter(int minY, int maxY, @Nullable IBlockState blockInRange, @Nullable IBlockState blockOutOfRange, double min,
                double max) {
            super(minY, maxY, blockInRange);
            this.blockOutOfRange = blockOutOfRange;
            this.min = min;
            this.max = max;
        }

        @Override
        public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
            if (density > min && density < max) {
                return blockInRange == null ? previousBlock : blockInRange;
            }
            return blockOutOfRange == null ? previousBlock : blockInRange;
        }
    }

    public static class SingleBlockFilterNoDensity extends DensityRangeReplacer {
        private final IBlockState filterBlock;
        private final FilterType blockFilterType;

        protected SingleBlockFilterNoDensity(int minY, int maxY, @Nullable IBlockState blockInRange, IBlockState filterBlock,
                FilterType blockFilterType) {
            super(minY, maxY, blockInRange);
            this.filterBlock = filterBlock;
            this.blockFilterType = blockFilterType;
        }

        @Override
        public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
            if (blockFilterType.isAllowed(filterBlock, previousBlock)) {
                return blockInRange == null ? previousBlock : blockInRange;
            }
            return previousBlock;
        }
    }


    public static class SingleBlockFilter extends DensityRangeReplacer {
        private final IBlockState blockOutOfRange;
        private final IBlockState filterBlock;
        private final FilterType blockFilterType;
        private final double min, max;

        protected SingleBlockFilter(int minY, int maxY, @Nullable IBlockState blockInRange, @Nullable IBlockState blockOutOfRange,
                IBlockState filterBlock, FilterType blockFilterType, double min, double max) {
            super(minY, maxY, blockInRange);
            this.blockOutOfRange = blockOutOfRange;
            this.filterBlock = filterBlock;
            this.blockFilterType = blockFilterType;
            this.min = min;
            this.max = max;
        }

        @Override
        public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
            if (blockFilterType.isAllowed(filterBlock, previousBlock)) {
                if (density > min && density < max) {
                    return blockInRange == null ? previousBlock : blockInRange;
                }
                return blockOutOfRange == null ? previousBlock : blockOutOfRange;
            }
            return previousBlock;
        }
    }

    public static class General extends DensityRangeReplacer {
        private final IBlockState blockOutOfRange;
        private final Set<IBlockState> filterBlock;
        private final FilterType blockFilterType;
        private final double min, max;

        protected General(int minY, int maxY, @Nullable IBlockState blockInRange, @Nullable IBlockState blockOutOfRange,
                Set<IBlockState> filterBlock, FilterType blockFilterType, double min, double max) {
            super(minY, maxY, blockInRange);
            this.blockOutOfRange = blockOutOfRange;
            this.filterBlock = filterBlock;
            this.blockFilterType = blockFilterType;
            this.min = min;
            this.max = max;
        }

        @Override
        public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
            if (blockFilterType.isAllowed(filterBlock, previousBlock)) {
                if (density > min && density < max) {
                    return blockInRange == null ? previousBlock : blockInRange;
                }
                return blockOutOfRange == null ? previousBlock : blockOutOfRange;
            }
            return previousBlock;
        }
    }
}
