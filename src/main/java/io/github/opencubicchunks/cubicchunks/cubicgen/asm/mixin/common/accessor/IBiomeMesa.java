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
package io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin.common.accessor;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeMesa.class)
public interface IBiomeMesa {
    @Accessor("COARSE_DIRT") static IBlockState getCoarseDirt() {
        throw new Error("IBiomeMesa failed to apply");
    }
    @Accessor("GRASS") static IBlockState getGrass() {
        throw new Error("IBiomeMesa failed to apply");
    }
    @Accessor("HARDENED_CLAY") static IBlockState getHardenedClay() {
        throw new Error("IBiomeMesa failed to apply");
    }
    @Accessor("STAINED_HARDENED_CLAY") static IBlockState getStainedHardenedClay() {
        throw new Error("IBiomeMesa failed to apply");
    }
    @Accessor("ORANGE_STAINED_HARDENED_CLAY") static IBlockState getOrangeStainedHardenedClay() {
        throw new Error("IBiomeMesa failed to apply");
    }
    @Accessor IBlockState[] getClayBands();
    @Accessor long getWorldSeed();
    @Accessor void setWorldSeed(long seed);
    @Accessor NoiseGeneratorPerlin getPillarNoise();
    @Accessor NoiseGeneratorPerlin getPillarRoofNoise();
    @Accessor NoiseGeneratorPerlin getClayBandsOffsetNoise();
    @Accessor boolean isBrycePillars();
    @Accessor boolean getHasForest();
}
