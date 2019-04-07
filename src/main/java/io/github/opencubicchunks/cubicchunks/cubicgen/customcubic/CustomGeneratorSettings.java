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

import static io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod.MODID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.cubicgen.ConversionUtils;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.BlockStateSerializer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.world.storage.IWorldInfoAccess;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

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
    public boolean waterLakes = true;

    public int waterLakeRarity = 4;
    public boolean lavaLakes = true;

    public int lavaLakeRarity = 8;
    public int aboveSeaLavaLakeRarity = 13; // approximately 10 * 4/3, all that end up above the surface are at the surface in vanilla
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

    public List<StandardOreConfig> standardOres = new ArrayList<>();

    public List<PeriodicGaussianOreConfig> periodicGaussianOres = new ArrayList<>();

    /**
     * Terrain shape
     */

    // TODO: needed until I make data fixers work correctly
    //public boolean useExpectedHeights = true;
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
    public Map<IntAABB, CustomGeneratorSettings> cubeAreas = new HashMap<>();
    public BiomeBlockReplacerConfig replacerConfig = BiomeBlockReplacerConfig.defaults();

    // TODO: public boolean negativeHeightVariationInvertsTerrain = true;
    public int version = CustomGeneratorSettingsFixer.VERSION;

    public CustomGeneratorSettings() {
    }

    public BiomeBlockReplacerConfig createBiomeBlockReplacerConfig() {
        replacerConfig.setDefault(MODID, "water_level", (double) this.waterLevel);
        replacerConfig.setDefault(MODID, "height_scale", (double) this.expectedHeightVariation);
        replacerConfig.setDefault(MODID, "height_offset", (double) this.expectedBaseHeight);
        return replacerConfig;
    }

    public String toJson() {
        Gson gson = gson();
        return gson.toJson(this);
    }
    
    public static CustomGeneratorSettings fromJson(String jsonString) {
        if (jsonString.isEmpty())
            return defaults();
        boolean isOutdated = !CustomGeneratorSettingsFixer.isUpToDate(jsonString);
        if (isOutdated) {
            jsonString = CustomGeneratorSettingsFixer.fixGeneratorOptions(jsonString, null);
        }
        return gson().fromJson(jsonString, CustomGeneratorSettings.class);
    }
    
    @Nullable
    public static String loadJsonStringFromSaveFolder(ISaveHandler saveHandler) {
        File externalGeneratorPresetFile = getPresetFile(saveHandler);
        if (externalGeneratorPresetFile.exists()) {
            try (FileReader reader = new FileReader(externalGeneratorPresetFile)){
                CharBuffer sb = CharBuffer.allocate((int) externalGeneratorPresetFile.length());
                reader.read(sb);
                sb.flip();
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public static File getPresetFolder(ISaveHandler saveHandler) {
        return new File(saveHandler.getWorldDirectory(),
                "/data/" + CustomCubicMod.MODID + "/");
    }

    public static File getPresetFile(ISaveHandler saveHandler) {
        return new File(getPresetFolder(saveHandler),
                "custom_generator_settings.json");
    }
    
    public static CustomGeneratorSettings load(World world) {
        String jsonString = world.getWorldInfo().getGeneratorOptions();
        CustomGeneratorSettings settings;
        if (jsonString.isEmpty()) {
            settings = defaults();
            IWorldInfoAccess wia = (IWorldInfoAccess) world.getWorldInfo();
            wia.setGeneratorOptions(settings.toJson());
            return settings;
        }
        boolean isOutdated = !CustomGeneratorSettingsFixer.isUpToDate(jsonString);
        if (isOutdated) {
            jsonString = CustomGeneratorSettingsFixer.fixGeneratorOptions(jsonString, null);
            IWorldInfoAccess wia = (IWorldInfoAccess) world.getWorldInfo();
            wia.setGeneratorOptions(jsonString);
        }
        Gson gson = gson();
        settings = gson.fromJson(jsonString, CustomGeneratorSettings.class);
        return settings;
    }
    
    public void save(World world) {
        saveToFile(world.getSaveHandler(), toJson());
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
            settings.standardOres.addAll(Arrays.asList(
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
            settings.periodicGaussianOres.addAll(Arrays.asList(
                    PeriodicGaussianOreConfig.builder()
                            .block(Blocks.LAPIS_ORE.getDefaultState())
                            .size(7).attempts(1).probability(0.933307775f) //resulted by approximating triangular behaviour with bell curve
                            .heightMean(-0.75f/*first belt at=16*/).heightStdDeviation(0.11231704455f/*x64 = 7.1882908513*/)
                            .heightSpacing(3.0f/*192*/)
                            .maxHeight(-0.5f).create()
            ));
        }
        return settings;
    }

    public static Gson gson() {
        return gsonBuilder().create();
    }

    public static GsonBuilder gsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.serializeSpecialFloatingPointValues();
        builder.enableComplexMapKeySerialization();
        builder.registerTypeAdapter(CustomGeneratorSettings.class, new Serializer());
        builder.registerTypeHierarchyAdapter(IBlockState.class, BlockStateSerializer.INSTANCE);
        builder.registerTypeHierarchyAdapter(Biome.class, new BiomeSerializer());
        builder.registerTypeAdapter(BiomeBlockReplacerConfig.class, new BiomeBlockReplacerConfigSerializer());
        return builder;
    }

    public static class StandardOreConfig {

        public IBlockState blockstate;
        // null == no biome restrictions
        public Set<Biome> biomes;
        public Set<IBlockState> genInBlockstates;
        public int spawnSize;
        public int spawnTries;
        public float spawnProbability = 1.0f;
        public float minHeight = Float.NEGATIVE_INFINITY;
        public float maxHeight = Float.POSITIVE_INFINITY;

        private StandardOreConfig(IBlockState state, Set<Biome> biomes, Set<IBlockState> genInBlockstates, int spawnSize, int spawnTries,
                float spawnProbability, float minHeight, float maxHeight) {
            this.blockstate = state;
            this.biomes = biomes;
            this.genInBlockstates = genInBlockstates;
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

            private IBlockState blockstate;
            private Set<Biome> biomes = null;
            private Set<IBlockState> genInBlockstates;
            private int spawnSize;
            private int spawnTries;
            private float spawnProbability;
            private float minHeight = Float.NEGATIVE_INFINITY;
            private float maxHeight = Float.POSITIVE_INFINITY;

            public Builder block(IBlockState blockstate) {
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
                this.biomes.addAll(Arrays.asList(biomes));
                return this;
            }

            public Builder genInBlockstates(IBlockState... states) {
                if (states == null) {
                    this.genInBlockstates = null;
                    return this;
                }
                if (this.genInBlockstates == null) {
                    this.genInBlockstates = new HashSet<>();
                }
                this.genInBlockstates.addAll(Arrays.asList(states));
                return this;
            }


            public Builder fromPeriodic(PeriodicGaussianOreConfig config) {
                return minHeight(config.minHeight)
                        .maxHeight(config.maxHeight)
                        .probability(config.spawnProbability)
                        .size(config.spawnSize)
                        .attempts(config.spawnTries)
                        .block(config.blockstate)
                        .biomes(config.biomes == null ? null : config.biomes.toArray(new Biome[0]))
                        .genInBlockstates(config.genInBlockstates == null ? null : config.genInBlockstates.toArray(new IBlockState[0]));
            }
            public StandardOreConfig create() {
                return new StandardOreConfig(blockstate, biomes, genInBlockstates, spawnSize, spawnTries, spawnProbability, minHeight, maxHeight);
            }
        }
    }

    public static class PeriodicGaussianOreConfig {

        public IBlockState blockstate;
        public Set<Biome> biomes = null;
        public Set<IBlockState> genInBlockstates; // unspecified = vanilla defaults
        public int spawnSize;
        public int spawnTries;
        public float spawnProbability;
        public float heightMean;
        public float heightStdDeviation;
        public float heightSpacing;
        public float minHeight;
        public float maxHeight;

        private PeriodicGaussianOreConfig(IBlockState blockstate, Set<Biome> biomes, Set<IBlockState> genInBlockstates, int spawnSize, int spawnTries,
                float spawnProbability, float heightMean, float heightStdDeviation, float heightSpacing, float minHeight, float maxHeight) {
            this.blockstate = blockstate;
            this.biomes = biomes;
            this.genInBlockstates = genInBlockstates;
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

            private IBlockState blockstate;
            private Set<Biome> biomes = null;
            private Set<IBlockState> genInBlockstates = null;
            private int spawnSize;
            private int spawnTries;
            private float spawnProbability;
            private float heightMean;
            private float heightStdDeviation;
            private float heightSpacing;
            private float minHeight = Float.NEGATIVE_INFINITY;
            private float maxHeight = Float.POSITIVE_INFINITY;

            public Builder block(IBlockState blockstate) {
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
                this.biomes.addAll(Arrays.asList(biomes));
                return this;
            }

            public Builder genInBlockstates(IBlockState... states) {
                if (states == null) {
                    this.genInBlockstates = null;
                    return this;
                }
                if (this.genInBlockstates == null) {
                    this.genInBlockstates = new HashSet<>();
                }
                this.genInBlockstates.addAll(Arrays.asList(states));
                return this;
            }

            public Builder fromStandard(StandardOreConfig config) {
                return minHeight(config.minHeight)
                        .maxHeight(config.maxHeight)
                        .probability(config.spawnProbability)
                        .size(config.spawnSize)
                        .attempts(config.spawnTries)
                        .block(config.blockstate)
                        .biomes(config.biomes == null ? null : config.biomes.toArray(new Biome[0]))
                        .genInBlockstates(config.genInBlockstates == null ? null : config.genInBlockstates.toArray(new IBlockState[0]))
                        .heightMean(0)
                        .heightStdDeviation(1)
                        .heightSpacing(2);
            }

            public PeriodicGaussianOreConfig create() {
                return new PeriodicGaussianOreConfig(blockstate, biomes, genInBlockstates, spawnSize, spawnTries, spawnProbability, heightMean,
                        heightStdDeviation, heightSpacing, minHeight, maxHeight);
            }

        }
    }


    private static class Serializer implements JsonDeserializer<CustomGeneratorSettings>, JsonSerializer<CustomGeneratorSettings> {

        private static final Field[] fields = CustomGeneratorSettings.class.getFields();

        static {
            for (Field f : fields) {
                f.setAccessible(true);
            }
        }

        private static final CustomGeneratorSettings defaults = CustomGeneratorSettings.defaults();

        @Override public CustomGeneratorSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return deserializeWithDefault(json, defaults, context);
        }

        private CustomGeneratorSettings deserializeWithDefault(JsonElement element, CustomGeneratorSettings def, JsonDeserializationContext ctx) {
            try {
                JsonObject root = element.getAsJsonObject();
                CustomGeneratorSettings ret = new CustomGeneratorSettings();

                for (Field field : fields) {
                    if (!field.getName().equals("cubeAreas")) {
                        if (root.has(field.getName())) {
                            JsonElement e = root.get(field.getName());
                            field.set(ret, ctx.deserialize(e, field.getGenericType()));
                        } else {
                            field.set(ret, field.get(def));
                        }
                    }
                }

                Map<IntAABB, CustomGeneratorSettings> map = new HashMap<>();
                // do cubeAreas once everything else is done so we have all the defaults
                if (root.has("cubeAreas") && root.get("cubeAreas").isJsonArray()) {
                    JsonArray array = root.get("cubeAreas").getAsJsonArray();
                    for (JsonElement entry : array) {
                        JsonArray mapEntry = entry.getAsJsonArray();
                        JsonElement key = mapEntry.get(0);
                        JsonElement value = mapEntry.get(1);
                        IntAABB keyValue = ctx.deserialize(key, IntAABB.class);
                        CustomGeneratorSettings valueValue = deserializeWithDefault(value, ret, ctx);
                        map.put(keyValue, valueValue);
                    }
                }
                ret.cubeAreas = map;
                return ret;
            } catch (IllegalAccessException e) {
                // everything should be made accessible in static initializer
                throw new Error(e);
            } catch (RuntimeException e) {
                throw new JsonParseException(e);
            }
        }

        @Override public JsonElement serialize(CustomGeneratorSettings src, Type typeOfSrc, JsonSerializationContext context) {
            return serializeWithDefault(src, defaults, context);
        }

        private JsonElement serializeWithDefault(CustomGeneratorSettings src, CustomGeneratorSettings def, JsonSerializationContext ctx) {
            try {
                JsonObject root = new JsonObject();
                for (Field field : fields) {
                    Object value = field.get(src);
                    if (field.getName().equals("cubeAreas")) {
                        JsonArray cubeAreas = new JsonArray();
                        for (Map.Entry<IntAABB, CustomGeneratorSettings> entry : src.cubeAreas.entrySet()) {
                            JsonArray arrEntry = new JsonArray();
                            arrEntry.add(ctx.serialize(entry.getKey()));
                            // use current src as default
                            arrEntry.add(serializeWithDefault(entry.getValue(), src, ctx));
                            cubeAreas.add(arrEntry);
                        }
                        root.add("cubeAreas", cubeAreas);
                    } else {
                        root.add(field.getName(), ctx.serialize(value));
                    }
                }
                return root;
            } catch (IllegalAccessException e) {
                // everything should be made accessible in static initializer
                throw new Error(e);
            }
        }
    }

    private static class BiomeBlockReplacerConfigSerializer
            implements JsonDeserializer<BiomeBlockReplacerConfig>, JsonSerializer<BiomeBlockReplacerConfig> {

        @Override public BiomeBlockReplacerConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject defaults = json.getAsJsonObject().get("defaults").getAsJsonObject();
            JsonObject overrides = json.getAsJsonObject().get("overrides").getAsJsonObject();

            BiomeBlockReplacerConfig conf = BiomeBlockReplacerConfig.defaults();
            for (Map.Entry<String, JsonElement> e : defaults.entrySet()) {
                ResourceLocation key = new ResourceLocation(e.getKey());
                Object value = getObject(context, e);
                conf.setDefault(key, value);
            }
            for (Map.Entry<String, JsonElement> e : overrides.entrySet()) {
                ResourceLocation key = new ResourceLocation(e.getKey());
                Object value = getObject(context, e);
                conf.set(key, value);
            }
            return conf;
        }

        private Object getObject(JsonDeserializationContext context, Map.Entry<String, JsonElement> e) {
            Object value;
            if (e.getValue().isJsonPrimitive()) {
                value = e.getValue().getAsJsonPrimitive().getAsDouble();
            } else {
                // currently the only object suppoorted is blockstate
                value = BlockStateSerializer.INSTANCE.deserialize(e.getValue(), IBlockState.class, context);
            }
            return value;
        }

        @Override public JsonElement serialize(BiomeBlockReplacerConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject root = new JsonObject();

            JsonObject defaults = new JsonObject();
            JsonObject overrides = new JsonObject();

            for (Map.Entry<ResourceLocation, Object> e : src.getDefaults().entrySet()) {
                defaults.add(e.getKey().toString(), getJsonElement(context, e));
            }
            for (Map.Entry<ResourceLocation, Object> e : src.getOverrides().entrySet()) {
                overrides.add(e.getKey().toString(), getJsonElement(context, e));
            }
            root.add("defaults", defaults);
            root.add("overrides", overrides);
            return root;
        }

        private JsonElement getJsonElement(JsonSerializationContext context, Map.Entry<ResourceLocation, Object> e) {
            JsonElement v;
            if (e.getValue() == null) {
                throw new NullPointerException("Null config entries cannot be serialized");
            }
            if (e.getValue() instanceof Number) {
                v = new JsonPrimitive((Number) e.getValue());
            } else if (e.getValue() instanceof IBlockState) {
                v = BlockStateSerializer.INSTANCE.serialize((IBlockState) e.getValue(), IBlockState.class, context);
            } else {
                throw new UnsupportedOperationException(e.getValue() + " of type " + e.getValue().getClass() + " is not supported");
            }
            return v;
        }
    }

    private static class BiomeSerializer implements JsonDeserializer<Biome>, JsonSerializer<Biome> {

        @Override public Biome deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return ForgeRegistries.BIOMES.getValue(new ResourceLocation(json.getAsString()));
        }

        @Override public JsonElement serialize(Biome src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getRegistryName().toString());
        }
    }

    // we can't use vanilla StructureBoundingBox because gson serialization relies on field names to not change
    // and for vanilla classes they do change because of obfuscation
    public static class IntAABB {

        /** The first x coordinate of a bounding box. */
        public int minX;
        /** The first y coordinate of a bounding box. */
        public int minY;
        /** The first z coordinate of a bounding box. */
        public int minZ;
        /** The second x coordinate of a bounding box. */
        public int maxX;
        /** The second y coordinate of a bounding box. */
        public int maxY;
        /** The second z coordinate of a bounding box. */
        public int maxZ;

        public IntAABB() {
        }

        public IntAABB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        @Override public boolean equals(Object o) {
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

        @Override public int hashCode() {
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
}
