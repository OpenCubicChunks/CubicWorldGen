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
package io.github.opencubicchunks.cubicchunks.cubicgen.hybrid;

import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.VanillaCompatibilityGeneratorProviderBase;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomTerrainGenerator;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HybridTerrainGenerator implements ICubeGenerator {

    private static final Set<String> SUPPORTED_DIMENSION_TYPES = new HashSet<>(Arrays.asList(
            DimensionType.OVERWORLD.getName(),
            DimensionType.NETHER.getName(),
            DimensionType.THE_END.getName()
    ));
    @Nonnull private final ICubeGenerator defaultCompatGen;
    @Nullable private final ICubeGenerator cubicGenerator;
    private final int worldHeightCubes;

    /**
     * Create a new HybridTerrainGenerator
     *
     * @param vanilla The vanilla generator to mirror
     * @param world   The world in which cubes are being generated
     */
    public HybridTerrainGenerator(IChunkGenerator vanilla, World world) {
        VanillaCompatibilityGeneratorProviderBase defaultProvider =
                VanillaCompatibilityGeneratorProviderBase.REGISTRY.getValue(VanillaCompatibilityGeneratorProviderBase.DEFAULT);
        assert defaultProvider != null;
        this.defaultCompatGen = defaultProvider.provideGenerator(vanilla, world);
        if (SUPPORTED_DIMENSION_TYPES.contains(world.provider.getDimensionType().getName())) {
            this.cubicGenerator = CustomTerrainGenerator.createForHybrid(world, world.getSeed());
        } else {
            this.cubicGenerator = null;
        }
        int worldHeightBlocks = ((ICubicWorld) world).getMaxGenerationHeight();
        worldHeightCubes = worldHeightBlocks / ICube.SIZE;
    }

    @Override
    public void generateColumn(Chunk column) {
        defaultCompatGen.generateColumn(column);
    }

    @Override
    public void recreateStructures(Chunk column) {
        defaultCompatGen.recreateStructures(column);
    }

    @SuppressWarnings("deprecation") @Override public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        return generateCube(cubeX, cubeY, cubeZ, new CubePrimer());
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ, CubePrimer primer) {
        if (cubicGenerator != null && (cubeY < 0 || cubeY >= worldHeightCubes)) {
            return cubicGenerator.generateCube(cubeX, cubeY, cubeZ, primer);
        } else {
            return defaultCompatGen.generateCube(cubeX, cubeY, cubeZ, primer);
        }
    }

    @Override
    public void populate(ICube cube) {
        if (cubicGenerator != null && (cube.getY() < 0 || cube.getY() >= worldHeightCubes)) {
            cubicGenerator.populate(cube);
        } else {
            defaultCompatGen.populate(cube);
        }
    }

    @Override
    public Box getFullPopulationRequirements(ICube cube) {
        Box box = defaultCompatGen.getFullPopulationRequirements(cube);
        if (box == NO_REQUIREMENT && cubicGenerator != null) {
            box = cubicGenerator.getFullPopulationRequirements(cube);
        }
        return box;
    }

    @Override
    public Box getPopulationPregenerationRequirements(ICube cube) {
        Box box = defaultCompatGen.getPopulationPregenerationRequirements(cube);
        if (box == NO_REQUIREMENT && cubicGenerator != null) {
            box = cubicGenerator.getPopulationPregenerationRequirements(cube);
        }
        return box;
    }

    @Override
    public void recreateStructures(ICube cube) {
        if (cubicGenerator != null) {
            cubicGenerator.recreateStructures(cube);
        }
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (cubicGenerator == null || (pos.getY() >= 0 && Coords.blockToCube(pos.getY()) < worldHeightCubes)) {
            return defaultCompatGen.getPossibleCreatures(creatureType, pos);
        } else {
            return cubicGenerator.getPossibleCreatures(creatureType, pos);
        }
    }

    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        BlockPos pos1 = defaultCompatGen.getClosestStructure(name, pos, findUnexplored);
        BlockPos pos2 = cubicGenerator == null ? null : cubicGenerator.getClosestStructure(name, pos, findUnexplored);
        if (pos2 == null) {
            return pos1;
        }
        if (pos1 == null) {
            return pos2;
        }
        return pos1.distanceSq(pos) < pos2.distanceSq(pos) ? pos1 : pos2;
    }
}
