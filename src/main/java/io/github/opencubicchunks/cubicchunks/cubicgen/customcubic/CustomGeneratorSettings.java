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
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.SyntaxError;
import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.ConversionUtils;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.RngHash;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.world.storage.IWorldInfoAccess;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.PresetLoadError;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BiomeDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.biome.BiomeSavannaMutated;
import net.minecraft.world.biome.BiomeSwamp;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.storage.ISaveHandler;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod.LOGGER;

public class CustomGeneratorSettings {
    /**
     * Note: many of these values are unused yet
     */

    /**
     * Vanilla standard options
     * <p>
     * Page 1
     */
    public boolean caves = true;

    public boolean strongholds = true;
    public boolean alternateStrongholdsPositions = false; // TODO: add to gui
    public boolean villages = true;

    public boolean mineshafts = true;
    public boolean temples = true;

    public boolean oceanMonuments = true;
    public boolean woodlandMansions = true;

    public boolean ravines = true;
    public boolean dungeons = true;

    public int dungeonCount = 7;

    public List<LakeConfig> lakes = new ArrayList<>();

    public int biome = -1;
    public int biomeSize = 4;
    public int riverSize = 4;

    /**
     * Vanilla standard options
     * <p>
     * Page 2
     */

    // probability: (vanillaChunkHeight/oreGenRangeSize) / amountOfICube.InVanillaChunk

    public StandardOreList standardOres = new StandardOreList();

    public PeriodicOreList periodicGaussianOres = new PeriodicOreList();

    /**
     * Terrain shape
     */

    public float expectedBaseHeight = 64;
    public float expectedHeightVariation = 64;
    public float actualHeight = 256;

    public float heightVariationFactor = 64;
    public float specialHeightVariationFactorBelowAverageY = 0.25f;
    public float heightVariationOffset = 0;
    public float heightFactor = 64;// height scale
    public float heightOffset = 64;// sea level

    public float depthNoiseFactor = ConversionUtils.VANILLA_DEPTH_NOISE_FACTOR;
    public float depthNoiseOffset = 0;
    public float depthNoiseFrequencyX = ConversionUtils.VANILLA_DEPTH_NOISE_FREQUENCY;
    public float depthNoiseFrequencyZ = ConversionUtils.VANILLA_DEPTH_NOISE_FREQUENCY;
    public int depthNoiseOctaves = 16;

    public float selectorNoiseFactor = ConversionUtils.VANILLA_SELECTOR_NOISE_FACTOR;
    public float selectorNoiseOffset = ConversionUtils.VANILLA_SELECTOR_NOISE_OFFSET;
    public float selectorNoiseFrequencyX = ConversionUtils.VANILLA_SELECTOR_NOISE_FREQUENCY_XZ;
    public float selectorNoiseFrequencyY = ConversionUtils.VANILLA_SELECTOR_NOISE_FREQUENCY_Y;
    public float selectorNoiseFrequencyZ = ConversionUtils.VANILLA_SELECTOR_NOISE_FREQUENCY_XZ;
    public int selectorNoiseOctaves = 8;

    public float lowNoiseFactor = 1;
    public float lowNoiseOffset = 0;
    public float lowNoiseFrequencyX = ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ;
    public float lowNoiseFrequencyY = ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_Y;
    public float lowNoiseFrequencyZ = ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ;
    public int lowNoiseOctaves = 16;

    public float highNoiseFactor = 1;
    public float highNoiseOffset = 0;
    public float highNoiseFrequencyX = ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ;
    public float highNoiseFrequencyY = ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_Y;
    public float highNoiseFrequencyZ = ConversionUtils.VANILLA_LOWHIGH_NOISE_FREQUENCY_XZ;
    public int highNoiseOctaves = 16;
    public List<ReplacerConfig> replacers = new ArrayList<>();

    // note: the AABB uses cube coords to simplify the generator
    public CubeAreas cubeAreas = new CubeAreas(new ArrayList<>());

    // TODO: public boolean negativeHeightVariationInvertsTerrain = true;
    public int version = CustomGeneratorSettingsFixer.LATEST;

    public CustomGeneratorSettings() {
    }

    public JsonObject toJsonObject() {
        return (JsonObject) CustomGenSettingsSerialization.jankson().toJson(this);
    }

    // TODO: clean up all the conversions between CustomGeneratorSettings, JsonObject and String

    public static JsonObject asJsonObject(String jsonString) {
        try {
            return CustomGeneratorSettingsFixer.INSTANCE.fixJson(jsonString);
        } catch (PresetLoadError err) {
            throw new RuntimeException(err);
        }// catch (SyntaxError err) {
        //  String message = err.getMessage() + "\n" + err.getLineMessage();
        //  throw new RuntimeException(message, err);
        // }
    }

    @Deprecated // the only remaining use is to validate json string.
    // This needs special validation code to catch other types of issues too
    public static CustomGeneratorSettings fromJson(String jsonString) {
        try {
            return CustomGeneratorSettingsFixer.INSTANCE.fixPreset(jsonString);
        } catch (PresetLoadError | DeserializationException err) {
            throw new RuntimeException(err);
        }// catch (SyntaxError err) {
        //  String message = err.getMessage() + "\n" + err.getLineMessage();
        //  throw new RuntimeException(message, err);
        // }
    }

