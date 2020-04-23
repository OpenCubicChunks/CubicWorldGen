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
package io.github.opencubicchunks.cubicchunks.cubicgen.common.biome;

import static java.lang.Math.abs;

import com.google.common.collect.Sets;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
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
    private final int maxPossibleDepth;
    private final int bedrockY;
    private IBlockState topBlock;
    private IBlockState fillerBlock;
    private final double horizontalGradientDepthDecreaseWeight;
    private final double oceanHeight;

    public SurfaceDefaultReplacer(IBlockState topBlock, IBlockState fillerBlock, IBuilder depthNoise,
            double horizontalGradientDepthDecreaseWeight, double oceanHeight, int surfaceDepthLimit, int bedrockY) {
        this.topBlock = topBlock;
        this.fillerBlock = fillerBlock;
        this.depthNoise = depthNoise;
        this.horizontalGradientDepthDecreaseWeight = horizontalGradientDepthDecreaseWeight;
        this.oceanHeight = oceanHeight;
        this.maxPossibleDepth = 9;
        this.bedrockY = bedrockY;
    }

    /**
     * Replaces a few top non-air blocks with biome surface and filler blocks
     */
    @Override
    public IBlockState getReplacedBlock(IBlockState previousBlock, int x, int y, int z, double dx, double dy, double dz, double density) {
        // skip everything below if there is no chance it will actually do something
        if (previousBlock.getBlock() == Blocks.AIR) {
            return previousBlock;
        }
        if (y <= bedrockY) {
            if (y < bedrockY) {
                return Blocks.AIR.getDefaultState();
            }
            return Blocks.BEDROCK.getDefaultState();
        }
        if (density > maxPossibleDepth * abs(dy) || density < 0) {
            return previousBlock;
        }

        double depth = depthNoise.get(x, 0, z);
        double densityAdjusted = density / abs(dy);
        if (density + dy <= 0) { // if air above
            if (y < oceanHeight - 7 - depth) { // if we are deep into the ocean
                return GRAVEL;
            }
            if (y < oceanHeight - 1) { // if just below the ocean level
                return filler(previousBlock, depth);
            }
            return top(depth);
        } else {
            double xzSize = Math.sqrt(dx * dx + dz * dz);
            double dyAdjusted = dy;
            if (dyAdjusted < 0 && densityAdjusted < depth + 1 - horizontalGradientDepthDecreaseWeight * xzSize / dy) {
                return fillerBlock;
            }

            if (fillerBlock.getBlock() == Blocks.SAND && depth > 1 && y > oceanHeight - depth) {
                return fillerBlock.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
            }
        }
        return previousBlock;
    }

    public void setTopBlock(IBlockState topBlock) {
        this.topBlock = topBlock;
    }

    public void setFillerBlock(IBlockState fillerBlock) {
        this.fillerBlock = fillerBlock;
    }

    public IBuilder getDepthNoise() {
        return depthNoise;
    }

    private IBlockState filler(IBlockState prev, double depth) {
        return depth > 0 ? fillerBlock : prev;
    }

    private IBlockState top(double depth) {
        return depth > 0 ? topBlock : Blocks.AIR.getDefaultState();
    }

    public static IBiomeBlockReplacerProvider provider() {
        return new IBiomeBlockReplacerProvider() {
            private final ResourceLocation HORIZONTAL_GRADIENT_DEC = CustomCubicMod.location("horizontal_gradient_depth_decrease_weight");
            private final ResourceLocation OCEAN_LEVEL = CustomCubicMod.location("water_level");
            private final ResourceLocation SURFACE_BLOCK_DEPTH_LIMIT = CustomCubicMod.location("surface_depth_limit");
            private final ResourceLocation DEPTH_NOISE_FACTOR = CustomCubicMod.location("biome_fill_depth_factor");
            private final ResourceLocation DEPTH_NOISE_OFFSET = CustomCubicMod.location("biome_fill_depth_offset");
            private final ResourceLocation DEPTH_NOISE_FREQUENCY = CustomCubicMod.location("biome_fill_noise_freq");
            private final ResourceLocation DEPTH_NOISE_OCTAVES = CustomCubicMod.location("biome_fill_noise_octaves");

            @Override
            public IBiomeBlockReplacer create(World world, CubicBiome cubicBiome, BiomeBlockReplacerConfig conf) {
                double gradientDec = conf.getDouble(HORIZONTAL_GRADIENT_DEC);
                double oceanY = conf.getDouble(OCEAN_LEVEL);

                double factor = conf.getDouble(DEPTH_NOISE_FACTOR);
                double offset = conf.getDouble(DEPTH_NOISE_OFFSET);
                double freq = conf.getDouble(DEPTH_NOISE_FREQUENCY);
                int octaves = (int) conf.getDouble(DEPTH_NOISE_OCTAVES);
                int maxDepth = (int) conf.getDouble(SURFACE_BLOCK_DEPTH_LIMIT);
                int bedrockY = ((ICubicWorld) world).getMinHeight();
                Biome biome = cubicBiome.getBiome();

                IBuilder builder = NoiseSource.perlin()
                        .frequency(freq).octaves(octaves).create()
                        .mul(factor).add(offset)
                        .cached2d(256, v -> v.getX() + v.getZ() * 16);
                return new SurfaceDefaultReplacer(biome.topBlock, biome.fillerBlock, builder, gradientDec, oceanY, maxDepth, bedrockY);
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
                        new ConfigOptionInfo(SURFACE_BLOCK_DEPTH_LIMIT, 9.0)
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
