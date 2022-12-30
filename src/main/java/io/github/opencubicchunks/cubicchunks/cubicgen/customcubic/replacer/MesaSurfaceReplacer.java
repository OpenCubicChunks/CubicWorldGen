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
import io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.accessor.IBiomeMesa;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.MesaSurfaceReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Arrays;
import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MesaSurfaceReplacer extends IBiomeBlockReplacer {

    private static final IBlockState STAINED_HARDENED_CLAY = Blocks.STAINED_HARDENED_CLAY.getDefaultState();
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState STONE = Blocks.STONE.getDefaultState();
    private static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    private static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    private static final IBlockState HARDENED_CLAY = Blocks.HARDENED_CLAY.getDefaultState();
    private static final IBlockState ORANGE_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);

    private final double mesaDepth;
    private final double heightOffset;
    private final double heightScale;
    private final double waterHeight;
    private final IBlockState[] clayBands;
    private final IBuilder clayBandsOffsetNoise;
    private final IBuilder pillarNoise;
    private final IBuilder pillarRoofNoise;
    private final IBuilder depthNoise;

    public MesaSurfaceReplacer(int minY, int maxY, double depth, double heightOffset, double heightScale, double waterHeight,
            IBlockState[] clayBands, IBuilder clayBandsOffsetNoise, IBuilder pillarNoise, IBuilder pillarRoofNoise, IBuilder depthNoise) {
        super(minY, maxY);
        this.mesaDepth = depth;
        this.heightOffset = heightOffset;
        this.heightScale = heightScale;
        this.waterHeight = waterHeight;
        this.clayBands = clayBands;
        this.clayBandsOffsetNoise = clayBandsOffsetNoise;
        this.pillarNoise = pillarNoise;
        this.pillarRoofNoise = pillarRoofNoise;
        this.depthNoise = depthNoise;
    }

    public static IBiomeBlockReplacer create(long worldSeed, MesaSurfaceReplacerConfig config) {

        double mesaDepth = config.mesaDepth;
        double heightOffset = config.heightOffset;
        double heightScale = config.heightScale;
        double waterHeight = config.waterHeight;

        BiomeMesa mesaVanilla = (BiomeMesa) Biomes.MESA;
        IBiomeMesa mesa = (IBiomeMesa) mesaVanilla;
        // always generate so that clay bands noise exists
        if (mesa.getClayBands() == null || mesa.getWorldSeed() != worldSeed) {
            mesaVanilla.generateBands(worldSeed);
        }
        assert mesa.getClayBands() != null;
        // so that we don't cause issues when we replace clayBands and scrollOffset noise
        mesa.setWorldSeed(worldSeed);

        IBlockState[] clayBands;
        if (config.clayBandsOverride != null) {
            clayBands = config.clayBandsOverride.stream().map(BlockStateDesc::stateFromNullable).toArray(IBlockState[]::new);
        } else {
            clayBands = Arrays.copyOf(mesa.getClayBands(), mesa.getClayBands().length);
        }

        //MesaSurfaceReplacerConfig.NoiseSource clayBandsOffsetNoiseSource = ;
        //MesaSurfaceReplacerConfig.NoiseSource pillarNoiseSource = ;
        //MesaSurfaceReplacerConfig.NoiseSource pillarRoofNoiseSource = ;
        //MesaSurfaceReplacerConfig.NoiseSource depthNoise = ;
        NoiseGeneratorPerlin clayBandsNoiseVanilla = ((IBiomeMesa) mesaVanilla).getClayBandsOffsetNoise();
        IBuilder clayBandsNoise = (x, y, z) -> clayBandsNoiseVanilla.getValue(x / 512.0, z / 512.0);
        clayBandsNoise = clayBandsNoise.cached2d(256, p -> p.getX() * 16 + p.getZ());

        Random random = new Random(worldSeed);
        NoiseGeneratorPerlin pillasPerlin = new NoiseGeneratorPerlin(random, 4);
        IBuilder pillarNoise = (x, y, z) -> pillasPerlin.getValue(x * 0.25D, z * 0.25D);
        NoiseGeneratorPerlin pillarRoofPerlin = new NoiseGeneratorPerlin(random, 1);
        IBuilder pillarRoofNoise = (x, y, z) -> pillarRoofPerlin.getValue(x * 0.001953125D, z * 0.001953125D);

        IBuilder depthNoise = MainSurfaceReplacer.createNoiseBuilder(worldSeed, config.surfaceDepthNoiseSeed, config.surfaceDepthNoiseType,
                config.surfaceDepthNoiseFrequencyX, config.surfaceDepthNoiseFrequencyY, config.surfaceDepthNoiseFrequencyZ,
                config.surfaceDepthNoiseOctaves, config.surfaceDepthNoiseFactor, config.surfaceDepthNoiseOffset);

        return new MesaSurfaceReplacer(config.minY, config.maxY, mesaDepth, heightOffset, heightScale, waterHeight,
                clayBands, clayBandsNoise, pillarNoise, pillarRoofNoise, depthNoise);
    }

    @Override public IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
        if (density < 0) {
            return previousBlock;
        }

        double depth = depthNoise.get(x, 0, z);
        double origDepthNoise = depth - 3;
        double pillarHeight = getPillarHeightVanilla(biome, x, z, origDepthNoise);
        pillarHeight = convertYFromVanilla(pillarHeight);
        if (y < pillarHeight) {
            // simulate pillar density ORed with te terrain
            density = Math.max(density, pillarHeight - y);
        }

        boolean coarse = Math.cos(origDepthNoise * Math.PI) > 0.0D;

        IBlockState top = STAINED_HARDENED_CLAY;
        IBlockState filler = biome.fillerBlock;

        if (depth < 0) {
            top = AIR;
            filler = STONE;
        }

        if (y >= waterHeight - 1) {
            boolean hasForest = biome instanceof IBiomeMesa && ((IBiomeMesa) biome).getHasForest();
            if (hasForest && y >= convertYFromVanilla(86) + depth * 2) {
                top = coarse ? COARSE_DIRT : GRASS;
                filler = getBand(x, y, z);
            } else if (y > waterHeight + 3 + depth) {
                filler = getBand(x, y, z);
                top = coarse ? HARDENED_CLAY : filler;
            } else {
                top = filler = ORANGE_STAINED_HARDENED_CLAY;
            }
        }

        if (density + dy <= 0) { // if air above
            return top;
        }
        double densityAdjusted = density / abs(dy);
        if (densityAdjusted < this.mesaDepth) {
            return filler;
        }
        return previousBlock;
    }

    private double convertYFromVanilla(double y) {
        y = (y - 64.0) / 64.0;
        y *= heightScale;
        y += heightOffset;
        return y;
    }

    private double getPillarHeightVanilla(Biome biome, int x, int z, double depth) {
        double pillarHeight = 0.0;
        if (biome instanceof IBiomeMesa && ((IBiomeMesa) biome).isBrycePillars()) {
            double pillarScale = Math.min(abs(depth),
                    this.pillarNoise.get(x, 0, z));

            if (pillarScale > 0.0D) {
                double pillarRoofVal = abs(this.pillarRoofNoise.get(x, 0, z));
                pillarHeight = pillarScale * pillarScale * 2.5D;
                double cutoffHeight = Math.ceil(pillarRoofVal * 50.0D) + 14.0D;

                if (pillarHeight > cutoffHeight) {
                    pillarHeight = cutoffHeight;
                }

                pillarHeight = pillarHeight + 64.0D;
            }
        }
        return pillarHeight;
    }

    private IBlockState getBand(int blockX, int blockY, int blockZ) {
        int offset = (int) Math.round(this.clayBandsOffsetNoise.get(blockX, 0, blockX) * 2.0D);
        return clayBands[Math.floorMod(blockY + offset + 64, clayBands.length)];
    }
}
