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

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.feature.CubicFeatureGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.feature.ICubicFeatureStart;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.cubeToCenterBlock;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CubicVillageGenerator extends CubicFeatureGenerator {

    public static List<Biome> VILLAGE_SPAWN_BIOMES = Arrays.asList(Biomes.PLAINS, Biomes.DESERT, Biomes.SAVANNA, Biomes.TAIGA);
    private int size;
    private int distance;
    private final CustomGeneratorSettings conf;
    private final int minTownSeparation;

    public CubicVillageGenerator(CustomGeneratorSettings conf) {
        super(2, 0);
        this.conf = conf;
        this.distance = 32;
        this.minTownSeparation = 8;

    }

    public CubicVillageGenerator(CustomGeneratorSettings conf, Map<String, String> map) {
        this(conf);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equals("size")) {
                this.size = MathHelper.getInt(entry.getValue(), this.size, 0);
            } else if (entry.getKey().equals("distance")) {
                this.distance = MathHelper.getInt(entry.getValue(), this.distance, 9);
            }
        }
    }

    @Override
    public String getStructureName() {
        return "Village";
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos startPos, boolean findUnexplored) {
        int distanceStep = this.distance;

        int i = startPos.getX() >> 4;
        int j = startPos.getZ() >> 4;
        int k = 0;

        for (Random random = new Random(); k <= 100; ++k) {
            for (int l = -k; l <= k; ++l) {
                boolean flag = l == -k || l == k;

                for (int i1 = -k; i1 <= k; ++i1) {
                    boolean flag1 = i1 == -k || i1 == k;

                    if (flag || flag1) {
                        int j1 = i + distanceStep * l;
                        int k1 = j + distanceStep * i1;

                        if (j1 < 0) {
                            j1 -= distanceStep - 1;
                        }

                        if (k1 < 0) {
                            k1 -= distanceStep - 1;
                        }

                        int l1 = j1 / distanceStep;
                        int i2 = k1 / distanceStep;
                        Random random1 = world.setRandomSeed(l1, i2, 10387312);
                        l1 = l1 * distanceStep;
                        i2 = i2 * distanceStep;

                        l1 = l1 + random1.nextInt(distanceStep - 8);
                        i2 = i2 + random1.nextInt(distanceStep - 8);

                        MapGenBase.setupChunkSeed(world.getSeed(), random, l1, i2);
                        random.nextInt();

                        if (this.canSpawnStructureAtCoords(world, random, l1, 64, i2)) {
                            return new BlockPos(cubeToCenterBlock(l1), 64, cubeToCenterBlock(i2));
                        } else if (k == 0) {
                            break;
                        }
                    }
                }

                if (k == 0) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(World world, Random rand, int chunkX, int chunkY, int chunkZ) {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0) {
            chunkX -= this.distance - 1;
        }

        if (chunkZ < 0) {
            chunkZ -= this.distance - 1;
        }

        int k = chunkX + rand.nextInt(this.distance - 8);
        int l = chunkZ + rand.nextInt(this.distance - 8);

        if (i == k && j == l) {
            return world.getBiomeProvider().areBiomesViable(cubeToCenterBlock(i), cubeToCenterBlock(j), 0, VILLAGE_SPAWN_BIOMES);
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(World world, Random rand, int chunkX, int chunkY, int chunkZ) {
        StructureStart start = new MapGenVillage.Start(world, rand, chunkX, chunkZ, this.size);
        CubicStart cubic = (CubicStart) start;
        cubic.initCubicVillage(world, chunkY, this.size);
        return start;
    }

    public interface CubicStart extends ICubicFeatureStart {
        void initCubicVillage(World world, int cubeY, int size);
    }
}
