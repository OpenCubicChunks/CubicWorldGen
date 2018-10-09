/*
 *  This file is part of Cubic World Generation, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.blockToCube;
import static io.github.opencubicchunks.cubicchunks.api.util.Coords.cubeToMinBlock;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.core.util.AddressTools;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import io.github.opencubicchunks.cubicchunks.cubicgen.ConversionUtils;
import io.github.opencubicchunks.cubicchunks.cubicgen.cache.HashCache;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacerProvider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.OceanWaterReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.SurfaceDefaultReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.TerrainShapeReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.MesaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.MutatedSavannaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.SwampWaterWithLilypadReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.TaigaSurfaceReplacer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

// a small hack to get biome generation working with the new system
// todo: make it not hacky
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BiomeSource {

    private static final int SECTION_SIZE = 4;

    private static final int CHUNKS_CACHE_RADIUS = 3;
    private static final int CHUNKS_CACHE_SIZE = CHUNKS_CACHE_RADIUS * CHUNKS_CACHE_RADIUS;

    private static final int SECTIONS_CACHE_RADIUS = 16;
    private static final int SECTIONS_CACHE_SIZE = SECTIONS_CACHE_RADIUS * SECTIONS_CACHE_RADIUS;

    private static final ToIntFunction<ChunkPos> HASH_CHUNKS = v -> v.x * CHUNKS_CACHE_RADIUS + v.z;
    private static final ToIntFunction<Vec3i> HASH_SECTIONS = v -> v.getX() * SECTIONS_CACHE_RADIUS + v.getZ();

    private final Map<Biome, IBiomeBlockReplacer[]> biomeBlockReplacers = new IdentityHashMap<>();
    private final double[] nearBiomeWeightArray;

    private BiomeProvider biomeGen;
    private final int smoothRadius;
    private final int smoothDiameter;

    /** Mapping from chunk position to 4x4 sections 4x4 blocks each */
    private final HashCache<ChunkPos, CubicBiome[]> biomeCacheSectionsChunk;
    /** Mapping from chunk positions to Cache with sections of 16x16 blocks (chunk) */
    private final HashCache<ChunkPos, Biome[]> biomeCacheBlocks;
    /** Mapping from chunk positions to Cache with sections of 16x16 blocks (chunk) */
    private final HashCache<ChunkPos, int[]> biomeBlockReplacerCache;

    private final HashCache<Vec3i, BiomeTerrainData> biomeDataCache;

    public BiomeSource(World world, BiomeBlockReplacerConfig conf, BiomeProvider biomeGen, int smoothRadius) {
        this.biomeGen = biomeGen;
        this.smoothRadius = smoothRadius;
        this.smoothDiameter = smoothRadius * 2 + 1;

        this.nearBiomeWeightArray = new double[this.smoothDiameter * this.smoothDiameter];

        for (int x = -this.smoothRadius; x <= this.smoothRadius; x++) {
            for (int z = -this.smoothRadius; z <= this.smoothRadius; z++) {
                final double val = 10.0F / Math.sqrt(x * x + z * z + 0.2F);
                this.nearBiomeWeightArray[x + this.smoothRadius + (z + this.smoothRadius) * this.smoothDiameter] = val;
            }
        }

        this.biomeCacheSectionsChunk = HashCache.create(CHUNKS_CACHE_SIZE, HASH_CHUNKS, this::generateBiomeSections);
        this.biomeCacheBlocks = HashCache.create(CHUNKS_CACHE_SIZE, HASH_CHUNKS, this::generateBiomes);
        this.biomeDataCache = HashCache.create(SECTIONS_CACHE_SIZE, HASH_SECTIONS, this::generateBiomeTerrainData);
        this.biomeBlockReplacerCache = HashCache.create(CHUNKS_CACHE_SIZE, HASH_CHUNKS, this::generateReplacers);

        for (Biome biome : ForgeRegistries.BIOMES) {
            CubicBiome cubicBiome = CubicBiome.getCubic(biome);
            Iterable<IBiomeBlockReplacerProvider> providers = cubicBiome.getReplacerProviders();
            List<IBiomeBlockReplacer> replacers = new ArrayList<>();
            for (IBiomeBlockReplacerProvider prov : providers) {
                replacers.add(prov.create(world, conf));
            }

            biomeBlockReplacers.put(biome, replacers.toArray(new IBiomeBlockReplacer[0]));
        }
    }

    private int[] generateReplacers(ChunkPos pos) {
        Biome[] biomes = biomeCacheBlocks.get(pos);
        return this.mapToReplacers(biomes);
    }

    private BiomeTerrainData generateBiomeTerrainData(Vec3i pos) {

        // Calculate weighted average of nearby biomes height and volatility
        double smoothVolatility = 0.0F;
        double smoothHeight = 0.0F;

        double biomeWeightSum = 0.0F;
        final Biome centerBiomeConfig = getBiomeForSection(pos.getX(), pos.getZ()).getBiome();
        final int lookRadius = this.smoothRadius;

        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++) {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++) {
                final Biome biome = getBiomeForSection(pos.getX() + nextX, pos.getZ() + nextZ).getBiome();

                final double biomeHeight = biome.getBaseHeight();
                final double biomeVolatility = biome.getHeightVariation();

                double biomeWeight = calcBiomeWeight(nextX, nextZ, biomeHeight);

                biomeWeight = Math.abs(biomeWeight);
                if (biomeHeight > centerBiomeConfig.getBaseHeight()) {
                    // prefer biomes with lower height?
                    biomeWeight /= 2.0F;
                }
                smoothVolatility += biomeVolatility * biomeWeight;
                smoothHeight += biomeHeight * biomeWeight;

                biomeWeightSum += biomeWeight;
            }
        }

        smoothVolatility /= biomeWeightSum;
        smoothHeight /= biomeWeightSum;

        BiomeTerrainData data = new BiomeTerrainData();
        // Convert from vanilla height/volatility format
        // to something easier to predict
        data.heightVariation = ConversionUtils.biomeHeightVariationVanilla((float) smoothVolatility);
        data.height += ConversionUtils.biomeHeightVanilla((float) smoothHeight);
        return data;
    }

    private Biome[] generateBiomes(ChunkPos pos) {
        return biomeGen.getBiomes(null,
                cubeToMinBlock(pos.x),
                cubeToMinBlock(pos.z),
                ICube.SIZE, ICube.SIZE);
    }

    private CubicBiome[] generateBiomeSections(ChunkPos pos) {
        return mapToCubic(biomeGen.getBiomesForGeneration(null,
                pos.x * SECTION_SIZE, pos.z * SECTION_SIZE,
                SECTION_SIZE, SECTION_SIZE));
    }

    private CubicBiome[] mapToCubic(Biome[] vanillaBiomes) {
        CubicBiome[] cubicBiomes = new CubicBiome[vanillaBiomes.length];
        for (int i = 0; i < vanillaBiomes.length; i++) {
            cubicBiomes[i] = CubicBiome.getCubic(vanillaBiomes[i]);
        }
        return cubicBiomes;
    }

    private int[] mapToReplacers(Biome[] cubicBiomes) {
        int[] replacers = new int[cubicBiomes.length];
        for (int i = 0; i < cubicBiomes.length; i++) {
            Set<Class<? extends IBiomeBlockReplacer>> r = Arrays.stream(biomeBlockReplacers.get(cubicBiomes[i]))
                    .map(IBiomeBlockReplacer::getClass).collect(Collectors.toSet());
            int rint = 0;
            if (r.contains(TerrainShapeReplacer.class)) {
                rint |= 1 << IBiomeBlockReplacer.Type.SHAPE.ordinal();
            }
            if (r.contains(SurfaceDefaultReplacer.class)) {
                rint |= 1 << IBiomeBlockReplacer.Type.SURFACE.ordinal();
            }
            if (r.contains(MesaSurfaceReplacer.class)) {
                rint |= 1 << IBiomeBlockReplacer.Type.MESA_SURFACE.ordinal();
            }
            if (r.contains(MutatedSavannaSurfaceReplacer.class)) {
                rint |= 1 << IBiomeBlockReplacer.Type.MUTATED_SAVANNA.ordinal();
            }
            if (r.contains(TaigaSurfaceReplacer.class)) {
                rint |= 1 << IBiomeBlockReplacer.Type.TAIGA.ordinal();
            }
            if (r.contains(OceanWaterReplacer.class)) {
                rint |= 1 << IBiomeBlockReplacer.Type.OCEAN.ordinal();
            }
            if (r.contains(SwampWaterWithLilypadReplacer.class)) {
                rint |= 1 << IBiomeBlockReplacer.Type.SWAMP.ordinal();
            }
            replacers[i] = rint;
        }
        return replacers;
    }

    private static final int HEIGHT = 0, VOLATILITY = 1;
    private final double[] chunkBiomeHeightVolatility = new double[5 * 5 * 2];
    private final int[] replacers = new int[ICube.SIZE * ICube.SIZE];
    private Biome[] biomes;
    private int startX, startZ;

    public void initCube(int cubeX, int cubeY, int cubeZ) {
        final int startXSection = cubeX << 2;
        final int startZSection = cubeZ << 2;

        final int startX = cubeToMinBlock(cubeX);
        final int startZ = cubeToMinBlock(cubeZ);
        this.startX = startX;
        this.startZ = startZ;

        for (int sectionX = 0; sectionX < 5; sectionX++) {
            for (int sectionZ = 0; sectionZ < 5; sectionZ++) {
                int idx = sectionX * 5 + sectionZ;
                int idx2 = idx << 1;
                chunkBiomeHeightVolatility[idx2 | HEIGHT] =
                        genenerateHeight(sectionX + startXSection, sectionZ + startZSection);
                chunkBiomeHeightVolatility[idx2 | VOLATILITY] =
                        genenerateVolatility(sectionX + startXSection, sectionZ + startZSection);

            }
        }

        biomes = biomeCacheBlocks.get(new ChunkPos(cubeX, cubeZ));
        for (int localX = 0; localX < ICube.SIZE; localX++) {
            for (int localZ = 0; localZ < ICube.SIZE; localZ++) {
                replacers[localX << 4 | localZ] = generateReplacers(startX | localX, startZ | localZ);
            }
        }
    }

    public int getReplacers(int x, int y, int z) {
        int localX = x & 0xF;
        int localZ = z & 0xF;
        return replacers[localX << 4 | localZ];
    }

    public double getHeight(int x, int y, int z) {
        int sectionX = (x - startX) >> 2;
        int sectionZ = (z - startZ) >> 2;
        return chunkBiomeHeightVolatility[(sectionX * 5 + sectionZ) << 1 | HEIGHT];
    }

    public Biome getBiome(int x, int z) {
        return biomes[(z & 15)*16 + (x & 15)];
    }

    public double getVolatility(int x, int y, int z) {
        int sectionX = (x - startX) >> 2;
        int sectionZ = (z - startZ) >> 2;
        return chunkBiomeHeightVolatility[(sectionX * 5 + sectionZ) << 1 | VOLATILITY];
    }

    public double genenerateHeight(int x, int z) {
        return biomeDataCache.get(new Vec3i(x, 0, z)).height;
    }

    public double genenerateVolatility(int x, int z) {
        return biomeDataCache.get(new Vec3i(x, 0, z)).heightVariation;
    }

    public int generateReplacers(int blockX, int blockZ) {
        ChunkPos pos = new ChunkPos(Coords.blockToCube(blockX), Coords.blockToCube(blockZ));
        return biomeBlockReplacerCache.get(pos)[Coords.blockToLocal(blockZ) << 4 | Coords.blockToLocal(blockX)];
    }

    private CubicBiome getBiomeForSection(int x, int z) {
        int localX = x & 3;
        int localZ = z & 3;

        int chunkX = x >> 2;
        int chunkZ = z >> 2;

        return biomeCacheSectionsChunk.get(new ChunkPos(chunkX, chunkZ))[localX | localZ << 2];
    }

    private double calcBiomeWeight(int nextX, int nextZ, double biomeHeight) {
        return this.nearBiomeWeightArray[nextX + this.smoothRadius + (nextZ + this.smoothRadius) * this.smoothDiameter] / (biomeHeight + 2.0F);
    }

    private static final class BiomeTerrainData {

        double height, heightVariation;
    }
}