    @Nullable
    public static String loadJsonStringFromSaveFolder(ISaveHandler saveHandler) {
        File externalGeneratorPresetFile = getPresetFile(saveHandler);
        if (externalGeneratorPresetFile.exists()) {
            try (SeekableByteChannel channel = Files.newByteChannel(externalGeneratorPresetFile.toPath())) {
                ByteBuffer buf = ByteBuffer.allocate((int) channel.size());
                channel.read(buf);
                return new String(buf.array(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return null;
    }

    private static File getPresetFolder(ISaveHandler saveHandler) {
        return new File(saveHandler.getWorldDirectory(),
                "/data/" + CustomCubicMod.MODID + "/");
    }

    public static File getPresetFile(ISaveHandler saveHandler) {
        return new File(getPresetFolder(saveHandler),
                "custom_generator_settings.json");
    }

    public static CustomGeneratorSettings getFromWorld(World world) {
        try {
            String jsonString = world.getWorldInfo().getGeneratorOptions();
            jsonString = CustomGeneratorSettingsFixer.INSTANCE.fixJsonString(jsonString, null);
            IWorldInfoAccess wia = (IWorldInfoAccess) world.getWorldInfo();
            wia.setGeneratorOptions(jsonString);

            return CustomGenSettingsSerialization.jankson().fromJsonCarefully(jsonString, CustomGeneratorSettings.class);
        } catch (PresetLoadError | DeserializationException err) {
            throw new RuntimeException(err);
        } catch (SyntaxError err) {
            String message = err.getMessage() + "\n" + err.getLineMessage();
            throw new RuntimeException(message, err);
        }
    }

    public static void saveToFile(ISaveHandler saveHandler, String json) {
        File folder = getPresetFolder(saveHandler);
        folder.mkdirs();
        File settingsFile = getPresetFile(saveHandler);
        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write(json);
            CustomCubicMod.LOGGER.info("Generator settings saved at " + settingsFile.getAbsolutePath());
        } catch (IOException e) {
            CustomCubicMod.LOGGER.error("Cannot create new directory at " + folder.getAbsolutePath());
            CustomCubicMod.LOGGER.error(json);
            CustomCubicMod.LOGGER.catching(e);
        }
    }

    public static CustomGeneratorSettings defaults() {
        CustomGeneratorSettings settings = new CustomGeneratorSettings();
        {
            settings.standardOres.list.addAll(Arrays.asList(
                    StandardOreConfig.builder()
                            .block(Blocks.DIRT.getDefaultState())
                            .size(33).attempts(10).probability(1f / (256f / ICube.SIZE)).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.GRAVEL.getDefaultState())
                            .size(33).attempts(8).probability(1f / (256f / ICube.SIZE)).create(),

                    StandardOreConfig.builder()
                            .block(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE))
                            .size(33).attempts(10).probability(256f / 80f / (256f / ICube.SIZE))
                            .maxHeight((80f - 64f) / 64f).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE))
                            .size(33).attempts(10).probability(256f / 80f / (256f / ICube.SIZE))
                            .maxHeight((80f - 64f) / 64f).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
                            .size(33).attempts(10).probability(256f / 80f / (256f / ICube.SIZE))
                            .maxHeight((80f - 64f) / 64f).create(),

                    StandardOreConfig.builder()
                            .block(Blocks.COAL_ORE.getDefaultState())
                            .size(17).attempts(20).probability(256f / 128f / (256f / ICube.SIZE))
                            .maxHeight(1).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.IRON_ORE.getDefaultState())
                            .size(9).attempts(20).probability(256f / 64f / (256f / ICube.SIZE))
                            .maxHeight(0).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.GOLD_ORE.getDefaultState())
                            .size(9).attempts(2).probability(256f / 32f / (256f / ICube.SIZE))
                            .maxHeight(-0.5f).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.REDSTONE_ORE.getDefaultState())
                            .size(8).attempts(8).probability(256f / 16f / (256f / ICube.SIZE))
                            .maxHeight(-0.75f).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.DIAMOND_ORE.getDefaultState())
                            .size(8).attempts(1).probability(256f / 16f / (256f / ICube.SIZE))
                            .maxHeight(-0.75f).create(),

                    StandardOreConfig.builder()
                            .block(Blocks.EMERALD_ORE.getDefaultState())
                            .size(1).attempts(11).probability(0.5f * 256f / 28f / (256f / ICube.SIZE))
                            .maxHeight(0)
                            .biomes(Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.MUTATED_EXTREME_HILLS,
                                    Biomes.MUTATED_EXTREME_HILLS_WITH_TREES).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE))
                            .size(7).attempts(7).probability(256f / 64f / (256f / ICube.SIZE))
                            .maxHeight(-0.5f)
                            .biomes(Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.MUTATED_EXTREME_HILLS,
                                    Biomes.MUTATED_EXTREME_HILLS_WITH_TREES).create(),
                    StandardOreConfig.builder()
                            .block(Blocks.GOLD_ORE.getDefaultState())
                            .size(20).attempts(2).probability(256f / 32f / (256f / ICube.SIZE))
                            .minHeight(-0.5f).maxHeight(0.25f)
                            .biomes(Biomes.MESA, Biomes.MESA_CLEAR_ROCK, Biomes.MESA_ROCK, Biomes.MUTATED_MESA, Biomes.MUTATED_MESA_CLEAR_ROCK,
                                    Biomes.MUTATED_MESA_ROCK).create()
            ));
        }

        {
            settings.periodicGaussianOres.list.addAll(Arrays.asList(
                    PeriodicGaussianOreConfig.builder()
                            .block(Blocks.LAPIS_ORE.getDefaultState())
                            .size(7).attempts(1).probability(0.933307775f) //resulted by approximating triangular behaviour with bell curve
                            .heightMean(-0.75f/*first belt at=16*/).heightStdDeviation(0.11231704455f/*x64 = 7.1882908513*/)
                            .heightSpacing(3.0f/*192*/)
                            .maxHeight(-0.5f).create()
            ));
        }

        {
            settings.lakes.addAll(Arrays.asList(
                    LakeConfig.builder().setBlock(Blocks.LAVA)
                            .setBiomes(FilterType.EXCLUDE, new BiomeDesc[0])
                            .setMainProbability(UserFunction.builder()
                                    // same as vanilla for y0-127, probabilities near y=256 are very low, so don't use them
                                    .point(0, 4 / 263f)
                                    .point(7, 4 / 263f)
                                    .point(8, 247 / 16306f)
                                    .point(62, 193 / 16306f)
                                    .point(63, 48 / 40765f)
                                    .point(127, 32 / 40765f)
                                    .point(128, 32 / 40765f)
                                    .build())
                            .setSurfaceProbability(UserFunction.builder()
                                    // sample vanilla probabilities at y=0, 31, 63, 95, 127
                                    .point(-1, 19921 / 326120f)
                                    .point(0, 19921 / 326120f)
                                    .point(31, 1332 / 40765f)
                                    .point(63, 579 / 81530f)
                                    .point(95, 161 / 32612f)
                                    .point(127, 129 / 40765f)
                                    .point(128, 129 / 40765f)
                                    .build())
                            .build(),
                    LakeConfig.builder().setBlock(Blocks.WATER)
                            .setBiomes(FilterType.EXCLUDE, Biomes.DESERT, Biomes.DESERT_HILLS)
                            .setMainProbability(UserFunction.builder()
                                    // same as vanilla
                                    .point(0, 1 / 64f)
                                    .build())
                            .setSurfaceProbability(UserFunction.builder()
                                    // same as vanilla for y=0-128, probabilities get too low at 2xx heights so dont use them
                                    .point(-1, 0.25f)
                                    .point(0, 0.25f)
                                    .point(128, 0.125f)
                                    .point(129, 0.125f)
                                    .build())
                            .build()
            ));
        }

        {

            double depthNoiseFreq = ConversionUtils.frequencyFromVanilla(0.0625f, 4);
            double depthNoiseFactor = 0.55 * ((1 << 4) - 1) / 3.0;
            double depthNoiseOffset = 3.0;
            // terrain
            settings.replacers.add(new DensityRangeReplacerConfig.Builder()
                    .setMinY(Integer.MIN_VALUE)
                    .setMaxY(Integer.MAX_VALUE)
                    .setBiomeFilter(null)
                    .setBlockFilterType(FilterType.EXCLUDE)
                    .setFilterBlocks(new ArrayList<>())
                    .setMinDensity(0)
                    .setMaxDensity(Double.POSITIVE_INFINITY)
                    .setBlockInRange(new BlockStateDesc(Blocks.STONE.getDefaultState()))
                    .setBlockOutOfRange(null)
                    .build());

            // surface
            settings.replacers.add(new MainSurfaceReplacerConfig.Builder()
                    .setMinY(Integer.MIN_VALUE)
                    .setMaxY(Integer.MAX_VALUE)
                    .setBiomeFilter(new AllOfCompositeBiomeFilter(
                            new ExcludeBiomeClass(
                                    BiomeClassMatchType.RAW_EXACT,
                                    BiomeSavannaMutated.class.getName(),
                                    BiomeMesa.class.getName()
                            ),
                            new ExcludeBiomes(
                                    new BiomeDesc("minecraft:redwood_taiga"),
                                    new BiomeDesc("minecraft:redwood_taiga_hills"),
                                    new BiomeDesc("minecraft:mutated_redwood_taiga"),
                                    new BiomeDesc("minecraft:mutated_redwood_taiga_hills")
                            )))
                    .setOceanLevel(63)
                    .setOverrideFiller(null)
                    .setOverrideTop(null)
                    .setMaxSurfaceDepth(9)
                    .setSurfaceDepthNoiseType(NoiseType.SIMPLEX_SPONGE_NOISE)
                    .setSurfaceDepthNoiseSeed(0)
                    .setSurfaceDepthNoiseFactor(depthNoiseFactor)
                    .setSurfaceDepthNoiseOffset(depthNoiseOffset)
                    .setSurfaceDepthNoiseFrequencyX(depthNoiseFreq)
                    .setSurfaceDepthNoiseFrequencyY(0)
                    .setSurfaceDepthNoiseFrequencyZ(depthNoiseFreq)
                    .setSurfaceDepthNoiseOctaves(4)
                    .setHorizontalGradientDepthDecreaseWeight(1.0)
                    .build());

            // double depth = (defaultReplacer.getDepthNoise().get(x, 0, z) - 3) * 3;
            // if (depth > 1.75D) {
            //     defaultReplacer.setTopBlock(Blocks.STONE.getDefaultState());
            //     defaultReplacer.setFillerBlock(Blocks.STONE.getDefaultState());
            // } else if (depth > -0.5D) {
            //     defaultReplacer.setTopBlock(COARSE_DIRT);
            // }
            settings.replacers.add(new DepthBasedSurfaceReplacerConfig.Builder()
                    .setMinY(Integer.MIN_VALUE)
                    .setMaxY(Integer.MAX_VALUE)
                    .setBiomeFilter(new IncludeBiomeClass(BiomeClassMatchType.RAW_EXACT, BiomeSavannaMutated.class.getName()))
                    .setOceanLevel(63)
                    .setOverrideFiller(null)
                    .setOverrideTop(null)
                    .setMaxSurfaceDepth(9)
                    .setSurfaceDepthNoiseType(NoiseType.SIMPLEX_SPONGE_NOISE)
                    .setSurfaceDepthNoiseSeed(0)
                    .setSurfaceDepthNoiseFactor(depthNoiseFactor)
                    .setSurfaceDepthNoiseOffset(depthNoiseOffset)
                    .setSurfaceDepthNoiseFrequencyX(depthNoiseFreq)
                    .setSurfaceDepthNoiseFrequencyY(0)
                    .setSurfaceDepthNoiseFrequencyZ(depthNoiseFreq)
                    .setSurfaceDepthNoiseOctaves(4)
                    .setHorizontalGradientDepthDecreaseWeight(1.0)
                    .topThreshold(1.75 / 3.0 + 3.0, new BlockStateDesc(Blocks.STONE.getDefaultState()))
                    .fillerThreshold(1.75 / 3.0 + 3.0, new BlockStateDesc(Blocks.STONE.getDefaultState()))
                    .topThreshold(-0.5 / 3.0 + 3.0,
                            new BlockStateDesc(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)))
                    .build());

            // double depth = (defaultReplacer.getDepthNoise().get(x, 0, z) - 3) * 3;
            // if (depth > 1.75D) {
            //     defaultReplacer.setTopBlock(COARSE_DIRT);
            // } else if (depth > -0.95D) {
            //     defaultReplacer.setTopBlock(PODZOL);
            // }
            settings.replacers.add(new DepthBasedSurfaceReplacerConfig.Builder()
                    .setMinY(Integer.MIN_VALUE)
                    .setMaxY(Integer.MAX_VALUE)
                    .setBiomeFilter(new IncludeBiomes(
                            new BiomeDesc("minecraft:redwood_taiga"),
                            new BiomeDesc("minecraft:redwood_taiga_hills"),
                            new BiomeDesc("minecraft:mutated_redwood_taiga"),
                            new BiomeDesc("minecraft:mutated_redwood_taiga_hills")
                    ))
                    .setOceanLevel(63)
                    .setOverrideFiller(null)
                    .setOverrideTop(null)
                    .setMaxSurfaceDepth(9)
                    .setSurfaceDepthNoiseType(NoiseType.SIMPLEX_SPONGE_NOISE)
                    .setSurfaceDepthNoiseSeed(0)
                    .setSurfaceDepthNoiseFactor(depthNoiseFactor)
                    .setSurfaceDepthNoiseOffset(depthNoiseOffset)
                    .setSurfaceDepthNoiseFrequencyX(depthNoiseFreq)
                    .setSurfaceDepthNoiseFrequencyY(0)
                    .setSurfaceDepthNoiseFrequencyZ(depthNoiseFreq)
                    .setSurfaceDepthNoiseOctaves(4)
                    .setHorizontalGradientDepthDecreaseWeight(1.0)
                    .topThreshold(1.75 / 3.0 + 3.0,
                            new BlockStateDesc(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)))
                    .topThreshold(-0.95D / 3.0 + 3.0,
                            new BlockStateDesc(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL)))
                    .build());

            // return Sets.newHashSet(
            //         new ConfigOptionInfo(OCEAN_LEVEL, 63.0, -1024.0, 1024.0),
            //         // TODO: do it properly, currently this value is just temporary until I figure out the right one
            //         // TODO: figure out what the above comment actually means
            //         new ConfigOptionInfo(DEPTH_NOISE_FACTOR, ((1 << 3) - 1) / 3.0, -16.0, 16.0),
            //         new ConfigOptionInfo(DEPTH_NOISE_OFFSET, 3.0, -16.0, 128.0),
            //         new ConfigOptionInfo(DEPTH_NOISE_FREQUENCY, ConversionUtils.frequencyFromVanilla(0.0625f, 4), 1.0 / (1<<16), 1),
            //         new ConfigOptionInfo(DEPTH_NOISE_OCTAVES, 4.0, 1, 16),
            //         new ConfigOptionInfo(MESA_DEPTH, 16.0, 0.0, 64.0),
            //         new ConfigOptionInfo(HEIGHT_OFFSET, 64.0, -256.0, 256.0),
            //         new ConfigOptionInfo(HEIGHT_SCALE, 64.0, 0.0, 1024.0)
            // );
            settings.replacers.add(new MesaSurfaceReplacerConfig.Builder()
                    .setMinY(Integer.MIN_VALUE)
                    .setMaxY(Integer.MAX_VALUE)
                    .setBiomeFilter(new IncludeBiomeClass(BiomeClassMatchType.RAW_EXACT, BiomeMesa.class.getName()))
                    .setWaterHeight(63)
                    .setSurfaceDepthNoiseType(NoiseType.SIMPLEX_SPONGE_NOISE)
                    .setSurfaceDepthNoiseSeed(0)
                    .setSurfaceDepthNoiseFactor(depthNoiseFactor)
                    .setSurfaceDepthNoiseOffset(depthNoiseOffset)
                    .setSurfaceDepthNoiseFrequencyX(depthNoiseFreq)
                    .setSurfaceDepthNoiseFrequencyY(0)
                    .setSurfaceDepthNoiseFrequencyZ(depthNoiseFreq)
                    .setSurfaceDepthNoiseOctaves(4)
                    .setMesaDepth(16)
                    .setHeightOffset(64)
                    .setHeightScale(64)
                    .setClayBandsOffsetNoiseSource(MesaSurfaceReplacerConfig.NoiseSource.MESA_CLAY_BANDS_OFFSET_NOISE)
                    .setClayBandsNoiseFrequencyX(1.0 / 512.0)
                    .setClayBandsNoiseFrequencyY(0)
                    .setClayBandsNoiseFrequencyZ(1.0 / 512.0)
                    .setClayBandsNoiseOffset(0)
                    .setClayBandsNoiseFactor(1)
                    .build());

            // ocean. NOTE: another way to do ocean would be to use density range
            settings.replacers.add(new DensityRangeReplacerConfig.Builder()
                    .setMinY(Integer.MIN_VALUE)
                    .setMaxY(63)
                    .setBiomeFilter(null)
                    .setBlockFilterType(FilterType.INCLUDE)
                    .setFilterBlocks(Collections.singletonList(new BlockStateDesc(Blocks.AIR.getDefaultState())))
                    .setMinDensity(Double.NEGATIVE_INFINITY)
                    .setMaxDensity(Double.POSITIVE_INFINITY)
                    .setBlockInRange(new BlockStateDesc(Blocks.WATER.getDefaultState()))
                    .setBlockOutOfRange(null)
                    .build());

            // TODO: swamp: separate bottom and top density ranges
            // if (noise > 0) {
            //     return Blocks.WATER.getDefaultState();
            // }
            // if (noise > 0 && noise < 0.12) {
            //     return Blocks.WATERLILY.getDefaultState();
            // }
            settings.replacers.add(new NoiseBasedSurfaceDecorationConfig.Builder()
                    .setMinY(63)
                    .setMaxY(64)
                    .setBiomeFilter(new IncludeBiomeClass(BiomeClassMatchType.RAW_WITH_SUBCLASSES, BiomeSwamp.class.getName()))
                    .setFeatureBlock(new BlockStateDesc(Blocks.WATERLILY.getDefaultState()))
                    .setGroundBlock(new BlockStateDesc(Blocks.WATER.getDefaultState()))
                    .setGroundMinNoise(0.0)
                    .setGroundMaxNoise(Double.POSITIVE_INFINITY)
                    .setFeatureMinNoise(0.0)
                    .setFeatureMaxNoise(0.12)
                    .setNoiseSource(NoiseBasedSurfaceDecorationConfig.NoiseSource.GRASS_COLOR_NOISE)
                    .setNoiseFreqX(0.25)
                    .setNoiseFreqZ(0.25)
                    .setNoiseOffset(0.0)
                    .setNoiseFactor(1.0)
                    .build());

            settings.replacers.add(new RandomYGradientReplacerConfig.Builder()
                    .setMinY(Integer.MIN_VALUE / 2)
                    .setMaxY(Integer.MIN_VALUE / 2 + 5)
                    .setBiomeFilter(null)
                    .setBlockToPlace(new BlockStateDesc(Blocks.BEDROCK.getDefaultState()))
                    .setProbabilityFunction(UserFunction.builder()
                            .point(Integer.MIN_VALUE >> 1, 1)
                            .point((Integer.MIN_VALUE >> 1) + 5, 0)
                            .build())
                    .build());
        }
        return settings;
    }

    public static class CubeAreas {

        public final List<Map.Entry<IntAABB, CustomGeneratorSettings>> map;

        public CubeAreas(List<Map.Entry<IntAABB, CustomGeneratorSettings>> map) {
            this.map = map;
        }
    }

    public static class LakeConfig {

        public BlockDesc block;
        public Set<BiomeDesc> biomes = new HashSet<>();
        public FilterType biomeSelect = FilterType.EXCLUDE;

        public UserFunction surfaceProbability;
        public UserFunction mainProbability;
        public GenerationCondition generateWhen;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private LakeConfig config = new LakeConfig();

            public Builder setBlock(BlockDesc block) {
                config.block = block;
                return this;
            }

            public Builder setBlock(Block block) {
                config.block = new BlockDesc(block);
                return this;
            }

            public Builder setBiomes(FilterType mode, BiomeDesc... biomes) {
                config.biomes = new HashSet<>(Arrays.asList(biomes));
                config.biomeSelect = mode;
                return this;
            }

            public Builder setBiomes(FilterType mode, Biome... biomes) {
                config.biomes = Arrays.stream(biomes).map(BiomeDesc::new).collect(Collectors.toSet());
                config.biomeSelect = mode;
                return this;
            }

            public Builder setSurfaceProbability(UserFunction surfaceProbability) {
                config.surfaceProbability = surfaceProbability;
                return this;
            }

            public Builder setMainProbability(UserFunction mainProbability) {
                config.mainProbability = mainProbability;
                return this;
            }

            public Builder setGenerateWhen(GenerationCondition condition) {
                config.generateWhen = condition;
                return this;
            }

            public LakeConfig build() {
                return config;
            }

        }
    }

    public static class ReplacerConfig {
        public int minY, maxY;
        public BiomeFilter biomeFilter;

        public ReplacerConfig() {
        }
        public ReplacerConfig(int minY, int maxY, BiomeFilter biomeFilter) {
            this.minY = minY;
            this.maxY = maxY;
            this.biomeFilter = biomeFilter;
        }
    }

    public static class DensityRangeReplacerConfig extends ReplacerConfig {
        public BlockStateDesc blockInRange;
        public BlockStateDesc blockOutOfRange;
        public List<BlockStateDesc> filterBlocks = new ArrayList<>();
        public FilterType blockFilterType;
        public double minDensity;
        public double maxDensity;

        public DensityRangeReplacerConfig() {
        }

        private DensityRangeReplacerConfig(Builder builder) {
            super(builder.minY, builder.maxY, builder.biomeFilter);
            this.blockInRange = builder.blockInRange;
            this.blockOutOfRange = builder.blockOutOfRange;
            this.filterBlocks = builder.filterBlocks;
            this.blockFilterType = builder.blockFilterType;
            this.minDensity = builder.minDensity;
            this.maxDensity = builder.maxDensity;
        }

        public static class Builder {

            private int minY;
            private int maxY;
            private BiomeFilter biomeFilter;
            private BlockStateDesc blockInRange;
            private BlockStateDesc blockOutOfRange;
            private List<BlockStateDesc> filterBlocks;
            private FilterType blockFilterType;
            private double minDensity;
            private double maxDensity;

            public Builder setMinY(int minY) {
                this.minY = minY;
                return this;
            }

            public Builder setMaxY(int maxY) {
                this.maxY = maxY;
                return this;
            }

            public Builder setBiomeFilter(BiomeFilter biomeFilter) {
                this.biomeFilter = biomeFilter;
                return this;
            }

            public Builder setBlockInRange(BlockStateDesc blockInRange) {
                this.blockInRange = blockInRange;
                return this;
            }

            public Builder setBlockOutOfRange(BlockStateDesc blockOutOfRange) {
                this.blockOutOfRange = blockOutOfRange;
                return this;
            }

            public Builder setFilterBlocks(List<BlockStateDesc> filterBlocks) {
                this.filterBlocks = filterBlocks;
                return this;
            }

            public Builder setBlockFilterType(FilterType blockFilterType) {
                this.blockFilterType = blockFilterType;
                return this;
            }

            public Builder setMinDensity(double minDensity) {
                this.minDensity = minDensity;
                return this;
            }

            public Builder setMaxDensity(double maxDensity) {
                this.maxDensity = maxDensity;
                return this;
            }

            public DensityRangeReplacerConfig build() {
                return new DensityRangeReplacerConfig(this);
            }
        }
    }

    public static class RandomYGradientReplacerConfig extends ReplacerConfig {
        public BlockStateDesc blockToPlace;
        public UserFunction probabilityFunction;
        public int seed;

        public RandomYGradientReplacerConfig() {
        }

        private RandomYGradientReplacerConfig(Builder builder) {
            super(builder.minY, builder.maxY, builder.biomeFilter);
            this.blockToPlace = builder.blockToPlace;
            this.probabilityFunction = builder.probabilityFunction;
            this.seed = builder.seed;
        }

        public static class Builder {

            private int minY;
            private int maxY;
            private BiomeFilter biomeFilter;
            private BlockStateDesc blockToPlace;
            private UserFunction probabilityFunction;
            private int seed;

            public Builder setMinY(int minY) {
                this.minY = minY;
                return this;
            }

            public Builder setMaxY(int maxY) {
                this.maxY = maxY;
                return this;
            }

            public Builder setBiomeFilter(BiomeFilter biomeFilter) {
                this.biomeFilter = biomeFilter;
                return this;
            }

            public Builder setBlockToPlace(BlockStateDesc blockToPlace) {
                this.blockToPlace = blockToPlace;
                return this;
            }

            public Builder setProbabilityFunction(UserFunction probabilityFunction) {
                this.probabilityFunction = probabilityFunction;
                return this;
            }

            public Builder setSeed(int seed) {
                this.seed = seed;
                return this;
            }

            public RandomYGradientReplacerConfig build() {
                return new RandomYGradientReplacerConfig(this);
            }
        }
    }

    public static class MainSurfaceReplacerConfig extends ReplacerConfig {
        public NoiseType surfaceDepthNoiseType;
        public int surfaceDepthNoiseSeed;
        public double surfaceDepthNoiseFrequencyX;
        public double surfaceDepthNoiseFrequencyY;
        public double surfaceDepthNoiseFrequencyZ;
        public int surfaceDepthNoiseOctaves;
        public double surfaceDepthNoiseFactor;
        public double surfaceDepthNoiseOffset;
        public double maxSurfaceDepth;
        public double horizontalGradientDepthDecreaseWeight;
        public double oceanLevel;
        public BlockStateDesc overrideTop;
        public BlockStateDesc overrideFiller;

        public MainSurfaceReplacerConfig() {
        }

        public MainSurfaceReplacerConfig(int minY, int maxY, BiomeFilter biomeFilter) {
            super(minY, maxY, biomeFilter);
        }

        public MainSurfaceReplacerConfig(Builder builder) {
            super(builder.minY, builder.maxY, builder.biomeFilter);
            this.surfaceDepthNoiseType = builder.surfaceDepthNoiseType;
            this.surfaceDepthNoiseSeed = builder.surfaceDepthNoiseSeed;
            this.surfaceDepthNoiseFrequencyX = builder.surfaceDepthNoiseFrequencyX;
            this.surfaceDepthNoiseFrequencyY = builder.surfaceDepthNoiseFrequencyY;
            this.surfaceDepthNoiseFrequencyZ = builder.surfaceDepthNoiseFrequencyZ;
            this.surfaceDepthNoiseOctaves = builder.surfaceDepthNoiseOctaves;
            this.surfaceDepthNoiseFactor = builder.surfaceDepthNoiseFactor;
            this.surfaceDepthNoiseOffset = builder.surfaceDepthNoiseOffset;
            this.maxSurfaceDepth = builder.maxSurfaceDepth;
            this.horizontalGradientDepthDecreaseWeight = builder.horizontalGradientDepthDecreaseWeight;
            this.oceanLevel = builder.oceanLevel;
            this.overrideTop = builder.overrideTop;
            this.overrideFiller = builder.overrideFiller;
        }

        public static class Builder {
            private int minY;
            private int maxY;
            private BiomeFilter biomeFilter;
            private NoiseType surfaceDepthNoiseType;
            private int surfaceDepthNoiseSeed;
            private double surfaceDepthNoiseFrequencyX;
            private double surfaceDepthNoiseFrequencyY;
            private double surfaceDepthNoiseFrequencyZ;
            private int surfaceDepthNoiseOctaves;
            private double surfaceDepthNoiseFactor;
            private double surfaceDepthNoiseOffset;
            private double maxSurfaceDepth;
            private double horizontalGradientDepthDecreaseWeight;
            private double oceanLevel;
            private BlockStateDesc overrideTop;
            private BlockStateDesc overrideFiller;

            public Builder setMinY(int minY) {
                this.minY = minY;
                return this;
            }

            public Builder setMaxY(int maxY) {
                this.maxY = maxY;
                return this;
            }

            public Builder setBiomeFilter(BiomeFilter biomeFilter) {
                this.biomeFilter = biomeFilter;
                return this;
            }

            public Builder setSurfaceDepthNoiseType(NoiseType surfaceDepthNoiseType) {
                this.surfaceDepthNoiseType = surfaceDepthNoiseType;
                return this;
            }

            public Builder setSurfaceDepthNoiseSeed(int surfaceDepthNoiseSeed) {
                this.surfaceDepthNoiseSeed = surfaceDepthNoiseSeed;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyX(double surfaceDepthNoiseFrequencyX) {
                this.surfaceDepthNoiseFrequencyX = surfaceDepthNoiseFrequencyX;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyY(double surfaceDepthNoiseFrequencyY) {
                this.surfaceDepthNoiseFrequencyY = surfaceDepthNoiseFrequencyY;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyZ(double surfaceDepthNoiseFrequencyZ) {
                this.surfaceDepthNoiseFrequencyZ = surfaceDepthNoiseFrequencyZ;
                return this;
            }

            public Builder setSurfaceDepthNoiseOctaves(int surfaceDepthNoiseOctaves) {
                this.surfaceDepthNoiseOctaves = surfaceDepthNoiseOctaves;
                return this;
            }

            public Builder setSurfaceDepthNoiseFactor(double surfaceDepthNoiseFactor) {
                this.surfaceDepthNoiseFactor = surfaceDepthNoiseFactor;
                return this;
            }

            public Builder setSurfaceDepthNoiseOffset(double surfaceDepthNoiseOffset) {
                this.surfaceDepthNoiseOffset = surfaceDepthNoiseOffset;
                return this;
            }

            public Builder setMaxSurfaceDepth(double maxSurfaceDepth) {
                this.maxSurfaceDepth = maxSurfaceDepth;
                return this;
            }

            public Builder setHorizontalGradientDepthDecreaseWeight(
                    double horizontalGradientDepthDecreaseWeight) {
                this.horizontalGradientDepthDecreaseWeight = horizontalGradientDepthDecreaseWeight;
                return this;
            }

            public Builder setOceanLevel(double oceanLevel) {
                this.oceanLevel = oceanLevel;
                return this;
            }

            public Builder setOverrideTop(BlockStateDesc overrideTop) {
                this.overrideTop = overrideTop;
                return this;
            }

            public Builder setOverrideFiller(BlockStateDesc overrideFiller) {
                this.overrideFiller = overrideFiller;
                return this;
            }

            public MainSurfaceReplacerConfig build() {
                return new MainSurfaceReplacerConfig(this);
            }
        }
    }

    public static class NoiseBasedSurfaceDecorationConfig extends ReplacerConfig {

        public double surfaceDensityThreshold;
        public BlockStateDesc groundBlock;
        public BlockStateDesc featureBlock;
        public NoiseSource noiseSource;
        public double noiseFreqX;
        public double noiseFreqY;
        public double noiseFreqZ;
        public double noiseFactor;
        public double noiseOffset;
        public int customNoiseSeed;
        public int customNoiseOctaves;
        public double featureMinNoise;
        public double featureMaxNoise;
        public double groundMinNoise;
        public double groundMaxNoise;

        public enum NoiseSource {
            TEMPERATURE_NOISE,
            GRASS_COLOR_NOISE,
            FLOW_NOISE_PERLIN,
            SPONGE_NOISE_SIMPLEX
        }

        public NoiseBasedSurfaceDecorationConfig() {
        }

        public NoiseBasedSurfaceDecorationConfig(Builder builder) {
            super(builder.minY, builder.maxY, builder.biomeFilter);
            this.surfaceDensityThreshold = builder.surfaceDensityThreshold;
            this.groundBlock = builder.groundBlock;
            this.featureBlock = builder.featureBlock;
            this.noiseSource = builder.noiseSource;
            this.noiseFreqX = builder.noiseFreqX;
            this.noiseFreqY = builder.noiseFreqY;
            this.noiseFreqZ = builder.noiseFreqZ;
            this.noiseFactor = builder.noiseFactor;
            this.noiseOffset = builder.noiseOffset;
            this.customNoiseSeed = builder.customNoiseSeed;
            this.customNoiseOctaves = builder.customNoiseOctaves;
            this.featureMinNoise = builder.featureMinNoise;
            this.featureMaxNoise = builder.featureMaxNoise;
            this.groundMinNoise = builder.groundMinNoise;
            this.groundMaxNoise = builder.groundMaxNoise;
        }

        public static class Builder {

            private int minY;
            private int maxY;
            private BiomeFilter biomeFilter;
            private double surfaceDensityThreshold;
            private BlockStateDesc groundBlock;
            private BlockStateDesc featureBlock;
            private NoiseSource noiseSource;
            private double noiseFreqX;
            private double noiseFreqY;
            private double noiseFreqZ;
            private double noiseFactor;
            private double noiseOffset;
            private int customNoiseSeed;
            private int customNoiseOctaves;
            private double featureMinNoise;
            private double featureMaxNoise;
            private double groundMinNoise;
            private double groundMaxNoise;

            public Builder setMinY(int minY) {
                this.minY = minY;
                return this;
            }

            public Builder setMaxY(int maxY) {
                this.maxY = maxY;
                return this;
            }

            public Builder setBiomeFilter(BiomeFilter biomeFilter) {
                this.biomeFilter = biomeFilter;
                return this;
            }

            public Builder setSurfaceDensityThreshold(double surfaceDensityThreshold) {
                this.surfaceDensityThreshold = surfaceDensityThreshold;
                return this;
            }

            public Builder setGroundBlock(BlockStateDesc groundBlock) {
                this.groundBlock = groundBlock;
                return this;
            }

            public Builder setFeatureBlock(BlockStateDesc featureBlock) {
                this.featureBlock = featureBlock;
                return this;
            }

            public Builder setNoiseSource(NoiseSource noiseSource) {
                this.noiseSource = noiseSource;
                return this;
            }

            public Builder setNoiseFreqX(double noiseFreqX) {
                this.noiseFreqX = noiseFreqX;
                return this;
            }

            public Builder setNoiseFreqY(double noiseFreqY) {
                this.noiseFreqY = noiseFreqY;
                return this;
            }

            public Builder setNoiseFreqZ(double noiseFreqZ) {
                this.noiseFreqZ = noiseFreqZ;
                return this;
            }

            public Builder setNoiseFactor(double noiseFactor) {
                this.noiseFactor = noiseFactor;
                return this;
            }

            public Builder setNoiseOffset(double noiseOffset) {
                this.noiseOffset = noiseOffset;
                return this;
            }

            public Builder setCustomNoiseSeed(int customNoiseSeed) {
                this.customNoiseSeed = customNoiseSeed;
                return this;
            }

            public Builder setCustomNoiseOctaves(int customNoiseOctaves) {
                this.customNoiseOctaves = customNoiseOctaves;
                return this;
            }

            public Builder setFeatureMinNoise(double featureMinNoise) {
                this.featureMinNoise = featureMinNoise;
                return this;
            }

            public Builder setFeatureMaxNoise(double featureMaxNoise) {
                this.featureMaxNoise = featureMaxNoise;
                return this;
            }

            public Builder setGroundMinNoise(double groundMinNoise) {
                this.groundMinNoise = groundMinNoise;
                return this;
            }

            public Builder setGroundMaxNoise(double groundMaxNoise) {
                this.groundMaxNoise = groundMaxNoise;
                return this;
            }

            public NoiseBasedSurfaceDecorationConfig build() {
                return new NoiseBasedSurfaceDecorationConfig(this);
            }
        }
    }

    public static class MesaSurfaceReplacerConfig extends ReplacerConfig {
        public double mesaDepth;
        public double heightOffset;
        public double heightScale;
        public double waterHeight;
        public NoiseType surfaceDepthNoiseType;
        public int surfaceDepthNoiseSeed;
        public double surfaceDepthNoiseFrequencyX;
        public double surfaceDepthNoiseFrequencyY;
        public double surfaceDepthNoiseFrequencyZ;
        public int surfaceDepthNoiseOctaves;
        public double surfaceDepthNoiseFactor;
        public double surfaceDepthNoiseOffset;
        public List<BlockStateDesc> clayBandsOverride;
        public MesaSurfaceReplacerConfig.NoiseSource clayBandsOffsetNoiseSource;
        public double clayBandsNoiseFrequencyX;
        public double clayBandsNoiseFrequencyY;
        public double clayBandsNoiseFrequencyZ;
        public double clayBandsNoiseFactor;
        public double clayBandsNoiseOffset;
        public int customClayBandsNoiseSeed;
        public int customClayBandsNoiseOctaves;

        public enum NoiseSource {
            MESA_CLAY_BANDS_OFFSET_NOISE,
            MINECRAFT_PERLIN_RAW,
            FLOW_NOISE_PERLIN,
            SPONGE_NOISE_SIMPLEX
        }

        public MesaSurfaceReplacerConfig() {
        }

        public MesaSurfaceReplacerConfig(Builder builder) {
            super(builder.minY, builder.maxY, builder.biomeFilter);
            this.mesaDepth = builder.mesaDepth;
            this.heightOffset = builder.heightOffset;
            this.heightScale = builder.heightScale;
            this.waterHeight = builder.waterHeight;
            this.surfaceDepthNoiseType = builder.surfaceDepthNoiseType;
            this.surfaceDepthNoiseSeed = builder.surfaceDepthNoiseSeed;
            this.surfaceDepthNoiseFrequencyX = builder.surfaceDepthNoiseFrequencyX;
            this.surfaceDepthNoiseFrequencyY = builder.surfaceDepthNoiseFrequencyY;
            this.surfaceDepthNoiseFrequencyZ = builder.surfaceDepthNoiseFrequencyZ;
            this.surfaceDepthNoiseOctaves = builder.surfaceDepthNoiseOctaves;
            this.surfaceDepthNoiseFactor = builder.surfaceDepthNoiseFactor;
            this.surfaceDepthNoiseOffset = builder.surfaceDepthNoiseOffset;
            this.clayBandsOverride = builder.clayBandsOverride;
            this.clayBandsOffsetNoiseSource = builder.clayBandsOffsetNoiseSource;
            this.clayBandsNoiseFrequencyX = builder.clayBandsNoiseFrequencyX;
            this.clayBandsNoiseFrequencyY = builder.clayBandsNoiseFrequencyY;
            this.clayBandsNoiseFrequencyZ = builder.clayBandsNoiseFrequencyZ;
            this.clayBandsNoiseFactor = builder.clayBandsNoiseFactor;
            this.clayBandsNoiseOffset = builder.clayBandsNoiseOffset;
            this.customClayBandsNoiseSeed = builder.customClayBandsNoiseSeed;
            this.customClayBandsNoiseOctaves = builder.customClayBandsNoiseOctaves;
        }

        public static class Builder {
            private int minY;
            private int maxY;
            private BiomeFilter biomeFilter;
            private double mesaDepth;
            private double heightOffset;
            private double heightScale;
            private double waterHeight;
            private NoiseType surfaceDepthNoiseType;
            private int surfaceDepthNoiseSeed;
            private double surfaceDepthNoiseFrequencyX;
            private double surfaceDepthNoiseFrequencyY;
            private double surfaceDepthNoiseFrequencyZ;
            private int surfaceDepthNoiseOctaves;
            private double surfaceDepthNoiseFactor;
            private double surfaceDepthNoiseOffset;
            private List<BlockStateDesc> clayBandsOverride;
            private MesaSurfaceReplacerConfig.NoiseSource clayBandsOffsetNoiseSource;
            private double clayBandsNoiseFrequencyX;
            private double clayBandsNoiseFrequencyY;
            private double clayBandsNoiseFrequencyZ;
            private double clayBandsNoiseFactor;
            private double clayBandsNoiseOffset;
            private int customClayBandsNoiseSeed;
            private int customClayBandsNoiseOctaves;

            public Builder setMinY(int minY) {
                this.minY = minY;
                return this;
            }

            public Builder setMaxY(int maxY) {
                this.maxY = maxY;
                return this;
            }

            public Builder setBiomeFilter(BiomeFilter biomeFilter) {
                this.biomeFilter = biomeFilter;
                return this;
            }

            public Builder setMesaDepth(double mesaDepth) {
                this.mesaDepth = mesaDepth;
                return this;
            }

            public Builder setHeightOffset(double heightOffset) {
                this.heightOffset = heightOffset;
                return this;
            }

            public Builder setHeightScale(double heightScale) {
                this.heightScale = heightScale;
                return this;
            }

            public Builder setWaterHeight(double waterHeight) {
                this.waterHeight = waterHeight;
                return this;
            }

            public Builder setSurfaceDepthNoiseType(NoiseType surfaceDepthNoiseType) {
                this.surfaceDepthNoiseType = surfaceDepthNoiseType;
                return this;
            }

            public Builder setSurfaceDepthNoiseSeed(int surfaceDepthNoiseSeed) {
                this.surfaceDepthNoiseSeed = surfaceDepthNoiseSeed;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyX(double surfaceDepthNoiseFrequencyX) {
                this.surfaceDepthNoiseFrequencyX = surfaceDepthNoiseFrequencyX;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyY(double surfaceDepthNoiseFrequencyY) {
                this.surfaceDepthNoiseFrequencyY = surfaceDepthNoiseFrequencyY;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyZ(double surfaceDepthNoiseFrequencyZ) {
                this.surfaceDepthNoiseFrequencyZ = surfaceDepthNoiseFrequencyZ;
                return this;
            }

            public Builder setSurfaceDepthNoiseOctaves(int surfaceDepthNoiseOctaves) {
                this.surfaceDepthNoiseOctaves = surfaceDepthNoiseOctaves;
                return this;
            }

            public Builder setSurfaceDepthNoiseFactor(double surfaceDepthNoiseFactor) {
                this.surfaceDepthNoiseFactor = surfaceDepthNoiseFactor;
                return this;
            }

            public Builder setSurfaceDepthNoiseOffset(double surfaceDepthNoiseOffset) {
                this.surfaceDepthNoiseOffset = surfaceDepthNoiseOffset;
                return this;
            }

            public Builder setClayBandsOverride(List<BlockStateDesc> clayBandsOverride) {
                this.clayBandsOverride = clayBandsOverride;
                return this;
            }

            public Builder setClayBandsOffsetNoiseSource(MesaSurfaceReplacerConfig.NoiseSource clayBandsOffsetNoiseSource) {
                this.clayBandsOffsetNoiseSource = clayBandsOffsetNoiseSource;
                return this;
            }

            public Builder setClayBandsNoiseFrequencyX(double clayBandsNoiseFrequencyX) {
                this.clayBandsNoiseFrequencyX = clayBandsNoiseFrequencyX;
                return this;
            }

            public Builder setClayBandsNoiseFrequencyY(double clayBandsNoiseFrequencyY) {
                this.clayBandsNoiseFrequencyY = clayBandsNoiseFrequencyY;
                return this;
            }

            public Builder setClayBandsNoiseFrequencyZ(double clayBandsNoiseFrequencyZ) {
                this.clayBandsNoiseFrequencyZ = clayBandsNoiseFrequencyZ;
                return this;
            }

            public Builder setClayBandsNoiseFactor(double clayBandsNoiseFactor) {
                this.clayBandsNoiseFactor = clayBandsNoiseFactor;
                return this;
            }

            public Builder setClayBandsNoiseOffset(double clayBandsNoiseOffset) {
                this.clayBandsNoiseOffset = clayBandsNoiseOffset;
                return this;
            }

            public Builder setCustomClayBandsNoiseSeed(int customClayBandsNoiseSeed) {
                this.customClayBandsNoiseSeed = customClayBandsNoiseSeed;
                return this;
            }

            public Builder setCustomClayBandsNoiseOctaves(int customClayBandsNoiseOctaves) {
                this.customClayBandsNoiseOctaves = customClayBandsNoiseOctaves;
                return this;
            }

            public MesaSurfaceReplacerConfig build() {
                return new MesaSurfaceReplacerConfig(this);
            }
        }
    }

    public static class DepthBasedSurfaceReplacerConfig extends MainSurfaceReplacerConfig {
        public Set<Entry> topThresholds = new HashSet<>();
        public Set<Entry> fillerThresholds = new HashSet<>();

        public DepthBasedSurfaceReplacerConfig() {
        }

        private DepthBasedSurfaceReplacerConfig(Builder builder) {
            super(builder.minY, builder.maxY, builder.biomeFilter);
            this.surfaceDepthNoiseType = builder.surfaceDepthNoiseType;
            this.surfaceDepthNoiseSeed = builder.surfaceDepthNoiseSeed;
            this.surfaceDepthNoiseFrequencyX = builder.surfaceDepthNoiseFrequencyX;
            this.surfaceDepthNoiseFrequencyY = builder.surfaceDepthNoiseFrequencyY;
            this.surfaceDepthNoiseFrequencyZ = builder.surfaceDepthNoiseFrequencyZ;
            this.surfaceDepthNoiseOctaves = builder.surfaceDepthNoiseOctaves;
            this.surfaceDepthNoiseFactor = builder.surfaceDepthNoiseFactor;
            this.surfaceDepthNoiseOffset = builder.surfaceDepthNoiseOffset;
            this.maxSurfaceDepth = builder.maxSurfaceDepth;
            this.horizontalGradientDepthDecreaseWeight = builder.horizontalGradientDepthDecreaseWeight;
            this.oceanLevel = builder.oceanLevel;
            this.overrideTop = builder.overrideTop;
            this.overrideFiller = builder.overrideFiller;
            this.topThresholds = builder.topThresholds.entrySet().stream()
                    .map(e -> new Entry(e.getKey(), e.getValue())).collect(Collectors.toSet());
            this.fillerThresholds = builder.fillerThresholds.entrySet().stream()
                    .map(e -> new Entry(e.getKey(), e.getValue())).collect(Collectors.toSet());
        }

        public static class Entry {
            public double y;
            public BlockStateDesc b;

            public Entry() {
            }

            public Entry(double y, BlockStateDesc b) {
                this.y = y;
                this.b = b;
            }
        }

        public static class Builder {
            private int minY;
            private int maxY;
            private BiomeFilter biomeFilter;
            private NoiseType surfaceDepthNoiseType;
            private int surfaceDepthNoiseSeed;
            private double surfaceDepthNoiseFrequencyX;
            private double surfaceDepthNoiseFrequencyY;
            private double surfaceDepthNoiseFrequencyZ;
            private int surfaceDepthNoiseOctaves;
            private double surfaceDepthNoiseFactor;
            private double surfaceDepthNoiseOffset;
            private double maxSurfaceDepth;
            private double horizontalGradientDepthDecreaseWeight;
            private double oceanLevel;
            private BlockStateDesc overrideTop;
            private BlockStateDesc overrideFiller;

            private final Map<Double, BlockStateDesc> topThresholds = new HashMap<>();
            private final Map<Double, BlockStateDesc> fillerThresholds = new HashMap<>();


            public Builder setMinY(int minY) {
                this.minY = minY;
                return this;
            }

            public Builder setMaxY(int maxY) {
                this.maxY = maxY;
                return this;
            }

            public Builder setBiomeFilter(BiomeFilter biomeFilter) {
                this.biomeFilter = biomeFilter;
                return this;
            }

            public Builder setSurfaceDepthNoiseType(NoiseType surfaceDepthNoiseType) {
                this.surfaceDepthNoiseType = surfaceDepthNoiseType;
                return this;
            }

            public Builder setSurfaceDepthNoiseSeed(int surfaceDepthNoiseSeed) {
                this.surfaceDepthNoiseSeed = surfaceDepthNoiseSeed;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyX(double surfaceDepthNoiseFrequencyX) {
                this.surfaceDepthNoiseFrequencyX = surfaceDepthNoiseFrequencyX;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyY(double surfaceDepthNoiseFrequencyY) {
                this.surfaceDepthNoiseFrequencyY = surfaceDepthNoiseFrequencyY;
                return this;
            }

            public Builder setSurfaceDepthNoiseFrequencyZ(double surfaceDepthNoiseFrequencyZ) {
                this.surfaceDepthNoiseFrequencyZ = surfaceDepthNoiseFrequencyZ;
                return this;
            }

            public Builder setSurfaceDepthNoiseOctaves(int surfaceDepthNoiseOctaves) {
                this.surfaceDepthNoiseOctaves = surfaceDepthNoiseOctaves;
                return this;
            }

            public Builder setSurfaceDepthNoiseFactor(double surfaceDepthNoiseFactor) {
                this.surfaceDepthNoiseFactor = surfaceDepthNoiseFactor;
                return this;
            }

            public Builder setSurfaceDepthNoiseOffset(double surfaceDepthNoiseOffset) {
                this.surfaceDepthNoiseOffset = surfaceDepthNoiseOffset;
                return this;
            }

            public Builder setMaxSurfaceDepth(double maxSurfaceDepth) {
                this.maxSurfaceDepth = maxSurfaceDepth;
                return this;
            }

            public Builder setHorizontalGradientDepthDecreaseWeight(
                    double horizontalGradientDepthDecreaseWeight) {
                this.horizontalGradientDepthDecreaseWeight = horizontalGradientDepthDecreaseWeight;
                return this;
            }

            public Builder setOceanLevel(double oceanLevel) {
                this.oceanLevel = oceanLevel;
                return this;
            }

            public Builder setOverrideTop(BlockStateDesc overrideTop) {
                this.overrideTop = overrideTop;
                return this;
            }

            public Builder setOverrideFiller(BlockStateDesc overrideFiller) {
                this.overrideFiller = overrideFiller;
                return this;
            }

            public Builder topThreshold(double threshold, BlockStateDesc blockState) {
                this.topThresholds.put(threshold, blockState);
                return this;
            }

            public Builder fillerThreshold(double threshold, BlockStateDesc blockState) {
                this.fillerThresholds.put(threshold, blockState);
                return this;
            }

            public DepthBasedSurfaceReplacerConfig build() {
                return new DepthBasedSurfaceReplacerConfig(this);
            }
        }

    }

    public static class UserFunction {

        // TODO: flatten to float array for performance?
        public Entry[] values;

        public UserFunction() {
            values = new Entry[0];
        }

        public UserFunction(Map<Double, Double> funcMap) {
            values = funcMap.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(e -> new Entry(e.getKey(), e.getValue()))
                    .toArray(Entry[]::new);
        }

        public UserFunction(Entry[] entries) {
            this.values = entries.clone();
        }

        public double getValue(double y) {
            if (values.length == 0) {
                return 0;
            }
            if (values.length == 1) {
                return values[0].v;
            }
            Entry e1 = values[0];
            Entry e2 = values[1];
            // TODO: binary search? do we want to support functions complex enough for it to be needed? Will it improve performance?
            for (int i = 2; i < values.length; i++) {
                if (values[i - 1].y < y) {
                    e1 = e2;
                    e2 = values[i];
                }
            }
            double yFract = MathUtil.unlerp(y, e1.y, e2.y);
            return MathUtil.lerp(yFract, e1.v, e2.v);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private Map<Double, Double> map = new HashMap<>();

            public Builder point(double y, double v) {
                this.map.put(y, v);
                return this;
            }

            public UserFunction build() {
                return new UserFunction(this.map);
            }
        }

        public static class Entry {

            public double y;
            public double v;

            public Entry() {
            }

            public Entry(double key, double value) {
                this.y = key;
                this.v = value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                Entry entry = (Entry) o;
                return Double.compare(entry.y, y) == 0;
            }

            @Override
            public int hashCode() {
                return Objects.hash(y);
            }

            @Override
            public String toString() {
                return "Entry{" +
                        "y=" + y +
                        ", v=" + v +
                        '}';
            }
        }
    }

    // workaround because jankson doesn't support deserializing to generic lists and maps

    public static class StandardOreList implements Iterable<StandardOreConfig> {

        public final List<StandardOreConfig> list = new ArrayList<>();

        @Override
        public Iterator<StandardOreConfig> iterator() {
            return list.iterator();
        }
    }

    public static class PeriodicOreList implements Iterable<PeriodicGaussianOreConfig> {

        public final List<PeriodicGaussianOreConfig> list = new ArrayList<>();

        @Override
        public Iterator<PeriodicGaussianOreConfig> iterator() {
            return list.iterator();
        }
    }

    public static class StandardOreConfig {

        public BlockStateDesc blockstate;
        // null == no biome restrictions
        public Set<BiomeDesc> biomes;
        public GenerationCondition placeBlockWhen;
        public GenerationCondition generateWhen;
        public int spawnSize;
        public int spawnTries;
        public float spawnProbability = 1.0f;
        public float minHeight = Float.NEGATIVE_INFINITY;
        public float maxHeight = Float.POSITIVE_INFINITY;

        public StandardOreConfig() {
        }

        private StandardOreConfig(BlockStateDesc state, Set<BiomeDesc> biomes,
                                  GenerationCondition placeBlockWhen, GenerationCondition generateWhen,
                                  int spawnSize, int spawnTries,
                                  float spawnProbability, float minHeight, float maxHeight) {
            this.blockstate = state;
            this.biomes = biomes;
            this.placeBlockWhen = placeBlockWhen;
            this.generateWhen = generateWhen;
            this.spawnSize = spawnSize;
            this.spawnTries = spawnTries;
            this.spawnProbability = spawnProbability;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private BlockStateDesc blockstate;
            private Set<BiomeDesc> biomes = null;
            private GenerationCondition placeBlockWhen;
            private GenerationCondition generateWhen;
            private int spawnSize;
            private int spawnTries;
            private float spawnProbability;
            private float minHeight = Float.NEGATIVE_INFINITY;
            private float maxHeight = Float.POSITIVE_INFINITY;

            public Builder block(IBlockState blockstate) {
                this.blockstate = new BlockStateDesc(blockstate);
                return this;
            }

            public Builder block(BlockStateDesc blockstate) {
                this.blockstate = blockstate;
                return this;
            }

            public Builder size(int spawnSize) {
                this.spawnSize = spawnSize;
                return this;
            }

            public Builder attempts(int spawnTries) {
                this.spawnTries = spawnTries;
                return this;
            }

            public Builder probability(float spawnProbability) {
                this.spawnProbability = spawnProbability;
                return this;
            }

            public Builder minHeight(float minHeight) {
                this.minHeight = minHeight;
                return this;
            }

            public Builder maxHeight(float maxHeight) {
                this.maxHeight = maxHeight;
                return this;
            }

            /**
             * If biomes is non-null, adds the biomes to allowed biomes, if it's null - removes biome-specific generation.
             */
            public Builder biomes(@Nullable Biome... biomes) {
                if (biomes == null) {
                    this.biomes = null;
                    return this;
                }
                if (this.biomes == null) {
                    this.biomes = new HashSet<>();
                }
                for (Biome biome : biomes) {
                    this.biomes.add(new BiomeDesc(biome));
                }
                return this;
            }

            public Builder genInBlockstates(IBlockState... states) {
                if (states == null) {
                    this.placeBlockWhen = null;
                    return this;
                }
                this.placeBlockWhen = new BlockstateMatchCondition(states);
                return this;
            }

            public Builder genInBlockstates(BlockStateDesc... states) {
                if (states == null) {
                    this.placeBlockWhen = null;
                    return this;
                }
                this.placeBlockWhen = new BlockstateMatchCondition(0, 0, 0, new HashSet<>(Arrays.asList(states)));
                return this;
            }

            public StandardOreConfig create() {
                return new StandardOreConfig(blockstate, biomes, placeBlockWhen, generateWhen, spawnSize, spawnTries, spawnProbability, minHeight, maxHeight);
            }
        }
    }

    public static class PeriodicGaussianOreConfig {

        public BlockStateDesc blockstate;
        public Set<BiomeDesc> biomes = null;
        public GenerationCondition placeBlockWhen;
        public GenerationCondition generateWhen;
        public int spawnSize;
        public int spawnTries;
        public float spawnProbability;
        public float heightMean;
        public float heightStdDeviation;
        public float heightSpacing;
        public float minHeight;
        public float maxHeight;

        public PeriodicGaussianOreConfig() {
        }

        private PeriodicGaussianOreConfig(BlockStateDesc blockstate, Set<BiomeDesc> biomes,
                                          GenerationCondition placeBlockWhen, GenerationCondition generateWhen,
                                          int spawnSize, int spawnTries,
                                          float spawnProbability, float heightMean,
                                          float heightStdDeviation, float heightSpacing, float minHeight, float maxHeight) {
            this.blockstate = blockstate;
            this.biomes = biomes;
            this.placeBlockWhen = placeBlockWhen;
            this.generateWhen = generateWhen;
            this.spawnSize = spawnSize;
            this.spawnTries = spawnTries;
            this.spawnProbability = spawnProbability;
            this.heightMean = heightMean;
            this.heightStdDeviation = heightStdDeviation;
            this.heightSpacing = heightSpacing;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private BlockStateDesc blockstate;
            private Set<BiomeDesc> biomes = null;
            private GenerationCondition placeBlockWhen;
            private GenerationCondition generateWhen;
            private int spawnSize;
            private int spawnTries;
            private float spawnProbability;
            private float heightMean;
            private float heightStdDeviation;
            private float heightSpacing;
            private float minHeight = Float.NEGATIVE_INFINITY;
            private float maxHeight = Float.POSITIVE_INFINITY;

            public Builder block(IBlockState blockstate) {
                this.blockstate = new BlockStateDesc(blockstate);
                return this;
            }

            public Builder block(BlockStateDesc blockstate) {
                this.blockstate = blockstate;
                return this;
            }

            public Builder size(int spawnSize) {
                this.spawnSize = spawnSize;
                return this;
            }

            public Builder attempts(int spawnTries) {
                this.spawnTries = spawnTries;
                return this;
            }

            public Builder probability(float spawnProbability) {
                this.spawnProbability = spawnProbability;
                return this;
            }

            public Builder heightMean(float heightMean) {
                this.heightMean = heightMean;
                return this;
            }

            public Builder heightStdDeviation(float heightStdDeviation) {
                this.heightStdDeviation = heightStdDeviation;
                return this;
            }

            public Builder heightSpacing(float heightSpacing) {
                this.heightSpacing = heightSpacing;
                return this;
            }

            public Builder minHeight(float minHeight) {
                this.minHeight = minHeight;
                return this;
            }

            public Builder maxHeight(float maxHeight) {
                this.maxHeight = maxHeight;
                return this;
            }

            public Builder biomes(Biome... biomes) {
                if (biomes == null) {
                    this.biomes = null;
                    return this;
                }
                if (this.biomes == null) {
                    this.biomes = new HashSet<>();
                }
                for (Biome biome : biomes) {
                    this.biomes.add(new BiomeDesc(biome));
                }
                return this;
            }

            public Builder genInBlockstates(IBlockState... states) {
                if (states == null) {
                    this.placeBlockWhen = null;
                    return this;
                }
                this.placeBlockWhen = new BlockstateMatchCondition(states);
                return this;
            }

            public Builder genInBlockstates(BlockStateDesc... states) {
                if (states == null) {
                    this.placeBlockWhen = null;
                    return this;
                }
                this.placeBlockWhen = new BlockstateMatchCondition(0, 0, 0, new HashSet<>(Arrays.asList(states)));
                return this;
            }

            public PeriodicGaussianOreConfig create() {
                return new PeriodicGaussianOreConfig(blockstate, biomes, placeBlockWhen, generateWhen, spawnSize, spawnTries, spawnProbability, heightMean,
                        heightStdDeviation, heightSpacing, minHeight, maxHeight);
            }

        }
    }

    // we can't use vanilla StructureBoundingBox because gson serialization relies on field names to not change
    // and for vanilla classes they do change because of obfuscation
    public static class IntAABB {

        /**
         * The first x coordinate of a bounding box.
         */
        public int minX;
        /**
         * The first y coordinate of a bounding box.
         */
        public int minY;
        /**
         * The first z coordinate of a bounding box.
         */
        public int minZ;
        /**
         * The second x coordinate of a bounding box.
         */
        public int maxX;
        /**
         * The second y coordinate of a bounding box.
         */
        public int maxY;
        /**
         * The second z coordinate of a bounding box.
         */
        public int maxZ;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            IntAABB intAABB = (IntAABB) o;

            if (minX != intAABB.minX) {
                return false;
            }
            if (minY != intAABB.minY) {
                return false;
            }
            if (minZ != intAABB.minZ) {
                return false;
            }
            if (maxX != intAABB.maxX) {
                return false;
            }
            if (maxY != intAABB.maxY) {
                return false;
            }
            if (maxZ != intAABB.maxZ) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = minX;
            result = 31 * result + minY;
            result = 31 * result + minZ;
            result = 31 * result + maxX;
            result = 31 * result + maxY;
            result = 31 * result + maxZ;
            return result;
        }

        public boolean contains(int x, int y, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ && y >= minY && y <= maxY;
        }
    }

    public interface BiomeFilter extends Predicate<Biome> {
    }

    public static class IncludeBiomes implements BiomeFilter {
        public Set<BiomeDesc> biomes;

        public IncludeBiomes() {
        }

        public IncludeBiomes(BiomeDesc... biomes) {
            this.biomes = new HashSet<>(Arrays.asList(biomes));
        }

        @Override public boolean test(Biome biome) {
            return this.biomes.contains(new BiomeDesc(biome));
        }
    }

    public static class ExcludeBiomes implements BiomeFilter {
        public Set<BiomeDesc> biomes;

        public ExcludeBiomes() {
        }

        public ExcludeBiomes(BiomeDesc... biomes) {
            this.biomes = new HashSet(Arrays.asList(biomes));
        }

        @Override public boolean test(Biome biome) {
            return !this.biomes.contains(new BiomeDesc(biome));
        }
    }

    public enum BiomeClassMatchType {
        RAW_EXACT((filter, obj) -> filter == obj.getClass()),
        BIOMECLASS_EXACT((filter, obj) -> filter == obj.getBiomeClass()),
        RAW_WITH_SUBCLASSES((filter, obj) -> filter.isAssignableFrom(obj.getClass())),
        BIOMECLASS_WITH_SUBCLASSES((filter, obj) -> filter.isAssignableFrom(obj.getBiomeClass()));

        private final BiPredicate<Class<?>, Biome> test;

        BiomeClassMatchType(BiPredicate<Class<?>, Biome> test) {
            this.test = test;
        }

        public boolean test(Class<?> filter, Biome biome) {
            return this.test.test(filter, biome);
        }
    }

    public static class IncludeBiomeClass implements BiomeFilter {
        private transient List<Class<?>> classes;
        public String[] classNames;
        public BiomeClassMatchType matchType;

        public IncludeBiomeClass() {
        }

        public IncludeBiomeClass(BiomeClassMatchType matchType, String... classNames) {
            this.classNames = classNames;
            this.matchType = matchType;
            init();
        }

        public void init() {
            this.classes = new ArrayList<>(this.classNames.length);
            for (String className : this.classNames) {
                try {
                    this.classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("CustomGeneratorSettings: no class " + className);
                }
            }
        }

        @Override public boolean test(Biome biome) {
            for (Class<?> cl : this.classes) {
                if (matchType.test(cl, biome)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class ExcludeBiomeClass implements BiomeFilter {
        private transient List<Class<?>> classes;
        public String[] classNames;
        public BiomeClassMatchType matchType;

        public ExcludeBiomeClass() {
        }

        public ExcludeBiomeClass(BiomeClassMatchType matchType, String... classNames) {
            this.classNames = classNames;
            this.matchType = matchType;
            init();
        }

        public void init() {
            this.classes = new ArrayList<>(this.classNames.length);
            for (String className : this.classNames) {
                try {
                    this.classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("CustomGeneratorSettings: no class " + className);
                }
            }
        }

        @Override public boolean test(Biome biome) {
            for (Class<?> cl : this.classes) {
                if (matchType.test(cl, biome)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static abstract class CompositeBiomeFilter implements BiomeFilter {
        public List<BiomeFilter> filters;

        public List<BiomeFilter> getFilters() {
            return filters;
        }
    }

    public static class AllOfCompositeBiomeFilter extends CompositeBiomeFilter {
        public AllOfCompositeBiomeFilter() {
            this.filters = new ArrayList<>();
        }

        public AllOfCompositeBiomeFilter(List<BiomeFilter> conditions) {
            this.filters = conditions;
        }

        public AllOfCompositeBiomeFilter(BiomeFilter... conditions) {
            this.filters = Arrays.asList(conditions);
        }

        @Override
        public boolean test(Biome biome) {
            for (BiomeFilter filter : filters) {
                if (!filter.test(biome)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class AnyOfCompositeBiomeFilter extends CompositeBiomeFilter {
        public AnyOfCompositeBiomeFilter() {
            this.filters = new ArrayList<>();
        }

        public AnyOfCompositeBiomeFilter(List<BiomeFilter> conditions) {
            this.filters = conditions;
        }

        @Override
        public boolean test(Biome biome) {
            for (BiomeFilter filter : filters) {
                if (filter.test(biome)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class NoneOfCompositeBiomeFilter extends CompositeBiomeFilter {
        public NoneOfCompositeBiomeFilter() {
            this.filters = new ArrayList<>();
        }

        public NoneOfCompositeBiomeFilter(List<BiomeFilter> conditions) {
            this.filters = conditions;
        }

        @Override
        public boolean test(Biome biome) {
            for (BiomeFilter filter : filters) {
                if (filter.test(biome)) {
                    return false;
                }
            }
            return true;
        }
    }


    public interface GenerationCondition {

        boolean canGenerate(Random rand, World world, BlockPos pos);
    }

    public static class RandomCondition implements GenerationCondition {

        public double chance;

        public RandomCondition() {
        }

        public RandomCondition(double chance) {
            this.chance = chance;
        }

        @Override
        public boolean canGenerate(Random rand, World world, BlockPos pos) {
            return rand.nextFloat() < chance;
        }
    }

    public static class PosRandomCondition implements GenerationCondition {

        public double chance;

        public PosRandomCondition() {
        }

        public PosRandomCondition(double chance) {
            this.chance = chance;
        }


        @Override
        public boolean canGenerate(Random rand, World world, BlockPos pos) {
            long r = RngHash.xxHash64(world.getSeed(), 0, pos.getX(), pos.getY(), pos.getZ());
            r >>>= 24;
            r &= (1L << 24) - 1L;
            return r < chance * (1 << 24);
        }
    }

    public static class PosRandomWithSeedCondition implements GenerationCondition {

        public double chance;
        public long seed;

        public PosRandomWithSeedCondition() {
        }

        public PosRandomWithSeedCondition(double chance, long seed) {
            this.chance = chance;
            this.seed = seed;
        }

        @Override
        public boolean canGenerate(Random rand, World world, BlockPos pos) {
            long r = RngHash.xxHash64(world.getSeed(), (int) seed, pos.getX(), pos.getY(), pos.getZ());
            r >>>= 24;
            r &= (1L << 24) - 1L;
            return r < chance * (1 << 24);
        }
    }

    public static class BlockstateMatchCondition implements GenerationCondition {
        private static final Hash.Strategy<IBlockState> IDENTITY_STRATEGY = new Hash.Strategy<IBlockState>() {
            @Override public int hashCode(IBlockState o) {
                return System.identityHashCode(o);
            }

            @Override public boolean equals(IBlockState a, IBlockState b) {
                return a == b;
            }
        };
        int x, y, z;
        ObjectOpenCustomHashSet<IBlockState> allowedBlockstates = new ObjectOpenCustomHashSet<>(2, IDENTITY_STRATEGY);
        Set<BlockStateDesc> allAllowedBlockstates = new HashSet<>();;

        public BlockstateMatchCondition() {
        }

        public BlockstateMatchCondition(int x, int y, int z, Set<BlockStateDesc> allowedBlockstates) {
            this.x = x;
            this.y = y;
            this.z = z;
            for (BlockStateDesc allowedBlockstate : allowedBlockstates) {
                IBlockState blockState = allowedBlockstate.getBlockState();
                if (blockState != null) {
                    this.allowedBlockstates.add(blockState);
                }
            }
            this.allAllowedBlockstates = allowedBlockstates;
        }

        public BlockstateMatchCondition(IBlockState... states) {
            Set<BlockStateDesc> set = new HashSet<>();
            for (IBlockState state : states) {
                this.allAllowedBlockstates.add(new BlockStateDesc(state));
                this.allowedBlockstates.add(state);
            }
        }

        @Override
        public boolean canGenerate(Random rand, World world, BlockPos pos) {
            return allowedBlockstates.contains(world.getBlockState(pos.add(x, y, z)));
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public Set<BlockStateDesc> getBlockstates() {
            return allAllowedBlockstates;
        }
    }

    public static abstract class CompositeCondition implements GenerationCondition {
        List<GenerationCondition> conditions;

        public List<GenerationCondition> getConditions() {
            return conditions;
        }
    }

    public static class AllOfCompositeCondition extends CompositeCondition {
        public AllOfCompositeCondition() {
            this.conditions = new ArrayList<>();
        }

        public AllOfCompositeCondition(List<GenerationCondition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public boolean canGenerate(Random rand, World world, BlockPos pos) {
            for (GenerationCondition condition : conditions) {
                if (!condition.canGenerate(rand, world, pos)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class AnyOfCompositeCondition extends CompositeCondition {
        public AnyOfCompositeCondition() {
            this.conditions = new ArrayList<>();
        }

        public AnyOfCompositeCondition(List<GenerationCondition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public boolean canGenerate(Random rand, World world, BlockPos pos) {
            for (GenerationCondition condition : conditions) {
                if (condition.canGenerate(rand, world, pos)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class NoneOfCompositeCondition extends CompositeCondition {

        public NoneOfCompositeCondition() {
            this.conditions = new ArrayList<>();
        }

        public NoneOfCompositeCondition(List<GenerationCondition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public boolean canGenerate(Random rand, World world, BlockPos pos) {
            for (GenerationCondition condition : conditions) {
                if (condition.canGenerate(rand, world, pos)) {
                    return false;
                }
            }
            return true;
        }
    }

    public enum FilterType {
        INCLUDE, EXCLUDE;

        public <T> boolean isAllowed(Set<T> objects, T obj) {
            return (this == INCLUDE) == objects.contains(obj);
        }

        public <T> boolean isAllowed(T filter, T toTest) {
            return (this == INCLUDE) == (filter == toTest);
        }

        public boolean emptyAlwaysFails() {
            return this == INCLUDE;
        }

        public boolean emptyAlwaysMatches() {
            return this == EXCLUDE;
        }
    }

    public enum NoiseType {
        PERLIN_FLOW_NOISE,
        SIMPLEX_SPONGE_NOISE
    }
}
