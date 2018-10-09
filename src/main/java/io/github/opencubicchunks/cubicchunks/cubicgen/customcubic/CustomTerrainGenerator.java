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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.blockToLocal;
import static io.github.opencubicchunks.cubicchunks.api.util.Coords.cubeToMinBlock;

import com.flowpowered.noise.module.source.Perlin;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubeGeneratorsRegistry;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.cubicgen.BasicCubeGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.cache.HashCacheDensityProvider;
import io.github.opencubicchunks.cubicchunks.cubicgen.cache.HashCacheDoubles;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.OceanWaterReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.SurfaceDefaultReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.TerrainShapeReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.MesaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.MutatedSavannaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.SwampWaterWithLilypadReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.replacer.TaigaSurfaceReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.BiomeSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseConsumer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.CubicCaveGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.CubicRavineGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.CubicStructureGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature.CubicFeatureGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature.CubicStrongholdGenerator;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A terrain generator that supports infinite(*) worlds
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomTerrainGenerator extends BasicCubeGenerator {

    private static final int CACHE_SIZE_2D = 16 * 16;
    private static final int CACHE_SIZE_3D = 16 * 16 * 16;
    private static final ToIntFunction<Vec3i> HASH_2D = (v) -> v.getX() + v.getZ() * 5;
    private static final ToIntFunction<Vec3i> HASH_3D = (v) -> v.getX() + v.getZ() * 5 + v.getY() * 25;
    private final Map<CustomGeneratorSettings.IntAABB, CustomTerrainGenerator> areaGenerators = new HashMap<>();
    // Number of octaves for the noise function
    private IBuilder terrainBuilder;
    private final BiomeSource biomeSource;
    private final CustomGeneratorSettings conf;
    private final Map<Biome, ICubicPopulator> populators = new HashMap<>();

    //TODO: Implement more structures
    @Nonnull private CubicCaveGenerator caveGenerator = new CubicCaveGenerator();
    @Nonnull private CubicStructureGenerator ravineGenerator;
    @Nonnull private CubicFeatureGenerator strongholds;

    public CustomTerrainGenerator(World world, final long seed) {
        this(world, CustomGeneratorSettings.fromJson(world.getWorldInfo().getGeneratorOptions()), seed);
    }

    public CustomTerrainGenerator(World world, CustomGeneratorSettings settings, final long seed) {
        super(world);
        this.conf = settings;

        for (Biome biome : ForgeRegistries.BIOMES) {
            CubicBiome cubicBiome = CubicBiome.getCubic(biome);
            populators.put(biome, cubicBiome.getDecorator(conf));
        }

        this.strongholds = new CubicStrongholdGenerator(conf);
        this.ravineGenerator = new CubicRavineGenerator(conf);

        this.biomeSource = new BiomeSource(world, conf.createBiomeBlockReplacerConfig(), world.getBiomeProvider(), 2);
        initGenerator(seed);

        if (settings.cubeAreas != null) {
            for (CustomGeneratorSettings.IntAABB aabb : settings.cubeAreas.keySet()) {
                this.areaGenerators.put(aabb, new CustomTerrainGenerator(world, settings.cubeAreas.get(aabb), seed));
            }
        }
    }

    private void initGenerator(long seed) {
        initNew(seed);
    }

    @Override public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        if (!areaGenerators.isEmpty()) {
            for (CustomGeneratorSettings.IntAABB aabb : areaGenerators.keySet()) {
                if (!aabb.contains(cubeX, cubeY, cubeZ)) {
                    continue;
                }
                return areaGenerators.get(aabb).generateCube(cubeX, cubeY, cubeZ);
            }
        }
        CubePrimer primer = new CubePrimer();
        generate(primer, cubeX, cubeY, cubeZ);
        generateStructures(primer, new CubePos(cubeX, cubeY, cubeZ));
        return primer;
    }

    @Override public void populate(ICube cube) {
        if (!areaGenerators.isEmpty()) {
            for (CustomGeneratorSettings.IntAABB aabb : areaGenerators.keySet()) {
                if (!aabb.contains(cube.getX(), cube.getY(), cube.getZ())) {
                    continue;
                }
                areaGenerators.get(aabb).populate(cube);
                return;
            }
        }
        /**
         * If event is not canceled we will use default biome decorators and
         * cube populators from registry.
         **/
        if (!MinecraftForge.EVENT_BUS.post(new CubePopulatorEvent(world, cube))) {
            CubicBiome cubicBiome = CubicBiome.getCubic(cube.getWorld().getBiome(Coords.getCubeCenter(cube)));

            CubePos pos = cube.getCoords();
            // For surface generators we should actually use special RNG with
            // seed
            // that depends only in world seed and cube X/Z
            // but using this for surface generation doesn't cause any
            // noticeable issues
            Random rand = Coords.coordsSeedRandom(cube.getWorld().getSeed(), cube.getX(), cube.getY(), cube.getZ());

            populators.get(cubicBiome.getBiome()).generate(world, rand, pos, cubicBiome.getBiome());
            CubeGeneratorsRegistry.generateWorld(world, rand, pos, cubicBiome.getBiome());

            strongholds.generateStructure((World) world, rand, pos);
        }
    }

    @Override
    public void recreateStructures(ICube cube) {
        this.strongholds.generate(world, null, cube.getCoords());
    }

    @Nullable @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        if ("Stronghold".equals(name)) {
            return strongholds.getClosestStrongholdPos((World) world, pos, true);
        }
        return null;
    }


    private HashCacheDensityProvider densityBiuilder;
    private IBuilder perlinHeight;

    private TerrainShapeReplacer terrainShape;
    private SurfaceDefaultReplacer surface;
    private MesaSurfaceReplacer mesaSurface;
    private MutatedSavannaSurfaceReplacer mutatedSavanna;
    private TaigaSurfaceReplacer taiga;
    private OceanWaterReplacer ocean;
    private SwampWaterWithLilypadReplacer swamp;

    private void initNew(long seed) {
        Random rnd = new Random(seed);
        double lowOffset = conf.lowNoiseOffset;
        double lowFactor = conf.lowNoiseFactor;

        double highOffset = conf.highNoiseOffset;
        double highFactor = conf.highNoiseFactor;

        double selectorOffset = conf.selectorNoiseOffset;
        double selectorFactor = conf.selectorNoiseFactor;

        double heightVarFactor = conf.heightVariationFactor;
        double heightVarOffset = conf.heightVariationOffset;

        double specialVariationFactor = conf.specialHeightVariationFactorBelowAverageY;

        double selectorFreqX = conf.selectorNoiseFrequencyX;
        double selectorFreqY = conf.selectorNoiseFrequencyY;
        double selectorFreqZ = conf.selectorNoiseFrequencyZ;

        Perlin perlinSel = new Perlin();
        perlinSel.setOctaveCount((conf.selectorNoiseOctaves));
        perlinSel.setSeed(rnd.nextInt());

        double lowFreqX = conf.lowNoiseFrequencyX;
        double lowFreqY = conf.lowNoiseFrequencyY;
        double lowFreqZ = conf.lowNoiseFrequencyZ;
        Perlin perlinLow = new Perlin();
        perlinLow.setOctaveCount(conf.lowNoiseOctaves);
        perlinLow.setSeed(rnd.nextInt());

        double highFreqX = conf.highNoiseFrequencyX;
        double highFreqY = conf.highNoiseFrequencyY;
        double highFreqZ = conf.highNoiseFrequencyZ;
        Perlin perlinHigh = new Perlin();
        perlinHigh.setOctaveCount(conf.highNoiseOctaves);
        perlinHigh.setSeed(rnd.nextInt());

        double depthFreqX = conf.depthNoiseFrequencyX;
        double depthFreqZ = conf.depthNoiseFrequencyZ;
        Perlin perlinHeight = new Perlin();
        perlinHeight.setOctaveCount(conf.depthNoiseOctaves);
        perlinHeight.setSeed(rnd.nextInt());
        this.perlinHeight = (x, y, z) -> perlinHeight.getValue(x*depthFreqX, 0, z*depthFreqZ);
        this.perlinHeight = this.perlinHeight.cached2d(CACHE_SIZE_2D, HASH_2D);

        densityBiuilder = HashCacheDensityProvider.create(CACHE_SIZE_3D,
                (x, y, z) -> x + z * 5 + y * 25,
                (x, y, z, biomeVolRaw, biomeHeight, height2d) -> {
                    double biomeVol = biomeVolRaw;
                    if (y < biomeHeight) {
                        biomeVol *= specialVariationFactor;
                    }
                    biomeVol = biomeVol * heightVarFactor + heightVarOffset;

                    double lowNoise = perlinLow.getValue(x * lowFreqX, y * lowFreqY, z * lowFreqZ) * 2 - 1;
                    lowNoise = lowNoise * lowFactor + lowOffset;

                    double highNoise = perlinHigh.getValue(x * highFreqX, y * highFreqY, z * highFreqZ) * 2 - 1;
                    highNoise = highNoise * highFactor + highOffset;

                    double selectorNoise = perlinSel.getValue(x * selectorFreqX, y * selectorFreqY, z * selectorFreqZ) * 2 - 1;
                    selectorNoise = MathHelper.clamp(selectorNoise * selectorFactor + selectorOffset, 0, 1);

                    double density = (highNoise - lowNoise) * selectorNoise + lowNoise + height2d;
                    density = density * biomeVol + biomeHeight;

                    if (biomeVol > 0) {
                        density -= y;
                    } else {
                        density += y;
                    }
                    return density;
                });

        this.terrainShape = (TerrainShapeReplacer) TerrainShapeReplacer.provider().create(world, conf.replacerConfig);
        this.surface = (SurfaceDefaultReplacer) SurfaceDefaultReplacer.provider().create(world, conf.replacerConfig);
        this.mesaSurface = (MesaSurfaceReplacer) MesaSurfaceReplacer.provider().create(world, conf.replacerConfig);
        this.mutatedSavanna = (MutatedSavannaSurfaceReplacer) MutatedSavannaSurfaceReplacer.provider().create(world, conf.replacerConfig);
        this.taiga = (TaigaSurfaceReplacer) TaigaSurfaceReplacer.provider().create(world, conf.replacerConfig);
        this.ocean = (OceanWaterReplacer) OceanWaterReplacer.provider().create(world, conf.replacerConfig);
        this.swamp = (SwampWaterWithLilypadReplacer) SwampWaterWithLilypadReplacer.provider().create(world, conf.replacerConfig);
    }

    private final double[] densities = new double[5 * 5 * 3];
    private final double[] values = new double[16 * 16 * 16 * 4];

    /**
     * Generate the cube as the specified location
     *
     * @param cubePrimer cube primer to use
     * @param cubeX cube x location
     * @param cubeY cube y location
     * @param cubeZ cube z location
     */
    private void generate(final CubePrimer cubePrimer, int cubeX, int cubeY, int cubeZ) {
        // when debugging is enabled, allow reloading generator settings after pressing L
        // no need to restart after applying changes.
        // Seed it changed to some constant because world isn't easily accessible here
        if (CustomCubicMod.DEBUG_ENABLED && FMLCommonHandler.instance().getSide().isClient() && Keyboard.isKeyDown(Keyboard.KEY_L)) {
            initGenerator(42);
        }

        biomeSource.initCube(cubeX, cubeY, cubeZ);
        initDensity(cubeX, cubeY, cubeZ);
        fillValues();
        acceptValues(cubePrimer, cubeX, cubeY, cubeZ);
    }

    private void acceptValues(CubePrimer cubePrimer, int cubeX, int cubeY, int cubeZ) {

        int cubeMinX = cubeToMinBlock(cubeX);
        int cubeMinY = cubeToMinBlock(cubeY);
        int cubeMinZ = cubeToMinBlock(cubeZ);

        double[] data = this.values;
        for (int localX = 0; localX < 16; localX++) {
            int blockX = cubeMinX | localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockZ = cubeMinZ | localZ;
                for (int localY = 0; localY < 16; localY++) {
                    int blockY = cubeMinY | localY;
                    int idx = (localX << 8 | localZ << 4 | localY) << 2;

                    double dx = data[idx];
                    double dy = data[idx | 1];
                    double dz = data[idx | 2];
                    double v = data[idx | 3];

                    IBlockState state = getBlock(blockX, blockY, blockZ, dx, dy, dz, v);
                    cubePrimer.setBlockState(localX, localY, localZ, state);
                }
            }
        }
    }

    private void fillValues() {
        int xScale = 4;
        int yScale = 8;
        int zScale = 4;

        double stepX = 1.0 / xScale;
        double stepY = 1.0 / yScale;
        double stepZ = 1.0 / zScale;

        double[] densities = this.densities;
        double[] values = this.values;

        for (int sectionX = 0; sectionX < 4; ++sectionX) {
            int x0Idx = sectionX * 5 * 3;
            int x1Idx = (sectionX + 1) * 5 * 3;
            int x = sectionX * 4;
            for (int sectionZ = 0; sectionZ < 4; ++sectionZ) {
                int z0Idx = sectionZ * 3;
                int z1Idx = (sectionZ + 1) * 3;
                int z = sectionZ * 4;
                for (int sectionY = 0; sectionY < 2; ++sectionY) {
                    int y0Idx = sectionY;
                    int y1Idx = sectionY + 1;
                    int y = sectionY * 8;

                    final double v000 = densities[x0Idx + y0Idx + z0Idx];
                    final double v001 = densities[x0Idx + y0Idx + z1Idx];
                    final double v010 = densities[x0Idx + y1Idx + z0Idx];
                    final double v011 = densities[x0Idx + y1Idx + z1Idx];
                    final double v100 = densities[x1Idx + y0Idx + z0Idx];
                    final double v101 = densities[x1Idx + y0Idx + z1Idx];
                    final double v110 = densities[x1Idx + y1Idx + z0Idx];
                    final double v111 = densities[x1Idx + y1Idx + z1Idx];

                    double v0y0 = v000;
                    double v0y1 = v001;
                    double v1y0 = v100;
                    double v1y1 = v101;
                    final double d_dy__0y0 = (v010 - v000) * stepY;
                    final double d_dy__0y1 = (v011 - v001) * stepY;
                    final double d_dy__1y0 = (v110 - v100) * stepY;
                    final double d_dy__1y1 = (v111 - v101) * stepY;

                    for (int yRel = 0; yRel < yScale; ++yRel) {
                        // int idx = (localx << 8 | localz << 4 | localy) << 2;
                        int noiseIdxY = (y | yRel) << 2;
                        double vxy0 = v0y0;
                        double vxy1 = v0y1;
                        final double d_dx__xy0 = (v1y0 - v0y0) * stepX;
                        final double d_dx__xy1 = (v1y1 - v0y1) * stepX;

                        // gradients start
                        double v0yz = v0y0;
                        double v1yz = v1y0;

                        final double d_dz__0yz = (v0y1 - v0y0) * stepX;
                        final double d_dz__1yz = (v1y1 - v1y0) * stepX;
                        // gradients end

                        for (int xRel = 0; xRel < xScale; ++xRel) {
                            int noiseIdxX = (x | xRel) << 10;
                            final double d_dz__xyz = (vxy1 - vxy0) * stepZ;
                            double vxyz = vxy0;

                            // gradients start
                            final double d_dx__xyz = (v1yz - v0yz) * stepZ;
                            // gradients end
                            for (int zRel = 0; zRel < zScale; ++zRel) {
                                int noiseIdxZ = (z | zRel) << 6;

                                int idx = noiseIdxX | noiseIdxY | noiseIdxZ;
                                values[idx] = d_dx__xyz;
                                values[idx | 2] = d_dz__xyz;
                                values[idx | 3] = vxyz;

                                vxyz += d_dz__xyz;
                            }

                            vxy0 += d_dx__xy0;
                            vxy1 += d_dx__xy1;
                            // gradients start
                            v0yz += d_dz__0yz;
                            v1yz += d_dz__1yz;
                            // gradients end
                        }

                        v0y0 += d_dy__0y0;
                        v0y1 += d_dy__0y1;
                        v1y0 += d_dy__1y0;
                        v1y1 += d_dy__1y1;

                    }
                    // gradients start
                    double v00z = v000;
                    double v01z = v010;
                    double v10z = v100;
                    double v11z = v110;

                    final double d_dz__00z = (v001 - v000) * stepZ;
                    final double d_dz__01z = (v011 - v010) * stepZ;
                    final double d_dz__10z = (v101 - v100) * stepZ;
                    final double d_dz__11z = (v111 - v110) * stepZ;

                    for (int zRel = 0; zRel < zScale; ++zRel) {
                        int noiseIdxZ = (z | zRel) << 6;

                        double vx0z = v00z;
                        double vx1z = v01z;

                        final double d_dx__x0z = (v10z - v00z) * stepX;
                        final double d_dx__x1z = (v11z - v01z) * stepX;

                        for (int xRel = 0; xRel < xScale; ++xRel) {
                            int noiseIdxX = (x | xRel) << 10;

                            double d_dy__xyz = (vx1z - vx0z) * stepY;

                            for (int yRel = 0; yRel < yScale; ++yRel) {
                                int noiseIdxY = (y | yRel) << 2;
                                int idx = noiseIdxX | noiseIdxY | noiseIdxZ;
                                values[idx | 1] = d_dy__xyz;
                            }

                            vx0z += d_dx__x0z;
                            vx1z += d_dx__x1z;
                        }
                        v00z += d_dz__00z;
                        v01z += d_dz__01z;
                        v10z += d_dz__10z;
                        v11z += d_dz__11z;
                    }
                    // gradients end
                }
            }
        }
    }

    private void initDensity(int cubeX, int cubeY, int cubeZ) {
        double depthOffset = conf.depthNoiseOffset;
        double depthFactor = conf.depthNoiseFactor;

        double heightFactor = conf.heightFactor;
        double heightOffset = conf.heightOffset;

        double[] densities = this.densities;

        int minBlockX = cubeX * 16;
        int minBlockY = cubeY * 16;
        int minBlockZ = cubeZ * 16;

        for (int xSection = 0; xSection < 5; xSection++) {
            int xIdx = xSection * 5 * 3;
            int blockX = (xSection << 2) + minBlockX;
            for (int zSection = 0; zSection < 5; zSection++) {
                int xzIdx = zSection * 3 + xIdx;
                int blockZ = (zSection << 2) + minBlockZ;
                double depthNoise = perlinHeight.get(blockX, 0, blockZ) * 2 - 1;
                depthNoise *= depthFactor;
                depthNoise += depthOffset;
                if (depthNoise < 0) {
                    depthNoise *= -0.3;
                }
                depthNoise = MathHelper.clamp(depthNoise * 3 - 2, -2, 1);
                if (depthNoise < 0) {
                    depthNoise *= 1.0 / (2 * 2 * 1.4);
                } else {
                    depthNoise *= 1.0 / 8.0;
                }
                depthNoise *= 0.2 * 17 / 64.0;

                double biomeHeight = biomeSource.getHeight(blockX, 0, blockZ) * heightFactor + heightOffset;
                double biomeVolRaw = biomeSource.getVolatility(blockX, 0, blockZ);

                for (int ySection = 0; ySection < 3; ySection++) {
                    int xyzIdx = ySection + xzIdx;
                    int blockY = (ySection << 3) + minBlockY;
                    densities[xyzIdx] = densityBiuilder.get(blockX, blockY, blockZ, biomeVolRaw, biomeHeight, depthNoise);
                }
            }
        }
    }

    /**
     * Retrieve the blockstate appropriate for the specified builder entry
     *
     * @return The block state
     */
    private IBlockState getBlock(int x, int y, int z, double dx, double dy, double dz, double density) {
        int replacers = biomeSource.getReplacers(x, y, z);
        Biome biome = biomeSource.getBiome(x, z);
        IBlockState block = Blocks.AIR.getDefaultState();

        /*
        SHAPE,
        SURFACE,
        MESA_SURFACE,
        MUTATED_SAVANNA,
        TAIGA,
        OCEAN,
        SWAMP
        */
        if ((replacers & 1) != 0) {
            block = terrainShape.getReplacedBlock(biome, block, x, y, z, dx, dy, dz, density);
        }
        if ((replacers & 2) != 0) {
            block = surface.getReplacedBlock(biome, block, x, y, z, dx, dy, dz, density);
        }
        if ((replacers & 4) != 0) {
            block = mesaSurface.getReplacedBlock(biome, block, x, y, z, dx, dy, dz, density);
        }
        if ((replacers & 8) != 0) {
            block = mutatedSavanna.getReplacedBlock(biome, block, x, y, z, dx, dy, dz, density);
        }
        if ((replacers & 16) != 0) {
            block = taiga.getReplacedBlock(biome, block, x, y, z, dx, dy, dz, density);
        }
        if ((replacers & 32) != 0) {
            block = ocean.getReplacedBlock(biome, block, x, y, z, dx, dy, dz, density);
        }
        if ((replacers & 64) != 0) {
            block = swamp.getReplacedBlock(biome, block, x, y, z, dx, dy, dz, density);
        }
        return block;
    }

    private void generateStructures(CubePrimer cube, CubePos cubePos) {
        // generate world populator
        if (this.conf.caves) {
            this.caveGenerator.generate(world, cube, cubePos);
        }
        if (this.conf.ravines) {
            this.ravineGenerator.generate(world, cube, cubePos);
        }
        if (this.conf.strongholds) {
            this.strongholds.generate(world, cube, cubePos);
        }
    }
}
