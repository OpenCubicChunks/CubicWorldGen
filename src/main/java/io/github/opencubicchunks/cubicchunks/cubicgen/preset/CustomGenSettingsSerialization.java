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
package io.github.opencubicchunks.cubicchunks.cubicgen.preset;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.Marshaller;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.CubeAreas;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.IntAABB;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.PeriodicGaussianOreConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.PeriodicOreList;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.StandardOreConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.StandardOreList;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.UserFunction;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BiomeDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockDesc;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.wrapper.BlockStateDesc;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class CustomGenSettingsSerialization {

    public static final JsonGrammar OUT_GRAMMAR = JsonGrammar.builder()
            .withComments(true)
            .bareSpecialNumerics(true)
            .printWhitespace(true)
            .printUnquotedKeys(false) // change when https://github.com/falkreon/Jankson/issues/38 is fixed
            .printCommas(true)
            .bareRootObject(false)
            .build();

    public static final Marshaller MARSHALLER = jankson().getMarshaller();

    private static CustomGeneratorSettings.ReplacerConfig deserializeReplacerConfig(JsonObject obj, Marshaller marshaller) throws DeserializationException {
        String type = obj.get(String.class, "type");
        JsonObject withoutType = obj.clone();
        withoutType.remove("type");
        switch (Objects.requireNonNull(type)) {
            case "densityRange":
                return marshaller.marshallCarefully(CustomGeneratorSettings.DensityRangeReplacerConfig.class, withoutType);
            case "mainSurface":
                return marshaller.marshallCarefully(CustomGeneratorSettings.MainSurfaceReplacerConfig.class, withoutType);
            case "mesaSurface":
                return marshaller.marshallCarefully(CustomGeneratorSettings.MesaSurfaceReplacerConfig.class, withoutType);
            case "noiseBasedSurfaceDecoration":
                return marshaller.marshallCarefully(CustomGeneratorSettings.NoiseBasedSurfaceDecorationConfig.class, withoutType);
            case "randomYGradient":
                return marshaller.marshallCarefully(CustomGeneratorSettings.RandomYGradientReplacerConfig.class, withoutType);
            case "depthBasedSurface":
                return marshaller.marshallCarefully(CustomGeneratorSettings.DepthBasedSurfaceReplacerConfig.class, withoutType);
            default:
                throw new DeserializationException("Unknown type: " + type);
        }
    }

    private static JsonObject serializeReplacerConfig(CustomGeneratorSettings.ReplacerConfig src, Marshaller marshaller) {
        JsonObject root = new JsonObject();
        root.setMarshaller(marshaller);
        String type;
        if (src.getClass() == CustomGeneratorSettings.DensityRangeReplacerConfig.class) {
            type = "densityRange";
        } else if (src.getClass() == CustomGeneratorSettings.MainSurfaceReplacerConfig.class) {
            type = "mainSurface";
        } else if (src.getClass() == CustomGeneratorSettings.MesaSurfaceReplacerConfig.class) {
            type = "mesaSurface";
        } else if (src.getClass() == CustomGeneratorSettings.NoiseBasedSurfaceDecorationConfig.class) {
            type = "noiseBasedSurfaceDecoration";
        } else if (src.getClass() == CustomGeneratorSettings.RandomYGradientReplacerConfig.class) {
            type = "randomYGradient";
        } else if (src.getClass() == CustomGeneratorSettings.DepthBasedSurfaceReplacerConfig.class) {
            type = "depthBasedSurface";
        } else {
            throw new RuntimeException("Unknown type " + src.getClass());
        }
        root.put("type", new JsonPrimitive(type));

        for (Field field : src.getClass().getFields()) {
            try {
                root.put(field.getName(), marshaller.serialize(field.get(src)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return root;
    }
    private static BiomeDesc deserializeBiome(String obj, Marshaller marshaller) {
        return new BiomeDesc(obj);
    }

    private static JsonElement serializeBiome(BiomeDesc biome, Marshaller marshaller) {
        return new JsonPrimitive(biome.getBiomeId());
    }

    public static UserFunction deserializeUserFunction(JsonArray arr, Marshaller marshaller) {
        UserFunction.Entry[] entries = new UserFunction.Entry[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            JsonObject e = (JsonObject) arr.get(i);
            entries[i] = new UserFunction.Entry(e.get(float.class, "y"), e.get(float.class, "v"));
        }
        return new UserFunction(entries);
    }

    public static JsonElement serializeUserFunction(UserFunction func, Marshaller marshaller) {
        JsonArray arr = new JsonArray();
        for (UserFunction.Entry value : func.values) {
            JsonObject e = new JsonObject();
            e.put("y", new JsonPrimitive(value.y));
            e.put("v", new JsonPrimitive(value.v));
            arr.add(e);
        }
        return arr;
    }

    private static BlockDesc deserializeBlock(String obj, Marshaller marshaller) {
        return new BlockDesc(obj);
    }

    private static JsonElement serializeBlock(BlockDesc block, Marshaller m) {
        return new JsonPrimitive(block.getBlockId());
    }

    public static BlockStateDesc deserializeBlockstate(JsonElement element, Marshaller marshaller) {
        if (element instanceof JsonPrimitive) {
            return new BlockStateDesc(((JsonPrimitive) element).asString(), new HashMap<>());
        }
        JsonObject obj = (JsonObject) element;
        String name = obj.get(String.class, "Name");
        Map<String, String> properties = new HashMap<>();
        if (obj.containsKey("Properties")) {
            JsonObject propertiesObj = obj.get(JsonObject.class, "Properties");
            for (Map.Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                properties.put(entry.getKey(), ((JsonPrimitive) entry.getValue()).asString());
            }
        }
        return new BlockStateDesc(name, properties);
    }

    public static JsonElement serializeBlockstate(BlockStateDesc state, Marshaller m) {
        JsonObject json = new JsonObject();
        ResourceLocation blockRL = new ResourceLocation(state.getBlockId());
        String blockId = blockRL.getNamespace().equals("minecraft") ? blockRL.getPath() : blockRL.toString();
        boolean hasProperties = !state.getProperties().isEmpty() &&
                (state.getBlockState() == null || state.getBlockState() != state.getBlockState().getBlock().getDefaultState());
        if (!hasProperties) {
            return new JsonPrimitive(blockId);
        }
        Map<String, String> defaultProperties = state.getBlockState() == null ? new HashMap<>() :
                new BlockStateDesc(state.getBlockState().getBlock().getDefaultState()).getProperties();
        json.put("Name", new JsonPrimitive(blockId));

        if (!state.getProperties().isEmpty()) {
            JsonObject properties = new JsonObject();
            state.getProperties().forEach((name, value) -> {
                if (!value.equals(defaultProperties.get(name))) {
                    properties.put(name, new JsonPrimitive(value));
                }
            });
            json.put("Properties", properties);
        }

        return json;
    }

    private static CubeAreas deserializeCubeAreas(JsonArray arr, Marshaller marshaller) throws DeserializationException {
        List<Map.Entry<IntAABB, CustomGeneratorSettings>> map = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonArray entry = (JsonArray) arr.get(i);
            JsonObject key = (JsonObject) entry.get(0);
            JsonObject value = (JsonObject) entry.get(1);

            IntAABB aabb = marshaller.marshallCarefully(IntAABB.class, key);
            CustomGeneratorSettings conf = marshaller.marshallCarefully(CustomGeneratorSettings.class, value);
            map.add(new AbstractMap.SimpleEntry<>(aabb, conf));
        }
        return new CubeAreas(map);
    }

    private static JsonElement serializeCubeAreas(CubeAreas areas, Marshaller marshaller) {
        JsonArray json = new JsonArray();
        json.setMarshaller(marshaller);
        for (Map.Entry<IntAABB, CustomGeneratorSettings> entry : areas.map) {
            JsonObject aabb = (JsonObject) marshaller.serialize(entry.getKey());
            aabb.setMarshaller(marshaller);
            JsonObject conf = (JsonObject) marshaller.serialize(entry.getValue());
            conf.setMarshaller(marshaller);

            JsonArray layer = new JsonArray();
            layer.setMarshaller(marshaller);
            layer.add(aabb);
            layer.add(conf);

            json.add(layer);
        }
        return json;
    }

    private static TreeMap<Integer, FlatLayer> deserializeFlatCubicLayers(JsonObject obj, Marshaller marshaller) throws DeserializationException {
        TreeMap<Integer, FlatLayer> map = new TreeMap<>();

        for (String yStr : obj.keySet()) {
            JsonObject value = (JsonObject) obj.get(yStr);
            int y = Integer.parseInt(yStr);
            FlatLayer layer = marshaller.marshallCarefully(FlatLayer.class, value);
            map.put(y, layer);
        }
        return map;
    }

    private static JsonElement serializeCubeFlatCubicLayers(TreeMap<Integer, FlatLayer> map, Marshaller marshaller) {
        JsonObject json = new JsonObject();
        for (Map.Entry<Integer, FlatLayer> entry : map.entrySet()) {
            json.put(entry.getKey().toString(), marshaller.serialize(entry.getValue()));
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    private static FlatGeneratorSettings deserializeFlatGenSettings(JsonObject obj, Marshaller marshaller) throws DeserializationException {
        FlatGeneratorSettings gen = new FlatGeneratorSettings();
        gen.layers = marshaller.marshallCarefully(TreeMap.class, obj.get("layers"));
        gen.version = obj.getInt("version", 0);
        return gen;
    }

    private static JsonObject serializeFlatGenSettings(FlatGeneratorSettings t, Marshaller marshaller) {
        JsonObject layers = (JsonObject) marshaller.serialize(t.layers);
        JsonObject obj = new JsonObject();
        obj.put("layers", layers);
        obj.put("version", new JsonPrimitive(t.version));
        return obj;
    }

    private static StandardOreList deserializeStandardOreList(JsonArray obj, Marshaller marshaller) throws DeserializationException {
        StandardOreList ores = new StandardOreList();
        for (JsonElement element : obj) {
            ores.list.add(marshaller.marshallCarefully(StandardOreConfig.class, element));
        }
        return ores;
    }

    private static JsonArray serializeStandardOreList(StandardOreList ores, Marshaller marshaller) {
        JsonArray json = new JsonArray();
        for (StandardOreConfig ore : ores.list) {
            json.add(marshaller.serialize(ore));
        }
        return json;
    }

    private static PeriodicOreList deserializePeriodicOreList(JsonArray obj, Marshaller marshaller) throws DeserializationException {
        PeriodicOreList ores = new PeriodicOreList();
        for (JsonElement element : obj) {
            ores.list.add(marshaller.marshallCarefully(PeriodicGaussianOreConfig.class, element));
        }
        return ores;
    }

    private static JsonArray serializePeriodicOreList(PeriodicOreList ores, Marshaller marshaller) {
        JsonArray json = new JsonArray();
        for (PeriodicGaussianOreConfig ore : ores.list) {
            json.add(marshaller.serialize(ore));
        }
        return json;
    }

    private static CustomGeneratorSettings.GenerationCondition deserializeGenerationCondition(JsonObject obj, Marshaller marshaller) throws DeserializationException {
        if (obj.containsKey("anyOf")) {
            JsonArray conditions = (JsonArray) obj.get("anyOf");
            List<CustomGeneratorSettings.GenerationCondition> conditionList = new ArrayList<>();
            for (JsonElement element : conditions) {
                conditionList.add(marshaller.marshallCarefully(CustomGeneratorSettings.GenerationCondition.class, element));
            }
            return new CustomGeneratorSettings.AnyOfCompositeCondition(conditionList);
        } else if (obj.containsKey("allOf")) {
            JsonArray conditions = (JsonArray) obj.get("allOf");
            List<CustomGeneratorSettings.GenerationCondition> conditionList = new ArrayList<>();
            for (JsonElement element : conditions) {
                conditionList.add(marshaller.marshallCarefully(CustomGeneratorSettings.GenerationCondition.class, element));
            }
            return new CustomGeneratorSettings.AllOfCompositeCondition(conditionList);
        } else if (obj.containsKey("noneOf")) {
            JsonArray conditions = (JsonArray) obj.get("noneOf");
            List<CustomGeneratorSettings.GenerationCondition> conditionList = new ArrayList<>();
            for (JsonElement element : conditions) {
                conditionList.add(marshaller.marshallCarefully(CustomGeneratorSettings.GenerationCondition.class, element));
            }
            return new CustomGeneratorSettings.NoneOfCompositeCondition(conditionList);
        } else if (obj.containsKey("rand")) {
            Object chance = ((JsonPrimitive) obj.get("rand")).getValue();
            return new CustomGeneratorSettings.RandomCondition(((Number) chance).doubleValue());
        } else if (obj.containsKey("posRand")) {
            Object chance = ((JsonPrimitive) obj.get("posRand")).getValue();
            return new CustomGeneratorSettings.PosRandomCondition(((Number) chance).doubleValue());
        } else if (obj.containsKey("posRandSeed")) {
            Object chance = ((JsonPrimitive) obj.get("posRandSeed")).getValue();
            Object seed = ((JsonPrimitive) obj.get("seed")).getValue();
            return new CustomGeneratorSettings.PosRandomWithSeedCondition(((Number) chance).doubleValue(),
                    ((Number) seed).longValue());
        } else {
            int x = obj.getInt("x", 0);
            int y = obj.getInt("y", 0);
            int z = obj.getInt("z", 0);
            Set<BlockStateDesc> blockstates = new HashSet<>();
            JsonElement blocks = obj.get("blocks");
            if (blocks instanceof JsonObject) {
                blockstates.add(marshaller.marshallCarefully(BlockStateDesc.class, blocks));
            } else {
                JsonArray blockArray = (JsonArray) blocks;
                for (JsonElement jsonElement : blockArray) {
                    blockstates.add(marshaller.marshallCarefully(BlockStateDesc.class, jsonElement));
                }
            }
            return new CustomGeneratorSettings.BlockstateMatchCondition(x, y, z, blockstates);
        }
    }

    private static JsonObject serializeGenerationCondition(CustomGeneratorSettings.GenerationCondition value, Marshaller marshaller) {
        if (value instanceof CustomGeneratorSettings.BlockstateMatchCondition) {
            CustomGeneratorSettings.BlockstateMatchCondition c = (CustomGeneratorSettings.BlockstateMatchCondition) value;
            JsonPrimitive x = new JsonPrimitive(c.getX());
            JsonPrimitive y = new JsonPrimitive(c.getY());
            JsonPrimitive z = new JsonPrimitive(c.getZ());
            JsonElement blocks;
            Set<BlockStateDesc> blockstates = c.getBlockstates();
            if (blockstates.size() == 1) {
                blocks = marshaller.serialize(blockstates.iterator().next());
            } else {
                JsonArray arr = new JsonArray();
                for (BlockStateDesc blockstate : blockstates) {
                    arr.add(marshaller.serialize(blockstate));
                }
                blocks = arr;
            }
            JsonObject obj = new JsonObject();
            obj.put("x", x);
            obj.put("y", y);
            obj.put("z", z);
            obj.put("blocks", blocks);
            return obj;
        } else if (value instanceof CustomGeneratorSettings.RandomCondition) {
            JsonObject obj = new JsonObject();
            obj.put("rand", new JsonPrimitive(((CustomGeneratorSettings.RandomCondition) value).chance));
            return obj;
        } else if (value instanceof CustomGeneratorSettings.PosRandomCondition) {
            JsonObject obj = new JsonObject();
            obj.put("posRand", new JsonPrimitive(((CustomGeneratorSettings.PosRandomCondition) value).chance));
            return obj;
        } else if (value instanceof CustomGeneratorSettings.PosRandomWithSeedCondition) {
            JsonObject obj = new JsonObject();
            obj.put("posRandSeed", new JsonPrimitive(((CustomGeneratorSettings.PosRandomWithSeedCondition) value).chance));
            obj.put("seed", new JsonPrimitive(((CustomGeneratorSettings.PosRandomWithSeedCondition) value).seed));
            return obj;
        } else {
            String name;
            if (value instanceof CustomGeneratorSettings.AnyOfCompositeCondition) {
                name = "anyOf";
            } else if (value instanceof CustomGeneratorSettings.AllOfCompositeCondition) {
                name = "allOf";
            } else if (value instanceof CustomGeneratorSettings.NoneOfCompositeCondition) {
                name = "noneOf";
            } else {
                throw new IllegalArgumentException("Unknown condition type " + value);
            }
            List<CustomGeneratorSettings.GenerationCondition> conditions = ((CustomGeneratorSettings.CompositeCondition) value).getConditions();
            JsonObject obj = new JsonObject();

            JsonArray conditionArray = new JsonArray();
            for (CustomGeneratorSettings.GenerationCondition condition : conditions) {
                conditionArray.add(marshaller.serialize(condition));
            }
            obj.put(name, conditionArray);
            return obj;
        }
    }

    private static CustomGeneratorSettings.BiomeFilter deserializeBiomeFilter(JsonObject obj, Marshaller marshaller) throws DeserializationException {
        String type = obj.get(String.class, "type");
        JsonObject withoutType = obj.clone();
        withoutType.remove("type");
        switch (Objects.requireNonNull(type)) {
            case "include":
                return marshaller.marshallCarefully(CustomGeneratorSettings.IncludeBiomes.class, withoutType);
            case "exclude":
                return marshaller.marshallCarefully(CustomGeneratorSettings.ExcludeBiomes.class, withoutType);
            case "includeClass":
                CustomGeneratorSettings.IncludeBiomeClass includeBiomeClass =
                        marshaller.marshallCarefully(CustomGeneratorSettings.IncludeBiomeClass.class, withoutType);
                includeBiomeClass.init();
                return includeBiomeClass;
            case "excludeClass":
                CustomGeneratorSettings.ExcludeBiomeClass excludeBiomeClass =
                        marshaller.marshallCarefully(CustomGeneratorSettings.ExcludeBiomeClass.class, withoutType);
                excludeBiomeClass.init();
                return excludeBiomeClass;
            case "allOf":
                return marshaller.marshallCarefully(CustomGeneratorSettings.AllOfCompositeBiomeFilter.class, withoutType);
            case "anyOf":
                return marshaller.marshallCarefully(CustomGeneratorSettings.AnyOfCompositeBiomeFilter.class, withoutType);
            case "noneOf":
                return marshaller.marshallCarefully(CustomGeneratorSettings.NoneOfCompositeBiomeFilter.class, withoutType);
            default:
                throw new DeserializationException("Unknown type: " + type);
        }
    }

    private static JsonObject serializeBiomeFilter(CustomGeneratorSettings.BiomeFilter src, Marshaller marshaller) {
        JsonObject root = new JsonObject();
        root.setMarshaller(marshaller);
        String type;
        if (src instanceof CustomGeneratorSettings.IncludeBiomes) {
            type = "include";
        } else if (src instanceof CustomGeneratorSettings.ExcludeBiomes) {
            type = "exclude";
        } else if (src instanceof CustomGeneratorSettings.IncludeBiomeClass) {
            type = "includeClass";
        } else if (src instanceof CustomGeneratorSettings.ExcludeBiomeClass) {
            type = "excludeClass";
        } else if (src instanceof CustomGeneratorSettings.AllOfCompositeBiomeFilter) {
            type = "allOf";
        } else if (src instanceof CustomGeneratorSettings.AnyOfCompositeBiomeFilter) {
            type = "anyOf";
        } else if (src instanceof CustomGeneratorSettings.NoneOfCompositeBiomeFilter) {
            type = "noneOf";
        } else {
            throw new RuntimeException("Unknown type " + src.getClass());
        }
        root.put("type", new JsonPrimitive(type));

        for (Field field : src.getClass().getFields()) {
            try {
                root.put(field.getName(), marshaller.serialize(field.get(src)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return root;
    }

    public static Jankson jankson() {

        Jankson.Builder builder = Jankson.builder();
        builder.registerDeserializer(JsonObject.class, CustomGeneratorSettings.ReplacerConfig.class, CustomGenSettingsSerialization::deserializeReplacerConfig);
        builder.registerSerializer(CustomGeneratorSettings.ReplacerConfig.class, CustomGenSettingsSerialization::serializeReplacerConfig);

        builder.registerDeserializer(String.class, BiomeDesc.class, CustomGenSettingsSerialization::deserializeBiome);
        builder.registerSerializer(BiomeDesc.class, CustomGenSettingsSerialization::serializeBiome);

        builder.registerDeserializer(JsonArray.class, UserFunction.class, CustomGenSettingsSerialization::deserializeUserFunction);
        builder.registerSerializer(UserFunction.class, CustomGenSettingsSerialization::serializeUserFunction);

        builder.registerDeserializer(String.class, BlockDesc.class, CustomGenSettingsSerialization::deserializeBlock);
        builder.registerSerializer(BlockDesc.class, CustomGenSettingsSerialization::serializeBlock);

        builder.registerDeserializer(JsonElement.class, BlockStateDesc.class, CustomGenSettingsSerialization::deserializeBlockstate);
        builder.registerSerializer(BlockStateDesc.class, CustomGenSettingsSerialization::serializeBlockstate);

        builder.registerDeserializer(JsonArray.class, CubeAreas.class, CustomGenSettingsSerialization::deserializeCubeAreas);
        builder.registerSerializer(CubeAreas.class, CustomGenSettingsSerialization::serializeCubeAreas);

        builder.registerDeserializer(JsonObject.class, TreeMap.class, CustomGenSettingsSerialization::deserializeFlatCubicLayers);
        builder.registerSerializer(TreeMap.class, CustomGenSettingsSerialization::serializeCubeFlatCubicLayers);

        builder.registerDeserializer(JsonObject.class, FlatGeneratorSettings.class, CustomGenSettingsSerialization::deserializeFlatGenSettings);
        builder.registerSerializer(FlatGeneratorSettings.class, CustomGenSettingsSerialization::serializeFlatGenSettings);

        builder.registerDeserializer(JsonArray.class, StandardOreList.class, CustomGenSettingsSerialization::deserializeStandardOreList);
        builder.registerSerializer(StandardOreList.class, CustomGenSettingsSerialization::serializeStandardOreList);

        builder.registerDeserializer(JsonArray.class, PeriodicOreList.class, CustomGenSettingsSerialization::deserializePeriodicOreList);
        builder.registerSerializer(PeriodicOreList.class, CustomGenSettingsSerialization::serializePeriodicOreList);

        builder.registerDeserializer(JsonObject.class, CustomGeneratorSettings.GenerationCondition.class, CustomGenSettingsSerialization::deserializeGenerationCondition);
        builder.registerSerializer(CustomGeneratorSettings.GenerationCondition.class, CustomGenSettingsSerialization::serializeGenerationCondition);

        builder.registerDeserializer(JsonObject.class, CustomGeneratorSettings.BiomeFilter.class, CustomGenSettingsSerialization::deserializeBiomeFilter);
        builder.registerSerializer(CustomGeneratorSettings.BiomeFilter.class, CustomGenSettingsSerialization::serializeBiomeFilter);

        return builder.build();
    }

}
