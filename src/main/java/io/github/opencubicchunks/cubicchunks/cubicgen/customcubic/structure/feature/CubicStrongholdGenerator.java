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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature;

import com.google.common.collect.Lists;
import io.github.opencubicchunks.cubicchunks.api.util.Bits;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.feature.CubicFeatureGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.feature.ICubicFeatureStart;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.*;
import static java.lang.Math.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CubicStrongholdGenerator extends CubicFeatureGenerator {

    private CubePos[] structureCoords;
    private double distance;
    private int spread;
    private List<Biome> allowedBiomes;
    private boolean positionsGenerated;
    private final CustomGeneratorSettings conf;

    public CubicStrongholdGenerator(CustomGeneratorSettings conf) {
        this(conf, true);
    }
    public CubicStrongholdGenerator(CustomGeneratorSettings conf, boolean syncToStrongholdsDat) {
        super(2, 0, syncToStrongholdsDat);
        this.conf = conf;
        this.structureCoords = new CubePos[128];
        this.distance = 32.0D;
        this.spread = 3;
        this.allowedBiomes = Lists.newArrayList();

        for (Biome biome : ForgeRegistries.BIOMES) {
            if (biome != null && biome.getBaseHeight() > 0.0F && !BiomeManager.strongHoldBiomesBlackList.contains(biome)) {
                this.allowedBiomes.add(biome);
            }
        }

        for (Biome biome : BiomeManager.strongHoldBiomes) {
            if (!this.allowedBiomes.contains(biome)) {
                this.allowedBiomes.add(biome);
            }
        }
    }

    @Override public String getStructureName() {
        return "Stronghold";
    }

    @Nullable @Override public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
        checkPositionsGenerated(world);

        BlockPos.MutableBlockPos currentBlock = new BlockPos.MutableBlockPos(0, 0, 0);

        BlockPos closestPos = null;
        double minDist = Double.MAX_VALUE;

        for (CubePos cubePos : this.structureCoords) {
            currentBlock.setPos(cubePos.getXCenter(), cubePos.getYCenter(), cubePos.getZCenter());
            double currentDist = currentBlock.distanceSq(pos);

            if (closestPos == null || currentDist < minDist) {
                closestPos = new BlockPos(currentBlock);
                minDist = currentDist;
            }
        }

        assert closestPos != null;

        return closestPos;
    }

    @Override protected boolean canSpawnStructureAtCoords(World world, Random rand, int chunkX, int chunkY, int chunkZ) {
        checkPositionsGenerated(world);

        return Arrays.stream(this.structureCoords)
                .anyMatch(cubePos -> chunkX == cubePos.getX() && chunkY == cubePos.getY() && chunkZ == cubePos.getZ());
    }

    @Override protected StructureStart getStructureStart(World world, Random rand, int chunkX, int chunkY, int chunkZ) {
        StructureStart start;
        do {
            start = new MapGenStronghold.Start(world, rand, chunkX, chunkZ);
            @SuppressWarnings("ConstantConditions") CubicStart cubic = (CubicStart) start;
            cubic.initCubicStronghold(world, chunkY, MathHelper.floor(conf.expectedBaseHeight - 10));
        } while (start.getComponents().isEmpty() || ((StructureStrongholdPieces.Stairs2) start.getComponents().get(0)).strongholdPortalRoom == null);
        return start;
    }

    private void checkPositionsGenerated(World world) {
        if (!this.positionsGenerated) {
            this.generatePositions(world);
            this.positionsGenerated = true;
        }
    }

    private void generatePositions(World world) {
        this.initializeStructureData(world);
        {
            int i = 0;
            for (ICubicFeatureStart start : this.structureMap) {
                if (i >= this.structureCoords.length) {
                    break;
                }
                this.structureCoords[i++] = start.getCubePos();
            }
        }

        int nextIndex = this.structureMap.getSize();
        if (nextIndex >= this.structureCoords.length) {
            return;
        }

        Random rand = new Random();
        rand.setSeed(world.getSeed());

        double angle = rand.nextDouble() * Math.PI * 2.0D;

        int minCubeY = blockToCube(conf.expectedBaseHeight - conf.expectedHeightVariation);
        int maxCubeY = blockCeilToCube(MathHelper.ceil(conf.expectedBaseHeight));

        int distFactor = 0;
        int ringStep = 0;
        for (int i = 0; i < this.structureCoords.length; ++i) {
            double distance = 4.0D * this.distance + this.distance * (double) distFactor * 6.0D + (rand.nextDouble() - 0.5D) * this.distance * 2.5D;
            int chunkX;
            int chunkY;
            int chunkZ;
            if (conf.alternateStrongholdsPositions) {
                double yAngle = -rand.nextDouble() * Math.PI;
                chunkX = (int) round(cos(angle) * cos(yAngle) * distance);
                chunkY = (int) round(sin(yAngle) * distance) + maxCubeY; // TODO: use configuration-based offset
                chunkZ = (int) round(sin(angle) * cos(yAngle) * distance);
            } else {
                chunkX = (int) round(cos(angle) * distance);
                chunkY = MathHelper.getInt(rand, minCubeY, maxCubeY);
                chunkZ = (int) round(sin(angle) * distance);
            }
            BlockPos blockPos = world.getBiomeProvider().findBiomePosition(
                    cubeToCenterBlock(chunkX), cubeToCenterBlock(chunkZ), 112, this.allowedBiomes, rand);

            if (blockPos != null) {
                chunkX = blockToCube(blockPos.getX());
                chunkZ = blockToCube(blockPos.getZ());
            }

            int spacingBits = spacingBitCount == 0 ? 0xFFFFFFFF : ~Bits.getMask(spacingBitCount);
            int spacingBitsY = spacingBitCountY == 0 ? 0xFFFFFFFF : ~Bits.getMask(spacingBitCountY);

            chunkX = chunkX & spacingBits;
            chunkY = chunkY & spacingBitsY;
            chunkZ = chunkZ & spacingBits;

            if (i >= nextIndex) {
                this.structureCoords[i] = new CubePos(chunkX, chunkY, chunkZ);
            }

            angle += (Math.PI * 2D) / (double) this.spread;
            ++ringStep;

            if (ringStep == this.spread) {
                ++distFactor;
                ringStep = 0;
                this.spread += 2 * this.spread / (distFactor + 1);
                this.spread = Math.min(this.spread, this.structureCoords.length - i);
                angle += rand.nextDouble() * Math.PI * 2.0D;
            }
        }
    }

    public interface CubicStart extends ICubicFeatureStart {
        void initCubicStronghold(World world, int cubeY, int baseY);
    }
}
