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
import io.github.opencubicchunks.cubicchunks.cubicgen.XxHash;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.world.storage.IWorldInfoAccess;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.PresetLoadError;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BiomeDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
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
import java.util.stream.Collectors;

import static io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod.MODID;

public class CustomGeneratorSettings {
    /**
     * Note: many of these values are unused yet
     */

    /**
     * Vanilla standard options
     * <p>
     * Page 1
     */
    public int waterLevel = 63;
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

    public boolean lavaOceans = false;

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

    // note: the AABB uses cube coords to simplify the generator
    public CubeAreas cubeAreas = new CubeAreas(new ArrayList<>());
    public BiomeBlockReplacerConfig replacerConfig = BiomeBlockReplacerConfig.defaults();

    // TODO: public boolean negativeHeightVariationInvertsTerrain = true;
    public int version = CustomGeneratorSettingsFixer.LATEST;

    public CustomGeneratorSettings() {
    }

    public BiomeBlockReplacerConfig createBiomeBlockReplacerConfig() {
        replacerConfig.setDefault(MODID, "water_level", (double) this.waterLevel);
        replacerConfig.setDefault(MODID, "height_scale", (double) this.expectedHeightVariation);
        replacerConfig.setDefault(MODID, "height_offset", (double) this.expectedBaseHeight);
        return replacerConfig;
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
                            .setBiomes(LakeConfig.BiomeSelectionMode.EXCLUDE, new BiomeDesc[0])
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
                            .setBiomes(LakeConfig.BiomeSelectionMode.EXCLUDE, Biomes.DESERT, Biomes.DESERT_HILLS)
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
        public BiomeSelectionMode biomeSelect = BiomeSelectionMode.EXCLUDE;

        public UserFunction surfaceProbability;
        public UserFunction mainProbability;
        public GenerationCondition generateWhen;

        public static Builder builder() {
            return new Builder();
        }

        public enum BiomeSelectionMode {
            INCLUDE, EXCLUDE;

            public boolean isAllowed(Set<BiomeDesc> biomes, Biome biome) {
                return (this == INCLUDE) == biomes.contains(new BiomeDesc(biome));
            }
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

            public Builder setBiomes(BiomeSelectionMode mode, BiomeDesc... biomes) {
                config.biomes = new HashSet<>(Arrays.asList(biomes));
                config.biomeSelect = mode;
                return this;
            }

            public Builder setBiomes(BiomeSelectionMode mode, Biome... biomes) {
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

    public static class UserFunction {

        // TODO: flatten to float array for performance?
        public Entry[] values;

        public UserFunction() {
            values = new Entry[0];
        }

        public UserFunction(Map<Float, Float> funcMap) {
            values = funcMap.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(e -> new Entry(e.getKey(), e.getValue()))
                    .toArray(Entry[]::new);
        }

        public UserFunction(Entry[] entries) {
            this.values = entries.clone();
        }

        public float getValue(float y) {
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
            float yFract = MathUtil.unlerp(y, e1.y, e2.y);
            return MathUtil.lerp(yFract, e1.v, e2.v);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private Map<Float, Float> map = new HashMap<>();

            public Builder point(float y, float v) {
                this.map.put(y, v);
                return this;
            }

            public UserFunction build() {
                return new UserFunction(this.map);
            }
        }

        public static class Entry {

            public float y;
            public float v;

            public Entry() {
            }

            public Entry(float key, float value) {
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
                return Float.compare(entry.y, y) == 0;
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

    public interface GenerationCondition {
        default void beforeGenerate(Random rand) {
        }

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
            long r = XxHash.xxHash64(world.getSeed(), 0, pos.getX(), pos.getY(), pos.getZ());
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
            long r = XxHash.xxHash64(world.getSeed(), (int) seed, pos.getX(), pos.getY(), pos.getZ());
            r >>>= 24;
            r &= (1L << 24) - 1L;
            return r < chance * (1 << 24);
        }
    }

    public static class BlockstateMatchCondition implements GenerationCondition {
        int x, y, z;
        Set<IBlockState> allowedBlockstates;
        Set<BlockStateDesc> allAllowedBlockstates;

        public BlockstateMatchCondition() {
            this.allowedBlockstates = new HashSet<>();
        }

        public BlockstateMatchCondition(int x, int y, int z, Set<BlockStateDesc> allowedBlockstates) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.allowedBlockstates = allowedBlockstates.stream()
                    .map(BlockStateDesc::getBlockState)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            this.allAllowedBlockstates = allowedBlockstates;
        }

        public BlockstateMatchCondition(IBlockState... states) {
            Set<BlockStateDesc> set = new HashSet<>();
            for (IBlockState state : states) {
                set.add(new BlockStateDesc(state));
            }
            allowedBlockstates = new HashSet<>(Arrays.asList(states));
            allAllowedBlockstates = set;
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
        List<GenerationCondition> conditions;

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
        List<GenerationCondition> conditions;

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
}
